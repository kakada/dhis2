/**
 * 
 */
package org.hisp.dhis.hub;

import java.io.IOException;
import java.net.URISyntaxException;

import org.hisp.dhis.dataset.CompleteDataSetRegistration;

/**
 * @author Kakada Chheang
 *
 */
public interface HubClientService {
	
	void send(CompleteDataSetRegistration registration) throws URISyntaxException, IOException;
	
}
