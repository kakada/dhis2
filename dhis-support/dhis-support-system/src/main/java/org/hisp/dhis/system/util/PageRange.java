package org.hisp.dhis.system.util;

import java.util.ArrayList;
import java.util.List;

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

/**
 * @author Lars Helge Overland
 */
public class PageRange
{
    /**
     * The current from index.
     */
    private int fromIndex = 0;
    
    /**
     * The current to index.
     */
    private int toIndex = 0;
    
    /**
     * Total number of values.
     */
    private int values;
    
    /**
     * Size of pages.
     */
    private int pageSize;
    
    protected PageRange()
    {
    }
    
    public PageRange( int values )
    {
        this.values = values;
    }
    
    public PageRange setPageSize( int pageSize )
    {
        this.pageSize = pageSize;
        return this;
    }
    
    public PageRange setPages( int pages )
    {
        this.pageSize = (int) Math.ceil( (double) values / pages );
        return this;
    }
    
    /**
     * Moves the range to the next page. Returns true if the range has another page,
     * false if not.
     */
    public boolean nextPage()
    {
        if ( toIndex >= values )
        {
            return false;
        }
        
        if ( toIndex > 0 ) // Not first invocation
        {
            fromIndex += pageSize;
        }            
        
        toIndex = Math.min( ( fromIndex + pageSize ), values );
        
        return true;
    }

    /**
     * Returns the number of pages in the list.
     */
    public int pageCount()
    {       
        int r = values / pageSize;
        int m = values % pageSize;
        
        return m == 0 ? r : ( r + 1 );
    }
    
    /**
     * Sets the current page position to the first page.
     */
    public void reset()
    {
        fromIndex = 0;
        toIndex = 0;
    }
    
    /**
     * Returns the current from index, which is closed / inclusive.
     */
    public int getFromIndex()
    {
        return fromIndex;
    }

    /**
     * Returns the current to index, which is open / exclusive.
     */
    public int getToIndex()
    {
        if ( toIndex == 0 )
        {
            throw new IllegalStateException( "nextPage() must be called before getToIndex()" );
        }
        
        return toIndex;
    }
    
    /**
     * Returns the page size.
     */
    public int getPageSize()
    {
        return pageSize;
    }
    
    /**
     * Returns a list of all pages. Each item is an array where index 0 holds the
     * from index and index 1 holds the to index. Resets the page.
     */
    public List<int[]> getPages()
    {
        List<int[]> pages = new ArrayList<>();
        
        while ( nextPage() )
        {
            int[] range = { getFromIndex(), getToIndex() };
            pages.add( range );
        }
        
        reset();
        
        return pages;
    }
}
