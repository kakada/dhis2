package org.hisp.dhis.analytics;

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

import static org.hisp.dhis.common.DimensionalObject.DATAELEMENT_DIM_ID;
import static org.hisp.dhis.common.DimensionalObject.PERIOD_DIM_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.DhisConvenienceTest;
import org.hisp.dhis.common.BaseDimensionalObject;
import org.hisp.dhis.common.DimensionType;
import org.hisp.dhis.common.DimensionalObject;
import org.hisp.dhis.common.DimensionalObjectUtils;
import org.hisp.dhis.common.NameableObject;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.system.util.ListUtils;
import org.junit.Test;

/**
 * @author Lars Helge Overland
 */
public class DataQueryParamsTest
    extends DhisConvenienceTest
{
    @Test
    public void testGetDimensionFromParam()
    {
        assertEquals( DATAELEMENT_DIM_ID, DimensionalObjectUtils.getDimensionFromParam( "de:D348asd782j;kj78HnH6hgT;9ds9dS98s2" ) );
    }
    
    @Test
    public void testGetDimensionItemsFromParam()
    {
        List<String> expected = new ArrayList<>( Arrays.asList( "D348asd782j", "kj78HnH6hgT", "9ds9dS98s2" ) );
        
        assertEquals( expected, DimensionalObjectUtils.getDimensionItemsFromParam( "de:D348asd782j;kj78HnH6hgT;9ds9dS98s2" ) );        
    }
    
    @Test
    public void testGetLevelFromLevelParam()
    {
        assertEquals( 4, DimensionalObjectUtils.getLevelFromLevelParam( "LEVEL-4-dFsdfejdf2" ) );
        assertEquals( 0, DimensionalObjectUtils.getLevelFromLevelParam( "LEVEL" ) );
        assertEquals( 0, DimensionalObjectUtils.getLevelFromLevelParam( "LEVEL-gFd" ) );        
    }
        
    @Test
    public void testGetMeasureCriteriaFromParam()
    {
        Map<MeasureFilter, Double> expected = new HashMap<>();
        expected.put( MeasureFilter.GT, 100d );
        expected.put( MeasureFilter.LT, 200d );
        
        assertEquals( expected, DataQueryParams.getMeasureCriteriaFromParam( "GT:100;LT:200" ) );
    }
    
    @Test
    public void testHasPeriods()
    {
        DataQueryParams params = new DataQueryParams();
        
        assertFalse( params.hasPeriods() );
        
        List<NameableObject> periods = new ArrayList<>();
        
        params.getDimensions().add( new BaseDimensionalObject( PERIOD_DIM_ID, DimensionType.PERIOD, periods ) );
        
        assertFalse( params.hasPeriods() );
        
        params.removeDimension( PERIOD_DIM_ID );

        assertFalse( params.hasPeriods() );
        
        periods.add( new Period() );
        params.getDimensions().add( new BaseDimensionalObject( PERIOD_DIM_ID, DimensionType.PERIOD, periods ) );
        
        assertTrue( params.hasPeriods() );
    }

    @Test
    public void testPruneToDimensionType()
    {
        DataQueryParams params = new DataQueryParams();
        params.getDimensions().add( new BaseDimensionalObject( DimensionalObject.INDICATOR_DIM_ID, DimensionType.INDICATOR, null, null, 
            ListUtils.getList( createIndicator( 'A', null ), createIndicator( 'B', null ) ) ) );
        params.getDimensions().add( new BaseDimensionalObject( DimensionalObject.ORGUNIT_DIM_ID, DimensionType.ORGANISATIONUNIT, null, null,
            ListUtils.getList( createOrganisationUnit( 'A' ), createOrganisationUnit( 'B' ) ) ) );
        params.getFilters().add( new BaseDimensionalObject( DimensionalObject.PERIOD_DIM_ID, DimensionType.PERIOD, null, null,
            ListUtils.getList( createPeriod( "201201" ), createPeriod( "201202" ) ) ) );

        assertEquals( 2, params.getDimensions().size() );
        assertEquals( 1, params.getFilters().size() );
        
        params.pruneToDimensionType( DimensionType.ORGANISATIONUNIT );
        
        assertEquals( 1, params.getDimensions().size() );
        assertEquals( DimensionType.ORGANISATIONUNIT, params.getDimensions().get( 0 ).getDimensionType() );
        assertEquals( 0, params.getFilters().size() );
    }
}
