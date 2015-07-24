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

import static org.hisp.dhis.program.ProgramIndicator.KEY_ATTRIBUTE;
import static org.hisp.dhis.program.ProgramIndicator.KEY_DATAELEMENT;
import static org.hisp.dhis.program.ProgramIndicator.KEY_CONSTANT;
import static org.hisp.dhis.program.ProgramIndicator.KEY_PROGRAM_VARIABLE;
import static org.hisp.dhis.program.ProgramIndicator.VALUE_TYPE_DATE;
import static org.hisp.dhis.program.ProgramIndicator.VALUE_TYPE_INT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementDomain;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValue;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chau Thu Tran
 */
public class ProgramIndicatorServiceTest
    extends DhisSpringTest
{
    @Autowired
    private ProgramIndicatorService programIndicatorService;

    @Autowired
    private TrackedEntityAttributeService attributeService;
    
    @Autowired
    private TrackedEntityInstanceService entityInstanceService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramStageService programStageService;

    @Autowired
    private ProgramInstanceService programInstanceService;

    @Autowired
    private TrackedEntityDataValueService dataValueService;
    
    @Autowired
    private DataElementService dataElementService;

    @Autowired
    private ProgramStageDataElementService programStageDataElementService;
  
    @Autowired
    private TrackedEntityAttributeValueService attributeValueService;

    @Autowired
    private ProgramStageInstanceService programStageInstanceService;
    

    @Autowired
    private ConstantService constantService;

    private Date incidenDate;

    private Date enrollmentDate;
    
    private ProgramStage psA;
    private ProgramStage psB;

    private Program programA;
    private Program programB;

    private ProgramInstance programInstance;
    
    private DataElement deA;    
    private DataElement deB;
    
    private TrackedEntityAttribute atA;
    private TrackedEntityAttribute atB;
    private TrackedEntityAttribute atC;
    private TrackedEntityAttribute atD;

    private ProgramIndicator indicatorA;
    private ProgramIndicator indicatorB;
    private ProgramIndicator indicatorC;
    private ProgramIndicator indicatorD;
    private ProgramIndicator indicatorE;
    private ProgramIndicator indicatorF;
    private ProgramIndicator indicatorG;
    private ProgramIndicator indicatorH;
    private ProgramIndicator indicatorI;
    private ProgramIndicator indicatorJ;
    
    @Override
    public void setUpTest()
    {
        OrganisationUnit organisationUnit = createOrganisationUnit( 'A' );
        organisationUnitService.addOrganisationUnit( organisationUnit );

        // ---------------------------------------------------------------------
        // Program 
        // ---------------------------------------------------------------------
        
        programA = createProgram( 'A', new HashSet<ProgramStage>(), organisationUnit );
        programService.addProgram( programA );

        psA = new ProgramStage( "StageA", programA );
        psA.setSortOrder( 1 );
        programStageService.saveProgramStage( psA );

        psB = new ProgramStage( "StageB", programA );
        psB.setSortOrder( 2 );
        programStageService.saveProgramStage( psB );

        Set<ProgramStage> programStages = new HashSet<>();
        programStages.add( psA );
        programStages.add( psB );
        programA.setProgramStages( programStages );
        programService.updateProgram( programA );

        programB = createProgram( 'B', new HashSet<ProgramStage>(), organisationUnit );
        programService.addProgram( programB );

        // ---------------------------------------------------------------------
        // Program Stage DE 
        // ---------------------------------------------------------------------
       
        deA = createDataElement( 'A' );
        deA.setDomainType( DataElementDomain.TRACKER );
	deA.setType( DataElement.VALUE_TYPE_NUMBER );
		
        deB = createDataElement( 'B' );
        deB.setDomainType( DataElementDomain.TRACKER );  
	deB.setType( DataElement.VALUE_TYPE_DATE );
        
        dataElementService.addDataElement( deA );
        dataElementService.addDataElement( deB );

        ProgramStageDataElement stageDataElementA = new ProgramStageDataElement( psA, deA, false, 1 );
        ProgramStageDataElement stageDataElementB = new ProgramStageDataElement( psA, deB, false, 2 );
        ProgramStageDataElement stageDataElementC = new ProgramStageDataElement( psB, deA, false, 1 );
        ProgramStageDataElement stageDataElementD = new ProgramStageDataElement( psB, deB, false, 2 );

        programStageDataElementService.addProgramStageDataElement( stageDataElementA );
        programStageDataElementService.addProgramStageDataElement( stageDataElementB );
        programStageDataElementService.addProgramStageDataElement( stageDataElementC );
        programStageDataElementService.addProgramStageDataElement( stageDataElementD );

        // ---------------------------------------------------------------------
        // TrackedEntityInstance & Enrollment
        // ---------------------------------------------------------------------       
        
        TrackedEntityInstance entityInstance = createTrackedEntityInstance( 'A', organisationUnit );
        entityInstanceService.addTrackedEntityInstance( entityInstance );

        incidenDate = DateUtils.getMediumDate( "2014-10-22" );
        enrollmentDate = DateUtils.getMediumDate( "2014-12-31" );

        programInstance = programInstanceService.enrollTrackedEntityInstance( entityInstance, programA, enrollmentDate, incidenDate,
            organisationUnit );

        incidenDate = DateUtils.getMediumDate( "2014-10-22" );
        enrollmentDate = DateUtils.getMediumDate( "2014-12-31" );

        programInstance = programInstanceService.enrollTrackedEntityInstance( entityInstance, programA, enrollmentDate, incidenDate,
            organisationUnit );
        
        //TODO enroll twice?
        
        // ---------------------------------------------------------------------
        // TrackedEntityAttribute
        // ---------------------------------------------------------------------       
        
        atA = createTrackedEntityAttribute( 'A', TrackedEntityAttribute.TYPE_NUMBER );
        atB = createTrackedEntityAttribute( 'B', TrackedEntityAttribute.TYPE_NUMBER );
        atC = createTrackedEntityAttribute( 'C', TrackedEntityAttribute.TYPE_DATE );
        atD = createTrackedEntityAttribute( 'D', TrackedEntityAttribute.TYPE_DATE );
        
        attributeService.addTrackedEntityAttribute( atA );
        attributeService.addTrackedEntityAttribute( atB );
        attributeService.addTrackedEntityAttribute( atC );
        attributeService.addTrackedEntityAttribute( atD );
        
      
        TrackedEntityAttributeValue attributeValueA = new TrackedEntityAttributeValue( atA, entityInstance, "1" );
        TrackedEntityAttributeValue attributeValueB = new TrackedEntityAttributeValue( atB, entityInstance, "2" );
        TrackedEntityAttributeValue attributeValueC = new TrackedEntityAttributeValue( atC, entityInstance, "2015-01-01" );
        TrackedEntityAttributeValue attributeValueD = new TrackedEntityAttributeValue( atD, entityInstance, "2015-01-03" );

        attributeValueService.addTrackedEntityAttributeValue( attributeValueA );
        attributeValueService.addTrackedEntityAttributeValue( attributeValueB );
        attributeValueService.addTrackedEntityAttributeValue( attributeValueC );
        attributeValueService.addTrackedEntityAttributeValue( attributeValueD );
        
        // ---------------------------------------------------------------------
        // TrackedEntityDataValue
        // ---------------------------------------------------------------------       
       
        ProgramStageInstance stageInstanceA = programStageInstanceService.getProgramStageInstance( programInstance, psA );
        ProgramStageInstance stageInstanceB = programStageInstanceService.getProgramStageInstance( programInstance, psB );

        Set<ProgramStageInstance> programStageInstances = new HashSet<>();
        programStageInstances.add( stageInstanceA );
        programStageInstances.add( stageInstanceB );
        programInstance.setProgramStageInstances( programStageInstances );        
        
        TrackedEntityDataValue dataValueA = new TrackedEntityDataValue( stageInstanceA, deA, "3" );
        TrackedEntityDataValue dataValueB = new TrackedEntityDataValue( stageInstanceA, deB, "2015-03-01" );
        TrackedEntityDataValue dataValueC = new TrackedEntityDataValue( stageInstanceB, deA, "5" );
        TrackedEntityDataValue dataValueD = new TrackedEntityDataValue( stageInstanceB, deB, "2015-03-15" );        
        
        dataValueService.saveTrackedEntityDataValue( dataValueA );
        dataValueService.saveTrackedEntityDataValue( dataValueB );
        dataValueService.saveTrackedEntityDataValue( dataValueC );
        dataValueService.saveTrackedEntityDataValue( dataValueD );

        // ---------------------------------------------------------------------
        // Constant
        // ---------------------------------------------------------------------       
                
        Constant constantA = createConstant( 'A', 7.0 );
        constantService.saveConstant( constantA );
        
        // ---------------------------------------------------------------------
        // ProgramIndicator
        // ---------------------------------------------------------------------       
       
        indicatorA = new ProgramIndicator( "IndicatorA", "IndicatorDesA", programA, VALUE_TYPE_INT, "( " + KEY_PROGRAM_VARIABLE + "{"
            + ProgramIndicator.ENROLLMENT_DATE + "} - " + KEY_PROGRAM_VARIABLE + "{" + ProgramIndicator.INCIDENT_DATE + "} )  / " 
            + ProgramIndicator.KEY_CONSTANT + "{" + constantA.getUid() + "}" );
        indicatorA.setUid( "UID-DATE" );
        indicatorA.setShortName( "DATE" );

        indicatorB = new ProgramIndicator( "IndicatorB", "IndicatorDesB", programA, ProgramIndicator.VALUE_TYPE_DATE, "70" );
        indicatorB.setRootDate( ProgramIndicator.INCIDENT_DATE );
        indicatorB.setUid( "UID-INT" );
        indicatorB.setShortName( "INT" );

        indicatorC = new ProgramIndicator( "IndicatorC", "IndicatorDesB", programB, ProgramIndicator.VALUE_TYPE_INT, "0" );
        indicatorC.setUid( "UID-C" );
        indicatorC.setShortName( "C" );
        
        indicatorD = new ProgramIndicator( "IndicatorD", "IndicatorDesD", programB, ProgramIndicator.VALUE_TYPE_INT, "0 + A + 4 + " + ProgramIndicator.KEY_PROGRAM_VARIABLE + "{"
            + ProgramIndicator.INCIDENT_DATE + "}" );
        indicatorD.setUid( "UID-D" );
        indicatorD.setShortName( "D" );
        
        String expressionE = KEY_DATAELEMENT + "{" + psA.getUid() + "." + deA.getUid() + "} + " + 
            KEY_DATAELEMENT + "{" + psB.getUid() + "." + deA.getUid() + "} - " + 
            KEY_ATTRIBUTE + "{" + atA.getUid() + "} + " + KEY_ATTRIBUTE + "{" + atB.getUid() + "}";
        
        indicatorE = new ProgramIndicator( "IndicatorE", "IndicatorDesE", programB, VALUE_TYPE_INT, expressionE );
        
        String expressionF = "(" + KEY_DATAELEMENT + "{" + psB.getUid() + "." + deB.getUid() + "} - " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deB.getUid() + "} ) + " + 
            KEY_ATTRIBUTE + "{" + atA.getUid() + "} + " + KEY_ATTRIBUTE + "{" + atB.getUid() + "}";
        
        indicatorF = new ProgramIndicator( "IndicatorF", "IndicatorDesF", programB, VALUE_TYPE_INT, expressionF );
        
        String expressionG = "(" + KEY_DATAELEMENT + "{" + psB.getUid() + "." + deB.getUid() + "} - " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deB.getUid() + "} ) + " +
            KEY_ATTRIBUTE + "{" + atA.getUid() + "} + " + 
            KEY_ATTRIBUTE + "{" + atB.getUid() + "} * " +
            KEY_CONSTANT + "{" + constantA.getUid() + "}";
        
        indicatorG = new ProgramIndicator( "IndicatorG", "IndicatorDesG", programB, VALUE_TYPE_INT, expressionG );
        
        String expressionH = "(" + KEY_PROGRAM_VARIABLE + "{" + ProgramIndicator.CURRENT_DATE + "} - " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deB.getUid() + "} ) + " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deA.getUid() + "}";
        
        indicatorH = new ProgramIndicator( "IndicatorH", "IndicatorDesH", programB, VALUE_TYPE_INT, expressionH );        
        
        String expressionI = "(" + KEY_PROGRAM_VARIABLE + "{" + ProgramIndicator.CURRENT_DATE + "} - " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deB.getUid() + "} ) + " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deA.getUid() + "}";
        
        indicatorI = new ProgramIndicator( "IndicatorI", "IndicatorDesI", programB, VALUE_TYPE_DATE, expressionI ); 
        indicatorI.setRootDate( ProgramIndicator.INCIDENT_DATE );        

        String expressionJ = "(" + KEY_ATTRIBUTE + "{" + atC.getUid() + "}  - " + 
            KEY_PROGRAM_VARIABLE + "{" + ProgramIndicator.ENROLLMENT_DATE + "} ) + " +           
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deA.getUid() + "} * " + 
            ProgramIndicator.KEY_CONSTANT + "{" + constantA.getUid() + "}";
        
        indicatorJ = new ProgramIndicator( "IndicatorJ", "IndicatorDesJ", programB, VALUE_TYPE_DATE, expressionJ ); 
        indicatorJ.setRootDate( ProgramIndicator.INCIDENT_DATE );
    }

    // -------------------------------------------------------------------------
    // CRUD tests
    // -------------------------------------------------------------------------

    @Test
    public void testAddProgramIndicator()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorA );
        int idB = programIndicatorService.addProgramIndicator( indicatorB );

        assertNotNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNotNull( programIndicatorService.getProgramIndicator( idB ) );
    }

    @Test
    public void testDeleteProgramIndicator()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorB );
        int idB = programIndicatorService.addProgramIndicator( indicatorA );

        assertNotNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNotNull( programIndicatorService.getProgramIndicator( idB ) );

        programIndicatorService.deleteProgramIndicator( indicatorB );

        assertNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNotNull( programIndicatorService.getProgramIndicator( idB ) );

        programIndicatorService.deleteProgramIndicator( indicatorA );

        assertNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNull( programIndicatorService.getProgramIndicator( idB ) );
    }

    @Test
    public void testUpdateProgramIndicator()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorB );

        assertNotNull( programIndicatorService.getProgramIndicator( idA ) );

        indicatorB.setName( "B" );
        programIndicatorService.updateProgramIndicator( indicatorB );

        assertEquals( "B", programIndicatorService.getProgramIndicator( idA ).getName() );
    }

    @Test
    public void testGetProgramIndicatorById()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorB );
        int idB = programIndicatorService.addProgramIndicator( indicatorA );

        assertEquals( indicatorB, programIndicatorService.getProgramIndicator( idA ) );
        assertEquals( indicatorA, programIndicatorService.getProgramIndicator( idB ) );
    }

    @Test
    public void testGetProgramIndicatorByName()
    {
        programIndicatorService.addProgramIndicator( indicatorB );
        programIndicatorService.addProgramIndicator( indicatorA );

        assertEquals( "IndicatorA", programIndicatorService.getProgramIndicator( "IndicatorA" ).getName() );
        assertEquals( "IndicatorB", programIndicatorService.getProgramIndicator( "IndicatorB" ).getName() );
    }

    @Test
    public void testGetAllProgramIndicators()
    {
        programIndicatorService.addProgramIndicator( indicatorB );
        programIndicatorService.addProgramIndicator( indicatorA );

        assertTrue( equals( programIndicatorService.getAllProgramIndicators(), indicatorB, indicatorA ) );
    }

    @Test
    public void testGetProgramIndicatorByShortName()
    {
        programIndicatorService.addProgramIndicator( indicatorB );
        programIndicatorService.addProgramIndicator( indicatorA );

        assertEquals( "INT", programIndicatorService.getProgramIndicatorByShortName( "INT" ).getShortName() );
        assertEquals( "DATE", programIndicatorService.getProgramIndicatorByShortName( "DATE" ).getShortName() );
    }

    @Test
    public void testGetProgramIndicatorByUid()
    {
        programIndicatorService.addProgramIndicator( indicatorB );
        programIndicatorService.addProgramIndicator( indicatorA );

        assertEquals( "UID-INT", programIndicatorService.getProgramIndicatorByUid( "UID-INT" ).getUid() );
        assertEquals( "UID-DATE", programIndicatorService.getProgramIndicatorByUid( "UID-DATE" ).getUid() );
    }

    // -------------------------------------------------------------------------
    // Logic tests
    // -------------------------------------------------------------------------

    @Test
    public void testGetProgramStageDataElementsInExpression()
    {
        Set<ProgramStageDataElement> elements = programIndicatorService.getProgramStageDataElementsInExpression( indicatorE );
        
        assertEquals( 2, elements.size() );
        
        assertTrue( elements.contains( new ProgramStageDataElement( psA, deA ) ) );
        assertTrue( elements.contains( new ProgramStageDataElement( psB, deA ) ) );
    }
    
    @Test
    public void testGetAttributesInExpression()
    {
        Set<TrackedEntityAttribute> attributes = programIndicatorService.getAttributesInExpression( indicatorE );
        
        assertEquals( 2, attributes.size() );
        assertTrue( attributes.contains( atA ) );
        assertTrue( attributes.contains( atB ) );
    }
    
    @Test
    public void testGetProgramIndicatorValue()
    {
        programIndicatorService.addProgramIndicator( indicatorB );
        programIndicatorService.addProgramIndicator( indicatorA );
        programIndicatorService.addProgramIndicator( indicatorE );
        programIndicatorService.addProgramIndicator( indicatorF );
        programIndicatorService.addProgramIndicator( indicatorG );
        programIndicatorService.addProgramIndicator( indicatorH );
        programIndicatorService.addProgramIndicator( indicatorI );
        programIndicatorService.addProgramIndicator( indicatorJ );

        String valueINT = programIndicatorService.getProgramIndicatorValue( programInstance, indicatorA );
        assertEquals( "10.0", valueINT );

        String valueDATE = programIndicatorService.getProgramIndicatorValue( programInstance,  indicatorB  );
        assertEquals( DateUtils.getMediumDateString( enrollmentDate ), valueDATE );
        
        String valueE = programIndicatorService.getProgramIndicatorValue( programInstance, indicatorE );
        assertEquals( "9.0", valueE  );
        
        String valueF = programIndicatorService.getProgramIndicatorValue( programInstance, indicatorF );
        assertEquals( "17.0", valueF );

        String valueG = programIndicatorService.getProgramIndicatorValue( programInstance, indicatorG );
        assertEquals( "29.0", valueG );
    }

    @Test
    public void testGetProgramIndicatorValues()
    {
        programIndicatorService.addProgramIndicator( indicatorA );
        programIndicatorService.addProgramIndicator( indicatorB );

        Map<String, String> indicatorMap = programIndicatorService.getProgramIndicatorValues( programInstance );
        assertEquals( 2, indicatorMap.keySet().size() );
        assertEquals( "10.0", indicatorMap.get( "IndicatorA" ) );
        assertEquals( DateUtils.getMediumDateString( enrollmentDate ), indicatorMap.get( "IndicatorB" ) );
    }
    
    @Test
    public void testGetExpressionDescription()
    {
        programIndicatorService.addProgramIndicator( indicatorB );
        programIndicatorService.addProgramIndicator( indicatorA );

        String description = programIndicatorService.getExpressionDescription( indicatorB.getExpression() );
        assertEquals( "70", description);
        
        description = programIndicatorService.getExpressionDescription( indicatorA.getExpression() );
        assertEquals( "( Enrollment date - Incident date )  / ConstantA", description);
        
    }
    
    @Test
    public void testExpressionIsValid()
    {
        programIndicatorService.addProgramIndicator( indicatorB );
        programIndicatorService.addProgramIndicator( indicatorA );
        programIndicatorService.addProgramIndicator( indicatorD );

        assertEquals( ProgramIndicator.VALID, programIndicatorService.expressionIsValid( indicatorB.getExpression() ) );
        assertEquals( ProgramIndicator.VALID, programIndicatorService.expressionIsValid( indicatorA.getExpression() ) );
        assertEquals( ProgramIndicator.EXPRESSION_NOT_WELL_FORMED, programIndicatorService.expressionIsValid( indicatorD.getExpression() ) );
    }    
}
