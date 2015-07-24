isAjax = true;
var generateResultParams = "";

function orgunitSelected( orgUnits, orgUnitNames )
{
	var width = jQuery('#program').width();
	jQuery('#program').width(width-30);
	showById( "programLoader" );
	disable('program');
	disable('listEntityInstanceBtn');
	showById('mainLinkLbl');
	showById('searchDiv');
	hideById('listEventDiv');
	hideById('listEventDiv');
	hideById('entityInstanceDashboard');
	hideById('smsManagementDiv');
	hideById('sendSmsFormDiv');
	hideById('editEntityInstanceDiv');
	hideById('resultSearchDiv');
	hideById('enrollmentDiv');
	hideById('listRelationshipDiv');
	hideById('addRelationshipDiv');
	hideById('migrationEntityInstanceDiv');

	clearListById('program');
	$('#contentDataRecord').html('');
	setFieldValue('orgunitName', orgUnitNames[0]);
	setFieldValue('orgunitId', orgUnits[0]);
	jQuery.get("getPrograms.action",{}, 
		function(json)
		{
			var count = 0;
			for ( i in json.programs ) {
				if(json.programs[i].type==1){
					count++;
					jQuery( '#program').append( '<option value="' + json.programs[i].uid +'" type="' + json.programs[i].type + '">' + json.programs[i].name + '</option>' );
				}
			}
			if(count==0){
				jQuery( '#program').prepend( '<option value="" selected>' + i18n_none_program + '</option>' );
			}
			else if(count>1){
				jQuery( '#program').prepend( '<option value="" selected>' + i18n_please_select + '</option>' );
				enable('listEntityInstanceBtn');
			}
			
			enableBtn();
			hideById('programLoader');
			jQuery('#program').width(width);
			enable('program');
		});
}

selection.setListenerFunction( orgunitSelected );

// --------------------------------------------------------------------
// List all events
// --------------------------------------------------------------------

function listAllTrackedEntityInstance( page )
{
	hideById('listEventDiv');
	hideById('advanced-search');
	contentDiv = 'listEventDiv';
	$('#contentDataRecord').html('');
	hideById('advanced-search');
	
	var params = "ou=" + getFieldValue("orgunitId");
	params += "&ouMode=SELECTED";
	params += "&program=" + getFieldValue('program');
	params += "&programStatus=ACTIVE";
	params += "&page=" + page;
	
	if( $('#followup').attr('checked')=='checked'){
		params += "followUp=true";
	}
	
	params += '&eventStatus=' + getFieldValue('status');
	params += "&eventStartDate=1900-01-01";
	params += "&eventEndDate=3000-01-01";
	
	$('#attributeIds option').each(function(i, item){
		params += "&attribute=" + item.value;
	});
	
	$.ajax({
		type : "GET",
		url : "../api/trackedEntityInstances.json",
		data : params,
		dataType : "json",
		success : function(json) {
			setInnerHTML('listEventDiv', displayEvents(json, page));
			showById('listEventDiv');
			jQuery('#loaderDiv').hide();
			setTableStyles();
		}
	});
}

