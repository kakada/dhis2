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

import org.hisp.dhis.configuration.Configuration;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.i18n.I18n;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * 
 * @version $ SetSynchronizationSettingsAction.java Jul 7, 2014 10:04:29 PM $
 */
public class SetHubSettingsAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private ConfigurationService configurationService;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String hubServerUrl;

    public void setHubServerUrl( String hubServerUrl )
    {
        this.hubServerUrl = hubServerUrl;
    }

    private String hubServerUsername;

    public void setHubServerUsername( String hubServerUsername )
    {
        this.hubServerUsername = hubServerUsername;
    }

    private String hubServerPassword;

    public void setHubServerPassword( String hubServerPassword )
    {
        this.hubServerPassword = hubServerPassword;
    }
    
    private String hubInsertTaskUrl;

	public void setHubInsertTaskUrl(String hubInsertTaskUrl) {
		this.hubInsertTaskUrl = hubInsertTaskUrl;
	}
	
	private String hubUpdateTaskUrl;
	
	public void setHubUpdateTaskUrl( String hubUpdateTaskUrl ) {
		this.hubUpdateTaskUrl = hubUpdateTaskUrl;
	}
	
	private boolean hubEnableMode;
	
	public void setHubEnableMode( boolean hubEnableMode ) {
		this.hubEnableMode = hubEnableMode;
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
        Configuration config = configurationService.getConfiguration();
        config.setHubServerUrl( hubServerUrl );
        config.setHubServerUsername( hubServerUsername );
        if( !hubServerPassword.isEmpty() )
        {
            config.setHubServerPassword( hubServerPassword );
        }
        
        config.setHubInsertTaskUrl(hubInsertTaskUrl);
        config.setHubUpdateTaskUrl(hubUpdateTaskUrl);
        config.setHubEnableMode(hubEnableMode);
        
        configurationService.setConfiguration( config );
      
        message = i18n.getString( "settings_updated" );
        
        return SUCCESS;
    }

}
