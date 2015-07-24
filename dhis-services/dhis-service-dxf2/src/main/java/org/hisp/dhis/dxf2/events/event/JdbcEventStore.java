package org.hisp.dhis.dxf2.events.event;

/*
 * Copyright (c) 2004-2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdList;
import static org.hisp.dhis.system.util.DateUtils.getMediumDateString;
import static org.hisp.dhis.system.util.TextUtils.getCommaDelimitedString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dxf2.common.IdSchemes;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.system.util.SqlHelper;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.util.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class JdbcEventStore
    implements EventStore
{
    private static final Log log = LogFactory.getLog( JdbcEventStore.class );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TrackedEntityInstanceService entityInstanceService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Event> getEvents( EventSearchParams params, List<OrganisationUnit> organisationUnits )
    {
        List<Event> events = new ArrayList<>();

        String sql = buildSql( params, organisationUnits );
        
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet( sql );

        log.debug( "Event query SQL: " + sql );

        Event event = new Event();
        
        event.setEvent( "not_valid" );

        Set<String> notes = new HashSet<>();
        
        IdSchemes idSchemes = ObjectUtils.firstNonNull( params.getIdSchemes(), new IdSchemes() );

        while ( rowSet.next() )
        {
            if ( rowSet.getString( "psi_uid" ) == null )
            {
                continue;
            }

            if ( !event.getEvent().equals( rowSet.getString( "psi_uid" ) ) )
            {
                event = new Event();

                event.setEvent( rowSet.getString( "psi_uid" ) );
                event.setTrackedEntityInstance( rowSet.getString( "tei_uid" ) );
                event.setStatus( EventStatus.valueOf( rowSet.getString( "psi_status" ) ) );

                event.setProgram( IdSchemes.getValue( rowSet.getString( "p_uid" ), rowSet.getString( "p_code" ), idSchemes.getProgramIdScheme() ) );
                event.setProgramStage( IdSchemes.getValue( rowSet.getString( "ps_uid" ), rowSet.getString( "ps_code" ), idSchemes.getProgramStageIdScheme() ) );
                event.setOrgUnit( IdSchemes.getValue( rowSet.getString( "ou_uid" ), rowSet.getString( "ou_code" ), idSchemes.getOrgUnitIdScheme() ) );

                if ( rowSet.getInt( "p_type" ) != Program.SINGLE_EVENT_WITHOUT_REGISTRATION )
                {
                    event.setEnrollment( rowSet.getString( "pi_uid" ) );
                    event.setEnrollmentStatus( EventStatus.fromInt( rowSet.getInt( "pi_status" ) ) );
                    event.setFollowup( rowSet.getBoolean( "pi_followup" ) );
                }

                event.setTrackedEntityInstance( rowSet.getString( "tei_uid" ) );

                event.setStoredBy( rowSet.getString( "psi_completeduser" ) );
                event.setOrgUnitName( rowSet.getString( "ou_name" ) );
                event.setDueDate( DateUtils.getLongDateString( rowSet.getDate( "psi_duedate" ) ) );
                event.setEventDate( DateUtils.getLongDateString( rowSet.getDate( "psi_executiondate" ) ) );
                event.setCreated( DateUtils.getLongDateString( rowSet.getDate( "psi_created" ) ) );
                event.setLastUpdated( DateUtils.getLongDateString( rowSet.getDate( "psi_lastupdated" ) ) );

                if ( rowSet.getBoolean( "ps_capturecoordinates" ) )
                {
                    Double longitude = rowSet.getDouble( "psi_longitude" );
                    Double latitude = rowSet.getDouble( "psi_latitude" );

                    if ( longitude != null && latitude != null )
                    {
                        Coordinate coordinate = new Coordinate( longitude, latitude );

                        try
                        {
                            List<Double> list = objectMapper.readValue( coordinate.getCoordinateString(),
                                new TypeReference<List<Double>>()
                                {
                                } );

                            coordinate.setLongitude( list.get( 0 ) );
                            coordinate.setLatitude( list.get( 1 ) );
                        }
                        catch ( IOException ignored )
                        {
                        }

                        if ( coordinate.isValid() )
                        {
                            event.setCoordinate( coordinate );
                        }
                    }
                }

                events.add( event );
            }

            if ( rowSet.getString( "pdv_value" ) != null && rowSet.getString( "de_uid" ) != null )
            {
                DataValue dataValue = new DataValue();
                dataValue.setValue( rowSet.getString( "pdv_value" ) );
                dataValue.setProvidedElsewhere( rowSet.getBoolean( "pdv_providedelsewhere" ) );
                dataValue.setDataElement( IdSchemes.getValue( rowSet.getString( "de_uid" ), rowSet.getString( "de_code" ), idSchemes.getDataElementIdScheme() ) );

                dataValue.setStoredBy( rowSet.getString( "pdv_storedby" ) );

                event.getDataValues().add( dataValue );
            }

            if ( rowSet.getString( "psinote_value" ) != null && !notes.contains( rowSet.getString( "psinote_id" ) ) )
            {
                Note note = new Note();
                note.setValue( rowSet.getString( "psinote_value" ) );
                note.setStoredDate( rowSet.getString( "psinote_storeddate" ) );
                note.setStoredBy( rowSet.getString( "psinote_storedby" ) );

                event.getNotes().add( note );
                notes.add( rowSet.getString( "psinote_id" ) );
            }
        }

        return events;
    }

    public int getEventCount( EventSearchParams params, List<OrganisationUnit> organisationUnits )
    {
        String sql = getEventSelectQuery( params, organisationUnits );
        
        sql = sql.replaceFirst( "select .*? from", "select count(*) from" );

        log.info( "Event query count SQL: " + sql );

        return jdbcTemplate.queryForObject( sql, Integer.class );
    }
    
    /**
     * Query is based on three sub queries on event, data value and comment, which 
     * are joined using program stage instance id. The purpose of the separate
     * queries is to be able to page properly on events.
     */
    private String buildSql( EventSearchParams params, List<OrganisationUnit> organisationUnits )
    {
        String sql = "select * from (";
        
        sql += getEventSelectQuery( params, organisationUnits );
        
        sql += getEventPagingQuery( params );
        
        sql += ") as event left join (";
        
        sql += getDataValueQuery();
        
        sql += ") as dv on event.psi_id=dv.pdv_id left join (";
        
        sql += getCommentQuery();
        
        sql += ") as cm on event.psi_id=cm.psic_id ";
        
        sql += "order by psi_uid desc ";
        
        return sql;
    }
    
    private String getEventSelectQuery( EventSearchParams params, List<OrganisationUnit> organisationUnits )
    {
        List<Integer> orgUnitIds = getIdList( organisationUnits );

        SqlHelper hlp = new SqlHelper();

        String sql =
            "select psi.programstageinstanceid as psi_id, psi.uid as psi_uid, psi.status as psi_status, psi.executiondate as psi_executiondate, psi.duedate as psi_duedate, psi.completeduser as psi_completeduser, " +
            "psi.longitude as psi_longitude, psi.latitude as psi_latitude, psi.created as psi_created, psi.lastupdated as psi_lastupdated, " +
            "pi.uid as pi_uid, pi.status as pi_status, pi.followup as pi_followup, p.uid as p_uid, p.code as p_code, " +
            "p.type as p_type, ps.uid as ps_uid, ps.code as ps_code, ps.capturecoordinates as ps_capturecoordinates, " +
            "ou.uid as ou_uid, ou.code as ou_code, ou.name as ou_name, tei.uid as tei_uid " +
            "from programstageinstance psi " +
            "inner join programinstance pi on pi.programinstanceid=psi.programinstanceid " +
            "inner join program p on p.programid=pi.programid " +
            "inner join programstage ps on ps.programstageid=psi.programstageid " +
            "left join trackedentityinstance tei on tei.trackedentityinstanceid=pi.trackedentityinstanceid ";            

        if ( params.getEventStatus() == null || EventStatus.isExistingEvent( params.getEventStatus() ) )
        {
            sql += "left join organisationunit ou on (psi.organisationunitid=ou.organisationunitid) ";
        }
        else
        {
            sql += "left join organisationunit ou on (tei.organisationunitid=ou.organisationunitid) ";
        }
                
        if ( params.getTrackedEntityInstance() != null )
        {
            sql += hlp.whereAnd() + " tei.trackedentityinstanceid=" + params.getTrackedEntityInstance().getId() + " ";
        }

        if ( params.getProgram() != null )
        {
            sql += hlp.whereAnd() + " p.programid = " + params.getProgram().getId() + " ";
        }

        if ( params.getProgramStage() != null )
        {
            sql += hlp.whereAnd() + " ps.programstageid = " + params.getProgramStage().getId() + " ";
        }

        if ( params.getProgramStatus() != null )
        {
            sql += hlp.whereAnd() + " pi.status = " + params.getProgramStatus().getValue() + " ";
        }

        if ( params.getFollowUp() != null )
        {
            sql += hlp.whereAnd() + " pi.followup is " + ( params.getFollowUp() ? "true" : "false" ) + " ";
        }
        
        if ( params.getLastUpdated() != null )
        {
            sql += hlp.whereAnd() + " psi.lastupdated > '" + DateUtils.getLongDateString( params.getLastUpdated() ) + "' ";
        }

        if ( params.getEventStatus() == null || EventStatus.isExistingEvent( params.getEventStatus() ) )
        {
            if ( orgUnitIds != null && !orgUnitIds.isEmpty() )
            {
                sql += hlp.whereAnd() + " psi.organisationunitid in (" + getCommaDelimitedString( orgUnitIds ) + ") ";
            }

            if ( params.getStartDate() != null )
            {
                sql += hlp.whereAnd() + " psi.executiondate >= '" + getMediumDateString( params.getStartDate() ) + "' ";
            }

            if ( params.getEndDate() != null )
            {
                sql += hlp.whereAnd() + " psi.executiondate <= '" + getMediumDateString( params.getEndDate() ) + "' ";
            }
        }
        else
        {
            if ( orgUnitIds != null && !orgUnitIds.isEmpty() )
            {
                sql += hlp.whereAnd() + " tei.organisationunitid in (" + getCommaDelimitedString( orgUnitIds ) + ") ";
            }

            if ( params.getStartDate() != null )
            {
                sql += hlp.whereAnd() + " psi.duedate >= '" + getMediumDateString( params.getStartDate() ) + "' ";
            }

            if ( params.getEndDate() != null )
            {
                sql += hlp.whereAnd() + " psi.duedate <= '" + getMediumDateString( params.getEndDate() ) + "' ";
            }

            if ( params.getEventStatus() == EventStatus.VISITED )
            {
                sql = "and psi.status = '" + EventStatus.ACTIVE.name() + "' and psi.executiondate is not null ";
            }
            else if ( params.getEventStatus() == EventStatus.COMPLETED )
            {
                sql = "and psi.status = '" + EventStatus.COMPLETED.name() + "' ";
            }
            else if ( params.getEventStatus() == EventStatus.SCHEDULE )
            {
                sql += "and psi.executiondate is null and date(now()) <= date(psi.duedate) and psi.status = '" + 
                    EventStatus.SCHEDULE.name() + "' ";
            }
            else if ( params.getEventStatus() == EventStatus.OVERDUE )
            {
                sql += "and psi.executiondate is null and date(now()) > date(psi.duedate) and psi.status = '" + 
                    EventStatus.SCHEDULE.name() + "' ";
            }
            else
            {
                sql += "and psi.status = '" + params.getEventStatus().name() + "' ";
            }
        }

        return sql;
    }

    private String getEventPagingQuery( EventSearchParams params )
    {
        String sql = " ";

        if ( params.isPaging() )
        {
            sql += "limit " + params.getPageSizeWithDefault() + " offset " + params.getOffset() + " ";
        }

        return sql;
    }
    
    private String getDataValueQuery()
    {
        String sql =
            "select pdv.programstageinstanceid as pdv_id, pdv.value as pdv_value, pdv.storedby as pdv_storedby, pdv.providedelsewhere as pdv_providedelsewhere, " +
            "de.uid as de_uid, de.code as de_code " +
            "from trackedentitydatavalue pdv " +
            "inner join dataelement de on pdv.dataelementid=de.dataelementid ";
        
        return sql;
    }
    
    private String getCommentQuery()
    {
        String sql =
            "select psic.programstageinstanceid as psic_id, psinote.trackedentitycommentid as psinote_id, psinote.commenttext as psinote_value, " +
            "psinote.createddate as psinote_storeddate, psinote.creator as psinote_storedby " +
            "from programstageinstancecomments psic " +
            "inner join trackedentitycomment psinote on psic.trackedentitycommentid=psinote.trackedentitycommentid ";
        
        return sql;
    }
}
