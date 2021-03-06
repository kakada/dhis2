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

/**
 * @author Chau Thu Tran
 * @version $Id$
 */
public interface TrackedEntityAttributeGroupService
{
    String ID = TrackedEntityAttributeGroupService.class.getName();

    /**
     * Adds an {@link TrackedEntityAttributeGroup}
     * 
     * @param TrackedEntityAttributeGroup The to TrackedEntityAttributeGroup
     *        add.
     * 
     * @return A generated unique id of the added
     *         {@link TrackedEntityAttributeGroup}.
     */
    int addTrackedEntityAttributeGroup( TrackedEntityAttributeGroup TrackedEntityAttributeGroup );

    /**
     * Deletes a {@link TrackedEntityAttributeGroup}.
     * 
     * @param TrackedEntityAttributeGroup the TrackedEntityAttributeGroup to
     *        delete.
     */
    void deleteTrackedEntityAttributeGroup( TrackedEntityAttributeGroup TrackedEntityAttributeGroup );

    /**
     * Updates a {@link TrackedEntityAttributeGroup}.
     * 
     * @param TrackedEntityAttributeGroup the TrackedEntityAttributeGroup to
     *        update.
     */
    void updateTrackedEntityAttributeGroup( TrackedEntityAttributeGroup TrackedEntityAttributeGroup );

    /**
     * Returns a {@link TrackedEntityAttributeGroup}.
     * 
     * @param id the id of the TrackedEntityAttributeGroup to return.
     * 
     * @return the TrackedEntityAttributeGroup with the given id
     */
    TrackedEntityAttributeGroup getTrackedEntityAttributeGroup( int id );

    /**
     * Returns a {@link TrackedEntityAttributeGroup}.
     * 
     * @param uid the id of the TrackedEntityAttributeGroup to return.
     * 
     * @return the TrackedEntityAttributeGroup with the given id
     */
    TrackedEntityAttributeGroup getTrackedEntityAttributeGroup( String uid );

    /**
     * Returns a {@link TrackedEntityAttributeGroup} with a given name.
     * 
     * @param name the name of the TrackedEntityAttributeGroup to return.
     * 
     * @return the TrackedEntityAttributeGroup with the given name, or null if
     *         no match.
     */
    TrackedEntityAttributeGroup getTrackedEntityAttributeGroupByName( String name );

    /**
     * Returns all {@link TrackedEntityAttributeGroup}
     * 
     * @return a collection of all TrackedEntityAttributeGroup, or an empty
     *         collection if there are no TrackedEntityAttributeGroups.
     */
    Collection<TrackedEntityAttributeGroup> getAllTrackedEntityAttributeGroups();

    /**
     * Returns The number of TrackedEntityAttributeGroups with the key searched
     * 
     * @param name Keyword for searching by name
     * 
     * @return A number
     * 
     */
    Integer getTrackedEntityAttributeGroupCountByName( String name );

    /**
     * Returns {@link TrackedEntityAttribute} list with paging
     * 
     * @param name Keyword for searching by name
     * @param min
     * @param max
     * @return a collection of all TrackedEntityAttribute, or an empty
     *         collection if there are no TrackedEntityAttributes.
     */
    Collection<TrackedEntityAttributeGroup> getTrackedEntityAttributeGroupsBetweenByName( String name,
        int min, int max );

    /**
     * Returns The number of all TrackedEntityAttribute available
     * 
     */
    Integer getTrackedEntityAttributeGroupCount();

    /**
     * Returns {@link TrackedEntityAttribute} list with paging
     * 
     * @param min
     * @param max
     * @return a collection of all TrackedEntityAttribute, or an empty
     *         collection if there are no TrackedEntityAttributes.
     */
    Collection<TrackedEntityAttributeGroup> getTrackedEntityAttributeGroupsBetween( int min, int max );
}
