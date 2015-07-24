package org.hisp.dhis.attribute.hibernate;

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

import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.attribute.Attribute;
import org.hisp.dhis.attribute.AttributeStore;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mortenoh
 */
public class HibernateAttributeStore
    extends HibernateIdentifiableObjectStore<Attribute>
    implements AttributeStore
{
    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getDataElementAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "dataElementAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getDataElementGroupAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "dataElementGroupAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getIndicatorAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "indicatorAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getIndicatorGroupAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "indicatorGroupAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getDataSetAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "dataSetAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getOrganisationUnitAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "organisationUnitAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getOrganisationUnitGroupAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "organisationUnitGroupAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getOrganisationUnitGroupSetAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "organisationUnitGroupSetAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getUserAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "userAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getUserGroupAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "userGroupAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getProgramAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "programAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getProgramStageAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "programStageAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getTrackedEntityAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "trackedEntityAttribute", true ) ).list() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<Attribute> getTrackedEntityAttributeAttributes()
    {
        return new HashSet<Attribute>( getCriteria( Restrictions.eq( "trackedEntityAttributeAttribute", true ) ).list() );
    }
}
