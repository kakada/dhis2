package org.hisp.dhis.webapi.controller;

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

import static org.hisp.dhis.webapi.utils.ContextUtils.CONTENT_TYPE_JSON;
import static org.hisp.dhis.webapi.utils.ContextUtils.CONTENT_TYPE_XML;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.view.BasicView;
import org.hisp.dhis.datacompletion.CompleteDataSetRegistrationRequest;
import org.hisp.dhis.datacompletion.CompleteDataSetRegistrationRequests;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataset.CompleteDataSetRegistration;
import org.hisp.dhis.dataset.CompleteDataSetRegistrationService;
import org.hisp.dhis.dataset.CompleteDataSetRegistrations;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dxf2.render.RenderService;
import org.hisp.dhis.dxf2.common.JacksonUtils;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.webapi.utils.ContextUtils;
import org.hisp.dhis.webapi.utils.InputUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Controller
@RequestMapping( value = CompleteDataSetRegistrationController.RESOURCE_PATH )
public class CompleteDataSetRegistrationController
{
    public static final String RESOURCE_PATH = "/completeDataSetRegistrations";

    public static final String MULTIPLE_SAVE_RESOURCE_PATH = "/multiple";

    @Autowired
    private CompleteDataSetRegistrationService registrationService;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private IdentifiableObjectManager manager;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private InputUtils inputUtils;

    @Autowired
    private I18nManager i18nManager;

    @Autowired
    private RenderService renderService;

    @RequestMapping( method = RequestMethod.GET, produces = CONTENT_TYPE_XML )
    public void getCompleteDataSetRegistrationsXml(
        @RequestParam Set<String> dataSet,
        @RequestParam( required = false ) String period,
        @RequestParam @DateTimeFormat( pattern = "yyyy-MM-dd" ) Date startDate,
        @RequestParam @DateTimeFormat( pattern = "yyyy-MM-dd" ) Date endDate,
        @RequestParam Set<String> orgUnit,
        @RequestParam( required = false ) boolean children,
        HttpServletResponse response ) throws IOException
    {
        response.setContentType( CONTENT_TYPE_XML );
        CompleteDataSetRegistrations completeDataSetRegistrations = getCompleteDataSetRegistrations( dataSet, period,
            startDate, endDate, orgUnit, children );

        JacksonUtils.toXmlWithView( response.getOutputStream(), completeDataSetRegistrations, BasicView.class );
    }

    @RequestMapping( method = RequestMethod.GET, produces = CONTENT_TYPE_JSON )
    public void getCompleteDataSetRegistrationsJson(
        @RequestParam Set<String> dataSet,
        @RequestParam( required = false ) String period,
        @RequestParam @DateTimeFormat( pattern = "yyyy-MM-dd" ) Date startDate,
        @RequestParam @DateTimeFormat( pattern = "yyyy-MM-dd" ) Date endDate,
        @RequestParam Set<String> orgUnit,
        @RequestParam( required = false ) boolean children,
        HttpServletResponse response ) throws IOException
    {
        response.setContentType( CONTENT_TYPE_JSON );
        CompleteDataSetRegistrations completeDataSetRegistrations = getCompleteDataSetRegistrations( dataSet, period,
            startDate, endDate, orgUnit, children );

        JacksonUtils.toJsonWithView( response.getOutputStream(), completeDataSetRegistrations, BasicView.class );
    }

    private CompleteDataSetRegistrations getCompleteDataSetRegistrations( Set<String> dataSet, String period,
        Date startDate, Date endDate, Set<String> orgUnit, boolean children )
    {
        Set<Period> periods = new HashSet<>();
        Set<DataSet> dataSets = new HashSet<>();
        Set<OrganisationUnit> organisationUnits = new HashSet<>();

        PeriodType periodType = periodService.getPeriodTypeByName( period );

        if ( periodType != null )
        {
            periods.addAll( periodService.getPeriodsBetweenDates( periodType, startDate, endDate ) );
        }
        else
        {
            periods.addAll( periodService.getPeriodsBetweenDates( startDate, endDate ) );
        }

        if ( periods.isEmpty() )
        {
            return new CompleteDataSetRegistrations();
        }

        if ( children )
        {
            organisationUnits.addAll( organisationUnitService.getOrganisationUnitsWithChildren( orgUnit ) );
        }
        else
        {
            organisationUnits.addAll( organisationUnitService.getOrganisationUnitsByUid( orgUnit ) );
        }

        dataSets.addAll( manager.getByUid( DataSet.class, dataSet ) );

        CompleteDataSetRegistrations completeDataSetRegistrations = new CompleteDataSetRegistrations();
        completeDataSetRegistrations.setCompleteDataSetRegistrations( new ArrayList<>(
            registrationService.getCompleteDataSetRegistrations( dataSets, organisationUnits, periods ) ) );

        return completeDataSetRegistrations;
    }

