package org.hisp.dhis.dataelement.hibernate;

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

import static org.hisp.dhis.system.util.ConversionUtils.getIdentifiers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.common.ListMap;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementDomain;
import org.hisp.dhis.dataelement.DataElementStore;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.system.util.TextUtils;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * @author Torgeir Lorange Ostby
 */
public class HibernateDataElementStore
    extends HibernateIdentifiableObjectStore<DataElement>
    implements DataElementStore
{
    private static final Log log = LogFactory.getLog( HibernateDataElementStore.class );

    // -------------------------------------------------------------------------
    // DataElement
    // -------------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> searchDataElementsByName( String key )
    {
        return getCriteria( Restrictions.ilike( "name", "%" + key + "%" ) ).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getAggregateableDataElements()
    {
        Set<String> types = new HashSet<>();

        types.add( DataElement.VALUE_TYPE_INT );
        types.add( DataElement.VALUE_TYPE_BOOL );
        
        return getCriteria( Restrictions.in( "type", types ) ).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getDataElementsByAggregationOperator( String aggregationOperator )
    {
        return getCriteria( Restrictions.eq( "aggregationOperator", aggregationOperator ) ).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getDataElementsByType( String type )
    {
        return getCriteria( Restrictions.eq( "type", type ) ).list();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getDataElementsByDomainType( DataElementDomain domainType )
    {
        return getCriteria( Restrictions.eq( "domainType", domainType ) ).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getDataElementsByDomainType( DataElementDomain domainType, int first, int max )
    {
        Criteria criteria = getCriteria();
        criteria.add( Restrictions.eq( "domainType", domainType ) );

        criteria.setFirstResult( first );
        criteria.setMaxResults( max );
        criteria.addOrder( Order.asc( "name" ) );

        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getDataElementByCategoryCombo( DataElementCategoryCombo categoryCombo )
    {
        return getCriteria( Restrictions.eq( "categoryCombo", categoryCombo ) ).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getDataElementsWithGroupSets()
    {
        String hql = "from DataElement d where d.groupSets.size > 0";

        return getQuery( hql ).list();
    }

    @Override
    public void setZeroIsSignificantForDataElements( Collection<Integer> dataElementIds )
    {
        String hql = "update DataElement set zeroIsSignificant = false";

        Query query = getQuery( hql );

        query.executeUpdate();
        
        //TODO improve

        if ( !dataElementIds.isEmpty() )
        {
            hql = "update DataElement set zeroIsSignificant=true where id in (:dataElementIds)";

            query = getQuery( hql );
            query.setParameterList( "dataElementIds", dataElementIds );

            query.executeUpdate();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getDataElementsByZeroIsSignificant( boolean zeroIsSignificant )
    {
        Criteria criteria = getCriteria();
        criteria.add( Restrictions.eq( "zeroIsSignificant", zeroIsSignificant ) );
        criteria.add( Restrictions.eq( "type", DataElement.VALUE_TYPE_INT ) );

        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getDataElementsWithoutGroups()
    {
        String hql = "from DataElement d where d.groups.size = 0";

        return getQuery( hql ).setCacheable( true ).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getDataElementsWithoutDataSets()
    {
        String hql = "from DataElement d where d.dataSets.size = 0 and d.domainType =:domainType";

        return getQuery( hql ).setParameter( "domainType", DataElementDomain.AGGREGATE ).setCacheable( true ).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getDataElementsWithDataSets()
    {
        String hql = "from DataElement d where d.dataSets.size > 0";

        return getQuery( hql ).setCacheable( true ).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getDataElementsByDataSets( Collection<DataSet> dataSets )
    {
        String hql = "select distinct de from DataElement de join de.dataSets ds where ds.id in (:ids)";

        return getQuery( hql ).setParameterList( "ids", getIdentifiers( DataSet.class, dataSets ) ).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> getDataElementsByAggregationLevel( int aggregationLevel )
    {
        String hql = "from DataElement de join de.aggregationLevels al where al = :aggregationLevel";

        return getQuery( hql ).setInteger( "aggregationLevel", aggregationLevel ).list();
    }

    @Override
    public ListMap<String, String> getDataElementCategoryOptionComboMap( Set<String> dataElementUids )
    {
        final String sql =
            "select dataelementuid, categoryoptioncombouid " +
                "from _dataelementcategoryoptioncombo " +
                "where dataelementuid in (" + TextUtils.getQuotedCommaDelimitedString( dataElementUids ) + ")";

        final ListMap<String, String> map = new ListMap<>();

        try
        {
            jdbcTemplate.query( sql, new RowCallbackHandler()
            {
                @Override
                public void processRow( ResultSet rs )
                    throws SQLException
                {
                    String de = rs.getString( 1 );
                    String coc = rs.getString( 2 );

                    map.putValue( de, coc );
                }
            } );
        }
        catch ( BadSqlGrammarException ex )
        {
            log.error( "Resource table _dataelementcategoryoptioncomboname does not exist, please generate it" );
            return new ListMap<>();
        }

        return map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DataElement> get( DataSet dataSet, String key, Integer max )
    {
        String hql = "select dataElement from DataSet dataSet inner join dataSet.dataElements as dataElement where dataSet.id = :dataSetId ";

        if ( key != null )
        {
            hql += " and lower(dataElement.name) like lower('%" + key + "%') ";
        }

        Query query = getQuery( hql );
        query.setInteger( "dataSetId", dataSet.getId() );
        
        if ( max != null )
        {
            query.setMaxResults( max );
        }

        return query.list();
    }

    @Override
    public int getCountByDomainType( DataElementDomain domainType )
    {
        return getCriteria( Restrictions.eq( "domainType", domainType ) ).list().size(); // TODO improve
    }
}
