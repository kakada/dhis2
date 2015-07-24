
$(function() {
  dhis2.contextmenu.makeContextMenu({
    menuId: 'contextMenu',
    menuItemActiveClass: 'contextMenuItemActive'
  });
});

// -----------------------------------------------------------------------------
// View details
// -----------------------------------------------------------------------------

function showUpdateAttributeForm( context ) {
  location.href = 'showUpdateAttributeForm.action?id=' + context.id;
}

function showAttributeDetails( context ) {
	jQuery.getJSON( 'getAttribute.action', { id: context.id },
		function ( json ) {
			setInnerHTML( 'nameField', json.attribute.name );	
			setInnerHTML( 'descriptionField', json.attribute.description );
			setInnerHTML( 'optionSetField', json.attribute.optionSet );
			setInnerHTML( 'idField', json.attribute.uid );
			
			var unique = ( json.attribute.unique == 'true') ? i18n_yes : i18n_no;
			setInnerHTML( 'uniqueField', unique );
			
			var inherit = ( json.attribute.inherit == 'true') ? i18n_yes : i18n_no;
			setInnerHTML( 'inheritField', inherit );
			
			var confidential = ( json.attribute.confidential == 'true') ? i18n_yes : i18n_no;
			setInnerHTML( 'confidentialField', confidential );
			
			var valueType = json.attribute.valueType;
			var typeMap = attributeTypeMap();
			setInnerHTML( 'valueTypeField', typeMap[valueType] );   

			if(json.attribute.unique == 'true'){
				var orgunitScope = json.attribute.orgunitScope;
				var programScope = json.attribute.programScope;
				if( orgunitScope=='false' && programScope=='false' ){
					setInnerHTML( 'scopeField', i18n_whole_system);
				}
				else if(orgunitScope=='true' && programScope=='false' ){
					setInnerHTML( 'scopeField', i18n_orgunit);
				}
				else if(orgunitScope=='false' && programScope=='true' ){
					setInnerHTML( 'scopeField', i18n_program);
				}	
				else{
					setInnerHTML( 'scopeField', i18n_program_within_orgunit);
				}
			}
			
			showDetails();
	});
}

function attributeTypeMap()
{
	var typeMap = [];
	typeMap['number'] = i18n_number;
	typeMap['string'] = i18n_text;
	typeMap['bool'] = i18n_yes_no;
	typeMap['trueOnly'] = i18n_yes_only;
	typeMap['date'] = i18n_date;
	typeMap['phoneNumber'] = i18n_phone_number;
	typeMap['trackerAssociate'] = i18n_tracker_associate;
	typeMap['optionSet'] = i18n_option_set;
	return typeMap;
}

// -----------------------------------------------------------------------------
// Remove Attribute
// -----------------------------------------------------------------------------

function removeAttribute( context )
{
	removeItem( context.id, context.name, i18n_confirm_delete, 'removeAttribute.action' );
}


function typeOnChange() {
	var type = getFieldValue('valueType');

	hideById("optionSetRow");
	disable("optionSetId");
	hideById("trackedEntityRow");
	disable("trackedEntityId");

	if( type == "optionSet" ) {
		showById("optionSetRow");
		enable("optionSetId");
	}
	else if( type == "trackerAssociate" ) {
		showById("trackedEntityRow");
		enable("trackedEntityId");
	}

	if( type == "number" || type == 'string' || type == 'letter' || type == 'phoneNumber' ) {
		enable("unique");
	}
	else {
		disable("unique");
	}
}

function uniqueOnChange(){
	if( $('#unique').attr('checked')=="checked") {
		jQuery('[name=uniqueTR]').show();
		jQuery('#valueType [value=bool]').hide();
		jQuery('#valueType [value=trueOnly]').hide();
		jQuery('#valueType [value=date]').hide();
		jQuery('#valueType [value=trackerAssociate]').hide();
		jQuery('#valueType [value=users]').hide();
		jQuery('#valueType [value=optionSet]').hide();
	}
	else {
		jQuery('[name=uniqueTR]').hide();
		jQuery('#valueType [value=bool]').show();
		jQuery('#valueType [value=trueOnly]').show();
		jQuery('#valueType [value=date]').show();
		jQuery('#valueType [value=trackerAssociate]').show();
		jQuery('#valueType [value=users]').show();
		jQuery('#valueType [value=optionSet]').show();
	}
}
