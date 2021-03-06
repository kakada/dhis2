package org.hisp.dhis.sqlview;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.common.IllegalQueryException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

/**
 * @author Dang Duy Hieu
 */
public class SqlViewServiceTest
    extends DhisSpringTest
{
    @Autowired
    private SqlViewService sqlViewService;

    protected static final String SQL1 = "SELECT   *  FROM     _categorystructure;;  ; ;;;  ;; ; ";

    protected static final String SQL2 = "SELECT COUNT(_ous.*) AS so_dem FROM _orgunitstructure AS _ous";

    protected static final String SQL3 = "SELECT COUNT(_cocn.*) AS so_dem, _icgss.indicatorid AS in_id"
        + "FROM _indicatorgroupsetstructure AS _icgss, _categoryoptioncomboname AS _cocn "
        + "GROUP BY _icgss.indicatorid;";

    protected static final String SQL4 = "SELECT de.name, dv.sourceid, dv.value, p.startdate "
        + "FROM dataelement AS de, datavalue AS dv, period AS p " + "WHERE de.dataelementid=dv.dataelementid "
        + "AND dv.periodid=p.periodid LIMIT 10";
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void assertEq( char uniqueCharacter, SqlView sqlView, String sql )
    {
        assertEquals( "SqlView" + uniqueCharacter, sqlView.getName() );
        assertEquals( "Description" + uniqueCharacter, sqlView.getDescription() );
        assertEquals( sql, sqlView.getSqlQuery() );
    }

    // -------------------------------------------------------------------------
    // SqlView
    // -------------------------------------------------------------------------

    @Test
    public void testAddSqlView()
    {
        SqlView sqlViewA = createSqlView( 'A', SQL1 );
        SqlView sqlViewB = createSqlView( 'B', SQL2 );

        int idA = sqlViewService.saveSqlView( sqlViewA );
        int idB = sqlViewService.saveSqlView( sqlViewB );

        sqlViewA = sqlViewService.getSqlView( idA );
        sqlViewB = sqlViewService.getSqlView( idB );

        assertEquals( idA, sqlViewA.getId() );
        assertEq( 'A', sqlViewA, SQL1 );

        assertEquals( idB, sqlViewB.getId() );
        assertEq( 'B', sqlViewB, SQL2 );
    }

    @Test
    public void testUpdateSqlView()
    {
        SqlView sqlView = createSqlView( 'A', SQL1 );

        int id = sqlViewService.saveSqlView( sqlView );

        sqlView = sqlViewService.getSqlView( id );

        assertEq( 'A', sqlView, SQL1 );

        sqlView.setName( "SqlViewC" );

        sqlViewService.updateSqlView( sqlView );
    }

    @Test
    public void testGetAndDeleteSqlView()
    {
        SqlView sqlViewA = createSqlView( 'A', SQL3 );
        SqlView sqlViewB = createSqlView( 'B', SQL4 );

        int idA = sqlViewService.saveSqlView( sqlViewA );
        int idB = sqlViewService.saveSqlView( sqlViewB );

        assertNotNull( sqlViewService.getSqlView( idA ) );
        assertNotNull( sqlViewService.getSqlView( idB ) );

        sqlViewService.deleteSqlView( sqlViewService.getSqlView( idA ) );

        assertNull( sqlViewService.getSqlView( idA ) );
        assertNotNull( sqlViewService.getSqlView( idB ) );

        sqlViewService.deleteSqlView( sqlViewService.getSqlView( idB ) );

        assertNull( sqlViewService.getSqlView( idA ) );
        assertNull( sqlViewService.getSqlView( idB ) );
    }

    @Test
    public void testGetSqlViewByName()
        throws Exception
    {
        SqlView sqlViewA = createSqlView( 'A', SQL1 );
        SqlView sqlViewB = createSqlView( 'B', SQL2 );

        int idA = sqlViewService.saveSqlView( sqlViewA );
        int idB = sqlViewService.saveSqlView( sqlViewB );

        assertEquals( sqlViewService.getSqlView( "SqlViewA" ).getId(), idA );
        assertEquals( sqlViewService.getSqlView( "SqlViewB" ).getId(), idB );
        assertNull( sqlViewService.getSqlView( "SqlViewC" ) );
    }

    @Test
    public void testCleanSqlQuery()
    {
        SqlView sqlViewA = createSqlView( 'A', SQL1 );

        sqlViewA.cleanSqlQuery();
        
        int idA = sqlViewService.saveSqlView( sqlViewA );

        assertEquals( sqlViewService.getSqlView( "SqlViewA" ).getId(), idA );

        SqlView sqlViewB = sqlViewService.getSqlView( idA );

        assertEq( 'A', sqlViewB, "SELECT * FROM _categorystructure;" );
    }

    @Test
    public void testSetUpViewTableName()
    {
        SqlView sqlViewC = createSqlView( 'C', SQL3 );
        SqlView sqlViewD = createSqlView( 'D', SQL4 );

        assertEquals( "_view_sqlviewc", sqlViewC.getViewName() );
        assertNotSame( "_view_sqlviewc", sqlViewD.getViewName() );
    }
    
    @Test
    public void testSubsituteSql()
    {
        Map<String, String> variables = new HashMap<>();
        variables.put( "level", "4" );
        variables.put( "id", "abc" );
        
        String sql = "select * from datavalue where level=${level} and id='${id}'";
        
        String expected = "select * from datavalue where level=4 and id='abc'";
        
        String actual = sqlViewService.substituteSql( sql, variables );
        
        assertEquals( expected, actual );
    }

    @Test
    public void testSubsituteSqlMalicious()
    {
        Map<String, String> variables = new HashMap<>();
        variables.put( "level", "; delete from datavalue;" );
        
        String sql = "select * from datavalue where level=${level}";
        
        String expected = "select * from datavalue where level=${level}";
        
        String actual = sqlViewService.substituteSql( sql, variables );
        
        assertEquals( expected, actual );
    }
    
    @Test
    public void testGetVariables()
    {
        String sql = "select * from dataelement where valuetype = '${valueType} and aggregationtype = '${aggregationType}'";
        
        Set<String> expected = Sets.newHashSet( "valueType", "aggregationType" );
        
        Set<String> actual = sqlViewService.getVariables( sql );
        
        assertEquals( expected, actual );
    }
    
    @Test( expected = IllegalQueryException.class )
    public void testValidateIllegalKeywords()
    {
        SqlView sqlView = new SqlView( "Name", "delete * from dataelement", SqlViewType.QUERY );
        
        sqlViewService.validateSqlView( sqlView, null, null );
    }

    @Test( expected = IllegalQueryException.class )
    public void testValidateProtectedTables()
    {
        SqlView sqlView = new SqlView( "Name", "select * from userinfo where userinfoid=1", SqlViewType.QUERY );
        
        sqlViewService.validateSqlView( sqlView, null, null );
    }

    @Test( expected = IllegalQueryException.class )
    public void testValidateMissingVariables()
    {
        SqlView sqlView = new SqlView( "Name", "select * from dataelement where valueType = '${valueType}' and aggregationtype = '${aggregationType}'", SqlViewType.QUERY );
        
        Map<String, String> variables = new HashMap<>();
        variables.put( "valueType", "int" );
        
        sqlViewService.validateSqlView( sqlView, null, variables );
    }

    @Test( expected = IllegalQueryException.class )
    public void testValidateIllegalSemiColon()
    {
        SqlView sqlView = new SqlView( "Name", "select * from dataelement; delete from dataelement", SqlViewType.QUERY );
        
        sqlViewService.validateSqlView( sqlView, null, null );
    }

    @Test( expected = IllegalQueryException.class )
    public void testValidateNotSelectQuery()
    {
        SqlView sqlView = new SqlView( "Name", "* from dataelement", SqlViewType.QUERY );
        
        sqlViewService.validateSqlView( sqlView, null, null );
    }
    
    @Test
    public void testValidateSuccessA()
    {
        SqlView sqlView = new SqlView( "Name", "select * from dataelement where valueType = '${valueType}'", SqlViewType.QUERY );
        
        Map<String, String> variables = new HashMap<>();
        variables.put( "valueType", "int" );
        
        sqlViewService.validateSqlView( sqlView, null, variables );
    }
    
    @Test
    public void testValidateSuccessB()
    {
        SqlView sqlView = new SqlView( "Name", "select ug.name from usergroup ug where ug.name ~* '^OU\\s(\\w.*)\\sAgency\\s(\\w.*)\\susers$'", SqlViewType.QUERY );
        
        sqlViewService.validateSqlView( sqlView, null, null );
    }
    
    @Test
    public void testValidateSuccessC()
    {
        SqlView sqlView = new SqlView( "Name", "SELECT a.dataelementid as dsd_id,a.name as dsd_name,b.dataelementid as ta_id,b.ta_name FROM dataelement a", SqlViewType.QUERY );
        
        sqlViewService.validateSqlView( sqlView, null, null );
    }
}
