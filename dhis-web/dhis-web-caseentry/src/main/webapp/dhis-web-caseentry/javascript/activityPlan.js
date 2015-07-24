isAjax = true;

function orgunitSelected( orgUnits, orgUnitNames )
{
	hideById("listEntityInstanceDiv");
	setFieldValue('orgunitName', orgUnitNames[0]);
	setFieldValue('orgunitId', orgUnits[0]);
}

selection.setListenerFunction( orgunitSelected );

function displayCadendar()
{
	if( byId('useCalendar').checked )
	{
		hideById('showEventSince');
		hideById('showEventUpTo');
		showById('startDueDate');
		showById('endDueDate');
		datePickerInRange( 'startDueDate' , 'endDueDate', false );
	}
	else
	{
		showById('showEventSince');
		showById('showEventUpTo');
		hideById('startDueDate');
		hideById('endDueDate');
		jQuery('#delete_endDueDate').remove();
		jQuery('#delete_startDueDate').remove();
		jQuery('#startDueDate').datepicker("destroy");
		jQuery('#endDueDate').datepicker("destroy");
	}
}

function showActitityList(page)
{
	setFieldValue('listAll', "true");
	hideById('listEntityInstanceDiv');
	$('#contentDataRecord').html('');
  
	showLoader();
	
	var params = "ou=" + getFieldValue("orgunitId");
	params += "&program=" + getFieldValue('program');
	params += "&ouMode=" + $('input[name=ouMode]:checked').val();
	params += "&programStatus=ACTIVE";
	params += "&page=" + page;
	params += "&eventStartDate=" + getFieldValue('startDueDate');
	params += "&eventEndDate=" + getFieldValue('endDueDate');
	
	if(getFieldValue('status')!=''){
		params += '&eventStatus=' + getFieldValue('status');
	}
	
	$('#attributeIds option').each(function(i, item){
		params += "&attribute=" + item.value;
	});
	
	$.ajax({
		type : "GET",
		url : "../api/trackedEntityInstances.json",
		data : params,
		dataType : "json",
		success : function(json) {
			setInnerHTML('listEntityInstanceDiv', displayevents(json, page));
			showById('listEntityInstanceDiv');
			setTableStyles();
			jQuery('#loaderDiv').hide();
			hideLoader();
		}
	});
}

function displayevents(json, page) {
	var table = "";
	
	// Header
	if (json.metaData.pager.total > 0) {
		table += "<p>" + i18n_total_result + " : " + json.metaData.pager.total
				+ "</p>";
	} else {
		table += "<p>" + i18n_no_result_found + "</p>";
	}
	
	var idx = 4;
	
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
	table += "<table class='listTable' width='100%'>";
	
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
		table += "<a href=\"javascript:isDashboard=false;showEvents('"
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
				+ "' href='javascript:showActitityList( " + i
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

function showEvents( teiUid){
	var params = "orgUnit=" + getFieldValue("orgunitId");
	params += "&program=" + getFieldValue('program');
	params += "&programStatus=ACTIVE";
	params += "&trackedEntityInstance=" + teiUid;
	if(getFieldValue('status')!=''){
		params += '&status=' + getFieldValue('status');
	}
	params += "&startDate=" + getFieldValue('startDueDate');
	params += "&endDate=" + getFieldValue('endDueDate');
	
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
				table += "<tr><td><a href='javascript:loadDataEntryDialog( \"" + uid + "\") ' >" + eventDate + "</a></td></tr>";
			}
			table += "</table>";
			$('#eventList').html(table);
			$('#eventList').dialog({
				title : i18n_events,
				maximize : true,
				closable : true,
				modal : true,
				width : 380,
				height : 290
			}).show('fast');
		}
	});
}

function exportActitityList( type )
{
	var params = "?ou=" + getFieldValue("orgunitId");
	params += "&program=" + getFieldValue('program');
	params += "&ouMode=" + $('input[name=ouMode]:checked').val();
	params += "&programStatus=ACTIVE";
	params += "&eventStartDate=" + getFieldValue('startDueDate');
	params += "&eventEndDate=" + getFieldValue('endDueDate');
	
	if(getFieldValue('status')!=''){
		params += '&eventStatus=' + getFieldValue('status');
	}
	
	$('#attributeIds option').each(function(i, item){
		params += "&attribute=" + item.value;
	});
	
    window.location.href = "../api/trackedEntityInstances." + type + params;
}

// --------------------------------------------------------------------
// EntityInstance program tracking
// --------------------------------------------------------------------

