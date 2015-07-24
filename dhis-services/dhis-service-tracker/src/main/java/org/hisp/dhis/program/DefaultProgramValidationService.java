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

import static org.hisp.dhis.program.ProgramExpression.OBJECT_PROGRAM_STAGE;
import static org.hisp.dhis.program.ProgramExpression.OBJECT_PROGRAM_STAGE_DATAELEMENT;
import static org.hisp.dhis.program.ProgramExpression.SEPARATOR_ID;
import static org.hisp.dhis.program.ProgramExpression.SEPARATOR_OBJECT;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValue;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Chau Thu Tran
 * @version $ DefaultProgramValidationService.java Apr 28, 2011 10:36:50 AM $
 */
@Transactional
public class DefaultProgramValidationService
    implements ProgramValidationService
{
    private final String regExp = "\\[(" + OBJECT_PROGRAM_STAGE_DATAELEMENT + "|" + OBJECT_PROGRAM_STAGE + ")"
        + SEPARATOR_OBJECT + "([a-zA-Z0-9\\- ]+[" + SEPARATOR_ID + "[a-zA-Z0-9\\- ]*]*)" + "\\]";

    private ProgramValidationStore validationStore;

    public void setValidationStore( ProgramValidationStore validationStore )
    {
        this.validationStore = validationStore;
    }

    private ProgramExpressionService expressionService;

    public void setExpressionService( ProgramExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }

    private TrackedEntityDataValueService dataValueService;

    public void setDataValueService( TrackedEntityDataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    // -------------------------------------------------------------------------
    // Implementation methods
    // -------------------------------------------------------------------------
    @Override
    public int addProgramValidation( ProgramValidation programValidation )
    {
        return validationStore.save( programValidation );
    }

    @Override
    public void updateProgramValidation( ProgramValidation programValidation )
    {
        validationStore.update( programValidation );
    }

    @Override
    public void deleteProgramValidation( ProgramValidation programValidation )
    {
        validationStore.delete( programValidation );
    }

    @Override
    public Collection<ProgramValidation> getAllProgramValidation()
    {
        return validationStore.getAll();
    }

    @Override
    public ProgramValidation getProgramValidation( int id )
    {
        return validationStore.get( id );
    }

    @Override
    public Collection<ProgramValidationResult> validate( Collection<ProgramValidation> validation,
        ProgramStageInstance programStageInstance )
    {
        Collection<ProgramValidationResult> result = new HashSet<>();

        // ---------------------------------------------------------------------
        // Get data-values
        // ---------------------------------------------------------------------

        Program program = programStageInstance.getProgramInstance().getProgram();
        Collection<TrackedEntityDataValue> entityInstanceDataValues = null;
        if ( program.isSingleEvent() )
        {
            entityInstanceDataValues = dataValueService.getTrackedEntityDataValues( programStageInstance );
        }
        else
        {
            entityInstanceDataValues = dataValueService.getTrackedEntityDataValues( programStageInstance.getProgramInstance()
                .getProgramStageInstances() );
        }

        Map<String, String> entityInstanceDataValueMap = new HashMap<>( entityInstanceDataValues.size() );
        for ( TrackedEntityDataValue entityInstanceDataValue : entityInstanceDataValues )
        {
            String key = entityInstanceDataValue.getProgramStageInstance().getProgramStage().getUid() + "."
                + entityInstanceDataValue.getDataElement().getUid();
            entityInstanceDataValueMap.put( key, entityInstanceDataValue.getValue() );
        }
        
        // ---------------------------------------------------------------------
        // Validate rules
        // ---------------------------------------------------------------------

        for ( ProgramValidation validate : validation )
        {
            String leftSideValue = expressionService.getProgramExpressionValue( validate.getLeftSide(),
                programStageInstance, entityInstanceDataValueMap );
            String rightSideValue = expressionService.getProgramExpressionValue( validate.getRightSide(),
                programStageInstance, entityInstanceDataValueMap );
            String operator = validate.getOperator().getMathematicalOperator();

            if ( leftSideValue != null && rightSideValue != null )
            {
                String expression = validate.getLeftSide().getExpression() + " " + validate.getRightSide().getExpression();
                if ( isNumberDataExpression( expression ) )
                {
                    double leftSide = Double.parseDouble( leftSideValue );
                    double rightSide = Double.parseDouble( rightSideValue );
                    if ( !((operator.equals( "==" ) && leftSide == rightSide)
                        || (operator.equals( "<" ) && leftSide < rightSide)
                        || (operator.equals( "<=" ) && leftSide <= rightSide)
                        || (operator.equals( ">" ) && leftSide > rightSide)
                        || (operator.equals( ">=" ) && leftSide >= rightSide) || (operator.equals( "!=" ) && leftSide != rightSide)) )
                    {
                        ProgramValidationResult validationResult = new ProgramValidationResult( programStageInstance,
                            validate, leftSideValue, rightSideValue );
                        result.add( validationResult );
                    }
                }
                else if ( !((operator.equals( "==" ) && leftSideValue.compareTo( rightSideValue ) == 0)
                    || (operator.equals( "<" ) && leftSideValue.compareTo( rightSideValue ) < 0)
                    || (operator.equals( "<=" ) && (leftSideValue.compareTo( rightSideValue ) <= 0))
                    || (operator.equals( ">" ) && leftSideValue.compareTo( rightSideValue ) > 0)
                    || (operator.equals( ">=" ) && leftSideValue.compareTo( rightSideValue ) >= 0) || (operator
                    .equals( "!=" ) && leftSideValue.compareTo( rightSideValue ) == 0)) )
                {
                    ProgramValidationResult validationResult = new ProgramValidationResult( programStageInstance,
                        validate, leftSideValue, rightSideValue );
                    result.add( validationResult );
                }
            }
        }

        return result;
    }

    @Override
    public Collection<ProgramValidation> getProgramValidation( Program program )
    {
        return validationStore.get( program );
    }

    @Override
    public Collection<ProgramValidation> getProgramValidation( ProgramStage programStage )
    {
        Collection<ProgramValidation> programValidation = getProgramValidation( programStage.getProgram() );

        Iterator<ProgramValidation> iter = programValidation.iterator();

        Pattern pattern = Pattern.compile( regExp );

        while ( iter.hasNext() )
        {
            ProgramValidation validation = iter.next();

            String expression = validation.getLeftSide().getExpression() + " "
                + validation.getRightSide().getExpression();
            Matcher matcher = pattern.matcher( expression );

            boolean flag = false;
            while ( matcher.find() )
            {
                String match = matcher.group();
                match = match.replaceAll( "[\\[\\]]", "" );

                String[] info = match.split( SEPARATOR_OBJECT );
                String[] ids = info[1].split( SEPARATOR_ID );

                String programStageUid = ids[0];

                if ( programStageUid.equals( programStage.getUid() ) )
                {
                    flag = true;
                    break;
                }
            }

            if ( !flag )
            {
                iter.remove();
            }
        }

        return programValidation;
    }  
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private boolean isNumberDataExpression( String programExpression )
    {
        Collection<DataElement> dataElements = expressionService.getDataElements( programExpression );
        
        for ( DataElement dataElement : dataElements )
        {
            if ( dataElement.getType().equals( DataElement.VALUE_TYPE_INT ) )
            {
                return true;
            }
        }
        
        return false;
    }
}
