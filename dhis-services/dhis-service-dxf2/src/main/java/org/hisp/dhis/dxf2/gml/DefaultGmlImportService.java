package org.hisp.dhis.dxf2.gml;

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

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.IdentifiableProperty;
import org.hisp.dhis.common.MergeStrategy;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.metadata.ImportService;
import org.hisp.dhis.dxf2.metadata.MetaData;
import org.hisp.dhis.dxf2.render.RenderService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.scheduling.TaskId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Halvdan Hoem Grelland
 */
public class DefaultGmlImportService
    implements GmlImportService
{
    private static final String GML_TO_DXF_STYLESHEET = "gml/gml2dxf2.xsl";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private ImportService importService;

    @Autowired
    private RenderService renderService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private IdentifiableObjectManager idObjectManager;

    // -------------------------------------------------------------------------
    // GmlImportService implementation
    // -------------------------------------------------------------------------

    @Override
    public MetaData fromGml( InputStream inputStream )
        throws IOException, TransformerException
    {
        InputStream dxfStream = transformGml( inputStream );
        MetaData metaData = renderService.fromXml( dxfStream, MetaData.class );
        dxfStream.close();

        Map<String, OrganisationUnit> uidMap  = Maps.newHashMap(), codeMap = Maps.newHashMap(), nameMap = Maps.newHashMap();

        matchAndFilterOnIdentifiers( metaData.getOrganisationUnits(), uidMap, codeMap, nameMap );

        Map<String, OrganisationUnit> persistedUidMap  = getMatchingPersistedOrgUnits( uidMap.keySet(),  IdentifiableProperty.UID );
        Map<String, OrganisationUnit> persistedCodeMap = getMatchingPersistedOrgUnits( codeMap.keySet(), IdentifiableProperty.CODE );
        Map<String, OrganisationUnit> persistedNameMap = getMatchingPersistedOrgUnits( nameMap.keySet(), IdentifiableProperty.NAME );

        Iterator<OrganisationUnit> persistedIterator = Iterators.concat( persistedUidMap.values().iterator(),
            persistedCodeMap.values().iterator(), persistedNameMap.values().iterator() );

        while ( persistedIterator.hasNext() )
        {
            OrganisationUnit persisted = persistedIterator.next(), imported = null;

            if ( !Strings.isNullOrEmpty( persisted.getUid() ) && uidMap.containsKey( persisted.getUid() ) )
            {
                imported = uidMap.get( persisted.getUid() );
            }
            else if ( !Strings.isNullOrEmpty( persisted.getCode() ) && codeMap.containsKey( persisted.getCode() ) )
            {
                imported = codeMap.get( persisted.getCode() );
            }
            else if ( !Strings.isNullOrEmpty( persisted.getName() ) && nameMap.containsKey( persisted.getName() ) )
            {
                imported = nameMap.get( persisted.getName() );
            }

            if ( imported == null || imported.getCoordinates() == null || imported.getFeatureType() == null )
            {
                continue; // Failed to dereference a persisted entity for this org unit or geo data incomplete/missing, therefore ignore
            }

            mergeNonGeoData( persisted, imported );
        }

        return metaData;
    }

    @Transactional
    @Override
    public void importGml( InputStream inputStream, String userUid, ImportOptions importOptions, TaskId taskId )
        throws IOException, TransformerException
    {
        importService.importMetaData( userUid, fromGml( inputStream ), importOptions, taskId );
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private InputStream transformGml( InputStream input )
        throws IOException, TransformerException
    {
        StreamSource gml = new StreamSource( input );
        StreamSource xsl = new StreamSource( new ClassPathResource( GML_TO_DXF_STYLESHEET ).getInputStream() );

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        TransformerFactory.newInstance().newTransformer( xsl ).transform( gml, new StreamResult( output ) );

        xsl.getInputStream().close();
        gml.getInputStream().close();

        return new ByteArrayInputStream( output.toByteArray() );
    }

    private void matchAndFilterOnIdentifiers( List<OrganisationUnit> sourceList, Map<String, OrganisationUnit> uidMap, Map<String,
        OrganisationUnit> codeMap, Map<String, OrganisationUnit> nameMap )
    {
        for ( OrganisationUnit orgUnit : sourceList ) // Identifier Matching priority: uid, code, name
        {
            // Only matches if UID is actually in DB as an empty UID on input will be replaced by auto-generated value
            if ( !Strings.isNullOrEmpty( orgUnit.getUid() ) && idObjectManager.exists( OrganisationUnit.class, orgUnit.getUid() ) )
            {
                uidMap.put( orgUnit.getUid(), orgUnit );
            }
            else if ( !Strings.isNullOrEmpty( orgUnit.getCode() ) )
            {
                codeMap.put( orgUnit.getCode(), orgUnit );
            }
            else if ( !Strings.isNullOrEmpty( orgUnit.getName() ) )
            {
                nameMap.put( orgUnit.getName(), orgUnit );
            }
        }
    }

    private Map<String, OrganisationUnit> getMatchingPersistedOrgUnits( Collection<String> identifiers, final IdentifiableProperty idProperty )
    {
        Collection<OrganisationUnit> orgUnits =
            idProperty == IdentifiableProperty.UID ? organisationUnitService.getOrganisationUnitsByUid( identifiers ) :
            idProperty == IdentifiableProperty.CODE ? organisationUnitService.getOrganisationUnitsByCodes( identifiers ) :
            idProperty == IdentifiableProperty.NAME ? organisationUnitService.getOrganisationUnitsByNames( identifiers ) :
            new HashSet<OrganisationUnit>();

        return Maps.uniqueIndex( orgUnits,
            new Function<OrganisationUnit, String>()
            {
                @Override
                public String apply( OrganisationUnit organisationUnit )
                {
                    return idProperty == IdentifiableProperty.UID ? organisationUnit.getUid() :
                           idProperty == IdentifiableProperty.CODE ? organisationUnit.getCode() :
                           idProperty == IdentifiableProperty.NAME ? organisationUnit.getName() : null;
                }
            }
        );
    }

    private void mergeNonGeoData( OrganisationUnit source, OrganisationUnit target )
    {
        String coordinates = target.getCoordinates(),
               featureType = target.getFeatureType();

        target.mergeWith( source, MergeStrategy.MERGE );

        target.setCoordinates( coordinates );
        target.setFeatureType( featureType );

        if ( source.getParent() != null )
        {
            OrganisationUnit parent = new OrganisationUnit();
            parent.setUid( source.getParent().getUid() );
            target.setParent( parent );
        }
    }
}
