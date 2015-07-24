
//------------------------------------------------------------------------------
// Add Relationship
//------------------------------------------------------------------------------

function showAddRelationship( entityInstanceId )
{
	hideById('listRelationshipDiv');
	
	jQuery('#loaderDiv').show();
	jQuery('#addRelationshipDiv').load('showAddRelationshipForm.action',
		{
			entityInstanceId:entityInstanceId
		}, function()
		{
			jQuery('[name=addRelationShipLink]').hide();
			showById('addRelationshipDiv');
			hideById('entityInstanceForm');
			jQuery('#loaderDiv').hide();
		});
}

// -----------------------------------------------------------------------------
// Add Relationship TrackedEntityInstance
// -----------------------------------------------------------------------------


//remove value of all the disabled identifier fields
//an identifier field is disabled when its value is inherited from another TEI ( underAge is true ) 
//we don't save inherited identifiers. Only save the representative id.
function removeRelationshipDisabledIdentifier()
{
	jQuery("#addRelationshipEntityInstanceForm :input.idfield").each(function(){
		if( jQuery(this).is(":disabled"))
			jQuery(this).val("");
	});
}


//------------------------------------------------------------------------------
// Relationship partner
//------------------------------------------------------------------------------

function manageRepresentative( entityInstanceId, partnerId )
{		
	$('#relationshipDetails').dialog('destroy').remove();
	$('<div id="relationshipDetails">' ).load( 'getPartner.action', 
		{
			entityInstanceId: entityInstanceId,
			partnerId: partnerId
		}).dialog({
			title: i18n_set_as_representative,
			maximize: true, 
			closable: true,
			modal:false,
			overlay:{background:'#000000', opacity:0.1},
			width: 400,
			height: 300
		});
}

function saveRepresentative( entityInstanceId, representativeId, copyAttribute )
{
	$.post( 'saveRepresentative.action', 
		{ 
			entityInstanceId:entityInstanceId, 
			representativeId: representativeId,
			copyAttribute: copyAttribute	
		}, saveRepresentativeCompleted );
}

function saveRepresentativeCompleted( messageElement )
{
	var type = $(messageElement).find('message').attr('type');
	var message = $(messageElement).find('message').text();
		
	if( type == 'success' )
	{
		jQuery('#relationshipDetails').dialog('close');
	}	
	else if( type == 'error' )
	{
		showErrorMessage( i18n_saving_representative_failed + ':' + '\n' + message );
	}
	else if( type == 'input' )
	{
		showWarningMessage( message );
	}
}

function removeRepresentative( entityInstanceId, representativeId )
{	
	$.post( 'removeRepresentative.action', 
		{ 
			entityInstanceId:entityInstanceId, 
			representativeId: representativeId 
		}, removeRepresentativeCompleted );
}

function removeRepresentativeCompleted( messageElement )
{
	var type = $(messageElement).find('message').attr('type');
	var message = $(messageElement).find('message').text();
	
	if( type == 'success' )
	{
		$('#relationshipDetails').dialog('close');
	}	
	else if( type == 'error' )
	{
		showErrorMessage( i18n_removing_representative_failed + ':' + '\n' + message );
	}
	else if( type == 'input' )
	{
		showWarningMessage( message );
	}
}

//-----------------------------------------------------------------------------
// Search Relationship Partner
//-----------------------------------------------------------------------------

function searchTEIForRelationship()
{
	hideById('searchRelationshipDiv');
	var params  = "ou=" + getFieldValue("orgunitId");
		params += "&ouMode=ALL";
		params += "&attribute=" + getFieldValue("searchingAttributeId") + ":LIKE:" + jQuery('#relationshipSelectForm [id=searchText]').val();
	
	var p = params;
	$('#attributeIds option').each(function(i, item){
		if ( p.indexOf(item.value) < 0 ) {
			params += "&attribute=" + item.value;
		}
	});
	
	$.ajax({
		type : "GET",
		url : "../api/trackedEntityInstances.json",
		data : params,
		dataType : "json",
		success : function(json) {
			setInnerHTML('searchRelationshipDiv', showRelationShips( json ));
			showById('searchRelationshipDiv');
			setTableStyles();
		}
	});		
}

function showRelationShips(json) {

	// Header
	var table = "";
	if (json.rows.length == 0) {
		table += "<p>" + i18n_no_result_found + "</p>";
	}
	else{
	
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
			var no = i + 1;
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
			table += "<a href=\"javascript:isDashboard=true;showTrackedEntityInstanceDashboardForm( '" + uid + "' )\" title='" + i18n_dashboard + "'><img src=\"../images/enroll.png\"></a>";
			table += "<a href=\"javascript:validateAddRelationship('" + uid + "');\" title='" + i18n_assign_relationship + "' ><img src=\"../images/relationship.png\" ></a>";
			table += "<a href=\"javascript:showTrackedEntityInstanceHistory('" + uid + "')\" title='" + i18n_tracked_entity_instance_details_and_history + "'><img src=\"../images/information.png\"></a>";
			table += "</td>";
			table += "</tr>";
		}
		table += "</tbody>";
		table += "</table>";
	}
	return table;
}

function validateAddRelationship(partnerId)
{
	var relationshipTypeId = jQuery( '#relationshipSelectForm [id=relationshipTypeId] option:selected' ).val();	
	if( relationshipTypeId==''){
		setHeaderMessage( i18n_please_select_relationship_type );
		return;
	}
	if( partnerId==null){
		setHeaderMessage( i18n_please_select_a_tracked_entity_instance_for_setting_relationship );
		return;
	}
	addRelationship(partnerId);
}

function addRelationship(partnerId) 
{
	var relationshipTypeId = jQuery( '#relationshipSelectForm [id=relationshipTypeId] option:selected' ).val();	
	var relTypeId = relationshipTypeId.substr( 0, relationshipTypeId.indexOf(':') );
	var relName = relationshipTypeId.substr( relationshipTypeId.indexOf(':') + 1, relationshipTypeId.length );
	
	var params = 'entityInstanceId=' + getFieldValue('entityInstanceId') + 
		'&partnerId=' + partnerId + 
		'&relationshipTypeId=' + relTypeId +
		'&relationshipName=' + relName ;
	
	$.ajax({
		url: 'saveRelationship.action',
		type:"POST",
		data: params,
		dataType: "xml",
		success:  function( messageElement ) {
			messageElement = messageElement.getElementsByTagName( 'message' )[0];
			var type = messageElement.getAttribute( 'type' );
			var message = messageElement.firstChild.nodeValue;
			
			if( type == 'success' ){
				jQuery('#searchRelationshipDiv [id=tr' + partnerId + ']').css("background-color","#C0C0C0")
				setHeaderMessage( i18n_save_success );
			}	
			else if( type == 'error' ){
				setHeaderMessage( i18n_adding_relationship_failed + ':' + '\n' + message );
			}
			else if( type == 'input' ){
				setHeaderMessage( message );
			}
		}
	}); 
		
	return false;
}

//------------------------------------------------------------------------------
// Remove Relationship
//------------------------------------------------------------------------------

function removeRelationship( relationshipId, entityInstanceA, aIsToB, entityInstanceB )
{	
	removeItem( relationshipId, entityInstanceA + ' is ' + aIsToB + ' to ' + entityInstanceB, i18n_confirm_delete_relationship, 'removeRelationship.action' );
}