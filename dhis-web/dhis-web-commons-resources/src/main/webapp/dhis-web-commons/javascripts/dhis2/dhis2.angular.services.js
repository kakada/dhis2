/* Pagination service */
var d2Services = angular.module('d2Services', ['ngResource'])

/* Factory for loading translation strings */
.factory('i18nLoader', function ($q, $http, SessionStorageService) {
 
    var getTranslationStrings = function(locale){
        var defaultUrl = 'i18n/i18n_app.properties';
        var url = '';
        if(locale === 'en' || !locale){
            url = defaultUrl;
        }
        else{
            url = 'i18n/i18n_app_' + locale + '.properties';
        }

        var tx = {locale: locale};

        var promise = $http.get(url).then(function(response){
            tx= {locale: locale, keys: dhis2.util.parseJavaProperties(response.data)};
            return tx;
        }, function(){
        	
        	setHeaderDelayMessage($translate('No translation file is found for the selected locale. Using default translation (English).'));
        	            
            var p = $http.get(defaultUrl).then(function(response){
                tx= {locale: locale, keys: dhis2.util.parseJavaProperties(response.data)};
                return tx;
            });
            return p;
        });
        return promise;
    };

    var getLocale = function(){
        var locale = 'en';

        var promise = $http.get('../api/me/profile.json').then(function(response){
            SessionStorageService.set( 'USER_PROFILE', response.data );
            if(response.data && response.data.settings && response.data.settings.keyUiLocale){
                locale = response.data.settings.keyUiLocale;
            }
            return locale;
        }, function(){
            return locale;
        });

        return promise;
    };
    return function () {
        var deferred = $q.defer(), translations;    
        var userProfile = SessionStorageService.get( 'USER_PROFILE' );
        if(userProfile && userProfile.settings && userProfile.settings.keyUiLocale){                
            getTranslationStrings(userProfile.settings.keyUiLocale).then(function(response){
                translations = response.keys;
                deferred.resolve(translations);
            });
            return deferred.promise;
        }
        else{
            getLocale().then(function(locale){
                getTranslationStrings(locale).then(function(response){
                    translations = response.keys;
                    deferred.resolve(translations);
                });
            });
            return deferred.promise;
        }
    };
})

.service('AuthorityService', function() {
    var getAuthorities = function(roles){
        var authority = {};
        if( roles && roles.userCredentials && roles.userCredentials.userRoles ){
            angular.forEach(roles.userCredentials.userRoles, function(role){
                angular.forEach(role.authorities, function(auth){
                    authority[auth] = true;
                });
            });
        }
        return authority;
    };
    
    return {
        getUserAuthorities: function(roles){
            var auth = getAuthorities(roles);
            var authority = {};
            authority.canDeleteEvent = auth['F_TRACKED_ENTITY_DATAVALUE_DELETE'] || auth['ALL'] ? true:false;
            authority.canAddOrUpdateEvent = auth['F_TRACKED_ENTITY_DATAVALUE_ADD'] || auth['ALL'] ? true:false;
            authority.canSearchTei = auth['F_TRACKED_ENTITY_INSTANCE_SEARCH'] || auth['ALL'] ? true:false;
            authority.canDeleteTei = auth['F_TRACKED_ENTITY_INSTANCE_DELETE'] || auth['ALL'] ? true:false;
            authority.canRegisterTei = auth['F_TRACKED_ENTITY_INSTANCE_ADD'] || auth['ALL'] ? true:false;
            authority.canEnrollTei = auth['F_PROGRAM_ENROLLMENT'] || auth['ALL'] ? true:false;
            authority.canUnEnrollTei = auth['F_PROGRAM_UNENROLLMENT'] || auth['ALL'] ? true:false;
            return authority;
        }
    };
})

/* Factory for loading external data */
.factory('ExternalDataFactory', function($http) {

    return {        
        get: function(fileName) {
            var promise = $http.get( fileName ).then(function(response){
                return response.data;
            });            
            return promise;
        }
    };
})

