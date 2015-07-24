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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.ExportView;
import org.hisp.dhis.dxf2.datavalue.custom.CustomPeriod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Lars Helge Overland
 */
@JacksonXmlRootElement( localName = "periodSet", namespace = DxfNamespaces.DXF_2_0 )
public class PeriodSetBack
{

	protected static final String FIELD_TYPE = "type";
    protected static final String FIELD_COMPLETEDATE = "completeDate";
    protected static final String FIELD_CREATED = "created";
    protected static final String FIELD_LASTUPDATED = "lastUpdated";
    
    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    protected String name;
	protected String type;
    protected String completeDate;
    protected String created;
    protected String lastUpdated;
    
    protected List<CustomPeriod> periods = new ArrayList<>();
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public PeriodSetBack()
    {
    }

    //--------------------------------------------------------------------------
    // Getters and setters
    //--------------------------------------------------------------------------

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getCompleteDate()
    {
        return completeDate;
    }

    public void setCompleteDate( String completeDate )
    {
        this.completeDate = completeDate;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getCreated()
    {
        return created;
    }

    public void setCreated( String created )
    {
        this.created = created;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated( String lastUpdated )
    {
        this.lastUpdated = lastUpdated;
    }
    
    @JsonProperty( value = "periods" )
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "periods", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "period", namespace = DxfNamespaces.DXF_2_0 )
    public List<CustomPeriod> getPeriods()
    {
        return periods;
    }

    public void setPeriods( List<CustomPeriod> periods )
    {
        this.periods = periods;
    }
    
    //--------------------------------------------------------------------------
    // Logic
    //--------------------------------------------------------------------------

    private Iterator<CustomPeriod> periodIterator;

    public void refreshPeriodIterator()
    {
        periodIterator = periods.iterator();
    }

    public boolean hasNextPeriod()
    {
        if ( periodIterator == null )
        {
            refreshPeriodIterator();
        }

        return periodIterator.hasNext();
    }

    public CustomPeriod getNextPeriod()
    {
        if ( periodIterator == null )
        {
            refreshPeriodIterator();
        }

        return periodIterator.next();
    }

    public CustomPeriod getPeriodInstance()
    {
        return new CustomPeriod();
    }

    public void close()
    {
    }

}
