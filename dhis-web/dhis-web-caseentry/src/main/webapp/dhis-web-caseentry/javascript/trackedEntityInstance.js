
function organisationUnitSelected(orgUnits, orgUnitNames) {
	showById('selectDiv');
	showById('searchDiv');
	showById("programLoader");
	disable('program');
	showById('mainLinkLbl');
	hideById('listEntityInstanceDiv');
	hideById('editEntityInstanceDiv');
	hideById('enrollmentDiv');
	hideById('listRelationshipDiv');
	hideById('addRelationshipDiv');
	hideById('migrationEntityInstanceDiv');
	hideById('entityInstanceDashboard');
	enable('listEntityInstanceBtn');
	enable('addEntityInstanceBtn');
	enable('advancedSearchBtn');
	enable('searchObjectId');
	setInnerHTML('entityInstanceDashboard', '');
	setInnerHTML('editEntityInstanceDiv', '');
	setFieldValue("orgunitName", orgUnitNames[0]);
	setInnerHTML("orgunitForSearch", orgUnitNames[0]);
	setFieldValue("orgunitId", orgUnits[0]);
	clearListById('program');
	jQuery.get("getAllPrograms.action", {}, function(json) {
		jQuery('#program').append(
				'<option value="">' + i18n_view_all + '</option>');
		for (i in json.programs) {
			if (json.programs[i].type == 1 || json.programs[i].type == 2) {
				jQuery('#program').append(
					'<option value="' + json.programs[i].uid + '" type="'
						+ json.programs[i].type + '" >' + json.programs[i].name + '</option>');
			}
		}
		enableBtn();
		hideById('programLoader');
		enable('program');
	});
}
selection.setListenerFunction(organisationUnitSelected);

// -----------------------------------------------------------------------------
// List && Search entityInstances
// -----------------------------------------------------------------------------

