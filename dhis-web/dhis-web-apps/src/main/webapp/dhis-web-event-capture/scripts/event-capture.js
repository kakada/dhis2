
/* global dhis2, angular, i18n_ajax_login_failed, _ */

dhis2.util.namespace('dhis2.ec');

// whether current user has any organisation units
dhis2.ec.emptyOrganisationUnits = false;

var i18n_no_orgunits = 'No organisation unit attached to current user, no data entry possible';
var i18n_offline_notification = 'You are offline, data will be stored locally';
var i18n_online_notification = 'You are online';
var i18n_need_to_sync_notification = 'There is data stored locally, please upload to server';
var i18n_sync_now = 'Upload';
var i18n_sync_success = 'Upload to server was successful';
var i18n_sync_failed = 'Upload to server failed, please try again later';
var i18n_uploading_data_notification = 'Uploading locally stored data to the server';

var PROGRAMS_METADATA = 'EVENT_PROGRAMS';

var EVENT_VALUES = 'EVENT_VALUES';
var optionSetsInPromise = [];

dhis2.ec.store = null;
dhis2.ec.memoryOnly = $('html').hasClass('ie7') || $('html').hasClass('ie8');
var adapters = [];    
if( dhis2.ec.memoryOnly ) {
    adapters = [ dhis2.storage.InMemoryAdapter ];
} else {
    adapters = [ dhis2.storage.IndexedDBAdapter, dhis2.storage.DomLocalStorageAdapter, dhis2.storage.InMemoryAdapter ];
}