/* service for wrapping sessionStorage '*/
.service('SessionStorageService', function($window){
    return {        
        get: function(key){
            return JSON.parse( $window.sessionStorage.getItem( key ) );
        },
        set: function(key, obj) {            
            $window.sessionStorage.setItem( key, JSON.stringify( obj ) );
        },
        clearAll: function(){
        	for(var key in $window.sessionStorage){
	            $window.sessionStorage.removeItem(key);
	        }
        }
    };
})

/* service for getting calendar setting */
.service('CalendarService', function(storage, $rootScope){    

    return {
        getSetting: function() {
            
            var dhis2CalendarFormat = {keyDateFormat: 'yyyy-MM-dd', keyCalendar: 'gregorian', momentFormat: 'YYYY-MM-DD'};                
            var storedFormat = storage.get('CALENDAR_SETTING');
            if(angular.isObject(storedFormat) && storedFormat.keyDateFormat && storedFormat.keyCalendar){
                if(storedFormat.keyCalendar === 'iso8601'){
                    storedFormat.keyCalendar = 'gregorian';
                }

                if(storedFormat.keyDateFormat === 'dd-MM-yyyy'){
                    dhis2CalendarFormat.momentFormat = 'DD-MM-YYYY';
                }
                
                dhis2CalendarFormat.keyCalendar = storedFormat.keyCalendar;
                dhis2CalendarFormat.keyDateFormat = storedFormat.keyDateFormat;
            }
            $rootScope.dhis2CalendarFormat = dhis2CalendarFormat;
            return dhis2CalendarFormat;
        }
    };            
})

/* service for dealing with dates */
.service('DateUtils', function($filter, CalendarService){
    
    return {        
        getDate: function(dateValue){
            if(!dateValue){
                return;
            }            
            var calendarSetting = CalendarService.getSetting();
            dateValue = moment(dateValue, calendarSetting.momentFormat)._d;
            return Date.parse(dateValue);
        },
        format: function(dateValue) {            
            if(!dateValue){
                return;
            }

            var calendarSetting = CalendarService.getSetting();            
            dateValue = moment(dateValue, calendarSetting.momentFormat)._d;
            dateValue = $filter('date')(dateValue, calendarSetting.keyDateFormat);            
            return dateValue;
        },
        formatToHrsMins: function(dateValue) {
            var calendarSetting = CalendarService.getSetting();
            var dateFormat = 'YYYY-MM-DD @ hh:mm A';
            if(calendarSetting.keyDateFormat === 'dd-MM-yyyy'){
                dateFormat = 'DD-MM-YYYY @ hh:mm A';
            }            
            return moment(dateValue).format(dateFormat);
        },
        getToday: function(){  
            var calendarSetting = CalendarService.getSetting();
            var tdy = $.calendars.instance(calendarSetting.keyCalendar).newDate();            
            var today = moment(tdy._year + '-' + tdy._month + '-' + tdy._day, 'YYYY-MM-DD')._d;            
            today = Date.parse(today);     
            today = $filter('date')(today,  calendarSetting.keyDateFormat);
            return today;
        },
        formatFromUserToApi: function(dateValue){            
            if(!dateValue){
                return;
            }
            var calendarSetting = CalendarService.getSetting();
            dateValue = moment(dateValue, calendarSetting.momentFormat)._d;
            dateValue = Date.parse(dateValue);     
            dateValue = $filter('date')(dateValue, 'yyyy-MM-dd'); 
            return dateValue;            
        },
        formatFromApiToUser: function(dateValue){            
            if(!dateValue){
                return;
            }            
            var calendarSetting = CalendarService.getSetting();
            dateValue = moment(dateValue, 'YYYY-MM-DD')._d;
            return $filter('date')(dateValue, calendarSetting.keyDateFormat); 
        }
    };
})

