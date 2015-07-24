/* global trackerCapture, angular */

trackerCapture.controller('RelationshipController',
        function($scope,
                $rootScope,
                $modal,                
                $location,
                TEIService,
                AttributesFactory,
                CurrentSelection,
                RelationshipFactory,
                ModalService,
                DialogService) {
    $rootScope.showAddRelationshipDiv = false;    
    $scope.relatedProgramRelationship = false;
    
    //listen for the selected entity       
    $scope.$on('dashboardWidgets', function(event, args) { 
        $scope.relationshipTypes = []; 
        $scope.relationships = [];
        $scope.relatedTeis = [];
        $scope.selections = CurrentSelection.get();
        $scope.optionSets = $scope.selections.optionSets;
        $scope.selectedTei = angular.copy($scope.selections.tei);        
        $scope.attributesById = CurrentSelection.getAttributesById();
        
        $scope.attributes = [];
        for(var key in $scope.attributesById){
            if($scope.attributesById.hasOwnProperty(key)){
                $scope.attributes.push($scope.attributesById[key]);
            }            
        }
        
        $scope.trackedEntity = $scope.selections.te;
        $scope.selectedEnrollment = $scope.selections.selectedEnrollment;
        $scope.selectedProgram = $scope.selections.pr;
        $scope.programs = $scope.selections.pr;
        
        RelationshipFactory.getAll().then(function(rels){
            $scope.relationshipTypes = rels;    
            angular.forEach(rels, function(rel){
                $scope.relationships[rel.id] = rel;
            });
            
            setRelationships();            
        });
    });
    
    $scope.showAddRelationship = function(related) {
        $scope.relatedProgramRelationship = related;
        $rootScope.showAddRelationshipDiv = !$rootScope.showAddRelationshipDiv;
       
        if($rootScope.showAddRelationshipDiv){
            var modalInstance = $modal.open({
                templateUrl: 'components/relationship/add-relationship.html',
                controller: 'AddRelationshipController',
                windowClass: 'modal-full-window',
                resolve: {
                    relationshipTypes: function () {
                        return $scope.relationshipTypes;
                    },
                    selections: function () {
                        return $scope.selections;
                    },
                    selectedTei: function(){
                        return $scope.selectedTei;
                    },
                    selectedProgram: function(){
                        return $scope.selectedProgram;
                    },
                    relatedProgramRelationship: function(){
                        return $scope.relatedProgramRelationship;
                    }
                }
            });

            modalInstance.result.then(function (relationships) {
                $scope.selectedTei.relationships = relationships;
                
                setRelationships();
            });
        }
    };
    
    $scope.removeRelationship = function(rel){
        
        var modalOptions = {
            closeButtonText: 'cancel',
            actionButtonText: 'delete',
            headerText: 'delete',
            bodyText: 'are_you_sure_to_delete_relationship'
        };

        ModalService.showModal({}, modalOptions).then(function(result){
            
            var index = -1;
            for(var i=0; i<$scope.selectedTei.relationships.length; i++){
                if($scope.selectedTei.relationships[i].relationship === rel.relId){
                    index = i;
                    break;
                }
            }

            if( index !== -1 ){
                $scope.selectedTei.relationships.splice(index,1);
                TEIService.update($scope.selectedTei, $scope.optionSets, $scope.attributesById).then(function(response){
                    if(response.status !== 'SUCCESS'){//update has failed
                        var dialogOptions = {
                                headerText: 'update_error',
                                bodyText: response.description
                            };
                        DialogService.showDialog({}, dialogOptions);
                        return;
                    }

                    var selections = CurrentSelection.get();
                    CurrentSelection.set({tei: $scope.selectedTei, te: $scope.selectedTei.trackedEntity, prs: selections.prs, pr: $scope.selectedProgram, prNames: selections.prNames, prStNames: selections.prStNames, enrollments: selections.enrollments, selectedEnrollment: $scope.selectedEnrollment, optionSets: selections.optionSets});                                
                    setRelationships();
                });
            }
        });        
    };
    
    $scope.showDashboard = function(teiId, relId){
        
        var dashboardProgram = null;
        
        if($scope.selectedProgram && $scope.selectedProgram.relationshipType){
            if($scope.selectedProgram.relationshipType.id === relId && $scope.selectedProgram.relatedProgram ){
                dashboardProgram = $scope.selectedProgram.relatedProgram.id;
            }
        }        
    
        $location.path('/dashboard').search({tei: teiId, program: dashboardProgram}); 
    };
    
    var setRelationships = function(){
        $scope.relatedTeis = [];
        angular.forEach($scope.selectedTei.relationships, function(rel){
            var teiId = rel.trackedEntityInstanceA;
            var relName = $scope.relationships[rel.relationship].aIsToB;
            if($scope.selectedTei.trackedEntityInstance === rel.trackedEntityInstanceA){
                teiId = rel.trackedEntityInstanceB;
                relName = $scope.relationships[rel.relationship].bIsToA;
            }
            var relative = {trackedEntityInstance: teiId, relName: relName, relId: rel.relationship, attributes: getRelativeAttributes(rel)};            
            $scope.relatedTeis.push(relative);
        });
    };
    
    var getRelativeAttributes = function(tei){
        
        var attributes = {};
        
        if(tei && tei.relative && tei.relative.attributes && !tei.relative.processed){
            angular.forEach(tei.relative.attributes, function(att){                
                var val = AttributesFactory.formatAttributeValue(att,$scope.attributesById, $scope.optionSets, 'USER');                
                attributes[att.attribute] = val;
            });
        }
        
        if(tei && tei.relative && tei.relative.processed){
            attributes = tei.relative.attributes;
        }
        
        return attributes;
    };
})

