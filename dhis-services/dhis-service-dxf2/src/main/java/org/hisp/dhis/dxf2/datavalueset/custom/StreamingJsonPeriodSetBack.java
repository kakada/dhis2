package org.hisp.dhis.dxf2.datavalueset.custom;

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

import com.fasterxml.jackson.core.JsonGenerator;

import org.hisp.dhis.dxf2.datavalue.custom.CustomPeriod;
import org.hisp.dhis.dxf2.datavalue.custom.StreamingJsonPeriod;

import java.io.IOException;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class StreamingJsonPeriodSetBack extends PeriodSetBack
{
    private JsonGenerator generator;

    private boolean startedArray;

    public StreamingJsonPeriodSetBack( JsonGenerator generator ) {
    	this.generator = generator;
    	
    	try
        {
            this.generator.writeObjectFieldStart( "periodSet" );
        }
        catch ( IOException ignored )
        {
            ignored.printStackTrace();
        }
    }
    
    @Override
    public void setType( String type )
    {
        writeObjectField( FIELD_TYPE, type );
    }
    
    @Override
    public void setCompleteDate( String completeDate ) {
    	writeObjectField( FIELD_COMPLETEDATE, completeDate );
    };
    
    @Override
    public void setCreated( String created ) {
    	writeObjectField( FIELD_CREATED, created );
    }
    
    @Override
    public void setLastUpdated( String lastUpdated ) {
    	writeObjectField( FIELD_LASTUPDATED, lastUpdated );
    }

    @Override
    public CustomPeriod getPeriodInstance()
    {
        if ( !startedArray )
        {
            try
            {
                generator.writeArrayFieldStart( "periods" );
                startedArray = true;
            }
            catch ( IOException ignored )
            {
            }
        }

        return new StreamingJsonPeriod( generator );
    }

    @Override
    public void close()
    {
        if ( generator == null )
        {
            return;
        }

        try
        {
            if ( startedArray )
            {
                generator.writeEndArray();
            }

            generator.writeEndObject();
        }
        catch ( IOException ignored )
        {
        }
    }

    private void writeObjectField( String fieldName, Object value )
    {
        if ( value == null )
        {
            return;
        }

        try
        {
            generator.writeObjectField( fieldName, value );
        }
        catch ( IOException ignored )
        {
        }
    }
}