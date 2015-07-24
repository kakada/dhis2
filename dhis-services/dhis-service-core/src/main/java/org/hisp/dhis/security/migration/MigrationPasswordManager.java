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

import org.hisp.dhis.security.PasswordManager;

/**
 * Drop-in replacement for PasswordManager which provides access to legacy password hashing methods as
 * well as the currently used hashing methods. This is useful in implementing seamless migration to
 * a new and more secure password hashing method.
 * In such a migration phase the system will need to be able to authenticate passwords and tokens
 * which are encoded using both the legacy hash method and the current one.
 * Specific methods for encoding and matching using the legacy hashing method are provided alongside the
 * current ones. The {@link #legacyOrCurrentMatches(String, String,String) legacyOrCurrentMatches}
 * method should be used to provide backwards compatibility where applicable.
 *
 * @author Halvdan Hoem Grelland
 */
public interface MigrationPasswordManager
    extends PasswordManager
{
    /**
     * Cryptographically hash a password using a legacy encoder.
     * Useful for access to the former (legacy) hash method when implementing migration to a new method.
     *
     * @param password the password to encode.
     * @param username the username (used for seeding salt generation).
     * @return the encoded (hashed) password.
     */
    public String legacyEncode( String password, String username );

    /**
     * Determines whether the supplied password equals the encoded password or not.
     * Uses the legacy hashing method to do so and is useful in implementing migration to a new method.
     *
     * @param rawPassword the password.
     * @param encodedPassword the encoded password.
     * @param username the username (used for salt generation).
     * @return true if the password matches the encoded password, false otherwise.
     */
    public boolean legacyMatches( String rawPassword, String encodedPassword, String username );

    /**
     * Determines whether encodedPassword is a valid hash of password using either the legacy or
     * current encoder (hashing method).
     * This method is a migration specific wrapper for the {@link org.hisp.dhis.security.PasswordManager#matches(String, String) matches}
     * method in order to support authenticating hashes which were generated using the legacy hash
     * implementation in addition to the current hashing scheme.
     * Use this method to provide backwards compatibility for hashes.
     * Replace with {@link org.hisp.dhis.security.PasswordManager#matches(String, String) matches}
     * when disabling (ending) backwards compatibility.
     *
     * @param rawPassword the un-encoded token as supplied from the user.
     * @param encodedPassword the encoded token to match against.
     * @param username the username associated with the token (used for salting by the legacy password encoder).
     * @return true if the token matches for either the legacy or current hashing scheme, false otherwise.
     */
    public boolean legacyOrCurrentMatches( String rawPassword, String encodedPassword, String username );

    /**
     * Return the class name of the legacy password encoder.
     * @return the name of the legacy password encoder class.
     */
    public String getLegacyPasswordEncoderClassName();
}
