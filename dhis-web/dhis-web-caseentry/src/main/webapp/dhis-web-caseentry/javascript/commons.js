var prefixId = 'ps_';
var COLOR_RED = "#fb4754";
var COLOR_GREEN = "#8ffe8f";
var COLOR_YELLOW = "#f9f95a";
var COLOR_LIGHTRED = "#fb6bfb";
var COLOR_GREY = "#bbbbbb";
var COLOR_LIGHT_RED = "#ff7676";
var COLOR_LIGHT_YELLOW = "#ffff99";
var COLOR_LIGHT_GREEN = "#ccffcc";
var COLOR_LIGHT_LIGHTRED = "#ff99ff";
var COLOR_LIGHT_GREY = "#ddd";
var MARKED_VISIT_COLOR = '#AAAAAA';
var SUCCESS_COLOR = '#ccffcc';
var ERROR_COLOR = '#ccccff';
var SAVING_COLOR = '#ffffcc';
var SUCCESS = 'success';
var ERROR = 'error';
var isDashboard = false;

// Disable caching for ajax requests in general
$(document).ready(function() {
	$.ajaxSetup({
		cache : false
	});
});

// -----------------------------------------------------------------------------
// Advanced search
// -----------------------------------------------------------------------------

function addAttributeOption() {
	jQuery('#advancedSearchTB [name=clearSearchBtn]').attr('disabled', false);
	var rowId = 'advSearchBox'
			+ jQuery('#advancedSearchTB select[name=searchObjectId]').length
			+ 1;
	var content = '<td>' + getInnerHTML('searchingAttributeIdTD') + '</td>';
	content += '<td>' + searchTextBox;
	content += '&nbsp;<input type="button" name="clearSearchBtn" class="normal-button" value="'
			+ i18n_clear
			+ '" onclick="removeAttributeOption('
			+ "'"
			+ rowId
			+ "'" + ');"></td>';
	content = '<tr id="' + rowId + '">' + content + '</tr>';
	jQuery('#advancedSearchTB').append(content);
}

function removeAttributeOption(rowId) {
	$('#' + rowId).remove();
	if ($('#advancedSearchTB tr').length <= 1) {
		$('#advancedSearchTB [name=clearSearchBtn]').attr('disabled', true);
	}
}

// ------------------------------------------------------------------------------
// Search entityInstances by selected attribute
// ------------------------------------------------------------------------------

function searchObjectOnChange(this_) {
	var container = $(this_).parent().parent().attr('id');
	var attributeId = $('#' + container + ' [id=searchObjectId]').val();
	var element = $('#' + container + ' [name=searchText]');
	var valueType = $('#' + container + ' [id=searchObjectId] option:selected')
			.attr('valueType');
	$('#searchText_' + container).removeAttr('readonly', false);
	$('#dateOperator_' + container).remove();
	$('#searchText_' + container).val("");
	$('#searchText_' + container).datepicker("destroy");
	$('#' + container + ' [id=dateOperator]').replaceWith("");
	if (valueType == 'bool') {
		element.replaceWith(getTrueFalseBox());
	}
	else if ( attributeId=='programDate' || valueType == 'date')
	{
		element.replaceWith( getDateField(container) );
		datePickerValid( 'searchText_' + container );
	} 
	else {
		element.replaceWith(searchTextBox);
	}
	
}

function getDateField( container )
{
	var dateField = '<select id="dateOperator_' + container + '" name="dateOperator" style="width:40px"><option value="GT"> > </option><option value="GE"> >= </option><option value="EQ"> = </option><option value="LT"> < </option><option value="LE"> <= </option></select>';
	dateField += '<input type="text" id="searchText_' + container + '" name="searchText" style="width:160px;" onkeyup="searchPatientsOnKeyUp( event );">';
	return dateField;
}

function getTrueFalseBox() {
	var trueFalseBox = '<select id="searchText" name="searchText">';
	trueFalseBox += '<option value="true">' + i18n_yes + '</option>';
	trueFalseBox += '<option value="false">' + i18n_no + '</option>';
	trueFalseBox += '</select>';
	return trueFalseBox;
}

// -----------------------------------------------------------------------------
// Search TrackedEntityInstance
// -----------------------------------------------------------------------------

function searchTrackedEntityInstancesOnKeyUp(event) {
	var key = getKeyCode(event);
	if (key == 13)// Enter
	{
		validateAdvancedSearch( 1 );
	}
}

function getKeyCode(e) {
	if (window.event) {
		return window.event.keyCode;
	}
	return (e) ? e.which : null;
}

function validateAdvancedSearch( page ) {
	hideById('listEntityInstanceDiv');
	var flag = true;
	if (getFieldValue('searchByProgramStage') == "false"
			|| (getFieldValue('searchByProgramStage') == "true" && jQuery('#advancedSearchTB tr').length > 1)) {
		jQuery("#searchDiv :input").each(function(i, item) {
			var elementName = $(this).attr('name');
			if (elementName == 'searchText' && jQuery(item).val() == '') {
				showWarningMessage(i18n_specify_search_criteria);
				flag = false;
			}
		});
	}

	if (flag) {
		contentDiv = 'listEntityInstanceDiv';
		jQuery("#loaderDiv").show();
		advancedSearch(getSearchParams(page), page);
	}
}

var followup = false;
function getSearchParams(page) {
	var params = "ou=" + getFieldValue("orgunitId");
	params += "&page=" + page;
	if (getFieldValue('program') != '') {
		params += "&program=" + getFieldValue('program');
		if( getFieldValue('programStatus')!=""){
			params += "&programStatus=" + getFieldValue('programStatus');
		}
	}
	
	if(getFieldValue('programStatus') != ''){
		params += "&programStatus=" + getFieldValue('programStatus');
	}
	
	if( getFieldValue('startDate') != ''){
		params += "&eventStartDate=" + getFieldValue('startDate');
		params += "&eventEndDate=" + getFieldValue('endDate');
	}
	
	if( getFieldValue('status')!= '' ){
		params += "&status=" + getFieldValue('status');
	}
	
	if( $('#followup').attr('checked')=='checked'){
		params += "followUp=true";
	}
	
	var flag = false;
	$('#advancedSearchTB tr').each(
		function(i, row) {
			var isProgramDate = false;
			var dateOperator = "";
			var p = "";
			jQuery(this).find(':input').each(
				function(idx, item) {
					if (item.type != "button") {
						if (idx == 0) {
							if (item.value == 'programDate') {
								isProgramDate = true;
							} else {
								p += "&attribute=" + item.value;
							}
						} else if (item.name == 'dateOperator') {
							dateOperator = item.value;
						} else if (item.name == 'searchText') {
							if (item.value != '') {
								if( isProgramDate ){
									p += "&programDate=" + dateOperator + ":" + item.value;
								}
								else if (dateOperator.length > 0) {
									p += dateOperator + ":" + item.value.toLowerCase();
								} else {
									var key = item.value.toLowerCase()
											.replace(/^\s*/, "")
											.replace(/\s*$/, "");
									p += ":LIKE:" + key;
								}
							} else {
								p = "";
							}
						}
					}
				});
			params += p;
		});
	
	var p = params;
	$('#attributeIds option').each(function(i, item) {
		if ( p.indexOf(item.value) < 0 ) {
				params += "&attribute=" + item.value;
		}
	}); 
		
	if( getFieldValue('ouMode') != '' ){
		params += '&ouMode=' + getFieldValue('ouMode');
	}
	
	return params;
}

// ----------------------------------------------------------------
// Get Params form Div
// ----------------------------------------------------------------

var phoneNumberAreaCode = "";
function getParamsForDiv(entityInstanceDiv) {
	var params = '';
	var dateOperator = '';
	$("#" + entityInstanceDiv + " :input").each(function() {
		var item = jQuery(this);
		var elementId = item.attr('id');
		if (item.attr('type') == 'checkbox') {
			var checked = item.attr('checked') ? true : false;
			params += elementId + "=" + checked + "&";
		} else if (elementId == 'dateOperator') {
			dateOperator = item.val();
		} else if ($(this).attr('type') != 'button') {
			var value = "";
			if (item.val() != null && item.val() != '') {
				if( (item.attr('phoneNumber')==undefined) 
						|| ( item.attr('phoneNumber')!=undefined && item.val()!= phoneNumberAreaCode) ){
					value = htmlEncode(item.val());
				}
			}
			if (dateOperator != '') {
				value = dateOperator + "'" + value + "'";
				dateOperator = "";
			}
			params += elementId + "=" + value + "&";
		}
	});
	return params;
}