function loadDataEntryDialog( programStageInstanceId )
{
	$.ajax({
		type : "GET",
		url : "getProgramStageInstanceByUid.action?programStageInstanceId=" + programStageInstanceId,
		dataType : "json",
		success : function(json) {
			var psiid = json.id;
			jQuery('.stage-object-selected').attr('psuid', json.programStage.uid);
			jQuery('[id=programStageInstanceId]').val(psiid);
			jQuery('#programStageUid').val(json.programStage.uid);
					
			$('#contentDataRecord' ).load("viewProgramStageRecords.action", {
					programStageInstanceId: psiid
				}, function( ) {
					showById('reportDateDiv');
					showById('entityInstanceInforTB');
					showById('entryForm');
					showById('inputCriteriaDiv');
				}).dialog({
					title:i18n_program_stage,
					maximize:true,
					closable:true,
					modal:false,
					overlay:{background:'#000000', opacity:0.1},
					width:850,
					height:500
			});
		}
	});
	
	
}


function statusEventOnChange()
{
	if( !byId('useCalendar').checked )
	{
		var status = getFieldValue("status");

		if( status == '' || status == 'SKIPPED' ){
			enable('showEventSince');
			enable('showEventUpTo');
			setDateRange();
		}
		else if( status == 'SCHEDULE' ){
			disable('showEventSince');
			enable('showEventUpTo');
			setDateRange();
		}
		else{
			enable('showEventSince');
			disable('showEventUpTo');
			setDateRange();
		}
	}
}

function setDateRange()
{
	var status = getFieldValue("status");
	var date = new Date();
	var d = date.getDate();
	var m = date.getMonth();
	var y= date.getFullYear();

	var startDateSince = "";
	var endDateSince = "";
	var startDateUpTo = "";
	var endDateUpTo = "";
	var startDate = "";
	var endDate = "";

	// Get dateRangeSince
	var days = getFieldValue('showEventSince');

    if( days == 'ALL' ) {
        startDateSince = jQuery.datepicker.formatDate(dateFormat, new Date(y - 100, m, d));
    }
    else {
        startDateSince = jQuery.datepicker.formatDate(dateFormat, new Date(y, m, d + eval(days)));
    }

    endDateSince = jQuery.datepicker.formatDate( dateFormat, new Date() );

	// getDateRangeUpTo
	days = getFieldValue('showEventUpTo');
	startDateUpTo = jQuery.datepicker.formatDate( dateFormat, new Date() );
	endDateUpTo = "";
	if( days == 'ALL'){
		endDateUpTo = jQuery.datepicker.formatDate( dateFormat, new Date(y+100, m, d) ) ;
	}
	else{
		endDateUpTo = jQuery.datepicker.formatDate( dateFormat, new Date(y, m, d + eval(days)) ) ;
	}

	// check status to get date-range
    if( status == ''
        || status == 'SKIPPED' ) {
        startDate = startDateSince;
        endDate = endDateUpTo;

    } else if( status == 'SCHEDULE' ) {
        startDate = startDateUpTo;
        endDate = endDateUpTo;
    }
    else {
        startDate = startDateSince;
        endDate = endDateSince;
    }

    jQuery("#startDueDate").val(startDate);
	jQuery("#endDueDate").val(endDate);
}

function setDateRangeUpTo( days )
{
    if( days == "" ) {
        return;
    }

    var date = new Date();
    var d = date.getDate();
    var m = date.getMonth();
    var y = date.getFullYear();

    var startDate = jQuery.datepicker.formatDate(dateFormat, new Date());
    var endDate = "";

    if( days == 'ALL' ) {
        endDate = jQuery.datepicker.formatDate(dateFormat, new Date(y + 100, m, d));
    }
    else {
        d = d + eval(days);
        endDate = jQuery.datepicker.formatDate(dateFormat, new Date(y, m, d));
    }

    jQuery("#startDueDate").val(startDate);
    jQuery("#endDueDate").val(endDate);
}

function setDateRangeAll()
{
	var date = new Date();
	var d = date.getDate();
	var m = date.getMonth();
	var y= date.getFullYear();
}


function programOnChange() {
	var program = getFieldValue('program');
	if( program != '' ){
		$.postJSON("getVisitScheduleAttributes.action", {
			id : program
		}, function(json) {
			clearListById('attributeIds');
			for ( var i in json.attributes) {
				if(json.attributes[i].displayed=='true'){
					jQuery('#attributeIds').append(
					'<option value="' + json.attributes[i].id 
					+ '" valueType="' + json.attributes[i].valueType  + '"></option>');
				}
			}
		});
	}
}