/* service for dealing with custom form */
.service('CustomFormService', function(){
    
    return {
        getForProgramStage: function(programStage, programStageDataElements){
            
            var htmlCode = programStage.dataEntryForm ? programStage.dataEntryForm.htmlCode : null;  
            
            if(htmlCode){                

                var inputRegex = /<input.*?\/>/g,
                    match,
                    inputFields = [],
                    hasEventDate = false;                

                while (match = inputRegex.exec(htmlCode)) {                
                    inputFields.push(match[0]);
                }
                
                for(var i=0; i<inputFields.length; i++){                    
                    var inputField = inputFields[i];                    
                    var inputElement = $.parseHTML( inputField );
                    var attributes = {};
                                       
                    $(inputElement[0].attributes).each(function() {
                        attributes[this.nodeName] = this.value;                       
                    });
                    
                    var fieldId = '', errorMessageId = '', newInputField;   
                    if(attributes.hasOwnProperty('id')){
                        
                        if(attributes['id'] === 'executionDate'){
                            fieldId = 'eventDate';
                            errorMessageId = '"' + 'eventDate' + '"';
                            hasEventDate = true;
                            
                            //name needs to be unique so that it can be used for validation in angularjs
                            if(attributes.hasOwnProperty('name')){
                                attributes['name'] = fieldId;
                            }
                            
                            newInputField = '<input type="text" ' +
                                                this.getAttributesAsString(attributes) +
                                                ' d2-validation ' +
                                                ' ng-model="currentEvent.' + fieldId + '"' +
                                                ' input-field-id="' + fieldId + '"' +
                                                ' d2-date ' +
                                                ' max-date="' + 0 + '"' + 
                                                ' placeholder="{{dhis2CalendarFormat.keyDateFormat}}" ' +
                                                ' ng-class="getInputNotifcationClass(prStDes.' + fieldId + '.dataElement.id,true)"' +
                                                ' blur-or-change="saveDatavalue(prStDes.'+ fieldId + ')"' + 
                                                ' ng-required="{{true}}">';
                        }
                        else{
                            fieldId = attributes['id'].substring(4, attributes['id'].length-1).split("-")[1]; 
                            errorMessageId = 'prStDes.' + fieldId + '.dataElement.id';
                            
                            //name needs to be unique so that it can be used for validation in angularjs
                            if(attributes.hasOwnProperty('name')){
                                attributes['name'] = fieldId;
                            }
                            
                            var prStDe = programStageDataElements[fieldId];
                            
                            var commonInputFieldProperty = this.getAttributesAsString(attributes) +
	                                                ' d2-validation ' +
	                                                ' ng-model="currentEvent.' + fieldId + '" ' +
	                                                ' input-field-id="' + fieldId + '"' +
	                                                ' ng-class="getInputNotifcationClass(prStDes.' + fieldId + '.dataElement.id,true)"' +
	                                                ' ng-disabled="selectedEnrollment.status===\'CANCELLED\' || selectedEnrollment.status===\'COMPLETED\' || currentEvent[uid]==\'uid\' || currentEvent.editingNotAllowed"' +
	                                                ' ng-required="{{prStDes.' + fieldId + '.compulsory}}" ';
                            
                            if( prStDe && prStDe.dataElement && prStDe.dataElement.type ){								
								//check if dataelement has optionset								
								if( prStDe.dataElement.optionSetValue){
									var optionSetId = prStDe.dataElement.optionSet.id;
	                                    newInputField = '<input type="text" ' +	                                                
	                                                ' typeahead="option.name as option.name for option in optionSets.'+optionSetId+'.options | filter:$viewValue | limitTo:20"' +
	                                                ' typeahead-editable="false" ' +
	                                                ' d2-typeahead-validation ' +
	                                                ' class="typeahead" ' +
	                                                ' placeholder="&#xf0d7;&nbsp;&nbsp;" ' +	                                                                                            
	                                                ' ng-blur="saveDatavalue(prStDes.'+ fieldId + ')"' +
	                                                ' typeahead-open-on-focus ' +
	                                                commonInputFieldProperty + ' >';
								}
								else{
		                            //check data element type and generate corresponding angular input field
		                            if(prStDe.dataElement.type === "int"){
		                                newInputField = '<input type="text" ' +
		                                                ' d2-number-validation ' +
		                                                ' number-type="' + prStDe.dataElement.numberType + '" ' +		                                                
		                                                ' ng-blur="saveDatavalue(prStDes.'+ fieldId + ')"' + 
		                                                commonInputFieldProperty + ' >';
		                            }		                            
		                            else if(prStDe.dataElement.type === "bool"){
		                                newInputField = '<select ' +		                                                
		                                                ' ng-change="saveDatavalue(prStDes.'+ fieldId + ')" ' +
		                                                commonInputFieldProperty + '>' + 
		                                                '<option value="">{{\'please_select\'| translate}}</option>' +
		                                                '<option value="false">{{\'no\'| translate}}</option>' + 
		                                                '<option value="true">{{\'yes\'| translate}}</option>' +
		                                                '</select> ';                                     
		                            }
		                            else if(prStDe.dataElement.type === "date"){
		                                var maxDate = prStDe.allowFutureDate ? '' : 0;
		                                newInputField = '<input type="text" ' +                                                
		                                                ' placeholder="{{dhis2CalendarFormat.keyDateFormat}}" ' +
		                                                ' d2-date ' +
		                                                ' max-date="' + maxDate + '"' +		                                                
		                                                ' blur-or-change="saveDatavalue(prStDes.'+ fieldId + ')"' + 
		                                                commonInputFieldProperty + ' >';
		                            }
		                            else if(prStDe.dataElement.type.type === "trueOnly"){
		                                newInputField = '<input type="checkbox" ' +
		                                                ' ng-change="saveDatavalue(prStDes.'+ fieldId + ')"' +
		                                                commonInputFieldProperty + ' >';
		                            }
		                            else{
		                                newInputField = '<input type="text" ' +
	                                                ' ng-blur="saveDatavalue(prStDes.'+ fieldId + ')"' +
	                                                commonInputFieldProperty + ' >';
		                            }		                            
	                           	}
                           	}                            
                        }
						
                        newInputField = newInputField + ' <span ng-show="(outerForm.'+ fieldId +'.$dirty && outerForm.'+ fieldId +'.$invalid) || (outerForm.submitted && outerForm.'+ fieldId +'.$invalid) || (currentEvent.' + fieldId + ' && outerForm.' + fieldId + '.$invalid)" class="required">{{getErrorMessage(' + errorMessageId + ')}}</span> ';
                        
                        htmlCode = htmlCode.replace(inputField, newInputField);
                    }
                }
                return {htmlCode: htmlCode, hasEventDate: hasEventDate};
            }
            return null;
        },
        getForTrackedEntity: function(trackedEntity, target){
            if(!trackedEntity ){
                return null;
            }

            var htmlCode = trackedEntity.dataEntryForm ? trackedEntity.dataEntryForm.htmlCode : null;
            if(htmlCode){                
            
                var trackedEntityFormAttributes = [];
                angular.forEach(trackedEntity.attributes, function(att){                    
                    trackedEntityFormAttributes[att.id] = att;
                });

                
                var inputRegex = /<input.*?\/>/g, match, inputFields = [];
                var hasProgramDate = false;
                while (match = inputRegex.exec(htmlCode)) {                
                    inputFields.push(match[0]);                    
                }

                for(var i=0; i<inputFields.length; i++){                    
                    var inputField = inputFields[i];                    
                    var inputElement = $.parseHTML( inputField );
                    var attributes = {};
                                       
                    $(inputElement[0].attributes).each(function() {
                        attributes[this.nodeName] = this.value;                       
                    });
                    
                    var attId = '', newInputField, programId;     
                    if(attributes.hasOwnProperty('attributeid')){
                        attId = attributes['attributeid'];

                        var fieldName = attId;                        
                        var attMaxDate = trackedEntityFormAttributes[attId].allowFutureDate ? '' : 0;
                        
                        var att = trackedEntityFormAttributes[attId];
                        
                        if( att ){
                        	
                        	var commonInputFieldProperty = ' name="' + fieldName + '"' +
	                                            ' element-id="' + i + '"' +
	                                            this.getAttributesAsString(attributes) +
	                                            ' d2-focus-next-on-enter' + 
	                                            ' ng-model="selectedTei.' + attId + '" ' +
	                                            ' ng-disabled="editingDisabled"' +
	                                            ' d2-validation ' +
	                                            ' ng-required=" ' + (att.mandatory || att.unique) + '" ';
	                                            
                        	//check if attribute has optionset
                        	if(att.optionSetValue){                        		
	                            var optionSetId = att.optionSet.id;                            
	                            newInputField = '<input type="text" ' +	                                            
	                                            ' d2-typeahead-validation ' +
	                                            ' class="typeahead" ' +
	                                            ' placeholder="&#xf0d7;&nbsp;&nbsp;" ' +
	                                            ' typeahead-editable="false" ' + 
	                                            ' typeahead="option.name as option.name for option in optionSets.' + optionSetId + '.options | filter:$viewValue | limitTo:50"' +
	                                            ' typeahead-open-on-focus ' +
	                                            ' ng-blur="validationAndSkipLogic(selectedTei,\'' + attId + '\')" ' +
	                                            commonInputFieldProperty + ' >';
		                        
                        	}
                        	else{
                        		//check attribute type and generate corresponding angular input field
		                        if(att.valueType === "number"){
		                            newInputField = '<input type="text" ' +
		                                            ' d2-number-validation ' +
		                                            ' ng-blur="validationAndSkipLogic(selectedTei,\'' + attId + '\')" ' +
		                                            commonInputFieldProperty + ' >';
		                        }		                        
		                        else if(att.valueType === "bool"){
		                            newInputField = '<select ' +		                                            
		                                            ' ng-change="validationAndSkipLogic(selectedTei,\'' + attId + '\')" ' +		                                            
		                                            commonInputFieldProperty + ' > ' +
		                                            ' <option value="">{{\'please_select\'| translate}}</option>' +
		                                            ' <option value="false">{{\'no\'| translate}}</option>' + 
		                                            ' <option value="true">{{\'yes\'| translate}}</option>' +
		                                            '</select> ';
		                        }
		                        else if(att.valueType === "date"){
		                            newInputField = '<input type="text" ' + 
		                                            ' placeholder="{{dhis2CalendarFormat.keyDateFormat}}" ' +
		                                            ' max-date="' + attMaxDate + '"' + '\'' +
		                                            ' d2-date' +
		                                            ' blur-or-change="validationAndSkipLogic(selectedTei,\'' + attId + '\')" ' +
		                                            commonInputFieldProperty + ' >';
		                        }
		                        else if(att.valueType === "trueOnly"){
		                            newInputField = '<input type="checkbox" ' +
		                                            ' ng-change="validationAndSkipLogic(selectedTei,\'' + attId + '\')" ' +
		                                            commonInputFieldProperty + ' >';
		                        }
		                        else if(att.valueType === "email"){
		                            newInputField = '<input type="email" ' +
		                                            ' ng-blur="validationAndSkipLogic(selectedTei,\'' + attId + '\')" ' +
		                                            commonInputFieldProperty + ' >';
		                        }
		                        else {
		                            newInputField = '<input type="text" ' +
		                                            ' ng-blur="validationAndSkipLogic(selectedTei,\'' + attId + '\')" ' +
		                                            commonInputFieldProperty + ' >';
		                        }
	                       	}
                       }
                         
                    }                        
                 
                    if(attributes.hasOwnProperty('programid')){
                        hasProgramDate = true;
                        programId = attributes['programid'];                        
                        if(programId === 'enrollmentDate'){
                            fieldName = 'dateOfEnrollment';
                            var enMaxDate = trackedEntity.selectEnrollmentDatesInFuture ? '' : 0;
                            newInputField = '<input type="text" ' +
                                            ' name="' + fieldName + '"' +
                                            ' element-id="' + i + '"' +       
                                            this.getAttributesAsString(attributes) +
                                            ' d2-focus-next-on-enter' + 
                                            ' placeholder="{{dhis2CalendarFormat.keyDateFormat}}" ' +
                                            ' ng-model="selectedEnrollment.dateOfEnrollment" ' +
                                            ' ng-disabled="\'' + target + '\' === \'PROFILE\'"' +
                                            ' d2-date' +
                                            ' max-date="' + enMaxDate + '"' +
                                            ' ng-required="true"> ';
                        }
                        if(programId === 'dateOfIncident' && trackedEntity.displayIncidentDate){
                            fieldName = 'dateOfIncident';
                            var inMaxDate = trackedEntity.selectIncidentDatesInFuture ? '' : 0;                            
                            newInputField = '<input type="text" ' +
                                            ' name="' + fieldName + '"' +
                                            ' element-id="' + i + '"' +    
                                            this.getAttributesAsString(attributes) +
                                            ' d2-focus-next-on-enter' + 
                                            ' placeholder="{{dhis2CalendarFormat.keyDateFormat}}" ' +
                                            ' ng-model="selectedEnrollment.dateOfIncident" ' +
                                            ' ng-disabled="\'' + target + '\' === \'PROFILE\'"' +
                                            ' d2-date ' +
                                            ' max-date="' + inMaxDate + '"> ';
                        }
                    }
                    
                    newInputField = //'<ng-form name="innerForm">' + 
                                        newInputField + 
                                        ' <span ng-show="outerForm.submitted && outerForm.'+ fieldName +'.$invalid" class="required">{{\'required\' | translate}}</span> ';
                                    //'</ng-form>';
                                    
                    htmlCode = htmlCode.replace(inputField, newInputField);                    
                }                
                return {htmlCode: htmlCode, hasProgramDate: hasProgramDate};
            }            
            return null;
        },
        getAttributesAsString: function(attributes){
            if(attributes){
                var attributesAsString = '';                
                for(var prop in attributes){
                    if(prop !== 'value'){
                        attributesAsString += prop + '="' + attributes[prop] + '" ';
                    }
                }
                return attributesAsString;
            }
            return null;
        }
    };            
})

