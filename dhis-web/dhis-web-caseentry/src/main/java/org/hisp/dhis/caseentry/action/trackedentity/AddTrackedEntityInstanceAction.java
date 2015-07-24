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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.relationship.Relationship;
import org.hisp.dhis.relationship.RelationshipService;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.relationship.RelationshipTypeService;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentity.TrackedEntityService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class AddTrackedEntityInstanceAction
    implements Action
{
    public static final String PREFIX_ATTRIBUTE = "attr";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityInstanceService entityInstanceService;

    private TrackedEntityAttributeService attributeService;

    private RelationshipTypeService relationshipTypeService;

    private RelationshipService relationshipService;

    private OrganisationUnitSelectionManager selectionManager;

    @Autowired
    private TrackedEntityService trackedEntityService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private TrackedEntityAttributeValueService attributeValueService;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String representativeId;

    private Integer relationshipTypeId;

    private Integer relationshipId;

    private boolean relationshipFromA;

    private Integer trackedEntityId;

    private String programId;

    private String message;

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
    {
        OrganisationUnit organisationUnit = selectionManager.getSelectedOrganisationUnit();
        TrackedEntityInstance entityInstance = new TrackedEntityInstance();
        TrackedEntity trackedEntity = null;

        if ( programId != null )
        {
            Program program = programService.getProgram( programId );
            trackedEntity = program.getTrackedEntity();
        }
        else
        {
            trackedEntity = trackedEntityService.getTrackedEntity( trackedEntityId );
        }

        entityInstance.setTrackedEntity( trackedEntity );
        entityInstance.setOrganisationUnit( organisationUnit );

        // ---------------------------------------------------------------------
        // Tracked Entity Attributes
        // ---------------------------------------------------------------------

        TrackedEntityInstance relationship = null;

        if ( relationshipId != null && relationshipTypeId != null )
        {
            relationship = entityInstanceService.getTrackedEntityInstance( relationshipId );
        }

        HttpServletRequest request = ServletActionContext.getRequest();

        Collection<TrackedEntityAttribute> attributes = attributeService.getAllTrackedEntityAttributes();

        Set<TrackedEntityAttributeValue> attributeValues = new HashSet<>();

        TrackedEntityAttributeValue attributeValue = null;

        if ( attributes != null && attributes.size() > 0 )
        {
            for ( TrackedEntityAttribute attribute : attributes )
            {
                String value = request.getParameter( PREFIX_ATTRIBUTE + attribute.getId() );
                if ( StringUtils.isNotBlank( value ) )
                {
                    attributeValue = new TrackedEntityAttributeValue();
                    attributeValue.setEntityInstance( entityInstance );
                    attributeValue.setAttribute( attribute );

                    attributeValue.setValue( value.trim() );
                    attributeValues.add( attributeValue );
                }
                else if ( attribute.getInherit() && relationship != null )
                {
                    TrackedEntityAttributeValue av = attributeValueService.getTrackedEntityAttributeValue(
                        relationship, attribute );
                    if ( av != null )
                    {
                        attributeValue = new TrackedEntityAttributeValue();
                        attributeValue.setEntityInstance( entityInstance );
                        attributeValue.setAttribute( attribute );
                        attributeValue.setValue( av.getValue() );

                        attributeValues.add( attributeValue );
                    }
                }
            }
        }

        int entityInstanceId = entityInstanceService.createTrackedEntityInstance( entityInstance, representativeId,
            relationshipTypeId, attributeValues );

        // -------------------------------------------------------------------------
        // Create relationship
        // -------------------------------------------------------------------------

        if ( relationship != null )
        {
            Relationship rel = new Relationship();
            if ( relationshipFromA )
            {
                rel.setEntityInstanceA( relationship );
                rel.setEntityInstanceB( entityInstance );
            }
            else
            {
                rel.setEntityInstanceA( entityInstance );
                rel.setEntityInstanceB( relationship );
            }
            if ( relationshipTypeId != null )
            {
                RelationshipType relType = relationshipTypeService.getRelationshipType( relationshipTypeId );
                if ( relType != null )
                {
                    rel.setRelationshipType( relType );
                    relationshipService.addRelationship( rel );
                }
            }
        }

        message = entityInstance.getUid() + "_" + entityInstanceId;

        return SUCCESS;
    }

    // -----------------------------------------------------------------------------
    // Getter/Setter
    // -----------------------------------------------------------------------------

    public void setRelationshipTypeService( RelationshipTypeService relationshipTypeService )
    {
        this.relationshipTypeService = relationshipTypeService;
    }

    public void setRelationshipId( Integer relationshipId )
    {
        this.relationshipId = relationshipId;
    }

    public void setRelationshipFromA( boolean relationshipFromA )
    {
        this.relationshipFromA = relationshipFromA;
    }

    public void setRelationshipService( RelationshipService relationshipService )
    {
        this.relationshipService = relationshipService;
    }

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    public void setTrackedEntityId( Integer trackedEntityId )
    {
        this.trackedEntityId = trackedEntityId;
    }

    public void setProgramId( String programId )
    {
        this.programId = programId;
    }

    public String getMessage()
    {
        return message;
    }

    public void setEntityInstanceService( TrackedEntityInstanceService entityInstanceService )
    {
        this.entityInstanceService = entityInstanceService;
    }

    public void setAttributeService( TrackedEntityAttributeService attributeService )
    {
        this.attributeService = attributeService;
    }

    public void setRepresentativeId( String representativeId )
    {
        this.representativeId = representativeId;
    }

    public void setRelationshipTypeId( Integer relationshipTypeId )
    {
        this.relationshipTypeId = relationshipTypeId;
    }

}
