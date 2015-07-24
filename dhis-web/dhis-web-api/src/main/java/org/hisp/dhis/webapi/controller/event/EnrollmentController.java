package org.hisp.dhis.webapi.controller.event;

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

import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.dxf2.events.enrollment.EnrollmentService;
import org.hisp.dhis.dxf2.events.enrollment.EnrollmentStatus;
import org.hisp.dhis.dxf2.events.enrollment.Enrollments;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummaries;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.dxf2.common.JacksonUtils;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.webapi.controller.exception.NotFoundException;
import org.hisp.dhis.webapi.utils.ContextUtils;
import org.hisp.dhis.webapi.webdomain.WebOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Controller
@RequestMapping( value = EnrollmentController.RESOURCE_PATH )
public class EnrollmentController
{
    public static final String RESOURCE_PATH = "/enrollments";

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private TrackedEntityInstanceService trackedEntityInstanceService;

    @Autowired
    private IdentifiableObjectManager manager;
    
    @Autowired
    private OrganisationUnitService organisationUnitService;

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @RequestMapping( value = "", method = RequestMethod.GET )
    public String getEnrollments(
        @RequestParam( value = "orgUnit", required = false ) String orgUnitUid,
        @RequestParam( value = "program", required = false ) String programUid,
        @RequestParam( value = "trackedEntityInstance", required = false ) String trackedEntityInstanceUid,
        @RequestParam( required = false ) @DateTimeFormat( pattern = "yyyy-MM-dd" ) Date startDate,
        @RequestParam( required = false ) @DateTimeFormat( pattern = "yyyy-MM-dd" ) Date endDate,
        @RequestParam( required = false ) OrganisationUnitSelectionMode ouMode,
        @RequestParam( value = "status", required = false ) EnrollmentStatus status,
        @RequestParam Map<String, String> parameters, Model model ) throws NotFoundException
    {
        WebOptions options = new WebOptions( parameters );
        Enrollments enrollments;

        if ( startDate != null && endDate != null && programUid != null && orgUnitUid != null && ouMode != null )
        {
            OrganisationUnit organisationUnit = getOrganisationUnit( orgUnitUid );
            List<OrganisationUnit> organisationUnits = getOrganisationUnits( organisationUnit, ouMode );
            
            Program program = getProgram( programUid );

            enrollments = status != null ?
                enrollmentService.getEnrollments( program, status, organisationUnits, startDate, endDate ) :
                enrollmentService.getEnrollments( program, organisationUnits, startDate, endDate );
        }
        else if ( orgUnitUid == null && programUid == null && trackedEntityInstanceUid == null )
        {
            enrollments = status != null ? enrollmentService.getEnrollments( status ) : enrollmentService.getEnrollments();
        }
        else if ( orgUnitUid != null && programUid != null )
        {
            OrganisationUnit organisationUnit = getOrganisationUnit( orgUnitUid );
            Program program = getProgram( programUid );

            enrollments = enrollmentService.getEnrollments( program, organisationUnit );
        }
        else if ( programUid != null && trackedEntityInstanceUid != null )
        {
            Program program = getProgram( programUid );
            TrackedEntityInstance trackedEntityInstance = getTrackedEntityInstance( trackedEntityInstanceUid );

            enrollments = status != null ? enrollmentService.getEnrollments( program, trackedEntityInstance, status )
                : enrollmentService.getEnrollments( program, trackedEntityInstance );
        }
        else if ( orgUnitUid != null )
        {
            OrganisationUnit organisationUnit = getOrganisationUnit( orgUnitUid );
            enrollments = status != null ? enrollmentService.getEnrollments( organisationUnit, status )
                : enrollmentService.getEnrollments( organisationUnit );
        }
        else if ( programUid != null )
        {
            Program program = getProgram( programUid );
            enrollments = status != null ? enrollmentService.getEnrollments( program, status ) : enrollmentService.getEnrollments( program );
        }
        else
        {
            TrackedEntityInstance trackedEntityInstance = getTrackedEntityInstance( trackedEntityInstanceUid );
            enrollments = status != null ? enrollmentService.getEnrollments( trackedEntityInstance, status ) : enrollmentService.getEnrollments( trackedEntityInstance );
        }

        model.addAttribute( "model", enrollments );
        model.addAttribute( "viewClass", options.getViewClass( "basic" ) );

        return "enrollments";
    }

