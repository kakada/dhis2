package org.hisp.dhis.importexport.dhis14.xml.converter;

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

import static org.hisp.dhis.system.util.CsvUtils.NEWLINE;
import static org.hisp.dhis.system.util.CsvUtils.SEPARATOR_B;
import static org.hisp.dhis.system.util.CsvUtils.csvEncode;
import static org.hisp.dhis.system.util.CsvUtils.getCsvEndValue;
import static org.hisp.dhis.system.util.CsvUtils.getCsvValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.amplecode.quick.BatchHandler;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.datavalue.DeflatedDataValue;
import org.hisp.dhis.importexport.CSVConverter;
import org.hisp.dhis.importexport.DeflatedDataValueDaily;
import org.hisp.dhis.importexport.ExportParams;
import org.hisp.dhis.importexport.ImportDataValue;
import org.hisp.dhis.importexport.ImportObjectService;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.analysis.ImportAnalyser;
import org.hisp.dhis.importexport.dhis14.util.Dhis14TypeHandler;
import org.hisp.dhis.importexport.importer.DataValueImporter;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.DailyPeriodType;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.system.util.MimicingHashMap;
import org.hisp.dhis.system.util.StreamUtils;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class DataValueDailyConverter
    extends DataValueImporter
    implements CSVConverter
{
    private static final String SEPARATOR = ",";

    private static final String FILENAME = "RoutineDataDailyCapture.txt";

    private DataElementCategoryService categoryService;

    private PeriodService periodService;

    private DataElementService dataElementService;

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    private Map<Object, Integer> dataElementMapping;

    private Map<Object, Integer> periodMapping;

    private Map<Object, Integer> sourceMapping;

    private BigDecimal totalEntry;

    private int periodId = 0;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public DataValueDailyConverter( PeriodService periodService, DataValueService dataValueService,
        DataElementService dataElementService )
    {
        this.periodService = periodService;
        this.dataValueService = dataValueService;
        this.dataElementService = dataElementService;
    }

    /**
     * Constructor for read operations.
     */
    public DataValueDailyConverter( BatchHandler<ImportDataValue> importDataValueBatchHandler,
        DataElementCategoryService categoryService, ImportObjectService importObjectService,
        ImportAnalyser importAnalyser, ImportParams params )
    {
        this.importDataValueBatchHandler = importDataValueBatchHandler;
        this.categoryService = categoryService;
        this.importObjectService = importObjectService;
        this.importAnalyser = importAnalyser;
        this.params = params;
        this.dataElementMapping = new MimicingHashMap<>();
        this.periodMapping = new MimicingHashMap<>();
        this.sourceMapping = new MimicingHashMap<>();
    }

    // -------------------------------------------------------------------------
    // CSVConverter implementation
    // -------------------------------------------------------------------------

    @Override
    public void write( ZipOutputStream out, ExportParams params )
    {
        try
        {
            out.putNextEntry( new ZipEntry( FILENAME ) );

            out.write( getCsvValue( csvEncode( "RoutineDataDailyCaptureID" ) ) );
            out.write( getCsvValue( csvEncode( "OrgUnitID" ) ) );
            out.write( getCsvValue( csvEncode( "DataElementID" ) ) );
            out.write( getCsvValue( csvEncode( "DataPeriodID" ) ) );
            out.write( getCsvValue( csvEncode( "Day01" ) ) );
            out.write( getCsvValue( csvEncode( "Day02" ) ) );
            out.write( getCsvValue( csvEncode( "Day03" ) ) );
            out.write( getCsvValue( csvEncode( "Day04" ) ) );
            out.write( getCsvValue( csvEncode( "Day05" ) ) );
            out.write( getCsvValue( csvEncode( "Day06" ) ) );
            out.write( getCsvValue( csvEncode( "Day07" ) ) );
            out.write( getCsvValue( csvEncode( "Day08" ) ) );
            out.write( getCsvValue( csvEncode( "Day09" ) ) );
            out.write( getCsvValue( csvEncode( "Day10" ) ) );
            out.write( getCsvValue( csvEncode( "Day11" ) ) );
            out.write( getCsvValue( csvEncode( "Day12" ) ) );
            out.write( getCsvValue( csvEncode( "Day13" ) ) );
            out.write( getCsvValue( csvEncode( "Day14" ) ) );
            out.write( getCsvValue( csvEncode( "Day15" ) ) );
            out.write( getCsvValue( csvEncode( "Day16" ) ) );
            out.write( getCsvValue( csvEncode( "Day17" ) ) );
            out.write( getCsvValue( csvEncode( "Day18" ) ) );
            out.write( getCsvValue( csvEncode( "Day19" ) ) );
            out.write( getCsvValue( csvEncode( "Day20" ) ) );
            out.write( getCsvValue( csvEncode( "Day21" ) ) );
            out.write( getCsvValue( csvEncode( "Day22" ) ) );
            out.write( getCsvValue( csvEncode( "Day23" ) ) );
            out.write( getCsvValue( csvEncode( "Day24" ) ) );
            out.write( getCsvValue( csvEncode( "Day25" ) ) );
            out.write( getCsvValue( csvEncode( "Day26" ) ) );
            out.write( getCsvValue( csvEncode( "Day27" ) ) );
            out.write( getCsvValue( csvEncode( "Day28" ) ) );
            out.write( getCsvValue( csvEncode( "Day29" ) ) );
            out.write( getCsvValue( csvEncode( "Day30" ) ) );
            out.write( getCsvValue( csvEncode( "Day31" ) ) );
            out.write( getCsvValue( csvEncode( "EntryNumber" ) ) );
            out.write( getCsvValue( csvEncode( "Check" ) ) );
            out.write( getCsvValue( csvEncode( "Verified" ) ) );
            out.write( getCsvValue( csvEncode( "Deleted" ) ) );
            out.write( getCsvValue( csvEncode( "Comment" ) ) );
            out.write( getCsvValue( csvEncode( "LastUserID" ) ) );
            out.write( getCsvEndValue( csvEncode( "LastUpdated" ) ) );

            out.write( NEWLINE );

            int j = 1;
            if ( params.isIncludeDataValues() )
            {
                if ( params.getStartDate() != null && params.getEndDate() != null )
                {
                    Collection<DeflatedDataValue> values = null;

                    Collection<Period> periods = periodService.getIntersectingPeriods( params.getStartDate(),
                        params.getEndDate() );

                    for ( final Period period : periods )
                    {
                        if ( period.getPeriodType().getName().equals( MonthlyPeriodType.NAME ) )
                        {
                            if ( period.getStartDate().equals( params.getStartDate() ) )
                            {
                                periodId = period.getId();
                                break;
                            }
                        }
                    }

                    HashMap<String, DeflatedDataValueDaily> dailyDataCache = new HashMap<>();

                    for ( final Integer element : params.getDataElements() )
                    {

                        for ( final Period period : periods )
                        {
                            if ( period.getPeriodType().getName().equals( DailyPeriodType.NAME ) )
                            {
                                values = dataValueService.getDeflatedDataValues( element, period.getId(),
                                    params.getOrganisationUnits() );

                                for ( final DeflatedDataValue value : values )
                                {
                                    String dailyDatakey = period.getStartDateString().substring( 0, 7 )
                                        + value.getDataElementId() + value.getSourceId();
                                    if ( dailyDataCache.containsKey( dailyDatakey ) )
                                    {

                                        setCachedDailyDataCapture( dailyDataCache, dailyDatakey, value,
                                            period.getStartDateString() );

                                    }
                                    else
                                    {
                                        
                                        DeflatedDataValueDaily newValue = setDailyDataCapture( value,
                                            period.getStartDateString() );
                                        dailyDataCache.put( dailyDatakey, newValue );
                                    }

                                }

                            }

                        }
                    }

                    for ( String key : dailyDataCache.keySet() )
                    {
                        DeflatedDataValueDaily value = dailyDataCache.get( key );
                        out.write( getCsvValue( j ) );
                        out.write( getCsvValue( value.getSourceId() ) );
                        out.write( getCsvValue( value.getDataElementId() ) );
                        out.write( getCsvValue( periodId ) );
                        out = getCSVDataExportField( out, value );
                        out.write( getCsvValue( 0 ) );
                        out.write( getCsvValue( 0 ) );
                        out.write( getCsvValue( 0 ) );
                        out.write( getCsvValue( csvEncode( value.getComment() ) ) );
                        out.write( getCsvValue( 1594 ) );
                        if ( value.getLastUpdated() != null )
                        {
                            out.write( getCsvEndValue( DateUtils.getAccessDateString( value.getLastUpdated() ) ) );
                        }
                        else
                        {
                            out.write( getCsvEndValue( DateUtils.getAccessDateString( params.getStartDate() ) ) );
                        }

                        out.write( NEWLINE );

                        j++;
                    }

                }
            }

            StreamUtils.closeZipEntry( out );
        }
        catch ( IOException ex )
        {
            throw new RuntimeException( "Failed to write data", ex );
        }
    }

    public void setCachedDailyDataCapture( HashMap<String, DeflatedDataValueDaily> cachedValue, String key,
        DeflatedDataValue value, String period )
    {

        if ( period.endsWith( "-01" ) )
        {
            cachedValue.get( key ).setDay1( value.getValue() );
        }
        else if ( period.endsWith( "-02" ) )
        {
            cachedValue.get( key ).setDay2( value.getValue() );
        }
        else if ( period.endsWith( "-03" ) )
        {
            cachedValue.get( key ).setDay3( value.getValue() );
        }
        else if ( period.endsWith( "-04" ) )
        {
            cachedValue.get( key ).setDay4( value.getValue() );
        }
        else if ( period.endsWith( "-05" ) )
        {
            cachedValue.get( key ).setDay5( value.getValue() );
        }
        else if ( period.endsWith( "-06" ) )
        {
            cachedValue.get( key ).setDay6( value.getValue() );
        }
        else if ( period.endsWith( "-07" ) )
        {
            cachedValue.get( key ).setDay7( value.getValue() );
        }
        else if ( period.endsWith( "-08" ) )
        {
            cachedValue.get( key ).setDay8( value.getValue() );
        }
        else if ( period.endsWith( "-09" ) )
        {
            cachedValue.get( key ).setDay9( value.getValue() );
        }
        else if ( period.endsWith( "-10" ) )
        {
            cachedValue.get( key ).setDay10( value.getValue() );
        }
        else if ( period.endsWith( "-11" ) )
        {
            cachedValue.get( key ).setDay11( value.getValue() );
        }
        else if ( period.endsWith( "-12" ) )
        {
            cachedValue.get( key ).setDay12( value.getValue() );
        }
        else if ( period.endsWith( "-13" ) )
        {
            cachedValue.get( key ).setDay13( value.getValue() );
        }
        else if ( period.endsWith( "-14" ) )
        {
            cachedValue.get( key ).setDay14( value.getValue() );
        }
        else if ( period.endsWith( "-15" ) )
        {
            cachedValue.get( key ).setDay15( value.getValue() );
        }
        else if ( period.endsWith( "-16" ) )
        {
            cachedValue.get( key ).setDay16( value.getValue() );
        }
        else if ( period.endsWith( "-17" ) )
        {
            cachedValue.get( key ).setDay17( value.getValue() );
        }
        else if ( period.endsWith( "-18" ) )
        {
            cachedValue.get( key ).setDay18( value.getValue() );
        }
        else if ( period.endsWith( "-19" ) )
        {
            cachedValue.get( key ).setDay19( value.getValue() );
        }
        else if ( period.endsWith( "-20" ) )
        {
            cachedValue.get( key ).setDay20( value.getValue() );
        }
        else if ( period.endsWith( "-21" ) )
        {
            cachedValue.get( key ).setDay21( value.getValue() );
        }
        else if ( period.endsWith( "-22" ) )
        {
            cachedValue.get( key ).setDay22( value.getValue() );
        }
        else if ( period.endsWith( "-23" ) )
        {
            cachedValue.get( key ).setDay23( value.getValue() );
        }
        else if ( period.endsWith( "-24" ) )
        {
            cachedValue.get( key ).setDay24( value.getValue() );
        }
        else if ( period.endsWith( "-25" ) )
        {
            cachedValue.get( key ).setDay25( value.getValue() );
        }
        else if ( period.endsWith( "-26" ) )
        {
            cachedValue.get( key ).setDay26( value.getValue() );
        }
        else if ( period.endsWith( "-27" ) )
        {
            cachedValue.get( key ).setDay27( value.getValue() );
        }
        else if ( period.endsWith( "-28" ) )
        {
            cachedValue.get( key ).setDay28( value.getValue() );
        }
        else if ( period.endsWith( "-29" ) )
        {
            cachedValue.get( key ).setDay29( value.getValue() );
        }
        else if ( period.endsWith( "-30" ) )
        {
            cachedValue.get( key ).setDay30( value.getValue() );
        }
        else if ( period.endsWith( "-31" ) )
        {
            cachedValue.get( key ).setDay31( value.getValue() );
        }

        // return cachedValue;
    }

    public DeflatedDataValueDaily setDailyDataCapture( DeflatedDataValue newDataValue, String period )
    {

        DeflatedDataValueDaily value = new DeflatedDataValueDaily();
        
        value.setCategoryOptionComboId( newDataValue.getCategoryOptionComboId() );
        value.setCategoryOptionComboName( newDataValue.getCategoryOptionComboName() );
        value.setComment( newDataValue.getComment() );
        value.setDataElementId( newDataValue.getDataElementId() );
        value.setDataElementName( newDataValue.getDataElementName() );
        value.setMax( newDataValue.getMax() );
        value.setMin( newDataValue.getMin() );
        value.setPeriod( newDataValue.getPeriod() );
        value.setPeriodId( newDataValue.getPeriodId() );
        value.setSourceId( newDataValue.getSourceId() );
        value.setSourceName( newDataValue.getSourceName() );
        value.setStoredBy( newDataValue.getStoredBy() );
        value.setLastUpdated( newDataValue.getLastUpdated() );
        value.setValue( newDataValue.getValue() );
        
        if ( period.endsWith( "-01" ) )
        {
            value.setDay1( value.getValue() );
        }
        else if ( period.endsWith( "-02" ) )
        {
            value.setDay2( value.getValue() );
        }
        else if ( period.endsWith( "-03" ) )
        {
            value.setDay3( value.getValue() );
        }
        else if ( period.endsWith( "-04" ) )
        {
            value.setDay4( value.getValue() );
        }
        else if ( period.endsWith( "-05" ) )
        {
            value.setDay5( value.getValue() );
        }
        else if ( period.endsWith( "-06" ) )
        {
            value.setDay6( value.getValue() );
        }
        else if ( period.endsWith( "-07" ) )
        {
            value.setDay7( value.getValue() );
        }
        else if ( period.endsWith( "-08" ) )
        {
            value.setDay8( value.getValue() );
        }
        else if ( period.endsWith( "-09" ) )
        {
            value.setDay9( value.getValue() );
        }
        else if ( period.endsWith( "-10" ) )
        {
            value.setDay10( value.getValue() );
        }
        else if ( period.endsWith( "-11" ) )
        {
            value.setDay11( value.getValue() );
        }
        else if ( period.endsWith( "-12" ) )
        {
            value.setDay12( value.getValue() );
        }
        else if ( period.endsWith( "-13" ) )
        {
            value.setDay13( value.getValue() );
        }
        else if ( period.endsWith( "-14" ) )
        {
            value.setDay14( value.getValue() );
        }
        else if ( period.endsWith( "-15" ) )
        {
            value.setDay15( value.getValue() );
        }
        else if ( period.endsWith( "-16" ) )
        {
            value.setDay16( value.getValue() );
        }
        else if ( period.endsWith( "-17" ) )
        {
            value.setDay17( value.getValue() );
        }
        else if ( period.endsWith( "-18" ) )
        {
            value.setDay18( value.getValue() );
        }
        else if ( period.endsWith( "-19" ) )
        {
            value.setDay19( value.getValue() );
        }
        else if ( period.endsWith( "-20" ) )
        {
            value.setDay20( value.getValue() );
        }
        else if ( period.endsWith( "-21" ) )
        {
            value.setDay21( value.getValue() );
        }
        else if ( period.endsWith( "-22" ) )
        {
            value.setDay22( value.getValue() );
        }
        else if ( period.endsWith( "-23" ) )
        {
            value.setDay23( value.getValue() );
        }
        else if ( period.endsWith( "-24" ) )
        {
            value.setDay24( value.getValue() );
        }
        else if ( period.endsWith( "-25" ) )
        {
            value.setDay25( value.getValue() );
        }
        else if ( period.endsWith( "-26" ) )
        {
            value.setDay26( value.getValue() );
        }
        else if ( period.endsWith( "-27" ) )
        {
            value.setDay27( value.getValue() );
        }
        else if ( period.endsWith( "-28" ) )
        {
            value.setDay28( value.getValue() );
        }
        else if ( period.endsWith( "-29" ) )
        {
            value.setDay29( value.getValue() );
        }
        else if ( period.endsWith( "-30" ) )
        {
            value.setDay30( value.getValue() );
        }
        else if ( period.endsWith( "-31" ) )
        {
            value.setDay31( value.getValue() );
        }

        return value;
    }

    @Override
    public void read( BufferedReader reader, ImportParams params )
    {
        String line = "";

        DataValue value = new DataValue();
        DataElement dataElement = new DataElement();
        Period period = new Period();
        OrganisationUnit organisationUnit = new OrganisationUnit();
        DataElementCategoryOptionCombo categoryOptionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
        DataElementCategoryOptionCombo proxyCategoryOptionCombo = new DataElementCategoryOptionCombo();
        proxyCategoryOptionCombo.setId( categoryOptionCombo.getId() );
        final String owner = params.getOwner();

        try
        {
            reader.readLine(); // Skip CSV header

            while ( (line = reader.readLine()) != null )
            {
                String[] values = line.split( SEPARATOR );
                Boolean validValue = true;

                dataElement.setId( dataElementMapping.get( Integer.parseInt( values[2] ) ) );
                period.setId( periodMapping.get( Integer.parseInt( values[3] ) ) );
                organisationUnit.setId( sourceMapping.get( Integer.parseInt( values[1] ) ) );

                value.setDataElement( dataElement );
                value.setPeriod( period );
                value.setSource( organisationUnit );

                if ( !values[6].isEmpty() ) // Numeric
                {
                    value.setValue( handleNumericValue( values[6] ) );
                    validValue = isValidNumeric( value.getValue() );

                }
                else if ( !values[4].isEmpty() ) // Text
                {
                    value.setValue( values[4].trim() );
                }
                else if ( !values[5].isEmpty() ) // Boolean
                {
                    value.setValue( Dhis14TypeHandler.convertYesNoFromDhis14( Integer.parseInt( values[5] ) ) );

                }
                else if ( !values[7].isEmpty() ) // Date
                {
                    value.setValue( values[7] );

                }
                else if ( !values[8].isEmpty() ) // Memo not supported
                {
                    validValue = false;
                }

                else if ( !values[9].isEmpty() ) // OLE not supported
                {
                    validValue = false;
                }

                value.setComment( values[13] );
                value.setLastUpdated( DateUtils.getDefaultDate( values[15] ) );
                value.setCreated( value.getLastUpdated() );
                value.setCategoryOptionCombo( proxyCategoryOptionCombo );
                value.setStoredBy( owner );

                if ( validValue )
                {
                    importObject( value, params );
                }
            }
        }
        catch ( IOException ex )
        {
            throw new RuntimeException( "Failed to read data", ex );
        }
    }

    // -------------------------------------------------------------------------
    // CSVConverter implementation
    // -------------------------------------------------------------------------

    private String handleNumericValue( String value )
    {
        if ( value != null )
        {
            // Remove all spaces
            value = value.replaceAll( " ", "" );
            // Remove all quotes
            value = value.replaceAll( "\"", "" );
            // Strip trailing zeros
            value = value.replaceAll( "\\.0+$", "" );
        }

        return value;
    }

    private boolean isValidNumeric( String value )
    {
        return value != null && value.matches( "-?\\d+(\\.\\d+)?" );
    }

    private ZipOutputStream getCSVDataExportField( ZipOutputStream out, DeflatedDataValueDaily value )
    {

        String dataElementType = dataElementService.getDataElement( value.getDataElementId() ).getType();

        try
        {
            if ( dataElementType.equals( DataElement.VALUE_TYPE_STRING ) )
            {
                out.write( getCsvValue( csvEncode( value.getValue() ) ) );
                out.write( SEPARATOR_B );
                out.write( SEPARATOR_B );
                out.write( SEPARATOR_B );
                out.write( SEPARATOR_B );
                out.write( SEPARATOR_B );
            }

            else if ( dataElementType.equals( DataElement.VALUE_TYPE_BOOL ) )
            {
                out.write( SEPARATOR_B );
                out.write( getCsvValue( csvEncode( Dhis14TypeHandler.convertBooleanToDhis14( value.getValue() ) ) ) );
                out.write( SEPARATOR_B );
                out.write( SEPARATOR_B );
                out.write( SEPARATOR_B );
                out.write( SEPARATOR_B );
            }

            else if ( dataElementType.equals( DataElement.VALUE_TYPE_NUMBER )
                || dataElementType.equals( DataElement.VALUE_TYPE_INT )
                || dataElementType.equals( DataElement.VALUE_TYPE_NEGATIVE_INT )
                || dataElementType.equals( DataElement.VALUE_TYPE_POSITIVE_INT )
                || dataElementType.equals( DataElement.VALUE_TYPE_ZERO_OR_POSITIVE_INT ) )
            {

                totalEntry = new BigDecimal( "0" );
                out.write( getCsvValue( value.getDay1() ) );
                addTotalEntry( value.getDay1() );
                out.write( getCsvValue( value.getDay2() ) );
                addTotalEntry( value.getDay2() );
                out.write( getCsvValue( value.getDay3() ) );
                addTotalEntry( value.getDay3() );
                out.write( getCsvValue( value.getDay4() ) );
                addTotalEntry( value.getDay4() );
                out.write( getCsvValue( value.getDay5() ) );
                addTotalEntry( value.getDay5() );
                out.write( getCsvValue( value.getDay6() ) );
                addTotalEntry( value.getDay6() );
                out.write( getCsvValue( value.getDay7() ) );
                addTotalEntry( value.getDay7() );
                out.write( getCsvValue( value.getDay8() ) );
                addTotalEntry( value.getDay8() );
                out.write( getCsvValue( value.getDay9() ) );
                addTotalEntry( value.getDay9() );
                out.write( getCsvValue( value.getDay10() ) );
                addTotalEntry( value.getDay10() );
                out.write( getCsvValue( value.getDay11() ) );
                addTotalEntry( value.getDay11() );
                out.write( getCsvValue( value.getDay12() ) );
                addTotalEntry( value.getDay12() );
                out.write( getCsvValue( value.getDay13() ) );
                addTotalEntry( value.getDay13() );
                out.write( getCsvValue( value.getDay14() ) );
                addTotalEntry( value.getDay14() );
                out.write( getCsvValue( value.getDay15() ) );
                addTotalEntry( value.getDay15() );
                out.write( getCsvValue( value.getDay16() ) );
                addTotalEntry( value.getDay16() );
                out.write( getCsvValue( value.getDay17() ) );
                addTotalEntry( value.getDay17() );
                out.write( getCsvValue( value.getDay18() ) );
                addTotalEntry( value.getDay18() );
                out.write( getCsvValue( value.getDay19() ) );
                addTotalEntry( value.getDay19() );
                out.write( getCsvValue( value.getDay20() ) );
                addTotalEntry( value.getDay20() );
                out.write( getCsvValue( value.getDay21() ) );
                addTotalEntry( value.getDay21() );
                out.write( getCsvValue( value.getDay22() ) );
                addTotalEntry( value.getDay22() );
                out.write( getCsvValue( value.getDay23() ) );
                addTotalEntry( value.getDay23() );
                out.write( getCsvValue( value.getDay24() ) );
                addTotalEntry( value.getDay24() );
                out.write( getCsvValue( value.getDay25() ) );
                addTotalEntry( value.getDay25() );
                out.write( getCsvValue( value.getDay26() ) );
                addTotalEntry( value.getDay26() );
                out.write( getCsvValue( value.getDay27() ) );
                addTotalEntry( value.getDay27() );
                out.write( getCsvValue( value.getDay28() ) );
                addTotalEntry( value.getDay28() );
                out.write( getCsvValue( value.getDay29() ) );
                addTotalEntry( value.getDay29() );
                out.write( getCsvValue( value.getDay30() ) );
                addTotalEntry( value.getDay30() );
                out.write( getCsvValue( value.getDay31() ) );
                addTotalEntry( value.getDay31() );
                // out.write( SEPARATOR_B );
                // added
                out.write( getCsvValue( totalEntry + "" ) );

            }

            else if ( dataElementType.equals( DataElement.VALUE_TYPE_DATE ) )
            {
                out.write( SEPARATOR_B );
                out.write( SEPARATOR_B );
                out.write( SEPARATOR_B );
                out.write( getCsvValue( csvEncode( DateUtils.getDefaultDate( value.getValue() ) ) ) );
                out.write( SEPARATOR_B );
                out.write( SEPARATOR_B );
            }
        }

        catch ( IOException ex )
        {
            throw new RuntimeException( "Failed handle CSV data field export", ex );
        }

        return out;
    }

    public void addTotalEntry( String value )
    {
        if ( value != null )
        {
            BigDecimal anotherPrice = new BigDecimal( value );
            totalEntry = totalEntry.add( anotherPrice );
        }
    }
}
