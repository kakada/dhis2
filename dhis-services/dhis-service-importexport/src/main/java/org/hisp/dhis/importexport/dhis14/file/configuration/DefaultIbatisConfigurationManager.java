package org.hisp.dhis.importexport.dhis14.file.configuration;

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

import java.io.File;
import java.util.Properties;

import org.hisp.dhis.importexport.IbatisConfigurationManager;
import org.hisp.dhis.setting.SystemSettingManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Lars Helge Overland
 * @version $Id: DefaultIbatisConfigurationManager.java 6270 2008-11-13 11:49:21Z larshelg $
 */
public class DefaultIbatisConfigurationManager
    implements IbatisConfigurationManager
{
    private static final String KEY_CONNECTION_URL_DATABASE = "ibatis.connection.url.database";
    private static final String KEY_PASSWORD = "ibatis.connection.password";
    private static final String KEY_USERNAME = "ibatis.connection.username";
    private static final String KEY_LEVELS = "ibatis.levels";
    
    private static final String ACCESS_EXTENSION = ".mdb";

    @Autowired
    private SystemSettingManager systemSettingManager;    
    
    // -------------------------------------------------------------------------
    // IbatisConfigurationManager implementation
    // -------------------------------------------------------------------------

    @Override
    public Properties getPropertiesConfiguration()
    {
        Properties properties = new Properties();
        
        properties.put( KEY_CONNECTION_URL_DATABASE, systemSettingManager.getSystemSetting( KEY_CONNECTION_URL_DATABASE ) );
        properties.put( KEY_USERNAME, systemSettingManager.getSystemSetting( KEY_USERNAME ) );
        properties.put( KEY_PASSWORD, systemSettingManager.getSystemSetting( KEY_PASSWORD ) );
        properties.put( KEY_LEVELS, systemSettingManager.getSystemSetting( KEY_LEVELS ) );
        
        return properties;
    }
    
    @Override
    public void setConfiguration( String connectionUrl, String username, String password, String levels )
    {
        systemSettingManager.saveSystemSetting( KEY_CONNECTION_URL_DATABASE, connectionUrl );
        systemSettingManager.saveSystemSetting( KEY_USERNAME, username );
        systemSettingManager.saveSystemSetting( KEY_PASSWORD, password );
        systemSettingManager.saveSystemSetting( KEY_LEVELS, levels );
    }

    @Override
    public boolean fileIsValid( String path )
    {
        if ( path == null || !path.endsWith( ACCESS_EXTENSION ) )
        {
            return false;
        }
        
        File file = new File( path );
        
        return file.exists();
    }
}
