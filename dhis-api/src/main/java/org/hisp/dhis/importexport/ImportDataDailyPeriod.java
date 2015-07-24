package org.hisp.dhis.importexport;

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

import java.io.Serializable;
import java.util.Date;


/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class ImportDataDailyPeriod
    implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5777420243045995151L;

	/**
     * Determines if a de-serialized file is compatible with this class.
     */
    
    private int periodId;
    
    private int periodTypeId;
    
    private Date startDate;
    
    private Date endDate;

    public ImportDataDailyPeriod()
    {
    }
    
	public ImportDataDailyPeriod(int periodId, int periodTypeId,
			Date startDate, Date endDate) {
		super();
		this.periodId = periodId;
		this.periodTypeId = periodTypeId;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public int getPeriodId() {
		return periodId;
	}

	public void setPeriodId(int periodId) {
		this.periodId = periodId;
	}

	public int getPeriodTypeId() {
		return periodTypeId;
	}

	public void setPeriodTypeId(int periodTypeId) {
		this.periodTypeId = periodTypeId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + periodId;
		result = prime * result + periodTypeId;
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImportDataDailyPeriod other = (ImportDataDailyPeriod) obj;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (periodId != other.periodId)
			return false;
		if (periodTypeId != other.periodTypeId)
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImportDataDailyPeriod [periodId=" + periodId
				+ ", periodTypeId=" + periodTypeId + ", startDate=" + startDate
				+ ", endDate=" + endDate + "]";
	}
    

    
    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    
    // -------------------------------------------------------------------------
    // Equals & hashCode
    // -------------------------------------------------------------------------



    // -------------------------------------------------------------------------
    // Getters & setters
    // -------------------------------------------------------------------------

}