    @RequestMapping( method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE )
    public void saveCompleteDataSetRegistrationsJson( HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        CompleteDataSetRegistrations completeDataSetRegistrations = renderService.fromJson( request.getInputStream(),
            CompleteDataSetRegistrations.class );

        saveCompleteDataSetRegistrations( completeDataSetRegistrations );
    }

    @RequestMapping( method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE )
    public void saveCompleteDataSetRegistrationsXml( HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        CompleteDataSetRegistrations completeDataSetRegistrations = renderService.fromXml( request.getInputStream(),
            CompleteDataSetRegistrations.class );

        saveCompleteDataSetRegistrations( completeDataSetRegistrations );
    }

    private void saveCompleteDataSetRegistrations( CompleteDataSetRegistrations completeDataSetRegistrations )
    {
        for ( CompleteDataSetRegistration completeDataSetRegistration : completeDataSetRegistrations.getCompleteDataSetRegistrations() )
        {
            Period period = getObject( completeDataSetRegistration.getPeriod() );
            DataSet dataSet = getObject( completeDataSetRegistration.getDataSet() );
            DataElementCategoryOptionCombo attributeOptionCombo = getObject( completeDataSetRegistration.getAttributeOptionCombo() );
            OrganisationUnit organisationUnit = getObject( completeDataSetRegistration.getSource() );

            Date date = completeDataSetRegistration.getDate();
            String storedBy = completeDataSetRegistration.getStoredBy();

            registerCompleteDataSet( dataSet, period, organisationUnit, attributeOptionCombo, storedBy, date );
        }
    }

    @SuppressWarnings( "unchecked" )
    private <T extends IdentifiableObject> T getObject( T object )
    {
        if ( object == null )
        {
            return null;
        }

        if ( Period.class.isInstance( object ) )
        {
            return (T) periodService.reloadIsoPeriod( ((Period) object).getRealUid() );
        }

        if ( object.getUid() != null )
        {
            return (T) manager.get( object.getClass(), object.getUid() );
        }

        if ( object.getCode() != null )
        {
            return (T) manager.get( object.getClass(), object.getCode() );
        }

        return null;
    }

    @RequestMapping( method = RequestMethod.POST, produces = "text/plain" )
    public void saveCompleteDataSetRegistration(
        @RequestParam String ds,
        @RequestParam String pe,
        @RequestParam String ou,
        @RequestParam( required = false ) String cc,
        @RequestParam( required = false ) String cp,
        @RequestParam( required = false ) Date cd,
        @RequestParam( required = false ) String sb,
        @RequestParam( required = false ) boolean multiOu, HttpServletResponse response )
    {
        DataSet dataSet = dataSetService.getDataSet( ds );

        if ( dataSet == null )
        {
            ContextUtils.conflictResponse( response, "Illegal data set identifier: " + ds );
            return;
        }

        Period period = PeriodType.getPeriodFromIsoString( pe );

        if ( period == null )
        {
            ContextUtils.conflictResponse( response, "Illegal period identifier: " + pe );
            return;
        }

        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( ou );

        if ( organisationUnit == null )
        {
            ContextUtils.conflictResponse( response, "Illegal organisation unit identifier: " + ou );
            return;
        }

        DataElementCategoryOptionCombo attributeOptionCombo = inputUtils.getAttributeOptionCombo( response, cc, cp );

        if ( attributeOptionCombo == null )
        {
            return;
        }

        // ---------------------------------------------------------------------
        // Check locked status
        // ---------------------------------------------------------------------

        if ( dataSetService.isLocked( dataSet, period, organisationUnit, attributeOptionCombo, null, multiOu ) )
        {
            ContextUtils.conflictResponse( response, "Data set is locked: " + ds );
            return;
        }

        // ---------------------------------------------------------------------
        // Register as completed data set
        // ---------------------------------------------------------------------

        Set<OrganisationUnit> children = organisationUnit.getChildren();

        String storedBy = (sb == null) ? currentUserService.getCurrentUsername() : sb;

        Date completionDate = (cd == null) ? new Date() : cd;

        List<CompleteDataSetRegistration> registrations = new ArrayList<>();

        if ( !multiOu )
        {
            CompleteDataSetRegistration completeDataSetRegistration = registerCompleteDataSet( dataSet, period,
                organisationUnit, attributeOptionCombo, storedBy, completionDate );

            if ( completeDataSetRegistration != null )
                registrations.add( completeDataSetRegistration );
        }
        else
        {
            for ( OrganisationUnit unit : children )
            {
                if ( unit.getDataSets().contains( dataSet ) )
                {
                    CompleteDataSetRegistration completeDataSetRegistration = registerCompleteDataSet( dataSet, period,
                        organisationUnit, attributeOptionCombo, storedBy, completionDate );

                    if ( completeDataSetRegistration != null )
                        registrations.add( completeDataSetRegistration );
                }
            }
        }

        registrationService.saveCompleteDataSetRegistrations( registrations, true );
    }

