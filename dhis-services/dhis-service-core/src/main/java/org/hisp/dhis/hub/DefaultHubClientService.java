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

package org.hisp.dhis.hub;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.hisp.dhis.configuration.Configuration;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.dataset.CompleteDataSetRegistration;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.instedd.hub.client.form.FormData;
import org.instedd.hub.client.service.HubClientServiceImpl;
import org.instedd.hub.client.service.IHubClientService;

/**
 * @author Kakada Chheang
 *
 */
public class DefaultHubClientService implements HubClientService {
	
	// -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
	
	private ConfigurationService configurationService;
	
	public void setConfigurationService( ConfigurationService configurationService ) {
		this.configurationService = configurationService;
	}
	
	private DataValueService dataValueService;
	
	public void setDataValueService( DataValueService dataValueService ) {
		this.dataValueService = dataValueService;
	}

	@Override
	public void notifyCompleteDataSetRegistration(CompleteDataSetRegistration registration) throws URISyntaxException, IOException {
		Configuration configuration = configurationService.getConfiguration();
		if(configuration.isHubEnableMode()) {
			URI uri = new URI(configuration.getHubServerUrl());
			IHubClientService hubClientService = new HubClientServiceImpl(configuration.getHubServerUsername(), configuration.getHubServerPassword());
			
			// form data
			FormData formData = new FormData();
			formData.addParam("taskName", configuration.getHubInsertTask());
			formData.addParam("dataset", registration.getDataSet().getName());
			formData.addParam("organisation", registration.getSource().getName());
			formData.addParam("period", registration.getPeriodName());
			formData.addParam("storedBy", registration.getStoredBy());
			formData.addParam("completedDate", registration.getDate().toString());
			formData.addParam("attributeOptionCombo", registration.getAttributeOptionCombo().getName());
			
			// Get data value of data set
			Collection<DataValue> dataValues = dataValueService.getDataValues(registration.getSource(), registration.getPeriod(), registration.getDataSet().getDataElements());
    		for( DataValue dataValue : dataValues) {
    			String dataElementFormName = null;
    			if (dataValue.getDataElement().getFormName() != null) {
    				dataElementFormName = dataValue.getDataElement().getFormName();
    			} else {
    				dataElementFormName = dataValue.getDataElement().getName() + " " + (dataValue.getCategoryOptionCombo().isDefault() ? "" : dataValue.getCategoryOptionCombo().getName());
    			}
    			formData.addParam(dataElementFormName, dataValue.getValue());
    		}
        	
    		hubClientService.doPost(uri, formData);
		}
	}
	
	@Override
	public void notifyUpdateCompleteDataSetRegistration(CompleteDataSetRegistration registration) throws URISyntaxException, IOException {
		Configuration configuration = configurationService.getConfiguration();
		if(configuration.isHubEnableMode()) {
			URI uri = new URI(configuration.getHubServerUrl());
			IHubClientService hubClientService = new HubClientServiceImpl(configuration.getHubServerUsername(), configuration.getHubServerPassword());
			
			// form data
			FormData formData = new FormData();
			formData.addParam("taskName", configuration.getHubUpdateTask());
			formData.addParam("dataset", registration.getDataSet().getName());
			formData.addParam("organisation", registration.getSource().getName());
			formData.addParam("period", registration.getPeriodName());
			formData.addParam("storedBy", registration.getStoredBy());
			formData.addParam("completedDate", registration.getDate().toString());
			formData.addParam("attributeOptionCombo", registration.getAttributeOptionCombo().getName());
			
			// Get data value of data set
			Collection<DataValue> dataValues = dataValueService.getDataValues(registration.getSource(), registration.getPeriod(), registration.getDataSet().getDataElements());
    		for( DataValue dataValue : dataValues) {
    			String dataElementFormName = null;
    			if (dataValue.getDataElement().getFormName() != null) {
    				dataElementFormName = dataValue.getDataElement().getFormName();
    			} else {
    				dataElementFormName = dataValue.getDataElement().getName() + " " + (dataValue.getCategoryOptionCombo().isDefault() ? "" : dataValue.getCategoryOptionCombo().getName());
    			}
    			formData.addParam(dataElementFormName, dataValue.getValue());
    		}
        	
    		hubClientService.doPost(uri, formData);
		}
	}
	
	@Override
	public void notifyDeleteCompleteDataSetRegistration(CompleteDataSetRegistration registration) throws URISyntaxException, IOException {
		Configuration configuration = configurationService.getConfiguration();
		if(configuration.isHubEnableMode()) {
			URI uri = new URI(configuration.getHubServerUrl());
			IHubClientService hubClientService = new HubClientServiceImpl(configuration.getHubServerUsername(), configuration.getHubServerPassword());
			
			// form data
			FormData formData = new FormData();
			formData.addParam("taskName", configuration.getHubDeleteTask());
			formData.addParam("dataset", registration.getDataSet().getName());
			formData.addParam("organisation", registration.getSource().getName());
			formData.addParam("period", registration.getPeriodName());
			formData.addParam("storedBy", registration.getStoredBy());
			formData.addParam("completedDate", registration.getDate().toString());
			formData.addParam("attributeOptionCombo", registration.getAttributeOptionCombo().getName());
			
			// Get data value of data set
			Collection<DataValue> dataValues = dataValueService.getDataValues(registration.getSource(), registration.getPeriod(), registration.getDataSet().getDataElements());
    		for( DataValue dataValue : dataValues) {
    			String dataElementFormName = null;
    			if (dataValue.getDataElement().getFormName() != null) {
    				dataElementFormName = dataValue.getDataElement().getFormName();
    			} else {
    				dataElementFormName = dataValue.getDataElement().getName() + " " + (dataValue.getCategoryOptionCombo().isDefault() ? "" : dataValue.getCategoryOptionCombo().getName());
    			}
    			formData.addParam(dataElementFormName, dataValue.getValue());
    		}
        	
    		hubClientService.doPost(uri, formData);
		}
	}
	
}
