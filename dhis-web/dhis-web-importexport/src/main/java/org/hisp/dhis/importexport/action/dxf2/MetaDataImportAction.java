package org.hisp.dhis.importexport.action.dxf2;

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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dataelement.CategoryOptionGroup;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOption;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dxf2.csv.CsvImportService;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.gml.GmlImportService;
import org.hisp.dhis.dxf2.metadata.ImportService;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.importexport.action.util.ImportMetaDataCsvTask;
import org.hisp.dhis.importexport.action.util.ImportMetaDataGmlTask;
import org.hisp.dhis.importexport.action.util.ImportMetaDataTask;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.scheduling.TaskCategory;
import org.hisp.dhis.scheduling.TaskId;
import org.hisp.dhis.system.notification.Notifier;
import org.hisp.dhis.system.scheduling.Scheduler;
import org.hisp.dhis.system.util.StreamUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.validation.ValidationRule;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class MetaDataImportAction
    implements Action
{
    private static final Map<String, Class<? extends IdentifiableObject>> KEY_CLASS_MAP = new HashMap<String, Class<? extends IdentifiableObject>>() {{
       put( "dataelement", DataElement.class );
       put( "dataelementgroup", DataElementGroup.class);
       put( "categoryoption", DataElementCategoryOption.class );
       put( "categoryoptiongroup", CategoryOptionGroup.class );
       put( "organisationunit", OrganisationUnit.class );
       put( "organisationunitgroup", OrganisationUnitGroup.class );
       put( "validationrule", ValidationRule.class );
       put( "optionset", OptionSet.class );
    }};
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private ImportService importService;
    
    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private GmlImportService gmlImportService;
    
    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private Notifier notifier;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private File upload;

    public void setUpload( File upload )
    {
        this.upload = upload;
    }

    private boolean dryRun;

    public void setDryRun( boolean dryRun )
    {
        this.dryRun = dryRun;
    }

    private ImportStrategy strategy;

    public void setStrategy( String strategy )
    {
        this.strategy = ImportStrategy.valueOf( strategy );
    }

    private String importFormat;

    public String getImportFormat()
    {
        return importFormat;
    }

    public void setImportFormat( String importFormat )
    {
        this.importFormat = importFormat;
    }
    
    private String classKey;

    public void setClassKey( String classKey )
    {
        this.classKey = classKey;
    }

    private boolean preheatCache = true;
    
    public void setPreheatCache( boolean preheatCache )
    {
        this.preheatCache = preheatCache;
    }

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute() throws Exception
    {
        strategy = strategy != null ? strategy : ImportStrategy.NEW_AND_UPDATES;

        User user = currentUserService.getCurrentUser();
        
        TaskId taskId = new TaskId( TaskCategory.METADATA_IMPORT, user );

        notifier.clear( taskId );

        InputStream in = new FileInputStream( upload );
        in = StreamUtils.wrapAndCheckCompressionFormat( in );

        ImportOptions importOptions = new ImportOptions();
        importOptions.setStrategy( strategy.toString() );
        importOptions.setDryRun( dryRun );
        importOptions.setPreheatCache( preheatCache );

        String userId = user != null ? user.getUid() : null;

        if ( "csv".equals( importFormat ) )
        {
            if( classKey != null && KEY_CLASS_MAP.get( classKey ) != null )
            {
                scheduler.executeTask( new ImportMetaDataCsvTask( userId, importService, csvImportService,
                    importOptions, in, taskId, KEY_CLASS_MAP.get( classKey ) ) );
            }
        }
        else if ( "gml".equals( importFormat ) )
        {
            scheduler.executeTask( new ImportMetaDataGmlTask( userId, gmlImportService, importOptions, in, taskId ) );
        }
        else if ( "json".equals( importFormat ) || "xml".equals( importFormat ) )
        {
            scheduler.executeTask( new ImportMetaDataTask( userId, importService, importOptions, in, taskId, importFormat ) );
        }
        
        return SUCCESS;
    }
}