function TrackedEntityInstance() {

	var entityInstanceId;
	
	this.advancedSearch = function(params, page) {
		$.ajax({
			url : '../api/trackedEntityInstances.json',
			type : "GET",
			data : params,
			success : function(json) {
				setInnerHTML('listEntityInstanceDiv', displayTEIList(json, page));
				showById('listEntityInstanceDiv');
				jQuery('#loaderDiv').hide();
				setTableStyles();
			}
		});
	};
	
	this.validate = function(programId) {
		setMessage('');
		if (jQuery('.underAge').prop('checked') == 'true') {
			if (getFieldValue('representativeId') == '') {
				setMessage(i18n_please_choose_representative_for_this_under_age_tracked_entity_instance);
				$("#entityInstanceForm :input").attr("disabled", false);
				$("#entityInstanceForm").find("select").attr("disabled", false);
				return false;
			}
			if (getFieldValue('relationshipTypeId') == '') {
				setMessage(i18n_please_choose_relationshipType_for_this_under_age_tracked_entity_instance);
				$("#entityInstanceForm :input").attr("disabled", false);
				$("#entityInstanceForm").find("select").attr("disabled", false);
				return false;
			}
		}
		var params = "";
		if (programId !== "undefined") {
			params = "programId=" + programId + "&"
		}
		params += getParamsForDiv('entityInstanceForm');
		$("#entityInstanceForm :input").attr("disabled", true);
		$("#entityInstanceForm").find("select").attr("disabled", true);
		var json = null;
		$.ajax({
			type : "POST",
			url : 'validateTrackedEntityInstance.action',
			data : params,
			datatype : "json",
			async : false,
			success : function(data) {
				json = data;
			}
		});
		var response = json.response;
		var message = json.message;
		if (response == 'success') {
			if (message == 0) {
				return true;
			} else {
				if (message != "") {
					setMessage(message);
				} 
				$("#entityInstanceForm :input").attr("disabled", false);
				$("#entityInstanceForm").find("select").attr("disabled", false);
				return false;
			}
		} else {
			if (response == 'error') {
				setMessage(i18n_adding_tracked_entity_instance_failed + ':'
						+ '\n' + message);
			} else if (response == 'input') {
				setMessage(message);
			} else if (response == 'duplicate') {
				showListTrackedEntityInstanceDuplicate(data, false);
			}
			$("#entityInstanceForm :input").attr("disabled", false);
			$("#entityInstanceForm").find("select").attr("disabled", false);
			return false;
		}
	};
	
	this.add = function(programId, related, params, isContinue) {
		if (!this.validate(programId))
			return;
		if (programId != '') {
			params += '&programId=' + programId;
		}
		$.ajax({
			type : "POST",
			url : 'addTrackedEntityInstance.action',
			data : params,
			success : function(json) {
				if (json.response == 'success') {
					var entityInstanceUid = json.message.split('_')[0];
					var entityInstanceId = json.message.split('_')[1];
					var dateOfIncident = jQuery('#entityInstanceForm [id=dateOfIncident]').val();
					var enrollmentDate = jQuery('#entityInstanceForm [id=enrollmentDate]').val();
					
					// Enroll entityInstance into the program
					if (programId && enrollmentDate != undefined) {
						jQuery.postJSON( "saveProgramEnrollment.action",
						{
							entityInstanceId : entityInstanceId,
							programId : programId,
							dateOfIncident : dateOfIncident,
							enrollmentDate : enrollmentDate
						},
						function(json) {
							if (isContinue) {
								jQuery("#entityInstanceForm :input").each(function() {
										var type = $(this).attr('type'), id = $(this).attr('id');
										if (type != 'button' && type != 'submit' && id != 'enrollmentDate') {
											$(this).val("");
										}
									});
								$("#entityInstanceForm :input").prop("disabled",false);
								$("#entityInstanceForm").find("select").prop("disabled",false);
							} else {
								showTrackedEntityInstanceDashboardForm(entityInstanceUid);
							}
						});
					} else if (isContinue) {
						jQuery("#entityInstanceForm :input")
								.each(
										function() {
											var type = $(this).attr(
													'type'), id = $(
													this).attr('id');
											if (type != 'button'
													&& type != 'submit'
													&& id != 'enrollmentDate') {
												$(this).val("");
											}
										});
						$("#entityInstanceForm :input").prop(
								"disabled", false);
						$("#entityInstanceForm").find("select").prop(
								"disabled", false);
					} else {
						$("#entityInstanceForm :input").attr(
								"disabled", false);
						$("#entityInstanceForm").find("select").attr(
								"disabled", false);
						showTrackedEntityInstanceDashboardForm(entityInstanceUid);
					}
				}
			}
		});
	};
	
	this.update = function() {
		if (!this.validate(getFieldValue('program')))
			return;
		var params = 'programId=' + getFieldValue('program')
				+ '&' + getParamsForDiv('editEntityInstanceDiv');
		$.ajax({
			type : "POST",
			url : 'updateTrackedEntityInstance.action',
			data : params,
			success : function(json) {
				showTrackedEntityInstanceDashboardForm(getFieldValue('uid'));
				$("#entityInstanceForm :input").attr("disabled", false);
				$("#entityInstanceForm").find("select").attr("disabled", false);
			}
		});
	};
	
	this.remove = function(confirm_delete_tracked_entity_instance) {
		removeItem(this.entityInstanceId, "",
				confirm_delete_tracked_entity_instance,
				'removeTrackedEntityInstance.action');
	};
}

TrackedEntityInstance.listAll = function(page) {
	jQuery('#loaderDiv').show();
	contentDiv = 'listEntityInstanceDiv';
	var params = "page=" + page;
	if (getFieldValue('program') != '') {
		params += "&program=" + getFieldValue('program');
	}
	
	$('#attributeIds option').each(function(i, item){
		params += "&attribute=" + item.value;
	});
	
	$.ajax({
		type : "GET",
		url : "../api/trackedEntityInstances.json?ou="
				+ getFieldValue("orgunitId"),
		data : params,
		dataType : "json",
		success : function(json) {
			setInnerHTML('listEntityInstanceDiv', displayTEIList(json, page));
			showById('listEntityInstanceDiv');
			jQuery('#loaderDiv').hide();
			setTableStyles();
		}
	});
}

function listAllTrackedEntityInstance(page) {
	jQuery('#loaderDiv').show();
	hideById('listEntityInstanceDiv');
	hideById('editEntityInstanceDiv');
	hideById('migrationEntityInstanceDiv');
	hideById('advanced-search');
	showById('searchByIdTR');
	TrackedEntityInstance.listAll(page);
}