// -----------------------------------------------------------------------------
// View entityInstance details
// -----------------------------------------------------------------------------

function showTrackedEntityInstanceDetails(entityInstanceId) {
	$('#detailsInfo').load("getTrackedEntityInstanceDetails.action", {
		id : entityInstanceId
	}, function() {
	}).dialog({
		title : i18n_tracked_entity_instance_details,
		maximize : true,
		closable : true,
		modal : false,
		overlay : {
			background : '#000000',
			opacity : 0.1
		},
		width : 450,
		height : 300
	});
}

function showTrackedEntityInstanceHistory(entityInstanceId) {
	$('#detailsInfo').load("getTrackedEntityInstanceHistory.action", {
		entityInstanceId : entityInstanceId
	}, function() {
	}).dialog({
		title : i18n_tracked_entity_instance_details_and_history,
		maximize : true,
		closable : true,
		modal : false,
		overlay : {
			background : '#000000',
			opacity : 0.1
		},
		width : 800,
		height : 520
	});
}

function exportTrackedEntityInstanceHistory(entityInstanceId, type) {
	window.location.href = "getTrackedEntityInstanceHistory.action?entityInstanceId="
			+ entityInstanceId + "&type=" + type;
}

function setEventColorStatus(programStageInstanceId, status) {
	var boxElement = $('#ps_' + programStageInstanceId);
	var dueDateElementId = 'value_' + programStageInstanceId + '_date';
	var status = eval(status);
	switch (status) {
	case 1:
		boxElement.css('border-color', COLOR_GREEN);
		boxElement.css('background-color', COLOR_LIGHT_GREEN);
		$("#" + dueDateElementId).datepicker("destroy");
		disable(dueDateElementId);
		return;
	case 2:
		boxElement.css('border-color', COLOR_LIGHTRED);
		boxElement.css('background-color', COLOR_LIGHT_LIGHTRED);
		datePicker(dueDateElementId);
		enable(dueDateElementId);
		return;
	case 3:
		boxElement.css('border-color', COLOR_YELLOW);
		boxElement.css('background-color', COLOR_LIGHT_YELLOW);
		datePicker(dueDateElementId);
		enable(dueDateElementId);
		return;
	case 4:
		boxElement.css('border-color', COLOR_RED);
		boxElement.css('background-color', COLOR_LIGHT_RED);
		datePicker(dueDateElementId);
		enable(dueDateElementId);
		return;
	case 5:
		boxElement.css('border-color', COLOR_GREY);
		boxElement.css('background-color', COLOR_LIGHT_GREY);
		disable('ps_' + programStageInstanceId);
		$("#" + dueDateElementId).datepicker("destroy");
		disable(dueDateElementId);
		return;
	default:
		return;
	}
}

function enableBtn() {
	var program = getFieldValue('program');
	if (registration == undefined || !registration) {
		if (program != '') {
			enable('status');
			enable('listEntityInstanceBtn');
			enable('addEntityInstanceBtn');
			enable('advancedSearchBtn');
			enable('scheduledVisitDays');
		} else {
			disable('status');
			disable('listEntityInstanceBtn');
			disable('addEntityInstanceBtn');
			disable('advancedSearchBtn');
			disable('scheduledVisitDays');
		}
	} else if (program != '') {
		showById('enrollmentSelectTR');
	} else {
		hideById('enrollmentSelectTR');
	}
	
	if( program!=""){
		$.get("../api/programs/" + program + ".json?fields=programTrackedEntityAttributes", {}
			, function(json) {
				removeAttributeOption('advSearchBox0');
				var attributeList = jQuery('#searchObjectId');
				jQuery('input[name=clearSearchBtn]').each(function() {
					jQuery(this).click();
				});

				clearListById('searchObjectId');
				clearListById('attributeIds');
				for ( var i in json.programTrackedEntityAttributes) {
					var progamAttribute = json.programTrackedEntityAttributes[i];
					var attribute = progamAttribute.trackedEntityAttribute;
					jQuery('#searchObjectId').append(
						'<option value="' + attribute.id + '" >'
							+ attribute.name + '</option>');
					
					if(json.programTrackedEntityAttributes[i].displayed=='true'){
						jQuery('#attributeIds').append(
							'<option value="' + attribute.id 
							+ '" valueType="' + attribute.valueType  + '"></option>');
					}
				}
				
				if (getFieldValue('program') != '') {
					jQuery('#searchObjectId').append(
						'<option value="programDate" >' + i18n_enrollment_date
							+ '</option>');
				}

				addAttributeOption();
		});
	}
	else
	{
		$.get("../api/trackedEntityAttributes.json?fields=id,name&filter=displayInListNoProgram:eq:true", {}
			, function(json) {
				removeAttributeOption('advSearchBox0');
				var attributeList = jQuery('#searchObjectId');
				jQuery('input[name=clearSearchBtn]').each(function() {
					jQuery(this).click();
				});

				clearListById('searchObjectId');
				clearListById('attributeIds');
				for ( var i in json.trackedEntityAttributes) {
					var attribute = json.trackedEntityAttributes[i];
					jQuery('#searchObjectId').append(
						'<option value="' + attribute.id + '" >' + attribute.name + '</option>');
					
					jQuery('#attributeIds').append(
						'<option value="' + attribute.id 
						+ '" valueType="' + attribute.valueType  + '"></option>');
				}
				
				if (getFieldValue('program') != '') {
					jQuery('#searchObjectId').append(
						'<option value="programDate" >' + i18n_enrollment_date
							+ '</option>');
				}

				addAttributeOption();
		});
	}
	
}

function enableRadioButton(programId) {
	var prorgamStageId = $('#programStageAddEntityInstance').val();
	if (prorgamStageId == '') {
		$('#programStageAddEntityInstanceTR [name=statusEvent]').attr(
				"disabled", true);
	} else {
		$('#programStageAddEntityInstanceTR [name=statusEvent]').removeAttr(
				"disabled");
	}
}

function showColorHelp() {
	$('#colorHelpDiv').dialog({
		title : i18n_color_quick_help,
		maximize : true,
		closable : true,
		modal : false,
		width : 380,
		height : 290
	}).show('fast');
}

// ----------------------------------------------------------------------------
// Create New Event
// ----------------------------------------------------------------------------

function showCreateNewEvent(programInstanceId, programStageId) {
	var flag = false;
	if (programStageId != undefined) {
		$('#repeatableProgramStage_' + programInstanceId + " option ").each(
			function() {
				if ($(this).css("display") != 'none'
						&& programStageId == $(this).attr('prevStageId')) {
					$(this).attr("selected", "selected");
					setSuggestedDueDate(programInstanceId);
					flag = true;
				}
			});

		$('#repeatableProgramStage_' + programInstanceId + " option ").each(
			function() {
				if ($(this).css("display") != 'none'
						&& programStageId == $(this).val()) {
					$(this).attr("selected", "selected");
					setSuggestedDueDate(programInstanceId);
					flag = true;
				}
			});
		$('#repeatableProgramStage_' + programInstanceId).attr('disabled', true);
	} else {
		$('#repeatableProgramStage_' + programInstanceId).attr('disabled', false);
	}
	
	if (!flag) {
		$('#repeatableProgramStage_' + programInstanceId + " option ").each(
			function() {
				if ($(this).css("display") != 'none' && !flag) {
					$(this).attr("selected", "selected");
					setSuggestedDueDate(programInstanceId);
					flag = true;
				}
			});

		$('#repeatableProgramStage_' + programInstanceId).val("");
	}
	setInnerHTML('createEventMessage_' + programInstanceId, '');

	$('#createNewEncounterDiv_' + programInstanceId).dialog({
		title : i18n_create_new_event,
		maximize : true,
		closable : true,
		modal : true,
		overlay : {
			background : '#000000',
			opacity : 0.1
		},
		width : 450,
		height : 160
	}).show('fast');
}

