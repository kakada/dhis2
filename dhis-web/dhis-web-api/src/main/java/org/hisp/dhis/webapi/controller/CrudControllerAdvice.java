package org.hisp.dhis.webapi.controller;

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

import org.hisp.dhis.common.DeleteNotAllowedException;
import org.hisp.dhis.common.IllegalQueryException;
import org.hisp.dhis.common.MaintenanceModeException;
import org.hisp.dhis.dataapproval.exceptions.DataApprovalException;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.webapi.controller.exception.NotAuthenticatedException;
import org.hisp.dhis.webapi.controller.exception.NotFoundException;
import org.hisp.dhis.webapi.utils.ContextUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.beans.PropertyEditorSupport;
import java.util.Date;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@ControllerAdvice
public class CrudControllerAdvice
{
    @InitBinder
    protected void initBinder( WebDataBinder binder )
    {
        binder.registerCustomEditor( Date.class, new PropertyEditorSupport()
        {
            @Override
            public void setAsText( String value ) throws IllegalArgumentException
            {
                setValue( DateUtils.parseDate( value ) );
            }
        } );
    }

    @ExceptionHandler
    public ResponseEntity<String> notAuthenticatedExceptionHandler( NotAuthenticatedException ex )
    {
        return new ResponseEntity<>( ex.getMessage(), getHeaders(), HttpStatus.UNAUTHORIZED );
    }

    @ExceptionHandler( { NotFoundException.class } )
    public ResponseEntity<String> notFoundExceptionHandler( Exception ex )
    {
        return new ResponseEntity<>( ex.getMessage(), getHeaders(), HttpStatus.NOT_FOUND );
    }

    @ExceptionHandler( { HttpClientErrorException.class, HttpServerErrorException.class } )
    public ResponseEntity<String> httpClient( HttpStatusCodeException ex )
    {
        return new ResponseEntity<>( ex.getStatusText(), getHeaders(), ex.getStatusCode() );
    }

    @ExceptionHandler( ConstraintViolationException.class )
    public ResponseEntity<String> constraintViolationExceptionHandler( ConstraintViolationException ex )
    {
        return new ResponseEntity<>( ex.getMessage(), getHeaders(), HttpStatus.UNPROCESSABLE_ENTITY );
    }

    @ExceptionHandler( DeleteNotAllowedException.class )
    public ResponseEntity<String> deleteNotAllowedExceptionHandler( DeleteNotAllowedException ex )
    {
        return new ResponseEntity<>( ex.getMessage(), getHeaders(), HttpStatus.CONFLICT );
    }

    @ExceptionHandler( IllegalQueryException.class )
    public ResponseEntity<String> illegalQueryExceptionHandler( IllegalQueryException ex )
    {
        return new ResponseEntity<>( ex.getMessage(), getHeaders(), HttpStatus.CONFLICT );
    }

    @ExceptionHandler( IllegalArgumentException.class )
    public ResponseEntity<String> illegalArgumentExceptionHandler( IllegalArgumentException ex )
    {
        return new ResponseEntity<>( ex.getMessage(), getHeaders(), HttpStatus.CONFLICT );
    }

    @ExceptionHandler( MaintenanceModeException.class )
    public ResponseEntity<String> maintenanceModeExceptionHandler( MaintenanceModeException ex )
    {
        return new ResponseEntity<>( ex.getMessage(), getHeaders(), HttpStatus.SERVICE_UNAVAILABLE );
    }

    @ExceptionHandler( DataApprovalException.class )
    public void dataApprovalExceptionHandler( DataApprovalException ex, HttpServletResponse response )
    {
        ContextUtils.conflictResponse( response, ex.getClass().getName() ); //TODO fix message
    }

    private HttpHeaders getHeaders()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.TEXT_PLAIN );
        headers.setCacheControl( "max-age=0, no-cache, no-store" );
        return headers;
    }
}

