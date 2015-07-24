
function checkHubServerAvailability()
{
	$.getJSON( "../api/synchronization/availability", function( json ) {
		setHeaderDelayMessage( json.message );
	} );
}