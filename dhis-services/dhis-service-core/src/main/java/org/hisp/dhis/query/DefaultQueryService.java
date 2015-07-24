package org.hisp.dhis.query;

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

import com.google.common.collect.Lists;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Default implementation of QueryService which works with IdObjects.
 *
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class DefaultQueryService<T extends IdentifiableObject> implements QueryService
{
    @Autowired
    private QueryEngine<T> queryEngine;

    @Autowired
    private SchemaService schemaService;

    @Override
    public Result query( Query query )
    {
        List<T> objects = queryEngine.query( query );
        return new Result( objects );
    }

    @Override
    public Result query( Query query, ResultTransformer transformer )
    {
        List<T> objects = queryEngine.query( query );

        if ( transformer != null )
        {
            return transformer.transform( new MutableResult( objects ) );
        }

        return new Result( objects );
    }

    @Override
    public int count( Query query )
    {
        return queryEngine.count( query );
    }

    @Override
    public Query getQueryFromUrl( Class<?> klass, List<String> filters, List<Order> orders )
    {
        Query query = Query.from( schemaService.getDynamicSchema( klass ) );
        query.add( getRestrictions( query.getSchema(), filters ) );
        query.addOrders( orders );

        return query;
    }

    //--------------------------------------------------------------------------
    // Helpers
    //--------------------------------------------------------------------------

    private List<Restriction> getRestrictions( Schema schema, List<String> filters )
    {
        List<Restriction> restrictions = new ArrayList<>();
        List<String> candidates = getRestrictionCandidates( schema, filters );

        if ( candidates.isEmpty() )
        {
            return restrictions;
        }

        for ( String candidate : candidates )
        {
            restrictions.add( getRestriction( schema, candidate ) );
        }

        return restrictions;
    }

    private List<String> getRestrictionCandidates( Schema schema, List<String> filters )
    {
        List<String> candidates = new ArrayList<>();

        Iterator<String> iterator = filters.iterator();

        while ( iterator.hasNext() )
        {
            String candidate = iterator.next();

            if ( !candidate.contains( "." ) && getRestriction( schema, candidate ) != null )
            {
                candidates.add( candidate );
                iterator.remove();
            }
        }

        return candidates;
    }

    private Restriction getRestriction( Schema schema, String filter )
    {
        if ( filter == null )
        {
            return null;
        }

        String[] split = filter.split( ":" );

        if ( split.length != 3 )
        {
            return null;
        }

        Property property = schema.getProperty( split[0] );

        if ( property == null || !property.isPersisted() || !property.isSimple() )
        {
            return null;
        }

        switch ( split[1] )
        {
            case "eq":
            {
                return Restrictions.eq( split[0], QueryUtils.getValue( property.getKlass(), split[2] ) );
            }
            case "ne":
            {
                return Restrictions.ne( split[0], QueryUtils.getValue( property.getKlass(), split[2] ) );
            }
            case "neq":
            {
                return Restrictions.ne( split[0], QueryUtils.getValue( property.getKlass(), split[2] ) );
            }
            case "gt":
            {
                return Restrictions.gt( split[0], QueryUtils.getValue( property.getKlass(), split[2] ) );
            }
            case "lt":
            {
                return Restrictions.lt( split[0], QueryUtils.getValue( property.getKlass(), split[2] ) );
            }
            case "gte":
            {
                return Restrictions.ge( split[0], QueryUtils.getValue( property.getKlass(), split[2] ) );
            }
            case "ge":
            {
                return Restrictions.ge( split[0], QueryUtils.getValue( property.getKlass(), split[2] ) );
            }
            case "lte":
            {
                return Restrictions.le( split[0], QueryUtils.getValue( property.getKlass(), split[2] ) );
            }
            case "le":
            {
                return Restrictions.le( split[0], QueryUtils.getValue( property.getKlass(), split[2] ) );
            }
            case "like":
            {
                return Restrictions.like( split[0], "%" + split[2] + "%" );
            }
            case "ilike":
            {
                return Restrictions.ilike( split[0], "%" + split[2] + "%" );
            }
            case "in":
            {
                return Restrictions.in( split[0], parseInOperator( split[2] ) );
            }
        }

        return null;
    }

    private Collection<String> parseInOperator( String value )
    {
        if ( value == null || !value.startsWith( "[" ) || !value.endsWith( "]" ) )
        {
            return Lists.newArrayList();
        }

        String[] split = value.substring( 1, value.length() - 1 ).split( "," );

        return Lists.newArrayList( split );
    }
}
