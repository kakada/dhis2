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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hisp.dhis.common.Grid;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;

/**
 * @author Abyot Asalefew
 * @version $Id$
 */
public interface ProgramStageInstanceService
{
    String ID = ProgramStageInstanceService.class.getName();

    /**
     * Adds an {@link TrackedEntityAttribute}
     *
     * @param programStageInstance The to TrackedEntityAttribute add.
     * @return A generated unique id of the added {@link TrackedEntityAttribute}.
     */
    int addProgramStageInstance( ProgramStageInstance programStageInstance );

    /**
     * Deletes a {@link TrackedEntityAttribute}.
     *
     * @param programStageInstance the TrackedEntityAttribute to delete.
     */
    void deleteProgramStageInstance( ProgramStageInstance programStageInstance );

    /**
     * Updates an {@link TrackedEntityAttribute}.
     *
     * @param programStageInstance the TrackedEntityAttribute to update.
     */
    void updateProgramStageInstance( ProgramStageInstance programStageInstance );

    /**
     * Returns a {@link TrackedEntityAttribute}.
     *
     * @param id the id of the TrackedEntityAttribute to return.
     * @return the TrackedEntityAttribute with the given id
     */
    ProgramStageInstance getProgramStageInstance( int id );

    /**
     * Returns the {@link TrackedEntityAttribute} with the given UID.
     *
     * @param uid the UID.
     * @return the TrackedEntityAttribute with the given UID, or null if no match.
     */
    ProgramStageInstance getProgramStageInstance( String uid );

    /**
     * Retrieve an event on a program instance and a program stage. For
     * repeatable stage, the system returns the last event
     *
     * @param programInstance ProgramInstance
     * @param programStage    ProgramStage
     * @return ProgramStageInstance
     */
    ProgramStageInstance getProgramStageInstance( ProgramInstance programInstance, ProgramStage programStage );

    /**
     * Retrieve an event list on program instance list with a certain status
     *
     * @param programInstances ProgramInstance list
     * @param status        EventStatus
     * @return ProgramStageInstance list
     */
    Collection<ProgramStageInstance> getProgramStageInstances( Collection<ProgramInstance> programInstances,
        EventStatus status );

    /**
     * Get all events by TrackedEntityInstance, optionally filtering by completed.
     *
     * @param entityInstance TrackedEntityInstance
     * @param status      EventStatus
     * @return ProgramStageInstance list
     */
    List<ProgramStageInstance> getProgramStageInstances( TrackedEntityInstance entityInstance, EventStatus status );

    /**
     * Gets the number of ProgramStageInstances added since the given number of days.
     *
     * @param days number of days.
     * @return the number of ProgramStageInstances.
     */
    long getProgramStageInstanceCount( int days );

    /**
     * Retrieve scheduled list of entityInstances registered
     *
     * @return A SchedulingProgramObject list
     */
    Collection<SchedulingProgramObject> getSendMesssageEvents();

    /**
     * Get/export statistical report of a program
     *
     * @param program    Program needs to report
     * @param orgunitIds The ids of orgunits where the events happened
     * @param startDate  Optional date the instance should be on or after.
     * @param endDate    Optional date the instance should be on or before.
     * @param i18n       I18n object
     * @param format     I18nFormat
     * @return Program report
     */
    Grid getStatisticalReport( Program program, Collection<Integer> orgunitIds, Date startDate, Date endDate,
        I18n i18n, I18nFormat format );

    /**
     * Get/Export a report about the number of events of a program completed on
     * a orgunit
     *
     * @param orgunits  The ids of orgunits where the events happened
     * @param program   The program needs for reporting
     * @param startDate Optional date the instance should be on or after.
     * @param endDate   Optional date the instance should be on or before.
     * @return Grid
     */
    Grid getCompletenessProgramStageInstance( Collection<Integer> orgunits, Program program, String startDate,
        String endDate, I18n i18n );

    /**
     * Complete an event. Besides, program template messages will be send if it
     * was defined to send when to complete this program
     *
     * @param programStageInstance ProgramStageInstance
     * @param format               I18nFormat
     */
    void completeProgramStageInstance( ProgramStageInstance programStageInstance, I18nFormat format );

    /**
     * Set report date and orgunit where an event happened for the event
     *
     * @param programStageInstance ProgramStageInstance
     * @param executionDate        Report date
     * @param organisationUnit     Orgunit where the event happens
     */
    void setExecutionDate( ProgramStageInstance programStageInstance, Date executionDate,
        OrganisationUnit organisationUnit );

    /**
     * For the first case of an anonymous program, the program-instance doesn't
     * exist, So system has to create a program-instance and
     * program-stage-instance. The similar thing happens for single event with
     * registration.
     *
     * @param entityInstance   TrackedEntityInstance
     * @param program          Single event without registration
     * @param executionDate    Report date of the event
     * @param organisationUnit Orgunit where the event happens
     * @return ProgramStageInstance ProgramStageInstance object
     */
    ProgramStageInstance createProgramStageInstance( TrackedEntityInstance entityInstance, Program program, Date executionDate,
        OrganisationUnit organisationUnit );
}