    @RequestMapping( value = "/{id}", method = RequestMethod.GET )
    public String getEnrollment( @PathVariable String id, @RequestParam Map<String, String> parameters, Model model ) throws NotFoundException
    {
        WebOptions options = new WebOptions( parameters );
        Enrollment enrollment = getEnrollment( id );

        model.addAttribute( "model", enrollment );
        model.addAttribute( "viewClass", options.getViewClass( "detailed" ) );

        return "enrollment";
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @RequestMapping( value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_PROGRAM_ENROLLMENT')" )
    public void postEnrollmentXml( @RequestParam( defaultValue = "CREATE" ) ImportStrategy strategy, HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        ImportSummaries importSummaries = enrollmentService.addEnrollmentsXml( request.getInputStream(), strategy );
        response.setContentType( MediaType.APPLICATION_XML_VALUE );

        if ( importSummaries.getImportSummaries().size() > 1 )
        {
            response.setStatus( HttpServletResponse.SC_CREATED );
            JacksonUtils.toXml( response.getOutputStream(), importSummaries );
        }
        else
        {
            response.setStatus( HttpServletResponse.SC_CREATED );
            ImportSummary importSummary = importSummaries.getImportSummaries().get( 0 );

            if ( !importSummary.getStatus().equals( ImportStatus.ERROR ) )
            {
                response.setHeader( "Location", getResourcePath( request, importSummary ) );
            }

            JacksonUtils.toXml( response.getOutputStream(), importSummary );
        }
    }

    @RequestMapping( value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_PROGRAM_ENROLLMENT')" )
    public void postEnrollmentJson( @RequestParam( defaultValue = "CREATE" ) ImportStrategy strategy, HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        ImportSummaries importSummaries = enrollmentService.addEnrollmentsJson( request.getInputStream(), strategy );
        response.setContentType( MediaType.APPLICATION_JSON_VALUE );

        if ( importSummaries.getImportSummaries().size() > 1 )
        {
            response.setStatus( HttpServletResponse.SC_CREATED );
            JacksonUtils.toJson( response.getOutputStream(), importSummaries );
        }
        else
        {
            response.setStatus( HttpServletResponse.SC_CREATED );
            ImportSummary importSummary = importSummaries.getImportSummaries().get( 0 );

            if ( !importSummary.getStatus().equals( ImportStatus.ERROR ) )
            {
                response.setHeader( "Location", getResourcePath( request, importSummary ) );
            }

            JacksonUtils.toJson( response.getOutputStream(), importSummary );
        }
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @RequestMapping( value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_PROGRAM_UNENROLLMENT')" )
    public void updateEnrollmentXml( @PathVariable String id, HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        ImportSummary importSummary = enrollmentService.updateEnrollmentXml( id, request.getInputStream() );
        response.setContentType( MediaType.APPLICATION_XML_VALUE );

        JacksonUtils.toXml( response.getOutputStream(), importSummary );
    }

    @RequestMapping( value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_PROGRAM_UNENROLLMENT')" )
    public void updateEnrollmentJson( @PathVariable String id, HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        ImportSummary importSummary = enrollmentService.updateEnrollmentJson( id, request.getInputStream() );
        response.setContentType( MediaType.APPLICATION_JSON_VALUE );

        JacksonUtils.toJson( response.getOutputStream(), importSummary );
    }

    @RequestMapping( value = "/{id}/cancelled", method = RequestMethod.PUT )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_PROGRAM_UNENROLLMENT')" )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    public void cancelEnrollment( @PathVariable String id ) throws NotFoundException
    {
        Enrollment enrollment = getEnrollment( id );
        enrollment.setStatus( EnrollmentStatus.CANCELLED );

        enrollmentService.cancelEnrollment( enrollment );
    }

    @RequestMapping( value = "/{id}/completed", method = RequestMethod.PUT )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_PROGRAM_UNENROLLMENT')" )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    public void completedEnrollment( @PathVariable String id ) throws NotFoundException
    {
        Enrollment enrollment = getEnrollment( id );
        enrollment.setStatus( EnrollmentStatus.COMPLETED );

        enrollmentService.completeEnrollment( enrollment );
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @RequestMapping( value = "/{id}", method = RequestMethod.DELETE )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_PROGRAM_UNENROLLMENT')" )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    public void deleteEnrollment( @PathVariable String id ) throws NotFoundException
    {
        Enrollment enrollment = getEnrollment( id );
        enrollmentService.deleteEnrollment( enrollment );
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Enrollment getEnrollment( String id ) throws NotFoundException
    {
        Enrollment enrollment = enrollmentService.getEnrollment( id );

        if ( enrollment == null )
        {
            throw new NotFoundException( "Enrollment", id );
        }

        return enrollment;
    }

    private TrackedEntityInstance getTrackedEntityInstance( String id ) throws NotFoundException
    {
        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceService.getTrackedEntityInstance( id );

        if ( trackedEntityInstance == null )
        {
            throw new NotFoundException( "TrackedEntityInstance", id );
        }

        return trackedEntityInstance;
    }

    private OrganisationUnit getOrganisationUnit( String id ) throws NotFoundException
    {
        OrganisationUnit organisationUnit = manager.get( OrganisationUnit.class, id );

        if ( organisationUnit == null )
        {
            throw new NotFoundException( "OrganisationUnit", id );
        }

        return organisationUnit;
    }
    
    private List<OrganisationUnit> getOrganisationUnits( OrganisationUnit rootOrganisationUnit, OrganisationUnitSelectionMode ouMode )
    {
        List<OrganisationUnit> organisationUnits = new ArrayList<>();
        
        if ( OrganisationUnitSelectionMode.DESCENDANTS.equals( ouMode ) )
        {
            organisationUnits.addAll( organisationUnitService.getOrganisationUnitWithChildren( rootOrganisationUnit.getUid() ) );
        }
        else if ( OrganisationUnitSelectionMode.CHILDREN.equals( ouMode ) )
        {
            organisationUnits.add( rootOrganisationUnit );
            organisationUnits.addAll( rootOrganisationUnit.getChildren() );
        }
        else // SELECTED
        {
            organisationUnits.add( rootOrganisationUnit );
        }
        
        return organisationUnits;        
    }

    private Program getProgram( String id ) throws NotFoundException
    {
        Program program = manager.get( Program.class, id );

        if ( program == null )
        {
            throw new NotFoundException( "Program", id );
        }

        return program;
    }

    private String getResourcePath( HttpServletRequest request, ImportSummary importSummary )
    {
        return ContextUtils.getContextPath( request ) + "/api/" + "enrollments" + "/" + importSummary.getReference();
    }
}
