jQuery( document ).ready( function()
{
    validation2( 'addApprovalLevelForm', function( form )
    {
        form.submit();
    }, {
        'rules' : getValidationRules( "dataApprovalLevel" )
    } );
} );

function validateApprovalLevel()
{
	jQuery.getJSON( "validateApprovalLevel.action", { 
    	"organisationUnitLevel": jQuery( "#organisationUnitLevel" ).val(),
    	"categoryOptionGroupSet": jQuery( "#categoryOptionGroupSet" ).val()
    }, function( json ) {
    	if ( json.response == "error" ) {
    		setHeaderDelayMessage( json.message );
    		return false;
    	}
    	else {
    		$( "#addApprovalLevelForm" ).submit();
    	}
    } );
}