/* Context menu for grid*/
.service('ContextMenuSelectedItem', function(){
    this.selectedItem = '';
    
    this.setSelectedItem = function(selectedItem){  
        this.selectedItem = selectedItem;        
    };
    
    this.getSelectedItem = function(){
        return this.selectedItem;
    };
})

/* Error messages*/
.service('ErrorMessageService', function(){
    this.errorMessages = {};
    
    this.setErrorMessages = function(errorMessages){  
        this.errorMessages = errorMessages;        
    };
    
    this.getErrorMessages = function(){
        return this.errorMessages;
    };
    
    this.get = function(id){
        return this.errorMessages[id];
    };
})

/* Modal service for user interaction */
.service('ModalService', ['$modal', function($modal) {

    var modalDefaults = {
        backdrop: true,
        keyboard: true,
        modalFade: true,
        templateUrl: 'views/modal.html'
    };

    var modalOptions = {
        closeButtonText: 'Close',
        actionButtonText: 'OK',
        headerText: 'Proceed?',
        bodyText: 'Perform this action?'
    };

    this.showModal = function(customModalDefaults, customModalOptions) {
        if (!customModalDefaults)
            customModalDefaults = {};
        customModalDefaults.backdrop = 'static';
        return this.show(customModalDefaults, customModalOptions);
    };

    this.show = function(customModalDefaults, customModalOptions) {
        //Create temp objects to work with since we're in a singleton service
        var tempModalDefaults = {};
        var tempModalOptions = {};

        //Map angular-ui modal custom defaults to modal defaults defined in service
        angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);

        //Map modal.html $scope custom properties to defaults defined in service
        angular.extend(tempModalOptions, modalOptions, customModalOptions);

        if (!tempModalDefaults.controller) {
            tempModalDefaults.controller = function($scope, $modalInstance) {
                $scope.modalOptions = tempModalOptions;
                $scope.modalOptions.ok = function(result) {
                    $modalInstance.close(result);
                };
                $scope.modalOptions.close = function(result) {
                    $modalInstance.dismiss('cancel');
                };
            };
        }

        return $modal.open(tempModalDefaults).result;
    };

}])