    @RequestMapping( method = RequestMethod.POST, consumes = "application/json", value = MULTIPLE_SAVE_RESOURCE_PATH )
    public void saveCompleteDataSetRegistration(
        @RequestBody CompleteDataSetRegistrationRequests completeDataSetRegistrationRequests,
        HttpServletResponse response )
    {
        List<CompleteDataSetRegistration> registrations = new ArrayList<>();

        for ( CompleteDataSetRegistrationRequest completeDataSetRegistrationRequest : completeDataSetRegistrationRequests )
        {
            String ds = completeDataSetRegistrationRequest.getDs();
            DataSet dataSet = dataSetService.getDataSet( ds );

            if ( dataSet == null )
            {
                ContextUtils.conflictResponse( response, "Illegal data set identifier: " + ds );
                return;
            }

            String pe = completeDataSetRegistrationRequest.getPe();
            Period period = PeriodType.getPeriodFromIsoString( pe );

            if ( period == null )
            {
                ContextUtils.conflictResponse( response, "Illegal period identifier: " + pe );
                return;
            }

            String ou = completeDataSetRegistrationRequest.getOu();
            OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( ou );

            if ( organisationUnit == null )
            {
                ContextUtils.conflictResponse( response, "Illegal organisation unit identifier: " + ou );
                return;
            }

            String cc = completeDataSetRegistrationRequest.getCc();
            String cp = completeDataSetRegistrationRequest.getCp();
            DataElementCategoryOptionCombo attributeOptionCombo = inputUtils.getAttributeOptionCombo( response, cc, cp );

            if ( attributeOptionCombo == null )
            {
                return;
            }

            // ---------------------------------------------------------------------
            // Check locked status
            // ---------------------------------------------------------------------

            boolean multiOu = completeDataSetRegistrationRequest.isMultiOu();

            if ( dataSetService.isLocked( dataSet, period, organisationUnit, attributeOptionCombo, null, multiOu ) )
            {
                ContextUtils.conflictResponse( response, "Data set is locked: " + ds );
                return;
            }

            // ---------------------------------------------------------------------
            // Register as completed data set
            // ---------------------------------------------------------------------

            String sb = completeDataSetRegistrationRequest.getSb();

            String storedBy = (sb == null) ? currentUserService.getCurrentUsername() : sb;

            Date cd = completeDataSetRegistrationRequest.getCd();

            Date completionDate = (cd == null) ? new Date() : cd;

            Set<OrganisationUnit> orgUnits = new HashSet<>();

            orgUnits.add( organisationUnit );

            if ( multiOu )
            {
                orgUnits.addAll( organisationUnit.getChildren() );
            }

            for ( OrganisationUnit orgUnit : orgUnits )
            {
                if ( orgUnit.getDataSets().contains( dataSet ) )
                {
                    CompleteDataSetRegistration completeDataSetRegistration = registerCompleteDataSet( dataSet, period,
                        orgUnit, attributeOptionCombo, storedBy, completionDate );

                    if ( completeDataSetRegistration != null )
                        registrations.add( completeDataSetRegistration );
                }
            }
        }

        registrationService.saveCompleteDataSetRegistrations( registrations, true );
    }

