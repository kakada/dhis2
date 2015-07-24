package org.hisp.dhis.webapi.controller.event;

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

import com.google.common.collect.Lists;
import org.hisp.dhis.common.Pager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.query.Order;
import org.hisp.dhis.schema.descriptors.ProgramSchemaDescriptor;
import org.hisp.dhis.webapi.controller.AbstractCrudController;
import org.hisp.dhis.webapi.webdomain.WebMetaData;
import org.hisp.dhis.webapi.webdomain.WebOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Controller
@RequestMapping( value = ProgramSchemaDescriptor.API_ENDPOINT )
public class ProgramController
    extends AbstractCrudController<Program>
{
    @Autowired
    private ProgramInstanceService programInstanceService;

    @Autowired
    private ProgramService programService;

    @Override
    protected void postCreateEntity( Program program )
    {
        if ( program.isSingleEvent() && !program.isRegistration() )
        {
            ProgramInstance programInstance = new ProgramInstance();
            programInstance.setEnrollmentDate( new Date() );
            programInstance.setDateOfIncident( new Date() );
            programInstance.setProgram( program );
            programInstance.setStatus( ProgramInstance.STATUS_ACTIVE );

            programInstanceService.addProgramInstance( programInstance );
        }
    }

    @Override
    protected List<Program> getEntityList( WebMetaData metaData, WebOptions options, List<String> filters, List<Order> orders )
    {
        String type = options.getOptions().get( "type" );
        String orgUnit = options.getOptions().get( "orgUnit" );
        Boolean userFilter = Boolean.parseBoolean( options.getOptions().get( "userFilter" ) );

        List<Program> entityList;

        if ( type != null || orgUnit != null || userFilter )
        {
            options.getOptions().put( "paging", "false" );
        }

        if ( options.getOptions().containsKey( "query" ) )
        {
            entityList = Lists.newArrayList( manager.filter( getEntityClass(), options.getOptions().get( "query" ) ) );
        }
        else if ( options.hasPaging() )
        {
            int count = manager.getCount( getEntityClass() );

            Pager pager = new Pager( options.getPage(), count, options.getPageSize() );
            metaData.setPager( pager );

            entityList = new ArrayList<>( manager.getBetweenSorted( getEntityClass(), pager.getOffset(), pager.getPageSize() ) );
        }
        else
        {
            entityList = new ArrayList<>( manager.getAllSorted( getEntityClass() ) );
        }

        if ( userFilter )
        {
            List<Program> programs = Lists.newArrayList( programService.getProgramsByCurrentUser() );
            entityList.retainAll( programs );
        }

        if ( type != null )
        {
            try
            {
                int programType = Integer.parseInt( type );

                Iterator<Program> iterator = entityList.iterator();

                while ( iterator.hasNext() )
                {
                    Program program = iterator.next();

                    if ( program.getType() != programType )
                    {
                        iterator.remove();
                    }
                }
            }
            catch ( NumberFormatException ignored )
            {
            }
        }

        if ( orgUnit != null )
        {
            OrganisationUnit organisationUnit = manager.get( OrganisationUnit.class, orgUnit );

            if ( organisationUnit != null )
            {
                Iterator<Program> iterator = entityList.iterator();

                while ( iterator.hasNext() )
                {
                    Program program = iterator.next();

                    if ( !program.getOrganisationUnits().contains( organisationUnit ) )
                    {
                        iterator.remove();
                    }
                }
            }
        }

        return entityList;
    }
}
