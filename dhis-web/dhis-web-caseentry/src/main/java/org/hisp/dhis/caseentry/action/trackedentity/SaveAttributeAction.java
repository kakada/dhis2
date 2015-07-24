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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * 
 * @version $SaveAttributeAction.java Mar 29, 2012 10:33:00 AM$
 */
public class SaveAttributeAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityInstanceService entityInstanceService;

    private TrackedEntityAttributeValueService attributeValueService;
    
    private ProgramService programService;

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private String programId;

    private Integer entityInstanceId;

    private Integer statusCode;

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    public void setEntityInstanceService( TrackedEntityInstanceService entityInstanceService )
    {
        this.entityInstanceService = entityInstanceService;
    }

    public void setAttributeValueService( TrackedEntityAttributeValueService attributeValueService )
    {
        this.attributeValueService = attributeValueService;
    }

    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }

    public void setProgramId( String programId )
    {
        this.programId = programId;
    }

    public String getProgramId()
    {
        return programId;
    }

    public Integer getEntityInstanceId()
    {
        return entityInstanceId;
    }

    public void setEntityInstanceId( Integer entityInstanceId )
    {
        this.entityInstanceId = entityInstanceId;
    }

    public Integer getStatusCode()
    {
        return statusCode;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        TrackedEntityInstance entityInstance = entityInstanceService.getTrackedEntityInstance( entityInstanceId );
        Program program = programService.getProgram( programId );

        saveAttributeValues( entityInstance, program );

        statusCode = 0;

        return SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void saveAttributeValues( TrackedEntityInstance entityInstance, Program program )
    {
        HttpServletRequest request = ServletActionContext.getRequest();

        String value = null;

        Collection<ProgramTrackedEntityAttribute> programAttributes = program.getProgramAttributes();
        
        TrackedEntityAttributeValue attributeValue = null;

        if ( programAttributes != null && programAttributes.size() > 0 )
        {
            for ( ProgramTrackedEntityAttribute programAttribute : programAttributes )
            {
                value = request.getParameter( AddTrackedEntityInstanceAction.PREFIX_ATTRIBUTE + programAttribute.getAttribute().getId() );
                attributeValue = attributeValueService.getTrackedEntityAttributeValue( entityInstance, programAttribute.getAttribute() );

                if ( StringUtils.isNotBlank( value ) )
                {
                    if ( attributeValue == null )
                    {
                        attributeValue = new TrackedEntityAttributeValue();
                        attributeValue.setEntityInstance( entityInstance );
                        attributeValue.setAttribute( programAttribute.getAttribute() );
                        attributeValue.setValue( value.trim() );
                        attributeValueService.addTrackedEntityAttributeValue( attributeValue );
                    }
                    else
                    {
                        attributeValue.setValue( value.trim() );
                        attributeValueService.updateTrackedEntityAttributeValue( attributeValue );
                    }

                    entityInstance.getAttributeValues().add( attributeValue );
                }
                else if ( attributeValue != null )
                {
                    entityInstance.getAttributeValues().remove( attributeValue );
                    attributeValueService.deleteTrackedEntityAttributeValue( attributeValue );
                }
            }
        }

        entityInstanceService.updateTrackedEntityInstance( entityInstance );
    }

}
