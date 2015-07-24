package org.hisp.dhis.system.util;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Lars Helge Overland
 */
public class ListUtils
{
    /**
     * Removes from the given list the elements at the given indexes. Ignores
     * indexes which are out of bounds of list.
     * 
     * @param list the list to remove elements from.
     * @param indexes the indexes for the elements to remove.
     */
    public static <T> void removeAll( List<T> list, List<Integer> indexes )
    {
        if ( list == null || indexes == null )
        {
            return;
        }
        
        Collections.sort( indexes, Collections.reverseOrder() );
        
        final int size = list.size();
        
        for ( Integer index : indexes )
        {
            if ( index >= 0 && index < size )
            {
                list.remove( (int) index );
            }
        }
    }
    
    /**
     * Removes from the given list the elements at the given indexes. Ignores
     * indexes which are out of bounds of list.
     * 
     * @param indexes the list to remove elements from.
     * @param indexes the indexes for the elements to remove.
     */
    public static <T> void removeAll( List<T> list, Integer... indexes )
    {
        List<Integer> inx = new ArrayList<>( Arrays.asList( indexes ) );
        
        removeAll( list, inx );
    }
    
    /**
     * Retains only elements in the give list that are part of the given retain
     * list.
     * 
     * @param list the target list.
     * @param retain the elements to retain in the target list.
     */
    public static <T> List<T> retainAll( List<T> list, List<T> retain )
    {
        if ( list == null || retain == null )
        {
            return list;
        }
        
        list.retainAll( retain );
        return list;
    }
    
    /**
     * Returns a sublist of the given list with the elements at the given indexes.
     * 
     * @param list the list to select from.
     * @param indexes the indexes of the elements in the list to select.
     */
    public static <T> List<T> getAtIndexes( List<T> list, List<Integer> indexes )
    {
        List<T> elements = new ArrayList<>();
        
        for ( Integer index : indexes )
        {
            elements.add( list.get( index ) );
        }
        
        return elements;
    }
    
    /**
     * Checks whether the given list contains duplicates. List entries are compared
     * using the given comparator.
     * 
     * @param list the list.
     * @param comparator the comparator.
     * @return true if the list contains duplicates, false if not.
     */
    public static <T> boolean containsDuplicates( List<T> list, Comparator<T> comparator )
    {
        Collections.sort( list, comparator );
        
        T previous = null;
        
        for ( T entry : list )
        {
            if ( previous != null && previous.equals( entry ) )
            {
                return true;
            }
            
            previous = entry;
        }
        
        return false;
    }
    
    /**
     * Returns the duplicates in the given list. List entries are compared
     * using the given comparator.
     * 
     * @param list the list.
     * @param comparator the comparator.
     * @return a set of duplicates from the given list.
     */
    public static <T> Set<T> getDuplicates( List<T> list, Comparator<T> comparator )
    {
        Set<T> duplicates = new HashSet<>();
        
        Collections.sort( list, comparator );
        
        T previous = null;
        
        for ( T entry : list )
        {
            if ( previous != null && previous.equals( entry ) )
            {
                duplicates.add( entry );
            }
            
            previous = entry;
        }
        
        return duplicates;
    }

    /**
     * Returns the duplicates in the given list.
     * 
     * @param list the list.
     * @return a set of duplicates from the given list.
     */
    public static <T> Set<T> getDuplicates( List<T> list )
    {
        Set<T> duplicates = new HashSet<>();
        UniqueArrayList<T> uniqueElements = new UniqueArrayList<>();
        
        for ( T element : list )
        {
            if ( !uniqueElements.add( element ) )
            {
                duplicates.add( element );
            }
        }
        
        return duplicates;
    }
    
    /**
     * Returns a Collection with the given items.
     * 
     * @param items the items which should be included in the Collection.
     * @return a Collection.
     */
    @SafeVarargs
    public static final <T> Collection<T> getCollection( final T... items )
    {
        List<T> list = new ArrayList<>();
        
        for ( T item : items )
        {
            list.add( item );
        }
        
        return list;
    }

    /**
     * Returns a List with the given items.
     * 
     * @param items the items which should be included in the List.
     * @return a List.
     */
    @SafeVarargs
    public static final <T> List<T> getList( final T... items )
    {
        List<T> list = new ArrayList<>();
        
        for ( T item : items )
        {
            list.add( item );
        }
        
        return list;
    }
    
    /**
     * Removes empty strings from the given list. Empty includes null.
     * 
     * @param list the list of strings.
     */
    public static void removeEmptys( List<String> list )
    {
        if ( list != null && !list.isEmpty() )
        {
            Iterator<String> iterator = list.iterator();
            
            while ( iterator.hasNext() )
            {
                if ( StringUtils.isEmpty( iterator.next() ) )
                {
                    iterator.remove();
                }
            }
        }
    }
    
    /**
     * Returns the sub list of the given list avoiding exceptions, starting on 
     * the given start index and returning at maximum the given max number of items. 
     * 
     * @param list the list.
     * @param start the start index.
     * @param max the max number of items to return.
     */
    public static <T> List<T> subList( List<T> list, int start, int max )
    {
        if ( list == null )
        {
            return null;
        }
        
        int end = start + max;
        
        return list.subList( Math.max( 0, start ), Math.min( list.size(), end ) );
    }
    
    /**
     * Unions the given array of lists into a single list.
     * 
     * @param lists the array of lists.
     * @return a union of the given lists.
     */
    @SafeVarargs
    public static final <T> List<T> union( final List<T>... lists )
    {
        final List<T> union = new ArrayList<>();
        
        for ( List<T> list : lists )
        {
            union.addAll( list );
        }
        
        return union;
    }
    
    /**
     * Returns a contiguous list of Integers starting on and including a, ending
     * on and excluding b.
     * 
     * @param a start, inclusive.
     * @param b end, exclusive.
     * @return a list of Integers.
     */
    public static List<Integer> getClosedOpenList( int a, int b )
    {
        List<Integer> list = new ArrayList<Integer>();
        
        for ( int i = a; i < b; i++ )
        {
            list.add( i );
        }
        
        return list;
    }
}