function displayTEIList(json, page) {

	// Header
	var title = "";
	
	if (getFieldValue('program') != "") {
		var status = jQuery('#statusEnrollment option:selected').text();
		var programName = jQuery('#program option:selected')
				.text();
		title = i18n_for + " " + status + " " + i18n_enrollments_in + " "
				+ programName + " " + i18n_program;
		setInnerHTML('enrollmentInfor', title);
	}
	var table = "<p>" + i18n_the_following_tracked_entity_instances_found_in
			+ " " + getFieldValue('orgunitName') + " " + title + "</p>";
	if (json.metaData.pager.total > 0) {
		table += "<p>" + i18n_total_result + " : " + json.metaData.pager.total
				+ "</p>";
	} else {
		table += "<p>" + i18n_no_result_found + "</p>";
	}
	
	// TEI list
	table += "<table class='listTable' width='100%'>";
	
	var idx = 4;
	if(getFieldValue('program') != '') {
		idx = 5;
	}
	
	// Yes/No and Yes Only attributes in result
	
	var attList = new Array();
	var attDate = new Array();
	$('#attributeIds option').each(function(i, item) {
		var valueType = $(item).attr('valueType');
		var value = $(item).val();
		if ( valueType == 'bool' || valueType == 'trueOnly' || valueType == 'trackerAssociate' ) {
			for (var i = idx; i < json.width; i++) {
				if( value==json.headers[i].name ){
					attList.push(i);
				}
				else if( valueType=='date'){
					attDate.push(i);
				}
			}
		}
		else if ( valueType == 'date' ) {
			for (var i = idx; i < json.width; i++) {
				if( value==json.headers[i].name ){
					attDate.push(i);
				}
			}
		}
	});

	// TEI List
	
	table += "<col width='30' />";
	for (var i = idx; i < json.width; i++) {
		table += "<col />";
	}
	table += "<col width='200' />";
	table += "<thead><tr><th>#</th>";
	for (var i = idx; i < json.width; i++) {
		table += "<th>" + json.headers[i].column + "</th>";
	}
	table += "<th>" + i18n_operations + "</th>";
	table += "</tr></thead>";
	
	table += "<tbody id='list'>";
	for ( var i in json.rows) {
		var cols = json.rows[i];
		var uid = cols[0];
		var no = eval(json.metaData.pager.page);
		no = (no - 1) * 50 + eval(i) + 1;
		table += "<tr id='tr" + uid + "'>";
		table += "<td>" + no + "</td>";
		for (var j = idx; j < json.width; j++) {
			var colVal = cols[j];
			if (j == 4) {
				colVal = json.metaData.names[colVal];
			}
			
			if( jQuery.inArray( j, attList )>=0 && colVal!="" ){
				colVal = (colVal=='true')? i18n_yes : i18n_no;
			}
			else if( jQuery.inArray( j, attDate )>=0 && colVal!="" ){
				colVal = colVal.split(' ')[0];
			}
			
			table += "<td onclick=\"javascript:isDashboard=true;showTrackedEntityInstanceDashboardForm( '"
				+ uid
				+ "' )\" title='"
				+ i18n_dashboard
				+ "'>" + colVal + "</td>";
		}
		
		// Operations column
		table += "<td>";
		table += "<a href=\"javascript:isDashboard=true;showTrackedEntityInstanceDashboardForm( '"
				+ uid
				+ "' )\" title='"
				+ i18n_dashboard
				+ "'><img src='../images/enroll.png' alt='"
				+ i18n_dashboard
				+ "'></a>";
		table += "<a href=\"javascript:isDashboard=false;statusSearching=3;showUpdateTrackedEntityInstanceForm( '"
				+ uid
				+ "' )\" title='"
				+ i18n_edit_profile
				+ "'><img src= '../images/edit.png' alt='"
				+ i18n_edit_profile
				+ "'></a>";
		table += "<a href=\"javascript:setFieldValue( 'isShowEntityInstanceList', 'false' ); showRelationshipList('"
				+ uid
				+ "' )\" title='"
				+ i18n_manage_relationship
				+ "'><img src='../images/relationship.png' alt='"
				+ i18n_manage_relationship + "'></a>";
		if (canChangeLocation) {
			table += "<a href=\"javascript:isDashboard=false;getTrackedEntityInstanceLocation( '"
					+ uid
					+ "' );\" title='"
					+ i18n_change_location
					+ "'><img src='../icons/dataentry.png' alt='"
					+ i18n_change_location
					+ "' style='width:25px; height:25px'></a>";
		}
		table += "<a href=\"javascript:removeTrackedEntityInstance( '" + uid
				+ "', '', '" + i18n_confirm_delete_tracked_entity_instance
				+ "' )\" title='" + i18n_remove
				+ "'><img src='../images/delete.png' alt='" + i18n_remove
				+ "'></a>";
		table += "<a href=\"javascript:showTrackedEntityInstanceHistory( '"
				+ uid + "')\" title='"
				+ i18n_tracked_entity_instance_details_and_history
				+ "'><img src='../images/information.png' alt='"
				+ i18n_tracked_entity_instance_details_and_history + "'></a>";
		table += "</td>";
		table += "</tr>";
	}
	table += "</tbody>";
	table += "</table>";
	
	return table + paging(json, page);
}