/* Dialog service for user interaction */
.service('DialogService', ['$modal', function($modal) {

    var dialogDefaults = {
        backdrop: true,
        keyboard: true,
        backdropClick: true,
        modalFade: true,            
        templateUrl: 'views/dialog.html'
    };

    var dialogOptions = {
        closeButtonText: 'close',
        actionButtonText: 'ok',
        headerText: 'dhis2_tracker',
        bodyText: 'Perform this action?'
    };

    this.showDialog = function(customDialogDefaults, customDialogOptions) {
        if (!customDialogDefaults)
            customDialogDefaults = {};
        customDialogDefaults.backdropClick = false;
        return this.show(customDialogDefaults, customDialogOptions);
    };

    this.show = function(customDialogDefaults, customDialogOptions) {
        //Create temp objects to work with since we're in a singleton service
        var tempDialogDefaults = {};
        var tempDialogOptions = {};

        //Map angular-ui modal custom defaults to modal defaults defined in service
        angular.extend(tempDialogDefaults, dialogDefaults, customDialogDefaults);

        //Map modal.html $scope custom properties to defaults defined in service
        angular.extend(tempDialogOptions, dialogOptions, customDialogOptions);

        if (!tempDialogDefaults.controller) {
            tempDialogDefaults.controller = function($scope, $modalInstance) {
                $scope.dialogOptions = tempDialogOptions;
                $scope.dialogOptions.ok = function(result) {
                    $modalInstance.close(result);
                };                           
            };
        }

        return $modal.open(tempDialogDefaults).result;
    };

}])

