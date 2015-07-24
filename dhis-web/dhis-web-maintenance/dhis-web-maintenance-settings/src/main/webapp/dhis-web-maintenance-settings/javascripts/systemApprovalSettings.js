$(function() {
  dhis2.contextmenu.makeContextMenu({
    menuId: 'contextMenu',
    menuItemActiveClass: 'contextMenuItemActive'
  });
});

function renumberApprovalLevels( ) {
    $( ".levelNumber" ).text( function( i ) {
        return ( i + 1 );
    });
}

function removeApprovalLevel( context ) {
    removeItem( context.id, context.name, i18n_confirm_delete_data_approval_level, 'removeApprovalLevel.action', renumberApprovalLevels );
}

function moveApprovalLevelUp( context ) {
    location.href = 'moveApprovalLevelUp.action?id=' + context.id;
}

function moveApprovalLevelDown( context ) {
    location.href = 'moveApprovalLevelDown.action?id=' + context.id;
}
