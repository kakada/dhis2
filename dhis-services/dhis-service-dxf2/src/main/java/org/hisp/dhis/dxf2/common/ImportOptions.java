package org.hisp.dhis.dxf2.common;

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

import org.hisp.dhis.common.IdentifiableProperty;
import org.hisp.dhis.common.MergeStrategy;
import org.hisp.dhis.importexport.ImportStrategy;

/**
 * The idScheme is a general setting which will apply to all objects. The idSchemes
 * can also be defined for specific objects such as dataElementIdScheme. The
 * general setting will override specific settings.
 *
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class ImportOptions
{
    private static final ImportOptions DEFAULT_OPTIONS = new ImportOptions(
        IdentifiableProperty.UID, IdentifiableProperty.UID, false, true, ImportStrategy.NEW_AND_UPDATES, false );

    private IdentifiableProperty idScheme;

    private IdentifiableProperty dataElementIdScheme;

    private IdentifiableProperty orgUnitIdScheme;

    private boolean dryRun;

    private boolean preheatCache = true;

    private boolean async;

    private ImportStrategy importStrategy;

    private MergeStrategy mergeStrategy = MergeStrategy.MERGE_IF_NOT_NULL;

    private boolean skipExistingCheck;

    private boolean sharing;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public ImportOptions()
    {
    }

    public ImportOptions( ImportStrategy importStrategy )
    {
        this.importStrategy = importStrategy;
    }

    public ImportOptions( IdentifiableProperty dataElementIdScheme, IdentifiableProperty orgUnitIdScheme,
        boolean dryRun, boolean preheatCache, ImportStrategy importStrategy, boolean skipExistingCheck )
    {
        this.dataElementIdScheme = dataElementIdScheme;
        this.orgUnitIdScheme = orgUnitIdScheme;
        this.preheatCache = preheatCache;
        this.dryRun = dryRun;
        this.importStrategy = importStrategy;
        this.skipExistingCheck = skipExistingCheck;
    }

    public ImportOptions( IdentifiableProperty idScheme, IdentifiableProperty dataElementIdScheme, IdentifiableProperty orgUnitIdScheme,
        boolean dryRun, boolean preheatCache, ImportStrategy importStrategy, boolean skipExistingCheck )
    {
        this.idScheme = idScheme;
        this.dataElementIdScheme = dataElementIdScheme;
        this.orgUnitIdScheme = orgUnitIdScheme;
        this.preheatCache = preheatCache;
        this.dryRun = dryRun;
        this.importStrategy = importStrategy;
        this.skipExistingCheck = skipExistingCheck;
    }

    //--------------------------------------------------------------------------
    // Logic
    //--------------------------------------------------------------------------

    public static ImportOptions getDefaultImportOptions()
    {
        return DEFAULT_OPTIONS;
    }

    //--------------------------------------------------------------------------
    // Get methods
    //--------------------------------------------------------------------------

    public IdentifiableProperty getIdScheme()
    {
        return idScheme != null ? idScheme : IdentifiableProperty.UID;
    }

    public IdentifiableProperty getDataElementIdScheme()
    {
        return dataElementIdScheme != null ? dataElementIdScheme : (idScheme != null ? idScheme : IdentifiableProperty.UID);
    }

    public IdentifiableProperty getOrgUnitIdScheme()
    {
        return orgUnitIdScheme != null ? orgUnitIdScheme : (idScheme != null ? idScheme : IdentifiableProperty.UID);
    }

    public boolean isDryRun()
    {
        return dryRun;
    }

    public boolean isPreheatCache()
    {
        return preheatCache;
    }

    public ImportStrategy getImportStrategy()
    {
        return importStrategy != null ? importStrategy : ImportStrategy.NEW_AND_UPDATES;
    }

    public MergeStrategy getMergeStrategy()
    {
        return mergeStrategy;
    }

    public void setMergeStrategy( MergeStrategy mergeStrategy )
    {
        this.mergeStrategy = mergeStrategy;
    }

    public boolean isSkipExistingCheck()
    {
        return skipExistingCheck;
    }

    //--------------------------------------------------------------------------
    // Set methods
    //--------------------------------------------------------------------------

    public void setIdScheme( String scheme )
    {
        this.idScheme = scheme != null ? IdentifiableProperty.valueOf( scheme.toUpperCase() ) : null;
    }

    public void setDataElementIdScheme( String scheme )
    {
        this.dataElementIdScheme = scheme != null ? IdentifiableProperty.valueOf( scheme.toUpperCase() ) : null;
    }

    public void setOrgUnitIdScheme( String scheme )
    {
        this.orgUnitIdScheme = scheme != null ? IdentifiableProperty.valueOf( scheme.toUpperCase() ) : null;
    }

    public void setDryRun( boolean dryRun )
    {
        this.dryRun = dryRun;
    }

    public void setPreheatCache( boolean preheatCache )
    {
        this.preheatCache = preheatCache;
    }

    public boolean isAsync()
    {
        return async;
    }

    public void setAsync( boolean async )
    {
        this.async = async;
    }

    public void setStrategy( String strategy )
    {
        this.importStrategy = strategy != null ? ImportStrategy.valueOf( strategy.toUpperCase() ) : null;
    }

    public void setImportStrategy( String strategy )
    {
        this.importStrategy = strategy != null ? ImportStrategy.valueOf( strategy.toUpperCase() ) : null;
    }

    public void setSkipExistingCheck( boolean skipExistingCheck )
    {
        this.skipExistingCheck = skipExistingCheck;
    }

    public boolean isSharing()
    {
        return sharing;
    }

    public void setSharing( boolean sharing )
    {
        this.sharing = sharing;
    }

    @Override
    public String toString()
    {
        return "[Id scheme: " + idScheme + ", data element id scheme: " + dataElementIdScheme + ", org unit id scheme: " +
            orgUnitIdScheme + ", dry run: " + dryRun + ", preheat cache: " + preheatCache + ", async: " +
            async + ", strategy: " + importStrategy + ", skip check: " + skipExistingCheck + "]";
    }
}
