package org.hisp.dhis.program;

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

import org.hisp.dhis.common.Grid;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Abyot Asalefew
 * @version $Id$
 */
public interface ProgramInstanceService
{
    String ID = ProgramInstanceService.class.getName();

    /**
     * Adds an {@link ProgramInstance}
     *
     * @param programInstance The to ProgramInstance add.
     * @return A generated unique id of the added {@link ProgramInstance}.
     */
    int addProgramInstance( ProgramInstance programInstance );

    /**
     * Deletes a {@link ProgramInstance}.
     *
     * @param programInstance the ProgramInstance to delete.
     */
    void deleteProgramInstance( ProgramInstance programInstance );

    /**
     * Updates an {@link ProgramInstance}.
     *
     * @param programInstance the ProgramInstance to update.
     */
    void updateProgramInstance( ProgramInstance programInstance );

    /**
     * Returns a {@link ProgramInstance}.
     *
     * @param id the id of the ProgramInstance to return.
     * @return the ProgramInstance with the given id
     */
    ProgramInstance getProgramInstance( int id );

    /**
     * Returns the {@link ProgramInstance} with the given UID.
     *
     * @param uid the UID.
     * @return the ProgramInstance with the given UID, or null if no match.
     */
    ProgramInstance getProgramInstance( String uid );

    /**
     * Retrieve program instances on a program
     *
     * @param program Program
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstances( Program program );

    /**
     * Retrieve program instances on program list
     *
     * @param programs Program list
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstances( Collection<Program> programs );

    /**
     * Retrieve program instances of whom registered in to a orgunit from
     * program list
     *
     * @param programs         Program list
     * @param organisationUnit Organisation Unit
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstances( Collection<Program> programs, OrganisationUnit organisationUnit );

    /**
     * Retrieve program instances of whom registered in to a orgunit from
     * program list with a certain status
     *
     * @param programs         Program list
     * @param organisationUnit Organisation Unit
     * @param status           Status of program-instance, include STATUS_ACTIVE,
     *                         STATUS_COMPLETED and STATUS_CANCELLED
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstances( Collection<Program> programs, OrganisationUnit organisationUnit,
        int status );

    /**
     * Retrieve program instances on a program by status
     *
     * @param program Program
     * @param status  Status of program-instance, include STATUS_ACTIVE,
     *                STATUS_COMPLETED and STATUS_CANCELLED
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstances( Program program, Integer status );

    /**
     * Retrieve program instances on a program list by status
     *
     * @param programs Program list
     * @param status   Status of program-instance, include STATUS_ACTIVE,
     *                 STATUS_COMPLETED and STATUS_CANCELLED
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstances( Collection<Program> programs, Integer status );

    /**
     * Retrieve program instances on a TrackedEntityInstance by a status
     *
     * @param entityInstance TrackedEntityInstance
     * @param status         Status of program-instance, include STATUS_ACTIVE,
     *                       STATUS_COMPLETED and STATUS_CANCELLED
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstances( TrackedEntityInstance entityInstance, Integer status );

    /**
     * Retrieve program instances on a TrackedEntityInstance by a program
     *
     * @param entityInstance TrackedEntityInstance
     * @param program        Program
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstances( TrackedEntityInstance entityInstance, Program program );

    /**
     * Retrieve program instances on a TrackedEntityInstance with a status by a program
     *
     * @param entityInstance TrackedEntityInstance
     * @param program        Program
     * @param status         Status of program-instance, include STATUS_ACTIVE,
     *                       STATUS_COMPLETED and STATUS_CANCELLED
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstances( TrackedEntityInstance entityInstance, Program program, Integer status );

    /**
     * Retrieve program instances with active status on an orgunit by a program
     * with result limited
     *
     * @param program          Program
     * @param organisationUnit Organisation Unit
     * @param min              First result
     * @param max              Maximum results
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstances( Program program, OrganisationUnit organisationUnit, Integer min,
        Integer max );

    /**
     * Retrieve program instances with active status on an orgunit by a program
     * for a certain period with result limited
     *
     * @param program    Program
     * @param orgunitIds Organisation Units
     * @param startDate  The start date for retrieving on enrollment-date
     * @param endDate    The end date for retrieving on enrollment-date
     * @param min        First result
     * @param max        Maximum results
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstances( Program program, Collection<Integer> orgunitIds, Date startDate,
        Date endDate, Integer min, Integer max );

    /**
     * Get the number of program instances which are active status and
     * registered in a certain orgunit by a program for a certain period
     *
     * @param program    Program
     * @param orgunitIds Organisation Units
     * @param startDate  The start date for retrieving on enrollment-date
     * @param endDate    The end date for retrieving on enrollment-date
     * @return ProgramInstance list
     */
    int countProgramInstances( Program program, Collection<Integer> orgunitIds, Date startDate, Date endDate );

