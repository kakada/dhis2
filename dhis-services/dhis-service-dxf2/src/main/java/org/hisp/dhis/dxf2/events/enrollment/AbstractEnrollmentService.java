package org.hisp.dhis.dxf2.events.enrollment;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.IdentifiableObjectUtils;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.common.QueryItem;
import org.hisp.dhis.common.QueryOperator;
import org.hisp.dhis.dxf2.events.event.Note;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.dxf2.importsummary.ImportConflict;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.i18n.I18nManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceQueryParams;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.hisp.dhis.trackedentitycomment.TrackedEntityComment;
import org.hisp.dhis.trackedentitycomment.TrackedEntityCommentService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public abstract class AbstractEnrollmentService
    implements EnrollmentService
{
    @Autowired
    protected ProgramInstanceService programInstanceService;

    @Autowired
    protected ProgramService programService;

    @Autowired
    protected TrackedEntityInstanceService trackedEntityInstanceService;

    @Autowired
    protected org.hisp.dhis.trackedentity.TrackedEntityInstanceService teiService;

    @Autowired
    protected TrackedEntityAttributeService trackedEntityAttributeService;

    @Autowired
    protected TrackedEntityAttributeValueService trackedEntityAttributeValueService;

    @Autowired
    protected CurrentUserService currentUserService;

    @Autowired
    protected TrackedEntityCommentService commentService;

    @Autowired
    protected IdentifiableObjectManager manager;

    @Autowired
    protected I18nManager i18nManager;

    @Autowired
    protected UserService userService;

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    public Enrollments getEnrollments()
    {
        List<Program> programs = getProgramsWithRegistration();

        List<ProgramInstance> programInstances = new ArrayList<>(
            programInstanceService.getProgramInstances( programs ) );

        return getEnrollments( programInstances );
    }

    @Override
    public Enrollments getEnrollments( EnrollmentStatus status )
    {
        List<Program> programs = getProgramsWithRegistration();

        List<ProgramInstance> programInstances = new ArrayList<>(
            programInstanceService.getProgramInstances( programs, status.getValue() ) );

        return getEnrollments( programInstances );
    }

    @Override
    public Enrollments getEnrollments( TrackedEntityInstance trackedEntityInstance )
    {
        org.hisp.dhis.trackedentity.TrackedEntityInstance entityInstance = getTrackedEntityInstance( trackedEntityInstance
            .getTrackedEntityInstance() );

        return getEnrollments( entityInstance );
    }

    @Override
    public Enrollments getEnrollments( TrackedEntityInstance trackedEntityInstance, EnrollmentStatus status )
    {
        org.hisp.dhis.trackedentity.TrackedEntityInstance entityInstance = getTrackedEntityInstance( trackedEntityInstance
            .getTrackedEntityInstance() );
        return getEnrollments( entityInstance, status );
    }

    @Override
    public Enrollments getEnrollments( org.hisp.dhis.trackedentity.TrackedEntityInstance entityInstance )
    {
        List<ProgramInstance> programInstances = new ArrayList<>( entityInstance.getProgramInstances() );

        return getEnrollments( programInstances );
    }

    @Override
    public Enrollments getEnrollments( org.hisp.dhis.trackedentity.TrackedEntityInstance entityInstance,
        EnrollmentStatus status )
    {
        List<ProgramInstance> programInstances = new ArrayList<>(
            programInstanceService.getProgramInstances( entityInstance, status.getValue() ) );

        return getEnrollments( programInstances );
    }

    @Override
    public Enrollments getEnrollments( Program program )
    {
        List<ProgramInstance> programInstances = new ArrayList<>(
            programInstanceService.getProgramInstances( program ) );
        return getEnrollments( programInstances );
    }

    @Override
    public Enrollments getEnrollments( Program program, EnrollmentStatus status )
    {
        List<ProgramInstance> programInstances = new ArrayList<>(
            programInstanceService.getProgramInstances( program, status.getValue() ) );

        return getEnrollments( programInstances );
    }

    @Override
    public Enrollments getEnrollments( Program program, EnrollmentStatus status, OrganisationUnit organisationUnit, Date startDate, Date endDate )
    {
        List<ProgramInstance> programInstances = new ArrayList<>(
            programInstanceService.getProgramInstancesByStatus( status.getValue(), program, Arrays.asList( organisationUnit.getId() ), startDate, endDate ) );

        return getEnrollments( programInstances );
    }

    @Override
    public Enrollments getEnrollments( Program program, EnrollmentStatus status, List<OrganisationUnit> organisationUnits, Date startDate, Date endDate )
    {
        List<ProgramInstance> programInstances = new ArrayList<>(
            programInstanceService.getProgramInstancesByStatus( status.getValue(), program, IdentifiableObjectUtils.getIdentifiers( organisationUnits ), startDate, endDate ) );

        return getEnrollments( programInstances );
    }

    @Override
    public Enrollments getEnrollments( OrganisationUnit organisationUnit )
    {
        List<Program> programs = getProgramsWithRegistration();
        List<ProgramInstance> programInstances = new ArrayList<>(
            programInstanceService.getProgramInstances( programs, organisationUnit ) );

        return getEnrollments( programInstances );
    }

    @Override
    public Enrollments getEnrollments( OrganisationUnit organisationUnit, EnrollmentStatus status )
    {
        List<Program> programs = getProgramsWithRegistration();
        List<ProgramInstance> programInstances = new ArrayList<>(
            programInstanceService.getProgramInstances( programs, organisationUnit, status.getValue() ) );

        return getEnrollments( programInstances );
    }

    @Override
    public Enrollments getEnrollments( Program program, OrganisationUnit organisationUnit )
    {
        return getEnrollments( programInstanceService.getProgramInstances( program, organisationUnit, 0, null ) );
    }

    @Override
    public Enrollments getEnrollments( Program program, OrganisationUnit organisationUnit, Date startDate, Date endDate )
    {
        return getEnrollments(
            programInstanceService.getProgramInstances( program, Arrays.asList( organisationUnit.getId() ), startDate, endDate, 0, Integer.MAX_VALUE ) );
    }

    @Override
    public Enrollments getEnrollments( Program program, List<OrganisationUnit> organisationUnits, Date startDate, Date endDate )
    {
        return getEnrollments(
            programInstanceService.getProgramInstances( program, IdentifiableObjectUtils.getIdentifiers( organisationUnits ), startDate, endDate, 0, Integer.MAX_VALUE ) );
    }

    @Override
    public Enrollments getEnrollments( Program program, TrackedEntityInstance trackedEntityInstance )
    {
        org.hisp.dhis.trackedentity.TrackedEntityInstance entityInstance = getTrackedEntityInstance( trackedEntityInstance
            .getTrackedEntityInstance() );
        return getEnrollments( programInstanceService.getProgramInstances( entityInstance, program ) );
    }

    @Override
    public Enrollments getEnrollments( Program program, TrackedEntityInstance trackedEntityInstance,
        EnrollmentStatus status )
    {
        org.hisp.dhis.trackedentity.TrackedEntityInstance entityInstance = getTrackedEntityInstance( trackedEntityInstance
            .getTrackedEntityInstance() );
        return getEnrollments( programInstanceService.getProgramInstances( entityInstance, program, status.getValue() ) );
    }

    @Override
    public Enrollments getEnrollments( Collection<ProgramInstance> programInstances )
    {
        Enrollments enrollments = new Enrollments();

        for ( ProgramInstance programInstance : programInstances )
        {
            if ( programInstance != null && programInstance.getEntityInstance() != null )
            {
                enrollments.getEnrollments().add( getEnrollment( programInstance ) );
            }
        }

        return enrollments;
    }

    @Override
    public Enrollment getEnrollment( String id )
    {
        ProgramInstance programInstance = programInstanceService.getProgramInstance( id );

        return programInstance != null ? getEnrollment( programInstance ) : null;
    }

    @Override
    public Enrollment getEnrollment( ProgramInstance programInstance )
    {
        Enrollment enrollment = new Enrollment();

        enrollment.setEnrollment( programInstance.getUid() );

        if ( programInstance.getEntityInstance() != null )
        {
            enrollment.setTrackedEntityInstance( programInstance.getEntityInstance().getUid() );
        }

        if ( programInstance.getOrganisationUnit() != null )
        {
            enrollment.setOrgUnit( programInstance.getOrganisationUnit().getUid() );
        }

        enrollment.setProgram( programInstance.getProgram().getUid() );
        enrollment.setStatus( EnrollmentStatus.fromInt( programInstance.getStatus() ) );
        enrollment.setDateOfEnrollment( programInstance.getEnrollmentDate() );
        enrollment.setDateOfIncident( programInstance.getDateOfIncident() );
        enrollment.setFollowup( programInstance.getFollowup() );

        List<TrackedEntityComment> comments = programInstance.getComments();

        for ( TrackedEntityComment comment : comments )
        {
            Note note = new Note();

            note.setValue( comment.getCommentText() );
            note.setStoredBy( comment.getCreator() );

            if ( comment.getCreatedDate() != null )
            {
                note.setStoredDate( comment.getCreatedDate().toString() );
            }

            enrollment.getNotes().add( note );
        }

        return enrollment;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Override
    public ImportSummary addEnrollment( Enrollment enrollment )
    {
        ImportSummary importSummary = new ImportSummary();

        org.hisp.dhis.trackedentity.TrackedEntityInstance entityInstance = getTrackedEntityInstance( enrollment.getTrackedEntityInstance() );
        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceService.getTrackedEntityInstance( entityInstance );

        Program program = getProgram( enrollment.getProgram() );

        Enrollments enrollments = getEnrollments( program, trackedEntityInstance, EnrollmentStatus.ACTIVE );

        if ( !enrollments.getEnrollments().isEmpty() )
        {
            importSummary.setStatus( ImportStatus.ERROR );
            importSummary.setDescription( "TrackedEntityInstance " + trackedEntityInstance.getTrackedEntityInstance()
                + " already have an active enrollment in program " + program.getUid() );
            importSummary.getImportCount().incrementIgnored();

            return importSummary;
        }

        if ( program.getOnlyEnrollOnce() )
        {
            enrollments = getEnrollments( program, trackedEntityInstance, EnrollmentStatus.COMPLETED );

            if ( !enrollments.getEnrollments().isEmpty() )
            {
                importSummary.setStatus( ImportStatus.ERROR );
                importSummary.setDescription( "TrackedEntityInstance " + trackedEntityInstance.getTrackedEntityInstance()
                    + " already have a completed enrollment in program " + program.getUid() + ", and this program is" +
                    " configured to only allow enrolling one time." );
                importSummary.getImportCount().incrementIgnored();

                return importSummary;
            }
        }

        Set<ImportConflict> importConflicts = new HashSet<>();
        importConflicts.addAll( checkAttributes( enrollment ) );

        importSummary.setConflicts( importConflicts );

        if ( !importConflicts.isEmpty() )
        {
            importSummary.setStatus( ImportStatus.ERROR );
            importSummary.getImportCount().incrementIgnored();

            return importSummary;
        }

        OrganisationUnit organisationUnit = getOrganisationUnit( enrollment.getOrgUnit() );

        ProgramInstance programInstance = programInstanceService.enrollTrackedEntityInstance( enrollment.getEnrollment(), entityInstance, program,
            enrollment.getDateOfEnrollment(), enrollment.getDateOfIncident(), organisationUnit );

        if ( programInstance == null )
        {
            importSummary.setStatus( ImportStatus.ERROR );
            importSummary.setDescription( "Could not enroll TrackedEntityInstance "
                + enrollment.getTrackedEntityInstance() + " into program " + enrollment.getProgram() );

            return importSummary;
        }

        updateAttributeValues( enrollment );
        programInstance.setFollowup( enrollment.getFollowup() );
        programInstanceService.updateProgramInstance( programInstance );

        saveTrackedEntityComment( programInstance, enrollment );

        importSummary.setReference( programInstance.getUid() );
        importSummary.getImportCount().incrementImported();

        return importSummary;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Override
    public ImportSummary updateEnrollment( Enrollment enrollment )
    {
        ImportSummary importSummary = new ImportSummary();

        if ( enrollment == null || enrollment.getEnrollment() == null )
        {
            importSummary = new ImportSummary( ImportStatus.ERROR, "No enrollment or enrollment ID was supplied" );
            importSummary.getImportCount().incrementIgnored();

            return importSummary;
        }

        ProgramInstance programInstance = programInstanceService.getProgramInstance( enrollment.getEnrollment() );

        if ( programInstance == null )
        {
            importSummary = new ImportSummary( ImportStatus.ERROR, "Enrollment ID was not valid." );
            importSummary.getImportCount().incrementIgnored();

            return importSummary;
        }

        Set<ImportConflict> importConflicts = new HashSet<>();
        importConflicts.addAll( checkAttributes( enrollment ) );

        importSummary.setConflicts( importConflicts );

        if ( !importConflicts.isEmpty() )
        {
            importSummary.setStatus( ImportStatus.ERROR );
            importSummary.getImportCount().incrementIgnored();

            return importSummary;
        }

        org.hisp.dhis.trackedentity.TrackedEntityInstance entityInstance = getTrackedEntityInstance( enrollment.getTrackedEntityInstance() );
        Program program = getProgram( enrollment.getProgram() );

        programInstance.setProgram( program );
        programInstance.setEntityInstance( entityInstance );
        programInstance.setDateOfIncident( enrollment.getDateOfIncident() );
        programInstance.setEnrollmentDate( enrollment.getDateOfEnrollment() );
        programInstance.setFollowup( enrollment.getFollowup() );

        if ( programInstance.getStatus() != enrollment.getStatus().getValue() )
        {
            if ( EnrollmentStatus.CANCELLED == enrollment.getStatus() )
            {
                programInstanceService.cancelProgramInstanceStatus( programInstance );
            }
            else if ( EnrollmentStatus.COMPLETED == enrollment.getStatus() )
            {
                programInstanceService.completeProgramInstanceStatus( programInstance );
            }
            else
            {
                importSummary = new ImportSummary( ImportStatus.ERROR,
                    "Re-enrollment is not allowed, please create a new enrollment." );
                importSummary.getImportCount().incrementIgnored();

                return importSummary;
            }
        }

        updateAttributeValues( enrollment );
        programInstanceService.updateProgramInstance( programInstance );

        saveTrackedEntityComment( programInstance, enrollment );

        importSummary.setReference( enrollment.getEnrollment() );
        importSummary.getImportCount().incrementUpdated();

        return importSummary;
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Override
    public void deleteEnrollment( Enrollment enrollment )
    {
        ProgramInstance programInstance = programInstanceService.getProgramInstance( enrollment.getEnrollment() );
        Assert.notNull( programInstance );

        programInstanceService.deleteProgramInstance( programInstance );
    }

    @Override
    public void cancelEnrollment( Enrollment enrollment )
    {
        ProgramInstance programInstance = programInstanceService.getProgramInstance( enrollment.getEnrollment() );
        Assert.notNull( programInstance );

        programInstanceService.cancelProgramInstanceStatus( programInstance );
    }

    @Override
    public void completeEnrollment( Enrollment enrollment )
    {

        ProgramInstance programInstance = programInstanceService.getProgramInstance( enrollment.getEnrollment() );
        Assert.notNull( programInstance );

        programInstanceService.completeProgramInstanceStatus( programInstance );
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private List<ImportConflict> checkAttributes( Enrollment enrollment )
    {
        List<ImportConflict> importConflicts = new ArrayList<>();

        Program program = getProgram( enrollment.getProgram() );
        org.hisp.dhis.trackedentity.TrackedEntityInstance trackedEntityInstance = teiService
            .getTrackedEntityInstance( enrollment.getTrackedEntityInstance() );

        Map<TrackedEntityAttribute, Boolean> mandatoryMap = Maps.newHashMap();
        Map<String, String> attributeValueMap = Maps.newHashMap();

        for ( ProgramTrackedEntityAttribute programTrackedEntityAttribute : program.getProgramAttributes() )
        {
            mandatoryMap
                .put( programTrackedEntityAttribute.getAttribute(), programTrackedEntityAttribute.isMandatory() );
        }

        for ( TrackedEntityAttributeValue value : trackedEntityInstance.getAttributeValues() )
        {
            // ignore attributes which do not belong to this program
            if ( mandatoryMap.containsKey( value.getAttribute() ) )
            {
                attributeValueMap.put( value.getAttribute().getUid(), value.getValue() );
            }
        }

        for ( Attribute attribute : enrollment.getAttributes() )
        {
            attributeValueMap.put( attribute.getAttribute(), attribute.getValue() );
            importConflicts.addAll( validateAttributeType( attribute ) );
        }

        TrackedEntityInstance instance = trackedEntityInstanceService.getTrackedEntityInstance( enrollment
            .getTrackedEntityInstance() );

        for ( TrackedEntityAttribute trackedEntityAttribute : mandatoryMap.keySet() )
        {
            Boolean mandatory = mandatoryMap.get( trackedEntityAttribute );

            if ( mandatory && !attributeValueMap.containsKey( trackedEntityAttribute.getUid() ) )
            {
                importConflicts.add( new ImportConflict( "Attribute.attribute", "Missing mandatory attribute "
                    + trackedEntityAttribute.getUid() ) );
                continue;
            }

            if ( trackedEntityAttribute.isUnique() )
            {
                OrganisationUnit organisationUnit = manager.get( OrganisationUnit.class, instance.getOrgUnit() );

                importConflicts.addAll( checkScope( trackedEntityInstance, trackedEntityAttribute,
                    attributeValueMap.get( trackedEntityAttribute.getUid() ), organisationUnit, program ) );
            }

            attributeValueMap.remove( trackedEntityAttribute.getUid() );
        }

        if ( !attributeValueMap.isEmpty() )
        {
            importConflicts.add( new ImportConflict( "Attribute.attribute",
                "Only program attributes is allowed for enrollment " + attributeValueMap ) );
        }

        return importConflicts;
    }

    private List<ImportConflict> checkScope( org.hisp.dhis.trackedentity.TrackedEntityInstance tei,
        TrackedEntityAttribute attribute, String value, OrganisationUnit organisationUnit, Program program )
    {
        List<ImportConflict> importConflicts = new ArrayList<>();

        TrackedEntityInstanceQueryParams params = new TrackedEntityInstanceQueryParams();

        QueryItem queryItem = new QueryItem( attribute, QueryOperator.EQ, value, attribute.getValueType(), null );
        params.addAttribute( queryItem );

        if ( attribute.getOrgunitScope() && attribute.getProgramScope() )
        {
            params.setProgram( program );
            params.addOrganisationUnit( organisationUnit );
        }
        else if ( attribute.getOrgunitScope() )
        {
            params.addOrganisationUnit( organisationUnit );
        }
        else if ( attribute.getProgramScope() )
        {
            params.setOrganisationUnitMode( OrganisationUnitSelectionMode.ALL );
            params.setProgram( program );
        }
        else
        {
            params.setOrganisationUnitMode( OrganisationUnitSelectionMode.ALL );
        }

        Grid instances = teiService.getTrackedEntityInstances( params );

        if ( instances.getHeight() == 0
            || (instances.getHeight() == 1 && instances.getRow( 0 ).contains( tei.getUid() )) )
        {
            return importConflicts;
        }

        importConflicts.add( new ImportConflict( "Attribute.value", "Non-unique attribute value '" + value
            + "' for attribute " + attribute.getUid() ) );

        return importConflicts;
    }

    private void updateAttributeValues( Enrollment enrollment )
    {
        org.hisp.dhis.trackedentity.TrackedEntityInstance trackedEntityInstance = teiService
            .getTrackedEntityInstance( enrollment.getTrackedEntityInstance() );
        Map<String, String> attributeValueMap = Maps.newHashMap();

        for ( Attribute attribute : enrollment.getAttributes() )
        {
            attributeValueMap.put( attribute.getAttribute(), attribute.getValue() );
        }

        for ( TrackedEntityAttributeValue value : trackedEntityInstance.getAttributeValues() )
        {
            if ( attributeValueMap.containsKey( value.getAttribute().getUid() ) )
            {
                String newValue = attributeValueMap.get( value.getAttribute().getUid() );
                value.setValue( newValue );

                trackedEntityAttributeValueService.updateTrackedEntityAttributeValue( value );

                attributeValueMap.remove( value.getAttribute().getUid() );
            }
        }

        for ( String key : attributeValueMap.keySet() )
        {
            TrackedEntityAttribute attribute = trackedEntityAttributeService.getTrackedEntityAttribute( key );

            if ( attribute != null )
            {
                TrackedEntityAttributeValue value = new TrackedEntityAttributeValue();
                value.setValue( attributeValueMap.get( key ) );
                value.setAttribute( attribute );

                trackedEntityAttributeValueService.addTrackedEntityAttributeValue( value );
                trackedEntityInstance.addAttributeValue( value );
            }
        }
    }

    private List<Program> getProgramsWithRegistration()
    {
        List<Program> programs = new ArrayList<>();
        programs.addAll( programService.getPrograms( Program.SINGLE_EVENT_WITH_REGISTRATION ) );
        programs.addAll( programService.getPrograms( Program.MULTIPLE_EVENTS_WITH_REGISTRATION ) );

        return programs;
    }

    private org.hisp.dhis.trackedentity.TrackedEntityInstance getTrackedEntityInstance( String trackedEntityInstance )
    {
        org.hisp.dhis.trackedentity.TrackedEntityInstance entityInstance = teiService
            .getTrackedEntityInstance( trackedEntityInstance );

        if ( entityInstance == null )
        {
            throw new IllegalArgumentException( "TrackedEntityInstance does not exist." );
        }

        return entityInstance;
    }

    private Program getProgram( String id )
    {
        Program program = programService.getProgram( id );

        if ( program == null )
        {
            throw new IllegalArgumentException( "Program does not exist." );
        }

        return program;

    }

    private OrganisationUnit getOrganisationUnit( String id )
    {
        OrganisationUnit organisationUnit = manager.search( OrganisationUnit.class, id );

        if ( organisationUnit == null )
        {
            throw new IllegalArgumentException( "OrganisationUnit does not exist." );
        }

        return organisationUnit;
    }

    private List<ImportConflict> validateAttributeType( Attribute attribute )
    {
        List<ImportConflict> importConflicts = Lists.newArrayList();
        TrackedEntityAttribute teAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( attribute
            .getAttribute() );

        if ( teAttribute == null )
        {
            importConflicts.add( new ImportConflict( "Attribute.attribute", "Does not point to a valid attribute." ) );
            return importConflicts;
        }

        if ( attribute.getValue().length() > 255 )
        {
            importConflicts.add( new ImportConflict( "Attribute.value", "Value length is greater than 256 chars." ) );
        }

        if ( TrackedEntityAttribute.TYPE_NUMBER.equals( teAttribute.getValueType() )
            && !MathUtils.isNumeric( attribute.getValue() ) )
        {
            importConflicts.add( new ImportConflict( "Attribute.value", "Value is not numeric." ) );
        }
        else if ( TrackedEntityAttribute.TYPE_BOOL.equals( teAttribute.getValueType() )
            && !MathUtils.isBool( attribute.getValue() ) )
        {
            importConflicts.add( new ImportConflict( "Attribute.value", "Value is not boolean." ) );
        }
        else if ( TrackedEntityAttribute.TYPE_DATE.equals( teAttribute.getValueType() )
            && !DateUtils.dateIsValid( attribute.getValue() ) )
        {
            importConflicts.add( new ImportConflict( "Attribute.value", "Value is not date." ) );
        }
        else if ( TrackedEntityAttribute.TYPE_TRUE_ONLY.equals( teAttribute.getValueType() )
            && "true".equals( attribute.getValue() ) )
        {
            importConflicts.add( new ImportConflict( "Attribute.value", "Value is not true (true-only value type)." ) );
        }
        else if ( TrackedEntityAttribute.TYPE_USERS.equals( teAttribute.getValueType() ) )
        {
            if ( userService.getUserCredentialsByUsername( attribute.getValue() ) == null )
            {
                importConflicts.add( new ImportConflict( "Attribute.value", "Value is not pointing to a valid username." ) );
            }
        }
        else if ( TrackedEntityAttribute.TYPE_OPTION_SET.equals( teAttribute.getValueType() )
            && !teAttribute.getOptionSet().getOptionCodes().contains( attribute.getValue() ) )
        {
            importConflicts.add( new ImportConflict( "Attribute.value", "Value is not pointing to a valid option code." ) );
        }

        return importConflicts;
    }

    private void saveTrackedEntityComment( ProgramInstance programInstance, Enrollment enrollment )
    {
        String storedBy = currentUserService.getCurrentUsername();

        for ( Note note : enrollment.getNotes() )
        {
            TrackedEntityComment comment = new TrackedEntityComment();
            comment.setCreator( storedBy );
            comment.setCreatedDate( new Date() );
            comment.setCommentText( note.getValue() );

            commentService.addTrackedEntityComment( comment );

            programInstance.getComments().add( comment );

            programInstanceService.updateProgramInstance( programInstance );
        }
    }
}