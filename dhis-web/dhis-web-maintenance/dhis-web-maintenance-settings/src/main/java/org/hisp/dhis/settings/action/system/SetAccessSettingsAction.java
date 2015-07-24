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

import static org.hisp.dhis.setting.SystemSettingManager.KEY_ACCOUNT_INVITE;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_ACCOUNT_RECOVERY;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_CAN_GRANT_OWN_USER_AUTHORITY_GROUPS;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_CREDENTIALS_EXPIRES;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_OPENID_PROVIDER;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_OPENID_PROVIDER_LABEL;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_SELF_REGISTRATION_NO_RECAPTCHA;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_ALLOW_OBJECT_ASSIGNMENT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.configuration.Configuration;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Lars Helge Overland
 */
public class SetAccessSettingsAction
    implements Action
{
    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private SystemSettingManager systemSettingManager;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private Integer selfRegistrationRole;

    public void setSelfRegistrationRole( Integer selfRegistrationRole )
    {
        this.selfRegistrationRole = selfRegistrationRole;
    }

    private Integer selfRegistrationOrgUnit;

    public void setSelfRegistrationOrgUnit( Integer selfRegistrationOrgUnit )
    {
        this.selfRegistrationOrgUnit = selfRegistrationOrgUnit;
    }

    private Boolean selfRegistrationNoRecaptcha;

    public void setSelfRegistrationNoRecaptcha( Boolean selfRegistrationNoRecaptcha )
    {
        this.selfRegistrationNoRecaptcha = selfRegistrationNoRecaptcha;
    }

    private Boolean accountRecovery;

    public void setAccountRecovery( Boolean accountRecovery )
    {
        this.accountRecovery = accountRecovery;
    }

    private Boolean accountInvite;

    public void setAccountInvite( Boolean accountInvite )
    {
        this.accountInvite = accountInvite;
    }

    private Boolean canGrantOwnUserAuthorityGroups;

    public void setCanGrantOwnUserAuthorityGroups( Boolean canGrantOwnUserAuthorityGroups )
    {
        this.canGrantOwnUserAuthorityGroups = canGrantOwnUserAuthorityGroups;
    }

    private Boolean allowObjectAssignment;

    public void setAllowObjectAssignment( Boolean allowObjectAssignment )
    {
        this.allowObjectAssignment = allowObjectAssignment;
    }

    private Integer credentialsExpires;

    public void setCredentialsExpires( Integer credentialsExpires )
    {
        this.credentialsExpires = credentialsExpires;
    }

    private String openIdProvider;

    public void setOpenIdProvider( String openIdProvider )
    {
        this.openIdProvider = openIdProvider;
    }

    private String openIdProviderLabel;

    public void setOpenIdProviderLabel( String openIdProviderLabel )
    {
        this.openIdProviderLabel = openIdProviderLabel;
    }

    private List<String> corsWhitelist = new ArrayList<>();

    public void setCorsWhitelist( ArrayList<String> corsWhitelist )
    {
        this.corsWhitelist = corsWhitelist;
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

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
        UserAuthorityGroup group = null;
        OrganisationUnit unit = null;

        if ( selfRegistrationRole != null )
        {
            group = userService.getUserAuthorityGroup( selfRegistrationRole );
        }

        if ( selfRegistrationOrgUnit != null )
        {
            unit = organisationUnitService.getOrganisationUnit( selfRegistrationOrgUnit );
        }

        Configuration config = configurationService.getConfiguration();
        config.setSelfRegistrationRole( group );
        config.setSelfRegistrationOrgUnit( unit );
        configurationService.setConfiguration( config );

        systemSettingManager.saveSystemSetting( KEY_ACCOUNT_RECOVERY, accountRecovery );
        systemSettingManager.saveSystemSetting( KEY_ACCOUNT_INVITE, accountInvite );
        systemSettingManager.saveSystemSetting( KEY_CAN_GRANT_OWN_USER_AUTHORITY_GROUPS, canGrantOwnUserAuthorityGroups );
        systemSettingManager.saveSystemSetting( KEY_ALLOW_OBJECT_ASSIGNMENT, allowObjectAssignment );
        systemSettingManager.saveSystemSetting( KEY_SELF_REGISTRATION_NO_RECAPTCHA, selfRegistrationNoRecaptcha );
        systemSettingManager.saveSystemSetting( KEY_OPENID_PROVIDER, StringUtils.isEmpty( openIdProvider ) ? null : openIdProvider );

        if ( !StringUtils.isEmpty( openIdProviderLabel ) )
        {
            systemSettingManager.saveSystemSetting( KEY_OPENID_PROVIDER_LABEL, openIdProviderLabel );
        }

        if ( credentialsExpires != null )
        {
            systemSettingManager.saveSystemSetting( KEY_CREDENTIALS_EXPIRES, credentialsExpires );
        }

        systemSettingManager.saveSystemSetting( SystemSettingManager.KEY_CORS_WHITELIST, (Serializable) corsWhitelist );

        message = i18n.getString( "settings_updated" );

        return SUCCESS;
    }
}