    /**
     * Retrieve history of a TrackedEntityInstance
     *
     * @param entityInstance TrackedEntityInstance
     * @param i18n           I18n object
     * @return Grid list in which each grid is the program information details
     * of the TrackedEntityInstance
     */
    List<Grid> getProgramInstanceReport( TrackedEntityInstance entityInstance, I18n i18n );

    /**
     * Export a program information details report
     *
     * @param programInstance ProgramInstance
     * @param i18n            I18n object
     * @return Grid object
     */
    Grid getProgramInstanceReport( ProgramInstance programInstance, I18n i18n );

    /**
     * Retrieve program instances with a certain status on a program and an
     * orgunit ids list for a period
     *
     * @param status     of program-instance, include STATUS_ACTIVE,
     *                   STATUS_COMPLETED and STATUS_CANCELLED
     * @param program    ProgramInstance
     * @param orgunitIds A list of orgunit ids
     * @param startDate  The start date for retrieving on enrollment-date
     * @param endDate    The end date for retrieving on enrollment-date
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstancesByStatus( Integer status, Program program,
        Collection<Integer> orgunitIds, Date startDate, Date endDate );

    /**
     * Retrieve program instances with a certain status on a program and an
     * orgunit ids list for a period
     *
     * @param status     of program-instance, include STATUS_ACTIVE,
     *                   STATUS_COMPLETED and STATUS_CANCELLED
     * @param program    ProgramInstance
     * @param orgunitIds A list of orgunit ids
     * @param startDate  The start date for retrieving on enrollment-date
     * @param endDate    The end date for retrieving on enrollment-date
     * @return ProgramInstance list
     */
    Collection<ProgramInstance> getProgramInstancesByStatus( Integer status, Program program,
        Collection<Integer> orgunitIds, Date startDate, Date endDate, Integer min, Integer max );

    /**
     * Get the number of program instances of a program which have a certain
     * status and an orgunit ids list for a period
     *
     * @param status     of program-instance, include STATUS_ACTIVE,
     *                   STATUS_COMPLETED and STATUS_CANCELLED
     * @param program    ProgramInstance
     * @param orgunitIds A list of orgunit ids
     * @param startDate  The start date for retrieving on enrollment-date
     * @param endDate    The end date for retrieving on enrollment-date
     * @return A number
     */
    int countProgramInstancesByStatus( Integer status, Program program, Collection<Integer> orgunitIds, Date startDate,
        Date endDate );


    /**
     * Retrieve scheduled list of entityInstances registered
     *
     * @return A SchedulingProgramObject list
     */
    Collection<SchedulingProgramObject> getScheduleMesssages();

    /**
     * Enroll a TrackedEntityInstance into a program. Must be run inside a transaction.
     *
     * @param uid UID to use for new instance
     * @param entityInstance TrackedEntityInstance
     * @param program        Program
     * @param enrollmentDate The date of enrollment
     * @param dateOfIncident The date of incident
     * @param orgunit        Organisation Unit
     * @return ProgramInsance
     */
    ProgramInstance enrollTrackedEntityInstance( String uid, TrackedEntityInstance entityInstance, Program program, Date enrollmentDate, Date dateOfIncident,
        OrganisationUnit orgunit );

    /**
     * Enroll a TrackedEntityInstance into a program. Must be run inside a transaction.
     *
     * @param entityInstance TrackedEntityInstance
     * @param program        Program
     * @param enrollmentDate The date of enrollment
     * @param dateOfIncident The date of incident
     * @param orgunit        Organisation Unit
     * @return ProgramInsance
     */
    ProgramInstance enrollTrackedEntityInstance( TrackedEntityInstance entityInstance, Program program, Date enrollmentDate, Date dateOfIncident,
        OrganisationUnit orgunit );

    /**
     * Check a program instance if it can be completed automatically. If there
     * is some event of this program-isntance uncompleted or this program has
     * any repeatable stage, then this program cannot be completed automatically
     *
     * @param programInstance ProgramInstance
     * @return True/False value
     */
    boolean canAutoCompleteProgramInstanceStatus( ProgramInstance programInstance );

    /**
     * Complete a program instance. Besides, program template messages will be
     * send if it was defined to send when to complete this program
     *
     * @param programInstance ProgramInstance
     */
    void completeProgramInstanceStatus( ProgramInstance programInstance );

    /**
     * Set status as skipped for overdue events; Remove scheduled events
     *
     * @param programInstance ProgramInstance
     */
    void cancelProgramInstanceStatus( ProgramInstance programInstance );
}
