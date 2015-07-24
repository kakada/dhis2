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

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.Schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class Query
{
    private final Schema schema;

    private List<Order> orders = new ArrayList<>();

    private List<Restriction> restrictions = new ArrayList<>();

    private Integer firstResult;

    private Integer maxResults;

    public static Query from( Schema schema )
    {
        return new Query( schema );
    }

    private Query( Schema schema )
    {
        this.schema = schema;
    }

    public Schema getSchema()
    {
        return schema;
    }

    public List<Order> getOrders()
    {
        return orders;
    }

    public void setOrders( List<Order> orders )
    {
        this.orders = orders;
    }

    public List<Restriction> getRestrictions()
    {
        return restrictions;
    }

    public void setRestrictions( List<Restriction> restrictions )
    {
        this.restrictions = restrictions;
    }

    public Integer getFirstResult()
    {
        return firstResult;
    }

    public Query setFirstResult( Integer firstResult )
    {
        this.firstResult = firstResult;
        return this;
    }

    public Integer getMaxResults()
    {
        return maxResults;
    }

    public Query setMaxResults( Integer maxResults )
    {
        this.maxResults = maxResults;
        return this;
    }

    // Builder
    public Query add( Restriction... restrictions )
    {
        for ( Restriction restriction : restrictions )
        {
            if ( restriction == null || !schema.haveProperty( restriction.getPath() ) )
            {
                continue;
            }

            if ( restriction.getParameters().size() > restriction.getOperator().getMax()
                || restriction.getParameters().size() < restriction.getOperator().getMin() )
            {
                continue;
            }

            this.restrictions.add( restriction );
        }

        return this;
    }

    public Query add( Collection<Restriction> restrictions )
    {
        this.restrictions.addAll( restrictions );
        return this;
    }

    public Query addOrder( Order... orders )
    {
        for ( Order order : orders )
        {
            if ( order != null )
            {
                this.orders.add( order );
            }
        }

        return this;
    }

    public Query addOrders( Collection<Order> orders )
    {
        this.orders.addAll( orders );
        return this;
    }

    public Query forceDefaultOrder()
    {
        orders.clear();
        return setDefaultOrder();
    }

    public Query setDefaultOrder()
    {
        if ( !orders.isEmpty() )
        {
            return this;
        }

        Optional<Property> name = Optional.fromNullable( schema.getPersistedProperty( "name" ) );
        Optional<Property> created = Optional.fromNullable( schema.getPersistedProperty( "created" ) );

        if ( name.isPresent() )
        {
            addOrder( Order.asc( name.get() ) );
        }
        else if ( created.isPresent() )
        {
            addOrder( Order.desc( created.get() ) );
        }

        return this;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this )
            .add( "firstResult", firstResult )
            .add( "maxResults", maxResults )
            .add( "orders", orders )
            .add( "restrictions", restrictions )
            .toString();
    }
}