dhis2.ec.store = new dhis2.storage.Store({
    name: 'dhis2ec',
    adapters: [dhis2.storage.IndexedDBAdapter, dhis2.storage.DomSessionStorageAdapter, dhis2.storage.InMemoryAdapter],
    objectStores: ['programs', 'programStages', 'geoJsons', 'optionSets', 'events', 'programValidations', 'ouLevels']
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
        var OfflineECStorageService = angular.element('body').injector().get('OfflineECStorageService');

        OfflineECStorageService.hasLocalData().then(function(localData){
            if(localData){
                var message = i18n_need_to_sync_notification
                    + ' <button id="sync_button" type="button">' + i18n_sync_now + '</button>';

                setHeaderMessage(message);

                $('#sync_button').bind('click', uploadLocalData);
            }
            else{
                if (dhis2.ec.emptyOrganisationUnits) {
                    setHeaderMessage(i18n_no_orgunits);
                }
                else {
                    setHeaderDelayMessage(i18n_online_notification);
                }
            }
        });
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
    if (dhis2.ec.emptyOrganisationUnits) {
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

function downloadMetaData(){    
    
    console.log('Loading required meta-data');
    var def = $.Deferred();
    var promise = def.promise();
    
    promise = promise.then( dhis2.ec.store.open );    
    promise = promise.then( getUserRoles );
    promise = promise.then( getCalendarSetting );
    promise = promise.then( getOrgUnitLevels );    
    promise = promise.then( getMetaPrograms );     
    promise = promise.then( getPrograms );     
    promise = promise.then( getProgramStages );
    promise = promise.then( getOptionSets );
    promise = promise.then( getMetaProgramValidations );
    promise = promise.then( getProgramValidations );
    promise.done( function() {    
        //Enable ou selection after meta-data has downloaded
        $( "#orgUnitTree" ).removeClass( "disable-clicks" );
        
        console.log( 'Finished loading meta-data' ); 
        dhis2.availability.startAvailabilityCheck();
        console.log( 'Started availability check' );
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

function getOrgUnitLevels()
{
    dhis2.ec.store.getKeys( 'ouLevels').done(function(res){        
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
                dhis2.ec.store.setAll( 'ouLevels', response.organisationUnitLevels );
            }            
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
        data:'filter=type:eq:3&paging=false&fields=id,name,version,programStages[id,version,programStageSections[id],programStageDataElements[dataElement[id,optionSet[id,version]]]]'
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
        
        if(program.programStages && program.programStages[0].programStageDataElements){
            build = build.then(function() {
                var d = $.Deferred();
                var p = d.promise();
                dhis2.ec.store.get('programs', program.id).done(function(obj) {
                    if(!obj || obj.version !== program.version) {
                        promise = promise.then( getProgram( program.id ) );
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
            url: '../api/programs.json?filter=id:eq:' + id +'&fields=id,name,type,version,dataEntryMethod,dateOfEnrollmentDescription,dateOfIncidentDescription,displayIncidentDate,ignoreOverdueEvents,organisationUnits[id,name],programStages[id,name,version],userRoles[id,name]',
            type: 'GET'
        }).done( function( response ){
            
            _.each( _.values( response.programs ), function ( program ) { 
                
                var ou = {};
                _.each(_.values( program.organisationUnits), function(o){
                    ou[o.id] = o.name;
                });

                program.organisationUnits = ou;

                var ur = {};
                _.each(_.values( program.userRoles), function(u){
                    ur[u.id] = u.name;
                });

                program.userRoles = ur;

                dhis2.ec.store.set( 'programs', program );

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
            build = build.then(function() {
                var d = $.Deferred();
                var p = d.promise();
                dhis2.ec.store.get('programStages', program.programStages[0].id).done(function(obj) {
                    if(!obj || obj.version !== program.programStages[0].version) {
                        promise = promise.then( getProgramStage( program.programStages[0].id ) );
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
            url: '../api/programStages.json?filter=id:eq:' + id +'&fields=id,name,version,description,reportDateDescription,captureCoordinates,dataEntryForm,minDaysFromStart,repeatable,preGenerateUID,programStageSections[id,name,programStageDataElements[dataElement[id]]],programStageDataElements[displayInReports,sortOrder,allowProvidedElsewhere,allowFutureDate,compulsory,dataElement[id,name,type,optionSetValue,numberType,formName,optionSet[id]]]',
            type: 'GET'
        }).done( function( response ){            
            _.each( _.values( response.programStages ), function( programStage ) {                
                dhis2.ec.store.set( 'programStages', programStage );
            });
        });
    };
}

function getOptionSets( programs )
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
        
        if(program.programStages && program.programStages[0].programStageDataElements){
            _.each(_.values( program.programStages[0].programStageDataElements), function(prStDe){
                if( prStDe.dataElement && prStDe.dataElement.optionSet && prStDe.dataElement.optionSet.id ){
                    build = build.then(function() {
                        var d = $.Deferred();
                        var p = d.promise();
                        dhis2.ec.store.get('optionSets', prStDe.dataElement.optionSet.id).done(function(obj) {
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
            url: '../api/optionSets.json?filter=id:eq:' + id +'&fields=id,name,version,options[id,name,code]',
            type: 'GET'
        }).done( function( response ){            
            _.each( _.values( response.optionSets ), function( optionSet ) {                
                dhis2.ec.store.set( 'optionSets', optionSet );
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
        
        def.resolve( programValidations );
        
    }).fail(function(){
        def.resolve( null );
    });
    
    return def.promise();    
}

function getProgramValidations( programValidations )
{
    if( !programValidations ){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();

    _.each( _.values( programValidations ), function ( programValidation ) {
        build = build.then(function() {
            var d = $.Deferred();
            var p = d.promise();
            dhis2.ec.store.get('programValidations', programValidation.id).done(function(obj) {
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
            mainDef.resolve();
        } );
    }).fail(function(){
        mainDef.resolve();
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
                
                    dhis2.ec.store.set( 'programValidations', programValidation );
                }
            });
        });
    };
}

function uploadLocalData()
{
    var OfflineECStorageService = angular.element('body').injector().get('OfflineECStorageService');
    setHeaderWaitMessage(i18n_uploading_data_notification);
     
    OfflineECStorageService.uploadLocalData().then(function(){
        dhis2.ec.store.removeAll( 'events' );
        log( 'Successfully uploaded local events' );      
        setHeaderDelayMessage( i18n_sync_success );
        selection.responseReceived(); //notify angular
    });
}