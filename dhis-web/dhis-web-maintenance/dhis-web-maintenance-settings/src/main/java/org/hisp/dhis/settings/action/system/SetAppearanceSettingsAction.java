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

import static org.hisp.dhis.setting.SystemSettingManager.*;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.setting.StyleManager;

import com.opensymphony.xwork2.Action;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class SetAppearanceSettingsAction
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

    private StyleManager styleManager;

    public void setStyleManager( StyleManager styleManager )
    {
        this.styleManager = styleManager;
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private String applicationTitle;

    public void setApplicationTitle( String applicationTitle )
    {
        this.applicationTitle = applicationTitle;
    }
    
    private String applicationIntro;

    public void setApplicationIntro( String applicationIntro )
    {
        this.applicationIntro = applicationIntro;
    }
    
    private String applicationNotification;

    public void setApplicationNotification( String applicationNotification )
    {
        this.applicationNotification = applicationNotification;
    }
    
    private String applicationFooter;

    public void setApplicationFooter( String applicationFooter )
    {
        this.applicationFooter = applicationFooter;
    }
    
    private String applicationRightFooter;

    public void setApplicationRightFooter( String applicationRightFooter )
    {
        this.applicationRightFooter = applicationRightFooter;
    }

    private String flag;

    public void setFlag( String flag )
    {
        this.flag = flag;
    }

    private String startModule;

    public void setStartModule( String startModule )
    {
        this.startModule = startModule;
    }

    private String currentStyle;

    public void setCurrentStyle( String style )
    {
        this.currentStyle = style;
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
    
    private String localeSelect;

    public void setLocaleSelect( String localeSelect )
    {
        this.localeSelect = localeSelect;
    }
    
    private boolean requireAddToView;
    
    public void setRequireAddToView( boolean requireAddToView )
    {
        this.requireAddToView = requireAddToView;
    }

    private boolean customLoginPageLogo;
    
    public void setCustomLoginPageLogo( boolean customLoginPageLogo )
    {
        this.customLoginPageLogo = customLoginPageLogo;
    }
    
    private boolean customTopMenuLogo;
    
    public void setCustomTopMenuLogo( boolean customTopMenuLogo )
    {
        this.customTopMenuLogo = customTopMenuLogo;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
    {
        applicationIntro = StringUtils.trimToNull( applicationIntro );
        applicationTitle = StringUtils.trimToNull( applicationTitle );
        applicationNotification = StringUtils.trimToNull( applicationNotification );
        applicationFooter = StringUtils.trimToNull( applicationFooter );
        applicationRightFooter = StringUtils.trimToNull( applicationRightFooter );
        flag = StringUtils.trimToNull( flag );
        startModule = StringUtils.trimToNull( startModule );

        if ( flag != null && flag.equals( "NO_FLAG" ) )
        {
            flag = null;
        }

        if ( startModule != null && startModule.equals( "NO_START_PAGE" ) )
        {
            startModule = null;
        }
        
        systemSettingManager.saveSystemSetting( KEY_APPLICATION_TITLE + localeSelect, applicationTitle );
        systemSettingManager.saveSystemSetting( KEY_APPLICATION_INTRO + localeSelect, applicationIntro );
        systemSettingManager.saveSystemSetting( KEY_APPLICATION_NOTIFICATION + localeSelect, applicationNotification );
        systemSettingManager.saveSystemSetting( KEY_APPLICATION_FOOTER + localeSelect, applicationFooter );
        systemSettingManager.saveSystemSetting( KEY_APPLICATION_RIGHT_FOOTER + localeSelect, applicationRightFooter );
        systemSettingManager.saveSystemSetting( KEY_FLAG, flag );
        systemSettingManager.saveSystemSetting( KEY_START_MODULE, startModule );
        systemSettingManager.saveSystemSetting( KEY_REQUIRE_ADD_TO_VIEW, requireAddToView );
        systemSettingManager.saveSystemSetting( KEY_CUSTOM_LOGIN_PAGE_LOGO, customLoginPageLogo );
        systemSettingManager.saveSystemSetting( KEY_CUSTOM_TOP_MENU_LOGO, customTopMenuLogo );
        styleManager.setSystemStyle( currentStyle );

        message = i18n.getString( "settings_updated" );

        return SUCCESS;
    }
}
