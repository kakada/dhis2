/* global dhis2, angular */

dhis2.util.namespace('dhis2.tc');

// whether current user has any organisation units
dhis2.tc.emptyOrganisationUnits = false;

var i18n_no_orgunits = 'No organisation unit attached to current user, no data entry possible';
var i18n_offline_notification = 'You are offline, data will be stored locally';
var i18n_online_notification = 'You are online';
var i18n_need_to_sync_notification = 'There is data stored locally, please upload to server';
var i18n_sync_now = 'Upload';
var i18n_sync_success = 'Upload to server was successful';
var i18n_sync_failed = 'Upload to server failed, please try again later';
var i18n_uploading_data_notification = 'Uploading locally stored data to the server';

var optionSetsInPromise = [];
var attributesInPromise = [];

dhis2.tc.store = null;
dhis2.tc.memoryOnly = $('html').hasClass('ie7') || $('html').hasClass('ie8');
var adapters = [];    
if( dhis2.tc.memoryOnly ) {
    adapters = [ dhis2.storage.InMemoryAdapter ];
} else {
    adapters = [ dhis2.storage.IndexedDBAdapter, dhis2.storage.DomLocalStorageAdapter, dhis2.storage.InMemoryAdapter ];
}

dhis2.tc.store = new dhis2.storage.Store({
    name: 'dhis2tc',
    adapters: [dhis2.storage.IndexedDBAdapter, dhis2.storage.DomSessionStorageAdapter, dhis2.storage.InMemoryAdapter],
    objectStores: ['programs', 'programStages', 'trackedEntities', 'trackedEntityForms', 'attributes', 'relationshipTypes', 'optionSets', 'programValidations', 'ouLevels']      
});

(function($) {
    $.safeEach = function(arr, fn)
    {
        if (arr)
        {
            $.each(arr, fn);
        }
    };
})(jQuery);

/**
 * Page init. The order of events is:
 *
 * 1. Load ouwt 2. Load meta-data (and notify ouwt) 3. Check and potentially
 * download updated forms from server
 */
$(document).ready(function()
{
    $.ajaxSetup({
        type: 'POST',
        cache: false
    });

    $('#loaderSpan').show();
});

$(document).bind('dhis2.online', function(event, loggedIn)
{
    if (loggedIn)
    {
        if (dhis2.tc.emptyOrganisationUnits) {
            setHeaderMessage(i18n_no_orgunits);
        }
        else {
            setHeaderDelayMessage(i18n_online_notification);
        }
    }
    else
    {
        var form = [
            '<form style="display:inline;">',
            '<label for="username">Username</label>',
            '<input name="username" id="username" type="text" style="width: 70px; margin-left: 10px; margin-right: 10px" size="10"/>',
            '<label for="password">Password</label>',
            '<input name="password" id="password" type="password" style="width: 70px; margin-left: 10px; margin-right: 10px" size="10"/>',
            '<button id="login_button" type="button">Login</button>',
            '</form>'
        ].join('');

        setHeaderMessage(form);
        ajax_login();
    }
});

$(document).bind('dhis2.offline', function()
{
    if (dhis2.tc.emptyOrganisationUnits) {
        setHeaderMessage(i18n_no_orgunits);
    }
    else {
        setHeaderMessage(i18n_offline_notification);
    }
});

function ajax_login()
{
    $('#login_button').bind('click', function()
    {
        var username = $('#username').val();
        var password = $('#password').val();

        $.post('../dhis-web-commons-security/login.action', {
            'j_username': username,
            'j_password': password
        }).success(function()
        {
            var ret = dhis2.availability.syncCheckAvailability();

            if (!ret)
            {
                alert(i18n_ajax_login_failed);
            }
        });
    });
}

// -----------------------------------------------------------------------------
// Metadata downloading
// -----------------------------------------------------------------------------

