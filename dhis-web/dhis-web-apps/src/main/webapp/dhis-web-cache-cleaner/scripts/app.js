'use strict';

/* App Module */

var cacheCleaner = angular.module('cacheCleaner',
		 ['ui.bootstrap', 
		  'ngRoute', 
		  'ngCookies', 
		  'cacheCleanerDirectives', 
		  'cacheCleanerControllers', 
		  'cacheCleanerServices',
          'cacheCleanerFilters',
          'd2Services',
          'd2Controllers',
		  'angularLocalStorage', 
		  'pascalprecht.translate',
          'd2HeaderBar'])
              
.value('DHIS2URL', '..')

.config(function($translateProvider) {   
    $translateProvider.preferredLanguage('en');
    $translateProvider.useLoader('i18nLoader');
});