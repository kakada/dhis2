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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.dataentryform.DataEntryForm;
import org.hisp.dhis.dataentryform.DataEntryFormService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chau Thu Tran
 * 
 * @version $ TrackedEntityFormStoreTest.java Nov 8, 2013 10:07:30 AM $
 */
public class TrackedEntityFormStoreTest
    extends DhisSpringTest
{
    @Autowired
    private TrackedEntityFormStore entityFormStore;

    @Autowired
    private DataEntryFormService dataEntryFormService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    private TrackedEntityForm entityFormA;

    private TrackedEntityForm entityFormB;

    private TrackedEntityForm entityFormC;

    private Program programA;

    private Program programB;

    private DataEntryForm dataEntryForm;

    // -------------------------------------------------------------------------
    // Fixture
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest()
        throws Exception
    {
        OrganisationUnit organisationUnit = createOrganisationUnit( 'A' );
        organisationUnitService.addOrganisationUnit( organisationUnit );

        programA = createProgram( 'A', new HashSet<ProgramStage>(), organisationUnit );
        programService.addProgram( programA );

        programB = createProgram( 'B', new HashSet<ProgramStage>(), organisationUnit );
        programService.addProgram( programB );

        DataEntryForm dataEntryFormA = new DataEntryForm( "DataEntryForm-A" );
        dataEntryFormService.addDataEntryForm( dataEntryFormA );

        DataEntryForm dataEntryFormB = new DataEntryForm( "DataEntryForm-B" );
        dataEntryFormService.addDataEntryForm( dataEntryFormB );

        DataEntryForm dataEntryFormC = new DataEntryForm( "DataEntryForm-C" );
        dataEntryFormService.addDataEntryForm( dataEntryFormC );

        entityFormA = new TrackedEntityForm( programA, dataEntryForm );
        entityFormB = new TrackedEntityForm( programB, dataEntryForm );
        entityFormC = new TrackedEntityForm( dataEntryForm );
    }

    @Test
    public void testGetentityFormByProgram()
    {
        entityFormStore.save( entityFormA );
        entityFormStore.save( entityFormB );
        entityFormStore.save( entityFormC );

        TrackedEntityForm form = entityFormStore.get( programA );
        assertEquals( entityFormA, form );
    }

    @Test
    public void testGetCommonentityForm()
    {
        entityFormStore.save( entityFormA );
        entityFormStore.save( entityFormB );
        entityFormStore.save( entityFormC );

        TrackedEntityForm form = entityFormStore.getFormsWithoutProgram();
        assertEquals( entityFormC, form );
    }

}