//Controller for adding new relationship
.controller('AddRelationshipController', 
    function($scope, 
            $rootScope,
            $translate,
            DateUtils,
            CurrentSelection,
            OperatorFactory,
            AttributesFactory,
            EntityQueryFactory,
            TEIService,
            TEIGridService,
            DialogService,
            Paginator,
            SessionStorageService,
            $modalInstance, 
            relationshipTypes,
            selectedProgram,
            relatedProgramRelationship,
            selections,
            selectedTei){
    
    $scope.relationshipTypes = relationshipTypes;
    $scope.selectedProgram = selectedProgram;
    $scope.relatedProgramRelationship = relatedProgramRelationship;
    $scope.selectedTei = selectedTei;
    $scope.programs = selections.prs;
    $scope.attributesById = CurrentSelection.getAttributesById();
    
    $scope.selectedRelationship = {};
    $scope.relationship = {};

    //Selections
    $scope.selectedOrgUnit = SessionStorageService.get('SELECTED_OU');
    $scope.optionSets = selections.optionSets;
    $scope.selectedTeiForDisplay = angular.copy($scope.selectedTei);
    $scope.ouModes = [{name: 'SELECTED'}, {name: 'CHILDREN'}, {name: 'DESCENDANTS'}, {name: 'ACCESSIBLE'}];         
    $scope.selectedOuMode = $scope.ouModes[0];
    
    //Paging
    $scope.pager = {pageSize: 50, page: 1, toolBarDisplay: 5}; 
    
    //Searching
    $scope.showAdvancedSearchDiv = false;
    $scope.searchText = {value: null};
    $scope.emptySearchText = false;
    $scope.searchFilterExists = false;   
    $scope.defaultOperators = OperatorFactory.defaultOperators;
    $scope.boolOperators = OperatorFactory.boolOperators;
    
    $scope.trackedEntityList = null; 
    $scope.enrollment = {programStartDate: '', programEndDate: '', operator: $scope.defaultOperators[0]};
   
    $scope.searchMode = {listAll: 'LIST_ALL', freeText: 'FREE_TEXT', attributeBased: 'ATTRIBUTE_BASED'};
    $scope.selectedSearchMode = $scope.searchMode.listAll;
    
    if(angular.isObject($scope.programs) && $scope.programs.length === 1){
        $scope.selectedProgramForRelative = $scope.programs[0];        
    } 
    
    if($scope.selectedProgram){
        if($scope.selectedProgram.relatedProgram && $scope.relatedProgramRelationship){
            angular.forEach($scope.programs, function(pr){
                if(pr.id === $scope.selectedProgram.relatedProgram.id){
                    $scope.selectedProgramForRelative = pr;
                }
            });
        }
        
        if($scope.selectedProgram.relationshipType){
            angular.forEach($scope.relationshipTypes, function(rel){
                if(rel.id === $scope.selectedProgram.relationshipType.id){
                    $scope.relationship.selected = rel;
                }
            });
        }
    }
    
    //watch for selection of relationship
    $scope.$watch('relationship.selected', function() {        
        if( angular.isObject($scope.relationship.selected)){
            $scope.selectedRelationship = {aIsToB: $scope.relationship.selected.aIsToB, bIsToA: $scope.relationship.selected.bIsToA};  
        }
    });
    
    function resetFields(){
        
        $scope.teiForRelationship = null;
        $scope.teiFetched = false;    
        $scope.emptySearchText = false;
        $scope.emptySearchAttribute = false;
        $scope.showAdvancedSearchDiv = false;
        $scope.showRegistrationDiv = false;  
        $scope.showTrackedEntityDiv = false;
        $scope.trackedEntityList = null; 
        $scope.teiCount = null;

        $scope.queryUrl = null;
        $scope.programUrl = null;
        $scope.attributeUrl = {url: null, hasValue: false};
        $scope.sortColumn = {};
    }
    
    //listen for selections
    $scope.$on('relationship', function() { 
        var relationshipInfo = CurrentSelection.getRelationshipInfo();
        $scope.teiForRelationship = relationshipInfo.tei;
    });
    
    //sortGrid
    $scope.sortGrid = function(gridHeader){
        if ($scope.sortColumn && $scope.sortColumn.id === gridHeader.id){
            $scope.reverse = !$scope.reverse;
            return;
        }        
        $scope.sortColumn = gridHeader;
        if($scope.sortColumn.valueType === 'date'){
            $scope.reverse = true;
        }
        else{
            $scope.reverse = false;    
        }
    };
    
    $scope.d2Sort = function(tei){        
        if($scope.sortColumn && $scope.sortColumn.valueType === 'date'){            
            var d = tei[$scope.sortColumn.id];         
            return DateUtils.getDate(d);
        }
        return tei[$scope.sortColumn.id];
    };
    
    $scope.search = function(mode){ 
        
        resetFields();
        
        $scope.selectedSearchMode = mode;        
   
        if($scope.selectedProgramForRelative){
            $scope.programUrl = 'program=' + $scope.selectedProgramForRelative.id;
        }        
        
        //check search mode
        if( $scope.selectedSearchMode === $scope.searchMode.freeText ){ 
            
            if(!$scope.searchText.value){                
                $scope.emptySearchText = true;
                $scope.teiFetched = false;   
                $scope.teiCount = null;
                return;
            }       

            $scope.queryUrl = 'query=LIKE:' + $scope.searchText.value;                     
        }
        
        if( $scope.selectedSearchMode === $scope.searchMode.attributeBased ){            
            $scope.searchText.value = null;
            $scope.attributeUrl = EntityQueryFactory.getAttributesQuery($scope.attributes, $scope.enrollment);
            
            if(!$scope.attributeUrl.hasValue && !$scope.selectedProgramForRelative){
                $scope.emptySearchAttribute = true;
                $scope.teiFetched = false;   
                $scope.teiCount = null;
                return;
            }
        }
        
        $scope.fetchTei();
    };
    
    $scope.fetchTei = function(){

        //get events for the specified parameters
        TEIService.search($scope.selectedOrgUnit.id, 
                                            $scope.selectedOuMode.name,
                                            $scope.queryUrl,
                                            $scope.programUrl,
                                            $scope.attributeUrl.url,
                                            $scope.pager,
                                            true).then(function(data){
            //$scope.trackedEntityList = data;            
            if(data.rows){
                $scope.teiCount = data.rows.length;
            }                    
            
            if( data.metaData.pager ){
                $scope.pager = data.metaData.pager;
                $scope.pager.toolBarDisplay = 5;

                Paginator.setPage($scope.pager.page);
                Paginator.setPageCount($scope.pager.pageCount);
                Paginator.setPageSize($scope.pager.pageSize);
                Paginator.setItemCount($scope.pager.total);                    
            }
            
            //process tei grid
            $scope.trackedEntityList = TEIGridService.format(data,false, $scope.optionSets);
            $scope.showTrackedEntityDiv = true;
            $scope.teiFetched = true;
            
            if(!$scope.sortColumn.id){                                      
                $scope.sortGrid({id: 'created', name: $translate('registration_date'), valueType: 'date', displayInListNoProgram: false, showFilter: false, show: false});
            }
            
        });
    };
    
    //set attributes as per selected program
    $scope.setAttributesForSearch = function(program){
        
        $scope.selectedProgramForRelative = program;
        AttributesFactory.getByProgram($scope.selectedProgramForRelative).then(function(atts){
            $scope.attributes = atts;
            $scope.attributes = $scope.generateAttributeFilters($scope.attributes);
            $scope.gridColumns = $scope.generateGridColumns($scope.attributes);
        });
        
        $scope.search( $scope.selectedSearchMode );        
    }; 
    
    $scope.setAttributesForSearch( $scope.selectedProgramForRelative );
    
    $scope.jumpToPage = function(){
        if($scope.pager && $scope.pager.page && $scope.pager.pageCount && $scope.pager.page > $scope.pager.pageCount){
            $scope.pager.page = $scope.pager.pageCount;
        }
        
        $scope.search($scope.selectedSearchMode);
    };
    
    $scope.resetPageSize = function(){
        $scope.pager.page = 1;        
        $scope.search($scope.selectedSearchMode);
    };
    
    $scope.getPage = function(page){
        $scope.pager.page = page;
        $scope.search($scope.selectedSearchMode);
    };
    
    $scope.generateAttributeFilters = function(attributes){

        angular.forEach(attributes, function(attribute){
            if(attribute.valueType === 'number' || attribute.valueType === 'date'){
                attribute.operator = $scope.defaultOperators[0];
            }
        });
                    
        return attributes;
    };

    //generate grid columns from teilist attributes
    $scope.generateGridColumns = function(attributes){

        var columns = attributes ? angular.copy(attributes) : [];
       
        //also add extra columns which are not part of attributes (orgunit for example)
        columns.push({id: 'orgUnitName', name: 'Organisation unit', type: 'string', displayInListNoProgram: false});
        columns.push({id: 'created', name: 'Registration date', type: 'string', displayInListNoProgram: false});
        
        //generate grid column for the selected program/attributes
        angular.forEach(columns, function(column){
            if(column.id === 'orgUnitName' && $scope.selectedOuMode.name !== 'SELECTED'){
                column.show = true;
            }
            
            if(column.displayInListNoProgram){
                column.show = true;
            }           
           
            if(column.type === 'date'){
                $scope.filterText[column.id]= {start: '', end: ''};
            }
        });        
        return columns;        
    };   
    
    $scope.showHideSearch = function(simpleSearch){
        if(simpleSearch){
            $scope.showAdvancedSearchDiv = false;
        }
        else{
            $scope.showAdvancedSearchDiv = !$scope.showAdvancedSearchDiv;
        }
        
        if($scope.showAdvancedSearchDiv){
            $scope.showTrackedEntityDiv = false;
        }
        else{
            $scope.showTrackedEntityDiv = true;
        }
    };
    
    $scope.showRegistration = function(){
        $scope.showRegistrationDiv = !$scope.showRegistrationDiv;
        
        if($scope.showRegistrationDiv){
            $scope.showTrackedEntityDiv = false;
            /*$timeout(function() { 
                $rootScope.$broadcast('registrationWidget', {registrationMode: 'RELATIONSHIP'});
            }, 100);*/
        }
        else{
            $scope.showTrackedEntityDiv = true;            
        }        
    };
    
    $scope.close = function () {
        $modalInstance.close($scope.selectedTei.relationships ? $scope.selectedTei.relationships : []);
        $rootScope.showAddRelationshipDiv = !$rootScope.showAddRelationshipDiv;
    };
    
    $scope.setRelationshipSides = function(side){
        if(side === 'A'){            
            $scope.selectedRelationship.bIsToA = $scope.selectedRelationship.aIsToB === $scope.relationship.selected.aIsToB ? $scope.relationship.selected.bIsToA : $scope.relationship.selected.aIsToB;
        }
        if(side === 'B'){
            $scope.selectedRelationship.aIsToB = $scope.selectedRelationship.bIsToA === $scope.relationship.selected.bIsToA ? $scope.relationship.selected.aIsToB : $scope.relationship.selected.bIsToA;
        }
    };
    
    $scope.assignRelationship = function(relativeTei){
        $scope.teiForRelationship = relativeTei;
        $rootScope.showAddRelationshipDiv = !$rootScope.showAddRelationshipDiv;
    };
    
    
    $scope.back = function(){
        $scope.teiForRelationship = null;
        $rootScope.showAddRelationshipDiv = !$rootScope.showAddRelationshipDiv;
    };
    
    $scope.addRelationship = function(){
        if($scope.selectedTei && $scope.teiForRelationship && $scope.relationship.selected){            
            var tei = angular.copy($scope.selectedTei);
            var relationship = {};
            relationship.relationship = $scope.relationship.selected.id;
            relationship.displayName = $scope.relationship.selected.name;
            relationship.relative = {};
            
            
            relationship.trackedEntityInstanceA = $scope.selectedRelationship.aIsToB === $scope.relationship.selected.aIsToB ? $scope.selectedTei.trackedEntityInstance : $scope.teiForRelationship.id;
            relationship.trackedEntityInstanceB = $scope.selectedRelationship.bIsToA === $scope.relationship.selected.bIsToA ? $scope.teiForRelationship.id : $scope.selectedTei.trackedEntityInstance;
            
            tei.relationships = [];
            angular.forEach($scope.selectedTei.relationships, function(rel){
                tei.relationships.push({relationship: rel.relationship, displayName: rel.displayName, trackedEntityInstanceA: rel.trackedEntityInstanceA, trackedEntityInstanceB: rel.trackedEntityInstanceB});
            });
            tei.relationships.push(relationship);
            
            TEIService.update(tei, $scope.optionSets, $scope.attributesById).then(function(response){
                if(response.status !== 'SUCCESS'){//update has failed
                    var dialogOptions = {
                            headerText: 'relationship_error',
                            bodyText: response.description
                        };
                    DialogService.showDialog({}, dialogOptions);
                    return;
                }
                
                relationship.relative.processed = true;
                relationship.relative.attributes = $scope.teiForRelationship;
                
                if($scope.selectedTei.relationships){
                    $scope.selectedTei.relationships.push(relationship);
                }
                else{
                    $scope.selectedTei.relationships = [relationship];
                }
                
                $modalInstance.close($scope.selectedTei.relationships);                
            });
        }        
    };
})

