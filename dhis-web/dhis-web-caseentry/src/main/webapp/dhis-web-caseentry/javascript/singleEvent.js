
var _continue = false;

function orgunitSelected( orgUnits, orgUnitNames )
{	
	var width = jQuery('#program').width();
	jQuery('#program').width(width-30);
	showById( "programLoader" );
	disable('program');
	hideById('addNewDiv');
	setFieldValue("orgunitName", orgUnitNames[0]);
	setFieldValue("orgunitId", orgUnits[0]);
	clearListById('program');
	$.postJSON( 'singleEventPrograms.action', {}, function( json )
	{
		var count = 0;
		for ( i in json.programs ) {
			if( json.programs[i].type==2){
				jQuery( '#program').append( '<option value="' + json.programs[i].id +'" programStageId="' + json.programs[i].programStageId + '" type="' + json.programs[i].type + '">' + json.programs[i].name + '</option>' );
				count++;
			}
		}
		
		if(count==0){
			jQuery( '#program').prepend( '<option value="" >' + i18n_none_program + '</option>' );
		}
		else{
			jQuery( '#program').prepend( '<option value="" selected>' + i18n_please_select + '</option>' );
			enable('addEntityInstanceBtn');
		}
		
		enableBtn();
		hideById('programLoader');
		jQuery('#program').width(width);
		enable('program');
	});
}
selection.setListenerFunction( orgunitSelected );

function showAddTrackedEntityInstanceForm()
{
	hideById('dataEntryMenu');
	showById('eventActionMenu');
	showById('nextEventLink');
	hideById('contentDiv');
	hideById('searchDiv');
	hideById('advanced-search');
	hideById('listRelationshipDiv');
	hideById('listEntityInstanceDiv');
	showById('entityInstanceMamagementLink');
	hideById('mainLinkLbl');
	setInnerHTML('addNewDiv','');
	jQuery('#loaderDiv').show();
	jQuery('#addNewDiv').load('showEventWithRegistrationForm.action',
		{
			programId: getFieldValue('program')
		}, function()
		{
			unSave = true;
			showById('addNewDiv');
			jQuery('#loaderDiv').hide();
			setInnerHTML('entityInstanceMamagementLink', i18n_single_event_with_registration_management);
		});
}

function showUpdateEventForm( entityInstanceId )
{
	showLoader();
	hideById('searchDiv');
	hideById('programNameDiv');
	hideById('singleDataEntryFormDiv');
	hideById('dataEntryMenu');
	showById('eventActionMenu');
	hideById('nextEventLink');
	setInnerHTML('addNewDiv','');
	hideById('listEntityInstanceDiv');
	hideById('mainLinkLbl');
	
	unSave = false;
	loadProgramStages(entityInstanceId, getFieldValue('program'));
}

function validateData()
{
	var params = "programId=" + getFieldValue('program') + "&" + getParamsForDiv('entityInstanceForm');
	$("#entityInstanceForm :input").attr("disabled", true);
	$("#entryForm :input").attr("disabled", true);
	$.ajax({
		type: "POST",
		url: 'validateTrackedEntityInstance.action',
		data: params,
		success: function( json ){
			var type = json.response;
			var message = json.message;
			
			if ( type == 'success' ){
				if( message == 0 ){
					addTrackedEntityInstance();
				}
				else if( message == 1 ){
					showErrorMessage( i18n_adding_tracked_entity_instance_failed + ':' + '\n' + i18n_duplicate_identifier );
				}
				else if( message == 2 ){
					showErrorMessage( i18n_adding_tracked_entity_instance_failed + ':' + '\n' + i18n_this_tracked_entity_instance_could_not_be_enrolled_please_check_validation_criteria );
				}
			}
			else{
				$("#entityInstanceForm :input").attr("disabled", true);
				if ( type == 'error' )
				{
					showErrorMessage( i18n_adding_tracked_entity_instance_failed + ':' + '\n' + message );
				}
				else if ( type == 'input' )
				{
					showWarningMessage( message );
				}
				else if( type == 'duplicate' )
				{
					showListTrackedEntityInstanceDuplicate(data, false);
				}
					
				$("#entityInstanceForm :input").attr("disabled", false);
			}
		}
    });	
}

function addTrackedEntityInstance()
{
	$.ajax({
		type: "POST",
		url: 'addTrackedEntityInstance.action',
		data: getParamsForDiv('entityInstanceForm'),
		success: function(json) {
			var entityInstanceId = json.message.split('_')[1];
			addData( getFieldValue('program'), entityInstanceId );
		}
     });
}

function addData( programId, entityInstanceId )
{		
	var params = "programId=" + getFieldValue('program');
		params += "&entityInstanceId=" + entityInstanceId;
		params += "&" + getParamsForDiv('entryForm');
		
	$.ajax({
		type: "POST",
		url: 'saveValues.action',
		data: params,
		success: function(json) {
			if( _continue==true )
			{
				$("#entityInstanceForm :input").attr("disabled", false);
				$("#entryForm :input").attr("disabled", false);
				jQuery('#entityInstanceForm :input').each(function()
				{
					var type=$( this ).attr('type');
					if(type=='checkbox'){
						this.checked = false;
					}
					if(type!='button'){
						$( this ).val('');
					}
					enable(this.id);
				});
				jQuery('#entryForm :input').each(function()
				{
					var type=$( this ).attr('type');
					if(type=='checkbox'){
						this.checked = false;
					}
					else if(type!='button'){
						$( this ).val('');
					}
				});
			}
			else
			{
				hideById('addNewDiv');
				if( getFieldValue('listAll')=='true'){
					listAllTrackedEntityInstance();
				}
				else{
					showById('searchDiv');
					showById('contentDiv');
				}
			}
			backEventList();
			showSuccessMessage( i18n_save_success );
		}
     });
    return false;
}

function showEntryFormDiv()
{
	hideById('singleEventForm');
}

function backEventList()
{
	showById('dataEntryMenu');
	hideById('eventActionMenu');
	showSearchForm();
	if( getFieldValue('listAll')=='true'){
		listAllTrackedEntityInstance();
	}
	hideById('backBtnFromEntry');
}

// --------------------------------------------------------
// Check an available TEI allowed to enroll a program
// --------------------------------------------------------

function validateAllowEnrollment( entityInstanceId, programId  )
{	
	jQuery.getJSON( "validateProgramEnrollment.action",
		{
			entityInstanceId: entityInstanceId,
			programId: programId
		}, 
		function( json ) 
		{    
			jQuery('#loaderDiv').hide();
			hideById('message');
			var type = json.response;
			if ( type == 'success' ){
				showSelectedDataRecoding(entityInstanceId, programId );
			}
			else if ( type == 'input' ){
				showWarningMessage( json.message );
			}
		});
}

function completedAndAddNewEvent()
{
	_continue=true;
	jQuery("#singleEventForm").submit();
}
