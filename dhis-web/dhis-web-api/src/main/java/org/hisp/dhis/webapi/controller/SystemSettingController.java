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

import org.hisp.dhis.dxf2.render.RenderService;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.webapi.utils.ContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author Lars Helge Overland
 */
@Controller
@RequestMapping( "/systemSettings" )
public class SystemSettingController
{
    @Autowired
    private SystemSettingManager systemSettingManager;

    @Autowired
    private RenderService renderService;

    @RequestMapping( value = "/{key}", method = RequestMethod.POST, consumes = { ContextUtils.CONTENT_TYPE_TEXT, ContextUtils.CONTENT_TYPE_HTML } )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_SYSTEM_SETTING')" )
    public void setSystemSetting(
        @PathVariable( value = "key" ) String key,
        @RequestParam( required = false, value = "key" ) String keyParam,
        @RequestParam( required = false ) String value,
        @RequestBody( required = false ) String valuePayload, HttpServletResponse response )
    {
        if ( key == null && keyParam == null )
        {
            ContextUtils.conflictResponse( response, "Key must be specified" );
            return;
        }

        if ( value == null && valuePayload == null )
        {
            ContextUtils.conflictResponse( response, "Value must be specified as query param or as payload" );
        }

        value = value != null ? value : valuePayload;

        key = key != null ? key : keyParam;

        systemSettingManager.saveSystemSetting( key, value );

        ContextUtils.okResponse( response, "System setting " + key + " set as value '" + value + "'." );
    }

    @RequestMapping( method = RequestMethod.POST, consumes = { ContextUtils.CONTENT_TYPE_JSON } )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_SYSTEM_SETTING')" )
    public void setSystemSetting( @RequestBody Map<String, Object> settings, HttpServletResponse response )
    {
        for ( String key : settings.keySet() )
        {
            systemSettingManager.saveSystemSetting( key, (Serializable) settings.get( key ) );
        }

        ContextUtils.okResponse( response, "System settings imported" );
    }

    @RequestMapping( value = "/{key}", method = RequestMethod.GET, produces = ContextUtils.CONTENT_TYPE_TEXT )
    public @ResponseBody String getSystemSettingAsText( @PathVariable( "key" ) String key )
    {
        Serializable setting = systemSettingManager.getSystemSetting( key );

        return setting != null ? String.valueOf( setting ) : null;
    }

    @RequestMapping( method = RequestMethod.GET, produces = { ContextUtils.CONTENT_TYPE_JSON, ContextUtils.CONTENT_TYPE_HTML } )
    public void getSystemSettingsJson( @RequestParam( value = "key", required = false ) Set<String> key, HttpServletResponse response ) throws IOException
    {
        response.setContentType( MediaType.APPLICATION_JSON_VALUE );
        renderService.toJson( response.getOutputStream(), getSystemSettings( key ) );
    }

    @RequestMapping( method = RequestMethod.GET, produces = "application/javascript" )
    public void getSystemSettingsJsonP(
        @RequestParam( value = "key", required = false ) Set<String> key,
        @RequestParam( defaultValue = "callback" ) String callback,
        HttpServletResponse response ) throws IOException
    {
        response.setContentType( "application/javascript" );
        renderService.toJsonP( response.getOutputStream(), getSystemSettings( key ), callback );
    }

    private Map<String, Serializable> getSystemSettings( Set<String> key )
    {
        Map<String, Serializable> value;

        if ( key != null && !key.isEmpty() )
        {
            value = systemSettingManager.getSystemSettings( key );
        }
        else
        {
            value = systemSettingManager.getSystemSettingsAsMap();
        }

        return value;
    }

    @RequestMapping( value = "/{key}", method = RequestMethod.DELETE )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_SYSTEM_SETTING')" )
    public void removeSystemSetting( @PathVariable( "key" ) String key )
    {
        systemSettingManager.deleteSystemSetting( key );
    }
}