.controller('RelativeRegistrationController', 
        function($rootScope,
                $scope,
                $timeout,
                AttributesFactory,
                TEService,
                TEIService,
                EnrollmentService,
                DialogService,
                CurrentSelection,
                OptionSetService,
                DateUtils,
                SessionStorageService) {
    $scope.selectedOrgUnit = SessionStorageService.get('SELECTED_OU');
    $scope.enrollment = {enrollmentDate: '', incidentDate: ''};    
    
    var selections = CurrentSelection.get();
    $scope.optionSets = selections.optionSets;
    $scope.programs = selections.prs;    
  
    if(angular.isObject($scope.programs) && $scope.programs.length === 1){
        $scope.selectedProgramForRelative = $scope.programs[0];
        AttributesFactory.getByProgram($scope.selectedProgramForRelative).then(function(atts){
            $scope.attributes = atts;
        });
    }  
    
    //watch for selection of program
    $scope.$watch('selectedProgramForRelative', function() {        
        AttributesFactory.getByProgram($scope.selectedProgramForRelative).then(function(atts){
            $scope.attributes = atts;
        });
    }); 
            
    $scope.trackedEntities = {available: []};
    TEService.getAll().then(function(entities){
        $scope.trackedEntities.available = entities;   
        $scope.trackedEntities.selected = $scope.trackedEntities.available[0];
    });
    
    $scope.registerEntity = function(){
        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid ){
            return false;
        }
        
        //form is valid, continue the registration
        //get selected entity
        var selectedTrackedEntity = $scope.trackedEntities.selected.id; 
        if($scope.selectedProgramForRelative){
            selectedTrackedEntity = $scope.selectedProgramForRelative.trackedEntity.id;
        }
        
        //get tei attributes and their values
        //but there could be a case where attributes are non-mandatory and
        //registration form comes empty, in this case enforce at least one value
        $scope.valueExists = false;
        var registrationAttributes = [];    
        angular.forEach($scope.attributes, function(attribute){
            var val = attribute.value;
            if(!angular.isUndefined(val)){                
                if(attribute.valueType === 'date'){
                    val = DateUtils.formatFromUserToApi(val);
                }
                if(attribute.optionSetValue &&
                        attribute.optionSet &&
                        attribute.optionSet.id &&                
                        $scope.optionSets[attribute.optionSet.id] &&
                        $scope.optionSets[attribute.optionSet.id].options ){  
                    
                    val = OptionSetService.getCode($scope.optionSets[attribute.optionSet.id].options, val);
                }
                registrationAttributes.push({attribute: attribute.id, value: val});
                $scope.valueExists = true;
            } 
        });       
        
        if(!$scope.valueExists){
            //registration form is empty
            return false;
        }
        
        //prepare tei model and do registration
        $scope.tei = {trackedEntity: selectedTrackedEntity, orgUnit: $scope.selectedOrgUnit.id, attributes: registrationAttributes };   
        var teiId = '';
    
        TEIService.register($scope.tei).then(function(tei){
            
            if(tei.status === 'SUCCESS'){
                
                teiId = tei.reference;
                
                //registration is successful and check for enrollment
                if($scope.selectedProgramForRelative){    
                    //enroll TEI
                    var enrollment = {trackedEntityInstance: teiId,
                                program: $scope.selectedProgramForRelative.id,
                                status: 'ACTIVE',
                                dateOfEnrollment: DateUtils.formatFromUserToApi($scope.enrollment.enrollmentDate),
                                dateOfIncident: $scope.enrollment.incidentDate === '' ? DateUtils.formatFromUserToApi($scope.enrollment.enrollmentDate) : DateUtils.formatFromUserToApi($scope.enrollment.incidentDate)
                            };
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
                    });
                }
            }
            else{
                //registration has failed
                var dialogOptions = {
                        headerText: 'registration_error',
                        bodyText: tei.description
                    };
                DialogService.showDialog({}, dialogOptions);
                return;
            }
            
            $timeout(function() { 
                //reset form
                angular.forEach($scope.attributes, function(attribute){
                    delete attribute.value;                
                });            

                $scope.enrollment.enrollmentDate = '';
                $scope.enrollment.incidentDate =  '';
                $scope.outerForm.submitted = false; 
                
                $scope.tei.id = teiId;
                $scope.broadCastSelections();
                
            }, 100);        
            
        });
    };
    
    $scope.broadCastSelections = function(){
        if($scope.tei){
            angular.forEach($scope.tei.attributes, function(att){
                $scope.tei[att.attribute] = att.value;
            });

            $scope.tei.orgUnitName = $scope.selectedOrgUnit.name;
            $scope.tei.created = DateUtils.formatFromApiToUser(new Date());
            
            CurrentSelection.setRelationshipInfo({tei: $scope.tei});
            
            $timeout(function() { 
                $rootScope.$broadcast('relationship', {});
            }, 100);
        }        
    };
});