function setSuggestedDueDate(programInstanceId) {
	var lastVisit = "";
	if ($('.stage-object-selected').length != 0) {
		var lastVisit = $('.stage-object-selected').attr('reportDate');
		$('#tb_' + programInstanceId + ' input').each(function() {
			var reportDate = $(this).attr('reportDate');
			if (reportDate > lastVisit) {
				lastVisit = reportDate;
			}
		});
	}

	if (lastVisit == '') {
		lastVisit = getCurrentDate();
	}

	var standardInterval = $(
			'#repeatableProgramStage_' + programInstanceId + ' option:selected')
			.attr('standardInterval');
	var date = $.datepicker.parseDate(dateFormat, lastVisit);
	var d = date.getDate() + eval(standardInterval);
	var m = date.getMonth();
	var y = date.getFullYear();
	var edate = new Date(y, m, d);
	var sdate = $.datepicker.formatDate(dateFormat, edate);
	$('#dueDateNewEncounter_' + programInstanceId).val(sdate);
}

// ------------------------------------------------------
// Register Irregular-encounter
// ------------------------------------------------------

function registerIrregularEncounter(programInstanceId, programStageId,
		programStageUid, programStageName, dueDate) {
	if (dueDate == '') {
		showById("spanDueDateNewEncounter_" + programInstanceId);
	} else {
		$.postJSON(
			"registerIrregularEncounter.action",
			{
				programInstanceId : programInstanceId,
				programStageId : programStageId,
				dueDate : dueDate
			},
			function(json) {
				var programStageInstanceId = json.message;
				disableCompletedButton(false);
				var elementId = prefixId + programStageInstanceId;
				var flag = false;
				var programType = $('.stage-object-selected').attr(
						'type');
				var selectedStage = $('#repeatableProgramStage_'
						+ programInstanceId + ' option:selected');
				var elementBox = '<td>'
						+ '<div class="orgunit-object" id="org_'
						+ programStageInstanceId
						+ '">&nbsp;</div>'
						+ '<input name="programStageBtn" '
						+ 'pi="'
						+ programInstanceId
						+ '" '
						+ 'id="'
						+ elementId
						+ '" '
						+ 'psid="'
						+ programStageId
						+ '" '
						+ 'psuid="'
						+ programStageUid
						+ '" '
						+ 'psname="'
						+ programStageName
						+ '" '
						+ 'status="3"'
						+ 'programType="'
						+ selectedStage.attr('programType')
						+ '" '
						+ 'reportDate="" '
						+ 'reportDateDes="'
						+ selectedStage.attr('reportDateDes')
						+ '" '
						+ 'dueDate="'
						+ dueDate
						+ '" '
						+ 'openAfterEnrollment="'
						+ selectedStage.attr('openAfterEnrollment')
						+ '" '
						+ 'reportDateToUse="'
						+ selectedStage.attr('reportDateToUse')
						+ '" '
						+ 'class="stage-object" '
						+ 'value="'
						+ programStageName
						+ '&#13;&#10;&nbsp;'
						+ dueDate
						+ '" '
						+ 'onclick="javascript:loadDataEntry('
						+ programStageInstanceId
						+ ')" '
						+ 'type="button" ' + '></td>';
					$("#programStageIdTR_" + programInstanceId + " input[name='programStageBtn']")
						.each(function(i, item) {
							var element = $(item);
							var dueDateInStage = element.attr('dueDate');
							if (dueDate < dueDateInStage && !flag) {
								$(
									elementBox
											+ '<td id="arrow_'
											+ programStageInstanceId
											+ '"><img src="images/rightarrow.png"></td>')
									.insertBefore(element.parent());
								flag = true;
							}
						});

					if (!flag) {
						$("#programStageIdTR_" + programInstanceId)
							.append('<td id="arrow_'
								+ programStageInstanceId
								+ '">'
								+ '<img src="images/rightarrow.png"></td>'
								+ elementBox);
					}

					if ($('#tb_' + programInstanceId + " :input").length > 4) {
						$('#tb_' + programInstanceId + ' .arrow-left')
								.removeClass("hidden");
						$('#tb_' + programInstanceId + ' .arrow-right')
								.removeClass("hidden");
					}

					if (dueDate < getCurrentDate()) {
						setEventColorStatus(programStageInstanceId, 4);
					} else {
						setEventColorStatus(programStageInstanceId, 3);
					}

					$('#ps_' + programStageInstanceId).focus();
					var repeatable = $('#repeatableProgramStage_'
							+ programInstanceId + " [value="
							+ programStageId + "]")
					if (repeatable.attr("repeatable") == "false") {
						repeatable.css("display", "none");
					}

					$('#createNewEncounterDiv_' + programInstanceId)
							.dialog("close");
					resetActiveEvent(programInstanceId);
					loadDataEntry(programStageInstanceId);
					// Disable Create new event button in the entry form
					// if doesn't have
					// any stage for register
					flag = true;
					$('#repeatableProgramStage_' + programInstanceId + " option ")
						.each(
							function() {
								if ($(this).attr('localid') == programStageId
										&& $(this).attr('repeatable') == 'false') {
									$(this).css("display", "none");
								}
								if ($(this).css('display') != "none") {
									flag = false;
								}
							});

					if (flag) {
						disable('newEncounterBtn_' + programInstanceId);
					}

					$('#createNewEncounterDiv_' + programInstanceId).dialog('close');
					showSuccessMessage(i18n_create_event_success);
				});
	}
}

function disableCompletedButton(disabled) {
	if (disabled) {
		disable('completeBtn');
		enable('uncompleteBtn');
		enable('uncompleteAndAddNewBtn');
	} else {
		enable('completeBtn');
		disable('uncompleteBtn');
		disable('uncompleteAndAddNewBtn');
	}
}

// -----------------------------------------------------------------------------
// Save due-date
// -----------------------------------------------------------------------------

function saveDueDate(programInstanceId, programStageInstanceId,
		programStageInstanceName) {
	var field = byId('value_' + programStageInstanceId + '_date');
	var dateOfIncident = new Date(byId('dateOfIncident').value);
	var dueDate = new Date(field.value);
	if (dueDate < dateOfIncident) {
		field.style.backgroundColor = '#FFCC00';
		alert(i18n_date_less_incident);
		return;
	}
	field.style.backgroundColor = '#ffffcc';
	var dateDueSaver = new DateDueSaver(programStageInstanceId, field.value,
			'#ccffcc');
	dateDueSaver.save();
}
function DateDueSaver(programStageInstanceId_, dueDate_, resultColor_) {

	var programStageInstanceId = programStageInstanceId_;
	var dueDate = dueDate_;
	var resultColor = resultColor_;
	
	this.save = function() {
		var params = 'programStageInstanceId=' + programStageInstanceId
				+ '&dueDate=' + dueDate;
		$.ajax({
			type : "POST",
			url : "saveDueDate.action",
			data : params,
			dataType : "xml",
			success : function(result) {
				handleResponse(result);
			},
			error : function(request, status, errorThrown) {
				handleHttpError(request);
			}
		});
	};
	
	function handleResponse(rootElement) {
		var codeElement = rootElement.getElementsByTagName('code')[0];
		var code = parseInt(codeElement.firstChild.nodeValue);
		if (code == 0) {
			var box = $('#ps_' + programStageInstanceId);
			box.attr('dueDate', dueDate);
			var boxName = box.attr('psname') + "\n" + dueDate;
			box.val(boxName);
			if (dueDate < getCurrentDate()) {
				box.css('border-color', COLOR_RED);
				box.css('background-color', COLOR_LIGHT_RED);
				$('#stat_' + programStageInstanceId + " option[value=3]")
						.remove();
				$('#stat_' + programStageInstanceId).prepend(
						"<option value='4' selected>" + i18n_overdue
								+ "</option>");
			} else {
				box.css('border-color', COLOR_YELLOW);
				box.css('background-color', COLOR_LIGHT_YELLOW);
				$('#stat_' + programStageInstanceId + " option[value=4]")
						.remove();
				$('#stat_' + programStageInstanceId).prepend(
						"<option value='3' selected>"
								+ i18n_scheduled_in_future + "</option>");
			}
			markValue(resultColor);
		} else {
			markValue(COLOR_GREY);
			window.alert(i18n_saving_value_failed_status_code + '\n\n' + code);
		}
	}
	
	function handleHttpError(errorCode) {
		markValue(COLOR_GREY);
		window.alert(i18n_saving_value_failed_error_code + '\n\n' + errorCode);
	}
	
	function markValue(color) {
		var element = document.getElementById('value_' + programStageInstanceId
				+ '_date');
		element.style.backgroundColor = color;
	}
}
// -----------------------------------------------------------------------------
// Cosmetic UI
// -----------------------------------------------------------------------------
function resize() {
	var width = 500;
	var w = $(window).width();
	if ($(".entity-instance-object").length > 1) {
		width += 150;
	}
	if ($(".show-new-event").length > 0) {
		width += 150;
	}
	$('.stage-flow').each(
	  function() {
		var programInstanceId = this.id.split('_')[1];
		if ($(this).find(".table-flow").outerWidth() > $(this)
				.width()) {
			$('#tb_' + programInstanceId).find('.arrow-left')
					.removeClass("hidden");
			$('#tb_' + programInstanceId).find('.arrow-right')
					.removeClass("hidden");
		} else {
			$('#tb_' + programInstanceId).find('.arrow-left')
					.addClass("hidden");
			$('#tb_' + programInstanceId).find('.arrow-right')
					.addClass("hidden");
		}
	});
	
	$('.stage-flow').css('width', w - width);
	$('.table-flow').css('width', w - width);
	$('.table-flow tr').each(function() {
		$(this).find('td').attr("width", "10px");
		$(this).find('td:last').removeAttr("width");
	});
}
function moveLeft(programInstanceFlowDiv) {
	$("#" + programInstanceFlowDiv).animate({
		scrollLeft : "-=200"
	}, 'fast');
}
function moveRight(programInstanceFlowDiv) {
	$("#" + programInstanceFlowDiv).animate({
		scrollLeft : "+=200"
	}, 'fast');
}
function setEventStatus(field, programStageInstanceId) {
	var status = field.value;
	field.style.backgroundColor = SAVING_COLOR;
	$.postUTF8('setEventStatus.action', {
		programStageInstanceId : programStageInstanceId,
		status : status
	}, function(json) {
		enable('ps_' + programStageInstanceId);
		var eventBox = $('#ps_' + programStageInstanceId);
		eventBox.attr('status', status);
		setEventColorStatus(programStageInstanceId, status);
		resetActiveEvent(eventBox.attr("pi"));
		if (status == 1 || status == 2) {
			hideById('del_' + programStageInstanceId);
		} else {
			showById('del_' + programStageInstanceId);
			if (status == 5) {
				disable('ps_' + programStageInstanceId);
				var id = 'ps_' + programStageInstanceId;
				if (jQuery(".stage-object-selected").attr('id') == id) {
					hideById('entryForm');
					hideById('executionDateTB');
					hideById('inputCriteriaDiv');
				}
			}
		}
		field.style.backgroundColor = SUCCESS_COLOR;
	});
}

