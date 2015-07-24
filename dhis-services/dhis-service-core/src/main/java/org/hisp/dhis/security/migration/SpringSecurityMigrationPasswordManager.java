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

import org.hisp.dhis.security.UsernameSaltSource;
import org.hisp.dhis.security.spring.SpringSecurityPasswordManager;
import org.springframework.security.authentication.encoding.PasswordEncoder;

/**
 * @author Halvdan Hoem Grelland
 */
public class SpringSecurityMigrationPasswordManager
    extends SpringSecurityPasswordManager
    implements MigrationPasswordManager
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PasswordEncoder legacyPasswordEncoder;

    public void setLegacyPasswordEncoder( PasswordEncoder legacyPasswordEncoder )
    {
        this.legacyPasswordEncoder = legacyPasswordEncoder;
    }

    private UsernameSaltSource usernameSaltSource;

    public void setUsernameSaltSource( UsernameSaltSource usernameSaltSource )
    {
        this.usernameSaltSource = usernameSaltSource;
    }

    // -------------------------------------------------------------------------
    // MigrationPasswordManager implementation
    // -------------------------------------------------------------------------

    @Override
    public final String legacyEncode( String password, String username )
    {
        return legacyPasswordEncoder.encodePassword( password, usernameSaltSource.getSalt( username ) );
    }

    @Override
    public boolean legacyMatches( String rawPassword, String encodedPassword, String username )
    {
        return legacyPasswordEncoder.isPasswordValid( encodedPassword, rawPassword, usernameSaltSource.getSalt( username ) );
    }

    @Override
    public boolean legacyOrCurrentMatches( String rawPassword, String encodedPassword, String username )
    {
        return legacyMatches( rawPassword, encodedPassword, username ) || super.matches( rawPassword, encodedPassword );
    }

    @Override
    public String getLegacyPasswordEncoderClassName()
    {
        return legacyPasswordEncoder.getClass().getName();
    }
}
