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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.amplecode.staxwax.reader.XMLReader;
import org.amplecode.staxwax.writer.XMLWriter;
import org.hisp.dhis.importexport.ExportParams;
import org.hisp.dhis.importexport.ImportObjectService;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.XMLConverter;
import org.hisp.dhis.importexport.dhis14.util.Dhis14DateUtil;
import org.hisp.dhis.importexport.dhis14.util.Dhis14ObjectMappingUtil;
import org.hisp.dhis.importexport.importer.PeriodImporter;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class PeriodConverter
    extends PeriodImporter
    implements XMLConverter
{
    public static final String ELEMENT_NAME = "DataPeriod";

    private static final String FIELD_ID = "DataPeriodID";

    private static final String FIELD_DATA_PERIODNAMEENG = "DataPeriodNameEng";

    private static final String FIELD_DATA_PERIODNAME = "DataPeriodName";

    private static final String FIELD_PERIOD_TYPE = "DataPeriodTypeID";

    private static final String FIELD_START_DATE = "ValidFrom";

    private static final String FIELD_END_DATE = "ValidTo";

    private static final String FIELD_SELECTED = "Selected";

    private static final String FIELD_DATAPERIODNAMEALT1 = "DataPeriodNameAlt1";

    private static final String FIELD_DATAPERIODNAMEALT2 = "DataPeriodNameAlt2";

    private static final String FIELD_DATAPERIODNAMEALT3 = "DataPeriodNameAlt3";

    private Map<String, Integer> periodTypeMapping;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructor for write operations.
     */
    public PeriodConverter( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    /**
     * Constructor for read operations.
     * 
     * @param importObjectService the importObjectService to use.
     * @param periodService the periodService to use.
     * @param periodTypeMapping the periodTypeMapping to use.
     */
    public PeriodConverter( ImportObjectService importObjectService, PeriodService periodService,
        Map<String, Integer> periodTypeMapping )
    {
        this.importObjectService = importObjectService;
        this.periodService = periodService;
        this.periodTypeMapping = periodTypeMapping;
    }

    // -------------------------------------------------------------------------
    // XMLConverter implementation
    // -------------------------------------------------------------------------

    @Override
    public void write( XMLWriter writer, ExportParams params )
    {
        Collection<Period> periods = periodService.getPeriods( params.getPeriods() );

        if ( periods != null && periods.size() > 0 )
        {
            for ( Period period : periods )
            {
                if ( period.getPeriodType().getName().equals( MonthlyPeriodType.NAME ) )
                {

                    writer.openElement( ELEMENT_NAME );

                    writer.writeElement( FIELD_ID, String.valueOf( period.getId() ) );

                    String startDate = period.getStartDateString();
                    String endDate = period.getEndDateString();

                    String dataPeriodNameEng = null;
                    String dataPeriodName = null;
                    String validFrom = null;
                    String validTo = null;

                    Calendar msAccessCalendarDate = getCalendarDate( "1899-12-29" );

                    Calendar startCalendarDate = getCalendarDate( startDate );
                    Calendar endCalendarDate = getCalendarDate( endDate );

                    dataPeriodNameEng = getDateName( startDate );
                    dataPeriodName = getDateName( startDate );

                    validFrom = Days.daysBetween( new DateTime( msAccessCalendarDate ),
                        new DateTime( startCalendarDate ) ).getDays()
                        + "";
                    validTo = Days.daysBetween( new DateTime( msAccessCalendarDate ), new DateTime( endCalendarDate ) )
                        .getDays() + "";

                    writer.writeElement( FIELD_DATA_PERIODNAMEENG, dataPeriodNameEng );
                    writer.writeElement( FIELD_DATA_PERIODNAME, dataPeriodName );
                    writer.writeElement( FIELD_PERIOD_TYPE, String.valueOf( 1 ) );
                    writer.writeElement( FIELD_START_DATE, validFrom );
                    writer.writeElement( FIELD_END_DATE, validTo );
                    writer.writeElement( FIELD_SELECTED, String.valueOf( 0 ) );
                    writer.writeElement( FIELD_DATAPERIODNAMEALT1, "" );
                    writer.writeElement( FIELD_DATAPERIODNAMEALT2, "" );
                    writer.writeElement( FIELD_DATAPERIODNAMEALT3, "" );

                    writer.closeElement();

                }
            }
        }
    }

    @Override
    public void read( XMLReader reader, ImportParams params )
    {
        Period period = new Period();

        Map<String, String> values = reader.readElements( ELEMENT_NAME );

        Integer periodTypeId = Integer.parseInt( values.get( FIELD_PERIOD_TYPE ) );
        PeriodType periodType = Dhis14ObjectMappingUtil.getPeriodTypeMap().get( periodTypeId );
        period.setPeriodType( periodType );

        period.setId( Integer.valueOf( values.get( FIELD_ID ) ) );
        period.getPeriodType().setId( periodTypeMapping.get( periodType.getName() ) );
        period.setStartDate( Dhis14DateUtil.getDate( Integer.parseInt( values.get( FIELD_START_DATE ) ) ) );
        period.setEndDate( Dhis14DateUtil.getDate( Integer.parseInt( values.get( FIELD_END_DATE ) ) ) );

        importObject( period, params );
    }

    public Calendar getCalendarDate( String date )
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
        try
        {
            cal.setTime( sdf.parse( date ) );
        }
        catch ( ParseException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return cal;
    }

    public String getDateName( String date )
    {
        DateFormat dateFormat = new SimpleDateFormat( "MMM-yy" );
        Date predefined;
        String name = "";
        try
        {
            predefined = new SimpleDateFormat( "yyyy-MM-dd" ).parse( date );
            name = dateFormat.format( predefined );
        }
        catch ( ParseException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return name;
    }

}
