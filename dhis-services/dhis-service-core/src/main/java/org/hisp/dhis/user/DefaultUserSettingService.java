package org.hisp.dhis.user;

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

import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Torgeir Lorange Ostby
 * @version $Id: DefaultUserSettingService.java 5724 2008-09-18 14:37:01Z larshelg $
 */
@Transactional
public class DefaultUserSettingService
    implements UserSettingService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private UserSettingStore userSettingStore;

    public void setUserSettingStore( UserSettingStore userSettingStore )
    {
        this.userSettingStore = userSettingStore;
    }

    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // UserSettingService implementation
    // -------------------------------------------------------------------------

    @Override
    public void addUserSetting( UserSetting userSetting )
    {
        userSettingStore.addUserSetting( userSetting );
    }

    @Override
    public void addOrUpdateUserSetting( UserSetting userSetting )
    {
        UserSetting setting = getUserSetting( userSetting.getUser(), userSetting.getName() );

        if ( setting != null )
        {
            setting.mergeWith( userSetting );
            updateUserSetting( setting );
        }
        else
        {
            addUserSetting( userSetting );
        }
    }

    @Override
    public void updateUserSetting( UserSetting userSetting )
    {
        userSettingStore.updateUserSetting( userSetting );
    }

    @Override
    public void deleteUserSetting( UserSetting userSetting )
    {
        userSettingStore.deleteUserSetting( userSetting );
    }

    @Override
    public Collection<UserSetting> getAllUserSettings( User user )
    {
        return userSettingStore.getAllUserSettings( user );
    }

    @Override
    public Collection<UserSetting> getUserSettings( String name )
    {
        return userSettingStore.getUserSettings( name );
    }

    @Override
    public UserSetting getUserSetting( User user, String name )
    {
        return userSettingStore.getUserSetting( user, name );
    }

    @Override
    public Serializable getUserSettingValue( User user, String name, Serializable defaultValue )
    {
        UserSetting setting = getUserSetting( user, name );

        return setting != null && setting.getValue() != null ? setting.getValue() : defaultValue;
    }

    @Override
    public Map<User, Serializable> getUserSettings( String name, Serializable defaultValue )
    {
        Map<User, Serializable> map = new HashMap<>();

        for ( UserSetting setting : userSettingStore.getUserSettings( name ) )
        {
            map.put( setting.getUser(), setting.getValue() != null ? setting.getValue() : defaultValue );
        }

        return map;
    }

    @Override
    public void removeUserSettings( User user )
    {
        userSettingStore.removeUserSettings( user );
    }

    // -------------------------------------------------------------------------
    // UserSettingService implementation
    // -------------------------------------------------------------------------

    @Override
    public void saveUserSetting( String name, Serializable value )
    {
        User currentUser = currentUserService.getCurrentUser();
        
        save( name, value, currentUser );
    }

    @Override
    public void saveUserSetting( String name, Serializable value, String username )
    {
        UserCredentials credentials = userService.getUserCredentialsByUsername( username );
        
        if ( credentials != null )
        {        
            save( name, value, credentials.getUser() );
        }
    }

    private void save( String name, Serializable value, User user )
    {
        if ( user == null )
        {
            return;
        }

        UserSetting userSetting = getUserSetting( user, name );

        if ( userSetting == null )
        {
            userSetting = new UserSetting();
            userSetting.setUser( user );
            userSetting.setName( name );
            userSetting.setValue( value );

            addUserSetting( userSetting );
        }
        else
        {
            userSetting.setValue( value );

            updateUserSetting( userSetting );
        }
    }

    @Override
    public Serializable getUserSetting( String name )
    {
        User currentUser = currentUserService.getCurrentUser();
        return getUserSetting(name, currentUser);
    }


    @Override
    public Serializable getUserSetting( String name, String username )
    {
        UserCredentials credentials = userService.getUserCredentialsByUsername( username );
        
        return getUserSetting( name, credentials == null ? null : credentials.getUser() );
    }

    private Serializable getUserSetting( String name, User currentUser ) 
    {
        if ( currentUser == null )
        {
            return null;
        }

        UserSetting userSetting = getUserSetting( currentUser, name );

        if ( userSetting != null )
        {
            return userSetting.getValue();
        }

        return null;
    }

    @Override
    public Serializable getUserSetting( String name, Serializable defaultValue )
    {
        User currentUser = currentUserService.getCurrentUser();

        if ( currentUser == null )
        {
            return defaultValue;
        }

        UserSetting userSetting = getUserSetting( currentUser, name );

        if ( userSetting != null )
        {
            return userSetting.getValue();
        }

        return defaultValue;
    }

    @Override
    public Collection<UserSetting> getAllUserSettings()
    {
        User currentUser = currentUserService.getCurrentUser();

        if ( currentUser == null )
        {
            return Collections.emptySet();
        }

        return getAllUserSettings( currentUser );
    }

    @Override
    public void deleteUserSetting( String name )
    {
        User currentUser = currentUserService.getCurrentUser();

        if ( currentUser != null )
        {
            deleteUserSetting( getUserSetting( currentUser, name ) );
        }
    }
}