function displayEvents(json, page) {
	var table = "";
	
	// Header
	if (json.metaData.pager.total > 0) {
		table += "<p>" + i18n_total_result + " : " + json.metaData.pager.total
				+ "</p>";
	} else {
		table += "<p>" + i18n_no_result_found + "</p>";
	}
	
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
	
	// TEI list
	
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
		table += "<a href=\"javascript:isDashboard=false;showEvents(" + isAdvancedSearch + ", '"
				+ uid
				+ "' )\" title='"
				+ i18n_events
				+ "'><img src='../images/edit_sections.png' alt='"
				+ i18n_events
				+ "'></a>";
		table += "<a href=\"javascript:isDashboard=false;showTrackedEntityInstanceDashboardForm( '"
				+ uid
				+ "' )\" title='"
				+ i18n_dashboard
				+ "'><img src='../images/enroll.png' alt='"
				+ i18n_dashboard
				+ "'></a>";
		table += "<a href=\"javascript:programTrackingList( '" + uid + "', false ) \" "
				+ " title='"
				+ i18n_edit
				+ "'><img src= '../images/edit.png' alt='"
				+ i18n_edit
				+ "'></a>";
		table += "<a href=\"javascript:showTrackedEntityInstanceHistory( '" + uid + "' ) \" "
				+ " title='"
				+ i18n_tracked_entity_instance_details_and_history
				+ "'><img src= '../images/information.png' alt='"
				+ i18n_tracked_entity_instance_details_and_history
				+ "'></a>";
		table += "</td>";
		table += "</tr>";
	}
	table += "</tbody>";
	table += "</table>";
	
	return table + paging(json, page);
	
	if( json.metaData.pager.total > 0 ){
		// Event list
		table += "<table class='listTable' width='100%'>";
		
		table += "<col width='30' />";// Ordered no.
		table += "<col />"; // Event-date
		table += "<col />"; // Data values
		table += "<col width='200' />"; // Operations
		
		table += "<thead><tr><th>#</th>";
		table += "<th>" + i18n_event_date + "</th>";
		table += "<th>" + i18n_data_values + "</th>";
		table += "<th>" + i18n_operations + "</th>";
		table += "</tr></thead>";
		
		table += "<tbody id='list'>";
		for ( var i in json.events) {
			var row = json.events[i];
			var uid = row.event;
			var teiUid = row.trackedEntityInstance;
			var no = eval(json.metaData.pager.page);
			no = (no - 1) * json.metaData.pager.pageSize + eval(i) + 1;
			table += "<tr id='tr" + uid + "'>";
			table += "<td>" + no + "</td>";// No.
			table += "<td>" + row.eventDate + "</td>";// Event-date
			
			// Data values
			table += "<td>";
			if( row.dataValues!=undefined ){
				table += "<table>";
				for (var j in row.dataValues) {
					var colVal = row.dataValues[j].dataElement;
					table += "<tr><td>" +  json.metaData.de[colVal] + ": </td>";
					table += "<td>" +  row.dataValues[j].value + "</td></tr>";
				}
				table += "</table>";
			}
			else{
				table += "</td>";
			}
			
			
		}
		table += "</tbody>";
		table += "</table>";
	
		table += paging(json, page);
	}
	return table;
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

function showEvents( isAdvancedSearch, teiUid){
	var params = "orgUnit=" + getFieldValue("orgunitId");
	params += "&program=" + getFieldValue('program');
	params += "&trackedEntityInstance=" + teiUid;
	params += '&status=' + getFieldValue('status');
	if( isAdvancedSearch ){ // advanced-search
		params += "&startDate=" + getFieldValue('startDate');
		params += "&endDate=" + getFieldValue('endDate');
	}
	else // list
	{
		params += "&startDate=1900-01-01";
		params += "&endDate=3000-01-01";
	}
	
	$.ajax({
		type : "GET",
		url : "../api/events.json",
		data : params,
		dataType : "json",
		success : function(json) {
			var table = "<table>"
			for ( var i in json.events) {
				var row = json.events[i];
				var uid = row.event;
				var eventDate = row.eventDate;
				if(eventDate===undefined){
					eventDate = row.dueDate;
				}
				eventDate = eventDate.substring(0,10);
				table += "<tr><td><a href='javascript:programTrackingList( \"" + uid + "\") ' >" + eventDate + "</a></td></tr>";
			}
			table += "</table>";
			$('#eventList').html(table);
			$('#eventList').dialog({
				title : i18n_events,
				maximize : true,
				closable : true,
				modal : false,
				width : 380,
				height : 290
			}).show('fast');
		}
	});
}

// --------------------------------------------------------------------
// Search events
// --------------------------------------------------------------------

followup = true;

function advancedSearch( params, page )
{
	setFieldValue('listAll', "false");
	$('#contentDataRecord').html('');
	$('#listEventDiv').html('');
	hideById('listEventDiv');
	showLoader()
	
	var params = "ou=" + getFieldValue("orgunitId");
	params += "&ouMode=" + getFieldValue("ouMode");
	params += "&program=" + getFieldValue('program');
	params += "&programStatus=ACTIVE";
	params += "&page=" + page;
	
	if( $('#followup').attr('checked')=='checked'){
		params += "followUp=true";
	}
	
	params += '&eventStatus=' + getFieldValue('status');
	params += "&eventStartDate=" + getFieldValue('startDate');
	params += "&eventEndDate=" + getFieldValue('endDate');
	
	$('#attributeIds option').each(function(i, item){
		params += "&attribute=" + item.value;
	});
	
	$.ajax({
		type : "GET",
		url : "../api/trackedEntityInstances.json",
		data : params,
		dataType : "json",
		success : function(json) {
			setInnerHTML('listEventDiv', displayEvents(json, page));
			showById('listEventDiv');
			jQuery('#loaderDiv').hide();
			setTableStyles();
		}
	});
}

