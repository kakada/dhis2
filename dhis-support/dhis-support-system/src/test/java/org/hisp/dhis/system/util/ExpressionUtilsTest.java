package org.hisp.dhis.system.util;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author Lars Helge Overland
 */
public class ExpressionUtilsTest
{
    @Test
    public void testIsTrue()
    {
        assertTrue( ExpressionUtils.isTrue( "2 > 1", null ) );
        assertTrue( ExpressionUtils.isTrue( "(2 * 3) == 6", null ) );
        assertTrue( ExpressionUtils.isTrue( "\"a\" == \"a\"", null ) );
        assertTrue( ExpressionUtils.isTrue( "'b' == 'b'", null ) );
        assertTrue( ExpressionUtils.isTrue( "('b' == 'b') && ('c' == 'c')", null ) );
        assertTrue( ExpressionUtils.isTrue( "'goat' == 'goat'", null ) );
        
        assertFalse( ExpressionUtils.isTrue( "2 < 1", null ) );
        assertFalse( ExpressionUtils.isTrue( "(2 * 3) == 8", null ) );
        assertFalse( ExpressionUtils.isTrue( "\"a\" == \"b\"", null ) );
        assertFalse( ExpressionUtils.isTrue( "'b' == 'c'", null ) );
        assertFalse( ExpressionUtils.isTrue( "'goat' == 'cow'", null ) );
    }
    
    @Test
    public void testIsTrueWithVars()
    {
        Map<String, Object> vars = new HashMap<String, Object>();
        
        vars.put( "v1", "4" );
        vars.put( "v2", "12" );
        vars.put( "v3", "goat" );
        vars.put( "v4", "horse" );
        
        assertTrue( ExpressionUtils.isTrue( "v1 > 1", vars ) );
        assertTrue( ExpressionUtils.isTrue( "v2 < 18", vars ) );
        assertTrue( ExpressionUtils.isTrue( "v2 < '23'", vars ) );
        assertTrue( ExpressionUtils.isTrue( "v3 == 'goat'", vars ) );
        assertTrue( ExpressionUtils.isTrue( "v4 == 'horse'", vars ) );
        assertTrue( ExpressionUtils.isTrue( "v4 == \"horse\"", vars ) );

        assertFalse( ExpressionUtils.isTrue( "v1 < 1", vars ) );
        assertFalse( ExpressionUtils.isTrue( "v2 > 18", vars ) );
        assertFalse( ExpressionUtils.isTrue( "v2 > '23'", vars ) );
        assertFalse( ExpressionUtils.isTrue( "v3 == 'cow'", vars ) );
        assertFalse( ExpressionUtils.isTrue( "v4 == 'goat'", vars ) );
        assertFalse( ExpressionUtils.isTrue( "v4 == \"goat\"", vars ) );
    }
}