function resetActiveEvent(programInstanceId) {
	var hasActiveEvent = false;
	$(".stage-object")
			.each(
					function() {
						var status = $(this).attr('status');
						if (status != 1 && status != 5 && !hasActiveEvent) {
							var value = $(this).val();
							var programStageInstanceId = $(this).attr('id')
									.split('_')[1];
							$('#td_' + programInstanceId).attr(
									"onClick",
									"javascript:loadActiveProgramStageRecords("
											+ programInstanceId + ", "
											+ programStageInstanceId + ")")
							$('#tr2_' + programInstanceId).html(
									"<a>>>" + value + "</a>");
							$('#tr2_' + programInstanceId).attr(
									"onClick",
									"javascript:loadActiveProgramStageRecords("
											+ programInstanceId + ", "
											+ programStageInstanceId + ")");
							var id = 'ps_' + programStageInstanceId;
							enable('ps_' + programStageInstanceId);
							if ($(".stage-object-selected").attr('id') != $(
									this).attr('id')) {
								hasActiveEvent = true;
							}
						}
					});
	if (!hasActiveEvent) {
		$('#td_' + programInstanceId).attr(
				"onClick",
				"javascript:loadActiveProgramStageRecords(" + programInstanceId
						+ ", false)")
		$('#tr2_' + programInstanceId).html("");
		$('#tr2_' + programInstanceId).attr("onClick", "");
		hideById('executionDateTB');
	}
}
function removeEvent(programStageInstanceId, isEvent) {
	var result = window.confirm(i18n_comfirm_delete_event);
	if (result) {
		var eventBox = $('#ps_' + programStageInstanceId);
		var programStageId = eventBox.attr('psid');
		var programInstanceId = eventBox.attr('pi');
		$.postJSON("removeCurrentEncounter.action", {
			"id" : programStageInstanceId
		}, function(json) {
			if (json.response == "success") {
				$(
						"#repeatableProgramStage_" + programInstanceId
								+ " [value='" + programStageId + "']").css(
						"display", "block");
				$("tr#tr" + programStageInstanceId).remove();
				$("table.listTable tbody tr").removeClass(
						"listRow listAlternateRow");
				$("table.listTable tbody tr:odd").addClass("listAlternateRow");
				$("table.listTable tbody tr:even").addClass("listRow");
				$("table.listTable tbody").trigger("update");
				hideById('smsManagementDiv');
				if (isEvent) {
					showById('searchDiv');
					showById('listEntityInstanceDiv');
				}
				var id = 'ps_' + programStageInstanceId;
				if ($(".stage-object-selected").attr('id') == id) {
					hideById('entryForm');
					hideById('executionDateTB');
					hideById('inputCriteriaDiv');
				}
				$('#ps_' + programStageInstanceId).remove();
				$('#arrow_' + programStageInstanceId).remove();
				$('#org_' + programStageInstanceId).remove();
				resetActiveEvent(programInstanceId);
				$('#repeatableProgramStage_' + programInstanceId + " option ")
						.each(function() {
							if ($(this).attr('localid') == programStageId) {
								$(this).css('display', 'block');
							}
						});
				enable('newEncounterBtn_' + programInstanceId);
				showSuccessMessage(i18n_delete_success);
			} else if (json.response == "error") {
				showWarningMessage(json.message);
			}
		});
	}
}
// -----------------------------------------------------------------------------
// Show relationship with new entity-instance
// -----------------------------------------------------------------------------
function showRelationshipList(entityInstanceId) {
	hideById('addRelationshipDiv');
	hideById('entityInstanceDashboard');
	hideById('selectDiv');
	hideById('searchDiv');
	hideById('listEntityInstanceDiv');
	hideById('entryForm');
	$('#loaderDiv').show();
	$('#listRelationshipDiv').load('showRelationshipList.action', {
		id : entityInstanceId
	}, function() {
		showById('listRelationshipDiv');
		$('#loaderDiv').hide();
	});
}
// -----------------------------------------------------------------------------
// Update EntityInstance
// -----------------------------------------------------------------------------
function showUpdateTrackedEntityInstanceForm(entityInstanceId) {
	hideById('listEntityInstanceDiv');
	setInnerHTML('editEntityInstanceDiv', '');
	hideById('selectDiv');
	hideById('searchDiv');
	hideById('migrationEntityInstanceDiv');
	setInnerHTML('entityInstanceDashboard', '');
	$('#loaderDiv').show();
	var params = "";
	if( getFieldValue('program')!='' ){
		params += "?programId=" + getFieldValue('program');
	}
	
	$('#editEntityInstanceDiv').load(
			'showUpdateTrackedEntityInstanceForm.action' + params, {
				id : entityInstanceId
			}, function() {
				$('#loaderDiv').hide();
				showById('editEntityInstanceDiv');
			});
}

function showRepresentativeInfo(entityInstanceId) {
	$('#representativeInfo').dialog({
		title : i18n_representative_info,
		maximize : true,
		closable : true,
		modal : false,
		overlay : {
			background : '#000000',
			opacity : 0.1
		},
		width : 400,
		height : 300
	});
}
function removeDisabledIdentifier() {
	$("input.idfield").each(function() {
		if ($(this).is(":disabled")) {
			$(this).val("");
		}
	});
}
// -----------------------------------------------------------------------------
// Show representative form
// -----------------------------------------------------------------------------

