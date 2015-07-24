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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The main interface for working with user settings. Implementation need to get
 * the current user from {@link CurrentUserService}.
 * 
 * @author Torgeir Lorange Ostby
 * @version $Id: UserSettingService.java 2869 2007-02-20 14:26:09Z andegje $
 */
public interface UserSettingService
{
    String ID = UserSettingService.class.getName();

    final String AUTO_SAVE_DATA_ENTRY_FORM = "autoSaveDataEntryForm";
    final String KEY_CURRENT_DOMAIN_TYPE = "currentDomainType";
    final String KEY_STYLE = "stylesheet";
    final String KEY_STYLE_DIRECTORY = "stylesheetDirectory";
    final String KEY_MESSAGE_EMAIL_NOTIFICATION = "keyMessageEmailNotification";
    final String KEY_MESSAGE_SMS_NOTIFICATION = "keyMessageSmsNotification";
    final String KEY_UI_LOCALE = "keyUiLocale";
    final String KEY_DB_LOCALE = "keyDbLocale";
    final String KEY_GENERATE_REPORT_INTERFACE = "keyGenerateReportInterface";
    final String KEY_ANALYSIS_DISPLAY_PROPERTY = "keyAnalysisDisplayProperty";
    final String AUTO_SAVE_CASE_ENTRY_FORM = "autoSaveCaseEntryForm";
    final String AUTO_SAVE_TRACKED_ENTITY_REGISTRATION_ENTRY_FORM = "autoSavetTrackedEntityForm";
    final String DEFAULT_ANALYSIS_DISPLAY_PROPERTY = "name";
    
    final List<Integer> DASHBOARD_CHARTS_TO_DISPLAY = Arrays.asList( 4, 6, 8 );

    // -------------------------------------------------------------------------
    // UserSettings
    // -------------------------------------------------------------------------

    /**
     * Adds a UserSetting.
     *
     * @param userSetting the UserSetting to add.
     */
    void addUserSetting( UserSetting userSetting );

    /**
     * If a matching UserSetting exists, based on its user and name, it will be
     * updated, if not, the given UserSetting will be added.
     *
     * @param userSetting the UserSetting.
     */
    void addOrUpdateUserSetting( UserSetting userSetting );

    /**
     * Updates a UserSetting.
     *
     * @param userSetting the UserSetting to update.
     */
    void updateUserSetting( UserSetting userSetting );

    /**
     * Retrieves the UserSetting associated with the given User for the given
     * UserSetting name.
     *
     * @param user the User.
     * @param name the name of the UserSetting.
     * @return the UserSetting.
     */
    UserSetting getUserSetting( User user, String name );

    /**
     * Retrieves a user setting value for the given user and setting name. Returns
     * the given default value if the setting does not exist or the setting value
     * is null.
     *
     * @param user         the user.
     * @param name         the setting name.
     * @param defaultValue the default value.
     * @return a setting value.
     */
    Serializable getUserSettingValue( User user, String name, Serializable defaultValue );

    /**
     * Retrieves all UserSettings for the given User.
     *
     * @param user the User.
     * @return a Collection of UserSettings.
     */
    Collection<UserSetting> getAllUserSettings( User user );

    /**
     * Retrieves all UserSettings with the given name.
     * 
     * @param name the name.
     * @return a Collection of UserSettings.
     */
    Collection<UserSetting> getUserSettings( String name );

    /**
     * Deletes a UserSetting.
     *
     * @param userSetting the UserSetting to delete.
     */
    void deleteUserSetting( UserSetting userSetting );

    /**
     * Returns a Map with an entry for all UserSettings with the given name where
     * the key is the user and the value is the value of the user setting.
     *
     * @param name         the name of the UserSetting.
     * @param defaultValue the value to return if the UserSetting value is null.
     * @return a Map.
     */
    Map<User, Serializable> getUserSettings( String name, Serializable defaultValue );

    /**
     * Removes all user settings associated with the given user.
     *
     * @param user the user.
     */
    void removeUserSettings( User user );

    /**
     * Saves the name/value pair as a user setting connected to the currently
     * logged in user.
     * 
     * @param name the name/handle of the value.
     * @param value the value to store.
     * @throws NoCurrentUserException if there is no current user.
     */
    void saveUserSetting( String name, Serializable value );

    /**
     * Saves the name/value pair as a user setting connected to user identified by username
     *
     * @param name the name/handle of the value.
     * @param value the value to store.
     * @param username the username of user.
     * @throws NoCurrentUserException if there is no user.
     */
    void saveUserSetting( String name, Serializable value, String username );

    /**
     * Returns the value of the user setting specified by the given name.
     * 
     * @param name the name of the user setting.
     * @return the value corresponding to the named user setting, or null if
     *         there is no match.
     * @throws NoCurrentUserException if there is no current user.
     */
    Serializable getUserSetting( String name );

    /**
     * Returns the value of the user setting specified by the given name.
     * 
     * @param name the name of the user setting.
     * @return the value corresponding to the named user setting, or null if
     *         there is no match.
     * @throws NoCurrentUserException if there is no current user.
     */
    Serializable getUserSetting( String name, String username );

    /**
     * Returns the value of the user setting specified by the given name. If
     * there is no current user or the user setting doesn't exist, the specified
     * default value is returned.
     * 
     * @param name the name of the user setting.
     * @param defaultValue the value to return if there is no current user or no
     *        user setting corresponding to the given name.
     * @return the value corresponding to the names user setting, or the default
     *         value if there is no current user or matching user setting.
     */
    Serializable getUserSetting( String name, Serializable defaultValue );

    /**
     * Returns all user settings belonging to the current user.
     * 
     * @return all user settings belonging to the current user.
     * @throws NoCurrentUserException if there is no current user.
     */
    Collection<UserSetting> getAllUserSettings();

    /**
     * Deletes the user setting with the given name.
     * 
     * @param name the name of the user setting to delete.
     * @throws NoCurrentUserException if there is no current user.
     */
    void deleteUserSetting( String name );
}
