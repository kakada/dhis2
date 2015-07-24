package org.hisp.dhis.setting;

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

import org.hisp.dhis.calendar.CalendarService;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.period.YearlyPeriodType;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Stian Strandli
 */
public interface SystemSettingManager
{
    final String ID = SystemSettingManager.class.getName();

    final String KEY_APPLICATION_TITLE = "applicationTitle";
    final String KEY_APPLICATION_INTRO = "keyApplicationIntro";
    final String KEY_APPLICATION_NOTIFICATION = "keyApplicationNotification";
    final String KEY_APPLICATION_FOOTER = "keyApplicationFooter";
    final String KEY_APPLICATION_RIGHT_FOOTER = "keyApplicationRightFooter";
    final String KEY_FLAG = "keyFlag";
    final String KEY_FLAG_IMAGE = "keyFlagImage";
    final String KEY_START_MODULE = "startModule";
    final String KEY_FORUM_INTEGRATION = "forumIntegration";
    final String KEY_OMIT_INDICATORS_ZERO_NUMERATOR_DATAMART = "omitIndicatorsZeroNumeratorDataMart";
    final String KEY_REPORT_TEMPLATE_DIRECTORY = "reportTemplateDirectory";
    final String KEY_GOOGLE_MAPS_API_KEY = "googleMapsAPIKey";
    final String KEY_FACTOR_OF_DEVIATION = "factorDeviation";
    final String KEY_TRACKED_ENTITY_EXCEL_TEMPLATE_FILE_NAME = "trackedEntityExcelTemplateFileName";
    final String KEY_DATAMART_TASK = "keyDataMartTask";
    final String KEY_DATASETCOMPLETENESS_TASK = "keyDataSetCompletenessTask";
    final String KEY_EMAIL_HOST_NAME = "keyEmailHostName";
    final String KEY_EMAIL_PORT = "keyEmailPort";
    final String KEY_EMAIL_USERNAME = "keyEmailUsername";
    final String KEY_EMAIL_PASSWORD = "keyEmailPassword";
    final String KEY_EMAIL_TLS = "keyEmailTls";
    final String KEY_EMAIL_SENDER = "keyEmailSender";
    final String KEY_INSTANCE_BASE_URL = "keyInstanceBaseUrl";
    final String KEY_SCHEDULED_PERIOD_TYPES = "keyScheduledPeriodTypes";
    final String KEY_SCHEDULED_TASKS = "keySchedTasks";
    final String KEY_ORGUNITGROUPSET_AGG_LEVEL = "orgUnitGroupSetAggregationLevel";
    final String KEY_SMS_CONFIG = "SMS_CONFIG";
    final String KEY_CACHE_STRATEGY = "keyCacheStrategy";
    final String KEY_TIME_FOR_SENDING_MESSAGE = "timeSendingMessage";
    final String KEY_SEND_MESSAGE_SCHEDULED_TASKS = "sendMessageScheduled";
    final String KEY_SCHEDULE_MESSAGE_TASKS = "scheduleMessage";
    final String KEY_PHONE_NUMBER_AREA_CODE = "phoneNumberAreaCode";
    final String KEY_MULTI_ORGANISATION_UNIT_FORMS = "multiOrganisationUnitForms";
    final String KEY_SCHEDULE_AGGREGATE_QUERY_BUILDER_TASKS = "scheduleAggregateQueryBuilder";
    final String KEY_SCHEDULE_AGGREGATE_QUERY_BUILDER_TASK_STRATEGY = "scheduleAggregateQueryBuilderTackStrategy";
    final String KEY_CONFIGURATION = "keyConfig";
    final String KEY_ACCOUNT_RECOVERY = "keyAccountRecovery";
    final String KEY_ACCOUNT_INVITE = "keyAccountInvite";
    final String KEY_LAST_MONITORING_RUN = "keyLastMonitoringRun";
    final String KEY_GOOGLE_ANALYTICS_UA = "googleAnalyticsUA";
    final String KEY_CREDENTIALS_EXPIRES = "credentialsExpires";
    final String KEY_SELF_REGISTRATION_NO_RECAPTCHA = "keySelfRegistrationNoRecaptcha";
    final String KEY_OPENID_PROVIDER = "keyOpenIdProvider";
    final String KEY_OPENID_PROVIDER_LABEL = "keyOpenIdProviderLabel";
    final String KEY_CAN_GRANT_OWN_USER_AUTHORITY_GROUPS = "keyCanGrantOwnUserAuthorityGroups";
    final String KEY_HIDE_UNAPPROVED_DATA_IN_ANALYTICS = "keyHideUnapprovedDataInAnalytics";
    final String KEY_ANALYTICS_MAX_LIMIT = "keyAnalyticsMaxLimit";
    final String KEY_CUSTOM_LOGIN_PAGE_LOGO = "keyCustomLoginPageLogo";
    final String KEY_CUSTOM_TOP_MENU_LOGO = "keyCustomTopMenuLogo";
    final String KEY_ANALYTICS_MAINTENANCE_MODE = "keyAnalyticsMaintenanceMode";
    final String KEY_DATABASE_SERVER_CPUS = "keyDatabaseServerCpus";
    final String KEY_LAST_SUCCESSFUL_DATA_SYNC = "keyLastSuccessfulDataSynch";
    final String KEY_LAST_SUCCESSFUL_ANALYTICS_TABLES_UPDATE = "keyLastSuccessfulAnalyticsTablesUpdate";
    final String KEY_LAST_SUCCESSFUL_RESOURCE_TABLES_UPDATE = "keyLastSuccessfulResourceTablesUpdate";
    final String KEY_HELP_PAGE_LINK = "helpPageLink";
    final String KEY_ACCEPTANCE_REQUIRED_FOR_APPROVAL = "keyAcceptanceRequiredForApproval";
    final String KEY_SYSTEM_NOTIFICATIONS_EMAIL = "keySystemNotificationsEmail";
    final String KEY_ANALYSIS_RELATIVE_PERIOD = "keyAnalysisRelativePeriod";
    final String KEY_CORS_WHITELIST = "keyCorsWhitelist";
    final String KEY_REQUIRE_ADD_TO_VIEW = "keyRequireAddToView";
    final String KEY_ALLOW_OBJECT_ASSIGNMENT = "keyAllowObjectAssignment";