function toggleUnderAge(this_) {
	if ($(this_).is(":checked")) {
		$('#representativeDiv').dialog('destroy').remove();
		$('<div id="representativeDiv">').load('showAddRepresentative.action',
			{
				related:true
			}, function() {}).dialog({
				title : i18n_tracker_associate,
				maximize : true,
				closable : true,
				modal : true,
				overlay : {
					background : '#000000',
					opacity : 0.1
				},
				width : 800,
				height : 450
			});
	} else {
		$("#representativeDiv :input.idfield").each(function() {
			if ($(this).is(":disabled")) {
				$(this).removeAttr("disabled").val("");
			}
		});
		$("#representativeId").val("");
		$("#relationshipTypeId").val("");
	}
}
// ----------------------------------------------------------------
// Enrollment program
// ----------------------------------------------------------------
function showProgramEnrollmentForm(entityInstanceId) {
	$('#enrollmentDiv').load('showProgramEnrollmentForm.action', {
		id : entityInstanceId
	}).dialog({
		title : i18n_enroll_program,
		maximize : true,
		closable : true,
		modal : true,
		overlay : {
			background : '#000000',
			opacity : 0.1
		},
		width : 550,
		height : 450
	});
}
function programOnchange(programId) {
	if (programId == 0) {
		hideById('enrollmentDateTR');
		hideById('dateOfIncidentTR');
		disable('enrollmentDateField');
		disable('dateOfIncidentField');
	} else {
		var type = $('#enrollmentDiv [name=programId] option:selected').attr(
				'programType')
		
		showById('enrollmentDateTR');
		enable('enrollmentDateField');
		var dateOfEnrollmentDescription = $(
				'#enrollmentDiv [name=programId] option:selected').attr(
				'dateOfEnrollmentDescription');
		var dateOfIncidentDescription = $(
				'#enrollmentDiv [name=programId] option:selected').attr(
				'dateOfIncidentDescription');
		setInnerHTML('enrollmentDateDescription',
				dateOfEnrollmentDescription);
		setInnerHTML('dateOfIncidentDescription', dateOfIncidentDescription);
		var displayIncidentDate = $(
				'#enrollmentDiv [name=programId] option:selected').attr(
				'displayIncidentDate');
		if (displayIncidentDate == 'true') {
			showById('dateOfIncidentTR');
			enable('dateOfIncidentField');
		} else {
			hideById('dateOfIncidentTR');
			disable('dateOfIncidentField');
		}
		
		var program = $('#programEnrollmentSelectDiv [id=programId] option:selected');
		$('#identifierAndAttributeDiv')
				.load("getAttribute.action", {
							id : program.val(),
							entityInstanceId : getFieldValue('entityInstanceId')
						},
						function() {
							$("#dateOfIncidentField").datepicker("destroy");
							$("#enrollmentDateField").datepicker("destroy");
							if (program.attr("selectEnrollmentDatesInFuture") == 'true'
									|| program
											.attr("selectIncidentDatesInFuture") == 'true') {
								datePickerInRange('dateOfIncidentField',
										'enrollmentDateField', false, true);
							} else {
								datePickerInRangeValid('dateOfIncidentField',
										'enrollmentDateField', false, true);
							}
							showById('identifierAndAttributeDiv');
						});
	}
}
function saveSingleEnrollment(entityInstanceId, programId) {
	$.postJSON("saveProgramEnrollment.action", {
		entityInstanceId : entityInstanceId,
		programId : programId,
		dateOfIncident : getCurrentDate(),
		enrollmentDate : getCurrentDate()
	}, function(json) {
		var programInstanceId = json.programInstanceId;
		var programStageInstanceId = json.activeProgramStageInstanceId;
		var programStageName = json.activeProgramStageName;
		var programInfor = getInnerHTML('infor_' + programId);
		var dueDate = json.dueDate;
		var type = $('#enrollmentDiv [id=programId] option:selected').attr(
				'programType');
		var activedRow = "<tr id='tr1_" + programInstanceId + "' type='" + type
				+ "'" + " programStageInstanceId='" + programStageInstanceId
				+ "'>" + " <td id='td_" + programInstanceId + "'>"
				+ " <a href='javascript:loadActiveProgramStageRecords("
				+ programInstanceId + "," + programStageInstanceId + ")'>"
				+ "<span id='infor_" + programInstanceId
				+ "' class='selected bold'>" + programInfor
				+ "</span></a></td>" + "</tr>";
		$('#tr_' + programId).remove();
		$('#activeTB').append(activedRow);
		$('#enrollmentDiv').dialog("close");
		validateIdentifier(entityInstanceId, programId,
				'identifierAndAttributeDiv');
		loadActiveProgramStageRecords(programInstanceId);
		showSuccessMessage(i18n_enrol_success);
	});
}
// ----------------------------------------------------------------
// Program enrollmment && unenrollment
// ----------------------------------------------------------------

function validateProgramEnrollment()
{
	$('#loaderDiv').show();
	$.ajax({
			type : "GET",
			url : 'validateProgramEnrollment.action',
			data : getParamsForDiv('programEnrollmentSelectDiv'),
			success : function(json) {
				var type = json.response;
				if (type == 'success') {
					saveEnrollment();
				} else if (type == 'error') {
					setMessage(i18n_program_enrollment_failed + ':' + '\n'
							+ message);
				} else if (type == 'input') {
					setMessage(json.message);
				}
				$("[id=tab-2] :input").each(function() {
					 var input = $(this);
					 var id = 'dashboard_' + input.attr('id');
					 setInnerHTML(id, input.val());
                 });
                 $('#identifierAndAttributeDiv :input').each(function() {
					 var input = $(this);
					 var id = input.attr('id');
					 if( input.val() != "" ){
							 setInnerHTML('value_' + id, input.val());
							 showById('row_' + id);
					 }
					 var input = $(this);
					 jQuery("#tab-2 [id=" + id + "]").val(input.val());
                 });
				$('#loaderDiv').hide();
			}
		});
}

function saveEnrollment() {
	var entityInstanceId = $('#enrollmentDiv [id=entityInstanceId]').val();
	var programId = $('#enrollmentDiv [id=programId] option:selected').val();
	var programName = $('#enrollmentDiv [id=programId] option:selected').text();
	var dateOfIncident = $('#enrollmentDiv [id=dateOfIncidentField]').val();
	var enrollmentDate = $('#enrollmentDiv [id=enrollmentDateField]').val();
	$.postJSON("saveProgramEnrollment.action", {
		entityInstanceId : entityInstanceId,
		programId : programId,
		dateOfIncident : dateOfIncident,
		enrollmentDate : enrollmentDate
	}, function(json) {
		var programInstanceId = json.programInstanceId;
		var programStageInstanceId = json.activeProgramStageInstanceId;
		var programStageName = json.activeProgramStageName;
		var dueDate = json.dueDate;
		var type = $('#enrollmentDiv [id=programId] option:selected').attr(
				'programType');
		var activedRow = "";
		if (programStageInstanceId != '') {
			activedRow = "<tr id='tr1_" + programInstanceId + "' type='" + type
					+ "'" + " programStageInstanceId='"
					+ programStageInstanceId + "'>" + " <td id='td_"
					+ programInstanceId + "'>"
					+ " <a href='javascript:loadActiveProgramStageRecords("
					+ programInstanceId + "," + programStageInstanceId + ")'>"
					+ "<span id='infor_" + programInstanceId
					+ "' class='selected bold'>" + programName + " ("
					+ enrollmentDate + ")</span></a></td>" + "</tr>";
			activedRow += "<tr id='tr2_" + programInstanceId + "'"
					+ +" onclick='javascript:loadActiveProgramStageRecords("
					+ programInstanceId + "," + programStageInstanceId
					+ ")' style='cursor:pointer;'>"
					+ "<td colspan='2'><a>&#8226; " + programStageName + " ("
					+ dueDate + ")</a></td></tr>";
		} else {
			activedRow = "<tr id='tr1_" + programInstanceId + "' type='" + type
					+ "'>" + " <td id='td_" + programInstanceId + "'>"
					+ " <a href='javascript:loadActiveProgramStageRecords("
					+ programInstanceId + ")'>" + "<span id='infor_"
					+ programInstanceId + "' class='selected bold'>"
					+ programName + " (" + enrollmentDate + ")</span></a></td>"
					+ "</tr>";
		}
		$('#activeTB').prepend(activedRow);
		$('#enrollmentDiv').dialog("close");
		saveIdentifierAndAttribute(entityInstanceId, programId,
				'identifierAndAttributeDiv');
		loadActiveProgramStageRecords(programInstanceId);
		showSuccessMessage(i18n_enrol_success);
	});
}

