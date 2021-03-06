package org.hisp.dhis.webapi.controller.mapping;

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

import org.hisp.dhis.analytics.AggregationType;
import org.hisp.dhis.analytics.AnalyticsService;
import org.hisp.dhis.analytics.DataQueryParams;
import org.hisp.dhis.common.DimensionalObject;
import org.hisp.dhis.common.DisplayProperty;
import org.hisp.dhis.common.NameableObjectUtils;
import org.hisp.dhis.dxf2.render.RenderService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.system.filter.OrganisationUnitWithValidCoordinatesFilter;
import org.hisp.dhis.system.util.FilterUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.webapi.utils.ContextUtils;
import org.hisp.dhis.webapi.webdomain.GeoFeature;
import org.hisp.dhis.webapi.webdomain.WebOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Lars Helge Overland
 */
@Controller
@RequestMapping( value = GeoFeatureController.RESOURCE_PATH )
public class GeoFeatureController
{
    public static final String RESOURCE_PATH = "/geoFeatures";

    private static final Map<String, Integer> FEATURE_TYPE_MAP = new HashMap<String, Integer>()
    {
        {
            put( OrganisationUnit.FEATURETYPE_POINT, GeoFeature.TYPE_POINT );
            put( OrganisationUnit.FEATURETYPE_MULTIPOLYGON, GeoFeature.TYPE_POLYGON );
            put( OrganisationUnit.FEATURETYPE_POLYGON, GeoFeature.TYPE_POLYGON );
            put( null, 0 );
        }
    };

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private OrganisationUnitGroupService organisationUnitGroupService;

    @Autowired
    private RenderService renderService;
    
    @Autowired
    private CurrentUserService currentUserService;

    @RequestMapping( method = RequestMethod.GET, produces = { ContextUtils.CONTENT_TYPE_JSON, ContextUtils.CONTENT_TYPE_HTML } )
    public void getGeoFeaturesJson(
        @RequestParam String ou,
        @RequestParam( required = false ) DisplayProperty displayProperty,
        @RequestParam Map<String, String> parameters,
        HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        WebOptions options = new WebOptions( parameters );
        boolean includeGroupSets = "detailed".equals( options.getViewClass() );

        List<GeoFeature> features = getGeoFeatures( ou, displayProperty, request, response, includeGroupSets );
        if ( features == null ) return;

        response.setContentType( MediaType.APPLICATION_JSON_VALUE );
        renderService.toJson( response.getOutputStream(), features );
    }

    @RequestMapping( method = RequestMethod.GET, produces = { "application/javascript" } )
    public void getGeoFeaturesJsonP(
        @RequestParam String ou,
        @RequestParam( required = false ) DisplayProperty displayProperty,
        @RequestParam( defaultValue = "callback" ) String callback,
        @RequestParam Map<String, String> parameters,
        HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        WebOptions options = new WebOptions( parameters );
        boolean includeGroupSets = "detailed".equals( options.getViewClass() );

        List<GeoFeature> features = getGeoFeatures( ou, displayProperty, request, response, includeGroupSets );
        if ( features == null ) return;

        response.setContentType( "application/javascript" );
        renderService.toJsonP( response.getOutputStream(), features, callback );
    }

    private List<GeoFeature> getGeoFeatures( String ou, DisplayProperty displayProperty, HttpServletRequest request, HttpServletResponse response, boolean includeGroupSets )
    {
        Set<String> set = new HashSet<>();
        set.add( ou );

        DataQueryParams params = analyticsService.getFromUrl( set, null, AggregationType.SUM, null, 
            false, false, false, false, false, false, displayProperty, null, null, null );

        DimensionalObject dim = params.getDimension( DimensionalObject.ORGUNIT_DIM_ID );

        List<OrganisationUnit> organisationUnits = NameableObjectUtils.asTypedList( dim.getItems() );

        FilterUtils.filter( organisationUnits, new OrganisationUnitWithValidCoordinatesFilter() );

        boolean modified = !ContextUtils.clearIfNotModified( request, response, organisationUnits );

        if ( !modified )
        {
            return null;
        }

        Collection<OrganisationUnitGroupSet> groupSets = includeGroupSets ? organisationUnitGroupService.getAllOrganisationUnitGroupSets() : null;

        List<GeoFeature> features = new ArrayList<>();

        Set<OrganisationUnit> roots = currentUserService.getCurrentUser().getDataViewOrganisationUnitsWithFallback();
        
        for ( OrganisationUnit organisationUnit : organisationUnits )
        {
            GeoFeature feature = new GeoFeature();
            feature.setId( organisationUnit.getUid() );
            feature.setCode( organisationUnit.getCode() );
            feature.setHcd( organisationUnit.hasChildrenWithCoordinates() );
            feature.setHcu( organisationUnit.hasCoordinatesUp() );
            feature.setLe( organisationUnit.getLevel() );
            feature.setPg( organisationUnit.getParentGraph( roots ) );
            feature.setPi( organisationUnit.getParent() != null ? organisationUnit.getParent().getUid() : null );
            feature.setPn( organisationUnit.getParent() != null ? organisationUnit.getParent().getDisplayName() : null );
            feature.setTy( FEATURE_TYPE_MAP.get( organisationUnit.getFeatureType() ) );
            feature.setCo( organisationUnit.getCoordinates() );

            if ( DisplayProperty.SHORTNAME.equals( params.getDisplayProperty() ) )
            {
                feature.setNa( organisationUnit.getDisplayShortName() );
            }
            else
            {
                feature.setNa( organisationUnit.getDisplayName() );
            }

            if ( includeGroupSets )
            {
                for ( OrganisationUnitGroupSet groupSet : groupSets )
                {
                    OrganisationUnitGroup group = organisationUnit.getGroupInGroupSet( groupSet );

                    if ( group != null )
                    {
                        feature.getDimensions().put( groupSet.getUid(), group.getName() );
                    }
                }
            }

            features.add( feature );
        }

        Collections.sort( features, GeoFeatureTypeComparator.INSTANCE );
        return features;
    }

    static class GeoFeatureTypeComparator
        implements Comparator<GeoFeature>
    {
        public static final GeoFeatureTypeComparator INSTANCE = new GeoFeatureTypeComparator();

        @Override
        public int compare( GeoFeature o1, GeoFeature o2 )
        {
            return Integer.valueOf( o1.getTy() ).compareTo( Integer.valueOf( o2.getTy() ) );
        }
    }
}