    @RequestMapping( method = RequestMethod.DELETE )
    public void deleteCompleteDataSetRegistration(
        @RequestParam Set<String> ds,
        @RequestParam String pe,
        @RequestParam String ou,
        @RequestParam( required = false ) String cc,
        @RequestParam( required = false ) String cp,
        @RequestParam( required = false ) boolean multiOu, HttpServletResponse response )
    {
        Set<DataSet> dataSets = new HashSet<>( manager.getByUid( DataSet.class, ds ) );

        if ( dataSets.size() != ds.size() )
        {
            ContextUtils.conflictResponse( response, "Illegal data set identifier in this list: " + ds );
            return;
        }

        Period period = PeriodType.getPeriodFromIsoString( pe );

        if ( period == null )
        {
            ContextUtils.conflictResponse( response, "Illegal period identifier: " + pe );
            return;
        }

        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( ou );

        if ( organisationUnit == null )
        {
            ContextUtils.conflictResponse( response, "Illegal organisation unit identifier: " + ou );
            return;
        }

        DataElementCategoryOptionCombo attributeOptionCombo = inputUtils.getAttributeOptionCombo( response, cc, cp );

        if ( attributeOptionCombo == null )
        {
            return;
        }

        // ---------------------------------------------------------------------
        // Check locked status
        // ---------------------------------------------------------------------

        List<String> lockedDataSets = new ArrayList<>();
        for ( DataSet dataSet : dataSets )
        {
            if ( dataSetService.isLocked( dataSet, period, organisationUnit, attributeOptionCombo, null, multiOu ) )
            {
                lockedDataSets.add( dataSet.getUid() );
            }
        }

        if ( lockedDataSets.size() != 0 )
        {
            ContextUtils.conflictResponse( response, "Locked Data set(s) : " + StringUtils.join( lockedDataSets, ", " ) );
            return;
        }

        // ---------------------------------------------------------------------
        // Un-register as completed data set
        // ---------------------------------------------------------------------

        Set<OrganisationUnit> orgUnits = new HashSet<>();
        orgUnits.add( organisationUnit );

        if ( multiOu )
        {
            orgUnits.addAll( organisationUnit.getChildren() );
        }

        unRegisterCompleteDataSet( dataSets, period, orgUnits, attributeOptionCombo );
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private CompleteDataSetRegistration registerCompleteDataSet( DataSet dataSet, Period period,
        OrganisationUnit orgUnit, DataElementCategoryOptionCombo attributeOptionCombo, String storedBy, Date completionDate )
    {
        I18nFormat format = i18nManager.getI18nFormat();

        if ( dataSet == null )
        {
            throw new HttpClientErrorException( HttpStatus.BAD_REQUEST, "DataSet can not be null." );
        }

        if ( period == null )
        {
            throw new HttpClientErrorException( HttpStatus.BAD_REQUEST, "period can not be null." );
        }

        if ( orgUnit == null )
        {
            throw new HttpClientErrorException( HttpStatus.BAD_REQUEST, "organisationUnit can not be null" );
        }

        if ( attributeOptionCombo == null )
        {
            throw new HttpClientErrorException( HttpStatus.BAD_REQUEST, "attributeOptionCombo can not be null" );
        }

        CompleteDataSetRegistration registration = registrationService.getCompleteDataSetRegistration( dataSet, period,
            orgUnit, attributeOptionCombo );

        if ( registration == null )
        {
            registration = new CompleteDataSetRegistration();

            registration.setDataSet( dataSet );
            registration.setPeriod( period );
            registration.setSource( orgUnit );
            registration.setAttributeOptionCombo( attributeOptionCombo );

            registration.setDate( completionDate != null ? completionDate : new Date() );
            registration.setStoredBy( storedBy != null ? storedBy : currentUserService.getCurrentUsername() );
            registration.setPeriodName( format.formatPeriod( registration.getPeriod() ) );

            registrationService.saveCompleteDataSetRegistration( registration );
        }
        else
        {
            registration.setDate( completionDate != null ? completionDate : new Date() );
            registration.setStoredBy( storedBy != null ? storedBy : currentUserService.getCurrentUsername() );
            registration.setPeriodName( format.formatPeriod( registration.getPeriod() ) );

            registrationService.updateCompleteDataSetRegistration( registration );
        }


        return registration;
    }

    private void unRegisterCompleteDataSet( Set<DataSet> dataSets, Period period,
        Set<OrganisationUnit> orgUnits, DataElementCategoryOptionCombo attributeOptionCombo )
    {
        List<CompleteDataSetRegistration> registrations = new ArrayList<>();

        for ( OrganisationUnit unit : orgUnits )
        {
            for ( DataSet dataSet : dataSets )
            {
                if ( unit.getDataSets().contains( dataSet ) )
                {
                    CompleteDataSetRegistration registration = registrationService
                        .getCompleteDataSetRegistration( dataSet, period, unit, attributeOptionCombo );

                    if ( registration != null )
                    {
                        registrations.add( registration );
                    }
                }
            }
        }
        if ( !registrations.isEmpty() )
        {
            registrationService.deleteCompleteDataSetRegistrations( registrations );
        }
    }
}
