package org.hisp.dhis.trackedentity;

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

import java.util.Collection;
import java.util.HashSet;

import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Abyot Asalefew
 */
@Transactional
public class DefaultTrackedEntityAttributeService
    implements TrackedEntityAttributeService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityAttributeStore attributeStore;

    public void setAttributeStore( TrackedEntityAttributeStore attributeStore )
    {
        this.attributeStore = attributeStore;
    }

    private ProgramService programService;

    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }

    // -------------------------------------------------------------------------
    // Implementation methods
    // -------------------------------------------------------------------------

    @Override
    public void deleteTrackedEntityAttribute( TrackedEntityAttribute attribute )
    {
        attributeStore.delete( attribute );
    }

    @Override
    public Collection<TrackedEntityAttribute> getAllTrackedEntityAttributes()
    {
        return attributeStore.getAll();
    }

    @Override
    public TrackedEntityAttribute getTrackedEntityAttribute( int id )
    {
        return attributeStore.get( id );
    }

    @Override
    public int addTrackedEntityAttribute( TrackedEntityAttribute attribute )
    {
        return attributeStore.save( attribute );
    }

    @Override
    public void updateTrackedEntityAttribute( TrackedEntityAttribute attribute )
    {
        attributeStore.update( attribute );
    }

    @Override
    public TrackedEntityAttribute getTrackedEntityAttributeByName( String name )
    {
        return attributeStore.getByName( name );
    }

    @Override
    public TrackedEntityAttribute getTrackedEntityAttributeByShortName( String shortName )
    {
        return attributeStore.getByShortName( shortName );
    }

    @Override
    public TrackedEntityAttribute getTrackedEntityAttributeByCode( String code )
    {
        return attributeStore.getByShortName( code );
    }

    @Override
    public Collection<TrackedEntityAttribute> getOptionalAttributesWithoutGroup()
    {
        return attributeStore.getOptionalAttributesWithoutGroup();
    }

    @Override
    public Collection<TrackedEntityAttribute> getTrackedEntityAttributesWithoutGroup()
    {
        return attributeStore.getWithoutGroup();
    }

    @Override
    public TrackedEntityAttribute getTrackedEntityAttribute( String uid )
    {
        return attributeStore.getByUid( uid );
    }

    @Override
    public Collection<TrackedEntityAttribute> getTrackedEntityAttributesByDisplayOnVisitSchedule(
        boolean displayOnVisitSchedule )
    {
        return attributeStore.getByDisplayOnVisitSchedule( displayOnVisitSchedule );
    }

    @Override
    public Collection<TrackedEntityAttribute> getTrackedEntityAttributesWithoutProgram()
    {
        Collection<TrackedEntityAttribute> result = attributeStore.getAll();

        Collection<Program> programs = programService.getAllPrograms();

        if ( result != null )
        {
            for ( Program program : programs )
            {
                result.removeAll( program.getProgramAttributes() );
            }

            return result;
        }

        return new HashSet<>();
    }

    @Override
    public Collection<TrackedEntityAttribute> getTrackedEntityAttributesDisplayInList()
    {
        return attributeStore.getDisplayInList();
    }

    @Override
    public Collection<TrackedEntityAttribute> getTrackedEntityAttributesBetweenByName( String name, int min, int max )
    {
        return attributeStore.getAllLikeName( name, min, max );
    }

    @Override
    public int getTrackedEntityAttributeCount()
    {
        return attributeStore.getCount();
    }

    @Override
    public Collection<TrackedEntityAttribute> getTrackedEntityAttributesBetween( int min, int max )
    {
        return attributeStore.getAllOrderedName( min, max );
    }

    @Override
    public int getTrackedEntityAttributeCountByName( String name )
    {
        return attributeStore.getCountLikeName( name );
    }  
}
