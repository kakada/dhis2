package org.hisp.dhis.common;

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

import java.util.Date;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;

/**
 * @author Lars Helge Overland
 */
public enum ValueType
{   
    TEXT( String.class ),
    LONG_TEXT( String.class ),
    LETTER( String.class ),
    TYPE_PHONE_NUMBER( String.class ),
    EMAIL( String.class ),
    BOOLEAN( Boolean.class ),
    TRUE_ONLY( Boolean.class ),
    DATE( Date.class ),
    NUMBER( Double.class ),
    UNIT_INTERVAL( Double.class ),
    PERCENTAGE( Double.class ),
    INTEGER( Integer.class ),
    INTEGER_POSITIVE( Integer.class ),
    INTEGER_NEGATIVE( Integer.class ),
    INTEGER_ZERO_OR_POSITIVE( Integer.class ),
    NEGATIVE_INTEGER( Integer.class );
    
    private final Class<?> javaClass;
    
    private ValueType()
    {
        this.javaClass = null;
    }
    
    private ValueType( Class<?> javaClass )
    {
        this.javaClass = javaClass;
    }
    
    public Class<?> getJavaClass()
    {
        return javaClass;
    }

    /**
     * TODO replace string value type on data element with ValueType and remove
     * this method.
     */
    public static ValueType getFromDataElement( DataElement dataElement )
    {        
        if ( DataElement.VALUE_TYPE_STRING.equals( dataElement.getType() ) )
        {
            if ( DataElement.VALUE_TYPE_LONG_TEXT.equals( dataElement.getTextType() ) )
            {
                return ValueType.LONG_TEXT;
            }
            else
            {
                return ValueType.TEXT;
            }
        }
        else if ( DataElement.VALUE_TYPE_INT.equals( dataElement.getType() ) )
        {
            if ( DataElement.VALUE_TYPE_UNIT_INTERVAL.equals( dataElement.getNumberType() ) )
            {
                return ValueType.UNIT_INTERVAL;
            }
            else if ( DataElement.VALUE_TYPE_PERCENTAGE.equals( dataElement.getNumberType() ) )
            {
                return ValueType.PERCENTAGE;
            }
            else if ( DataElement.VALUE_TYPE_INT.equals( dataElement.getNumberType() ) )
            {
                return ValueType.INTEGER;
            }
            else if ( DataElement.VALUE_TYPE_POSITIVE_INT.equals( dataElement.getNumberType() ) )
            {
                return ValueType.INTEGER_POSITIVE;
            }
            else if ( DataElement.VALUE_TYPE_ZERO_OR_POSITIVE_INT.equals( dataElement.getNumberType() ) )
            {
                return ValueType.INTEGER_ZERO_OR_POSITIVE;
            }
            else if ( DataElement.VALUE_TYPE_NEGATIVE_INT.equals( dataElement.getNumberType() ) )
            {
                return ValueType.INTEGER_NEGATIVE;
            }
            else
            {
                return ValueType.NUMBER;
            }
        }
        else if ( DataElement.VALUE_TYPE_BOOL.equals( dataElement.getType() ) )
        {
            return ValueType.BOOLEAN;
        }
        else if ( DataElement.VALUE_TYPE_TRUE_ONLY.equals( dataElement.getType() ) )
        {
            return ValueType.TRUE_ONLY;
        }
        else if ( DataElement.VALUE_TYPE_DATE.equals( dataElement.getType() ) )
        {
            return ValueType.DATE;
        }

        return ValueType.TEXT; // Fall back
    }

    /**
     * TODO replace string value type on attribute with ValueType and remove
     * this method.
     */
    public static ValueType getFromAttribute( TrackedEntityAttribute attribute )
    {
        if ( TrackedEntityAttribute.TYPE_NUMBER.equals( attribute.getValueType() ) )
        {
            return ValueType.NUMBER;
        }
        else if ( TrackedEntityAttribute.TYPE_BOOL.equals( attribute.getValueType() ) || TrackedEntityAttribute.TYPE_TRUE_ONLY.equals( attribute.getValueType() ) )
        {
            return ValueType.BOOLEAN;
        }
        else if ( TrackedEntityAttribute.TYPE_DATE.equals( attribute.getValueType() ) )
        {
            return ValueType.DATE;
        }
        
        return ValueType.TEXT; // Fall back
    }    
}
