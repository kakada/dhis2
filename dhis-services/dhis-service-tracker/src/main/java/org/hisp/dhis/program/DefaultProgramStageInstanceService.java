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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.GridHeader;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.message.MessageConversation;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.sms.SmsSender;
import org.hisp.dhis.sms.SmsServiceException;
import org.hisp.dhis.sms.outbound.OutboundSms;
import org.hisp.dhis.system.grid.ListGrid;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceReminder;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceReminderService;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Abyot Asalefew
 */
@Transactional
public class DefaultProgramStageInstanceService
    implements ProgramStageInstanceService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ProgramStageInstanceStore programStageInstanceStore;

    public void setProgramStageInstanceStore( ProgramStageInstanceStore programStageInstanceStore )
    {
        this.programStageInstanceStore = programStageInstanceStore;
    }

    private ProgramInstanceService programInstanceService;

    public void setProgramInstanceService( ProgramInstanceService programInstanceService )
    {
        this.programInstanceService = programInstanceService;
    }

    private SmsSender smsSender;

    public void setSmsSender( SmsSender smsSender )
    {
        this.smsSender = smsSender;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private TrackedEntityInstanceReminderService reminderService;

    public void setReminderService( TrackedEntityInstanceReminderService reminderService )
    {
        this.reminderService = reminderService;
    }

    private MessageService messageService;

    public void setMessageService( MessageService messageService )
    {
        this.messageService = messageService;
    }

    // -------------------------------------------------------------------------
    // Implementation methods
    // -------------------------------------------------------------------------

    @Override
    public int addProgramStageInstance( ProgramStageInstance programStageInstance )
    {
        programStageInstance.setAutoFields();
        return programStageInstanceStore.save( programStageInstance );
    }

    @Override
    public void deleteProgramStageInstance( ProgramStageInstance programStageInstance )
    {
        programStageInstanceStore.delete( programStageInstance );
    }

    @Override
    public ProgramStageInstance getProgramStageInstance( int id )
    {
        return programStageInstanceStore.get( id );
    }

    @Override
    public ProgramStageInstance getProgramStageInstance( String uid )
    {
        return programStageInstanceStore.getByUid( uid );
    }

    @Override
    public ProgramStageInstance getProgramStageInstance( ProgramInstance programInstance, ProgramStage programStage )
    {
        return programStageInstanceStore.get( programInstance, programStage );
    }
    
    @Override
    public void updateProgramStageInstance( ProgramStageInstance programStageInstance )
    {
        programStageInstance.setAutoFields();
        programStageInstanceStore.update( programStageInstance );
    }

    @Override
    public Collection<ProgramStageInstance> getProgramStageInstances( Collection<ProgramInstance> programInstances,
        EventStatus status )
    {
        return programStageInstanceStore.get( programInstances, status );
    }

    @Override
    public List<ProgramStageInstance> getProgramStageInstances( TrackedEntityInstance entityInstance, EventStatus status )
    {
        return programStageInstanceStore.get( entityInstance, status );
    }

    @Override
    public long getProgramStageInstanceCount( int days )
    {
        Calendar cal = PeriodType.createCalendarInstance();
        cal.add( Calendar.DAY_OF_YEAR, ( days * -1 ) );

        return programStageInstanceStore.getProgramStageInstanceCountLastUpdatedAfter( cal.getTime() );
    }

    @Override
    public Collection<SchedulingProgramObject> getSendMesssageEvents()
    {
        return programStageInstanceStore.getSendMesssageEvents();
    }

    @Override
    public Grid getStatisticalReport( Program program, Collection<Integer> orgunitIds, Date startDate, Date endDate,
        I18n i18n, I18nFormat format )
    {
        Grid grid = new ListGrid();
        grid.setTitle( i18n.getString( "program_overview" ) + " - " + program.getDisplayName() );
        grid.setSubtitle( i18n.getString( "from" ) + " " + format.formatDate( startDate ) + "  "
            + i18n.getString( "to" ) + " " + format.formatDate( endDate ) );

        grid.addHeader( new GridHeader( "", false, true ) );
        grid.addHeader( new GridHeader( "", false, false ) );
        grid.addHeader( new GridHeader( "", false, false ) );
        grid.addHeader( new GridHeader( "", false, false ) );
        grid.addHeader( new GridHeader( "", false, false ) );
        grid.addHeader( new GridHeader( "", false, false ) );
        grid.addHeader( new GridHeader( "", false, false ) );
        grid.addHeader( new GridHeader( "", false, false ) );

        // Total new enrollments in the period

        int total = programInstanceService.countProgramInstances( program, orgunitIds, startDate, endDate );
        grid.addRow();
        grid.addValue( i18n.getString( "total_new_enrollments_in_this_period" ) );
        grid.addValue( total ).addEmptyValues( 6 );

        // Total programs completed in this period

        int totalCompleted = programInstanceService.countProgramInstancesByStatus( ProgramInstance.STATUS_COMPLETED,
            program, orgunitIds, startDate, endDate );
        grid.addRow();
        grid.addValue( i18n.getString( "total_programs_completed_in_this_period" ) );
        grid.addValue( totalCompleted ).addEmptyValues( 6 );

        // Total programs discontinued (un-enrollments)

        int totalDiscontinued = programInstanceService.countProgramInstancesByStatus( ProgramInstance.STATUS_CANCELLED,
            program, orgunitIds, startDate, endDate );
        grid.addRow();
        grid.addValue( i18n.getString( "total_programs_discontinued_unenrollments" ) );
        grid.addValue( totalDiscontinued ).addEmptyValues( 6 );

        // Average number of stages for complete programs

        grid.addRow();
        grid.addValue( i18n.getString( "average_number_of_stages_for_complete_programs" ) );
        double percent = 0.0;
        if ( totalCompleted != 0 )
        {
           int stageCompleted =  programStageInstanceStore.averageNumberCompleted( program, orgunitIds, startDate, endDate,
                ProgramInstance.STATUS_ACTIVE );
            percent = (stageCompleted + 0.0) / totalCompleted;
        }
        grid.addValue( MathUtils.getRounded( percent ) ).addEmptyValues( 6 );

        // Add empty row

        grid.addRow().addEmptyValues( 8 );

        // Summary by stage

        grid.addRow();
        grid.addValue( i18n.getString( "summary_by_stage" ) ).addEmptyValues( 7 );

        // Add titles for stage details

        grid.addRow();
        grid.addValue( i18n.getString( "program_stages" ) );
        grid.addValue( i18n.getString( "visits_scheduled_all" ) );
        grid.addValue( i18n.getString( "visits_done" ) );
        grid.addValue( i18n.getString( "visits_done_percent" ) );
        grid.addValue( i18n.getString( "forms_completed" ) );
        grid.addValue( i18n.getString( "forms_completed_percent" ) );
        grid.addValue( i18n.getString( "visits_overdue" ) );
        grid.addValue( i18n.getString( "visits_overdue_percent" ) );

        // Add values for stage details

        for ( ProgramStage programStage : program.getProgramStages() )
        {
            grid.addRow();
            grid.addValue( programStage.getDisplayName() );

            // Visits scheduled (All)
            int totalAll = programStageInstanceStore.count( programStage, orgunitIds, startDate, endDate, null );
            grid.addValue( totalAll );

            // Visits done (#) = Incomplete + Complete stages.
            int totalCompletedEvent = programStageInstanceStore.count( programStage, orgunitIds, startDate, endDate,
                true );
            int totalVisit = totalCompletedEvent
                + programStageInstanceStore.count( programStage, orgunitIds, startDate, endDate, false );
            grid.addValue( totalVisit );

            // Visits done (%)

            percent = 0.0;
            if ( totalAll != 0 )
            {
                percent = (totalVisit + 0.0) * 100 / totalAll;
            }
            grid.addValue( MathUtils.getRounded( percent ) + "%" );

            // Forms completed (#) = Program stage instances where the user has
            // clicked complete.

            grid.addValue( totalCompletedEvent );

            // Forms completed (%)
            if ( totalAll != 0 )
            {
                percent = (totalCompletedEvent + 0.0) * 100 / totalAll;
            }
            grid.addValue( MathUtils.getRounded( percent ) + "%" );

            // Visits overdue (#)
            int overdue = programStageInstanceStore.getOverDueCount( programStage, orgunitIds, startDate, endDate );
            grid.addValue( overdue );

            // Visits overdue (%)

            percent = 0.0;
            if ( totalAll != 0 )
            {
                percent = (overdue + 0.0) * 100 / totalAll;
            }
            grid.addValue( MathUtils.getRounded( percent ) + "%" );
        }

        return grid;
    }

    @Override
    public Grid getCompletenessProgramStageInstance( Collection<Integer> orgunitIds, Program program, String startDate,
        String endDate, I18n i18n )
    {
        return programStageInstanceStore.getCompleteness( orgunitIds, program, startDate, endDate, i18n );
    }

    @Override
    public void completeProgramStageInstance( ProgramStageInstance programStageInstance, I18nFormat format )
    {
        Calendar today = Calendar.getInstance();
        PeriodType.clearTimeOfDay( today );
        Date date = today.getTime();

        programStageInstance.setStatus( EventStatus.COMPLETED );
        programStageInstance.setCompletedDate( date );
        programStageInstance.setCompletedUser( currentUserService.getCurrentUsername() );

        // ---------------------------------------------------------------------
        // Send sms-message when to completed the event
        // ---------------------------------------------------------------------

        List<OutboundSms> outboundSms = programStageInstance.getOutboundSms();

        if ( outboundSms == null )
        {
            outboundSms = new ArrayList<>();
        }

        outboundSms.addAll( sendMessages( programStageInstance,
            TrackedEntityInstanceReminder.SEND_WHEN_TO_C0MPLETED_EVENT, format ) );

        // ---------------------------------------------------------------------
        // Send DHIS message when to completed the event
        // ---------------------------------------------------------------------

        List<MessageConversation> messageConversations = programStageInstance.getMessageConversations();

        if ( messageConversations == null )
        {
            messageConversations = new ArrayList<>();
        }

        messageConversations.addAll( sendMessageConversations( programStageInstance,
            TrackedEntityInstanceReminder.SEND_WHEN_TO_C0MPLETED_EVENT, format ) );

        // ---------------------------------------------------------------------
        // Update the event
        // ---------------------------------------------------------------------

        updateProgramStageInstance( programStageInstance );
        
        // ---------------------------------------------------------------------
        // Check Completed status for all of ProgramStageInstance of
        // ProgramInstance
        // ---------------------------------------------------------------------

        if ( !programStageInstance.getProgramInstance().getProgram().getType()
            .equals( Program.SINGLE_EVENT_WITHOUT_REGISTRATION ) )
        {
            boolean canCompleted = programInstanceService.canAutoCompleteProgramInstanceStatus( programStageInstance
                .getProgramInstance() );
            if ( canCompleted )
            {
                programInstanceService.completeProgramInstanceStatus( programStageInstance.getProgramInstance() );
            }
        }

    }

    @Override
    public void setExecutionDate( ProgramStageInstance programStageInstance, Date executionDate,
        OrganisationUnit organisationUnit )
    {
        programStageInstance.setExecutionDate( executionDate );
        programStageInstance.setOrganisationUnit( organisationUnit );

        if ( programStageInstance.getProgramInstance().getProgram().isSingleEvent() )
        {
            programStageInstance.setDueDate( executionDate );
        }

        updateProgramStageInstance( programStageInstance );
    }

    /**
     * For the first case of an anonymous program, the program-instance doesn't
     * exist, So system has to create a program-instance and
     * program-stage-instance. The similar thing happens for single event with
     * registration.
     */
    @Override
    public ProgramStageInstance createProgramStageInstance( TrackedEntityInstance instance, Program program,
        Date executionDate, OrganisationUnit organisationUnit )
    {
        ProgramStage programStage = null;

        if ( program.getProgramStages() != null )
        {
            programStage = program.getProgramStages().iterator().next();
        }

        int type = program.getType();
        ProgramInstance programInstance = null;

        if ( type == Program.SINGLE_EVENT_WITH_REGISTRATION )
        {
            // Add a new program instance
            programInstance = new ProgramInstance();
            programInstance.setEnrollmentDate( executionDate );
            programInstance.setDateOfIncident( executionDate );
            programInstance.setProgram( program );
            programInstance.setStatus( ProgramInstance.STATUS_ACTIVE );
            programInstance.setEntityInstance( instance );

            programInstanceService.addProgramInstance( programInstance );
        }
        else if ( type == Program.SINGLE_EVENT_WITHOUT_REGISTRATION )
        {
            Collection<ProgramInstance> programInstances = programInstanceService.getProgramInstances( program );
            if ( programInstances == null || programInstances.size() == 0 )
            {
                // Add a new program instance if it doesn't exist
                programInstance = new ProgramInstance();
                programInstance.setEnrollmentDate( executionDate );
                programInstance.setDateOfIncident( executionDate );
                programInstance.setProgram( program );
                programInstance.setStatus( ProgramInstance.STATUS_ACTIVE );
                programInstanceService.addProgramInstance( programInstance );
            }
            else
            {
                programInstance = programInstanceService.getProgramInstances( program ).iterator().next();
            }
        }

        // Add a new program stage instance
        ProgramStageInstance programStageInstance = new ProgramStageInstance();
        programStageInstance.setProgramInstance( programInstance );
        programStageInstance.setProgramStage( programStage );
        programStageInstance.setDueDate( executionDate );
        programStageInstance.setExecutionDate( executionDate );
        programStageInstance.setOrganisationUnit( organisationUnit );

        addProgramStageInstance( programStageInstance );

        return programStageInstance;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private OutboundSms sendEventMessage( TrackedEntityInstanceReminder reminder,
        ProgramStageInstance programStageInstance, TrackedEntityInstance entityInstance, I18nFormat format )
    {
        Set<String> phoneNumbers = reminderService.getPhonenumbers( reminder, entityInstance );
        OutboundSms outboundSms = null;

        if ( phoneNumbers.size() > 0 )
        {
            String msg = reminderService.getMessageFromTemplate( reminder, programStageInstance, format );
            try
            {
                outboundSms = new OutboundSms();
                outboundSms.setMessage( msg );
                outboundSms.setRecipients( phoneNumbers );
                outboundSms.setSender( currentUserService.getCurrentUsername() );
                smsSender.sendMessage( outboundSms, null );
            }
            catch ( SmsServiceException e )
            {
                e.printStackTrace();
            }
        }

        return outboundSms;
    }
    
    private Collection<OutboundSms> sendMessages( ProgramStageInstance programStageInstance, int status,
        I18nFormat format )
    {
        TrackedEntityInstance entityInstance = programStageInstance.getProgramInstance().getEntityInstance();
        Collection<OutboundSms> outboundSmsList = new HashSet<>();

        Collection<TrackedEntityInstanceReminder> reminders = programStageInstance.getProgramStage()
            .getReminders();
        for ( TrackedEntityInstanceReminder rm : reminders )
        {
            if ( rm != null
                && rm.getWhenToSend() != null
                && rm.getWhenToSend() == status
                && (rm.getMessageType() == TrackedEntityInstanceReminder.MESSAGE_TYPE_DIRECT_SMS || rm.getMessageType() == TrackedEntityInstanceReminder.MESSAGE_TYPE_BOTH) )
            {
                OutboundSms outboundSms = sendEventMessage( rm, programStageInstance, entityInstance, format );
                if ( outboundSms != null )
                {
                    outboundSmsList.add( outboundSms );
                }
            }
        }

        return outboundSmsList;
    }

    private Collection<MessageConversation> sendMessageConversations( ProgramStageInstance programStageInstance,
         int status, I18nFormat format )
     {
         Collection<MessageConversation> messageConversations = new HashSet<>();

         Collection<TrackedEntityInstanceReminder> reminders = programStageInstance.getProgramStage()
             .getReminders();
         for ( TrackedEntityInstanceReminder rm : reminders )
         {
             if ( rm != null
                 && rm.getWhenToSend() != null
                 && rm.getWhenToSend() == status
                 && (rm.getMessageType() == TrackedEntityInstanceReminder.MESSAGE_TYPE_DHIS_MESSAGE || rm
                     .getMessageType() == TrackedEntityInstanceReminder.MESSAGE_TYPE_BOTH) )
             {
                 int id = messageService.sendMessage( programStageInstance.getProgramStage().getDisplayName(),
                     reminderService.getMessageFromTemplate( rm, programStageInstance, format ), null,
                     reminderService.getUsers( rm, programStageInstance.getProgramInstance().getEntityInstance() ),
                     null, false, true );
                 messageConversations.add( messageService.getMessageConversation( id ) );
             }
         }

         return messageConversations;
     }
}
