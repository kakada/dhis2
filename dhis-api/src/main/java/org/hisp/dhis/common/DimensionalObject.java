package org.hisp.dhis.common;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.dataelement.CategoryOptionGroupSet;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategory;
import org.hisp.dhis.dataelement.DataElementGroupSet;
import org.hisp.dhis.legend.LegendSet;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;

/**
* @author Lars Helge Overland
*/
public interface DimensionalObject
    extends NameableObject
{
    final String DATA_X_DIM_ID = "dx"; // in, de, ds, do
    final String DATA_COLLAPSED_DIM_ID = "dy"; // Collapsed event data dimensions
    final String INDICATOR_DIM_ID = "in";
    final String DATAELEMENT_DIM_ID = "de";
    final String DATASET_DIM_ID = "ds";
    final String DATAELEMENT_OPERAND_ID = "dc";
    final String CATEGORYOPTIONCOMBO_DIM_ID = "co";
    final String PERIOD_DIM_ID = "pe";
    final String ORGUNIT_DIM_ID = "ou";
    final String ORGUNIT_GROUP_DIM_ID = "oug"; // Used for org unit target
    final String PROGRAM_INDICATOR_DIM_ID = "pin";
    final String ITEM_DIM_ID = "item";

    final String OU_MODE_SELECTED = "selected"; //TODO replace with OrganisationUnitSelectionMode
    final String OU_MODE_CHILDREN = "children";
    final String OU_MODE_DESCENDANTS = "descendants";
    final String OU_MODE_ALL = "all";
    
    final String DIMENSION_SEP = "-";

    final String LONGITUDE_DIM_ID = "longitude";
    final String LATITUDE_DIM_ID = "latitude";

    final List<String> DATA_X_DIMS = Arrays.asList( INDICATOR_DIM_ID, DATAELEMENT_DIM_ID, DATASET_DIM_ID, DATAELEMENT_OPERAND_ID );    
    final List<String> STATIC_DIMS = Arrays.asList( LONGITUDE_DIM_ID, LATITUDE_DIM_ID );
    
    final Map<String, String> PRETTY_NAMES = DimensionalObjectUtils.asMap( 
        DATA_X_DIM_ID, "Data",
        CATEGORYOPTIONCOMBO_DIM_ID, "Data details",
        PERIOD_DIM_ID, "Period",
        ORGUNIT_DIM_ID, "Organisation unit" );
    
    final Map<DimensionType, Class<? extends DimensionalObject>> DYNAMIC_DIMENSION_TYPE_CLASS_MAP = new HashMap<DimensionType, Class<? extends DimensionalObject>>() { {
        put( DimensionType.CATEGORY, DataElementCategory.class );
        put( DimensionType.DATAELEMENT_GROUPSET, DataElementGroupSet.class );
        put( DimensionType.ORGANISATIONUNIT_GROUPSET, OrganisationUnitGroupSet.class );
        put( DimensionType.CATEGORYOPTION_GROUPSET, CategoryOptionGroupSet.class );
        put( DimensionType.TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttribute.class );
        put( DimensionType.TRACKED_ENTITY_DATAELEMENT, DataElement.class );        
    } };
    
    /**
     * Gets the dimension identifier.
     */
    String getDimension();
    
    /**
     * Gets the dimension type.
     */
    DimensionType getDimensionType();
    
    /**
     * Gets the dimension name, which corresponds to a column in the analytics
     * tables, with fall back to dimension.
     */
    String getDimensionName();
     
    /**
     * Dimension items.
     */
    List<NameableObject> getItems();

    /**
     * Indicates whether this dimension should use all dimension items. All
     * dimension options is represented as an option list of zero elements.
     */
    boolean isAllItems();

    /**
     * Indicates whether this dimension has any dimension items.
     */
    boolean hasItems();
    
    /**
     * Gets the legend set.
     */
    LegendSet getLegendSet();

    /**
     * Indicates whether this dimension has a legend set.
     */
    boolean hasLegendSet();
    
    /**
     * Gets the filter. Contains operator and filter. Applicable for events.
     */
    String getFilter();

    /**
     * Indicates the analytics type of this dimensional object.
     */
    AnalyticsType getAnalyticsType();
    
    /**
     * Indicates whether this object should be handled as a data dimension. 
     * Persistent property.
     */
    boolean isDataDimension();
}