// Paging

function paging(json, page) {
	var searchMethod = "listAllTrackedEntityInstance";
	if( isAdvancedSearch ){
		searchMethod = "validateAdvancedSearch";
	}
	
	var table = "<table width='100%' style='background-color: #ebf0f6;'><tr><td colspan='"
			+ json.width + "'>";
	table += "<div class='paging'>";
	table += "<span class='first' title='" + i18n_first + "'>««</span>";
	table += "<span class='prev' title='" + i18n_prev + "'>«</span>";
	for (var i = 1; i <= json.metaData.pager.pageCount; i++) {
		if (i == page) {
			table += "<span class='page' title='" + i18n_page + " " + i + "'>"
					+ i + "</span>";
		} else {
			table += "<a class='page' title='" + i18n_page + " " + i
					+ "' href='javascript:" + searchMethod + "( " + i
					+ ");'>" + i + "</a>";
		}
		table += "<span class='seperator'>|</span>";
	}
	table += "<span class='next' title='" + i18n_next + "'>» </span>";
	table += "<span class='last' title='" + i18n_last + "'>»»</span>";
	table += "</div>";
	table += "</tr></table>";
	return table;
}

function advancedSearch(params, page) {
	var entityInstance = new TrackedEntityInstance();
	entityInstance.advancedSearch(params, page);
}

// -----------------------------------------------------------------------------
// Remove entityInstance
// -----------------------------------------------------------------------------

function removeTrackedEntityInstance(entityInstanceId) {
	var entityInstance = new TrackedEntityInstance();
	entityInstance.entityInstanceId = entityInstanceId;
	entityInstance.remove(i18n_confirm_delete_tracked_entity_instance);
}

// -----------------------------------------------------------------------------
// Add TrackedEntityInstance
// -----------------------------------------------------------------------------

function addTrackedEntityInstance(programId, related, isContinue) {
	var entityInstance = new TrackedEntityInstance();
	entityInstance.add(programId, related, getParamsForDiv('entityInstanceForm'), isContinue);
	registrationProgress = true;
	return false;
}

function updateTrackedEntityInstance() {
	var entityInstance = new TrackedEntityInstance();
	var params = getParamsForDiv('entityInstanceForm');
	entityInstance.update();
	return false;
}

function showAddTrackedEntityInstanceForm(entityInstanceId, programId,
		relatedProgramId, related) {
	hideById('listEntityInstanceDiv');
	hideById('selectDiv');
	hideById('searchDiv');
	hideById('migrationEntityInstanceDiv');
	hideById('listRelationshipDiv');
	setInnerHTML('addRelationshipDiv', '');
	setInnerHTML('entityInstanceDashboard', '');
	jQuery('#loaderDiv').show();
	jQuery('#editEntityInstanceDiv').load(
			'showAddTrackedEntityInstanceForm.action', {
				programId : programId,
				entityInstanceId : entityInstanceId,
				relatedProgramId : relatedProgramId,
				related : related
			}, function() {
				showById('editEntityInstanceDiv');
				if (related) {
					jQuery('[name=addRelationShipLink]').show();
					hideById('entityInstanceMamagementLink');
					setFieldValue('relationshipId', entityInstanceId);
				} else {
					jQuery('[name=addRelationShipLink]').hide();
					showById('entityInstanceMamagementLink');
				}
				jQuery('#loaderDiv').hide();
			});
}

// ----------------------------------------------------------------
// Click Back to main form
// ----------------------------------------------------------------

function onClickBackBtn() {
	showById('mainLinkLbl');
	showById('selectDiv');
	showById('searchDiv');
	showById('listEntityInstanceDiv');
	hideById('editEntityInstanceDiv');
	hideById('enrollmentDiv');
	hideById('listRelationshipDiv');
	hideById('addRelationshipDiv');
	hideById('migrationEntityInstanceDiv');
	setInnerHTML('entityInstanceDashboard', '');
	loadTrackedEntityInstanceList();
}

