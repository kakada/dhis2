package org.hisp.dhis.webapi.controller;

import static org.hisp.dhis.system.util.DateUtils.DATE_PATTERN;
import static org.hisp.dhis.webapi.utils.ContextUtils.CONTENT_TYPE_JSON;
import static org.hisp.dhis.webapi.utils.ContextUtils.CONTENT_TYPE_XML;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dxf2.common.IdSchemes;
import org.hisp.dhis.dxf2.datavalueset.DataValueSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping( value = FormDataController.RESOURCE_PATH )
public class FormDataController {
	
	public static final String RESOURCE_PATH = "/formData";

    private static final Log log = LogFactory.getLog( FormDataController.class );
    
    @Autowired
    private DataValueSetService dataValueSetService;
    
    // -------------------------------------------------------------------------
    // Get
    // -------------------------------------------------------------------------
    @RequestMapping( method = RequestMethod.GET, produces = CONTENT_TYPE_JSON )
    public void getDataValueSetJson(
        @RequestParam Set<String> dataSet,
        @RequestParam( required = false ) @DateTimeFormat( pattern = DATE_PATTERN ) Date startDate,
        @RequestParam( required = false ) @DateTimeFormat( pattern = DATE_PATTERN ) Date endDate,
        @RequestParam( required = false ) Set<String> orgUnit,
        @RequestParam String period,
        @RequestParam( required = false ) boolean children,
        IdSchemes idSchemes, HttpServletResponse response ) throws IOException
    {
        response.setContentType( CONTENT_TYPE_JSON );
        
        log.debug( "Get XML bulk data value set for start date: " + startDate + ", end date: " + endDate );

        dataValueSetService.writeDataValueSetJson( dataSet, period, response.getOutputStream(), idSchemes );
    }
    
}
