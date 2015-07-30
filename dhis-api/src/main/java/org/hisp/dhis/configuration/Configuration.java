package org.hisp.dhis.configuration;

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

import org.hisp.dhis.indicator.IndicatorGroup;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.YearlyPeriodType;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;

/**
 * @author Lars Helge Overland
 */
public class Configuration
    implements Serializable
{
    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = 936186436040704261L;
    
    private static final PeriodType DEFAULT_INFRASTRUCTURAL_PERIODTYPE = new YearlyPeriodType();
    
    private int id;

    // -------------------------------------------------------------------------
    // Various
    // -------------------------------------------------------------------------

    private String systemId;
    
    private UserGroup feedbackRecipients;
    
    private OrganisationUnitLevel offlineOrganisationUnitLevel;

    private IndicatorGroup infrastructuralIndicators;

    private DataElementGroup infrastructuralDataElements;
    
    private PeriodType infrastructuralPeriodType;
    
    private UserAuthorityGroup selfRegistrationRole;
    
    private OrganisationUnit selfRegistrationOrgUnit;

    // -------------------------------------------------------------------------
    // Remote synch
    // -------------------------------------------------------------------------

    private String remoteServerUrl;
    
    private String remoteServerUsername;
    
    private String remoteServerPassword;
    
    // -------------------------------------------------------------------------
    // Remote Hub synch
    // -------------------------------------------------------------------------
    
    private String hubServerUrl;
    
    private String hubServerUsername;
    
    private String hubServerPassword;
    
    private String hubInsertTask;
    
    private String hubUpdateTask;
    
    private String hubDeleteTask;
    
    private Boolean hubEnableMode;

    // -------------------------------------------------------------------------
    // SMTP
    // -------------------------------------------------------------------------
    
    private String smtpPassword;
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Configuration()
    {
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public PeriodType getInfrastructuralPeriodTypeDefaultIfNull()
    {
        return infrastructuralPeriodType != null ? infrastructuralPeriodType : DEFAULT_INFRASTRUCTURAL_PERIODTYPE;
    }
    
    public boolean selfRegistrationAllowed()
    {
        return selfRegistrationRole != null && selfRegistrationOrgUnit != null;
    }
    
    // -------------------------------------------------------------------------
    // Set and get methods
    // -------------------------------------------------------------------------

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public String getSystemId()
    {
        return systemId;
    }

    public void setSystemId( String systemId )
    {
        this.systemId = systemId;
    }

    public UserGroup getFeedbackRecipients()
    {
        return feedbackRecipients;
    }

    public void setFeedbackRecipients( UserGroup feedbackRecipients )
    {
        this.feedbackRecipients = feedbackRecipients;
    }

    public void setOfflineOrganisationUnitLevel( OrganisationUnitLevel offlineOrganisationUnitLevel )
    {
        this.offlineOrganisationUnitLevel = offlineOrganisationUnitLevel;
    }

    public OrganisationUnitLevel getOfflineOrganisationUnitLevel()
    {
        return offlineOrganisationUnitLevel;
    }

    public IndicatorGroup getInfrastructuralIndicators()
    {
        return infrastructuralIndicators;
    }

    public void setInfrastructuralIndicators( IndicatorGroup infrastructuralIndicators )
    {
        this.infrastructuralIndicators = infrastructuralIndicators;
    }

    public DataElementGroup getInfrastructuralDataElements()
    {
        return infrastructuralDataElements;
    }

    public void setInfrastructuralDataElements( DataElementGroup infrastructuralDataElements )
    {
        this.infrastructuralDataElements = infrastructuralDataElements;
    }

    public PeriodType getInfrastructuralPeriodType()
    {
        return infrastructuralPeriodType;
    }

    public void setInfrastructuralPeriodType( PeriodType infrastructuralPeriodType )
    {
        this.infrastructuralPeriodType = infrastructuralPeriodType;
    }

    public UserAuthorityGroup getSelfRegistrationRole()
    {
        return selfRegistrationRole;
    }

    public void setSelfRegistrationRole( UserAuthorityGroup selfRegistrationRole )
    {
        this.selfRegistrationRole = selfRegistrationRole;
    }

    public OrganisationUnit getSelfRegistrationOrgUnit()
    {
        return selfRegistrationOrgUnit;
    }

    public void setSelfRegistrationOrgUnit( OrganisationUnit selfRegistrationOrgUnit )
    {
        this.selfRegistrationOrgUnit = selfRegistrationOrgUnit;
    }

    public String getRemoteServerUrl()
    {
        return remoteServerUrl;
    }

    public void setRemoteServerUrl( String remoteServerUrl )
    {
        this.remoteServerUrl = remoteServerUrl;
    }

    public String getRemoteServerUsername()
    {
        return remoteServerUsername;
    }

    public void setRemoteServerUsername( String remoteServerUsername )
    {
        this.remoteServerUsername = remoteServerUsername;
    }

    /**
     * Do not serialize.
     */
    public String getRemoteServerPassword()
    {
        return remoteServerPassword;
    }

    public void setRemoteServerPassword( String remoteServerPassword )
    {
        this.remoteServerPassword = remoteServerPassword;
    }
    
	/**
	 * @return the hubEnableMode
	 */
	public Boolean isHubEnableMode() {
		return hubEnableMode;
	}

	/**
	 * @param hubEnableMode the hubEnableMode to set
	 */
	public void setHubEnableMode(Boolean hubEnableMode) {
		this.hubEnableMode = hubEnableMode;
	}
	
    /**
	 * @return the hubServerUrl
	 */
	public String getHubServerUrl() {
		return hubServerUrl;
	}

	/**
	 * @param hubServerUrl the hubServerUrl to set
	 */
	public void setHubServerUrl(String hubServerUrl) {
		this.hubServerUrl = hubServerUrl;
	}

	/**
	 * @return the hubServerUsername
	 */
	public String getHubServerUsername() {
		return hubServerUsername;
	}

	/**
	 * @param hubServerUsername the hubServerUsername to set
	 */
	public void setHubServerUsername(String hubServerUsername) {
		this.hubServerUsername = hubServerUsername;
	}

	/**
	 * @return the hubServerPassword
	 */
	public String getHubServerPassword() {
		return hubServerPassword;
	}

	/**
	 * @param hubServerPassword the hubServerPassword to set
	 */
	public void setHubServerPassword(String hubServerPassword) {
		this.hubServerPassword = hubServerPassword;
	}
	
	public String getHubInsertTask() {
		return hubInsertTask;
	}

	public void setHubInsertTask(String hubInsertTask) {
		this.hubInsertTask = hubInsertTask;
	}

	public String getHubUpdateTask() {
		return hubUpdateTask;
	}
	
	public void setHubUpdateTask(String hubUpdateTaskUrl) {
		this.hubUpdateTask = hubUpdateTaskUrl;
	}
	
	public String getHubDeleteTask() {
		return hubDeleteTask;
	}

	public void setHubDeleteTask( String hubDeleteTask ) {
		this.hubDeleteTask = hubDeleteTask;
	}

	/**
     * Do not serialize.
     */
    public String getSmtpPassword()
    {
        return smtpPassword;
    }
    
    public void setSmtpPassword( String smtpPassword )
    {
        this.smtpPassword = smtpPassword;
    }
}