function downloadMetaData()
{
    console.log('Loading required meta-data');
    var def = $.Deferred();
    var promise = def.promise();

    promise = promise.then( dhis2.tc.store.open );
    promise = promise.then( getUserRoles );
    promise = promise.then( getCalendarSetting );
    promise = promise.then( getRelationships );       
    promise = promise.then( getTrackedEntities );
    promise = promise.then( getMetaPrograms );     
    promise = promise.then( getPrograms );     
    promise = promise.then( getProgramStages );    
    promise = promise.then( getOptionSetsForDataElements );
    promise = promise.then( getMetaTrackeEntityAttributes );
    promise = promise.then( getTrackedEntityAttributes );
    promise = promise.then( getOptionSetsForAttributes );
    promise = promise.then( getMetaProgramValidations );
    promise = promise.then( getProgramValidations );    
    promise = promise.then( getTrackedEntityForms );
    promise = promise.then( getOrgUnitLevels );
    promise.done(function() {
        
        //Enable ou selection after meta-data has downloaded
        $( "#orgUnitTree" ).removeClass( "disable-clicks" );
        
        console.log( 'Finished loading meta-data' );        
        selection.responseReceived(); 
    });

    def.resolve();
    
}

function getUserRoles()
{
    var SessionStorageService = angular.element('body').injector().get('SessionStorageService');
    
    if( SessionStorageService.get('USER_ROLES') ){
       return; 
    }
    
    var def = $.Deferred();

    $.ajax({
        url: '../api/me.json?fields=id,name,userCredentials[userRoles[id,authorities]]',
        type: 'GET'
    }).done(function(response) {
        SessionStorageService.set('USER_ROLES', response);
        def.resolve();
    }).fail(function(){
        def.resolve();
    });

    return def.promise();
}

function getCalendarSetting()
{
    if(localStorage['CALENDAR_SETTING']){
       return; 
    }
    
    var def = $.Deferred();

    $.ajax({
        url: '../api/systemSettings?key=keyCalendar&key=keyDateFormat',
        type: 'GET'
    }).done(function(response) {
        localStorage['CALENDAR_SETTING'] = JSON.stringify(response);
        def.resolve();
    }).fail(function(){
        def.resolve();
    });

    return def.promise();
}

function getRelationships()
{    
    dhis2.tc.store.getKeys( 'relationshipTypes').done(function(res){        
        if(res.length > 0){
            return;
        }
        var def = $.Deferred();

        $.ajax({
            url: '../api/relationshipTypes.json?paging=false&fields=id,name,aIsToB,bIsToA,displayName',
            type: 'GET'
        }).done(function(response) {        
            dhis2.tc.store.setAll( 'relationshipTypes', response.relationshipTypes );
            def.resolve();        
        }).fail(function(){
            def.resolve();
        });

        return def.promise();
    });    
}

function getTrackedEntities()
{
    dhis2.tc.store.getKeys('trackedEntities').done(function(res){
        if(res.length > 0){
            return;
        }
        
        var def = $.Deferred();

        $.ajax({
            url: '../api/trackedEntities',
            type: 'GET',
            data: 'viewClass=detailed&paging=false'
        }).done(function(response) {
            dhis2.tc.store.setAll( 'trackedEntities', response.trackedEntities );        
            def.resolve();
        }).fail(function(){
            def.resolve();
        });

        return def.promise();
    });    
}

function getMetaPrograms()
{
    var def = $.Deferred();

    $.ajax({
        url: '../api/programs.json',
        type: 'GET',
        data:'filter=type:eq:1&paging=false&fields=id,version,programTrackedEntityAttributes[trackedEntityAttribute[id,optionSet[id,version]]],programStages[id,name,version,minDaysFromStart,standardInterval,periodType,generatedByEnrollmentDate,reportDateDescription,repeatable,autoGenerateEvent,openAfterEnrollment,reportDateToUse,programStageDataElements[dataElement[optionSet[id,version]]]]'
    }).done( function(response) {          
        var programs = [];
        _.each( _.values( response.programs ), function ( program ) { 
            programs.push(program);
        });
        
        def.resolve( programs );
    }).fail(function(){
        def.resolve( null );
    });
    
    return def.promise(); 
}

