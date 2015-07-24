package org.hisp.dhis.dataapproval;

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

import static org.hisp.dhis.setting.SystemSettingManager.KEY_ACCEPTANCE_REQUIRED_FOR_APPROVAL;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_HIDE_UNAPPROVED_DATA_IN_ANALYTICS;
import static org.hisp.dhis.system.util.CollectionUtils.asSet;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.DhisTest;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dataapproval.exceptions.DataApprovalException;
import org.hisp.dhis.dataelement.CategoryOptionGroup;
import org.hisp.dhis.dataelement.CategoryOptionGroupSet;
import org.hisp.dhis.dataelement.CategoryOptionGroupStore;
import org.hisp.dhis.dataelement.DataElementCategory;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOption;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.hibernate.HibernateCategoryOptionGroupStore;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.mock.MockCurrentUserService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupAccess;
import org.hisp.dhis.user.UserGroupAccessService;
import org.hisp.dhis.user.UserGroupService;
import org.hisp.dhis.user.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Jim Grace
 */
public class DataApprovalServiceCategoryOptionGroupTest
    extends DhisTest
{
    private static final String ACCESS_NONE = "--------";
    private static final String ACCESS_READ = "r-------";

    @Autowired
    private DataApprovalService dataApprovalService;

    @Autowired
    private DataApprovalStore dataApprovalStore;

    @Autowired
    private DataApprovalLevelService dataApprovalLevelService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private DataElementCategoryService categoryService;

    @Autowired
    private CategoryOptionGroupStore categoryOptionGroupStore;

    @Autowired
    private HibernateCategoryOptionGroupStore hibernateCategoryOptionGroupStore;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    protected IdentifiableObjectManager identifiableObjectManager;

    @Autowired
    private SystemSettingManager systemSettingManager;

    @Autowired
    protected UserGroupAccessService userGroupAccessService;

    @Autowired
    protected UserGroupService userGroupService;

    @Autowired
    protected UserService _userService;

    @Autowired
    protected CurrentUserService currentUserService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // -------------------------------------------------------------------------
    // Supporting data
    // -------------------------------------------------------------------------

    private OrganisationUnit global;
    private OrganisationUnit americas;
    private OrganisationUnit asia;
    private OrganisationUnit brazil;
    private OrganisationUnit china;
    private OrganisationUnit india;

    private User userA;

    private Date dateA;

    private CurrentUserService superUser;
    private CurrentUserService globalConsultant;
    private CurrentUserService globalUser;
    private CurrentUserService globalReadEverything;
    private CurrentUserService brazilInteragencyUser;
    private CurrentUserService chinaInteragencyUser;
    private CurrentUserService indiaInteragencyUser;
    private CurrentUserService brazilAgencyAUser;
    private CurrentUserService chinaAgencyAUser;
    private CurrentUserService chinaAgencyBUser;
    private CurrentUserService indiaAgencyAUser;
    private CurrentUserService brazilPartner1User;
    private CurrentUserService chinaPartner1User;
    private CurrentUserService chinaPartner2User;
    private CurrentUserService indiaPartner1User;
    private CurrentUserService currentMockUserService;

    private DataElementCategoryOption brazilA1;
    private DataElementCategoryOption chinaA1_1;
    private DataElementCategoryOption chinaA1_2;
    private DataElementCategoryOption chinaA2;
    private DataElementCategoryOption chinaB2;
    private DataElementCategoryOption indiaA1;

    private DataElementCategory mechanismCategory;

    private DataElementCategoryCombo mechanismCategoryCombo;

    private DataElementCategoryOptionCombo brazilA1Combo;
    private DataElementCategoryOptionCombo chinaA1_1Combo;
    private DataElementCategoryOptionCombo chinaA1_2Combo;
    private DataElementCategoryOptionCombo chinaA2Combo;
    private DataElementCategoryOptionCombo chinaB2Combo;
    private DataElementCategoryOptionCombo indiaA1Combo;

    private CategoryOptionGroup agencyA;
    private CategoryOptionGroup agencyB;
    private CategoryOptionGroup partner1;
    private CategoryOptionGroup partner2;

    private CategoryOptionGroupSet agencies;
    private CategoryOptionGroupSet partners;

    private DataApprovalLevel globalLevel1;
    private DataApprovalLevel countryLevel2;
    private DataApprovalLevel agencyLevel3;
    private DataApprovalLevel partnerLevel4;

    private DataSet dataSetA;

    private Period periodA;

    // -------------------------------------------------------------------------
    // Set up/tear down helper methods
    // -------------------------------------------------------------------------

    private CurrentUserService getMockCurrentUserService( String userName, boolean superUserFlag, OrganisationUnit orgUnit, String... auths )
    {
        CurrentUserService mockCurrentUserService = new MockCurrentUserService( superUserFlag, asSet( orgUnit ), asSet( orgUnit ), auths );

        User user = mockCurrentUserService.getCurrentUser();

        user.setFirstName( "Test" );
        user.setSurname( userName );

        UserCredentials credentials = user.getUserCredentials();

        credentials.setUsername( userName );

        for ( UserAuthorityGroup role : credentials.getUserAuthorityGroups() )
        {
            role.setName( CodeGenerator.generateCode() ); // Give the role an arbitrary name

            userService.addUserAuthorityGroup( role );
        }

        userService.addUserCredentials( credentials );
        userService.addUser( user );

        return mockCurrentUserService;
    }

    private UserGroup getUserGroup( String userGroupName, Set<User> users )
    {
        UserGroup userGroup = new UserGroup();
        userGroup.setAutoFields();

        userGroup.setName( userGroupName );
        userGroup.setMembers( users );

        userGroupService.addUserGroup( userGroup );

        return userGroup;
    }

    private void setPrivateAccess( BaseIdentifiableObject object, UserGroup... userGroups )
    {
        object.setPublicAccess( ACCESS_NONE );
        object.setUser( userA ); // Needed for sharing to work

        for ( UserGroup group : userGroups )
        {
            UserGroupAccess userGroupAccess = new UserGroupAccess();

            userGroupAccess.setAccess( ACCESS_READ );

            userGroupAccess.setUserGroup( group );

            userGroupAccessService.addUserGroupAccess( userGroupAccess );

            object.getUserGroupAccesses().add( userGroupAccess );
        }

        identifiableObjectManager.updateNoAcl( object );
    }

    // -------------------------------------------------------------------------
    // Set up/tear down
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest() throws Exception
    {
        userService = _userService;

        // ---------------------------------------------------------------------
        // Add supporting data
        // ---------------------------------------------------------------------

        global = createOrganisationUnit( "Global" );
        americas = createOrganisationUnit( "Americas", global );
        asia = createOrganisationUnit( "Asia", global );
        brazil = createOrganisationUnit( "Brazil", americas );
        china = createOrganisationUnit( "China", asia );
        india = createOrganisationUnit( "India", asia );

        global.setLevel( 1 );
        americas.setLevel( 2 );
        asia.setLevel( 2 );
        brazil.setLevel( 3 );
        china.setLevel( 3 );
        india.setLevel( 3 );

        organisationUnitService.addOrganisationUnit( global );
        organisationUnitService.addOrganisationUnit( americas );
        organisationUnitService.addOrganisationUnit( asia );
        organisationUnitService.addOrganisationUnit( brazil );
        organisationUnitService.addOrganisationUnit( china );
        organisationUnitService.addOrganisationUnit( india );

        int globalId = global.getId();
        int americasId = americas.getId();
        int asiaId = asia.getId();
        int brazilId = brazil.getId();
        int chinaId = china.getId();
        int indiaId = india.getId();

        jdbcTemplate.execute(
                "CREATE TABLE _orgunitstructure "+
                        "(" +
                        "  organisationunitid integer NOT NULL, " +
                        "  level integer, " +
                        "  idlevel1 integer, " +
                        "  idlevel2 integer, " +
                        "  idlevel3 integer, " +
                        "  CONSTRAINT _orgunitstructure_pkey PRIMARY KEY (organisationunitid)" +
                        ");" );

        jdbcTemplate.execute( "INSERT INTO _orgunitstructure VALUES (" + globalId + ", 1, " + globalId + ", null, null);" );
        jdbcTemplate.execute( "INSERT INTO _orgunitstructure VALUES (" + americasId + ", 2, " + globalId + ", " + americasId + ", null);" );
        jdbcTemplate.execute( "INSERT INTO _orgunitstructure VALUES (" + asiaId + ", 2, " + globalId + ", " + asiaId + ", null);" );
        jdbcTemplate.execute( "INSERT INTO _orgunitstructure VALUES (" + brazilId + ", 3, " + globalId + ", " + americasId + ", " + brazilId + ");" );
        jdbcTemplate.execute( "INSERT INTO _orgunitstructure VALUES (" + chinaId + ", 3, " + globalId + ", " + asiaId + ", " + chinaId + ");" );
        jdbcTemplate.execute( "INSERT INTO _orgunitstructure VALUES (" + indiaId + ", 3, " + globalId + ", " + asiaId + ", " + indiaId + ");" );

        userA = createUser( 'A' );
        userService.addUser( userA );

        dateA = new Date();

        superUser = getMockCurrentUserService( "SuperUser", true, global, UserAuthorityGroup.AUTHORITY_ALL );
        globalConsultant = getMockCurrentUserService( "GlobalConsultant", false, global, DataApproval.AUTH_APPROVE, DataApproval.AUTH_ACCEPT_LOWER_LEVELS, DataApproval.AUTH_APPROVE_LOWER_LEVELS );
        globalUser = getMockCurrentUserService( "GlobalUser", false, global, DataApproval.AUTH_APPROVE, DataApproval.AUTH_ACCEPT_LOWER_LEVELS );
        globalReadEverything = getMockCurrentUserService( "GlobalReadEverything", false, global, DataApproval.AUTH_VIEW_UNAPPROVED_DATA );
        brazilInteragencyUser = getMockCurrentUserService( "BrazilInteragencyUser", false, brazil, DataApproval.AUTH_APPROVE, DataApproval.AUTH_ACCEPT_LOWER_LEVELS );
        chinaInteragencyUser = getMockCurrentUserService( "ChinaInteragencyUser", false, china, DataApproval.AUTH_APPROVE, DataApproval.AUTH_ACCEPT_LOWER_LEVELS );
        indiaInteragencyUser = getMockCurrentUserService( "IndiaInteragencyUser", false, india, DataApproval.AUTH_APPROVE, DataApproval.AUTH_ACCEPT_LOWER_LEVELS );
        brazilAgencyAUser = getMockCurrentUserService( "BrazilAgencyAUser", false, brazil, DataApproval.AUTH_APPROVE, DataApproval.AUTH_ACCEPT_LOWER_LEVELS );
        chinaAgencyAUser = getMockCurrentUserService( "ChinaAgencyAUser", false, china, DataApproval.AUTH_APPROVE, DataApproval.AUTH_ACCEPT_LOWER_LEVELS );
        chinaAgencyBUser = getMockCurrentUserService( "ChinaAgencyBUser", false, china, DataApproval.AUTH_APPROVE, DataApproval.AUTH_ACCEPT_LOWER_LEVELS );
        indiaAgencyAUser = getMockCurrentUserService( "IndiaAgencyAUser", false, india, DataApproval.AUTH_APPROVE, DataApproval.AUTH_ACCEPT_LOWER_LEVELS );
        brazilPartner1User = getMockCurrentUserService( "BrazilPartner1User", false, brazil, DataApproval.AUTH_APPROVE );
        chinaPartner1User = getMockCurrentUserService( "ChinaPartner1User", false, china, DataApproval.AUTH_APPROVE );
        chinaPartner2User = getMockCurrentUserService( "ChinaPartner2User", false, china, DataApproval.AUTH_APPROVE );
        indiaPartner1User = getMockCurrentUserService( "IndiaPartner1User", false, india, DataApproval.AUTH_APPROVE );
        currentMockUserService = null;

        UserGroup globalUsers = getUserGroup( "GlobalUsers", asSet( globalUser.getCurrentUser(), globalConsultant.getCurrentUser(), globalReadEverything.getCurrentUser() ) );
        UserGroup brazilInteragencyUsers = getUserGroup( "BrazilInteragencyUsers", asSet( brazilInteragencyUser.getCurrentUser() ) );
        UserGroup chinaInteragencyUsers = getUserGroup( "ChinaInteragencyUsers", asSet( chinaInteragencyUser.getCurrentUser() ) );
        UserGroup indiaInteragencyUsers = getUserGroup( "IndiaInteragencyUsers", asSet( indiaInteragencyUser.getCurrentUser() ) );
        UserGroup brazilAgencyAUsers = getUserGroup( "BrazilAgencyAUsers", asSet( brazilAgencyAUser.getCurrentUser() ) );
        UserGroup chinaAgencyAUsers = getUserGroup( "ChinaAgencyAUsers", asSet( chinaAgencyAUser.getCurrentUser() ) );
        UserGroup chinaAgencyBUsers = getUserGroup( "ChinaAgencyBUsers", asSet( chinaAgencyBUser.getCurrentUser() ) );
        UserGroup indiaAgencyAUsers = getUserGroup( "IndiaAgencyAUsers", asSet( indiaAgencyAUser.getCurrentUser() ) );
        UserGroup brazilPartner1Users = getUserGroup( "BrazilPartner1Users", asSet( brazilPartner1User.getCurrentUser() ) );
        UserGroup chinaPartner1Users = getUserGroup( "ChinaPartner1Users", asSet( chinaPartner1User.getCurrentUser() ) );
        UserGroup chinaPartner2Users = getUserGroup( "ChinaPartner2Users", asSet( chinaPartner2User.getCurrentUser() ) );
        UserGroup indiaPartner1Users = getUserGroup( "IndiaPartner1Users", asSet( indiaPartner1User.getCurrentUser() ) );

        brazilA1 = new DataElementCategoryOption( "BrazilA1" );
        chinaA1_1 = new DataElementCategoryOption( "ChinaA1_1" );
        chinaA1_2 = new DataElementCategoryOption( "ChinaA1_2" );
        chinaA2 = new DataElementCategoryOption( "ChinaA2" );
        chinaB2 = new DataElementCategoryOption( "ChinaB2" );
        indiaA1 = new DataElementCategoryOption( "IndiaA1" );

        brazilA1.setOrganisationUnits( asSet( brazil ) );
        chinaA1_1.setOrganisationUnits( asSet( china ) );
        chinaA1_2.setOrganisationUnits( asSet( china ) );
        chinaA2.setOrganisationUnits( asSet( china ) );
        chinaB2.setOrganisationUnits( asSet( china ) );
        indiaA1.setOrganisationUnits( asSet( india ) );

        categoryService.addDataElementCategoryOption( brazilA1 );
        categoryService.addDataElementCategoryOption( chinaA1_1 );
        categoryService.addDataElementCategoryOption( chinaA1_2 );
        categoryService.addDataElementCategoryOption( chinaA2 );
        categoryService.addDataElementCategoryOption( chinaB2 );
        categoryService.addDataElementCategoryOption( indiaA1 );

        setPrivateAccess( brazilA1, globalUsers, brazilInteragencyUsers, brazilAgencyAUsers, brazilPartner1Users );
        setPrivateAccess( chinaA1_1, globalUsers, chinaInteragencyUsers, chinaAgencyAUsers, chinaPartner1Users );
        setPrivateAccess( chinaA1_2, globalUsers, chinaInteragencyUsers, chinaAgencyAUsers, chinaPartner1Users );
        setPrivateAccess( chinaA2, globalUsers, chinaInteragencyUsers, chinaAgencyAUsers, chinaPartner2Users );
        setPrivateAccess( chinaB2, globalUsers, chinaInteragencyUsers, chinaAgencyBUsers, chinaPartner2Users );
        setPrivateAccess( indiaA1, globalUsers, indiaInteragencyUsers, indiaAgencyAUsers, indiaPartner1Users );

        mechanismCategory = createDataElementCategory( 'A', brazilA1, chinaA1_1, chinaA1_2, chinaA2, chinaB2, indiaA1 );
        categoryService.addDataElementCategory( mechanismCategory );

        mechanismCategoryCombo = createCategoryCombo( 'A', mechanismCategory );
        categoryService.addDataElementCategoryCombo( mechanismCategoryCombo );

        brazilAgencyAUser.getCurrentUser().getUserCredentials().getCatDimensionConstraints().add( mechanismCategory );
        chinaAgencyAUser.getCurrentUser().getUserCredentials().getCatDimensionConstraints().add( mechanismCategory );
        chinaAgencyBUser.getCurrentUser().getUserCredentials().getCatDimensionConstraints().add( mechanismCategory );
        indiaAgencyAUser.getCurrentUser().getUserCredentials().getCatDimensionConstraints().add( mechanismCategory );
        brazilPartner1User.getCurrentUser().getUserCredentials().getCatDimensionConstraints().add( mechanismCategory );
        chinaPartner1User.getCurrentUser().getUserCredentials().getCatDimensionConstraints().add( mechanismCategory );
        chinaPartner2User.getCurrentUser().getUserCredentials().getCatDimensionConstraints().add( mechanismCategory );
        indiaPartner1User.getCurrentUser().getUserCredentials().getCatDimensionConstraints().add( mechanismCategory );

        userService.updateUser( brazilAgencyAUser.getCurrentUser() );
        userService.updateUser( chinaAgencyAUser.getCurrentUser() );
        userService.updateUser( chinaAgencyBUser.getCurrentUser() );
        userService.updateUser( indiaAgencyAUser.getCurrentUser() );
        userService.updateUser( brazilPartner1User.getCurrentUser() );
        userService.updateUser( chinaPartner1User.getCurrentUser() );
        userService.updateUser( chinaPartner2User.getCurrentUser() );
        userService.updateUser( indiaPartner1User.getCurrentUser() );

        brazilA1Combo = createCategoryOptionCombo( 'A', mechanismCategoryCombo, brazilA1 );
        chinaA1_1Combo = createCategoryOptionCombo( 'B', mechanismCategoryCombo, chinaA1_1 );
        chinaA1_2Combo = createCategoryOptionCombo( 'C', mechanismCategoryCombo, chinaA1_2 );
        chinaA2Combo = createCategoryOptionCombo( 'D', mechanismCategoryCombo, chinaA2 );
        chinaB2Combo = createCategoryOptionCombo( 'E', mechanismCategoryCombo, chinaB2 );
        indiaA1Combo = createCategoryOptionCombo( 'F', mechanismCategoryCombo, indiaA1 );

        categoryService.addDataElementCategoryOptionCombo( brazilA1Combo );
        categoryService.addDataElementCategoryOptionCombo( chinaA1_1Combo );
        categoryService.addDataElementCategoryOptionCombo( chinaA1_2Combo );
        categoryService.addDataElementCategoryOptionCombo( chinaA2Combo );
        categoryService.addDataElementCategoryOptionCombo( chinaB2Combo );
        categoryService.addDataElementCategoryOptionCombo( indiaA1Combo );

        agencyA = createCategoryOptionGroup( 'A', brazilA1, chinaA1_1, chinaA1_2, chinaA2, indiaA1 );
        agencyB = createCategoryOptionGroup( 'B', chinaB2 );
        partner1 = createCategoryOptionGroup( '1', brazilA1, chinaA1_1, chinaA1_2, indiaA1 );
        partner2 = createCategoryOptionGroup( '2', chinaA2, chinaB2 );

        categoryService.saveCategoryOptionGroup( agencyA );
        categoryService.saveCategoryOptionGroup( agencyB );
        categoryService.saveCategoryOptionGroup( partner1 );
        categoryService.saveCategoryOptionGroup( partner2 );

        setPrivateAccess( agencyA, globalUsers, brazilInteragencyUsers, chinaInteragencyUsers, indiaInteragencyUsers,
                brazilAgencyAUsers, chinaAgencyAUsers, indiaAgencyAUsers );
        setPrivateAccess( agencyB, globalUsers, chinaInteragencyUsers, chinaAgencyBUsers );
        setPrivateAccess( partner1, globalUsers, brazilInteragencyUsers, chinaInteragencyUsers, indiaInteragencyUsers,
                brazilAgencyAUsers, chinaAgencyAUsers, indiaAgencyAUsers,
                brazilPartner1Users, chinaPartner1Users, indiaPartner1Users );
        setPrivateAccess( partner2, globalUsers, chinaInteragencyUsers, chinaAgencyAUsers, chinaPartner2Users );

        agencies = new CategoryOptionGroupSet( "Agencies" );
        partners = new CategoryOptionGroupSet( "Partners" );

        categoryService.saveCategoryOptionGroupSet( partners );
        categoryService.saveCategoryOptionGroupSet( agencies );

        setPrivateAccess( agencies, globalUsers, brazilInteragencyUsers, chinaInteragencyUsers, indiaInteragencyUsers,
                brazilAgencyAUsers, chinaAgencyAUsers, chinaAgencyBUsers, chinaAgencyBUsers, indiaAgencyAUsers );

        setPrivateAccess( partners, globalUsers, brazilInteragencyUsers, chinaInteragencyUsers, indiaInteragencyUsers,
                brazilAgencyAUsers, chinaAgencyAUsers, chinaAgencyBUsers, chinaAgencyBUsers, indiaAgencyAUsers,
                brazilPartner1Users, chinaPartner1Users, chinaPartner2Users, indiaPartner1Users);

        agencies.addCategoryOptionGroup( agencyA );
        agencies.addCategoryOptionGroup( agencyB );
        partners.addCategoryOptionGroup( partner1 );
        partners.addCategoryOptionGroup( partner2 );

        agencyA.setGroupSet( agencies );
        agencyB.setGroupSet( agencies );
        partner1.setGroupSet( partners );
        partner2.setGroupSet( partners );

        categoryService.updateCategoryOptionGroupSet( partners );
        categoryService.updateCategoryOptionGroupSet( agencies );

        categoryService.updateCategoryOptionGroup( agencyA );
        categoryService.updateCategoryOptionGroup( agencyB );
        categoryService.updateCategoryOptionGroup( partner1 );
        categoryService.updateCategoryOptionGroup( partner2 );

        globalLevel1 = new DataApprovalLevel( "GlobalLevel1", 1, null );
        countryLevel2 = new DataApprovalLevel( "CountryLevel2", 3, null );
        agencyLevel3 = new DataApprovalLevel( "AgencyLevel3", 3, agencies );
        partnerLevel4 = new DataApprovalLevel( "PartnerLevel4", 3, partners );

        dataApprovalLevelService.addDataApprovalLevel( globalLevel1, 1 );
        dataApprovalLevelService.addDataApprovalLevel( countryLevel2, 2 );
        dataApprovalLevelService.addDataApprovalLevel( agencyLevel3, 3 );
        dataApprovalLevelService.addDataApprovalLevel( partnerLevel4, 4 );

        dataSetA = createDataSet( 'A', PeriodType.getPeriodTypeByName( "Monthly" ) );
        dataSetA.setCategoryCombo( mechanismCategoryCombo );
        dataSetA.setApproveData( true );
        dataSetService.addDataSet( dataSetA );

        periodA = createPeriod( "201801" );
        periodService.addPeriod( periodA );

        systemSettingManager.saveSystemSetting( KEY_HIDE_UNAPPROVED_DATA_IN_ANALYTICS, true );
        systemSettingManager.saveSystemSetting( KEY_ACCEPTANCE_REQUIRED_FOR_APPROVAL, true );
    }

    @Override
    public boolean emptyDatabaseAfterTest()
    {
        return true;
    }

    @Override
    public void tearDownTest()
    {
        setDependency( dataApprovalService, "currentUserService", currentUserService, CurrentUserService.class );
        setDependency( dataApprovalStore, "currentUserService", currentUserService, CurrentUserService.class );
        setDependency( dataApprovalLevelService, "currentUserService", currentUserService, CurrentUserService.class );
        setDependency( organisationUnitService, "currentUserService", currentUserService, CurrentUserService.class );
        setDependency( hibernateCategoryOptionGroupStore, "currentUserService", currentUserService, CurrentUserService.class );
    }

    // -------------------------------------------------------------------------
    // Test helper methods
    // -------------------------------------------------------------------------

    private void setUser( CurrentUserService mockUserService )
    {
        if ( mockUserService != currentMockUserService )
        {
            setDependency( dataApprovalService, "currentUserService", mockUserService, CurrentUserService.class );
            setDependency( dataApprovalStore, "currentUserService", mockUserService, CurrentUserService.class );
            setDependency( dataApprovalLevelService, "currentUserService", mockUserService, CurrentUserService.class );
            setDependency( organisationUnitService, "currentUserService", mockUserService, CurrentUserService.class );
            setDependency( hibernateCategoryOptionGroupStore, "currentUserService", mockUserService, CurrentUserService.class );

            currentMockUserService = mockUserService;
        }
    }

    private String getOptionNamesFromCombo( DataElementCategoryOptionCombo combo )
    {
        return combo.getName().substring( 1, combo.getName().length() - 1);
    }

    private String getStatusString( DataApprovalStatus status )
    {
        DataApproval a = status.getDataApproval();
        String approval = a == null ? "approval=null" :
                "ou=" + ( a.getOrganisationUnit() == null ? "(null)" : a.getOrganisationUnit().getName() )
                        + " mechanism=" + ( a.getAttributeOptionCombo() == null ? "(null)" : getOptionNamesFromCombo( a.getAttributeOptionCombo() ) )
                        + " level=" + ( a.getDataApprovalLevel() == null ? "(null)" : a.getDataApprovalLevel().getLevel() );

        DataApprovalPermissions p = status.getPermissions();

        return approval + " " + status.getState().toString()
                + " approve=" + ( p.isMayApprove() ? "T" : "F" )
                + " unapprove=" + ( p.isMayUnapprove() ? "T" : "F" )
                + " accept=" + ( p.isMayAccept() ? "T" : "F" )
                + " unaccept=" + ( p.isMayUnaccept() ? "T" : "F" )
                + " read=" + ( p.isMayReadData() ? "T" : "F" );
    }

    /**
     * Returns approval status and permissions information as a string.
     * This allows a test to compare the result against a string and test
     * several things at once. More importantly, it shows in the log
     * all of the ways in which the test status and permissions differs from
     * expected, instead of showing only one different value. This can
     * save time in understanding the difference between the expected value
     * and the test result.
     *
     * @param mockUserService user to execute the test
     * @param dataSet Approval data set
     * @param period Approval period
     * @param organisationUnit Approval orgaisation unit
     * @param attributeOptionCombo Approval attribute option combination
     * @return A String representing the state, level, and allowed user actions
     */
    private String getStatusAndPermissions( CurrentUserService mockUserService, DataSet dataSet,
                                         Period period, OrganisationUnit organisationUnit,
                                         DataElementCategoryOptionCombo attributeOptionCombo )
    {
        setUser( mockUserService );

        DataApprovalStatus status = dataApprovalService.getDataApprovalStatusAndPermissions( dataSet, period, organisationUnit, attributeOptionCombo );

        return getStatusString( status );
    }

    private String[] getUserApprovalsAndPermissions( CurrentUserService mockUserService, DataSet dataSet, Period period, OrganisationUnit orgUnit )
    {
        setUser( mockUserService );

        List<DataApprovalStatus> approvals = dataApprovalService.getUserDataApprovalsAndPermissions( asSet( dataSet ), period, orgUnit );

        List<String> approvalStrings = new ArrayList<>();

        for ( DataApprovalStatus status : approvals )
        {
            approvalStrings.add( getStatusString ( status ) );
        }

        Collections.sort( approvalStrings );

        return Arrays.copyOf( approvalStrings.toArray(), approvalStrings.size(), String[].class );
    }

    private boolean approve( CurrentUserService mockUserService, DataApprovalLevel dataApprovalLevel,
                             DataSet dataSet, Period period, OrganisationUnit organisationUnit,
                             DataElementCategoryOptionCombo mechanismCombo )
    {
        DataApproval da = new DataApproval( dataApprovalLevel, dataSet, period,
                organisationUnit, mechanismCombo, false, dateA, userA );

        setUser( mockUserService );

        try
        {
            dataApprovalService.approveData( Arrays.asList( da ) );

            return true;
        }
        catch ( DataApprovalException ex )
        {
            return false;
        }
    }

    private boolean unapprove( CurrentUserService mockUserService, DataApprovalLevel dataApprovalLevel,
                               DataSet dataSet, Period period, OrganisationUnit organisationUnit,
                               DataElementCategoryOptionCombo mechanismCombo )
    {
        DataApproval da = new DataApproval( dataApprovalLevel, dataSet, period,
                organisationUnit, mechanismCombo, false, dateA, userA );

        setUser( mockUserService );

        try
        {
            dataApprovalService.unapproveData( Arrays.asList( da ) );

            return true;
        }
        catch ( DataApprovalException ex )
        {
            return false;
        }
    }

    private boolean accept( CurrentUserService mockUserService, DataApprovalLevel dataApprovalLevel,
                            DataSet dataSet, Period period, OrganisationUnit organisationUnit,
                            DataElementCategoryOptionCombo mechanismCombo )
    {
        DataApproval da = new DataApproval( dataApprovalLevel, dataSet, period,
                organisationUnit, mechanismCombo, false, dateA, userA );

        setUser( mockUserService );

        try
        {
            dataApprovalService.acceptData( Arrays.asList( da ) );

            return true;
        }
        catch ( DataApprovalException ex )
        {
            return false;
        }
    }

    private boolean unaccept( CurrentUserService mockUserService, DataApprovalLevel dataApprovalLevel,
                              DataSet dataSet, Period period, OrganisationUnit organisationUnit,
                              DataElementCategoryOptionCombo mechanismCombo )
    {
        DataApproval da = new DataApproval( dataApprovalLevel, dataSet, period,
                organisationUnit, mechanismCombo, false, dateA, userA );

        setUser( mockUserService );

        try
        {
            dataApprovalService.unacceptData( Arrays.asList( da ) );

            return true;
        }
        catch ( DataApprovalException ex )
        {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Generate test code helper methods
    // -------------------------------------------------------------------------

    private void generateUserApprovalsAndPermissions( CurrentUserService mockUserService, DataSet dataSet, Period period, OrganisationUnit orgUnit )
    {
        String[] approvalStrings = getUserApprovalsAndPermissions( mockUserService, dataSet, period, orgUnit );

        System.out.println( "assertArrayEquals( new String[] {" );

        int count = 0;

        for ( String s : approvalStrings )
        {
            System.out.println( "                \"" + s + "\"" + ( ++count < approvalStrings.length ? "," : " }," ) );
        }

        String username = mockUserService.getCurrentUsername();

        System.out.println( "            userApprovalsAndPermissions( "
            + username.substring( 0, 1 ).toLowerCase() + username.substring( 1, username.length() )
            + ", dataSetA, periodA, null ) );" );

        System.out.println();
    }

    private void generateAllApprovalsAndPermissions()
    {
        generateUserApprovalsAndPermissions( superUser, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( globalConsultant, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( globalUser, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( globalReadEverything, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( brazilInteragencyUser, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( chinaInteragencyUser, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( indiaInteragencyUser, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( brazilAgencyAUser, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( chinaAgencyAUser, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( chinaAgencyBUser, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( indiaAgencyAUser, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( brazilPartner1User, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( chinaPartner1User, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( chinaPartner2User, dataSetA, periodA, null );
        generateUserApprovalsAndPermissions( indiaPartner1User, dataSetA, periodA, null );
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    public void test()
    {
        // ---------------------------------------------------------------------
        // Nothing approved yet
        // ---------------------------------------------------------------------

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( superUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalConsultant, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( globalUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalReadEverything, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyBUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( brazilPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner2User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( indiaPartner1User, dataSetA, periodA, null ) );

        // ---------------------------------------------------------------------
        // Approve ChinaA1_1 at level 4
        // ---------------------------------------------------------------------

        assertTrue( approve( superUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unapprove( superUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( approve( globalConsultant, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unapprove( globalConsultant, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( globalUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( globalReadEverything, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( brazilInteragencyUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaInteragencyUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( indiaInteragencyUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( brazilAgencyAUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaAgencyAUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaAgencyBUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( indiaAgencyAUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( brazilPartner1User, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaPartner2User, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( indiaPartner1User, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( approve( chinaPartner1User, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        // ---------------------------------------------------------------------
        // ChinaA1_1 is approved at level 4
        // ---------------------------------------------------------------------

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( superUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalConsultant, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( globalUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalReadEverything, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyBUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( brazilPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner2User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( indiaPartner1User, dataSetA, periodA, null ) );

        // ---------------------------------------------------------------------
        // Approve ChinaA1_2 at level 4
        // ---------------------------------------------------------------------

        //TODO: test approving at wrong levels

        assertTrue( approve( superUser, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );
        assertTrue( unapprove( superUser, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );

        assertTrue( approve( globalConsultant, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );
        assertTrue( unapprove( globalConsultant, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );

        assertFalse( approve( globalUser, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );
        assertFalse( approve( globalReadEverything, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );

        assertFalse( approve( brazilInteragencyUser, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );
        assertFalse( approve( chinaInteragencyUser, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );
        assertFalse( approve( indiaInteragencyUser, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );

        assertFalse( approve( brazilAgencyAUser, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );
        assertFalse( approve( chinaAgencyAUser, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );
        assertFalse( approve( chinaAgencyBUser, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );
        assertFalse( approve( indiaAgencyAUser, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );

        assertFalse( approve( brazilPartner1User, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );
        assertFalse( approve( chinaPartner2User, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );
        assertFalse( approve( indiaPartner1User, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );

        assertTrue( approve( chinaPartner1User, partnerLevel4, dataSetA, periodA, china, chinaA1_2Combo ) );

        // ---------------------------------------------------------------------
        // ChinaA1_1 is approved at level 4
        // ChinaA1_2 is approved at level 4
        // ---------------------------------------------------------------------

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( superUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalConsultant, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( globalUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalReadEverything, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyBUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( brazilPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner2User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( indiaPartner1User, dataSetA, periodA, null ) );

        // ---------------------------------------------------------------------
        // Accept ChinaA1_1 at level 4
        // ---------------------------------------------------------------------

        assertTrue( accept( superUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unaccept( superUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( accept( globalConsultant, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unaccept( globalConsultant, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( globalUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( globalReadEverything, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( brazilInteragencyUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaInteragencyUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( indiaInteragencyUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( brazilAgencyAUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaAgencyBUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( indiaAgencyAUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( brazilPartner1User, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaPartner1User, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaPartner2User, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( indiaPartner1User, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( accept( chinaAgencyAUser, partnerLevel4, dataSetA, periodA, china, chinaA1_1Combo ) );

        // ---------------------------------------------------------------------
        // ChinaA1_1 is accepted at level 4
        // ChinaA1_2 is approved at level 4
        // ---------------------------------------------------------------------

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 ACCEPTED_HERE approve=T unapprove=T accept=F unaccept=T read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( superUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 ACCEPTED_HERE approve=T unapprove=T accept=F unaccept=T read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalConsultant, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_1 level=4 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( globalUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=4 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalReadEverything, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 ACCEPTED_HERE approve=T unapprove=T accept=F unaccept=T read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyBUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( brazilPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=4 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner2User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( indiaPartner1User, dataSetA, periodA, null ) );

        // ---------------------------------------------------------------------
        // Approve ChinaA1_1 at level 3
        // ---------------------------------------------------------------------
        assertTrue( approve( superUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unapprove( superUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( approve( globalConsultant, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unapprove( globalConsultant, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( globalUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( globalReadEverything, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( brazilInteragencyUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaInteragencyUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( indiaInteragencyUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( brazilAgencyAUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaAgencyBUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( indiaAgencyAUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( brazilPartner1User, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaPartner1User, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaPartner2User, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( indiaPartner1User, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( approve( chinaAgencyAUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        // ---------------------------------------------------------------------
        // ChinaA1_1 is approved at level 3
        // ChinaA1_2 is approved at level 4
        // ---------------------------------------------------------------------

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=3 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( superUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=3 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalConsultant, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_1 level=3 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( globalUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=3 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalReadEverything, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=3 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=3 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyBUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( brazilPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=3 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner2User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( indiaPartner1User, dataSetA, periodA, null ) );

        // ---------------------------------------------------------------------
        // Accept ChinaA1_1 at level 3
        // ---------------------------------------------------------------------

        assertTrue( accept( superUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unaccept( superUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( accept( globalConsultant, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unaccept( globalConsultant, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( globalUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( globalReadEverything, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( brazilInteragencyUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( indiaInteragencyUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( brazilAgencyAUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaAgencyAUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaAgencyBUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( indiaAgencyAUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( brazilPartner1User, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaPartner1User, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaPartner2User, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( indiaPartner1User, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( accept( chinaInteragencyUser, agencyLevel3, dataSetA, periodA, china, chinaA1_1Combo ) );

        // ---------------------------------------------------------------------
        // ChinaA1_1 is accepted at level 3
        // ChinaA1_2 is approved at level 4
        // ---------------------------------------------------------------------

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=3 ACCEPTED_HERE approve=T unapprove=T accept=F unaccept=T read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( superUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=3 ACCEPTED_HERE approve=T unapprove=T accept=F unaccept=T read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalConsultant, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_1 level=3 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( globalUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=3 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalReadEverything, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=3 ACCEPTED_HERE approve=T unapprove=T accept=F unaccept=T read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=3 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyBUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( brazilPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=3 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner2User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( indiaPartner1User, dataSetA, periodA, null ) );

        // ---------------------------------------------------------------------
        // Approve ChinaA1_1 at level 2
        // ---------------------------------------------------------------------

        assertTrue( approve( superUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unapprove( superUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( approve( globalConsultant, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unapprove( globalConsultant, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( globalUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( globalReadEverything, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( brazilInteragencyUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( indiaInteragencyUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( brazilAgencyAUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaAgencyAUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaAgencyBUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( indiaAgencyAUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( approve( brazilPartner1User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaPartner1User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( chinaPartner2User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( approve( indiaPartner1User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( approve( chinaInteragencyUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        // ---------------------------------------------------------------------
        // ChinaA1_1 is approved at level 2
        // ChinaA1_2 is approved at level 4
        // ---------------------------------------------------------------------

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=2 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( superUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=2 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalConsultant, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_1 level=2 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( globalUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=2 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalReadEverything, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=2 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=2 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyBUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( brazilPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=2 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner2User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( indiaPartner1User, dataSetA, periodA, null ) );

        // ---------------------------------------------------------------------
        // Accept ChinaA1_1 at level 2
        // ---------------------------------------------------------------------

        assertTrue( accept( superUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unaccept( superUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( accept( globalConsultant, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( unaccept( globalConsultant, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( globalReadEverything, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( brazilInteragencyUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaInteragencyUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( indiaInteragencyUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( brazilAgencyAUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaAgencyAUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaAgencyBUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( indiaAgencyAUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertFalse( accept( brazilPartner1User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaPartner1User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( chinaPartner2User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertFalse( accept( indiaPartner1User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( accept( globalUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        // ---------------------------------------------------------------------
        // ChinaA1_1 is accepted at level 2
        // ChinaA1_2 is approved at level 4
        // ---------------------------------------------------------------------

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=2 ACCEPTED_HERE approve=T unapprove=T accept=F unaccept=T read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( superUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=2 ACCEPTED_HERE approve=T unapprove=T accept=F unaccept=T read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalConsultant, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_1 level=2 ACCEPTED_HERE approve=T unapprove=T accept=F unaccept=T read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( globalUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=2 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalReadEverything, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=2 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=2 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyBUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( brazilPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=2 ACCEPTED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner2User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( indiaPartner1User, dataSetA, periodA, null ) );

        // ---------------------------------------------------------------------
        // Approve ChinaA1_1 at level 1
        // ---------------------------------------------------------------------

        assertFalse( approve( superUser, globalLevel1, dataSetA, periodA, china, chinaA1_1Combo ) ); // Wrong org unit.

        assertTrue( approve( superUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertTrue( unapprove( superUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertTrue( approve( globalConsultant, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertTrue( unapprove( globalConsultant, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertFalse( approve( globalReadEverything, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertFalse( approve( brazilInteragencyUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( approve( chinaInteragencyUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( approve( indiaInteragencyUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertFalse( approve( brazilAgencyAUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( approve( chinaAgencyAUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( approve( chinaAgencyBUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( approve( indiaAgencyAUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertFalse( approve( brazilPartner1User, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( approve( chinaPartner1User, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( approve( chinaPartner2User, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( approve( indiaPartner1User, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertTrue( approve( globalUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        // ---------------------------------------------------------------------
        // ChinaA1_1 is approved at level 1
        // ChinaA1_2 is approved at level 4
        // ---------------------------------------------------------------------

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=1 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( superUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=1 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalConsultant, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA1_1 level=1 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( globalUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=1 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( globalReadEverything, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=1 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaInteragencyUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( brazilAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=1 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( chinaAgencyBUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=F unapprove=F accept=F unaccept=F read=F" },
            getUserApprovalsAndPermissions( indiaAgencyAUser, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( brazilPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA1_1 level=1 APPROVED_HERE approve=F unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner1User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( chinaPartner2User, dataSetA, periodA, null ) );

        assertArrayEquals( new String[] {
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( indiaPartner1User, dataSetA, periodA, null ) );

        // ---------------------------------------------------------------------
        // Unapprove ChinaA1_1 at level 1
        // ---------------------------------------------------------------------

        assertTrue( unapprove( superUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertTrue( approve( superUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertTrue( unapprove( globalConsultant, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertTrue( approve( globalConsultant, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertFalse( unapprove( globalReadEverything, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertFalse( unapprove( brazilInteragencyUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( unapprove( chinaInteragencyUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( unapprove( indiaInteragencyUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertFalse( unapprove( brazilAgencyAUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( unapprove( chinaAgencyAUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( unapprove( chinaAgencyBUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( unapprove( indiaAgencyAUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertFalse( unapprove( brazilPartner1User, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( unapprove( chinaPartner1User, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( unapprove( chinaPartner2User, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );
        assertFalse( unapprove( indiaPartner1User, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertTrue( unapprove( globalUser, globalLevel1, dataSetA, periodA, global, chinaA1_1Combo ) );

        assertArrayEquals( new String[] {
                "ou=Brazil mechanism=BrazilA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaA1_1 level=2 ACCEPTED_HERE approve=T unapprove=T accept=F unaccept=T read=T",
                "ou=China mechanism=ChinaA1_2 level=4 APPROVED_HERE approve=F unapprove=T accept=T unaccept=F read=T",
                "ou=China mechanism=ChinaA2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=China mechanism=ChinaB2 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T",
                "ou=India mechanism=IndiaA1 level=4 UNAPPROVED_READY approve=T unapprove=F accept=F unaccept=F read=T" },
            getUserApprovalsAndPermissions( superUser, dataSetA, periodA, null ) );

        // ---------------------------------------------------------------------
        // Unaccept ChinaA1_1 at level 2
        // ---------------------------------------------------------------------

        assertTrue( unaccept( superUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( accept( superUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        assertTrue( unaccept( globalConsultant, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
        assertTrue( accept( globalConsultant, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );

        //TODO: Fix and test:
//        assertFalse( accept( globalReadEverything, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//
//        assertFalse( unaccept( brazilInteragencyUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//        assertFalse( unaccept( chinaInteragencyUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//        assertFalse( unaccept( indiaInteragencyUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//
//        assertFalse( unaccept( brazilAgencyAUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//        assertFalse( unaccept( chinaAgencyAUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//        assertFalse( unaccept( chinaAgencyBUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//        assertFalse( unaccept( indiaAgencyAUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//
//        assertFalse( unaccept( brazilPartner1User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//        assertFalse( unaccept( chinaPartner1User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//        assertFalse( unaccept( chinaPartner2User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//        assertFalse( unaccept( indiaPartner1User, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//
//        assertTrue( unaccept( globalUser, countryLevel2, dataSetA, periodA, china, chinaA1_1Combo ) );
//
//        generateAllApprovalsAndPermissions();
    }
}
