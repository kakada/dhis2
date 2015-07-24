package org.hisp.dhis.security.migration;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Implements migration of legacy user password hashes on user login.
 *
 * The procedure to do so works by preceding the ordinary authentication check
 * (which is performed using the current password hashing method) with an authentication
 * procedure using the legacy password hashing method.
 *
 * If the currently stored hash and the legacyHash(suppliedPassword, usernameSalt) matches
 * the password is hashed again using the current method and replaces the stored hash for the user.
 * The user is now migrated to the current password hashing scheme and will on next logon not
 * authenticate using the legacy hash method but the current one.
 *
 * In either case the call is followed by the authentication procedure in DaoAuthenticationProvider
 * which performs the final authentication (using the current method).
 *
 * @author Halvdan Hoem Grelland
 */
public class MigrationAuthenticationProvider
    extends DaoAuthenticationProvider
{
    private static final Log log = LogFactory.getLog( MigrationAuthenticationProvider.class );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    private MigrationPasswordManager passwordManager;

    public void setPasswordManager( MigrationPasswordManager passwordManager )
    {
        this.passwordManager = passwordManager;
    }

    // -------------------------------------------------------------------------
    // Pre-auth check-and-switch for legacy password hash match
    // -------------------------------------------------------------------------

    @Override
    protected void additionalAuthenticationChecks( UserDetails userDetails,
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken )
        throws AuthenticationException
    {
        String password = (String) usernamePasswordAuthenticationToken.getCredentials();
        String username = userDetails.getUsername();

        // If legacyHash(password, username) matches stored hash, re-hash password with current method and switch with stored hash
        if( passwordManager.legacyMatches( password, userDetails.getPassword(), username ) )
        {
            UserCredentials userCredentials = userService.getUserCredentialsByUsername( username );

            if ( userCredentials != null )
            {
                userService.encodeAndSetPassword( userCredentials, password );
                userService.updateUser( userCredentials.getUser() );

                log.info( "User " + userCredentials.getUsername() + " was migrated from " + passwordManager.getLegacyPasswordEncoderClassName() +
                    " to " + passwordManager.getPasswordEncoderClassName() + " based password hashing on login." );

                userDetails = getUserDetailsService().loadUserByUsername( username );
            }
        }
        super.additionalAuthenticationChecks( userDetails, usernamePasswordAuthenticationToken );
    }
}
