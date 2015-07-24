
function checkRemoteServerAvailability()
{
	$.getJSON( "../api/synchronization/availability", function( json ) {
		setHeaderDelayMessage( json.message );
	} );
}