function unenrollmentForm(programInstanceId, status) {
	var comfirmMessage = i18n_complete_program_confirm_message;
	if (status == 2)
		comfirmMessage = i18n_quit_confirm_message;
	if ( confirm(comfirmMessage) ) {
		$.ajax({
			type : "POST",
			url : 'setProgramInstanceStatus.action',
			data : "programInstanceId=" + programInstanceId + "&status=" + status,
			success : function(json) {
				moveToCompleteDiv( programInstanceId, programStageInstanceId );
				
				$("[id=tab-2] :input").prop('disabled', true);
				$("[id=tab-3] :input").prop('disabled', true);
				$("[id=tab-4] :input").prop('disabled', true);
				$("[id=tab-5] :input").prop('disabled', true);
				$("[id=tab-3] :input").datepicker("destroy");
				$("#completeProgram").attr('disabled', true);
				$("#incompleteProgram").attr('disabled', false);
				// disable remove event icons
				$('[id=tab-3]').find('img').parent().removeAttr("href");
				if (status == 1) {
					showSuccessMessage(i18n_complete_success);
				} else if (status == 2) {
					showSuccessMessage(i18n_program_cancelled_success);
				} else {
					showSuccessMessage(i18n_program_active_success);
				}
			}
		});
	}
}

function reenrollmentForm(programInstanceId) {
	if ( confirm(i18n_reenrollment_confirm_message) ) {
		$.ajax({
			type : "POST",
			url : 'setProgramInstanceStatus.action',
			data : "programInstanceId=" + programInstanceId + "&completed=false",
			success : function(json) {
				moveToActiveDiv( programInstanceId, programStageInstanceId );
				$("[id=tab-1] :input").prop('disabled', false);
				// Disable skipped events
				$("[id=tab-1] [status=5]").prop('disabled', true);
				$("[id=tab-2] :input").prop('disabled', false);
				$("[id=tab-3] :input").prop('disabled', false);
				$("[id=tab-4] :input").prop('disabled', false);
				$("[id=tab-5] :input").prop('disabled', false);
				$("#completeProgram").attr('disabled', false);
				$("#incompleteProgram").attr('disabled', true);
				$("[id=tab-3] :input").datepicker("destroy");
				// enable remove event icons
				$('[id=tab-3]').find('img').parent().each(function() {
					var e = $(this);
					e.attr('href', e.attr("link"));
				});
				showSuccessMessage(i18n_reenrol_success);
			}
		});
	}
}
function removeProgramInstance(programInstanceId) {
	if (confirm(i18n_remove_confirm_message)) {
		$.postJSON('removeProgramInstance.action', {
			id : programInstanceId
		}, function(json) {
			$('#activeTB [id=tr1_' + programInstanceId + ']').remove();
			$('#activeTB [id=tr2_' + programInstanceId + ']').remove();
			hideById('programEnrollmentDiv');
		});
	}
}
// ----------------------------------------------------------------
// Identifiers && Attributes for selected program
// ----------------------------------------------------------------
function saveIdentifierAndAttribute(entityInstanceId, programId, paramsDiv) {
	var params = getParamsForDiv(paramsDiv);
	params += "&entityInstanceId=" + entityInstanceId;
	params += "&programId=" + programId;
	$.ajax({
		type : "POST",
		url : 'saveAttribute.action',
		data : params,
		success : function(json) {
			$("[id=tab-2] :input").each(function() {
				var input = $(this);
				var id = 'dashboard_' + input.attr('id');
				setInnerHTML(id, input.val());
			});
			$('#propertyForm :input').each(function() {
				var input = $(this);
				var id = input.attr('id');
				if( input.val() != "" ){
					setInnerHTML('value_' + id, input.val());
					showById('row_' + id);
				}
				var input = $(this);
				jQuery("#tab-2 [id=" + id + "]").val(input.val());
			});
			showSuccessMessage(i18n_save_success);
		}
	});
}

// ----------------------------------------------------------------
// EntityInstance Location
// ----------------------------------------------------------------

function getTrackedEntityInstanceLocation(entityInstanceId) {
	hideById('listEntityInstanceDiv');
	hideById('selectDiv');
	hideById('searchDiv');
	setInnerHTML('entityInstanceDashboard', '');
	$('#loaderDiv').show();
	$('#migrationEntityInstanceDiv').load(
			"getTrackedEntityInstanceLocation.action", {
				entityInstanceId : entityInstanceId
			}, function() {
				showById('migrationEntityInstanceDiv');
				$("#loaderDiv").hide();
			});
}
function registerTrackedEntityInstanceLocation(entityInstanceId) {
	$.getJSON('registerTrackedEntityInstanceLocation.action', {
		entityInstanceId : entityInstanceId
	}, function(json) {
		if(json.response=='input'){
			showWarningMessage( i18n_please_select_an_orgunit );
		}
		else{
			showTrackedEntityInstanceDashboardForm(entityInstanceId);
			showSuccessMessage(i18n_save_success);
		}
	});
}

// ----------------------------------------------------------------
// List program-stage-instance of selected program
// ----------------------------------------------------------------

function getVisitSchedule(programInstanceId) {
	$('#tab-3').load("getVisitSchedule.action", {
		programInstanceId : programInstanceId
	});
}

// ----------------------------------------------------------------
// Dash board
// ----------------------------------------------------------------

