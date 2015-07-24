/* global angular */

'use strict';

/* Controllers */
var eventCaptureControllers = angular.module('eventCaptureControllers', [])

//Controller for settings page
.controller('MainController',
        function($scope,
                $modal,
                $timeout,
                $translate,
                $anchorScroll,
                orderByFilter,
                SessionStorageService,
                Paginator,
                OptionSetService,
                ProgramValidationService,
                ProgramFactory,
                ProgramStageFactory,                
                DHIS2EventFactory,
                DHIS2EventService,
                ContextMenuSelectedItem,                
                DateUtils,
                CalendarService,
                GridColumnService,
                CustomFormService,
                ECStorageService,
                ErrorMessageService,
                CurrentSelection,
                ModalService,
                DialogService,
                AuthorityService) {
    //selected org unit
    $scope.selectedOrgUnit = '';
    $scope.treeLoaded = false;    
    
    $scope.calendarSetting = CalendarService.getSetting();
    
    //Paging
    $scope.pager = {pageSize: 50, page: 1, toolBarDisplay: 5};   
    
    //Editing
    $scope.eventRegistration = false;
    $scope.editGridColumns = false;
    $scope.editingEventInFull = false;
    $scope.editingEventInGrid = false;   
    $scope.updateSuccess = false;
    $scope.currentGridColumnId = '';  
    $scope.dhis2Events = [];
    $scope.currentEvent = {};
    $scope.currentEventOriginialValue = {}; 
    $scope.displayCustomForm = false;
    $scope.currentElement = {id: '', update: false};
    $scope.optionSets = [];
    $scope.proceedSelection = true;
    $scope.formUnsaved = false;    
    
    //notes
    $scope.note = {};
    $scope.today = DateUtils.getToday();    
    
    var userProfile = SessionStorageService.get('USER_PROFILE');
    var storedBy = userProfile && userProfile.username ? userProfile.username : '';
    
    $scope.noteExists = false;
        
    //watch for selection of org unit from tree
    $scope.$watch('selectedOrgUnit', function() {
        
        if(angular.isObject($scope.selectedOrgUnit)){
            SessionStorageService.set('SELECTED_OU', $scope.selectedOrgUnit);
            
            $scope.userAuthority = AuthorityService.getUserAuthorities(SessionStorageService.get('USER_ROLES'));
            
            //get ouLevels
            ECStorageService.currentStore.open().done(function(){
                ECStorageService.currentStore.getAll('ouLevels').done(function(response){
                    var ouLevels = angular.isObject(response) ? orderByFilter(response, '-level').reverse() : [];
                    CurrentSelection.setOuLevels(orderByFilter(ouLevels, '-level').reverse());
                });
            });
            
            if($scope.optionSets.length < 1){
                $scope.optionSets = [];
                OptionSetService.getAll().then(function(optionSets){
                    angular.forEach(optionSets, function(optionSet){  
                        $scope.optionSets[optionSet.id] = optionSet;
                    });                    
                    $scope.loadPrograms();
                });
            }
            else{
                $scope.loadPrograms();
            }
        }        
    });
    
    //load programs associated with the selected org unit.
    $scope.loadPrograms = function() {
        
        $scope.resetOu = false;
        $scope.selectedProgramStage = null;
        $scope.programValidations = [];
        $scope.dhis2Events = [];
        $scope.currentEvent = {};
        $scope.currentEventOriginialValue = {};

        $scope.eventRegistration = false;
        $scope.editGridColumns = false;
        $scope.editingEventInFull = false;
        $scope.editingEventInGrid = false;   
        $scope.updateSuccess = false;
        $scope.currentGridColumnId = '';           
        $scope.displayCustomForm = false;
        
        if (angular.isObject($scope.selectedOrgUnit)) {    
            
            ProgramFactory.getProgramsByOu($scope.selectedOrgUnit, $scope.selectedProgram).then(function(response){
                $scope.programs = response.programs;
                $scope.selectedProgram = response.selectedProgram;
                $scope.getProgramDetails();
            });
        }    
    };
    
    $scope.getProgramDetails = function(){        
        
        $scope.selectedProgramStage = null;
        $scope.eventFetched = false;
        
        //Filtering
        $scope.reverse = false;
        $scope.sortHeader = {};
        $scope.filterText = {};
        
        if( $scope.userAuthority && $scope.userAuthority.canAddOrUpdateEvent &&
                $scope.selectedProgram && 
                $scope.selectedProgram.programStages && 
                $scope.selectedProgram.programStages[0] && 
                $scope.selectedProgram.programStages[0].id){ 
                
            //because this is single event, take the first program stage
            ProgramStageFactory.get($scope.selectedProgram.programStages[0].id).then(function (programStage){

                $scope.selectedProgramStage = programStage;   

                angular.forEach($scope.selectedProgramStage.programStageSections, function(section){
                    section.open = true;
                });

                $scope.prStDes = [];  
                $scope.eventGridColumns = [];
                $scope.filterTypes = {};                               
                $scope.newDhis2Event = {};

                $scope.eventGridColumns.push({name: 'form_id', id: 'uid', type: 'string', compulsory: false, showFilter: false, show: false});
                $scope.filterTypes['uid'] = 'string';                

                $scope.eventGridColumns.push({name: $scope.selectedProgramStage.reportDateDescription ? $scope.selectedProgramStage.reportDateDescription : 'incident_date', id: 'event_date', type: 'date', compulsory: false, showFilter: false, show: true});
                $scope.filterTypes['event_date'] = 'date';
                $scope.filterText['event_date']= {};

                var errorMessages = {};
                errorMessages['eventDate'] = $translate('required');
                angular.forEach($scope.selectedProgramStage.programStageDataElements, function(prStDe){
                    $scope.prStDes[prStDe.dataElement.id] = prStDe;

                    errorMessages[prStDe.dataElement.id] = "";
                    if(prStDe.compulsory){
                        var msg = $translate('required');
                        if(prStDe.dataElement.type === 'int'){
                            msg = $translate(prStDe.dataElement.numberType)+ ' ' + $translate('required');
                        }
                        errorMessages[prStDe.dataElement.id] = msg;
                    }

                    $scope.newDhis2Event[prStDe.dataElement.id] = '';                    

                    //generate grid headers using program stage data elements
                    //create a template for new event
                    //for date type dataelements, filtering is based on start and end dates                    
                    $scope.eventGridColumns.push({name: prStDe.dataElement.formName ? prStDe.dataElement.formName : prStDe.dataElement.name, 
                                                  id: prStDe.dataElement.id, 
                                                  type: prStDe.dataElement.type, 
                                                  compulsory: prStDe.compulsory, 
                                                  showFilter: false, 
                                                  show: prStDe.displayInReports});

                    $scope.filterTypes[prStDe.dataElement.id] = prStDe.dataElement.type;

                    if(prStDe.dataElement.type === 'date' || prStDe.dataElement.type === 'int' ){
                        $scope.filterText[prStDe.dataElement.id]= {};
                    }
                });
                
                $scope.customForm = CustomFormService.getForProgramStage($scope.selectedProgramStage, $scope.prStDes);

                if($scope.selectedProgramStage.captureCoordinates){
                    $scope.newDhis2Event.coordinate = {};
                }
                $scope.newDhis2Event.eventDate = '';

                ErrorMessageService.setErrorMessages(errorMessages);

                ProgramValidationService.getByProgram($scope.selectedProgram.id).then(function(pvs){
                    $scope.programValidations = pvs;
                    $scope.loadEvents();
                });
            });
        }
    };
        
    //get events for the selected program (and org unit)
    $scope.loadEvents = function(){   
        
        $scope.noteExists = false;            
        $scope.dhis2Events = [];
        $scope.eventLength = 0;

        $scope.eventFetched = false;
               
        if( $scope.selectedProgram && 
                $scope.selectedProgram.programStages && 
                $scope.selectedProgram.programStages[0] && 
                $scope.selectedProgram.programStages[0].id){
            
            //Load events for the selected program stage and orgunit
            DHIS2EventFactory.getByStage($scope.selectedOrgUnit.id, $scope.selectedProgramStage.id, $scope.pager, true ).then(function(data){

                if(data.events){
                    $scope.eventLength = data.events.length;
                }                

                $scope.dhis2Events = data.events; 

                if( data.pager ){
                    $scope.pager = data.pager;
                    $scope.pager.toolBarDisplay = 5;

                    Paginator.setPage($scope.pager.page);
                    Paginator.setPageCount($scope.pager.pageCount);
                    Paginator.setPageSize($scope.pager.pageSize);
                    Paginator.setItemCount($scope.pager.total);                    
                }

                //process event list for easier tabular sorting
                if( angular.isObject( $scope.dhis2Events ) ) {

                    for(var i=0; i < $scope.dhis2Events.length; i++){  

                        if($scope.dhis2Events[i].notes && !$scope.noteExists){
                            $scope.noteExists = true;
                        }

                        //check if event is empty
                        if(!angular.isUndefined($scope.dhis2Events[i].dataValues)){                            

                            angular.forEach($scope.dhis2Events[i].dataValues, function(dataValue){

                                //converting event.datavalues[i].datavalue.dataelement = value to
                                //event[dataElement] = value for easier grid display.                                
                                if($scope.prStDes[dataValue.dataElement]){                                    

                                    var val = dataValue.value;
                                    if(angular.isObject($scope.prStDes[dataValue.dataElement].dataElement)){                               

                                        //converting int string value to number for proper sorting.
                                        if($scope.prStDes[dataValue.dataElement].dataElement.type === 'int'){
                                            if( dhis2.validation.isNumber(val)  ){
                                                val = new Number(val);
                                            }                                
                                        }
                                        if($scope.prStDes[dataValue.dataElement].dataElement.optionSetValue){                                            
                                            if($scope.prStDes[dataValue.dataElement].dataElement.optionSet &&
                                                    $scope.prStDes[dataValue.dataElement].dataElement.optionSet.id &&
                                                    $scope.optionSets[$scope.prStDes[dataValue.dataElement].dataElement.optionSet.id] &&
                                                    $scope.optionSets[$scope.prStDes[dataValue.dataElement].dataElement.optionSet.id].options ){
                                                val = OptionSetService.getName($scope.optionSets[$scope.prStDes[dataValue.dataElement].dataElement.optionSet.id].options, val);
                                            }                                                
                                        }
                                        if($scope.prStDes[dataValue.dataElement].dataElement.type === 'date'){
                                            val = DateUtils.formatFromApiToUser(val);                                               
                                        }
                                        if( $scope.prStDes[dataValue.dataElement].dataElement.type === 'trueOnly'){
                                            if(val === 'true'){
                                                val = true;
                                            }
                                            else{
                                                val = false;
                                            }
                                        }                                    
                                    }                                    
                                    $scope.dhis2Events[i][dataValue.dataElement] = val; 
                                }

                            });

                            $scope.dhis2Events[i]['uid'] = $scope.dhis2Events[i].event;                                
                            $scope.dhis2Events[i].eventDate = DateUtils.formatFromApiToUser($scope.dhis2Events[i].eventDate);                                
                            $scope.dhis2Events[i]['event_date'] = $scope.dhis2Events[i].eventDate;

                            delete $scope.dhis2Events[i].dataValues;
                        }
                    }

                    if($scope.noteExists && !GridColumnService.columnExists($scope.eventGridColumns, 'comment')){
                        $scope.eventGridColumns.push({name: 'comment', id: 'comment', type: 'string', compulsory: false, showFilter: false, show: true});
                    }
                    
                    if(!$scope.sortHeader.id){
                        $scope.sortEventGrid({name: $scope.selectedProgramStage.reportDateDescription ? $scope.selectedProgramStage.reportDateDescription : 'incident_date', id: 'event_date', type: 'date', compulsory: false, showFilter: false, show: true});
                    }
                }                
                $scope.eventFetched = true;
            });
        }        
    };    
    
    $scope.jumpToPage = function(){
        
        if($scope.pager && $scope.pager.page && $scope.pager.pageCount && $scope.pager.page > $scope.pager.pageCount){
            $scope.pager.page = $scope.pager.pageCount;
        }
        $scope.loadEvents();
    };
    
    $scope.resetPageSize = function(){
        $scope.pager.page = 1;        
        $scope.loadEvents();
    };
    
    $scope.getPage = function(page){    
        $scope.pager.page = page;
        $scope.loadEvents();
    };
    
    $scope.sortEventGrid = function(gridHeader){        
        if ($scope.sortHeader && $scope.sortHeader.id === gridHeader.id){
            $scope.reverse = !$scope.reverse;
            return;
        }        
        $scope.sortHeader = gridHeader;
        if($scope.sortHeader.type === 'date'){
            $scope.reverse = true;
        }
        else{
            $scope.reverse = false;    
        }        
    };
    
    $scope.d2Sort = function(dhis2Event){        
        if($scope.sortHeader && $scope.sortHeader.type === 'date'){            
            var d = dhis2Event[$scope.sortHeader.id];         
            return DateUtils.getDate(d);
        }
        return dhis2Event[$scope.sortHeader.id];
    };
    
    $scope.showHideColumns = function(){
        
        $scope.hiddenGridColumns = 0;
        
        angular.forEach($scope.eventGridColumns, function(eventGridColumn){
            if(!eventGridColumn.show){
                $scope.hiddenGridColumns++;
            }
        });
        
        var modalInstance = $modal.open({
            templateUrl: 'views/column-modal.html',
            controller: 'ColumnDisplayController',
            resolve: {
                gridColumns: function () {
                    return $scope.eventGridColumns;
                },
                hiddenGridColumns: function(){
                    return $scope.hiddenGridColumns;
                }
            }
        });

        modalInstance.result.then(function (gridColumns) {
            $scope.eventGridColumns = gridColumns;
        }, function () {
        });
    };
    
    $scope.searchInGrid = function(gridColumn){
        
        $scope.currentFilter = gridColumn;
       
        for(var i=0; i<$scope.eventGridColumns.length; i++){
            
            //toggle the selected grid column's filter
            if($scope.eventGridColumns[i].id === gridColumn.id){
                $scope.eventGridColumns[i].showFilter = !$scope.eventGridColumns[i].showFilter;
            }            
            else{
                $scope.eventGridColumns[i].showFilter = false;
            }
        }
    };    
    
    $scope.removeStartFilterText = function(gridColumnId){
        $scope.filterText[gridColumnId].start = undefined;
    };
    
    $scope.removeEndFilterText = function(gridColumnId){
        $scope.filterText[gridColumnId].end = undefined;
    };
    
    $scope.cancel = function(){
        
        if($scope.formHasUnsavedData()){
            var modalOptions = {
                closeButtonText: 'cancel',
                actionButtonText: 'proceed',
                headerText: 'warning',
                bodyText: 'unsaved_data_exists_proceed'
            };

            ModalService.showModal({}, modalOptions).then(function(result){            
                $scope.showEventList();
            });
        }
        else{
            $scope.showEventList();
        }
    };
    
    $scope.showEventList = function(){
        $scope.eventRegistration = false;
        $scope.editingEventInFull = false;
        $scope.editingEventInGrid = false;
        $scope.currentElement.updated = false;        
        $scope.currentEvent = {};
        $scope.currentEventOriginialValue = angular.copy($scope.currentEvent);
    };
    
    $scope.showEventRegistration = function(){        
        $scope.displayCustomForm = $scope.customForm ? true:false;        
        $scope.currentEvent = {};
        $scope.eventRegistration = !$scope.eventRegistration;          
        $scope.currentEvent = angular.copy($scope.newDhis2Event);        
        $scope.outerForm.submitted = false;
        $scope.note = {};
        
        if($scope.selectedProgramStage.preGenerateUID){
            $scope.eventUID = dhis2.util.uid();
            $scope.currentEvent['uid'] = $scope.eventUID;
        }        
        $scope.currentEventOriginialValue = angular.copy($scope.currentEvent);        
    };    
    
    $scope.showEditEventInGrid = function(){
        $scope.currentEvent = ContextMenuSelectedItem.getSelectedItem();
        $scope.currentEventOriginialValue = angular.copy($scope.currentEvent);
        $scope.editingEventInGrid = !$scope.editingEventInGrid;
        
        $scope.outerForm.$valid = true;
    };
    
    $scope.showEditEventInFull = function(){       
        $scope.note = {};
        $scope.displayCustomForm = $scope.customForm ? true:false;

        $scope.currentEvent = ContextMenuSelectedItem.getSelectedItem();
        $scope.editingEventInFull = !$scope.editingEventInFull;   
        $scope.eventRegistration = false;
        
        angular.forEach($scope.selectedProgramStage.programStageDataElements, function(prStDe){
            if(!$scope.currentEvent.hasOwnProperty(prStDe.dataElement.id)){
                $scope.currentEvent[prStDe.dataElement.id] = '';
            }
        }); 
        $scope.currentEventOriginialValue = angular.copy($scope.currentEvent);
        
    };
    
    $scope.switchDataEntryForm = function(){
        $scope.displayCustomForm = !$scope.displayCustomForm;
    };
    
    $scope.addEvent = function(addingAnotherEvent){                
        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid ){
            angular.forEach($scope.selectedProgramStage.programStageSections, function(section){
                section.open = true;
            });
            return false;
        }
        
        //the form is valid, get the values
        //but there could be a case where all dataelements are non-mandatory and
        //the event form comes empty, in this case enforce at least one value
        var valueExists = false;
        var dataValues = [];        
        for(var dataElement in $scope.prStDes){            
            var val = $scope.currentEvent[dataElement];
            if(val){
                valueExists = true;            
                if($scope.prStDes[dataElement].dataElement.optionSetValue){
                    if($scope.prStDes[dataElement].dataElement.optionSet){                        
                        val = OptionSetService.getCode($scope.optionSets[$scope.prStDes[dataElement].dataElement.optionSet.id].options,val);
                    }
                }

                if($scope.prStDes[dataElement].dataElement.type === 'date'){
                    val = DateUtils.formatFromUserToApi(val);
                }
            }
            dataValues.push({dataElement: dataElement, value: val});
        }
        
        if(!valueExists){
            var dialogOptions = {
                headerText: 'empty_form',
                bodyText: 'please_fill_at_least_one_dataelement'
            };

            DialogService.showDialog({}, dialogOptions);
            return false;
        }        
        
        if(addingAnotherEvent){
            $scope.disableSaveAndAddNew = true;
        }
        
        var newEvent = angular.copy($scope.currentEvent);        
        
        //prepare the event to be created
        var dhis2Event = {
                program: $scope.selectedProgram.id,
                programStage: $scope.selectedProgramStage.id,
                orgUnit: $scope.selectedOrgUnit.id,
                status: 'ACTIVE',            
                eventDate: DateUtils.formatFromUserToApi(newEvent.eventDate),
                dataValues: dataValues
        }; 
        
        if($scope.selectedProgramStage.preGenerateUID && !angular.isUndefined(newEvent['uid'])){
            dhis2Event.event = newEvent['uid'];
        }
        
        if(!angular.isUndefined($scope.note.value) && $scope.note.value != ''){
            dhis2Event.notes = [{value: $scope.note.value}];
            
            newEvent.notes = [{value: $scope.note.value, storedDate: $scope.today, storedBy: storedBy}];
            
            $scope.noteExists = true;
        }
        
        if($scope.selectedProgramStage.captureCoordinates){
            dhis2Event.coordinate = {latitude: $scope.currentEvent.coordinate.latitude ? $scope.currentEvent.coordinate.latitude : '',
                                     longitude: $scope.currentEvent.coordinate.longitude ? $scope.currentEvent.coordinate.longitude : ''};             
        }
        
        //send the new event to server
        DHIS2EventFactory.create(dhis2Event).then(function(data) {
            if (data.importSummaries[0].status === 'ERROR') {
                var dialogOptions = {
                    headerText: 'event_registration_error',
                    bodyText: data.importSummaries[0].description
                };

                DialogService.showDialog({}, dialogOptions);
            }
            else {
                
                //add the new event to the grid                
                newEvent.event = data.importSummaries[0].reference;                
                if( !$scope.dhis2Events ){
                    $scope.dhis2Events = [];                   
                }
                newEvent['uid'] = newEvent.event;
                newEvent['event_date'] = newEvent.eventDate; 
                $scope.dhis2Events.splice(0,0,newEvent);
                
                $scope.eventLength++;
                
                $scope.eventRegistration = false;
                $scope.editingEventInFull = false;
                $scope.editingEventInGrid = false;  
                    
                //reset form              
                $scope.currentEvent = {};
                $scope.currentEvent = angular.copy($scope.newDhis2Event); 
                $scope.currentEventOriginialValue = angular.copy($scope.currentEvent);
                               
                $scope.note = {};
                $scope.outerForm.submitted = false;
                $scope.outerForm.$setPristine();
                $scope.disableSaveAndAddNew = false;
                
                //this is to hide typeAheadPopUps - shouldn't be an issue in 
                //the first place.                
                $timeout(function() {
                    angular.element('#hideTypeAheadPopUp').trigger('click');
                }, 10);
                
                //decide whether to stay in the current screen or not.
                if(addingAnotherEvent){
                    $scope.showEventRegistration();
                    $anchorScroll();
                }
            }
        });
    }; 
    
    $scope.updateEvent = function(){
        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid ){
            angular.forEach($scope.selectedProgramStage.programStageSections, function(section){
                section.open = true;
            });
            return false;
        }
        
        //the form is valid, get the values
        var dataValues = [];        
        for(var dataElement in $scope.prStDes){
            var val = $scope.currentEvent[dataElement];
            
            if(val && $scope.prStDes[dataElement].dataElement.optionSetValue){
                if($scope.prStDes[dataElement].dataElement.optionSet){                    
                    val = OptionSetService.getCode($scope.optionSets[$scope.prStDes[dataElement].dataElement.optionSet.id].options,val); 
                }    
            }
            if(val && $scope.prStDes[dataElement].dataElement.type === 'date'){
                val = DateUtils.formatFromUserToApi(val);    
            }
            dataValues.push({dataElement: dataElement, value: val});
        }
        
        var updatedEvent = {
                            program: $scope.currentEvent.program,
                            programStage: $scope.currentEvent.programStage,
                            orgUnit: $scope.currentEvent.orgUnit,
                            status: 'ACTIVE',                                        
                            eventDate: DateUtils.formatFromUserToApi($scope.currentEvent.eventDate),
                            event: $scope.currentEvent.event, 
                            dataValues: dataValues
                        };

        if($scope.selectedProgramStage.captureCoordinates){
            updatedEvent.coordinate = {latitude: $scope.currentEvent.coordinate.latitude ? $scope.currentEvent.coordinate.latitude : '',
                                     longitude: $scope.currentEvent.coordinate.longitude ? $scope.currentEvent.coordinate.longitude : ''};             
        }
        
        if(!angular.isUndefined($scope.note.value) && $scope.note.value != ''){
           
            updatedEvent.notes = [{value: $scope.note.value}];
            
            if($scope.currentEvent.notes){
                $scope.currentEvent.notes.splice(0,0,{value: $scope.note.value, storedDate: $scope.today, storedBy: storedBy});
            }
            else{
                $scope.currentEvent.notes = [{value: $scope.note.value, storedDate: $scope.today, storedBy: storedBy}];
            }   
            
            $scope.noteExists = true;
        }

        DHIS2EventFactory.update(updatedEvent).then(function(data){            
            
            //reflect the change in the gird            
            $scope.resetEventValue($scope.currentEvent);            
            $scope.outerForm.submitted = false;            
            $scope.editingEventInFull = false;
            $scope.currentEvent = {};
            $scope.currentEventOriginialValue = angular.copy($scope.currentEvent); 
        });       
    };
       
    $scope.updateEventDataValue = function(currentEvent, dataElement){

        $scope.updateSuccess = false;
        
        //get current element
        $scope.currentElement = {id: dataElement};
        
        //get new and old values
        var newValue = $scope.currentEvent[dataElement];        
        var oldValue = $scope.currentEventOriginialValue[dataElement];
        
        //check for form validity
        if( $scope.isFormInvalid() ){
            $scope.currentElement.updated = false;            
            //reset value back to original
            $scope.currentEvent[dataElement] = oldValue;            
            $scope.resetEventValue($scope.currentEvent);
            return;            
        }
        
        if( $scope.prStDes[dataElement].compulsory && !newValue ) {
            $scope.currentElement.updated = false;                        
            //reset value back to original
            $scope.currentEvent[dataElement] = oldValue;            
            $scope.resetEventValue($scope.currentEvent);
            return;
        }        
                
        if( newValue != oldValue ){
            
            if($scope.prStDes[dataElement].dataElement.optionSetValue){
                if($scope.prStDes[dataElement].dataElement.optionSet){
                    newValue = OptionSetService.getCode($scope.optionSets[$scope.prStDes[dataElement].dataElement.optionSet.id].options, newValue);
                }
            }            
            if($scope.prStDes[dataElement].dataElement.type === 'date'){
                newValue = DateUtils.formatFromUserToApi(newValue);
            }
            
            var updatedSingleValueEvent = {event: $scope.currentEvent.event, dataValues: [{value: newValue, dataElement: dataElement}]};
            var updatedFullValueEvent = DHIS2EventService.reconstructEvent($scope.currentEvent, $scope.selectedProgramStage.programStageDataElements);

            DHIS2EventFactory.updateForSingleValue(updatedSingleValueEvent, updatedFullValueEvent).then(function(data){
                
                //reflect the new value in the grid
                $scope.resetEventValue($scope.currentEvent);
                
                //update original value
                $scope.currentEventOriginialValue = angular.copy($scope.currentEvent);      
                
                $scope.currentElement.updated = true;
                $scope.updateSuccess = true;
            });
        }
    };
    
    $scope.resetEventValue = function(currentEvent){
        var continueLoop = true;
        for(var i=0; i< $scope.dhis2Events.length && continueLoop; i++){
            if($scope.dhis2Events[i].event === currentEvent.event ){
                $scope.dhis2Events[i] = currentEvent;
                continueLoop = false;
            }
        }
    };
    
    $scope.removeEvent = function(){
        
        var dhis2Event = ContextMenuSelectedItem.getSelectedItem();
        
        var modalOptions = {
            closeButtonText: 'cancel',
            actionButtonText: 'remove',
            headerText: 'remove',
            bodyText: 'are_you_sure_to_remove'
        };

        ModalService.showModal({}, modalOptions).then(function(result){
            
            DHIS2EventFactory.delete(dhis2Event).then(function(data){
                
                var continueLoop = true, index = -1;
                for(var i=0; i< $scope.dhis2Events.length && continueLoop; i++){
                    if($scope.dhis2Events[i].event === dhis2Event.event ){
                        $scope.dhis2Events[i] = dhis2Event;
                        continueLoop = false;
                        index = i;
                    }
                }
                $scope.dhis2Events.splice(index,1);                
                $scope.currentEvent = {};             
            });
        });        
    };
        
    $scope.showNotes = function(dhis2Event){
        
        var modalInstance = $modal.open({
            templateUrl: 'views/notes.html',
            controller: 'NotesController',
            resolve: {
                dhis2Event: function () {
                    return dhis2Event;
                }
            }
        });

        modalInstance.result.then(function (){
        });
    };
    
    $scope.getHelpContent = function(){
    };
    
    $scope.showMap = function(event){
        var modalInstance = $modal.open({
            templateUrl: '../dhis-web-commons/coordinatecapture/map.html',
            controller: 'MapController',
            windowClass: 'modal-full-window',
            resolve: {
                location: function () {
                    return {lat: event.coordinate.latitude, lng: event.coordinate.longitude};
                }
            }
        });

        modalInstance.result.then(function (location) {
            if(angular.isObject(location)){
                event.coordinate.latitude = location.lat;
                event.coordinate.longitude = location.lng;
            }
        }, function () {
        });
    };
    
    $scope.formIsChanged = function(){        
        var isChanged = false;
        for(var i=0; i<$scope.selectedProgramStage.programStageDataElements.length && !isChanged; i++){
            var deId = $scope.selectedProgramStage.programStageDataElements[i].dataElement.id;
            if($scope.currentEvent[deId] && $scope.currentEventOriginialValue[deId] !== $scope.currentEvent[deId]){
                isChanged = true;
            }
        }        
        if(!isChanged){
            if($scope.currentEvent.eventDate !== $scope.currentEventOriginialValue.eventDate){
                isChanged = true;
            }
        }
        
        return isChanged;
    };
    
    $scope.isFormInvalid = function(){
        if($scope.outerForm.submitted){
            return $scope.outerForm.$invalid;
        }
        
        if(!$scope.outerForm.$dirty){
            return false;
        }
        
        var formIsInvalid = false;
        for(var k in $scope.outerForm.$error){            
            if(angular.isObject($scope.outerForm.$error[k])){
                
                for(var i=0; i<$scope.outerForm.$error[k].length && !formIsInvalid; i++){
                    if($scope.outerForm.$error[k][i].$dirty && $scope.outerForm.$error[k][i].$invalid){
                        formIsInvalid = true;
                    }
                }
            }
            
            if(formIsInvalid){
                break;
            }
        }
        
        return formIsInvalid;
    };
    
    $scope.getErrorMessage = function(id){
        return ErrorMessageService.get(id);
    };
    
    $scope.formHasUnsavedData = function(){        
        if(angular.isObject($scope.currentEvent) && angular.isObject($scope.currentEventOriginialValue)){
            return !angular.equals($scope.currentEvent, $scope.currentEventOriginialValue);
        }
        return false;
    };
    
    //watch for event editing
    $scope.$watchCollection('[editingEventInFull, eventRegistration]', function() {        
        if($scope.editingEventInFull || $scope.eventRegistration){
            //Disable ou selection while in editing mode
            $( "#orgUnitTree" ).addClass( "disable-clicks" );
        }
        else{
            //enable ou selection if not in editing mode
            $( "#orgUnitTree" ).removeClass( "disable-clicks" );
        }
    });
});