.service('Paginator', function () {
    this.page = 1;
    this.pageSize = 50;
    this.itemCount = 0;
    this.pageCount = 0;
    this.toolBarDisplay = 5;

    this.setPage = function (page) {
        if (page > this.getPageCount()) {
            return;
        }

        this.page = page;
    };
    
    this.getPage = function(){
        return this.page;
    };
    
    this.setPageSize = function(pageSize){
      this.pageSize = pageSize;
    };
    
    this.getPageSize = function(){
        return this.pageSize;
    };
    
    this.setItemCount = function(itemCount){
      this.itemCount = itemCount;
    };
    
    this.getItemCount = function(){
        return this.itemCount;
    };
    
    this.setPageCount = function(pageCount){
        this.pageCount = pageCount;
    };

    this.getPageCount = function () {
        return this.pageCount;
    };
    
    this.setToolBarDisplay = function(toolBarDisplay){
        this.toolBarDisplay = toolBarDisplay;
    };

    this.getToolBarDisplay = function(){
        return this.toolBarDisplay;
    };
    
    this.lowerLimit = function() { 
        var pageCountLimitPerPageDiff = this.getPageCount() - this.getToolBarDisplay();

        if (pageCountLimitPerPageDiff < 0) { 
            return 0; 
        }

        if (this.getPage() > pageCountLimitPerPageDiff + 1) { 
            return pageCountLimitPerPageDiff; 
        } 

        var low = this.getPage() - (Math.ceil(this.getToolBarDisplay()/2) - 1); 

        return Math.max(low, 0);
    };
})

.service('GridColumnService', function(){
    return {        
        columnExists: function(cols, id) {
            var colExists = false;
            if(!angular.isObject(cols) || !id || angular.isObject(cols) && !cols.length){
                return colExists;
            }
            
            for(var i=0; i<cols.length && !colExists; i++){
                if(cols[i].id === id){
                    colExists = true;
                }
            }
            return colExists;
        }
    };
});
