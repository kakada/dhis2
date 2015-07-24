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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.relationship.Relationship;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.relationship.RelationshipTypeService;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeGroup;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeGroupService;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityForm;
import org.hisp.dhis.trackedentity.TrackedEntityFormService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentity.TrackedEntityService;
import org.hisp.dhis.trackedentity.comparator.TrackedEntityAttributeGroupSortOrderComparator;
import org.hisp.dhis.trackedentity.comparator.TrackedEntityAttributeSortOrderInListNoProgramComparator;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.user.User;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class GetTrackedEntityInstanceAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityInstanceService entityInstanceService;

    public void setEntityInstanceService( TrackedEntityInstanceService entityInstanceService )
    {
        this.entityInstanceService = entityInstanceService;
    }

    private ProgramService programService;

    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }

    private RelationshipTypeService relationshipTypeService;

    public void setRelationshipTypeService( RelationshipTypeService relationshipTypeService )
    {
        this.relationshipTypeService = relationshipTypeService;
    }

    private TrackedEntityFormService trackedEntityFormService;

    public void setTrackedEntityFormService( TrackedEntityFormService trackedEntityFormService )
    {
        this.trackedEntityFormService = trackedEntityFormService;
    }

    private TrackedEntityAttributeGroupService attributeGroupService;

    public void setAttributeGroupService( TrackedEntityAttributeGroupService attributeGroupService )
    {
        this.attributeGroupService = attributeGroupService;
    }

    private TrackedEntityAttributeService attributeService;

    public void setAttributeService( TrackedEntityAttributeService attributeService )
    {
        this.attributeService = attributeService;
    }

    @Autowired
    private TrackedEntityService trackedEntityService;

    @Autowired
    private ProgramInstanceService programInstanceService;

    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private String id;

    public void setId( String id )
    {
        this.id = id;
    }

    private Collection<RelationshipType> relationshipTypes;

    public Collection<RelationshipType> getRelationshipTypes()
    {
        return relationshipTypes;
    }

    private TrackedEntityInstance entityInstance;

    public TrackedEntityInstance getEntityInstance()
    {
        return entityInstance;
    }

    private Collection<Program> programs;

    public Collection<Program> getPrograms()
    {
        return programs;
    }

    private Map<Integer, String> attributeValueMap = new HashMap<>();

    public Map<Integer, String> getAttributeValueMap()
    {
        return attributeValueMap;
    }

    private Collection<TrackedEntityAttribute> noGroupAttributes = new HashSet<>();

    public Collection<TrackedEntityAttribute> getNoGroupAttributes()
    {
        return noGroupAttributes;
    }

    private List<TrackedEntityAttributeGroup> attributeGroups;

    public List<TrackedEntityAttributeGroup> getAttributeGroups()
    {
        return attributeGroups;
    }

    private Relationship relationship;

    public Relationship getRelationship()
    {
        return relationship;
    }

    private Map<Integer, Collection<TrackedEntityAttribute>> attributeGroupsMap = new HashMap<>();

    public Map<Integer, Collection<TrackedEntityAttribute>> getAttributeGroupsMap()
    {
        return attributeGroupsMap;
    }

    private Collection<User> healthWorkers;

    public Collection<User> getHealthWorkers()
    {
        return healthWorkers;
    }

    public void setTrackedEntityForm( TrackedEntityForm trackedEntityForm )
    {
        this.trackedEntityForm = trackedEntityForm;
    }

    private String programId;

    public void setProgramId( String programId )
    {
        this.programId = programId;
    }

    private Map<String, List<TrackedEntityAttribute>> attributesMap = new HashMap<>();

    public Map<String, List<TrackedEntityAttribute>> getAttributesMap()
    {
        return attributesMap;
    }

    private TrackedEntityForm trackedEntityForm;

    public TrackedEntityForm getTrackedEntityForm()
    {
        return trackedEntityForm;
    }

    private String customRegistrationForm;

    public String getCustomRegistrationForm()
    {
        return customRegistrationForm;
    }

    private Integer programStageInstanceId;

    public Integer getProgramStageInstanceId()
    {
        return programStageInstanceId;
    }

    public void setProgramStageInstanceId( Integer programStageInstanceId )
    {
        this.programStageInstanceId = programStageInstanceId;
    }

    private List<TrackedEntity> trackedEntities;

    public List<TrackedEntity> getTrackedEntities()
    {
        return trackedEntities;
    }

    private Map<Integer, Boolean> mandatoryMap = new HashMap<>();

    public Map<Integer, Boolean> getMandatoryMap()
    {
        return mandatoryMap;
    }

    private Map<Integer, Boolean> allowFutureDateMap = new HashMap<>();

    public void setAllowFutureDateMap( Map<Integer, Boolean> allowFutureDateMap )
    {
        this.allowFutureDateMap = allowFutureDateMap;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        relationshipTypes = relationshipTypeService.getAllRelationshipTypes();
        trackedEntities = new ArrayList<>( trackedEntityService.getAllTrackedEntity() );
        entityInstance = entityInstanceService.getTrackedEntityInstance( id );

        healthWorkers = entityInstance.getOrganisationUnit().getUsers();
        Program program = null;

        if ( programId == null || programId.isEmpty() )
        {
            trackedEntityForm = trackedEntityFormService.getFormsWithoutProgram();

            if ( trackedEntityForm != null && trackedEntityForm.getDataEntryForm() != null )
            {
                customRegistrationForm = trackedEntityFormService.prepareDataEntryFormForAdd( trackedEntityForm
                    .getDataEntryForm().getHtmlCode(), trackedEntityForm.getProgram(), healthWorkers, entityInstance,
                    null, i18n, format );
            }
        }
        else
        {
            program = programService.getProgram( programId );
            trackedEntityForm = trackedEntityFormService.getFormsWithProgram( program );

            Collection<ProgramInstance> programInstances = programInstanceService.getProgramInstances( entityInstance,
                program, ProgramInstance.STATUS_ACTIVE );

            ProgramInstance programInstance = null;

            if ( programInstances != null && !programInstances.isEmpty() )
            {
                programInstance = programInstances.iterator().next();
            }
            if ( trackedEntityForm != null && trackedEntityForm.getDataEntryForm() != null )
            {
                customRegistrationForm = trackedEntityFormService.prepareDataEntryFormForAdd( trackedEntityForm
                    .getDataEntryForm().getHtmlCode(), trackedEntityForm.getProgram(), healthWorkers, entityInstance,
                    programInstance, i18n, format );
            }
        }

        List<TrackedEntityAttribute> attributes = new ArrayList<>();

        if ( customRegistrationForm == null )
        {
            attributeGroups = new ArrayList<>(
                attributeGroupService.getAllTrackedEntityAttributeGroups() );
            Collections.sort( attributeGroups, new TrackedEntityAttributeGroupSortOrderComparator() );

            if ( program == null )
            {
                attributes = new ArrayList<>(
                    attributeService.getTrackedEntityAttributesDisplayInList() );
                Collections.sort( attributes, new TrackedEntityAttributeSortOrderInListNoProgramComparator() );

                for ( TrackedEntityAttribute attribute : attributes )
                {
                    mandatoryMap.put( attribute.getId(), false );
                    allowFutureDateMap.put( attribute.getId(), false );
                }
            }
            else
            {
                attributes = program.getTrackedEntityAttributes();
                for ( ProgramTrackedEntityAttribute programAttribute : program.getProgramAttributes() )
                {
                    mandatoryMap.put( programAttribute.getAttribute().getId(), programAttribute.isMandatory() );
                    allowFutureDateMap.put( programAttribute.getAttribute().getId(),
                        programAttribute.getAllowFutureDate() );
                }
            }

            for ( TrackedEntityAttribute attribute : attributes )
            {
                TrackedEntityAttributeGroup attributeGroup = attribute.getAttributeGroup();
                String groupName = (attributeGroup == null) ? "" : attributeGroup.getDisplayName();
                if ( attributesMap.containsKey( groupName ) )
                {
                    List<TrackedEntityAttribute> attrs = attributesMap.get( groupName );
                    attrs.add( attribute );
                }
                else
                {
                    List<TrackedEntityAttribute> attrs = new ArrayList<>();
                    attrs.add( attribute );
                    attributesMap.put( groupName, attrs );
                }
            }

        }

        // -------------------------------------------------------------------------
        // Get attribute values
        // -------------------------------------------------------------------------

        Collection<TrackedEntityAttributeValue> attributeValues = entityInstance.getAttributeValues();

        for ( TrackedEntityAttributeValue attributeValue : attributeValues )
        {
            String value = attributeValue.getValue();
            attributeValueMap.put( attributeValue.getAttribute().getId(), value );
        }

        return SUCCESS;

    }

}
