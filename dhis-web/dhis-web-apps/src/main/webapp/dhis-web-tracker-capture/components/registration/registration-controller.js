/* global trackerCapture, angular */

trackerCapture.controller('RegistrationController', 
        function($rootScope,
                $scope,
                $location,
                $timeout,
                AttributesFactory,
                DHIS2EventFactory,
                TEService,
                TEFormService,
                CustomFormService,
                EnrollmentService,
                DialogService,
                CurrentSelection,
                OptionSetService,
                EventUtils,
                RegistrationService,
                DateUtils,
                SessionStorageService) {
    
    $scope.today = DateUtils.getToday();
    $scope.trackedEntityForm = null;
    $scope.customForm = null;    
    $scope.selectedTei = {};
    $scope.tei = {};
    $scope.registrationMode = null;
    
    $scope.attributesById = CurrentSelection.getAttributesById();
    if(!$scope.attributesById){
        $scope.attributesById = [];
        AttributesFactory.getAll().then(function(atts){
            angular.forEach(atts, function(att){
                $scope.attributesById[att.id] = att;
            });
            
            CurrentSelection.setAttributesById($scope.attributesById);
        });
    }    
    
    $scope.optionSets = CurrentSelection.getOptionSets();        
    if(!$scope.optionSets){
        $scope.optionSets = [];
        OptionSetService.getAll().then(function(optionSets){
            angular.forEach(optionSets, function(optionSet){                        
                $scope.optionSets[optionSet.id] = optionSet;
            });

            CurrentSelection.setOptionSets($scope.optionSets);
        });
    }
    
    $scope.selectedOrgUnit = SessionStorageService.get('SELECTED_OU');
    $scope.selectedEnrollment = {dateOfEnrollment: '', dateOfIncident: ''};   
            
    $scope.trackedEntities = {available: []};
    TEService.getAll().then(function(entities){
        $scope.trackedEntities.available = entities;   
        $scope.trackedEntities.selected = $scope.trackedEntities.available[0];
    });
    
    //watch for selection of program
    $scope.$watch('selectedProgram', function() {        
        $scope.trackedEntityForm = null;
        $scope.customForm = null;
        if($scope.registrationMode === 'REGISTRATION'){
            $scope.getAttributes($scope.registrationMode);
        }        
    }); 
    
    //listen to modes of registration
    $scope.$on('registrationWidget', function(event, args){
        $scope.selectedTei = {};
        $scope.tei = {};
        $scope.registrationMode = args.registrationMode;
        $scope.getAttributes($scope.registrationMode);
        
        if($scope.registrationMode !== 'REGISTRATION'){
            $scope.selectedTei = args.selectedTei;            
            $scope.tei = angular.copy(args.selectedTei);            
        }
        
        if($scope.registrationMode === 'PROFILE'){
            $scope.selectedEnrollment = args.enrollment;
        }
    });
        
    $scope.getAttributes = function(_mode){
        var mode = _mode ? _mode : 'ENROLLMENT';
        AttributesFactory.getByProgram($scope.selectedProgram).then(function(atts){
            $scope.attributes = atts;
            $scope.customFormExists = false;               
            TEFormService.getByProgram($scope.selectedProgram, $scope.attributes).then(function(teForm){
                if(angular.isObject(teForm)){                        
                    $scope.customFormExists = true;
                    $scope.trackedEntityForm = teForm;                      
                    $scope.customForm = CustomFormService.getForTrackedEntity($scope.trackedEntityForm, mode);
                }                    
            });  
        });        
    };
    
    $scope.registerEntity = function(destination){        

        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid ){
            return false;
        }                   
        
        //form is valid, continue the registration
        //get selected entity        
        if(!$scope.selectedTei.trackedEntityInstance){
            $scope.selectedTei.trackedEntity = $scope.tei.trackedEntity = $scope.selectedProgram && $scope.selectedProgram.trackedEntity && $scope.selectedProgram.trackedEntity.id ? $scope.selectedProgram.trackedEntity.id : $scope.trackedEntities.selected.id;
            $scope.selectedTei.orgUnit = $scope.tei.orgUnit = $scope.selectedOrgUnit.id;
            $scope.selectedTei.attributes = $scope.selectedTei.attributes = [];
        }
        
        //get tei attributes and their values
        //but there could be a case where attributes are non-mandatory and
        //registration form comes empty, in this case enforce at least one value        
        
        var result = RegistrationService.processForm($scope.tei, $scope.selectedTei, $scope.attributesById);
        $scope.formEmpty = result.formEmpty;
        $scope.tei = result.tei;
        
        if($scope.formEmpty){//registration form is empty
            return false;
        }
        
        RegistrationService.registerOrUpdate($scope.tei, $scope.optionSets, $scope.attributesById).then(function(response){
            
            if(response.status === 'SUCCESS'){
                
                $scope.tei.trackedEntityInstance = response.reference;
                
                if( $scope.registrationMode === 'PROFILE' ){                    
                    reloadProfileWidget();
                }
                else{
                    if( $scope.selectedProgram ){
                        //enroll TEI
                        var enrollment = {};
                        enrollment.trackedEntityInstance = $scope.tei.trackedEntityInstance;
                        enrollment.program = $scope.selectedProgram.id;
                        enrollment.status = 'ACTIVE';
                        enrollment.orgUnit = $scope.selectedOrgUnit.id;
                        enrollment.dateOfEnrollment = $scope.selectedEnrollment.dateOfEnrollment;
                        enrollment.dateOfIncident = $scope.selectedEnrollment.dateOfIncident === '' ? $scope.selectedEnrollment.dateOfEnrollment : $scope.selectedEnrollment.dateOfIncident;

                        EnrollmentService.enroll(enrollment).then(function(data){
                            if(data.status !== 'SUCCESS'){
                                //enrollment has failed
                                var dialogOptions = {
                                        headerText: 'enrollment_error',
                                        bodyText: data.description
                                    };
                                DialogService.showDialog({}, dialogOptions);
                                return;
                            }
                            else{
                                enrollment.enrollment = data.reference;
                                $scope.selectedEnrollment = enrollment;                                                                
                                var dhis2Events = EventUtils.autoGenerateEvents($scope.tei.trackedEntityInstance, $scope.selectedProgram, $scope.selectedOrgUnit, enrollment);
                                if(dhis2Events.events.length > 0){
                                    DHIS2EventFactory.create(dhis2Events).then(function(data){
                                        notifyRegistrtaionCompletion(destination, $scope.tei.trackedEntityInstance);
                                    });
                                }else{
                                    notifyRegistrtaionCompletion(destination, $scope.tei.trackedEntityInstance);
                                }                            
                            }
                        });
                    }
                    else{
                       notifyRegistrtaionCompletion(destination, $scope.tei.trackedEntityInstance); 
                    }
                }                
            }
            else{//update/registration has failed
                var dialogOptions = {
                        headerText: $scope.tei && $scope.tei.trackedEntityInstance ? 'update_error' : 'registration_error',
                        bodyText: response.description
                    };
                DialogService.showDialog({}, dialogOptions);
                return;
            }
        });        
    };
    
    $scope.broadCastSelections = function(){
        angular.forEach($scope.tei.attributes, function(att){
            $scope.tei[att.attribute] = att.value;
        });
        
        $scope.tei.orgUnitName = $scope.selectedOrgUnit.name;
        $scope.tei.created = DateUtils.formatFromApiToUser(new Date());
        CurrentSelection.setRelationshipInfo({tei: $scope.tei, src: $scope.selectedRelationshipSource});
        $timeout(function() { 
            $rootScope.$broadcast('relationship', {});
        }, 100);
    };
    
    var goToDashboard = function(destination, teiId){
        //reset form
        $scope.selectedTei = {};
        $scope.selectedEnrollment = {};
        $scope.outerForm.submitted = false;

        if(destination === 'DASHBOARD') {
            $location.path('/dashboard').search({tei: teiId,                                            
                                    program: $scope.selectedProgram ? $scope.selectedProgram.id: null});
        }            
        else if(destination === 'RELATIONSHIP' ){
            $scope.tei.trackedEntityInstance = teiId;
            $scope.broadCastSelections();
        }
    };
    
    var reloadProfileWidget = function(){
        var selections = CurrentSelection.get();
        CurrentSelection.set({tei: $scope.selectedTei, te: $scope.selectedTei.trackedEntity, prs: selections.prs, pr: $scope.selectedProgram, prNames: selections.prNames, prStNames: selections.prStNames, enrollments: selections.enrollments, selectedEnrollment: $scope.selectedEnrollment, optionSets: selections.optionSets});        
        $timeout(function() { 
            $rootScope.$broadcast('profileWidget', {});            
        }, 100);
    };
    
    var notifyRegistrtaionCompletion = function(destination, teiId){
        goToDashboard( destination ? destination : 'DASHBOARD', teiId );
    };
});