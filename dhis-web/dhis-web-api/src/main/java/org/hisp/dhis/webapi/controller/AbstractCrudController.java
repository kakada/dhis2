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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Enums;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.hisp.dhis.acl.AclService;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.MergeStrategy;
import org.hisp.dhis.common.Pager;
import org.hisp.dhis.common.PagerUtils;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.common.OrderOptions;
import org.hisp.dhis.dxf2.common.TranslateOptions;
import org.hisp.dhis.dxf2.fieldfilter.FieldFilterService;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.metadata.ImportService;
import org.hisp.dhis.dxf2.metadata.ImportTypeSummary;
import org.hisp.dhis.dxf2.objectfilter.ObjectFilterService;
import org.hisp.dhis.dxf2.render.DefaultRenderService;
import org.hisp.dhis.dxf2.render.RenderService;
import org.hisp.dhis.hibernate.exception.CreateAccessDeniedException;
import org.hisp.dhis.hibernate.exception.DeleteAccessDeniedException;
import org.hisp.dhis.hibernate.exception.UpdateAccessDeniedException;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.node.Node;
import org.hisp.dhis.node.NodeUtils;
import org.hisp.dhis.node.config.InclusionStrategy;
import org.hisp.dhis.node.types.CollectionNode;
import org.hisp.dhis.node.types.ComplexNode;
import org.hisp.dhis.node.types.RootNode;
import org.hisp.dhis.node.types.SimpleNode;
import org.hisp.dhis.query.Order;
import org.hisp.dhis.query.Query;
import org.hisp.dhis.query.QueryService;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.webapi.controller.exception.NotFoundException;
import org.hisp.dhis.webapi.service.ContextService;
import org.hisp.dhis.webapi.service.LinkService;
import org.hisp.dhis.webapi.utils.ContextUtils;
import org.hisp.dhis.webapi.webdomain.WebMetaData;
import org.hisp.dhis.webapi.webdomain.WebOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public abstract class AbstractCrudController<T extends IdentifiableObject>
{
    //--------------------------------------------------------------------------
    // Dependencies
    //--------------------------------------------------------------------------

    @Autowired
    protected IdentifiableObjectManager manager;

    @Autowired
    protected CurrentUserService currentUserService;

    @Autowired
    protected ObjectFilterService objectFilterService;

    @Autowired
    protected FieldFilterService fieldFilterService;

    @Autowired
    protected AclService aclService;

    @Autowired
    protected SchemaService schemaService;

    @Autowired
    protected LinkService linkService;

    @Autowired
    protected RenderService renderService;

    @Autowired
    protected ImportService importService;

    @Autowired
    protected ContextService contextService;

    @Autowired
    protected I18nService i18nService;

    @Autowired
    protected QueryService queryService;

    //--------------------------------------------------------------------------
    // GET
    //--------------------------------------------------------------------------

    @RequestMapping( method = RequestMethod.GET )
    public @ResponseBody RootNode getObjectList(
        @RequestParam Map<String, String> rpParameters,
        TranslateOptions translateOptions, OrderOptions orderOptions,
        HttpServletResponse response, HttpServletRequest request )
    {
        List<String> fields = Lists.newArrayList( contextService.getParameterValues( "fields" ) );
        List<String> filters = Lists.newArrayList( contextService.getParameterValues( "filter" ) );
        List<Order> orders = orderOptions.getOrders( getSchema() );

        WebOptions options = new WebOptions( rpParameters );
        WebMetaData metaData = new WebMetaData();

        if ( fields.isEmpty() )
        {
            fields.add( ":identifiable" );
        }

        List<T> entities = getEntityList( metaData, options, filters, orders );
        Pager pager = metaData.getPager();

        entities = objectFilterService.filter( entities, filters );
        translate( entities, translateOptions );

        if ( options.hasPaging() && pager == null )
        {
            pager = new Pager( options.getPage(), entities.size(), options.getPageSize() );
            entities = PagerUtils.pageCollection( entities, pager );
        }

        postProcessEntities( entities );
        postProcessEntities( entities, options, rpParameters );

        if ( fields.contains( "access" ) )
        {
            options.getOptions().put( "viewClass", "sharing" );
        }

        handleLinksAndAccess( options, entities, false );

        linkService.generatePagerLinks( pager, getEntityClass() );

        RootNode rootNode = NodeUtils.createMetadata();
        rootNode.getConfig().setInclusionStrategy( getInclusionStrategy( rpParameters.get( "inclusionStrategy" ) ) );

        if ( pager != null )
        {
            rootNode.addChild( NodeUtils.createPager( pager ) );
        }

        rootNode.addChild( fieldFilterService.filter( getEntityClass(), entities, fields ) );

        return rootNode;
    }

    @RequestMapping( value = "/{uid}", method = RequestMethod.GET )
    public @ResponseBody RootNode getObject(
        @PathVariable( "uid" ) String pvUid,
        @RequestParam Map<String, String> rpParameters,
        TranslateOptions translateOptions,
        HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        List<String> fields = Lists.newArrayList( contextService.getParameterValues( "fields" ) );
        List<String> filters = Lists.newArrayList( contextService.getParameterValues( "filter" ) );

        if ( fields.isEmpty() )
        {
            fields.add( ":all" );
        }

        return getObjectInternal( pvUid, rpParameters, filters, fields, translateOptions );
    }

    @RequestMapping( value = "/{uid}/{property}", method = RequestMethod.GET )
    public @ResponseBody RootNode getObjectProperty(
        @PathVariable( "uid" ) String pvUid, @PathVariable( "property" ) String pvProperty,
        @RequestParam Map<String, String> rpParameters,
        TranslateOptions translateOptions,
        HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        List<String> fields = Lists.newArrayList( contextService.getParameterValues( "fields" ) );

        if ( fields.isEmpty() )
        {
            fields.add( ":all" );
        }

        String fieldFilter = "[" + Joiner.on( ',' ).join( fields ) + "]";

        return getObjectInternal( pvUid, rpParameters, Lists.<String>newArrayList(), Lists.newArrayList( pvProperty + fieldFilter ), translateOptions );
    }

    @RequestMapping( value = "/{uid}", method = RequestMethod.PATCH )
    public void partialUpdateObject(
        @PathVariable( "uid" ) String pvUid, @RequestParam Map<String, String> rpParameters,
        HttpServletRequest request, HttpServletResponse response ) throws IOException, InvocationTargetException, IllegalAccessException
    {
        WebOptions options = new WebOptions( rpParameters );
        List<T> entities = getEntity( pvUid, options );

        if ( entities.isEmpty() )
        {
            ContextUtils.notFoundResponse( response, getEntityName() + " does not exist: " + pvUid );
            return;
        }

        T persistedObject = entities.get( 0 );

        if ( !aclService.canUpdate( currentUserService.getCurrentUser(), persistedObject ) )
        {
            throw new UpdateAccessDeniedException( "You don't have the proper permissions to update this object." );
        }

        String payload = StreamUtils.copyToString( request.getInputStream(), Charset.forName( "UTF-8" ) );
        List<String> properties = new ArrayList<>();
        T object = null;

        if ( isJson( request ) )
        {
            properties = getJsonProperties( payload );
            object = renderService.fromJson( payload, getEntityClass() );
        }
        else if ( isXml( request ) )
        {
            properties = getXmlProperties( payload );
            object = renderService.fromXml( payload, getEntityClass() );
        }

        properties = getPersistedProperties( properties );

        if ( properties.isEmpty() || object == null )
        {
            response.setStatus( HttpServletResponse.SC_NO_CONTENT );
            return;
        }

        Schema schema = getSchema();

        for ( String keyProperty : properties )
        {
            Property property = schema.getProperty( keyProperty );

            Object value = property.getGetterMethod().invoke( object );
            property.getSetterMethod().invoke( persistedObject, value );
        }

        ImportTypeSummary summary = importService.importObject( currentUserService.getCurrentUser().getUid(), persistedObject,
            ImportStrategy.UPDATE, MergeStrategy.MERGE );

        serialize( request, response, summary );
    }

    private List<String> getJsonProperties( String payload ) throws IOException
    {
        ObjectMapper mapper = ((DefaultRenderService) renderService).getJsonMapper();
        JsonNode root = mapper.readTree( payload );

        return Lists.newArrayList( root.fieldNames() );
    }

    private List<String> getXmlProperties( String payload ) throws IOException
    {
        XmlMapper mapper = ((DefaultRenderService) renderService).getXmlMapper();
        JsonNode root = mapper.readTree( payload );

        return Lists.newArrayList( root.fieldNames() );
    }

    private List<String> getPersistedProperties( List<String> properties )
    {
        List<String> persistedProperties = new ArrayList<>();

        Schema schema = getSchema();

        for ( String property : properties )
        {
            if ( schema.havePersistedProperty( property ) )
            {
                persistedProperties.add( property );
            }
        }

        return persistedProperties;
    }

    @RequestMapping( value = "/{uid}/{property}", method = { RequestMethod.PUT, RequestMethod.PATCH } )
    public void updateObjectProperty(
        @PathVariable( "uid" ) String pvUid, @PathVariable( "property" ) String pvProperty, @RequestParam Map<String, String> rpParameters,
        HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        WebOptions options = new WebOptions( rpParameters );
        List<T> entities = getEntity( pvUid, options );

        if ( entities.isEmpty() )
        {
            ContextUtils.notFoundResponse( response, getEntityName() + " does not exist: " + pvUid );
            return;
        }

        if ( !getSchema().haveProperty( pvProperty ) )
        {
            ContextUtils.notFoundResponse( response, "Property " + pvProperty + " does not exist on " + getEntityName() );
            return;
        }

        Property property = getSchema().getProperty( pvProperty );
        T persistedObject = entities.get( 0 );

        if ( !aclService.canUpdate( currentUserService.getCurrentUser(), persistedObject ) )
        {
            throw new UpdateAccessDeniedException( "You don't have the proper permissions to update this object." );
        }

        if ( !property.isWritable() )
        {
            throw new UpdateAccessDeniedException( "This property is read-only." );
        }

        T object = deserialize( request );

        if ( object == null )
        {
            ContextUtils.badRequestResponse( response, "Unknown payload format." );
            return;
        }

        Object value = property.getGetterMethod().invoke( object );

        property.getSetterMethod().invoke( persistedObject, value );

        ImportTypeSummary summary = importService.importObject( currentUserService.getCurrentUser().getUid(), persistedObject,
            ImportStrategy.UPDATE, MergeStrategy.MERGE );

        serialize( request, response, summary );
    }

    protected void translate( List<?> entities, TranslateOptions translateOptions )
    {
        if ( translateOptions.isTranslate() )
        {
            if ( translateOptions.defaultLocale() )
            {
                i18nService.internationalise( entities );
            }
            else
            {
                i18nService.internationalise( entities, translateOptions.getLocale() );
            }
        }
    }

    private RootNode getObjectInternal( String uid, Map<String, String> parameters,
        List<String> filters, List<String> fields, TranslateOptions translateOptions ) throws Exception
    {
        WebOptions options = new WebOptions( parameters );
        List<T> entities = getEntity( uid, options );

        if ( translateOptions != null )
        {
            translate( entities, translateOptions );
        }

        if ( entities.isEmpty() )
        {
            throw new NotFoundException( uid );
        }

        entities = objectFilterService.filter( entities, filters );

        if ( options.hasLinks() )
        {
            linkService.generateLinks( entities, true );
        }

        if ( aclService.isSupported( getEntityClass() ) )
        {
            addAccessProperties( entities );
        }

        for ( T entity : entities )
        {
            postProcessEntity( entity );
            postProcessEntity( entity, options, parameters );
        }

        CollectionNode collectionNode = fieldFilterService.filter( getEntityClass(), entities, fields );

        if ( options.isTrue( "useWrapper" ) || entities.size() > 1 )
        {
            RootNode rootNode = NodeUtils.createMetadata( collectionNode );
            rootNode.getConfig().setInclusionStrategy( getInclusionStrategy( parameters.get( "inclusionStrategy" ) ) );

            return rootNode;
        }
        else
        {
            List<Node> children = collectionNode.getChildren();
            RootNode rootNode;

            if ( !children.isEmpty() )
            {
                rootNode = NodeUtils.createRootNode( children.get( 0 ) );
            }
            else
            {
                rootNode = NodeUtils.createRootNode( new ComplexNode( getSchema().getSingular() ) );
            }

            rootNode.getConfig().setInclusionStrategy( getInclusionStrategy( parameters.get( "inclusionStrategy" ) ) );

            return rootNode;
        }
    }

    //--------------------------------------------------------------------------
    // POST
    //--------------------------------------------------------------------------

    @RequestMapping( method = RequestMethod.POST, consumes = { "application/xml", "text/xml" } )
    public void postXmlObject( ImportOptions importOptions, HttpServletRequest request, HttpServletResponse response )
        throws Exception
    {
        if ( !aclService.canCreate( currentUserService.getCurrentUser(), getEntityClass() ) )
        {
            throw new CreateAccessDeniedException( "You don't have the proper permissions to create this object." );
        }

        T parsed = renderService.fromXml( request.getInputStream(), getEntityClass() );

        preCreateEntity( parsed );

        ImportTypeSummary summary = importService.importObject( currentUserService.getCurrentUser().getUid(), parsed,
            ImportStrategy.CREATE, importOptions.getMergeStrategy() );

        if ( ImportStatus.SUCCESS.equals( summary.getStatus() ) )
        {
            postCreateEntity( parsed );

            if ( summary.getImportCount().getImported() == 1 && summary.getLastImported() != null )
            {
                response.setHeader( "Location", contextService.getApiPath() + getSchema().getRelativeApiEndpoint()
                    + "/" + summary.getLastImported() );
            }
        }

        renderService.toXml( response.getOutputStream(), summary );
    }

    @RequestMapping( method = RequestMethod.POST, consumes = "application/json" )
    public void postJsonObject( ImportOptions importOptions, HttpServletRequest request, HttpServletResponse response )
        throws Exception
    {
        if ( !aclService.canCreate( currentUserService.getCurrentUser(), getEntityClass() ) )
        {
            throw new CreateAccessDeniedException( "You don't have the proper permissions to create this object." );
        }

        T parsed = renderService.fromJson( request.getInputStream(), getEntityClass() );

        preCreateEntity( parsed );

        ImportTypeSummary summary = importService.importObject( currentUserService.getCurrentUser().getUid(), parsed,
            ImportStrategy.CREATE, importOptions.getMergeStrategy() );

        if ( ImportStatus.SUCCESS.equals( summary.getStatus() ) )
        {
            postCreateEntity( parsed );

            if ( summary.getImportCount().getImported() == 1 && summary.getLastImported() != null )
            {
                response.setHeader( "Location", contextService.getApiPath() + getSchema().getRelativeApiEndpoint()
                    + "/" + summary.getLastImported() );
            }
        }

        renderService.toJson( response.getOutputStream(), summary );
    }

    //--------------------------------------------------------------------------
    // PUT
    //--------------------------------------------------------------------------

    @RequestMapping( value = "/{uid}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE } )
    public void putXmlObject( ImportOptions importOptions, @PathVariable( "uid" ) String pvUid, HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        List<T> objects = getEntity( pvUid );

        if ( objects.isEmpty() )
        {
            ContextUtils.notFoundResponse( response, getEntityName() + " does not exist: " + pvUid );
            return;
        }

        if ( !aclService.canUpdate( currentUserService.getCurrentUser(), objects.get( 0 ) ) )
        {
            throw new UpdateAccessDeniedException( "You don't have the proper permissions to update this object." );
        }

        T parsed = renderService.fromXml( request.getInputStream(), getEntityClass() );
        ((BaseIdentifiableObject) parsed).setUid( pvUid );

        preUpdateEntity( parsed );

        ImportTypeSummary summary = importService.importObject( currentUserService.getCurrentUser().getUid(), parsed,
            ImportStrategy.UPDATE, importOptions.getMergeStrategy() );

        if ( ImportStatus.SUCCESS.equals( summary.getStatus() ) )
        {
            postUpdateEntity( parsed );
        }

        renderService.toXml( response.getOutputStream(), summary );
    }

    @RequestMapping( value = "/{uid}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE )
    public void putJsonObject( ImportOptions importOptions, @PathVariable( "uid" ) String pvUid, HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        List<T> objects = getEntity( pvUid );

        if ( objects.isEmpty() )
        {
            ContextUtils.notFoundResponse( response, getEntityName() + " does not exist: " + pvUid );
            return;
        }

        if ( !aclService.canUpdate( currentUserService.getCurrentUser(), objects.get( 0 ) ) )
        {
            throw new UpdateAccessDeniedException( "You don't have the proper permissions to update this object." );
        }

        T parsed = renderService.fromJson( request.getInputStream(), getEntityClass() );
        ((BaseIdentifiableObject) parsed).setUid( pvUid );

        preUpdateEntity( parsed );

        ImportTypeSummary summary = importService.importObject( currentUserService.getCurrentUser().getUid(), parsed,
            ImportStrategy.UPDATE, importOptions.getMergeStrategy() );

        if ( ImportStatus.SUCCESS.equals( summary.getStatus() ) )
        {
            postUpdateEntity( parsed );
        }

        renderService.toJson( response.getOutputStream(), summary );
    }

    //--------------------------------------------------------------------------
    // DELETE
    //--------------------------------------------------------------------------

    @RequestMapping( value = "/{uid}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE )
    public void deleteObject( @PathVariable( "uid" ) String pvUid, HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        List<T> objects = getEntity( pvUid );

        if ( objects.isEmpty() )
        {
            ContextUtils.notFoundResponse( response, getEntityName() + " does not exist: " + pvUid );
            return;
        }

        if ( !aclService.canDelete( currentUserService.getCurrentUser(), objects.get( 0 ) ) )
        {
            throw new DeleteAccessDeniedException( "You don't have the proper permissions to delete this object." );
        }

        response.setStatus( HttpServletResponse.SC_NO_CONTENT );

        preDeleteEntity( objects.get( 0 ) );
        manager.delete( objects.get( 0 ) );
        postDeleteEntity();
    }

    //--------------------------------------------------------------------------
    // Identifiable object collections add, delete
    //--------------------------------------------------------------------------

    @RequestMapping( value = "/{uid}/{property}/{itemId}", method = RequestMethod.GET )
    public @ResponseBody RootNode getCollectionItem(
        @PathVariable( "uid" ) String pvUid,
        @PathVariable( "property" ) String pvProperty,
        @PathVariable( "itemId" ) String pvItemId,
        @RequestParam Map<String, String> parameters,
        TranslateOptions translateOptions,
        HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        RootNode rootNode = getObjectInternal( pvUid, parameters, Lists.<String>newArrayList(), Lists.newArrayList( pvProperty + "[:all]" ), translateOptions );

        // TODO optimize this using field filter (collection filtering)
        if ( !rootNode.getChildren().isEmpty() && rootNode.getChildren().get( 0 ).isCollection() )
        {
            for ( Node node : rootNode.getChildren().get( 0 ).getChildren() )
            {
                if ( node.isComplex() )
                {
                    for ( Node child : node.getChildren() )
                    {
                        if ( child.isSimple() && child.getName().equals( "id" ) )
                        {
                            if ( !((SimpleNode) child).getValue().equals( pvItemId ) )
                            {
                                rootNode.getChildren().get( 0 ).removeChild( node );
                            }
                        }
                    }
                }
            }
        }

        return rootNode;
    }

    @RequestMapping( value = "/{uid}/{property}/{itemId}", method = { RequestMethod.POST, RequestMethod.PUT } )
    @SuppressWarnings( "unchecked" )
    public void addCollectionItem(
        @PathVariable( "uid" ) String pvUid,
        @PathVariable( "property" ) String pvProperty,
        @PathVariable( "itemId" ) String pvItemId,
        HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        List<T> objects = getEntity( pvUid );

        if ( objects.isEmpty() )
        {
            ContextUtils.notFoundResponse( response, getEntityName() + " does not exist: " + pvUid );
            return;
        }

        if ( !getSchema().haveProperty( pvProperty ) )
        {
            ContextUtils.notFoundResponse( response, "Property " + pvProperty + " does not exist on " + getEntityName() );
            return;
        }

        Property property = getSchema().getProperty( pvProperty );

        if ( !property.isCollection() || !property.isIdentifiableObject() )
        {
            ContextUtils.conflictResponse( response, "Only adds within identifiable collection are allowed." );
            return;
        }

        IdentifiableObject inverseObject = manager.getNoAcl( (Class<? extends IdentifiableObject>) property.getItemKlass(), pvItemId );
        IdentifiableObject owningObject = objects.get( 0 );

        if ( inverseObject == null )
        {
            ContextUtils.notFoundResponse( response, "Collection " + pvProperty + " does not have an item with ID: " + pvItemId );
            return;
        }

        Collection<IdentifiableObject> collection;

        if ( property.isOwner() )
        {
            collection = (Collection<IdentifiableObject>) property.getGetterMethod().invoke( owningObject );
        }
        else
        {
            Schema owningSchema = getSchema( property.getItemKlass() );
            Property owningProperty = owningSchema.propertyByRole( property.getOwningRole() );
            collection = (Collection<IdentifiableObject>) owningProperty.getGetterMethod().invoke( inverseObject );

            IdentifiableObject o = owningObject;
            owningObject = inverseObject;
            inverseObject = o;
        }

        response.setStatus( HttpServletResponse.SC_NO_CONTENT );

        if ( !aclService.canUpdate( currentUserService.getCurrentUser(), owningObject ) )
        {
            throw new DeleteAccessDeniedException( "You don't have the proper permissions to update this object." );
        }

        // if it already contains this object, don't add it. It might be a list and not set, and we don't want duplicates.
        if ( collection.contains( inverseObject ) )
        {
            response.setStatus( HttpServletResponse.SC_NO_CONTENT );
            return; // nothing to do, just return with OK
        }

        collection.add( inverseObject );

        manager.update( owningObject );
        manager.refresh( inverseObject );
    }

    @RequestMapping( value = "/{uid}/{property}/{itemId}", method = RequestMethod.DELETE )
    @SuppressWarnings( "unchecked" )
    public void deleteCollectionItem(
        @PathVariable( "uid" ) String pvUid,
        @PathVariable( "property" ) String pvProperty,
        @PathVariable( "itemId" ) String pvItemId,
        HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        List<T> objects = getEntity( pvUid );

        if ( objects.isEmpty() )
        {
            ContextUtils.notFoundResponse( response, getEntityName() + " does not exist: " + pvUid );
            return;
        }

        if ( !getSchema().haveProperty( pvProperty ) )
        {
            ContextUtils.notFoundResponse( response, "Property " + pvProperty + " does not exist on " + getEntityName() );
            return;
        }

        Property property = getSchema().getProperty( pvProperty );

        if ( !property.isCollection() || !property.isIdentifiableObject() )
        {
            ContextUtils.conflictResponse( response, "Only deletes within identifiable collection are allowed." );
            return;
        }

        Collection<IdentifiableObject> collection;
        IdentifiableObject inverseObject = manager.getNoAcl( (Class<? extends IdentifiableObject>) property.getItemKlass(), pvItemId );
        IdentifiableObject owningObject = objects.get( 0 );

        if ( property.isOwner() )
        {
            collection = (Collection<IdentifiableObject>) property.getGetterMethod().invoke( owningObject );
        }
        else
        {
            Schema owningSchema = getSchema( property.getItemKlass() );
            Property owningProperty = owningSchema.propertyByRole( property.getOwningRole() );
            collection = (Collection<IdentifiableObject>) owningProperty.getGetterMethod().invoke( inverseObject );

            IdentifiableObject o = owningObject;
            owningObject = inverseObject;
            inverseObject = o;
        }

        if ( !aclService.canUpdate( currentUserService.getCurrentUser(), owningObject ) )
        {
            throw new DeleteAccessDeniedException( "You don't have the proper permissions to delete this object." );
        }

        if ( !collection.contains( inverseObject ) )
        {
            ContextUtils.notFoundResponse( response, "Collection " + pvProperty + " does not have an item with ID: " + pvItemId );
            return;
        }

        collection.remove( inverseObject );

        response.setStatus( HttpServletResponse.SC_NO_CONTENT );

        manager.update( owningObject );
        manager.refresh( inverseObject );
    }

    //--------------------------------------------------------------------------
    // Hooks
    //--------------------------------------------------------------------------

    /**
     * Override to process entities after it has been retrieved from
     * storage and before it is returned to the view. Entities is null-safe.
     */

    protected void postProcessEntities( List<T> entityList, WebOptions options, Map<String, String> parameters )
    {
    }

    /**
     * Override to process entities after it has been retrieved from
     * storage and before it is returned to the view. Entities is null-safe.
     */
    protected void postProcessEntities( List<T> entityList )
    {
    }

    /**
     * Override to process a single entity after it has been retrieved from
     * storage and before it is returned to the view. Entity is null-safe.
     */
    protected void postProcessEntity( T entity ) throws Exception
    {
    }

    /**
     * Override to process a single entity after it has been retrieved from
     * storage and before it is returned to the view. Entity is null-safe.
     */
    protected void postProcessEntity( T entity, WebOptions options, Map<String, String> parameters ) throws Exception
    {
    }

    protected void preCreateEntity( T entity )
    {
    }

    protected void postCreateEntity( T entity )
    {
    }

    protected void preUpdateEntity( T entity )
    {
    }

    protected void postUpdateEntity( T entity )
    {
    }

    protected void preDeleteEntity( T entity )
    {
    }

    protected void postDeleteEntity()
    {
    }

    //--------------------------------------------------------------------------
    // Helpers
    //--------------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    protected List<T> getEntityList( WebMetaData metaData, WebOptions options, List<String> filters, List<Order> orders )
    {
        List<T> entityList;
        boolean haveFilters = !filters.isEmpty();
        Query query = queryService.getQueryFromUrl( getEntityClass(), filters, orders );
        query.setDefaultOrder();

        if ( options.getOptions().containsKey( "query" ) )
        {
            entityList = Lists.newArrayList( manager.filter( getEntityClass(), options.getOptions().get( "query" ) ) );
        }
        else if ( options.hasPaging() && !haveFilters )
        {
            int count = queryService.count( query );

            Pager pager = new Pager( options.getPage(), count, options.getPageSize() );
            metaData.setPager( pager );

            query.setFirstResult( pager.getOffset() );
            query.setMaxResults( pager.getPageSize() );
            entityList = (List<T>) queryService.query( query ).getItems();
        }
        else
        {
            entityList = (List<T>) queryService.query( query ).getItems();
        }

        return entityList;
    }

    /**
     * Should not be overridden, instead override {@link getEntity(String, WebOptions}.
     */
    protected final List<T> getEntity( String uid )
    {
        return getEntity( uid, new WebOptions( new HashMap<String, String>() ) );
    }

    protected List<T> getEntity( String uid, WebOptions options )
    {
        ArrayList<T> list = new ArrayList<>();
        Optional<T> identifiableObject = Optional.fromNullable( manager.getNoAcl( getEntityClass(), uid ) );

        if ( identifiableObject.isPresent() )
        {
            list.add( identifiableObject.get() );
        }

        return list; //TODO consider ACL
    }

    private Schema schema;

    protected Schema getSchema()
    {
        if ( schema == null )
        {
            schema = schemaService.getDynamicSchema( getEntityClass() );
        }

        return schema;
    }

    protected Schema getSchema( Class<?> klass )
    {
        return schemaService.getDynamicSchema( klass );
    }

    protected void addAccessProperties( List<T> objects )
    {
        User user = currentUserService.getCurrentUser();

        for ( T object : objects )
        {
            ((BaseIdentifiableObject) object).setAccess( aclService.getAccess( object, user ) );
        }
    }

    protected void handleLinksAndAccess( WebOptions options, List<T> entityList, boolean deepScan )
    {
        if ( options != null && options.hasLinks() )
        {
            linkService.generateLinks( entityList, deepScan );
        }

        if ( entityList != null && aclService.isSupported( getEntityClass() ) )
        {
            addAccessProperties( entityList );
        }
    }

    private InclusionStrategy.Include getInclusionStrategy( String inclusionStrategy )
    {
        if ( inclusionStrategy != null )
        {
            Optional<InclusionStrategy.Include> optional = Enums.getIfPresent( InclusionStrategy.Include.class, inclusionStrategy );

            if ( optional.isPresent() )
            {
                return optional.get();
            }
        }

        return InclusionStrategy.Include.NON_NULL;
    }

    /**
     * Serializes an object, tries to guess output format using this order.
     *
     * @param request  HttpServletRequest from current session
     * @param response HttpServletResponse from current session
     * @param object   Object to serialize
     */
    protected void serialize( HttpServletRequest request, HttpServletResponse response, Object object ) throws IOException
    {
        String type = request.getHeader( "Accept" );
        type = !StringUtils.isEmpty( type ) ? type : request.getContentType();
        type = !StringUtils.isEmpty( type ) ? type : MediaType.APPLICATION_JSON_VALUE;

        // allow type to be overridden by path extension
        if ( request.getPathInfo().endsWith( ".json" ) )
        {
            type = MediaType.APPLICATION_JSON_VALUE;
        }
        else if ( request.getPathInfo().endsWith( ".xml" ) )
        {
            type = MediaType.APPLICATION_XML_VALUE;
        }

        if ( isCompatibleWith( type, MediaType.APPLICATION_JSON ) )
        {
            renderService.toJson( response.getOutputStream(), object );
        }
        else if ( isCompatibleWith( type, MediaType.APPLICATION_XML ) )
        {
            renderService.toXml( response.getOutputStream(), object );
        }
    }

    /**
     * Deserializes a payload from the request, handles JSON/XML payloads
     *
     * @param request HttpServletRequest from current session
     * @return Parsed entity or null if invalid type
     */
    protected T deserialize( HttpServletRequest request ) throws IOException
    {
        String type = request.getContentType();
        type = !StringUtils.isEmpty( type ) ? type : MediaType.APPLICATION_JSON_VALUE;

        // allow type to be overridden by path extension
        if ( request.getPathInfo().endsWith( ".json" ) )
        {
            type = MediaType.APPLICATION_JSON_VALUE;
        }
        else if ( request.getPathInfo().endsWith( ".xml" ) )
        {
            type = MediaType.APPLICATION_XML_VALUE;
        }

        if ( isCompatibleWith( type, MediaType.APPLICATION_JSON ) )
        {
            return renderService.fromJson( request.getInputStream(), getEntityClass() );
        }
        else if ( isCompatibleWith( type, MediaType.APPLICATION_XML ) )
        {
            return renderService.fromXml( request.getInputStream(), getEntityClass() );
        }

        return null;
    }

    /**
     * Are we receiving JSON data?
     *
     * @param request HttpServletRequest from current session
     * @return true if JSON compatible
     */
    protected boolean isJson( HttpServletRequest request )
    {
        String type = request.getContentType();
        type = !StringUtils.isEmpty( type ) ? type : MediaType.APPLICATION_JSON_VALUE;

        // allow type to be overridden by path extension
        if ( request.getPathInfo().endsWith( ".json" ) )
        {
            type = MediaType.APPLICATION_JSON_VALUE;
        }

        return isCompatibleWith( type, MediaType.APPLICATION_JSON );
    }

    /**
     * Are we receiving XML data?
     *
     * @param request HttpServletRequest from current session
     * @return true if XML compatible
     */
    protected boolean isXml( HttpServletRequest request )
    {
        String type = request.getContentType();
        type = !StringUtils.isEmpty( type ) ? type : MediaType.APPLICATION_JSON_VALUE;

        // allow type to be overridden by path extension
        if ( request.getPathInfo().endsWith( ".xml" ) )
        {
            type = MediaType.APPLICATION_XML_VALUE;
        }

        return isCompatibleWith( type, MediaType.APPLICATION_XML );
    }

    protected boolean isCompatibleWith( String type, MediaType mediaType )
    {
        return !StringUtils.isEmpty( type ) && MediaType.parseMediaType( type ).isCompatibleWith( mediaType );
    }

    //--------------------------------------------------------------------------
    // Reflection helpers
    //--------------------------------------------------------------------------

    private Class<T> entityClass;

    private String entityName;

    private String entitySimpleName;

    @SuppressWarnings( "unchecked" )
    protected Class<T> getEntityClass()
    {
        if ( entityClass == null )
        {
            Type[] actualTypeArguments = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
            entityClass = (Class<T>) actualTypeArguments[0];
        }

        return entityClass;
    }

    protected String getEntityName()
    {
        if ( entityName == null )
        {
            entityName = getEntityClass().getName();
        }

        return entityName;
    }

    protected String getEntitySimpleName()
    {
        if ( entitySimpleName == null )
        {
            entitySimpleName = getEntityClass().getSimpleName();
        }

        return entitySimpleName;
    }

    @SuppressWarnings( "unchecked" )
    protected T getEntityInstance()
    {
        try
        {
            return (T) Class.forName( getEntityName() ).newInstance();
        }
        catch ( InstantiationException | IllegalAccessException | ClassNotFoundException ex )
        {
            throw new RuntimeException( ex );
        }
    }
}