function loadTrackedEntityInstanceList() {
	hideById('editEntityInstanceDiv');
	hideById('enrollmentDiv');
	hideById('listRelationshipDiv');
	hideById('addRelationshipDiv');
	hideById('dataRecordingSelectDiv');
	hideById('dataEntryFormDiv');
	hideById('migrationEntityInstanceDiv');
	setInnerHTML('entityInstanceDashboard', '');
	setInnerHTML('editEntityInstanceDiv', '');
	showById('mainLinkLbl');
	showById('selectDiv');
	showById('searchDiv');
	if (statusSearching == 2) {
		return;
	} else if (statusSearching == 0) {
		TrackedEntityInstance.listAll();
	} else if (statusSearching == 1) {
		validateAdvancedSearch();
	} else if (statusSearching == 3) {
		showById('listEntityInstanceDiv');
	}
}

// ------------------------------------------------------------------------------
// Load data entry form
// ------------------------------------------------------------------------------

function loadDataEntry(programStageInstanceId) {
	setInnerHTML('dataEntryFormDiv', '');
	showById('dataEntryFormDiv');
	showById('executionDateTB');
	setFieldValue('dueDate', '');
	setFieldValue('executionDate', '');
	disable('validationBtn');
	disableCompletedButton(true);
	disable('uncompleteBtn');
	setFieldValue('programStageInstanceId', programStageInstanceId);
	$('#executionDate').unbind("change");
	$('#executionDate').change(
			function() {
				saveExecutionDate(getFieldValue('programId'),
						programStageInstanceId, byId('executionDate'));
			});
	jQuery(".stage-object-selected").removeClass('stage-object-selected');
	var selectedProgramStageInstance = jQuery('#' + prefixId
			+ programStageInstanceId);
	selectedProgramStageInstance.addClass('stage-object-selected');
	setFieldValue('programStageId', selectedProgramStageInstance.attr('psid'));
	showLoader();
	$('#dataEntryFormDiv').load(
			"dataentryform.action",
			{
				programStageInstanceId : programStageInstanceId
			},
			function() {
				var editDataEntryForm = getFieldValue('editDataEntryForm');
				if (editDataEntryForm == 'true') {
					var executionDate = jQuery('#executionDate').val();
					var completed = jQuery(
							'#entryFormContainer input[id=completed]').val();
					var irregular = jQuery(
							'#entryFormContainer input[id=irregular]').val();
					var reportDateDes = jQuery("#ps_" + programStageInstanceId)
							.attr("reportDateDes");
					setInnerHTML('reportDateDescriptionField', reportDateDes);
					enable('validationBtn');
					if (executionDate == '') {
						disable('validationBtn');
					} else if (executionDate != '') {
						disableCompletedButton(completed);
					}
					$(window).scrollTop(200);
				} else {
					blockEntryForm();
					disableCompletedButton(completed);
					disable('executionDate');
					hideById('inputCriteriaDiv');
				}
				resize();
				hideLoader();
				hideById('contentDiv');
				if (registrationProgress) {
					var reportDateToUse = selectedProgramStageInstance
							.attr('reportDateToUse');
					if (reportDateToUse != "undefined" && reportDateToUse != ''
							&& $('#executionDate').val() == '') {
						$('#executionDate').val(reportDateToUse);
						$('#executionDate').change();
					}
				}
				registrationProgress = false;
			});
}

function searchByIdsOnclick()
{
	if( getFieldValue('searchTeiByAttributes')==''){
		return;
	}
	var params = "ou=" + getFieldValue("orgunitId");
	params += "&page=1";
	if (getFieldValue('program') != '') {
		params += "&program=" + getFieldValue('program');
		if( getFieldValue('programStatus')!=""){
			params += "&programStatus=" + getFieldValue('programStatus');
		}
	}
	
	params += "&query=LIKE:" + getFieldValue('searchTeiByAttributes');
	
	$('#attributeIds option').each(function(i, item) {
		params += "&attribute=" + item.value;
	}); 
	
	$.ajax({
		url : '../api/trackedEntityInstances.json',
		type : "GET",
		data : params,
		success : function(json) {
			setInnerHTML('listEntityInstanceDiv', displayTEIList(json, 1));
			showById('listEntityInstanceDiv');
			jQuery('#loaderDiv').hide();
			setTableStyles();
		}
	});
}
