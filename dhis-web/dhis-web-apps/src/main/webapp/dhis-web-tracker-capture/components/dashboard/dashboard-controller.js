/* global trackerCapture, angular */

//Controller for dashboard
trackerCapture.controller('DashboardController',
        function($rootScope,
                $scope,
                $location,
                $modal,
                $timeout,
                $filter,
                TCStorageService,
                orderByFilter,
                SessionStorageService,
                TEIService, 
                TEService,
                OptionSetService,
                EnrollmentService,
                ProgramFactory,
                DHIS2EventFactory,
                DashboardLayoutService,
                AttributesFactory,
                CurrentSelection) {
    //selections
    $scope.selectedTeiId = ($location.search()).tei; 
    $scope.selectedProgramId = ($location.search()).program; 
    $scope.selectedOrgUnit = SessionStorageService.get('SELECTED_OU');
    
    $scope.sortedTeiIds = CurrentSelection.getSortedTeiIds();    
    
    $scope.previousTeiExists = false;
    $scope.nextTeiExists = false;
    
    if($scope.sortedTeiIds && $scope.sortedTeiIds.length > 0){
        var current = $scope.sortedTeiIds.indexOf($scope.selectedTeiId);
        
        if(current !== -1){
            if($scope.sortedTeiIds.length-1 > current){
                $scope.nextTeiExists = true;
            }
            
            if(current > 0){
                $scope.previousTeiExists = true;
            }
        }
    }
    
    $scope.selectedProgram;    
    $scope.selectedTei;
    
    //get ouLevels
    TCStorageService.currentStore.open().done(function(){
        TCStorageService.currentStore.getAll('ouLevels').done(function(response){
            var ouLevels = angular.isObject(response) ? orderByFilter(response, '-level').reverse() : [];
            CurrentSelection.setOuLevels(orderByFilter(ouLevels, '-level').reverse());
        });
    });
    
    //dashboard items   
    var getDashboardLayout = function(){        
        $rootScope.dashboardWidgets = [];    
        $scope.widgetsChanged = [];
        $scope.dashboardStatus = [];
        $scope.dashboardWidgetsOrder = {biggerWidgets: [], smallerWidgets: []};
        $scope.orderChanged = false;        
        
        DashboardLayoutService.get().then(function(response){
            $scope.dashboardLayouts = response;
            
            var selectedLayout = $scope.dashboardLayouts ['DEFAULT'];            
            if($scope.selectedProgram && $scope.selectedProgram.id){
                selectedLayout = $scope.dashboardLayouts [$scope.selectedProgram.id] ? $scope.dashboardLayouts [$scope.selectedProgram.id] : selectedLayout;
            }

            angular.forEach(selectedLayout.widgets, function(widget){
                switch(widget.title){
                    case 'enrollment':
                        $rootScope.enrollmentWidget = widget;
                        $rootScope.dashboardWidgets.push($rootScope.enrollmentWidget);
                        $scope.dashboardStatus[widget.title] = angular.copy(widget);
                        break;
                    case 'dataentry':
                        $rootScope.dataentryWidget = widget;
                        $rootScope.dashboardWidgets.push($rootScope.dataentryWidget);
                        $scope.dashboardStatus[widget.title] = angular.copy(widget);
                        break;
                    case 'report':
                        $rootScope.reportWidget = widget;
                        $rootScope.dashboardWidgets.push($rootScope.reportWidget);
                        $scope.dashboardStatus[widget.title] = angular.copy(widget);
                        break;
                    case 'current_selections':
                        $rootScope.selectedWidget = widget;
                        $rootScope.dashboardWidgets.push($rootScope.selectedWidget);
                        $scope.dashboardStatus[widget.title] = angular.copy(widget);
                        break;
                    case 'profile':
                        $rootScope.profileWidget = widget;
                        $rootScope.dashboardWidgets.push($rootScope.profileWidget);
                        $scope.dashboardStatus[widget.title] = angular.copy(widget);
                        break;
                    case 'relationships':
                        $rootScope.relationshipWidget = widget;
                        $rootScope.dashboardWidgets.push($rootScope.relationshipWidget);
                        $scope.dashboardStatus[widget.title] = angular.copy(widget);
                        break;    
                    case 'notes':
                        $rootScope.notesWidget = widget;
                        $rootScope.dashboardWidgets.push($rootScope.notesWidget);
                        $scope.dashboardStatus[widget.title] = angular.copy(widget);
                        break;    
                }
            });

            $scope.hasBigger = false;
            angular.forEach(orderByFilter($filter('filter')($scope.dashboardWidgets, {parent: "biggerWidget"}), 'order'), function(w){
                if(w.show){
                    $scope.hasBigger = true;
                }
                $scope.dashboardWidgetsOrder.biggerWidgets.push(w.title);
            });

            $scope.hasSmaller = false;
            angular.forEach(orderByFilter($filter('filter')($scope.dashboardWidgets, {parent: "smallerWidget"}), 'order'), function(w){
                if(w.show){
                    $scope.hasSmaller = true;
                }
                $scope.dashboardWidgetsOrder.smallerWidgets.push(w.title);
            });
            
            setWidgetsSize();
            $scope.broadCastSelections();            
        });        
    };    
    
    var setWidgetsSize = function(){        
        
        $scope.widgetSize = {smaller: "col-sm-6 col-md-4", bigger: "col-sm-6 col-md-8"};
        
        if(!$scope.hasSmaller){
            $scope.widgetSize = {smaller: "col-sm-1", bigger: "col-sm-11"};
        }

        if(!$scope.hasBigger){
            $scope.widgetSize = {smaller: "col-sm-11", bigger: "col-sm-1"};
        }
    };
    
    if($scope.selectedTeiId){
        
        //get option sets
        $scope.optionSets = [];
        OptionSetService.getAll().then(function(optionSets){            
            angular.forEach(optionSets, function(optionSet){
                $scope.optionSets[optionSet.id] = optionSet;
            });
            
            AttributesFactory.getAll().then(function(atts){
                
                $scope.attributesById = [];
                angular.forEach(atts, function(att){
                    $scope.attributesById[att.id] = att;
                });

                CurrentSelection.setAttributesById($scope.attributesById);
            
                //Fetch the selected entity
                TEIService.get($scope.selectedTeiId, $scope.optionSets, $scope.attributesById).then(function(response){
                    $scope.selectedTei = response;

                    //get the entity type
                    TEService.get($scope.selectedTei.trackedEntity).then(function(te){                    
                        $scope.trackedEntity = te;

                        //get enrollments for the selected tei
                        EnrollmentService.getByEntity($scope.selectedTeiId).then(function(response){                    
                            var enrollments = angular.isObject(response) && response.enrollments ? response.enrollments : [];
                            var selectedEnrollment = null;
                            if(enrollments.length === 1 && enrollments[0].status === 'ACTIVE'){
                                selectedEnrollment = enrollments[0];
                            }

                            ProgramFactory.getAll().then(function(programs){
                                $scope.programs = [];

                                $scope.programNames = [];  
                                $scope.programStageNames = [];        

                                //get programs valid for the selected ou and tei
                                angular.forEach(programs, function(program){                                    
                                    if( program.trackedEntity.id === $scope.selectedTei.trackedEntity ){
                                        $scope.programs.push(program);
                                        $scope.programNames[program.id] = {id: program.id, name: program.name};
										angular.forEach(program.programStages, function(stage){                
											$scope.programStageNames[stage.id] = {id: stage.id, name: stage.name};
										});

                                        if($scope.selectedProgramId && program.id === $scope.selectedProgramId || selectedEnrollment && selectedEnrollment.program === program.id){
                                            $scope.selectedProgram = program;
                                        }
                                    }                                
                                });
                                
                                DHIS2EventFactory.getEventsByProgram($scope.selectedTeiId, null).then(function(events){                                        
                                    //prepare selected items for broadcast
                                    CurrentSelection.setSelectedTeiEvents(events);                                        
                                    CurrentSelection.set({tei: $scope.selectedTei, te: $scope.trackedEntity, prs: $scope.programs, pr: $scope.selectedProgram, prNames: $scope.programNames, prStNames: $scope.programStageNames, enrollments: enrollments, selectedEnrollment: selectedEnrollment, optionSets: $scope.optionSets});                            
                                    getDashboardLayout(); 
                                });                    
                            });
                        });
                    });            
                });  
            });
        });
    }    
    
    //listen for any change to program selection
    //it is possible that such could happen during enrollment.
    $scope.$on('mainDashboard', function(event, args) {
        var selections = CurrentSelection.get();
        $scope.selectedProgram = null;
        angular.forEach($scope.programs, function(pr){
            if(pr.id === selections.pr){
                $scope.selectedProgram = pr;
            }
        });
        
        $scope.applySelectedProgram();
    }); 
    
    //watch for widget sorting    
    $scope.$watch('widgetsOrder', function() {        
        if(angular.isObject($scope.widgetsOrder)){
            $scope.orderChanged = false;
            for(var i=0; i<$scope.widgetsOrder.smallerWidgets.length; i++){
                if($scope.widgetsOrder.smallerWidgets.length === $scope.dashboardWidgetsOrder.smallerWidgets.length && $scope.widgetsOrder.smallerWidgets[i] !== $scope.dashboardWidgetsOrder.smallerWidgets[i]){
                    $scope.orderChanged = true;
                }
                
                if($scope.widgetsOrder.smallerWidgets.length !== $scope.dashboardWidgetsOrder.smallerWidgets.length){
                    $scope.orderChanged = true;
                }
            }
            
            for(var i=0; i<$scope.widgetsOrder.biggerWidgets.length; i++){
                if($scope.widgetsOrder.biggerWidgets.length === $scope.dashboardWidgetsOrder.biggerWidgets.length && $scope.widgetsOrder.biggerWidgets[i] !== $scope.dashboardWidgetsOrder.biggerWidgets[i]){
                    $scope.orderChanged = true;
                }
                
                if($scope.widgetsOrder.biggerWidgets.length !== $scope.dashboardWidgetsOrder.biggerWidgets.length){
                    $scope.orderChanged = true;
                }
            }
            
            if($scope.orderChanged){
                saveDashboardLayout();
            }
        }
    });
    
    $scope.applySelectedProgram = function(){
        getDashboardLayout();
    };
    
    $scope.broadCastSelections = function(){
        
        var selections = CurrentSelection.get();
        $scope.selectedTei = selections.tei;
        $scope.trackedEntity = selections.te;
        $scope.optionSets = selections.optionSets;
        
        CurrentSelection.set({tei: $scope.selectedTei, te: $scope.trackedEntity, prs: $scope.programs, pr: $scope.selectedProgram, prNames: $scope.programNames, prStNames: $scope.programStageNames, enrollments: selections.enrollments, selectedEnrollment: null, optionSets: $scope.optionSets});        
        $timeout(function() { 
            $rootScope.$broadcast('selectedItems', {programExists: $scope.programs.length > 0});            
        }, 100);
    };     
    
    $scope.back = function(){
        $location.path('/').search({program: $scope.selectedProgramId});                   
    };
    
    $scope.displayEnrollment = false;
    $scope.showEnrollment = function(){
        $scope.displayEnrollment = true;
    };
    
    $scope.removeWidget = function(widget){        
        widget.show = false;
        saveDashboardLayout();
    };
    
    $scope.expandCollapse = function(widget){
        widget.expand = !widget.expand;
        saveDashboardLayout();;
    };
    
    var saveDashboardLayout = function(){
        var widgets = [];
        $scope.hasBigger = false;
        $scope.hasSmaller = false;
        angular.forEach($rootScope.dashboardWidgets, function(widget){
            var w = angular.copy(widget);            
            if($scope.orderChanged){
                if($scope.widgetsOrder.biggerWidgets.indexOf(w.title) !== -1){
                    $scope.hasBigger = $scope.hasBigger || w.show;
                    w.parent = 'biggerWidget';
                    w.order = $scope.widgetsOrder.biggerWidgets.indexOf(w.title);
                }
                
                if($scope.widgetsOrder.smallerWidgets.indexOf(w.title) !== -1){
                    $scope.hasSmaller = $scope.hasSmaller || w.show;
                    w.parent = 'smallerWidget';
                    w.order = $scope.widgetsOrder.smallerWidgets.indexOf(w.title);
                }
            }
            widgets.push(w);
        });

        if($scope.selectedProgram && $scope.selectedProgram.id){
            $scope.dashboardLayouts[$scope.selectedProgram.id] = {widgets: widgets, program: $scope.selectedProgram.id};
        }
        
        DashboardLayoutService.saveLayout($scope.dashboardLayouts).then(function(){
            if(!$scope.orderChanged){
                $scope.hasSmaller = $filter('filter')($scope.dashboardWidgets, {parent: "smallerWidget", show: true}).length > 0;
                $scope.hasBigger = $filter('filter')($scope.dashboardWidgets, {parent: "biggerWidget", show: true}).length > 0;                                
            }                
            setWidgetsSize();      
        });
    };
    
    $scope.showHideWidgets = function(){
        var modalInstance = $modal.open({
            templateUrl: "components/dashboard/dashboard-widgets.html",
            controller: "DashboardWidgetsController"
        });

        modalInstance.result.then(function () {
        });
    };
    
    $rootScope.closeOpenWidget = function(widget){
        saveDashboardLayout();
    };
    
    $scope.fetchTei = function(mode){
        var current = $scope.sortedTeiIds.indexOf($scope.selectedTeiId);
        var pr = ($location.search()).program;
        var tei = null;
        if(mode === 'NEXT'){            
            tei = $scope.sortedTeiIds[current+1];
        }
        else{            
            tei = $scope.sortedTeiIds[current-1];
        }        
        $location.path('/dashboard').search({tei: tei, program: pr ? pr: null});
    };
});
