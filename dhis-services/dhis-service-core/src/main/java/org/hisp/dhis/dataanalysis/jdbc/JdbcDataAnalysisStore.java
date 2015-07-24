package org.hisp.dhis.dataanalysis.jdbc;

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

import static org.hisp.dhis.common.AggregatedValue.ZERO;
import static org.hisp.dhis.system.util.ConversionUtils.getIdentifiers;
import static org.hisp.dhis.system.util.MathUtils.isEqual;
import static org.hisp.dhis.system.util.TextUtils.getCommaDelimitedString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dataanalysis.DataAnalysisStore;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.datavalue.DeflatedDataValue;
import org.hisp.dhis.jdbc.StatementBuilder;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.system.objectmapper.DeflatedDataValueNameMinMaxRowMapper;
import org.hisp.dhis.system.util.ConversionUtils;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.system.util.PaginatedList;
import org.hisp.dhis.system.util.TextUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * @author Lars Helge Overland
 * @author Halvdan Hoem Grelland
 */
public class JdbcDataAnalysisStore
    implements DataAnalysisStore
{
    private static final Log log = LogFactory.getLog( JdbcDataAnalysisStore.class );
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private StatementBuilder statementBuilder;

    public void setStatementBuilder( StatementBuilder statementBuilder )
    {
        this.statementBuilder = statementBuilder;
    }
    
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------------------------------------------------------------------------
    // OutlierAnalysisStore implementation
    // -------------------------------------------------------------------------

    @Override
    public Map<Integer, Double> getStandardDeviation( DataElement dataElement, DataElementCategoryOptionCombo categoryOptionCombo, Set<Integer> organisationUnits, Date from )
    {
        Map<Integer, Double> map = new HashMap<>();
        
        if ( organisationUnits.isEmpty() )
        {
            return map;
        }
        
        final String sql = 
            "select ou.organisationunitid, " +
              "(select stddev_pop( cast( dv.value as " + statementBuilder.getDoubleColumnType() + " ) ) " +
              "from datavalue dv " +
              "inner join period pe on dv.periodid = pe.periodid " +
              "where dv.dataelementid = " + dataElement.getId() + " " +
              "and dv.categoryoptioncomboid = " + categoryOptionCombo.getId() + " " +
              "and pe.startdate >= '" + DateUtils.getMediumDateString( from ) + "' " +
              "and dv.sourceid = ou.organisationunitid) as deviation " +
            "from organisationunit ou " +
            "where ou.organisationunitid in (" + getCommaDelimitedString( organisationUnits ) + ")";
        
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet( sql );
        
        while ( rowSet.next() )
        {
            Object stdDev = rowSet.getObject( "deviation" );
            
            if ( stdDev != null && !isEqual( (Double) stdDev, ZERO ) )
            {
                map.put( rowSet.getInt( "organisationunitid" ), (Double) stdDev );
            }
        }
        
        return map;
    }
    
    @Override
    public Map<Integer, Double> getAverage( DataElement dataElement, DataElementCategoryOptionCombo categoryOptionCombo, Set<Integer> organisationUnits, Date from )
    {
        Map<Integer, Double> map = new HashMap<>();
        
        if ( organisationUnits.isEmpty() )
        {
            return map;
        }
        
        final String sql = 
            "select ou.organisationunitid, " +
                "(select avg( cast( dv.value as " + statementBuilder.getDoubleColumnType() + " ) ) " +
                "from datavalue dv " +
                "inner join period pe on dv.periodid = pe.periodid " +
                "where dv.dataelementid = " + dataElement.getId() + " " +
                "and dv.categoryoptioncomboid = " + categoryOptionCombo.getId() + " " +
                "and pe.startdate >= '" + DateUtils.getMediumDateString( from ) + "' " +
                "and dv.sourceid = ou.organisationunitid) as average " +
            "from organisationunit ou " +
            "where ou.organisationunitid in (" + getCommaDelimitedString( organisationUnits ) + ")";
        
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet( sql );
        
        while ( rowSet.next() )
        {
            Object avg = rowSet.getObject( "average" );
            
            if ( avg != null )
            {
                map.put( rowSet.getInt( "organisationunitid" ), (Double) avg );
            }
        }
        
        return map;        
    }
    
    @Override
    public Collection<DeflatedDataValue> getMinMaxViolations( Collection<DataElement> dataElements, Collection<DataElementCategoryOptionCombo> categoryOptionCombos,
        Collection<Period> periods, Collection<OrganisationUnit> organisationUnits, int limit )
    {
        if ( dataElements.isEmpty() || categoryOptionCombos.isEmpty() || periods.isEmpty() || organisationUnits.isEmpty() )
        {
            return new ArrayList<>();
        }
        
        String dataElementIds = getCommaDelimitedString( getIdentifiers( DataElement.class, dataElements ) );
        String organisationUnitIds = getCommaDelimitedString( getIdentifiers( OrganisationUnit.class, organisationUnits ) );
        String periodIds = getCommaDelimitedString( getIdentifiers( Period.class, periods ) );
        String categoryOptionComboIds = getCommaDelimitedString( getIdentifiers( DataElementCategoryOptionCombo.class, categoryOptionCombos ) );
        
        Map<Integer, String> optionComboMap = DataElementCategoryOptionCombo.getCategoryOptionComboMap( categoryOptionCombos );
        
        //TODO persist name on category option combo and use join to improve performance
        
        String sql = 
            "select dv.dataelementid, dv.periodid, dv.sourceid, dv.categoryoptioncomboid, dv.value, dv.storedby, dv.lastupdated, " +
            "dv.created, dv.comment, dv.followup, ou.name as sourcename, de.name as dataelementname, pt.name as periodtypename, pe.startdate, pe.enddate, mm.minimumvalue, mm.maximumvalue " +
            "from datavalue dv " +
            "join minmaxdataelement mm on ( dv.dataelementid = mm.dataelementid and dv.categoryoptioncomboid = mm.categoryoptioncomboid and dv.sourceid = mm.sourceid ) " +
            "join dataelement de on dv.dataelementid = de.dataelementid " +
            "join period pe on dv.periodid = pe.periodid " +
            "join periodtype pt on pe.periodtypeid = pt.periodtypeid " +
            "join organisationunit ou on dv.sourceid = ou.organisationunitid " +
            "where dv.dataelementid in (" + dataElementIds + ") " +
            "and dv.categoryoptioncomboid in (" + categoryOptionComboIds + ") " +
            "and dv.periodid in (" + periodIds + ") " + 
            "and dv.sourceid in (" + organisationUnitIds + ") and ( " +
                "cast( dv.value as " + statementBuilder.getDoubleColumnType() + " ) < mm.minimumvalue " +
                "or cast( dv.value as " + statementBuilder.getDoubleColumnType() + " ) > mm.maximumvalue )" +
            statementBuilder.limitRecord( 0, limit );
        
        return jdbcTemplate.query( sql, new DeflatedDataValueNameMinMaxRowMapper( null, null, optionComboMap ) );
    }
    
    @Override
    public Collection<DeflatedDataValue> getDeflatedDataValues( DataElement dataElement, DataElementCategoryOptionCombo categoryOptionCombo,
        Collection<Period> periods, Map<Integer, Integer> lowerBoundMap, Map<Integer, Integer> upperBoundMap )
    {
        if ( lowerBoundMap == null || lowerBoundMap.isEmpty() || periods.isEmpty() )
        {
            return new ArrayList<>();
        }
        
        //TODO parallel processes
                
        List<List<Integer>> organisationUnitPages = new PaginatedList<>( lowerBoundMap.keySet() ).setPageSize( 100 ).getPages();
        
        log.debug( "No of pages: " + organisationUnitPages.size() );
        
        Collection<DeflatedDataValue> dataValues = new ArrayList<>();
        
        for ( List<Integer> unitPage : organisationUnitPages )
        {
            dataValues.addAll( getDeflatedDataValues( dataElement, categoryOptionCombo, periods, unitPage, lowerBoundMap, upperBoundMap ) );
        }
        
        return dataValues;
    }
    
    private Collection<DeflatedDataValue> getDeflatedDataValues( DataElement dataElement, DataElementCategoryOptionCombo categoryOptionCombo,
        Collection<Period> periods, List<Integer> organisationUnits, Map<Integer, Integer> lowerBoundMap, Map<Integer, Integer> upperBoundMap )
    {
        String periodIds = TextUtils.getCommaDelimitedString( ConversionUtils.getIdentifiers( Period.class, periods ) );
        
        String sql = 
            "select dv.dataelementid, dv.periodid, dv.sourceid, dv.categoryoptioncomboid, dv.value, dv.storedby, dv.lastupdated, " +
            "dv.created, dv.comment, dv.followup, ou.name as sourcename, " +
            "'" + dataElement.getName() + "' as dataelementname, pt.name as periodtypename, pe.startdate, pe.enddate, " + 
            "'" + categoryOptionCombo.getName() + "' as categoryoptioncomboname " +
            "from datavalue dv " +
            "join period pe on dv.periodid = pe.periodid " +
            "join periodtype pt on pe.periodtypeid = pt.periodtypeid " +
            "join organisationunit ou on dv.sourceid = ou.organisationunitid " +
            "where dv.dataelementid = " + dataElement.getId() + " " +
            "and dv.categoryoptioncomboid = " + categoryOptionCombo.getId() + " " +
            "and dv.periodid in (" + periodIds + ") and ( ";
        
        for ( Integer organisationUnit : organisationUnits )
        {
            sql += "( dv.sourceid = " + organisationUnit + " " +
                "and ( cast( dv.value as " + statementBuilder.getDoubleColumnType() + " ) < " + lowerBoundMap.get( organisationUnit ) + " " +
                "or cast( dv.value as " + statementBuilder.getDoubleColumnType() + " ) > " + upperBoundMap.get( organisationUnit ) + " ) ) or ";
        }
        
        sql = sql.substring( 0, ( sql.length() - 3 ) ) + " )";
        
        return jdbcTemplate.query( sql, new DeflatedDataValueNameMinMaxRowMapper( lowerBoundMap, upperBoundMap, null ) );
    }

    @Override
    public Collection<DeflatedDataValue> getFollowupDataValues( OrganisationUnit organisationUnit, int limit )
    {
        final String idLevelColumn = "idlevel" + organisationUnit.getOrganisationUnitLevel();

        String sql =
            "select dv.dataelementid, dv.periodid, dv.sourceid, dv.categoryoptioncomboid, dv.value, " +
            "dv.storedby, dv.lastupdated, dv.created, dv.comment, dv.followup, mm.minimumvalue, mm.maximumvalue, de.name AS dataelementname, " +
            "pe.startdate, pe.enddate, pt.name AS periodtypename, ou.name AS sourcename, cc.categoryoptioncomboname " +
            "from datavalue dv " +
            "left join minmaxdataelement mm on (dv.sourceid = mm.sourceid and dv.dataelementid = mm.dataelementid and dv.categoryoptioncomboid = mm.categoryoptioncomboid) " +
            "join dataelement de on dv.dataelementid = de.dataelementid " +
            "join period pe on dv.periodid = pe.periodid " +
            "join periodtype pt on pe.periodtypeid = pt.periodtypeid " +
            "left join organisationunit ou on ou.organisationunitid = dv.sourceid " +
            "left join _categoryoptioncomboname cc on dv.categoryoptioncomboid = cc.categoryoptioncomboid " +
            "inner join _orgunitstructure ous on ous.organisationunitid = dv.sourceid " +
            "where ous." + idLevelColumn + " = " + organisationUnit.getId() + " " +
            "and dv.followup = true " +
            statementBuilder.limitRecord( 0, limit );
        
        return jdbcTemplate.query( sql, new DeflatedDataValueNameMinMaxRowMapper() );
    }
}