function exportXlsFile()
{
	var url = "getActivityPlanRecords.action?type=xls&trackingReport=true&" + generateResultParams;
	window.location.href = url;
}

// --------------------------------------------------------------------
// program tracking form
// --------------------------------------------------------------------

function programTrackingList( programStageInstanceId, isSendSMS ) 
{
	$('#eventList').dialog('close');
	hideById('listEventDiv');
	hideById('searchDiv');
	showLoader();
	setFieldValue('sendToList', "false");
	$('#smsManagementDiv' ).load("programTrackingList.action",
		{
			programStageInstanceId: programStageInstanceId
		}
		, function(){
			hideById('mainLinkLbl');
			hideById('mainFormLink');
			hideById('searchDiv');
			hideById('listEventDiv');
			showById('smsManagementDiv');
			hideLoader();
		});
}

// --------------------------------------------------------------------
// Post Comments/Send Message
// --------------------------------------------------------------------

function keypressOnMessage(event, field, programStageInstanceId )
{
	var key = getKeyCode( event );
	if ( key==13 ){ // Enter
		sendSmsOneTrackedEntityInstance( field, programStageInstanceId );
	}
}

// --------------------------------------------------------------------
// Dashboard
// --------------------------------------------------------------------

function loadDataEntry( programStageInstanceId )
{
	setInnerHTML('dataEntryFormDiv', '');
	showById('dataEntryFormDiv');
	showById('executionDateTB');
	setFieldValue( 'dueDate', '' );
	setFieldValue( 'executionDate', '' );
	disable('validationBtn');
	disableCompletedButton(true);
	disable('uncompleteBtn');
	jQuery( 'input[id=programStageInstanceId]').val( programStageInstanceId );
	
	$('#executionDate').unbind("change");
	$('#executionDate').change(function() {
		saveExecutionDate( getFieldValue('programId'), programStageInstanceId, byId('executionDate') );
	});
	
	jQuery(".stage-object-selected").removeClass('stage-object-selected');
	var selectedProgramStageInstance = jQuery( '#' + prefixId + programStageInstanceId );
	selectedProgramStageInstance.addClass('stage-object-selected');
	setFieldValue( 'programStageId', selectedProgramStageInstance.attr('psid') );
	
	showLoader();	
	$( '#dataEntryFormDiv' ).load( "dataentryform.action", 
		{ 
			programStageInstanceId: programStageInstanceId
		},function()
		{
			setFieldValue( 'programStageInstanceId', programStageInstanceId );
			var executionDate = jQuery('#executionDate').val();
			var completed = jQuery('#entryFormContainer input[id=completed]').val();
			var irregular = jQuery('#entryFormContainer input[id=irregular]').val();
			var reportDateDes = jQuery("#ps_" + programStageInstanceId).attr("reportDateDes");
			setInnerHTML('reportDateDescriptionField',reportDateDes);
			enable('validationBtn');
			if( executionDate == '' )
			{
				disable('validationBtn');
			}
			else if( executionDate != '' && completed == 'false' )
			{
				disableCompletedButton(false);
			}
			else if( completed == 'true' )
			{
				disableCompletedButton(true);
			}
			resize();
			hideLoader();
			hideById('contentDiv'); 
			jQuery('#dueDate').focus();
		});
}

function entryFormContainerOnReady(){}

// --------------------------------------------------------------------
// Show main form
// --------------------------------------------------------------------

function onClickBackBtn()
{
	showById('mainLinkLbl');
	showById('searchDiv');
	showById('listEventDiv');
	hideById('migrationEntityInstanceDiv');
	hideById('smsManagementDiv');
	hideById('entityInstanceDashboard');
	
	if( isAdvancedSearch ){
		validateAdvancedSearch(1);
	}
	else{
		listAllTrackedEntityInstance(1);
	}
	 
}

// load program instance history
function programTrackingReport( programInstanceId )
{
	$('#programTrackingReportDiv').load("getProgramReportHistory.action", 
		{
			programInstanceId:programInstanceId
		}).dialog(
		{
			title:i18n_program_report,
			maximize:true, 
			closable:true,
			modal:true,
			overlay:{background:'#000000', opacity:0.1},
			width:850,
			height:500
		});
}

function getProgramStageInstanceById(programStageInstanceId)
{
	$('#tab-2').load("getProgramStageInstanceById.action", 
	{
		programStageInstanceId:programStageInstanceId
	});
}
