package org.hisp.dhis.webapi.utils;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.common.NameableObjectUtils;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.comparator.SectionOrderComparator;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageDataElement;
import org.hisp.dhis.program.ProgramStageSection;
import org.hisp.dhis.webapi.webdomain.form.Field;
import org.hisp.dhis.webapi.webdomain.form.Form;
import org.hisp.dhis.webapi.webdomain.form.Group;
import org.hisp.dhis.webapi.webdomain.form.InputType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class FormUtils
{
    private static final String KEY_PERIOD_TYPE = "periodType";
    private static final String KEY_OPEN_FUTURE_PERIODS = "openFuturePeriods";
    private static final String KEY_DATA_ELEMENTS = "dataElements";
    private static final String KEY_INDICATORS = "indicators";
    private static final String KEY_EXPIRY_DAYS = "expiryDays";
    private static final String SEP = "-";

    public static Form fromDataSet( DataSet dataSet, boolean metaData )
    {
        Form form = new Form();
        form.setLabel( dataSet.getDisplayName() );
        form.setSubtitle( dataSet.getDisplayShortName() );

        form.getOptions().put( KEY_PERIOD_TYPE, dataSet.getPeriodType().getName() );
        form.getOptions().put( KEY_OPEN_FUTURE_PERIODS, dataSet.getOpenFuturePeriods() );
        form.getOptions().put( KEY_EXPIRY_DAYS, dataSet.getExpiryDays() );

        if ( dataSet.hasSections() )
        {
            List<Section> sections = new ArrayList<>( dataSet.getSections() );
            Collections.sort( sections, SectionOrderComparator.INSTANCE );
            
            for ( Section section : sections )
            {
                List<Field> fields = inputFromDataElements( new ArrayList<>( section.getDataElements() ), new ArrayList<>( section.getGreyedFields() ) );

                Group group = new Group();
                group.setLabel( section.getDisplayName() );
                group.setDescription( section.getDescription() );
                group.setDataElementCount( section.getDataElements().size() );
                group.setFields( fields );
                
                if ( metaData )
                {
                    group.getMetaData().put( KEY_DATA_ELEMENTS, NameableObjectUtils.getAsNameableObjects( section.getDataElements() ) );
                    group.getMetaData().put( KEY_INDICATORS, NameableObjectUtils.getAsNameableObjects( section.getIndicators() ) );
                }
                
                form.getGroups().add( group );
            }
        }
        else
        {
            List<Field> fields = inputFromDataElements( new ArrayList<>( dataSet.getDataElements() ) );

            Group group = new Group();
            group.setLabel( DataElementCategoryCombo.DEFAULT_CATEGORY_COMBO_NAME );
            group.setDescription( DataElementCategoryCombo.DEFAULT_CATEGORY_COMBO_NAME );
            group.setDataElementCount( dataSet.getDataElements().size() );
            group.setFields( fields );

            if ( metaData )
            {
                group.getMetaData().put( KEY_DATA_ELEMENTS, NameableObjectUtils.getAsNameableObjects( new ArrayList<>( dataSet.getDataElements() ) ) );
            }
            
            form.getGroups().add( group );
        }

        return form;
    }

    public static Form fromProgram( Program program )
    {
        Assert.notNull( program );

        Form form = new Form();
        form.setLabel( program.getDisplayName() );

        if ( !StringUtils.isEmpty( program.getDescription() ) )
        {
            form.getOptions().put( "description", program.getDescription() );
        }

        if ( !StringUtils.isEmpty( program.getDateOfEnrollmentDescription() ) )
        {
            form.getOptions().put( "dateOfEnrollmentDescription", program.getDateOfEnrollmentDescription() );
        }

        if ( !StringUtils.isEmpty( program.getDateOfIncidentDescription() ) )
        {
            form.getOptions().put( "dateOfIncidentDescription", program.getDateOfIncidentDescription() );
        }

        form.getOptions().put( "type", Program.TYPE_LOOKUP.get( program.getType() ) );

        ProgramStage programStage = program.getProgramStageByStage( 1 );

        if ( programStage == null )
        {
            if ( (Program.SINGLE_EVENT_WITHOUT_REGISTRATION == program.getType()
                || Program.SINGLE_EVENT_WITH_REGISTRATION == program.getType()) )
            {
                throw new NullPointerException();
            }
            else
            {
                return form;
            }
        }

        form.getOptions().put( "captureCoordinates", programStage.getCaptureCoordinates() );

        if ( programStage.getProgramStageSections().size() > 0 )
        {
            for ( ProgramStageSection section : programStage.getProgramStageSections() )
            {
                List<Field> fields = inputFromProgramStageDataElements( section.getProgramStageDataElements() );

                Group group = new Group();
                group.setLabel( section.getDisplayName() );
                group.setDataElementCount( section.getProgramStageDataElements().size() );
                group.setFields( fields );
                form.getGroups().add( group );
            }
        }
        else
        {
            List<Field> fields = inputFromProgramStageDataElements(
                new ArrayList<>( programStage.getProgramStageDataElements() ) );

            Group group = new Group();
            group.setLabel( "default" );
            group.setFields( fields );
            group.setDataElementCount( programStage.getProgramStageDataElements().size() );
            form.getGroups().add( group );
        }

        return form;
    }

    private static List<Field> inputFromProgramStageDataElements( List<ProgramStageDataElement> programStageDataElements )
    {
        List<DataElement> dataElements = new ArrayList<>();

        for ( ProgramStageDataElement programStageDataElement : programStageDataElements )
        {
            dataElements.add( programStageDataElement.getDataElement() );
        }

        return inputFromDataElements( dataElements, new ArrayList<DataElementOperand>() );
    }

    private static List<Field> inputFromDataElements( List<DataElement> dataElements )
    {
        return inputFromDataElements( dataElements, new ArrayList<DataElementOperand>() );
    }

    private static List<Field> inputFromDataElements( List<DataElement> dataElements, final List<DataElementOperand> greyedFields )
    {
        List<Field> fields = new ArrayList<>();

        for ( DataElement dataElement : dataElements )
        {
            for ( DataElementCategoryOptionCombo categoryOptionCombo : dataElement.getCategoryCombo().getSortedOptionCombos() )
            {
                if ( !isDisabled( dataElement, categoryOptionCombo, greyedFields ) )
                {
                    Field field = new Field();

                    if ( categoryOptionCombo.isDefault() )
                    {
                        field.setLabel( dataElement.getFormNameFallback() );
                    }
                    else
                    {
                        field.setLabel( dataElement.getFormNameFallback() + " " + categoryOptionCombo.getDisplayName() );
                    }

                    field.setDataElement( dataElement.getUid() );
                    field.setCategoryOptionCombo( categoryOptionCombo.getUid() );
                    field.setType( inputTypeFromDataElement( dataElement ) );

                    if ( dataElement.getOptionSet() != null )
                    {
                        field.setOptionSet( dataElement.getOptionSet().getUid() );
                    }

                    fields.add( field );
                }
            }
        }

        return fields;
    }

    private static boolean isDisabled( DataElement dataElement, DataElementCategoryOptionCombo dataElementCategoryOptionCombo, List<DataElementOperand> greyedFields )
    {
        for ( DataElementOperand operand : greyedFields )
        {
            if ( dataElement.getUid().equals( operand.getDataElement().getUid() )
                && dataElementCategoryOptionCombo.getUid().equals( operand.getCategoryOptionCombo().getUid() ) )
            {
                return true;
            }
        }

        return false;
    }

    private static InputType inputTypeFromDataElement( DataElement dataElement )
    {
        //TODO harmonize / use map
        
        if ( DataElement.VALUE_TYPE_STRING.equals( dataElement.getType() ) )
        {
            if ( DataElement.VALUE_TYPE_TEXT.equals( dataElement.getTextType() ) )
            {
                return InputType.TEXT;
            }
            if ( DataElement.VALUE_TYPE_LONG_TEXT.equals( dataElement.getTextType() ) )
            {
                return InputType.LONG_TEXT;
            }
        }
        else if ( DataElement.VALUE_TYPE_INT.equals( dataElement.getType() ) )
        {
            if ( DataElement.VALUE_TYPE_NUMBER.equals( dataElement.getNumberType() ) )
            {
                return InputType.NUMBER;
            }
            else if ( DataElement.VALUE_TYPE_INT.equals( dataElement.getNumberType() ) )
            {
                return InputType.INTEGER;
            }
            else if ( DataElement.VALUE_TYPE_POSITIVE_INT.equals( dataElement.getNumberType() ) )
            {
                return InputType.INTEGER_POSITIVE;
            }
            else if ( DataElement.VALUE_TYPE_ZERO_OR_POSITIVE_INT.equals( dataElement.getNumberType() ) )
            {
                return InputType.INTEGER_ZERO_OR_POSITIVE;
            }
            else if ( DataElement.VALUE_TYPE_NEGATIVE_INT.equals( dataElement.getNumberType() ) )
            {
                return InputType.INTEGER_NEGATIVE;
            }
            else if ( DataElement.VALUE_TYPE_UNIT_INTERVAL.equals( dataElement.getNumberType() ) )
            {
                return InputType.UNIT_INTERVAL;
            }
            else if ( DataElement.VALUE_TYPE_PERCENTAGE.equals( dataElement.getNumberType() ) )
            {
                return InputType.PERCENTAGE;
            }
        }
        else if ( DataElement.VALUE_TYPE_BOOL.equals( dataElement.getType() ) )
        {
            return InputType.BOOLEAN;
        }
        else if ( DataElement.VALUE_TYPE_TRUE_ONLY.equals( dataElement.getType() ) )
        {
            return InputType.TRUE_ONLY;
        }
        else if ( DataElement.VALUE_TYPE_DATE.equals( dataElement.getType() ) )
        {
            return InputType.DATE;
        }

        return null;
    }

    public static void fillWithDataValues( Form form, Collection<DataValue> dataValues )
    {
        Map<String, Field> operandFieldMap = buildCacheMap( form );

        for ( DataValue dataValue : dataValues )
        {
            DataElement dataElement = dataValue.getDataElement();
            DataElementCategoryOptionCombo categoryOptionCombo = dataValue.getCategoryOptionCombo();

            Field field = operandFieldMap.get( dataElement.getUid() + SEP + categoryOptionCombo.getUid() );
            
            field.setValue( dataValue.getValue() );
            field.setComment( dataValue.getComment() );
        }
    }

    private static Map<String, Field> buildCacheMap( Form form )
    {
        Map<String, Field> cacheMap = new HashMap<>();

        for ( Group group : form.getGroups() )
        {
            for ( Field field : group.getFields() )
            {
                cacheMap.put( field.getDataElement() + SEP + field.getCategoryOptionCombo(), field );
            }
        }

        return cacheMap;
    }
}
