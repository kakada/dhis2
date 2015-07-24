package org.hisp.dhis.dxf2.events.report;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import org.hisp.dhis.common.BaseLinkableObject;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.dxf2.events.event.Note;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Abyot Asalefew Gizaw <abyota@gmail.com>
 *
 */

@JacksonXmlRootElement( localName = "eventRow", namespace = DxfNamespaces.DXF_2_0 )
public class EventRow
    extends BaseLinkableObject
{
    private String trackedEntityInstance;
    
    private List<Attribute> attributes = new ArrayList<>();
    
    private String event;
    
    private String program;
    
    private String programStage;

    private String enrollment;
    
    private String eventOrgUnitName;
    
    private String registrationOrgUnit;
    
    private String registrationDate;
    
    private String eventDate;
    
    private String dueDate;
    
    private Boolean followup;
    
    private List<Note> notes = new ArrayList<>();

    public EventRow()
    {
    }
    
    @JsonProperty
    @JacksonXmlProperty( isAttribute = true )
    public String getTrackedEntityInstance()
    {
        return trackedEntityInstance;
    }

    public void setTrackedEntityInstance( String trackedEntityInstance )
    {
        this.trackedEntityInstance = trackedEntityInstance;
    }    
    
    @JsonProperty
    @JacksonXmlProperty( isAttribute = true )
    public List<Attribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes( List<Attribute> attributes )
    {
        this.attributes = attributes;
    }    

    @JsonProperty
    @JacksonXmlProperty( isAttribute = true )
    public String getEvent()
    {
        return event;
    }

    public void setEvent( String event )
    {
        this.event = event;
    }  
    
    @JsonProperty
    @JacksonXmlProperty( isAttribute = true )
    public String getProgram()
    {
        return program;
    }

    public void setProgram( String program )
    {
        this.program = program;
    }

    @JsonProperty
    @JacksonXmlProperty( isAttribute = true )
    public String getProgramStage()
    {
        return programStage;
    }

    public void setProgramStage( String programStage )
    {
        this.programStage = programStage;
    }

    @JsonProperty
    @JacksonXmlProperty( isAttribute = true )
    public String getEnrollment()
    {
        return enrollment;
    }

    public void setEnrollment( String enrollment )
    {
        this.enrollment = enrollment;
    }

    @JsonProperty
    @JacksonXmlProperty( isAttribute = true )
    public String getRegistrationOrgUnit()
    {
        return registrationOrgUnit;
    }

    public void setRegistrationOrgUnit( String registrationOrgUnit )
    {
        this.registrationOrgUnit = registrationOrgUnit;
    }
    
    @JsonProperty
    @JacksonXmlProperty( isAttribute = true )
    public String getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate( String registrationDate )
    {
        this.registrationDate = registrationDate;
    }

    @JsonProperty
    @JacksonXmlProperty( isAttribute = true )
    public String getEventOrgUnitName()
    {
        return eventOrgUnitName;
    }

    public void setEventOrgUnitName( String eventOrgUnitName )
    {
        this.eventOrgUnitName = eventOrgUnitName;
    }    

    @JsonProperty( required = true )
    @JacksonXmlProperty( isAttribute = true )
    public String getEventDate()
    {
        return eventDate;
    }

    public void setEventDate( String eventDate )
    {
        this.eventDate = eventDate;
    }

    @JsonProperty
    @JacksonXmlProperty( isAttribute = true )
    public String getDueDate()
    {
        return dueDate;
    }

    public void setDueDate( String dueDate )
    {
        this.dueDate = dueDate;
    }

    
    @JsonProperty
    @JacksonXmlElementWrapper( localName = "notes", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "note", namespace = DxfNamespaces.DXF_2_0 )
    public List<Note> getNotes()
    {
        return notes;
    }

    public void setNotes( List<Note> notes )
    {
        this.notes = notes;
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Boolean getFollowup()
    {
        return followup;
    }

    public void setFollowup( Boolean followup )
    {
        this.followup = followup;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;

        EventRow eventRow1 = (EventRow) o;

        if ( event != null ? !event.equals( eventRow1.event ) : eventRow1.event != null )
            return false;
        if ( attributes != null ? !attributes.equals( eventRow1.attributes ) : eventRow1.attributes != null ) 
            return false;
        
        if ( eventDate != null ? !eventDate.equals( eventRow1.eventDate ) : eventRow1.eventDate != null )
            return false;
        if ( dueDate != null ? !dueDate.equals( eventRow1.dueDate ) : eventRow1.dueDate != null )
            return false;
        if ( eventOrgUnitName != null ? !eventOrgUnitName.equals( eventRow1.eventOrgUnitName ) : eventRow1.eventOrgUnitName != null )
            return false;        
        if ( registrationOrgUnit != null ? !registrationOrgUnit.equals( eventRow1.registrationOrgUnit ) : eventRow1.registrationOrgUnit != null )
            return false;
        if ( registrationDate != null ? !registrationDate.equals( eventRow1.registrationDate ) : eventRow1.registrationDate != null )
            return false;
        if ( trackedEntityInstance != null ? !trackedEntityInstance.equals( eventRow1.trackedEntityInstance )
            : eventRow1.trackedEntityInstance != null )
            return false;
        if ( program != null ? !program.equals( eventRow1.program ) : eventRow1.program != null )
            return false;
        if ( programStage != null ? !programStage.equals( eventRow1.programStage ) : eventRow1.programStage != null )
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = event != null ? event.hashCode() : 0;
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        result = 31 * result + (program != null ? program.hashCode() : 0);
        result = 31 * result + (programStage != null ? programStage.hashCode() : 0);
        result = 31 * result + (eventOrgUnitName != null ? eventOrgUnitName.hashCode() : 0);
        result = 31 * result + (registrationOrgUnit != null ? registrationOrgUnit.hashCode() : 0);
        result = 31 * result + (registrationDate != null ? registrationDate.hashCode() : 0);
        result = 31 * result + (trackedEntityInstance != null ? trackedEntityInstance.hashCode() : 0);
        result = 31 * result + (eventDate != null ? eventDate.hashCode() : 0);
        result = 31 * result + (dueDate != null ? dueDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "Event{" + 
            "event='" + event + '\'' +
            ", attributes=" + attributes +
            ", program='" + program + '\'' + 
            ", programStage='" + programStage + '\'' + 
            ", eventOrgUnitName='" + eventOrgUnitName + '\'' + 
            ", registrationOrgUnit='" + registrationOrgUnit + '\'' +
            ", registrationDate='" + registrationDate + '\'' +
            ", trackedEntityInstance='" + trackedEntityInstance + '\'' + 
            ", eventDate='" + eventDate + '\'' + 
            ", dueDate='" + dueDate + '\'' +            
            '}';
    }
}
