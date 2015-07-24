package org.hisp.dhis.settings.action.system;

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

import static org.hisp.dhis.setting.SystemSettingManager.KEY_ANALYTICS_MAINTENANCE_MODE;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_ANALYTICS_MAX_LIMIT;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_CACHE_STRATEGY;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_DATABASE_SERVER_CPUS;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_FACTOR_OF_DEVIATION;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_GOOGLE_ANALYTICS_UA;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_HELP_PAGE_LINK;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_INSTANCE_BASE_URL;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_MULTI_ORGANISATION_UNIT_FORMS;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_OMIT_INDICATORS_ZERO_NUMERATOR_DATAMART;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_PHONE_NUMBER_AREA_CODE;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_SYSTEM_NOTIFICATIONS_EMAIL;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_ANALYSIS_RELATIVE_PERIOD;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.configuration.Configuration;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.user.UserGroupService;

import com.opensymphony.xwork2.Action;

/**
 * @author Lars Helge Overland
 */
public class SetGeneralSettingsAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SystemSettingManager systemSettingManager;

    public void setSystemSettingManager( SystemSettingManager systemSettingManager )
    {
        this.systemSettingManager = systemSettingManager;
    }

    private UserGroupService userGroupService;

    public void setUserGroupService( UserGroupService userGroupService )
    {
        this.userGroupService = userGroupService;
    }

    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }
    
    private IndicatorService indicatorService;
    
    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private String cacheStrategy;

    public void setCacheStrategy( String cacheStrategy )
    {
        this.cacheStrategy = cacheStrategy;
    }
    
    private Integer analyticsMaxLimit;
    
    public void setAnalyticsMaxLimit( Integer analyticsMaxLimit )
    {
        this.analyticsMaxLimit = analyticsMaxLimit;
    }
    
    private Integer databaseServerCpus;
    
    public void setDatabaseServerCpus( Integer databaseServerCpus )
    {
        this.databaseServerCpus = databaseServerCpus;
    }

    private Integer infrastructuralIndicators;

    public void setInfrastructuralIndicators( Integer infrastructuralIndicators )
    {
        this.infrastructuralIndicators = infrastructuralIndicators;
    }

    private Integer infrastructuralDataElements;

    public void setInfrastructuralDataElements( Integer infrastructuralDataElements )
    {
        this.infrastructuralDataElements = infrastructuralDataElements;
    }

    private String infrastructuralPeriodType;

    public void setInfrastructuralPeriodType( String infrastructuralPeriodType )
    {
        this.infrastructuralPeriodType = infrastructuralPeriodType;
    }
    
    private String analysisRelativePeriod;
    
    public void setAnalysisRelativePeriod( String analysisRelativePeriod )
    {
        this.analysisRelativePeriod = analysisRelativePeriod;
    }

    private Boolean omitIndicatorsZeroNumeratorDataMart;

    public void setOmitIndicatorsZeroNumeratorDataMart( Boolean omitIndicatorsZeroNumeratorDataMart )
    {
        this.omitIndicatorsZeroNumeratorDataMart = omitIndicatorsZeroNumeratorDataMart;
    }

    private Double factorDeviation;

    public void setFactorDeviation( Double factorDeviation )
    {
        this.factorDeviation = factorDeviation;
    }

    private Integer feedbackRecipients;

    public void setFeedbackRecipients( Integer feedbackRecipients )
    {
        this.feedbackRecipients = feedbackRecipients;
    }

    private Integer offlineOrganisationUnitLevel;

    public void setOfflineOrganisationUnitLevel( Integer offlineOrganisationUnitLevel )
    {
        this.offlineOrganisationUnitLevel = offlineOrganisationUnitLevel;
    }
    
    private String systemNotificationsEmail;

    public void setSystemNotificationsEmail( String systemNotificationsEmail )
    {
        this.systemNotificationsEmail = systemNotificationsEmail;
    }

    private String phoneNumberAreaCode;

    public void setPhoneNumberAreaCode( String phoneNumberAreaCode )
    {
        this.phoneNumberAreaCode = phoneNumberAreaCode;
    }

    private String googleAnalyticsUA;

    public void setGoogleAnalyticsUA( String googleAnalyticsUA )
    {
        this.googleAnalyticsUA = googleAnalyticsUA;
    }

    private boolean multiOrganisationUnitForms;

    public void setMultiOrganisationUnitForms( boolean multiOrganisationUnitForms )
    {
        this.multiOrganisationUnitForms = multiOrganisationUnitForms;
    }

    private boolean analyticsMaintenanceMode;
    
    public void setAnalyticsMaintenanceMode( boolean analyticsMaintenanceMode )
    {
        this.analyticsMaintenanceMode = analyticsMaintenanceMode;
    }
    
    private String helpPageLink;
    
    public void setHelpPageLink( String helpPageLink )
    {
        this.helpPageLink = helpPageLink;
    }

    private String instanceBaseUrl;

    public void setInstanceBaseUrl( String instanceBaseUrl )
    {
        this.instanceBaseUrl = instanceBaseUrl;
    }

    private String message;

    public String getMessage()
    {
        return message;
    }

    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
    {
        systemSettingManager.saveSystemSetting( KEY_CACHE_STRATEGY, cacheStrategy );
        systemSettingManager.saveSystemSetting( KEY_ANALYTICS_MAX_LIMIT, analyticsMaxLimit );
        systemSettingManager.saveSystemSetting( KEY_DATABASE_SERVER_CPUS, databaseServerCpus );
        systemSettingManager.saveSystemSetting( KEY_OMIT_INDICATORS_ZERO_NUMERATOR_DATAMART, omitIndicatorsZeroNumeratorDataMart );
        systemSettingManager.saveSystemSetting( KEY_FACTOR_OF_DEVIATION, factorDeviation );
        systemSettingManager.saveSystemSetting( KEY_PHONE_NUMBER_AREA_CODE, phoneNumberAreaCode );
        systemSettingManager.saveSystemSetting( KEY_MULTI_ORGANISATION_UNIT_FORMS, multiOrganisationUnitForms );
        systemSettingManager.saveSystemSetting( KEY_GOOGLE_ANALYTICS_UA, googleAnalyticsUA );
        systemSettingManager.saveSystemSetting( KEY_ANALYTICS_MAINTENANCE_MODE, analyticsMaintenanceMode );
        systemSettingManager.saveSystemSetting( KEY_HELP_PAGE_LINK, StringUtils.trimToNull( helpPageLink ) );
        systemSettingManager.saveSystemSetting( KEY_INSTANCE_BASE_URL, StringUtils.removeEnd( StringUtils.trimToNull( instanceBaseUrl ), "/" ) );
        systemSettingManager.saveSystemSetting( KEY_SYSTEM_NOTIFICATIONS_EMAIL, systemNotificationsEmail );
        systemSettingManager.saveSystemSetting( KEY_ANALYSIS_RELATIVE_PERIOD, analysisRelativePeriod );

        Configuration configuration = configurationService.getConfiguration();

        if ( feedbackRecipients != null )
        {
            configuration.setFeedbackRecipients( userGroupService.getUserGroup( feedbackRecipients ) );
        }

        if ( offlineOrganisationUnitLevel != null )
        {
            configuration.setOfflineOrganisationUnitLevel( 
                organisationUnitService.getOrganisationUnitLevel( offlineOrganisationUnitLevel ) );

            organisationUnitService.updateVersion();
        }

        if ( infrastructuralIndicators != null )
        {
            configuration.setInfrastructuralIndicators( indicatorService.getIndicatorGroup( infrastructuralIndicators ) );
        }

        if ( infrastructuralDataElements != null )
        {
            configuration.setInfrastructuralDataElements( dataElementService
                .getDataElementGroup( infrastructuralDataElements ) );
        }

        if ( infrastructuralPeriodType != null )
        {
            configuration.setInfrastructuralPeriodType( periodService.getPeriodTypeByClass( PeriodType
                .getPeriodTypeByName( infrastructuralPeriodType ).getClass() ) );
        }
        
        configurationService.setConfiguration( configuration );

        message = i18n.getString( "settings_updated" );

        return SUCCESS;
    }
}
