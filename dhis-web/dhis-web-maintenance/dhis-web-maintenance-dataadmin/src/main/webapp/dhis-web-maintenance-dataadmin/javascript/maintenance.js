
function performMaintenance()
{
    var clearAnalytics = $( "#clearAnalytics" ).is( ":checked" );
    var clearDataMart = $( "#clearDataMart" ).is( ":checked" );
    var dataMartIndex = $( "#dataMartIndex" ).is( ":checked" );
    var zeroValues = $( "#zeroValues" ).is( ":checked" );
    var dataSetCompleteness = $( "#dataSetCompleteness" ).is( ":checked" );
    var prunePeriods = $( "#prunePeriods" ).is( ":checked" );
    var removeExpiredInvitations = $( "#removeExpiredInvitations" ).is( ":checked" );
    var dropSqlViews = $( "#dropSqlViews" ).is( ":checked" );
    var createSqlViews = $( "#createSqlViews" ).is( ":checked" );
    var updateCategoryOptionCombos = $( "#updateCategoryOptionCombos" ).is( ":checked" );
    
    if ( clearAnalytics || clearDataMart || dataMartIndex || zeroValues || dataSetCompleteness || 
    	prunePeriods || removeExpiredInvitations || dropSqlViews || createSqlViews || updateCategoryOptionCombos )
    {
        setHeaderWaitMessage( i18n_performing_maintenance );
        
        var params = "clearAnalytics=" + clearAnalytics + 
        	"&clearDataMart=" + clearDataMart + 
            "&dataMartIndex=" + dataMartIndex +
            "&zeroValues=" + zeroValues +
            "&dataSetCompleteness=" + dataSetCompleteness +
            "&prunePeriods=" + prunePeriods +
            "&removeExpiredInvitations=" + removeExpiredInvitations +
            "&dropSqlViews=" + dropSqlViews +
            "&createSqlViews=" + createSqlViews +
            "&updateCategoryOptionCombos=" + updateCategoryOptionCombos;
        
		$.ajax({
			   type: "POST",
			   url: "performMaintenance.action",
			   data: params,
			   dataType: "xml",
			   success: function(result){
				   setHeaderDelayMessage( i18n_maintenance_performed );
			   }
			});
    }
    else
    {
    	setHeaderDelayMessage( i18n_select_options );
    }
}