    final String DEFAULT_SCHEDULE_AGGREGATE_QUERY_BUILDER_TASK_STRATEGY = "lastMonth";
    final String DEFAULT_FLAG = "dhis2";
    final int DEFAULT_MAX_NUMBER_OF_ATTEMPTS = 20;
    final int DEFAULT_TIMEFRAME_MINUTES = 1;
    final double DEFAULT_FACTOR_OF_DEVIATION = 2.0;
    final int DEFAULT_ORGUNITGROUPSET_AGG_LEVEL = 3;
    final String DEFAULT_GOOGLE_MAPS_API_KEY = "ABQIAAAAut6AhySExnYIXm5s2OFIkxRKNzJ-_9njnryRTbvC6CtrS4sRvRREWnxwlZUa630pLuPf3nD9i4fq9w";
    final String DEFAULT_START_MODULE = "dhis-web-dashboard-integration";
    final String DEFAULT_APPLICATION_TITLE = "District Health Information Software 2";
    final int DEFAULT_EMAIL_PORT = 587;
    final int DEFAULT_COMPLETENESS_OFFSET = 15;
    final String DEFAULT_TIME_FOR_SENDING_MESSAGE = "08:00";
    final String DEFAULT_CACHE_STRATEGY = "CACHE_6AM_TOMORROW";
    final int DEFAULT_ANALYTICS_MAX_LIMIT = 50000;
    final int DEFAULT_DATABASE_SERVER_CPUS = 0; // Detect automatically
    final String DEFAULT_HELP_PAGE_LINK = "../dhis-web-commons-about/help.action";
    final String DEFAULT_ANALYSIS_RELATIVE_PERIOD = "LAST_12_MONTHS";
	
    final Map<String, Serializable> DEFAULT_SETTINGS_VALUES = new HashMap<String, Serializable>()
    {
        {
            put( KEY_SCHEDULE_AGGREGATE_QUERY_BUILDER_TASK_STRATEGY, DEFAULT_SCHEDULE_AGGREGATE_QUERY_BUILDER_TASK_STRATEGY );
            put( KEY_FLAG, DEFAULT_FLAG );
            put( CalendarService.KEY_CALENDAR, CalendarService.DEFAULT_CALENDAR );
            put( CalendarService.KEY_DATE_FORMAT, CalendarService.DEFAULT_DATE_FORMAT );
            put( KEY_FACTOR_OF_DEVIATION, DEFAULT_FACTOR_OF_DEVIATION );
            put( KEY_ORGUNITGROUPSET_AGG_LEVEL, DEFAULT_ORGUNITGROUPSET_AGG_LEVEL );
            put( KEY_START_MODULE, DEFAULT_START_MODULE );
            put( KEY_APPLICATION_TITLE, DEFAULT_APPLICATION_TITLE );
            put( KEY_EMAIL_PORT, DEFAULT_EMAIL_PORT );
            put( KEY_TIME_FOR_SENDING_MESSAGE, DEFAULT_TIME_FOR_SENDING_MESSAGE );
            put( KEY_CACHE_STRATEGY, DEFAULT_CACHE_STRATEGY );
            put( KEY_ANALYTICS_MAX_LIMIT, DEFAULT_ANALYTICS_MAX_LIMIT );
            put( KEY_ANALYSIS_RELATIVE_PERIOD, DEFAULT_ANALYSIS_RELATIVE_PERIOD );
        }
    };

    final String SYSPROP_PORTAL = "runningAsPortal";

    final HashSet<String> DEFAULT_SCHEDULED_PERIOD_TYPES = new HashSet<String>()
    {
        {
            add( MonthlyPeriodType.NAME );
            add( QuarterlyPeriodType.NAME );
            add( YearlyPeriodType.NAME );
        }
    };

    void saveSystemSetting( String name, Serializable value );

    Serializable getSystemSetting( String name );

    Serializable getSystemSetting( String name, Serializable defaultValue );

    Collection<SystemSetting> getAllSystemSettings();

    void deleteSystemSetting( String name );

    List<String> getFlags();

    String getFlagImage();

    String getEmailHostName();

    int getEmailPort();

    String getEmailUsername();

    boolean getEmailTls();
    
    String getEmailSender();

    String getInstanceBaseUrl();

    boolean accountRecoveryEnabled();

    boolean accountInviteEnabled();

    boolean selfRegistrationNoRecaptcha();

    boolean emailEnabled();
    
    boolean systemNotificationEmailValid();

    boolean hideUnapprovedDataInAnalytics();
    
    String googleAnalyticsUA();

    Integer credentialsExpires();

    List<String> getCorsWhitelist();

    Map<String, Serializable> getSystemSettingsAsMap();
    
    Map<String, Serializable> getSystemSettings( Set<String> names );
}
