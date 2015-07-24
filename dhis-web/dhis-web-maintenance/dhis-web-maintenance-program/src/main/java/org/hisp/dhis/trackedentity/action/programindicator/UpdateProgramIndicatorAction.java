package org.hisp.dhis.trackedentity.action.programindicator;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.hisp.dhis.program.ProgramIndicator;
import org.hisp.dhis.program.ProgramIndicatorService;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * @version $ UpdateProgramIndicatorAction Apr 16, 2013 3:24:51 PM $
 */
public class UpdateProgramIndicatorAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ProgramIndicatorService programIndicatorService;

    public void setProgramIndicatorService( ProgramIndicatorService programIndicatorService )
    {
        this.programIndicatorService = programIndicatorService;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    private Integer id;

    public void setId( Integer id )
    {
        this.id = id;
    }

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private String code;

    public void setCode( String code )
    {
        this.code = code;
    }

    private String description;

    public void setDescription( String description )
    {
        this.description = description;
    }

    private String valueType;

    public void setValueType( String valueType )
    {
        this.valueType = valueType;
    }

    private String expression;

    public void setExpression( String expression )
    {
        this.expression = expression;
    }

    private String rootDate;

    public void setRootDate( String rootDate )
    {
        this.rootDate = rootDate;
    }

    private String shortName;

    public void setShortName( String shortName )
    {
        this.shortName = shortName;
    }

    private Integer programId;

    public Integer getProgramId()
    {
        return programId;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        code = (code == null && code.trim().length() == 0) ? null : code;
        expression = expression.trim();

        if ( valueType.equals( ProgramIndicator.VALUE_TYPE_DATE ) )
        {
            Pattern pattern = Pattern.compile( "[(+|-|*|\\)]+" );
            Matcher matcher = pattern.matcher( expression );
            if ( matcher.find() && matcher.start() != 0 )
            {
                expression = "+" + expression;
            }
        }

        ProgramIndicator indicator = programIndicatorService.getProgramIndicator( id );

        indicator.setName( StringUtils.trimToNull( name ) );
        indicator.setShortName( StringUtils.trimToNull( shortName ) );
        indicator.setCode( StringUtils.trimToNull( code ) );
        indicator.setDescription( StringUtils.trimToNull( description ) );
        indicator.setValueType( StringUtils.trimToNull( valueType ) );
        indicator.setExpression( StringUtils.trimToNull( expression ) );
        indicator.setRootDate( StringUtils.trimToNull( rootDate ) );

        programIndicatorService.updateProgramIndicator( indicator );

        programId = indicator.getProgram().getId();

        return SUCCESS;
    }

}
