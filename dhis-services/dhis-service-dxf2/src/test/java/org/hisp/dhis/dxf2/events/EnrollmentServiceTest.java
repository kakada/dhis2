package org.hisp.dhis.dxf2.events;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.dxf2.events.enrollment.EnrollmentService;
import org.hisp.dhis.dxf2.events.enrollment.EnrollmentStatus;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityService;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class EnrollmentServiceTest
    extends DhisSpringTest
{
    @Autowired
    private TrackedEntityService trackedEntityService;

    @Autowired
    private TrackedEntityInstanceService trackedEntityInstanceService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private IdentifiableObjectManager manager;

    @Autowired
    private ProgramInstanceService programInstanceService;

    private org.hisp.dhis.trackedentity.TrackedEntityInstance maleA;
    private org.hisp.dhis.trackedentity.TrackedEntityInstance maleB;
    private org.hisp.dhis.trackedentity.TrackedEntityInstance femaleA;
    private org.hisp.dhis.trackedentity.TrackedEntityInstance femaleB;

    private OrganisationUnit organisationUnitA;
    private OrganisationUnit organisationUnitB;

    private Program programA;
    private ProgramStage programStage;

    @Override
    protected void setUpTest() 
        throws Exception
    {
        organisationUnitA = createOrganisationUnit( 'A' );
        organisationUnitB = createOrganisationUnit( 'B' );
        organisationUnitB.setParent( organisationUnitA );

        manager.save( organisationUnitA );
        manager.save( organisationUnitB );

        TrackedEntity trackedEntity = createTrackedEntity( 'A' );
        trackedEntityService.addTrackedEntity( trackedEntity );

        maleA = createTrackedEntityInstance( 'A', organisationUnitA );
        maleB = createTrackedEntityInstance( 'B', organisationUnitB );
        femaleA = createTrackedEntityInstance( 'C', organisationUnitA );
        femaleB = createTrackedEntityInstance( 'D', organisationUnitB );

        maleA.setTrackedEntity( trackedEntity );
        maleB.setTrackedEntity( trackedEntity );
        femaleA.setTrackedEntity( trackedEntity );
        femaleB.setTrackedEntity( trackedEntity );

        manager.save( maleA );
        manager.save( maleB );
        manager.save( femaleA );
        manager.save( femaleB );

        programStage = createProgramStage( 'A', 0 );
        programStage.setGeneratedByEnrollmentDate( true );
        manager.save( programStage );

        programA = createProgram( 'A', new HashSet<ProgramStage>(), organisationUnitA );
        programA.setType( Program.SINGLE_EVENT_WITH_REGISTRATION );
        manager.save( programA );

        programA.getProgramStages().add( programStage );
        programStage.setProgram( programA );

        manager.save( programA );
    }

    @Test
    public void testGetEnrollments()
    {
        programInstanceService.enrollTrackedEntityInstance( maleA, programA, null, null, organisationUnitA );
        programInstanceService.enrollTrackedEntityInstance( femaleA, programA, null, null, organisationUnitA );

        assertEquals( 2, enrollmentService.getEnrollments().getEnrollments().size() );
    }

    @Test
    public void testGetEnrollmentsByPatient()
    {
        programInstanceService.enrollTrackedEntityInstance( maleA, programA, null, null, organisationUnitA );
        programInstanceService.enrollTrackedEntityInstance( femaleA, programA, null, null, organisationUnitA );

        assertEquals( 1, enrollmentService.getEnrollments( maleA ).getEnrollments().size() );
        assertEquals( 1, enrollmentService.getEnrollments( femaleA ).getEnrollments().size() );
    }

    @Test
    public void testGetEnrollmentsByPerson()
    {

        programInstanceService.enrollTrackedEntityInstance( maleA, programA, null, null, organisationUnitA );
        programInstanceService.enrollTrackedEntityInstance( femaleA, programA, null, null, organisationUnitA );

        TrackedEntityInstance male = trackedEntityInstanceService.getTrackedEntityInstance( maleA );
        TrackedEntityInstance female = trackedEntityInstanceService.getTrackedEntityInstance( femaleA );

        assertEquals( 1, enrollmentService.getEnrollments( male ).getEnrollments().size() );
        assertEquals( 1, enrollmentService.getEnrollments( female ).getEnrollments().size() );
    }

    @Test
    public void testGetEnrollmentsByStatus()
    {
        ProgramInstance piMale = programInstanceService.enrollTrackedEntityInstance( maleA, programA, null, null, organisationUnitA );
        ProgramInstance piFemale = programInstanceService.enrollTrackedEntityInstance( femaleA, programA, null, null, organisationUnitA );

        assertEquals( 2, enrollmentService.getEnrollments( EnrollmentStatus.ACTIVE ).getEnrollments().size() );
        assertEquals( 0, enrollmentService.getEnrollments( EnrollmentStatus.CANCELLED ).getEnrollments().size() );
        assertEquals( 0, enrollmentService.getEnrollments( EnrollmentStatus.COMPLETED ).getEnrollments().size() );

        programInstanceService.cancelProgramInstanceStatus( piMale );

        assertEquals( 1, enrollmentService.getEnrollments( EnrollmentStatus.ACTIVE ).getEnrollments().size() );
        assertEquals( 1, enrollmentService.getEnrollments( EnrollmentStatus.CANCELLED ).getEnrollments().size() );
        assertEquals( 0, enrollmentService.getEnrollments( EnrollmentStatus.COMPLETED ).getEnrollments().size() );

        programInstanceService.completeProgramInstanceStatus( piFemale );

        assertEquals( 0, enrollmentService.getEnrollments( EnrollmentStatus.ACTIVE ).getEnrollments().size() );
        assertEquals( 1, enrollmentService.getEnrollments( EnrollmentStatus.CANCELLED ).getEnrollments().size() );
        assertEquals( 1, enrollmentService.getEnrollments( EnrollmentStatus.COMPLETED ).getEnrollments().size() );
    }

    @Test
    public void testSaveEnrollment()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setOrgUnit( organisationUnitA.getUid() );
        enrollment.setTrackedEntityInstance( maleA.getUid() );
        enrollment.setProgram( programA.getUid() );
        enrollment.setDateOfIncident( new Date() );
        enrollment.setDateOfEnrollment( new Date() );

        ImportSummary importSummary = enrollmentService.addEnrollment( enrollment );
        assertEquals( ImportStatus.SUCCESS, importSummary.getStatus() );

        List<Enrollment> enrollments = enrollmentService.getEnrollments( maleA ).getEnrollments();

        assertEquals( 1, enrollments.size() );
        assertEquals( maleA.getUid(), enrollments.get( 0 ).getTrackedEntityInstance() );
        assertEquals( programA.getUid(), enrollments.get( 0 ).getProgram() );
    }

    @Test
    public void testUpdateEnrollment()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setOrgUnit( organisationUnitA.getUid() );
        enrollment.setTrackedEntityInstance( maleA.getUid() );
        enrollment.setProgram( programA.getUid() );
        enrollment.setDateOfIncident( new Date() );
        enrollment.setDateOfEnrollment( new Date() );

        ImportSummary importSummary = enrollmentService.addEnrollment( enrollment );
        assertEquals( ImportStatus.SUCCESS, importSummary.getStatus() );

        List<Enrollment> enrollments = enrollmentService.getEnrollments( maleA ).getEnrollments();

        assertEquals( 1, enrollments.size() );
        enrollment = enrollments.get( 0 );

        assertEquals( maleA.getUid(), enrollment.getTrackedEntityInstance() );
        assertEquals( programA.getUid(), enrollment.getProgram() );

        Date MARCH_20_81 = new DateTime( 81, 2, 20, 0, 0 ).toDate();

        enrollment.setDateOfEnrollment( MARCH_20_81 );
        enrollmentService.updateEnrollment( enrollment );

        enrollments = enrollmentService.getEnrollments( maleA ).getEnrollments();
        assertEquals( 1, enrollments.size() );
        assertEquals( MARCH_20_81, enrollments.get( 0 ).getDateOfEnrollment() );
    }

    @Test
    public void testUpdateCompleted()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setOrgUnit( organisationUnitA.getUid() );
        enrollment.setTrackedEntityInstance( maleA.getUid() );
        enrollment.setProgram( programA.getUid() );
        enrollment.setDateOfIncident( new Date() );
        enrollment.setDateOfEnrollment( new Date() );

        List<Enrollment> enrollments = enrollmentService.getEnrollments( maleA ).getEnrollments();
        assertEquals( 0, enrollments.size() );
        
        ImportSummary importSummary = enrollmentService.addEnrollment( enrollment );
        assertEquals( ImportStatus.SUCCESS, importSummary.getStatus() );

        enrollments = enrollmentService.getEnrollments( maleA ).getEnrollments();

        assertEquals( 1, enrollments.size() );
        enrollment = enrollments.get( 0 );
        enrollment.setStatus( EnrollmentStatus.COMPLETED );

        enrollmentService.updateEnrollment( enrollment );
        enrollments = enrollmentService.getEnrollments( maleA ).getEnrollments();
        assertEquals( 1, enrollments.size() );
        assertEquals( EnrollmentStatus.COMPLETED, enrollments.get( 0 ).getStatus() );
    }

    @Test
    public void testUpdateCancelled()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setOrgUnit( organisationUnitA.getUid() );
        enrollment.setTrackedEntityInstance( maleA.getUid() );
        enrollment.setProgram( programA.getUid() );
        enrollment.setDateOfIncident( new Date() );
        enrollment.setDateOfEnrollment( new Date() );

        ImportSummary importSummary = enrollmentService.addEnrollment( enrollment );
        assertEquals( ImportStatus.SUCCESS, importSummary.getStatus() );

        List<Enrollment> enrollments = enrollmentService.getEnrollments( maleA ).getEnrollments();

        assertEquals( 1, enrollments.size() );
        enrollment = enrollments.get( 0 );
        enrollment.setStatus( EnrollmentStatus.CANCELLED );

        enrollmentService.updateEnrollment( enrollment );
        enrollments = enrollmentService.getEnrollments( maleA ).getEnrollments();
        assertEquals( 1, enrollments.size() );
        assertEquals( EnrollmentStatus.CANCELLED, enrollments.get( 0 ).getStatus() );
    }

    @Test
    public void testUpdateReEnrollmentShouldFail()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setOrgUnit( organisationUnitA.getUid() );
        enrollment.setTrackedEntityInstance( maleA.getUid() );
        enrollment.setProgram( programA.getUid() );
        enrollment.setDateOfIncident( new Date() );
        enrollment.setDateOfEnrollment( new Date() );

        ImportSummary importSummary = enrollmentService.addEnrollment( enrollment );
        assertEquals( ImportStatus.SUCCESS, importSummary.getStatus() );

        List<Enrollment> enrollments = enrollmentService.getEnrollments( maleA ).getEnrollments();

        assertEquals( 1, enrollments.size() );
        enrollment = enrollments.get( 0 );
        enrollment.setStatus( EnrollmentStatus.CANCELLED );

        importSummary = enrollmentService.updateEnrollment( enrollment );
        assertEquals( ImportStatus.SUCCESS, importSummary.getStatus() );

        enrollments = enrollmentService.getEnrollments( maleA ).getEnrollments();
        assertEquals( 1, enrollments.size() );
        assertEquals( EnrollmentStatus.CANCELLED, enrollments.get( 0 ).getStatus() );

        enrollment.setStatus( EnrollmentStatus.ACTIVE );
        importSummary = enrollmentService.updateEnrollment( enrollment );
        assertEquals( ImportStatus.ERROR, importSummary.getStatus() );
    }

    @Test
    public void testMultipleEnrollmentsShouldFail()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setOrgUnit( organisationUnitA.getUid() );
        enrollment.setTrackedEntityInstance( maleA.getUid() );
        enrollment.setProgram( programA.getUid() );
        enrollment.setDateOfIncident( new Date() );
        enrollment.setDateOfEnrollment( new Date() );

        ImportSummary importSummary = enrollmentService.addEnrollment( enrollment );
        assertEquals( ImportStatus.SUCCESS, importSummary.getStatus() );
        importSummary = enrollmentService.addEnrollment( enrollment );
        assertEquals( ImportStatus.ERROR, importSummary.getStatus() );
        assertThat( importSummary.getDescription(), CoreMatchers.containsString( "already have an active enrollment in program" ) );
    }

    @Test
    @Ignore
    public void testUpdatePersonShouldKeepEnrollments()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setOrgUnit( organisationUnitA.getUid() );
        enrollment.setTrackedEntityInstance( maleA.getUid() );
        enrollment.setProgram( programA.getUid() );
        enrollment.setDateOfIncident( new Date() );
        enrollment.setDateOfEnrollment( new Date() );

        ImportSummary importSummary = enrollmentService.addEnrollment( enrollment );
        assertEquals( ImportStatus.SUCCESS, importSummary.getStatus() );

        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceService.getTrackedEntityInstance( maleA );
        trackedEntityInstanceService.updateTrackedEntityInstance( trackedEntityInstance );

        List<Enrollment> enrollments = enrollmentService.getEnrollments( trackedEntityInstance ).getEnrollments();

        assertEquals( 1, enrollments.size() );
        assertEquals( maleA.getUid(), enrollments.get( 0 ).getTrackedEntityInstance() );
        assertEquals( programA.getUid(), enrollments.get( 0 ).getProgram() );
    }
}
