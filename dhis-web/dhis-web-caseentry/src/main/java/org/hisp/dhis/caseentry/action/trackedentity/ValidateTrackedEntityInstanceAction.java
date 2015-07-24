package org.hisp.dhis.caseentry.action.trackedentity;

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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.validation.ValidationCriteriaService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 */
public class ValidateTrackedEntityInstanceAction
    implements Action
{
    public static final String PREFIX_ATTRIBUTE = "attr";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityInstanceService entityInstanceService;

    private ProgramService programService;

    private TrackedEntityAttributeService attributeService;

    private OrganisationUnitSelectionManager selectionManager;

    @Autowired
    private TrackedEntityAttributeService patientAttributeService;

    @Autowired
    private ValidationCriteriaService validationCriteriaService;

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private Integer id;

    private String programId;

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private String message;

    private Map<String, String> attributeValueMap = new HashMap<>();

    private Collection<TrackedEntityInstance> entityInstances;

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
    {
        OrganisationUnit orgunit = selectionManager.getSelectedOrganisationUnit();
        Program program = null;
        if ( programId != null )
        {
            program = programService.getProgram( programId );
        }

        TrackedEntityInstance entityInstance = null;
        if ( id != null )
        {
            entityInstance = entityInstanceService.getTrackedEntityInstance( id );
        }
        else
        {
            entityInstance = new TrackedEntityInstance();
        }

        entityInstance.setOrganisationUnit( orgunit );

        HttpServletRequest request = ServletActionContext.getRequest();

        Collection<TrackedEntityAttribute> attributes = attributeService.getAllTrackedEntityAttributes();

        if ( attributes != null && attributes.size() > 0 )
        {
            String value = null;

            Set<TrackedEntityAttributeValue> attributeValues = new HashSet<>();

            for ( TrackedEntityAttribute attribute : attributes )
            {
                value = request.getParameter( PREFIX_ATTRIBUTE + attribute.getId() );
                if ( StringUtils.isNotBlank( value ) )
                {
                    TrackedEntityAttributeValue attributeValue = new TrackedEntityAttributeValue();
                    attributeValue.setEntityInstance( entityInstance );
                    attributeValue.setAttribute( attribute );
                    attributeValue.setValue( value );
                    attributeValues.add( attributeValue );
                }
            }

            entityInstance.setAttributeValues( attributeValues );
        }

        // ---------------------------------------------------------------------
        // Validate entityInstance
        // ---------------------------------------------------------------------

        String[] errorCode = entityInstanceService.validateTrackedEntityInstance( entityInstance, program, format )
            .split( "_" );
        int code = Integer.parseInt( errorCode[0] );

        if ( code == TrackedEntityInstanceService.ERROR_DUPLICATE_IDENTIFIER )
        {
            message = i18n.getString( "duplicate_value_of" ) + " "
                + attributeService.getTrackedEntityAttribute( Integer.parseInt( errorCode[1] ) ).getDisplayName();
        }
        else if ( code == TrackedEntityInstanceService.ERROR_ENROLLMENT )
        {
            message = i18n.getString( "violate_validation" ) + " "
                + validationCriteriaService.getValidationCriteria( Integer.parseInt( errorCode[1] ) ).getDisplayName();
        }

        return SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Getter/Setter
    // -------------------------------------------------------------------------

    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }

    public void setProgramId( String programId )
    {
        this.programId = programId;
    }

    public Collection<TrackedEntityInstance> getEntityInstances()
    {
        return entityInstances;
    }

    public void setAttributeService( TrackedEntityAttributeService attributeService )
    {
        this.attributeService = attributeService;
    }

    public void setEntityInstanceService( TrackedEntityInstanceService entityInstanceService )
    {
        this.entityInstanceService = entityInstanceService;
    }

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    public String getMessage()
    {
        return message;
    }

    public Map<String, String> getAttributeValueMap()
    {
        return attributeValueMap;
    }

    public void setId( Integer id )
    {
        this.id = id;
    }

}