function getPrograms( programs )
{
    if( !programs ){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();

    _.each( _.values( programs ), function ( program ) {
        build = build.then(function() {
            var d = $.Deferred();
            var p = d.promise();
            dhis2.tc.store.get('programs', program.id).done(function(obj) {
                if(!obj || obj.version !== program.version) {
                    promise = promise.then( getProgram( program.id ) );
                }

                d.resolve();
            });

            return p;
        });
    });

    build.done(function() {
        def.resolve();

        promise = promise.done( function () {
            mainDef.resolve( programs );
        } );
    }).fail(function(){
        mainDef.resolve( null );
    });

    builder.resolve();

    return mainPromise;
}

function getProgram( id )
{
    return function() {
        return $.ajax( {
            url: '../api/programs.json',
            type: 'GET',
            data: 'paging=false&filter=id:eq:' + id +'&fields=id,name,type,version,dataEntryMethod,dateOfEnrollmentDescription,dateOfIncidentDescription,displayIncidentDate,ignoreOverdueEvents,selectEnrollmentDatesInFuture,selectIncidentDatesInFuture,onlyEnrollOnce,externalAccess,displayOnAllOrgunit,registration,relationshipText,relationshipFromA,relatedProgram[id,name],relationshipType[id,name],trackedEntity[id,name,description],userRoles[id,name],organisationUnits[id,name],userRoles[id,name],programStages[id,name,version,minDaysFromStart,standardInterval,periodType,generatedByEnrollmentDate,reportDateDescription,repeatable,autoGenerateEvent,openAfterEnrollment,reportDateToUse],programTrackedEntityAttributes[displayInList,mandatory,allowFutureDate,trackedEntityAttribute[id,unique]]'
        }).done( function( response ){
            
            _.each( _.values( response.programs ), function ( program ) { 
                
                var ou = {};
                if(program.organisationUnits){
                    _.each(_.values( program.organisationUnits), function(o){
                        ou[o.id] = o.name;
                    });
                }

                program.organisationUnits = ou;

                var ur = {};
                
                if(program.userRoles){
                    _.each(_.values( program.userRoles), function(u){
                        ur[u.id] = u.name;
                    });
                }                

                program.userRoles = ur;

                dhis2.tc.store.set( 'programs', program );

            });         
        });
    };
}

function getProgramStages( programs )
{
    if( !programs ){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();

    _.each( _.values( programs ), function ( program ) {
        
        if(program.programStages){
            _.each(_.values(program.programStages), function(programStage){
                build = build.then(function() {
                    var d = $.Deferred();
                    var p = d.promise();
                    dhis2.tc.store.get('programStages', programStage.id).done(function(obj) {
                        if(!obj || obj.version !== programStage.version) {
                            promise = promise.then( getProgramStage( programStage.id ) );
                        }
                        d.resolve();
                    });
                    return p;
                });            
            });
        }                             
    });

    build.done(function() {
        def.resolve();

        promise = promise.done( function () {
            mainDef.resolve( programs );
        } );
    }).fail(function(){
        mainDef.resolve( null );
    });

    builder.resolve();

    return mainPromise;    
}

function getProgramStage( id )
{
    return function() {
        return $.ajax( {
            url: '../api/programStages.json',
            type: 'GET',
            data: 'filter=id:eq:' + id +'&fields=id,name,sortOrder,version,dataEntryForm,captureCoordinates,blockEntryForm,autoGenerateEvent,allowGenerateNextVisit,generatedByEnrollmentDate,remindCompleted,reportDateDescription,minDaysFromStart,repeatable,openAfterEnrollment,standardInterval,periodType,reportDateToUse,programStageSections[id,name,programStageDataElements[dataElement[id]]],programStageDataElements[displayInReports,allowProvidedElsewhere,allowFutureDate,compulsory,dataElement[id,code,name,formName,type,optionSetValue,optionSet[id]]]'
        }).done( function( response ){            
            _.each( _.values( response.programStages ), function( programStage ) {
                dhis2.tc.store.set( 'programStages', programStage );
            });
        });
    };
}

function getOptionSetsForDataElements( programs )
{
    if( !programs ){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();    

    _.each( _.values( programs ), function ( program ) {
        
        if(program.programStages){
            _.each(_.values( program.programStages), function( programStage) {
                
                if(programStage.programStageDataElements){
                    _.each(_.values( programStage.programStageDataElements), function(prStDe){            
                        if( prStDe.dataElement.optionSet && prStDe.dataElement.optionSet.id ){
                            build = build.then(function() {
                                var d = $.Deferred();
                                var p = d.promise();
                                dhis2.tc.store.get('optionSets', prStDe.dataElement.optionSet.id).done(function(obj) {                                    
                                    if( (!obj || obj.version !== prStDe.dataElement.optionSet.version) && optionSetsInPromise.indexOf(prStDe.dataElement.optionSet.id) === -1) {                                
                                        optionSetsInPromise.push( prStDe.dataElement.optionSet.id );
                                        promise = promise.then( getOptionSet( prStDe.dataElement.optionSet.id ) );
                                    }
                                    d.resolve();
                                });

                                return p;
                            });
                        }            
                    });
                }                
            });
        }                                      
    });

    build.done(function() {
        def.resolve();

        promise = promise.done( function () {
            mainDef.resolve( programs );
        } );
    }).fail(function(){
        mainDef.resolve( null );
    });

    builder.resolve();

    return mainPromise;    
}

function getOptionSet( id )
{
    return function() {
        return $.ajax( {
            url: '../api/optionSets.json',
            type: 'GET',
            data: 'filter=id:eq:' + id +'&fields=id,name,version,options[id,name,code]'
        }).done( function( response ){            
            _.each( _.values( response.optionSets ), function( optionSet ) {                
                dhis2.tc.store.set( 'optionSets', optionSet );
            });
        });
    };
}

function getMetaProgramValidations( programs )
{
    if( !programs ){
        return;
    }
    
    var def = $.Deferred();
    
    var programIds = [];
    _.each( _.values( programs ), function ( program ) { 
        if( program.id ) {
            programIds.push( program.id );
        }
    });
    
    $.ajax({
        url: '../api/programValidations.json',
        type: 'GET',
        data:'paging=false&fields=id,program[id]'
    }).done( function(response) {          
        var programValidations = [];
        _.each( _.values( response.programValidations ), function ( programValidation ) { 
            if( programValidation &&
                programValidation.id &&
                programValidation.program &&
                programValidation.program.id &&
                programIds.indexOf( programValidation.program.id ) !== -1) {
            
                programValidations.push( programValidation );
            }  
            
        });
        
        def.resolve( {programValidations: programValidations, programs: programs} );
        
    }).fail(function(){
        def.resolve( null );
    });
    
    return def.promise();    
}

function getProgramValidations( data )
{
    if( !data || !data.programValidations ){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();

    _.each( _.values( data.programValidations ), function ( programValidation ) {
        build = build.then(function() {
            var d = $.Deferred();
            var p = d.promise();
            dhis2.tc.store.get('programValidations', programValidation.id).done(function(obj) {
                if(!obj) {
                    promise = promise.then( getProgramValidation( programValidation.id ) );
                }
                d.resolve();
            });

            return p;
        });
    });

    build.done(function() {
        def.resolve();

        promise = promise.done( function () {
            mainDef.resolve( data.programs );
        } );
    }).fail(function(){
        mainDef.resolve( null );
    });

    builder.resolve();

    return mainPromise;
}

function getProgramValidation( id )
{
    return function() {
        return $.ajax( {
            url: '../api/programValidations.json',
            type: 'GET',
            data: 'paging=false&filter=id:eq:' + id +'&fields=id,name,operator,displayName,rightSide,leftSide,program[id]'
        }).done( function( response ){
            
            _.each( _.values( response.programValidations ), function ( programValidation ) { 
                
                if( programValidation &&
                    programValidation.id &&
                    programValidation.program &&
                    programValidation.program.id ) {
                    dhis2.tc.store.set( 'programValidations', programValidation );
                }
            });
        });
    };
}

function getMetaTrackeEntityAttributes( programs ){
    
    var def = $.Deferred();
    
    $.ajax({
        url: '../api/trackedEntityAttributes.json',
        type: 'GET',
        data:'paging=false&filter=displayInListNoProgram:eq:true&fields=id,optionSet[id,version]'
    }).done( function(response) {          
        var trackedEntityAttributes = [];
        _.each( _.values( response.trackedEntityAttributes ), function ( trackedEntityAttribute ) {             
            if( trackedEntityAttribute && trackedEntityAttribute.id ) {            
                trackedEntityAttributes.push( trackedEntityAttribute );
            }            
        });
        
        _.each( _.values( programs ), function ( program ) {        
            if(program.programTrackedEntityAttributes){
                _.each(_.values(program.programTrackedEntityAttributes), function(teAttribute){                    
                    trackedEntityAttributes.push(teAttribute.trackedEntityAttribute);
                });
            }
        });
        
        def.resolve( {trackedEntityAttributes: trackedEntityAttributes, programs: programs} );
        
    }).fail(function(){
        def.resolve( null );
    });
    
    return def.promise();
}

function getTrackedEntityAttributes( data )
{
    if( !data.trackedEntityAttributes ){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();        

    _.each(_.values(data.trackedEntityAttributes), function(teAttribute){        
        build = build.then(function() {
            var d = $.Deferred();
            var p = d.promise();
            dhis2.tc.store.get('attributes', teAttribute.id).done(function(obj) {
                if((!obj || obj.version !== teAttribute.version) && attributesInPromise.indexOf(teAttribute.id) === -1) {
                    attributesInPromise.push( teAttribute.id );
                    promise = promise.then( getAttribute( teAttribute.id ) );
                }
                d.resolve();
            });
            return p;
        });            
    });

    build.done(function() {
        def.resolve();

        promise = promise.done( function () {
            mainDef.resolve( {trackedEntityAttributes: data.trackedEntityAttributes, programs: data.programs} );
        } );
    }).fail(function(){
        mainDef.resolve( null );
    });

    builder.resolve();

    return mainPromise;    
}

function getAttribute( id )
{
    return function() {
        return $.ajax( {
            url: '../api/trackedEntityAttributes.json',
            type: 'GET',
            data: 'filter=id:eq:' + id +'&paging=false&fields=id,name,code,version,description,valueType,optionSetValue,confidential,inherit,sortOrderInVisitSchedule,sortOrderInListNoProgram,displayOnVisitSchedule,displayInListNoProgram,unique,optionSet[id,version]'
        }).done( function( response ){            
            _.each( _.values( response.trackedEntityAttributes ), function( teAttribute ) {
                dhis2.tc.store.set( 'attributes', teAttribute );
            });
        });
    };
}


function getOptionSetsForAttributes( data )
{
    if( !data.trackedEntityAttributes ){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();

    _.each(_.values( data.trackedEntityAttributes), function( teAttribute) {           
        if( teAttribute.optionSet && teAttribute.optionSet.id ){
            build = build.then(function() {
                var d = $.Deferred();
                var p = d.promise();
                dhis2.tc.store.get('optionSets', teAttribute.optionSet.id).done(function(obj) {                            
                    if((!obj || obj.version !== teAttribute.optionSet.version) && optionSetsInPromise.indexOf(teAttribute.optionSet.id) === -1) {                                
                        optionSetsInPromise.push(teAttribute.optionSet.id);
                        promise = promise.then( getOptionSet( teAttribute.optionSet.id ) );
                    }
                    d.resolve();
                });

                return p;
            });
        }            
    });

    build.done(function() {
        def.resolve();

        promise = promise.done( function () {
            mainDef.resolve( data.programs );
        } );
    }).fail(function(){
        mainDef.resolve( null );
    });

    builder.resolve();

    return mainPromise;    
}

function getTrackedEntityForms( )
{
    dhis2.tc.store.getKeys( 'trackedEntityForms').done(function(res){        
        if(res.length > 0){
            return;
        }
        var def = $.Deferred();

        $.ajax({
            url: '../api/trackedEntityForms.json?paging=false&fields=id,program[id,name],dataEntryForm[name,htmlCode]',
            type: 'GET'
        }).done(function(response) {
            _.each( _.values( response.trackedEntityForms ), function ( trackedEntityForm ) { 
                
                if( trackedEntityForm && trackedEntityForm.id){
                    if(!trackedEntityForm.program || !trackedEntityForm.program.id){
                        trackedEntityForm.program = {id: 'NO_PROGRAM', name: 'NO_PROGRAM'};
                    }                    
                    trackedEntityForm.id = trackedEntityForm.program.id;
                    dhis2.tc.store.set( 'trackedEntityForms', trackedEntityForm );
                }
            });
            
            def.resolve();        
        }).fail(function(){
            def.resolve();
        });

        return def.promise();
    });    
}

function getOrgUnitLevels()
{
    dhis2.tc.store.getKeys( 'ouLevels').done(function(res){        
        if(res.length > 0){
            return;
        }
        var def = $.Deferred();

        $.ajax({
            url: '../api/organisationUnitLevels.json',
            type: 'GET',
            data:'filter=level:gt:1&fields=id,name,level&paging=false'
        }).done(function(response) {
            if(response.organisationUnitLevels){
                dhis2.tc.store.setAll( 'ouLevels', response.organisationUnitLevels );
            }            
            def.resolve();        
        }).fail(function(){
            def.resolve();
        });

        return def.promise();
    }); 
}