function showTrackedEntityInstanceDashboardForm(entityInstanceId) {
	hideById('selectDiv');
	hideById('searchDiv');
	hideById('listEntityInstanceDiv');
	hideById('editEntityInstanceDiv');
	hideById('listRelationshipDiv');
	hideById('addRelationshipDiv');
	hideById('migrationEntityInstanceDiv');
	hideById('smsManagementDiv');
	hideById('dataEntryFormDiv');
	setInnerHTML('listEventDiv', '');
	
	$('#loaderDiv').show();
	$('#entityInstanceDashboard').load('trackedEntityInstanceDashboard.action',
			{
				entityInstanceId : entityInstanceId
			}, function() {
				setInnerHTML('mainFormLink', i18n_main_form_link);
				$('#activeTB tr:first').click();
				showById('entityInstanceDashboard');
				$('#loaderDiv').hide();
			});
}
function activeProgramInstanceDiv(programInstanceId) {
	$("#entityInstanceDashboard .selected").each(function() {
		$(this).removeClass();
	});
	$("#infor_" + programInstanceId).each(function() {
		$(this).addClass('selected bold');
	});
	showById('pi_' + programInstanceId);
}
function hideProgramInstanceDiv(programInstanceId) {
	hideById('pi_' + programInstanceId);
	$('#pi_' + programInstanceId).removeClass("link-area-active");
	$("#img_" + programInstanceId).attr('src', '');
}
function loadActiveProgramStageRecords(programInstanceId, activeProgramStageInstanceId) {
	hideById('programEnrollmentDiv');
	if (programInstanceId == "")
		return;
	$('#loaderDiv').show();
	$('#programEnrollmentDiv')
			.load(
					'enrollmentform.action',
					{
						programInstanceId : programInstanceId
					},
					function() {
						showById('programEnrollmentDiv');
						var hasDataEntry = getFieldValue('hasDataEntry');
						var type = $('#tb_' + programInstanceId).attr(
								'programType');
						var program = $('#tr1_' + programInstanceId);
						var selectedProgram = program.attr('programId');
						var relationshipText = program.attr('relationshipText');
						var relatedProgramId = program.attr('relatedProgram');
						var entityInstanceId = getFieldValue('entityInstanceId');
						if (relationshipText != "") {
							setInnerHTML(
									'entityInstanceRelatedStageSpan',
									"&#8226; <a href='javascript:showAddTrackedEntityInstanceForm( "
											+ entityInstanceId
											+ ",\""
											+ relatedProgramId
											+ "\",\""
											+ selectedProgram
											+ "\" , false );' id='relatedEntityInstance_$!programStageInstance.id' >"
											+ relationshipText
											+ "</a><br>&nbsp;");
						} else {
							setInnerHTML('entityInstanceRelatedStageSpan',
									'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;');
						}
						if (type == '2') {
							hideById('colorHelpLink');
							hideById('programInstanceDiv');
							if (hasDataEntry == 'true'
									|| hasDataEntry == undefined) {
								var programStageInstanceId = $('.stage-object')
										.attr('id').split('_')[1];
								if (programStageInstanceId) {
									loadDataEntry(programStageInstanceId);
								}
							}
						} else {
							showById('programInstanceDiv');
							activeProgramInstanceDiv(programInstanceId);
							if (activeProgramStageInstanceId != undefined
									&& activeProgramStageInstanceId != false
									&& (hasDataEntry == 'true' || hasDataEntry == undefined)) {
								loadDataEntry(activeProgramStageInstanceId);
							}
						}
						if (activeProgramStageInstanceId != undefined) {
							$('#completedList').val('');
						}
						$('#loaderDiv').hide();
					});
}
function loadProgramStageRecords(programStageInstanceId, completed) {
	showLoader();
	$('#dataEntryFormDiv').load("viewProgramStageRecords.action", {
		programStageInstanceId : programStageInstanceId
	}, function() {
		if (completed) {
			$("#dataEntryFormDiv :input").each(function() {
				disable(this.id);
			});
		}
		showById('dataEntryFormDiv');
		hideLoader();
	});
}
function updateEnrollment(entityInstanceId, programId, programInstanceId,
		programName) {
	var dateOfIncident = $('#tab-3 [id=dateOfIncident]').val();
	var enrollmentDate = $('#tab-3 [id=enrollmentDate]').val();
	$.postJSON("saveProgramEnrollment.action", {
		entityInstanceId : getFieldValue('entityInstanceId'),
		programId : programId,
		dateOfIncident : dateOfIncident,
		enrollmentDate : enrollmentDate
	}, function(json) {
		var infor = programName + " (" + enrollmentDate + ")";
		setInnerHTML("infor_" + programInstanceId, infor);
		showSuccessMessage(i18n_enrol_success);
	});
}
// load program instance history
function programReports(programInstanceId) {
	$('#programReportDiv').load("getProgramReportHistory.action", {
		programInstanceId : programInstanceId
	});
}
// export program instance history
function exportProgramReports(programInstanceId, type) {
	window.location.href = 'getProgramReportHistory.action?programInstanceId='
			+ programInstanceId + "&type=" + type;
}
// load SMS message and comments
function getEventMessages(programInstanceId) {
	$('#eventMessagesDiv').load("getEventMessages.action", {
		programInstanceId : programInstanceId
	});
}
function dashboardHistoryToggle(evt) {
	$('#dashboardHistoryDiv').toggle();
}
function viewTEIProgram(displayedDiv, hidedDiv) {
	showById(displayedDiv);
	hideById(hidedDiv);
}
// --------------------------------------------------------------------
// Comment && Message
// --------------------------------------------------------------------
function sendSmsOneTrackedEntityInstanceForm() {
	$('#smsDiv').dialog({
		title : i18n_send_message,
		maximize : true,
		closable : true,
		modal : true,
		overlay : {
			background : '#000000',
			opacity : 0.1
		},
		width : 400,
		height : 200
	});
}
function sendSmsOneTrackedEntityInstance(field, id) {
	if (field.value == "") {
		field.style.backgroundColor = ERROR_COLOR;
		$('#' + field.id).attr("placeholder", i18n_this_field_is_required);
		return;
	}
	var url = 'sendSMS.action?';
	if (getFieldValue('sendFromEvent') == "true") {
		url += "programStageInstanceId=" + id;
	} else {
		url += "programInstanceId=" + getFieldValue('programInstanceId');
	}
	field.style.backgroundColor = SAVING_COLOR;
	$.postUTF8(url, {
		msg : field.value,
		sendTo : getFieldValue('sendTo')
	}, function(json) {
		if (json.response == "success") {
			$('#smsDiv').dialog('close');
			var date = new Date();
			var currentTime = date.getHours() + ":" + date.getMinutes();
			$('[name=commentTB]').prepend(
					"<tr><td>" + getFieldValue('currentDate') + " "
							+ currentTime + "</td>" + "<td>"
							+ getFieldValue('programStageName') + "</td>"
							+ "<td>" + getFieldValue('currentUsername')
							+ "</td>" + "<td>" + i18n_message + "</td>"
							+ "<td>" + field.value + "</td>" + +"<td>"
							+ field.value + "</td></tr>");
			field.style.backgroundColor = SUCCESS_COLOR;
			field.value = "";
			hideById('smsError');
			setInnerHTML('smsSuccess', json.message);
			$('#enrollmentDate').width('325');
			$('#dateOfIncident').width('325');
			$('#removeProgram').remove();
			showSuccessMessage(json.message);
		} else {
			field.style.backgroundColor = ERROR_COLOR;
			hideById('smsSuccess');
			setInnerHTML('smsError', json.message);
			showErrorMessage(json.message);
		}
		if ($("#messageTB tr.hidden").length > 0) {
			commentDivToggle(true);
		} else {
			commentDivToggle(false);
		}
	});
}
function keypressOnComment(event, field, programStageInstanceId) {
	if (!programStageInstanceId) {
		programStageInstanceId = $(
				"#entryFormContainer input[id='programStageInstanceId']").val();
	}
	var key = getKeyCode(event);
	if (key == 13) { // Enter
		addComment(field, programStageInstanceId);
	}
}
function addComment(field, programStageInstanceId) {
	field.style.backgroundColor = SAVING_COLOR;
	var commentText = field.value;
	if (commentText == '') {
		field.style.backgroundColor = ERROR_COLOR;
		$('#' + field.id).attr("placeholder", i18n_this_field_is_required);
		return;
	}
	$.postUTF8('saveTrackedEntityInstanceComment.action', {
		programStageInstanceId : programStageInstanceId,
		commentText : commentText
	}, function(json) {
		var programStageName = $("#ps_" + programStageInstanceId).attr(
				'programStageName');
		var date = new Date();
		var currentTime = date.getHours() + ":" + date.getMinutes();
		var content = "<tr><td>" + getCurrentDate("currentDate") + " "
				+ currentTime + "</td>"
		if (programStageName != undefined) {
			content += "<td>" + programStageName + "</td>"
		}
		content += "<td>" + getFieldValue('currentUsername') + "</td>"
		content += "<td>" + i18n_comment + "</td>";
		content += "<td>" + commentText + "</td></tr>";
		$('#commentTB').prepend(content);
		showSuccessMessage(i18n_comment_added);
		field.style.backgroundColor = SUCCESS_COLOR;
		if ($("#commentTB tr").length > 5) {
			commentDivToggle(true);
		} else {
			commentDivToggle(false);
		}
		$('#enrollmentDate').width('325');
		$('#dateOfIncident').width('325');
		$('#removeProgram').remove();
	});
}
function removeComment(programStageInstanceId, commentId) {
	var result = window.confirm(i18n_confirmation_delete_message);
	if (result) {
		setHeaderWaitMessage(i18n_deleting);
		$.postUTF8('removeTrackedEntityInstanceComment.action', {
			programStageInstanceId : programStageInstanceId,
			id : commentId
		}, function(json) {
			hideById('comment_' + commentId);
			$("tr#comment_" + commentId).remove();
			$("table.listTable tbody tr").removeClass(
					"listRow listAlternateRow");
			$("table.listTable tbody tr:odd").addClass("listAlternateRow");
			$("table.listTable tbody tr:even").addClass("listRow");
			$("table.listTable tbody").trigger("update");
			setHeaderDelayMessage(i18n_delete_success);
		});
	}
}
function commentKeyup() {
	var commentInput = byId('commentInput');
	if ($(commentInput).outerHeight() < commentInput.scrollHeight
			+ parseFloat($(commentInput).css("borderTopWidth"))
			+ parseFloat($(commentInput).css("borderBottomWidth")) - 1 ) {
		$(commentInput).height($(commentInput).height() + 10);
	}
}
function removeMessage(programInstanceId, programStageInstanceId, smsId) {
	var result = window.confirm(i18n_confirmation_delete_message);
	if (result) {
		setHeaderWaitMessage(i18n_deleting);
		$.postUTF8('removeSms.action', {
			programInstanceId : programInstanceId,
			programStageInstanceId : programStageInstanceId,
			id : smsId
		}, function(json) {
			$("tr#tr" + smsId).remove();
			$("table.listTable tbody tr").removeClass(
					"listRow listAlternateRow");
			$("table.listTable tbody tr:odd").addClass("listAlternateRow");
			$("table.listTable tbody tr:even").addClass("listRow");
			$("table.listTable tbody").trigger("update");
			setHeaderDelayMessage(i18n_delete_success);
		});
	}
}
function commentDivToggle(isHide) {
	jQuery("#commentReportTB tr").removeClass("hidden");
	$("#commentReportTB tr").each(function(index, item) {
		if (isHide && index > 4) {
			$(item).addClass("hidden");
		} else if (!isHide) {
			$(item).removeClass("hidden");
		}
		index++;
	});
	if ($("#commentReportTB tr").length <= 5) {
		hideById('showCommentBtn');
		hideById('hideCommentBtn');
	} else if (isHide) {
		showById('showCommentBtn');
		hideById('hideCommentBtn');
	} else {
		hideById('showCommentBtn');
		showById('hideCommentBtn');
	}
}
function backPreviousPage(entityInstanceId) {
	if (isDashboard) {
		showTrackedEntityInstanceDashboardForm(entityInstanceId)
	} else {
		loadTrackedEntityInstanceList();
	}
}
// ----------------------------------------------
// Data entry section
// ----------------------------------------------
function filterInSection($this) {
	var $tbody = $this.parent().parent().parent().parent().parent().find(
			"tbody");
	var $trTarget = $tbody.find("tr");
	if ($this.val() == '') {
		$trTarget.show();
	} else {
		var $trTargetChildren = $trTarget.find('td:first-child');
		$trTargetChildren.each(function(idx, item) {
			if ($(item).find('span').length != 0) {
				var text1 = $this.val().toUpperCase();
				var text2 = $(item).find('span').html().toUpperCase();
				if (text2.indexOf(text1) >= 0) {
					$(item).parent().show();
				} else {
					$(item).parent().hide();
				}
			}
		});
	}
	refreshZebraStripes($tbody);
}
function refreshZebraStripes($tbody) {
	$tbody.find('tr:visible:even').removeClass('listRow').removeClass(
			'listAlternateRow').addClass('listRow');
	$tbody.find('tr:visible:odd').removeClass('listRow').removeClass(
			'listAlternateRow').addClass('listAlternateRow');
}
function saveCoordinatesEvent() {
	var programStageInstanceId = $(
			"#entryFormContainer input[id='programStageInstanceId']").val();
	var longitude = $.trim(getFieldValue('longitude'));
	var latitude = $.trim(getFieldValue('latitude'));
	var isValid = true;
	if (longitude == '' && latitude == '') {
		isValid = true;
	} else if (longitude == '' || latitude == '') {
		alert(i18n_enter_values_for_longitude_and_latitude_fields);
		isValid = false;
	} else if (!isNumber(longitude)) {
		byId('longitude').style.backgroundColor = '#ffcc00';
		alert(i18n_enter_a_valid_number);
		isValid = false;
	} else if (!isNumber(latitude)) {
		byId('latitude').style.backgroundColor = '#ffcc00';
		alert(i18n_enter_a_valid_number);
		isValid = false;
	} else if (eval(longitude) > 180) {
		byId('longitude').style.backgroundColor = '#ffcc00';
		alert(i18n_enter_a_value_less_than_or_equal_to_180);
		isValid = false;
	} else if (eval(longitude) < -180) {
		byId('longitude').style.backgroundColor = '#ffcc00';
		alert(i18n_enter_a_value_greater_than_or_equal_to_nagetive_180);
		isValid = false;
	} else if (eval(latitude) > 90) {
		byId('latitude').style.backgroundColor = '#ffcc00';
		alert(i18n_enter_a_value_less_than_or_equal_to_90);
		isValid = false;
	} else if (eval(latitude) < -90) {
		byId('latitude').style.backgroundColor = '#ffcc00';
		alert(i18n_enter_a_value_greater_than_or_equal_to_nagetive_90);
		isValid = false;
	}
	if (isValid) {
		$.ajax({
					url : 'saveCoordinatesEvent.action',
					data : {
						programStageInstanceId : programStageInstanceId,
						longitude : longitude,
						latitude : latitude
					},
					cache : false,
					dataType : 'json'
				})
				.done(function() {
					byId('longitude').style.backgroundColor = SUCCESS_COLOR;
					byId('latitude').style.backgroundColor = SUCCESS_COLOR;
				})
				.fail(
						function() {
							if (DAO.store
									&& programStageInstanceId.indexOf('local') != -1) {
								DAO.store
										.get('dataValues',
												programStageInstanceId)
										.done(
												function(data) {
													data.coordinate = {};
													data.coordinate.longitude = longitude;
													data.coordinate.latitude = latitude;
													this
															.set('dataValues',
																	data)
															.done(
																	function() {
																		byId('longitude').style.backgroundColor = SUCCESS_COLOR;
																		byId('latitude').style.backgroundColor = SUCCESS_COLOR;
																	});
												});
							}
						});
	}
}
// ---------------------------------------------------------------------------------
// Followup program-instance
// ---------------------------------------------------------------------------------

