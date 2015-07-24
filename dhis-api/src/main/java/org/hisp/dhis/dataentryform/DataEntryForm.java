package org.hisp.dhis.dataentryform;

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

import java.io.Serializable;
import java.util.regex.Pattern;

import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.ImportableObject;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.ExportView;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * @author Bharath Kumar
 */
@JacksonXmlRootElement(localName = "dataEntryForm", namespace = DxfNamespaces.DXF_2_0)
public class DataEntryForm
    implements Serializable, ImportableObject
{
    public static final String STYLE_COMFORTABLE = "comfortable";
    public static final String STYLE_REGULAR = "regular";
    public static final String STYLE_COMPACT = "compact";
    public static final String STYLE_NONE = "none";

    public static final int CURRENT_FORMAT = 2;

    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = 3183079113944562138L;

    public static final Pattern INPUT_PATTERN = Pattern.compile( "value\\[\\d+\\]\\.value:value\\[\\d+\\]\\.value" );

    public static final Pattern OPERAND_PATTERN = Pattern.compile( "\\d+" );

    /**
     * The unique identifier for this DataEntryForm
     */
    private int id;

    /**
     * Name of DataEntryForm. Required and unique.
     */
    private String name;

    /**
     * The display style to use to render the form.
     */
    private String style;

    /**
     * HTML Code of DataEntryForm
     */
    private String htmlCode;

    /**
     * The format of the DataEntryForm.
     */
    private int format;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public DataEntryForm()
    {
    }

    public DataEntryForm( String name )
    {
        this.name = name;
    }

    public DataEntryForm( String name, String htmlCode )
    {
        this.name = name;
        this.htmlCode = htmlCode;
    }

    public DataEntryForm( String name, String style, String htmlCode )
    {
        this.name = name;
        this.style = style;
        this.htmlCode = htmlCode;
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    /**
     * Indicates whether this data entry form has custom form HTML code.
     */
    public boolean hasForm()
    {
        return htmlCode != null && !htmlCode.trim().isEmpty();
    }

    // -------------------------------------------------------------------------
    // hashCode and equals
    // -------------------------------------------------------------------------

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null )
        {
            return false;
        }

        if ( !(o instanceof DataEntryForm) )
        {
            return false;
        }

        final DataEntryForm other = (DataEntryForm) o;

        return name.equals( other.getName() );
    }
    
    @Override
    public String toString()
    {
        return "[" + name + "]";
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    @Override
    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getStyle()
    {
        return style;
    }

    public void setStyle( String style )
    {
        this.style = style;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getHtmlCode()
    {
        return htmlCode;
    }

    public void setHtmlCode( String htmlCode )
    {
        this.htmlCode = htmlCode;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public int getFormat()
    {
        return format;
    }

    public void setFormat( int format )
    {
        this.format = format;
    }
}
