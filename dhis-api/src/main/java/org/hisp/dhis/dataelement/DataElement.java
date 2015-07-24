package org.hisp.dhis.dataelement;

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

import static org.hisp.dhis.dataset.DataSet.NO_EXPIRY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.BaseDimensionalObject;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.MergeStrategy;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.DimensionalView;
import org.hisp.dhis.common.view.ExportView;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.comparator.DataSetFrequencyComparator;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.YearlyPeriodType;
import org.hisp.dhis.schema.PropertyType;
import org.hisp.dhis.schema.annotation.Property;
import org.hisp.dhis.schema.annotation.PropertyRange;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * A DataElement is a definition (meta-information about) of the entities that
 * are captured in the system. An example from public health care is a
 * DataElement representing the number BCG doses; A DataElement with "BCG dose"
 * as name, with type DataElement.TYPE_INT. DataElements can be structured
 * hierarchically, one DataElement can have a parent and a collection of
 * children. The sum of the children represent the same entity as the parent.
 * Hierarchies of DataElements are used to give more fine- or course-grained
 * representations of the entities.
 * <p/>
 * DataElement acts as a DimensionSet in the dynamic dimensional model, and as a
 * DimensionOption in the static DataElement dimension.
 *
 * @author Kristian Nordal
 */
@JacksonXmlRootElement( localName = "dataElement", namespace = DxfNamespaces.DXF_2_0 )
public class DataElement
    extends BaseDimensionalObject
{
    public static final String[] I18N_PROPERTIES = { "name", "shortName", "description", "formName" };

    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = -7131541880444446669L;

    public static final String VALUE_TYPE_INT = "int";
    public static final String VALUE_TYPE_STRING = "string";
    public static final String VALUE_TYPE_USER_NAME = "username";
    public static final String VALUE_TYPE_BOOL = "bool";
    public static final String VALUE_TYPE_TRUE_ONLY = "trueOnly";
    public static final String VALUE_TYPE_DATE = "date";
    public static final String VALUE_TYPE_UNIT_INTERVAL = "unitInterval";
    public static final String VALUE_TYPE_PERCENTAGE = "percentage";

    public static final String VALUE_TYPE_NUMBER = "number";
    public static final String VALUE_TYPE_POSITIVE_INT = "posInt";
    public static final String VALUE_TYPE_NEGATIVE_INT = "negInt";
    public static final String VALUE_TYPE_ZERO_OR_POSITIVE_INT = "zeroPositiveInt";
    public static final String VALUE_TYPE_TEXT = "text";
    public static final String VALUE_TYPE_LONG_TEXT = "longText";

    public static final String AGGREGATION_OPERATOR_SUM = "sum";
    public static final String AGGREGATION_OPERATOR_AVERAGE_SUM = "average"; // Sum in organisation unit
    public static final String AGGREGATION_OPERATOR_AVERAGE = "avg";
    public static final String AGGREGATION_OPERATOR_COUNT = "count";
    public static final String AGGREGATION_OPERATOR_STDDEV = "stddev";
    public static final String AGGREGATION_OPERATOR_VARIANCE = "variance";
    public static final String AGGREGATION_OPERATOR_MIN = "min";
    public static final String AGGREGATION_OPERATOR_MAX = "max";
    public static final String AGGREGATION_OPERATOR_NONE = "none";

    /**
     * The name to appear in forms.
     */
    private String formName;

    /**
     * The i18n variant of the display name. Should not be persisted.
     */
    protected transient String displayFormName;

    /**
     * The domain of this DataElement; e.g. DataElementDomainType.aggregate or
     * DataElementDomainType.TRACKER.
     */
    private DataElementDomain domainType;

    /**
     * The value type of this DataElement; e.g. DataElement.VALUE_TYPE_INT or
     * DataElement.VALUE_TYPE_BOOL.
     */
    private String type;

    /**
     * The number type. Is relevant when type is VALUE_TYPE_INT.
     */
    private String numberType;

    /**
     * The text type. Is relevant when type is VALUE_TYPE_STRING.
     */
    private String textType;

    /**
     * The aggregation operator of this DataElement; e.g. DataElement.SUM og
     * DataElement.AVERAGE.
     */
    private String aggregationOperator;

    /**
     * A combination of categories to capture data.
     */
    private DataElementCategoryCombo categoryCombo;

    /**
     * URL for lookup of additional information on the web.
     */
    private String url;

    /**
     * The data element groups which this
     */
    private Set<DataElementGroup> groups = new HashSet<>();

    /**
     * The data sets which this data element is a member of.
     */
    private Set<DataSet> dataSets = new HashSet<>();

    /**
     * The lower organisation unit levels for aggregation.
     */
    private List<Integer> aggregationLevels = new ArrayList<>();

    /**
     * There is no point of saving 0's for this data element default is false
     * ,we don't want to store 0's if not set to true
     */
    private boolean zeroIsSignificant;

    /**
     * Set of the dynamic attributes values that belong to this data element.
     */
    private Set<AttributeValue> attributeValues = new HashSet<>();

    /**
     * The option set for data values linked to this data element.
     */
    private OptionSet optionSet;

    /**
     * The option set for comments linked to this data element.
     */
    private OptionSet commentOptionSet;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public DataElement()
    {
        setAutoFields();
    }

    public DataElement( String name )
    {
        this();
        this.name = name;
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public void addDataElementGroup( DataElementGroup group )
    {
        groups.add( group );
        group.getMembers().add( this );
    }

    public void removeDataElementGroup( DataElementGroup group )
    {
        groups.remove( group );
        group.getMembers().remove( this );
    }

    public void updateDataElementGroups( Set<DataElementGroup> updates )
    {
        for ( DataElementGroup group : new HashSet<>( groups ) )
        {
            if ( !updates.contains( group ) )
            {
                removeDataElementGroup( group );
            }
        }

        for ( DataElementGroup group : updates )
        {
            addDataElementGroup( group );
        }
    }

    public void addDataSet( DataSet dataSet )
    {
        dataSets.add( dataSet );
        dataSet.getDataElements().add( this );
    }

    public void removeDataSet( DataSet dataSet )
    {
        dataSets.remove( dataSet );
        dataSet.getDataElements().remove( this );
    }

    /**
     * Indicates whether the value type of this data element is numeric.
     */
    public boolean isNumericType()
    {
        return VALUE_TYPE_INT.equals( type );
    }

    /**
     * Returns the value type. If value type is int and the number type exists,
     * the number type is returned, otherwise the type is returned.
     */
    public String getDetailedNumberType()
    {
        return (type != null && type.equals( VALUE_TYPE_INT ) && numberType != null) ? numberType : type;
    }

    /**
     * Returns the value type. If value type is string and the text type exists,
     * the text type is returned, if the type is string and the text type does
     * not exist string is returned.
     */
    public String getDetailedTextType()
    {
        return (type != null && type.equals( VALUE_TYPE_STRING ) && textType != null) ? textType : type;
    }

    /**
     * Returns the detailed data element type. If value type is int, the number
     * type is returned. If value type is string, the text type is returned.
     * Otherwise the type is returned.
     */
    public String getDetailedType()
    {
        if ( VALUE_TYPE_INT.equals( type ) )
        {
            return numberType;
        }
        else if ( VALUE_TYPE_STRING.equals( type ) )
        {
            return textType;
        }
        else
        {
            return type;
        }
    }

    /**
     * Returns whether aggregation should be skipped for this data element, based
     * on the setting of the data set which this data element is a members of,
     * if any.
     */
    public boolean isSkipAggregation()
    {
        return dataSets != null && dataSets.size() > 0 && dataSets.iterator().next().isSkipAggregation();
    }

    /**
     * Returns the data set of this data element. If this data element has
     * multiple data sets, the data set with the highest collection frequency is
     * returned.
     */
    public DataSet getDataSet()
    {
        List<DataSet> list = new ArrayList<>( dataSets );
        Collections.sort( list, DataSetFrequencyComparator.INSTANCE );
        return !list.isEmpty() ? list.get( 0 ) : null;
    }

    /**
     * Returns the PeriodType of the DataElement, based on the PeriodType of the
     * DataSet which the DataElement is registered for. If this data element has
     * multiple data sets, the data set with the highest collection frequency is
     * returned.
     */
    public PeriodType getPeriodType()
    {
        DataSet dataSet = getDataSet();

        return dataSet != null ? dataSet.getPeriodType() : null;
    }

    /**
     * Indicates whether this data element requires approval of data. Returns true
     * if only one of the data sets associated with this data element requires
     * approval.
     */
    public boolean isApproveData()
    {
        for ( DataSet dataSet : dataSets )
        {
            if ( dataSet != null && dataSet.isApproveData() )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Indicates whether collecting data for future periods should be allowed for
     * this data element.
     *
     * @return true if all the associated data sets allow future periods, false otherwise.
     */
    public boolean isAllowFuturePeriods()
    {
        for ( DataSet dataSet : dataSets )
        {
            if ( dataSet != null && !dataSet.isAllowFuturePeriods() )
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the frequency order for the PeriodType of this DataElement. If no
     * PeriodType exists, 0 is returned.
     */
    public int getFrequencyOrder()
    {
        PeriodType periodType = getPeriodType();

        return periodType != null ? periodType.getFrequencyOrder() : YearlyPeriodType.FREQUENCY_ORDER;
    }

    /**
     * Tests whether a PeriodType can be defined for the DataElement, which
     * requires that the DataElement is registered for DataSets with the same
     * PeriodType.
     */
    public boolean periodTypeIsValid()
    {
        PeriodType periodType = null;

        for ( DataSet dataSet : dataSets )
        {
            if ( periodType != null && !periodType.equals( dataSet.getPeriodType() ) )
            {
                return false;
            }

            periodType = dataSet.getPeriodType();
        }

        return true;
    }

    /**
     * Tests whether more than one aggregation level exists for the DataElement.
     */
    public boolean hasAggregationLevels()
    {
        return aggregationLevels != null && aggregationLevels.size() > 0;
    }

    public boolean hasCategoryCombo()
    {
        return categoryCombo != null;
    }
    
    /**
     * Tests whether the DataElement is associated with a
     * DataElementCategoryCombo with more than one DataElementCategory, or any
     * DataElementCategory with more than one DataElementCategoryOption.
     */
    public boolean isMultiDimensional()
    {
        if ( categoryCombo != null )
        {
            if ( categoryCombo.getCategories().size() > 1 )
            {
                return true;
            }

            for ( DataElementCategory category : categoryCombo.getCategories() )
            {
                if ( category.getCategoryOptions().size() > 1 )
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the domain type, or the default domain type if it does not exist.
     */
    public String getDomainTypeNullSafe()
    {
        return domainType != null ? domainType.getValue() : DataElementDomain.AGGREGATE.getValue();
    }

    /**
     * Returns the form name, or the name if it does not exist.
     */
    public String getFormNameFallback()
    {
        return formName != null && !formName.isEmpty() ? getDisplayFormName() : getDisplayName();
    }

    @JsonView( { DetailedView.class, DimensionalView.class } )
    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getDisplayFormName()
    {
        return displayFormName != null && !displayFormName.trim().isEmpty() ? displayFormName : formName;
    }

    public void setDisplayFormName( String displayFormName )
    {
        this.displayFormName = displayFormName;
    }

    /**
     * Returns the minimum number of expiry days from the data sets of this data
     * element.
     */
    public int getExpiryDays()
    {
        int expiryDays = Integer.MAX_VALUE;

        for ( DataSet dataSet : dataSets )
        {
            if ( dataSet.getExpiryDays() != NO_EXPIRY && dataSet.getExpiryDays() < expiryDays )
            {
                expiryDays = dataSet.getExpiryDays();
            }
        }

        return expiryDays == Integer.MAX_VALUE ? NO_EXPIRY : expiryDays;
    }

    public boolean hasDescription()
    {
        return description != null && !description.trim().isEmpty();
    }

    public boolean hasUrl()
    {
        return url != null && !url.trim().isEmpty();
    }

    public boolean hasOptionSet()
    {
        return optionSet != null;
    }

    public boolean hasLegendSet()
    {
        return legendSet != null;
    }

    // -------------------------------------------------------------------------
    // Helper getters
    // -------------------------------------------------------------------------

    @JsonProperty
    @JsonView( { DetailedView.class } )
    public boolean isOptionSetValue()
    {
        return optionSet != null;
    }
    
    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    @PropertyRange( min = 2 )
    public String getFormName()
    {
        return formName;
    }

    public void setFormName( String formName )
    {
        this.formName = formName;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public DataElementDomain getDomainType()
    {
        return domainType;
    }

    public void setDomainType( DataElementDomain domainType )
    {
        this.domainType = domainType;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getTextType()
    {
        return textType;
    }

    public void setTextType( String textType )
    {
        this.textType = textType;
    }

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
    public String getAggregationOperator()
    {
        return aggregationOperator;
    }

    public void setAggregationOperator( String aggregationOperator )
    {
        this.aggregationOperator = aggregationOperator;
    }

    @JsonProperty
    @JsonSerialize( as = BaseIdentifiableObject.class )
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public DataElementCategoryCombo getCategoryCombo()
    {
        return categoryCombo;
    }

    public void setCategoryCombo( DataElementCategoryCombo categoryCombo )
    {
        this.categoryCombo = categoryCombo;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    @Property( PropertyType.URL )
    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    @JsonProperty( "dataElementGroups" )
    @JsonSerialize( contentAs = BaseIdentifiableObject.class )
    @JsonView( { DetailedView.class } )
    @JacksonXmlElementWrapper( localName = "dataElementGroups", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "dataElementGroup", namespace = DxfNamespaces.DXF_2_0 )
    public Set<DataElementGroup> getGroups()
    {
        return groups;
    }

    public void setGroups( Set<DataElementGroup> groups )
    {
        this.groups = groups;
    }

    @JsonProperty
    @JsonSerialize( contentAs = BaseIdentifiableObject.class )
    @JsonView( { DetailedView.class } )
    @JacksonXmlElementWrapper( localName = "dataSets", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "dataSet", namespace = DxfNamespaces.DXF_2_0 )
    public Set<DataSet> getDataSets()
    {
        return dataSets;
    }

    public void setDataSets( Set<DataSet> dataSets )
    {
        this.dataSets = dataSets;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public List<Integer> getAggregationLevels()
    {
        return aggregationLevels;
    }

    public void setAggregationLevels( List<Integer> aggregationLevels )
    {
        this.aggregationLevels = aggregationLevels;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isZeroIsSignificant()
    {
        return zeroIsSignificant;
    }

    public void setZeroIsSignificant( boolean zeroIsSignificant )
    {
        this.zeroIsSignificant = zeroIsSignificant;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getNumberType()
    {
        return numberType;
    }

    public void setNumberType( String numberType )
    {
        this.numberType = numberType;
    }

    @JsonProperty( "attributeValues" )
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "attributeValues", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "attributeValue", namespace = DxfNamespaces.DXF_2_0 )
    public Set<AttributeValue> getAttributeValues()
    {
        return attributeValues;
    }

    public void setAttributeValues( Set<AttributeValue> attributeValues )
    {
        this.attributeValues = attributeValues;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public OptionSet getOptionSet()
    {
        return optionSet;
    }

    public void setOptionSet( OptionSet optionSet )
    {
        this.optionSet = optionSet;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public OptionSet getCommentOptionSet()
    {
        return commentOptionSet;
    }

    public void setCommentOptionSet( OptionSet commentOptionSet )
    {
        this.commentOptionSet = commentOptionSet;
    }

    @Override
    public void mergeWith( IdentifiableObject other, MergeStrategy strategy )
    {
        super.mergeWith( other, strategy );

        if ( other.getClass().isInstance( this ) )
        {
            DataElement dataElement = (DataElement) other;

            zeroIsSignificant = dataElement.isZeroIsSignificant();

            if ( strategy.isReplace() )
            {
                formName = dataElement.getFormName();
                domainType = dataElement.getDomainType();
                type = dataElement.getType();
                numberType = dataElement.getNumberType();
                textType = dataElement.getTextType();
                aggregationOperator = dataElement.getAggregationOperator();
                categoryCombo = dataElement.getCategoryCombo();
                url = dataElement.getUrl();
                optionSet = dataElement.getOptionSet();
                commentOptionSet = dataElement.getCommentOptionSet();
            }
            else if ( strategy.isMerge() )
            {
                formName = dataElement.getFormName() == null ? formName : dataElement.getFormName();
                domainType = dataElement.getDomainType() == null ? domainType : dataElement.getDomainType();
                type = dataElement.getType() == null ? type : dataElement.getType();
                numberType = dataElement.getNumberType() == null ? numberType : dataElement.getNumberType();
                textType = dataElement.getTextType() == null ? textType : dataElement.getTextType();
                aggregationOperator = dataElement.getAggregationOperator() == null ? aggregationOperator : dataElement.getAggregationOperator();
                categoryCombo = dataElement.getCategoryCombo() == null ? categoryCombo : dataElement.getCategoryCombo();
                url = dataElement.getUrl() == null ? url : dataElement.getUrl();
                optionSet = dataElement.getOptionSet() == null ? optionSet : dataElement.getOptionSet();
                commentOptionSet = dataElement.getCommentOptionSet() == null ? commentOptionSet : dataElement.getCommentOptionSet();
            }

            groups.clear();
            dataSets.clear();

            aggregationLevels.clear();
            aggregationLevels.addAll( dataElement.getAggregationLevels() );

            attributeValues.clear();
            attributeValues.addAll( dataElement.getAttributeValues() );
        }
    }
}
