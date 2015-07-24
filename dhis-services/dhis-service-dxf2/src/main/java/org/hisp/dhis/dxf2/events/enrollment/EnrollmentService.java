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

import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.importsummary.ImportSummaries;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public interface EnrollmentService
{
    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    Enrollments getEnrollments();

    Enrollments getEnrollments( EnrollmentStatus status );

    Enrollments getEnrollments( TrackedEntityInstance trackedEntityInstance );

    Enrollments getEnrollments( TrackedEntityInstance trackedEntityInstance, EnrollmentStatus status );

    Enrollments getEnrollments( org.hisp.dhis.trackedentity.TrackedEntityInstance entityInstance );

    Enrollments getEnrollments( org.hisp.dhis.trackedentity.TrackedEntityInstance entityInstance, EnrollmentStatus status );

    Enrollments getEnrollments( Program program );

    Enrollments getEnrollments( Program program, EnrollmentStatus status );

    Enrollments getEnrollments( Program program, EnrollmentStatus status, OrganisationUnit organisationUnit, Date startDate, Date endDate );

    Enrollments getEnrollments( Program program, EnrollmentStatus status, List<OrganisationUnit> organisationUnits, Date startDate, Date endDate );

    Enrollments getEnrollments( Program program, TrackedEntityInstance trackedEntityInstance );

    Enrollments getEnrollments( Program program, TrackedEntityInstance trackedEntityInstance, EnrollmentStatus status );

    Enrollments getEnrollments( OrganisationUnit organisationUnit );

    Enrollments getEnrollments( OrganisationUnit organisationUnit, EnrollmentStatus status );

    Enrollments getEnrollments( Program program, OrganisationUnit organisationUnit );

    Enrollments getEnrollments( Program program, OrganisationUnit organisationUnit, Date startDate, Date endDate );
    
    Enrollments getEnrollments( Program program, List<OrganisationUnit> organisationUnits, Date startDate, Date endDate );

    Enrollments getEnrollments( Collection<ProgramInstance> programInstances );

    Enrollment getEnrollment( String id );

    Enrollment getEnrollment( ProgramInstance programInstance );

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    ImportSummaries addEnrollmentsJson( InputStream inputStream, ImportStrategy strategy ) throws IOException;

    ImportSummaries addEnrollmentsXml( InputStream inputStream, ImportStrategy strategy ) throws IOException;

    ImportSummary addEnrollment( Enrollment enrollment );

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    ImportSummary updateEnrollmentJson( String id, InputStream inputStream ) throws IOException;

    ImportSummary updateEnrollmentXml( String id, InputStream inputStream ) throws IOException;

    ImportSummary updateEnrollment( Enrollment enrollment );

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    void deleteEnrollment( Enrollment enrollment );

    void cancelEnrollment( Enrollment enrollment );

    void completeEnrollment( Enrollment enrollment );
}