function markForFollowup(programInstanceId, followup) {
	$.postJSON("markForFollowup.action", {
		programInstanceId : programInstanceId,
		followup : followup
	}, function(json) {
		if (followup) {
			$('[name=imgMarkFollowup]').show();
			$('[name=imgUnmarkFollowup]').hide();
			showById("followup_" + programInstanceId);
		} else {
			$('[name=imgMarkFollowup]').hide();
			$('[name=imgUnmarkFollowup]').show();
			hideById("followup_" + programInstanceId);
		}
	});
}
function saveComment(programInstanceId) {
	$.postJSON("saveProgramInstanceComment.action", {
		programInstanceId : programInstanceId,
		comment : getFieldValue('comment')
	}, function(json) {
		$('#comment').css('background-color', SUCCESS_COLOR);
		if (getFieldValue('comment') != '') {
			setFieldValue('updateCommentBtn', i18n_update_comment);
			showSuccessMessage(i18n_update_success);
		} else {
			setFieldValue('updateCommentBtn', i18n_save_comment);
			showSuccessMessage(i18n_save_success);
		}
	});
}

// --------------------------------------------------------------------------
// Advanced-search TEI
// --------------------------------------------------------------------------

function advancedSearchOnclick() {
	$('#advanced-search').toggle();
	if ($('#advanced-search').is(':visible')) {
		hideById('searchByIdTR');
	} else {
		showById('searchByIdTR');
	}
}

function clearAndCloseSearch() {
	$('#advancedSearchTB tr').each(function(i, item) {
		if (i > 0 && $(this).id == undefined) {
			$(this).remove();
		}
	});
	addAttributeOption();
	hideById('advanced-search');
	showById('searchByIdTR');
	enable('program');
	setFieldValue('program', '');
}

function hideSearchCriteria() {
	hideById('advanced-search');
	showById('showSearchCriteriaDiv');
}

function showSearchCriteria() {
	showById('advanced-search');
	hideById('showSearchCriteriaDiv');
}

// ----------------------------------------------------------------------------
// Program boxes in Dashboard
// ----------------------------------------------------------------------------

function moveToCompleteDiv( programInstanceId, programStageInstanceId )
{
	var type = $("#tr1_" + programInstanceId).attr('type');
	var programStageInstanceId = $( "#tr1_" + programInstanceId).attr('programStageInstanceId');
	
	var completed = "<tr id='tr1_"
			+ programInstanceId
			+ "' type='" + type
			+ "' programStageInstanceId='" + programStageInstanceId
			+ "' onclick='javascript:loadActiveProgramStageRecords(" + programInstanceId + ");' >";
	completed += $('#td_' + programInstanceId).parent().html() + "</tr>";
	
	var activeEvent2 = $("#tr2_" + programInstanceId);
	if (activeEvent2.length > 0) {
		completed += "<tr class='hidden'>" + activeEvent2.parent().html() + "</tr>";
	}
	$('#completedTB').prepend(completed);
	$('#activeTB [id=tr1_' + programInstanceId + ']').remove();
	$('#activeTB [id=tr2_' + programInstanceId + ']').remove();
}

function moveToActiveDiv( programInstanceId, programStageInstanceId )
{
	var type = jQuery("#tr1_" + programInstanceId).attr( 'type');
	var programStageInstanceId = jQuery( "#tr1_" + programInstanceId).attr('programStageInstanceId');
	var completed = "<tr type='" + type
			+ "' programStageInstanceId='" + programStageInstanceId
			+ "' onclick='javascript:loadActiveProgramStageRecords(" + programInstanceId + ");' >";
	completed += $('#td_' + programInstanceId).parent().html() + "</tr>";
	var activeEvent = $("#tr2_" + programInstanceId);
	if (activeEvent.length > 0) {
		completed += "<tr>" + activeEvent.parent().html() + "</tr>";
	}
	$('#activeTB').prepend(completed);
	$('#completedTB [id=tr1_' + programInstanceId + ']').remove();
	$('#completedTB [id=tr2_' + programInstanceId + ']').remove();
}
