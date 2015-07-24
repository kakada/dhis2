//-----------------------------------------------------------------------------
//Add TrackedEntityInstance
//-----------------------------------------------------------------------------

function validateAddRepresentative()
{	
	$.postJSON("validateTrackedEntityInstance.action?" + getIdentifierTypeIdParams()
		,{}
		, function(json){
			if( json.message.length == 0 ){
				jQuery.ajax({
					type: "POST"
					,url: "addRepresentative.action"
					,data: jQuery("#addRepresentativeForm").serialize()
					,dataType : "xml"
					,success: function(xml){ 
						autoChooseTEI( xml );
					}
					,error: function()
					{
						alert(i18n_error_connect_to_server);
					}
				});
			}
			else{
				showErrorMessage( json.message );
			}
	});
}

//get and build a param String of all the identifierType id and its value
//excluding  identifiers which related is False
function getIdentifierTypeIdParams()
{
	var params = "";
	jQuery("#addRepresentativeForm :input").each(
		function()
		{
			if( jQuery(this).val() && !jQuery(this).is(":disabled") )
				params += "&" + jQuery(this).attr("name") +"="+ jQuery(this).val();
		}
	);
	return params;
}

function searchTEI()
{
	contentDiv = 'listEntityInstanceDiv';
	var params  = "ou=" + getFieldValue("orgunitId");
		params += "&ouMode=ALL";
		params += "&attribute=" + getFieldValue("attributeId") + ":LIKE:" + getFieldValue('searchValue');
	
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
			showTEIs( "searchForm div[id=listTEI]", json );
		}
	});
}

function showTEIs( divContainer, json )
{
	var container = jQuery( "#" + divContainer );
	container.html("");
	if ( json.rows.length == 0 ){
		var message = "<p>" + i18n_no_result_found + "</p>";
		container.html(message);
	}
	else{
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
		
		var result = "";
		var idx = 4;
		for ( var i in json.rows) {
			result += "<hr style='margin:5px 0px;'><table>";
			var cols = json.rows[i];
			var uid = cols[0];
			for (var j = idx; j < json.width; j++) {
				var colVal = cols[j];
				if( colVal!=''){
					if (j == 4) {
						colVal = json.metaData.names[colVal];
					}
					
					if( jQuery.inArray( j, attList )>=0 && colVal!="" ){
						colVal = (colVal=='true')? i18n_yes : i18n_no;
					}
					else if( jQuery.inArray( j, attDate )>=0 && colVal!="" ){
						colVal = colVal.split(' ')[0];
					}
					result += "<tr class='attributeRow'>"
							+ "<td class='bold'>" + json.headers[j].column + "</td>"
							+ "<td>" + colVal + "</td>	"	
							+ "</tr>";
				}
			}
			result += "<tr><td colspan='2'><input type='button' id='" + uid +"' value='" + i18n_choose_this_tracked_entity_instance + "' onclick='chooseTEI(this)'/></td></tr>";
			result += "</table>";
		}
		container.append(i18n_duplicate_warning + "<br>" + result);
	}		
}

// Will be call after save new TEI successfully
function autoChooseTEI( xmlElement )
{
	jQuery("#tab-2").html("<center><span class='bold'>" + i18n_add_tracked_entity_instance_successfully + "</span></center>");
	var root = jQuery(xmlElement);
	jQuery("#entityInstanceForm [id=representativeId]").val( root.find("id").text() );
	jQuery("#entityInstanceForm [id=relationshipTypeId]").val( root.find("relationshipTypeId").text() );
	root.find("identifier").each(
			function(){
				var inputField = jQuery("#entityInstanceForm iden" + jQuery(this).find("identifierTypeId").text());
				inputField.val( jQuery(this).find("identifierText").text() );
				inputField.attr({"disabled":"disabled"});
			}
	);
}

//------------------------------------------------------------------------------
// Set Representative information to parent page.
//------------------------------------------------------------------------------

function chooseTEI(this_)
{
	var relationshipTypeId = jQuery("#searchForm [id=relationshipTypeId]").val();
	if( isBlank( relationshipTypeId ))
	{
		alert(i18n_please_select_relationshipType);
		return;
	}
	
	var id = jQuery(this_).attr("id");
	jQuery("#entityInstanceForm [id=representativeId]").val(id);
	jQuery("#entityInstanceForm [id=relationshipTypeId]").val(relationshipTypeId);
	jQuery(".identifierRow"+id).each(function(){
		var inputField = window.parent.jQuery("#"+jQuery(this).attr("id"));
		if( inputField.metadata({type:"attr",name:"data"}).related  )
		{
			// only inherit identifierType which related is true
			inputField.val(jQuery(this).find("td.value").text());
			inputField.attr({"disabled":"disabled"});
		}
	});
	
	jQuery('#representativeDiv').dialog('close');
}

function toggleSearchType(this_)
{
	var type = jQuery(this_).val();
	if( "identifier" == type )
	{
		jQuery("#searchForm [id=rowIdentifier]").show().find("identifierTypeId").addClass('required:true');
		jQuery("#searchForm [id=rowAttribute]").hide().find("id=attributeId").removeClass("required");
		jQuery("#searchForm [id=searchValue]").val("");
	}
	else if( "attribute" == type )
	{
		jQuery("#searchForm [id=rowIdentifier]").hide().find("#identifierTypeId").removeClass("required");
		jQuery("#searchForm [id=rowAttribute]").show().find("#attributeId").addClass("required:true");
		jQuery("#searchForm [id=searchValue]").val("");
	}
	else if( "name" == type || "" == type )
	{
		jQuery("#searchForm [id=rowIdentifier]").hide().find("#identifierTypeId").removeClass("required");
		jQuery("#searchForm [id=rowAttribute]").hide().find("#attributeId").removeClass("required");
		jQuery("#searchForm [id=searchValue]").val("");
	}
}

function isBlank(text)
{
	return !text ||  /^\s*$/.test(text);
}

