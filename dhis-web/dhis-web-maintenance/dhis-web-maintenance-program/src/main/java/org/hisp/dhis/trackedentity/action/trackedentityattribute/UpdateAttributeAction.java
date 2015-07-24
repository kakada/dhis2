package org.hisp.dhis.trackedentity.action.trackedentityattribute;

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

import com.opensymphony.xwork2.Action;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.attribute.AttributeService;
import org.hisp.dhis.legend.LegendService;
import org.hisp.dhis.option.OptionService;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.system.util.AttributeUtils;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class UpdateAttributeAction
    implements Action
{
    private final Integer SCOPE_ORGUNIT = 1;

    private final Integer SCOPE_PROGRAM = 2;

    private final Integer SCOPE_PROGRAM_IN_ORGUNIT = 3;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityAttributeService trackedEntityAttributeService;

    public void setTrackedEntityAttributeService( TrackedEntityAttributeService trackedEntityAttributeService )
    {
        this.trackedEntityAttributeService = trackedEntityAttributeService;
    }

    @Autowired
    private TrackedEntityService trackedEntityService;

    @Autowired
    private OptionService optionService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private LegendService legendService;

    @Autowired
    private AttributeService attributeService;

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private int id;

    public void setId( int id )
    {
        this.id = id;
    }

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private String shortName;

    public void setShortName( String shortName )
    {
        this.shortName = shortName;
    }

    private String code;

    public void setCode( String code )
    {
        this.code = code;
    }

    private String description;

    public void setDescription( String description )
    {
        this.description = description;
    }

    private String valueType;

    public void setValueType( String valueType )
    {
        this.valueType = valueType;
    }

    private Boolean unique;

    public void setUnique( Boolean unique )
    {
        this.unique = unique;
    }

    private Integer optionSetId;

    public void setOptionSetId( Integer optionSetId )
    {
        this.optionSetId = optionSetId;
    }

    private Integer trackedEntityId;

    public void setTrackedEntityId( Integer trackedEntityId )
    {
        this.trackedEntityId = trackedEntityId;
    }

    private Integer legendSetId;

    public void setLegendSetId( Integer legendSetId )
    {
        this.legendSetId = legendSetId;
    }

    private Boolean inherit;

    public void setInherit( Boolean inherit )
    {
        this.inherit = inherit;
    }

    private String expression;

    public void setExpression( String expression )
    {
        this.expression = expression;
    }

    private Integer scope;

    public void setScope( Integer scope )
    {
        this.scope = scope;
    }

    private Boolean confidential;

    public void setConfidential( Boolean confidential )
    {
        this.confidential = confidential;
    }

    private List<String> jsonAttributeValues;

    public void setJsonAttributeValues( List<String> jsonAttributeValues )
    {
        this.jsonAttributeValues = jsonAttributeValues;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        TrackedEntityAttribute trackedEntityAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( id );

        trackedEntityAttribute.setName( name );
        trackedEntityAttribute.setShortName( shortName );
        trackedEntityAttribute.setCode( StringUtils.isEmpty( code.trim() ) ? null : code );
        trackedEntityAttribute.setDescription( description );
        trackedEntityAttribute.setValueType( valueType );
        trackedEntityAttribute.setExpression( expression );
        trackedEntityAttribute.setDisplayOnVisitSchedule( false );

        unique = (unique == null) ? false : true;
        trackedEntityAttribute.setUnique( unique );

        inherit = (inherit == null) ? false : true;
        trackedEntityAttribute.setInherit( inherit );

        confidential = (confidential == null) ? false : true;
        trackedEntityAttribute.setConfidential( confidential );

        if ( unique )
        {
            boolean orgunitScope = false;
            boolean programScope = false;
            if ( scope != null && (scope == SCOPE_ORGUNIT || scope == SCOPE_PROGRAM_IN_ORGUNIT) )
            {
                orgunitScope = true;
            }

            if ( scope != null && (scope == SCOPE_PROGRAM || scope == SCOPE_PROGRAM_IN_ORGUNIT) )
            {
                programScope = true;
            }

            trackedEntityAttribute.setOrgunitScope( orgunitScope );
            trackedEntityAttribute.setProgramScope( programScope );
        }
        else if ( valueType.equals( TrackedEntityAttribute.TYPE_OPTION_SET ) )
        {
            trackedEntityAttribute.setOptionSet( optionService.getOptionSet( optionSetId ) );
        }
        else if ( valueType.equals( TrackedEntityAttribute.TYPE_TRACKER_ASSOCIATE ) )
        {
            trackedEntityAttribute.setTrackedEntity( trackedEntityService.getTrackedEntity( trackedEntityId ) );
        }

        if ( legendSetId != null )
        {
            trackedEntityAttribute.setLegendSet( legendService.getLegendSet( legendSetId ) );
        }

        if ( jsonAttributeValues != null )
        {
            AttributeUtils.updateAttributeValuesFromJson( trackedEntityAttribute.getAttributeValues(), jsonAttributeValues,
                attributeService );
        }

        trackedEntityAttributeService.updateTrackedEntityAttribute( trackedEntityAttribute );

        return SUCCESS;
    }
}
