package org.hisp.dhis.importexport.service;

/*
 * Copyright (c) 2004-2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.amplecode.quick.BatchHandler;
import org.amplecode.quick.BatchHandlerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.chart.Chart;
import org.hisp.dhis.chart.ChartService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategory;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOption;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementGroupSet;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.importexport.GroupMemberAssociation;
import org.hisp.dhis.importexport.GroupMemberType;
import org.hisp.dhis.importexport.ImportDataValue;
import org.hisp.dhis.importexport.ImportDataValueService;
import org.hisp.dhis.importexport.ImportObject;
import org.hisp.dhis.importexport.ImportObjectManager;
import org.hisp.dhis.importexport.ImportObjectStatus;
import org.hisp.dhis.importexport.ImportObjectStore;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.importexport.ImportType;
import org.hisp.dhis.importexport.Importer;
import org.hisp.dhis.importexport.importer.ChartImporter;
import org.hisp.dhis.importexport.importer.ConstantImporter;
import org.hisp.dhis.importexport.importer.DataElementCategoryComboImporter;
import org.hisp.dhis.importexport.importer.DataElementCategoryImporter;
import org.hisp.dhis.importexport.importer.DataElementCategoryOptionImporter;
import org.hisp.dhis.importexport.importer.DataElementGroupImporter;
import org.hisp.dhis.importexport.importer.DataElementGroupSetImporter;
import org.hisp.dhis.importexport.importer.DataElementImporter;
import org.hisp.dhis.importexport.importer.DataSetImporter;
import org.hisp.dhis.importexport.importer.DataValueImporter;
import org.hisp.dhis.importexport.importer.GroupSetImporter;
import org.hisp.dhis.importexport.importer.IndicatorGroupImporter;
import org.hisp.dhis.importexport.importer.IndicatorGroupSetImporter;
import org.hisp.dhis.importexport.importer.IndicatorImporter;
import org.hisp.dhis.importexport.importer.IndicatorTypeImporter;
import org.hisp.dhis.importexport.importer.OrganisationUnitGroupImporter;
import org.hisp.dhis.importexport.importer.OrganisationUnitImporter;
import org.hisp.dhis.importexport.importer.OrganisationUnitLevelImporter;
import org.hisp.dhis.importexport.importer.PeriodImporter;
import org.hisp.dhis.importexport.importer.ReportImporter;
import org.hisp.dhis.importexport.importer.ReportTableImporter;
import org.hisp.dhis.importexport.importer.ValidationRuleImporter;
import org.hisp.dhis.importexport.mapping.GroupMemberAssociationVerifier;
import org.hisp.dhis.importexport.mapping.NameMappingUtil;
import org.hisp.dhis.importexport.mapping.ObjectMappingGenerator;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorGroup;
import org.hisp.dhis.indicator.IndicatorGroupSet;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.indicator.IndicatorType;
import org.hisp.dhis.jdbc.batchhandler.CategoryCategoryOptionAssociationBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.CategoryComboCategoryAssociationBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.ConstantBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataElementBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataElementCategoryBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataElementCategoryComboBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataElementCategoryOptionBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataElementGroupBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataElementGroupMemberBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataElementGroupSetBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataElementGroupSetMemberBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataSetBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataSetMemberBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataSetSourceAssociationBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.DataValueBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.GroupSetBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.GroupSetMemberBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.IndicatorBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.IndicatorGroupBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.IndicatorGroupMemberBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.IndicatorGroupSetBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.IndicatorGroupSetMemberBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.IndicatorTypeBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.OrganisationUnitBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.OrganisationUnitGroupBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.OrganisationUnitGroupMemberBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.PeriodBatchHandler;
import org.hisp.dhis.jdbc.batchhandler.ReportTableBatchHandler;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.report.Report;
import org.hisp.dhis.report.ReportService;
import org.hisp.dhis.reporttable.ReportTable;
import org.hisp.dhis.reporttable.ReportTableService;
import org.hisp.dhis.validation.ValidationRule;
import org.hisp.dhis.validation.ValidationRuleService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class DefaultImportObjectManager
    implements ImportObjectManager
{
    private static final Log log = LogFactory.getLog( DefaultImportObjectManager.class );

    private final ImportParams params = new ImportParams( ImportType.IMPORT, ImportStrategy.NEW_AND_UPDATES, true );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private BatchHandlerFactory batchHandlerFactory;

    public void setBatchHandlerFactory( BatchHandlerFactory batchHandlerFactory )
    {
        this.batchHandlerFactory = batchHandlerFactory;
    }

    private ObjectMappingGenerator objectMappingGenerator;

    public void setObjectMappingGenerator( ObjectMappingGenerator objectMappingGenerator )
    {
        this.objectMappingGenerator = objectMappingGenerator;
    }

    private ImportObjectStore importObjectStore;

    public void setImportObjectStore( ImportObjectStore importObjectStore )
    {
        this.importObjectStore = importObjectStore;
    }

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private OrganisationUnitGroupService organisationUnitGroupService;

    public void setOrganisationUnitGroupService( OrganisationUnitGroupService organisationUnitGroupService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
    }

    private ValidationRuleService validationRuleService;

    public void setValidationRuleService( ValidationRuleService validationRuleService )
    {
        this.validationRuleService = validationRuleService;
    }

    private ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }

    private ImportDataValueService importDataValueService;

    public void setImportDataValueService( ImportDataValueService importDataValueService )
    {
        this.importDataValueService = importDataValueService;
    }

    private ReportService reportService;

    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }

    private ReportTableService reportTableService;

    public void setReportTableService( ReportTableService reportTableService )
    {
        this.reportTableService = reportTableService;
    }

    private ChartService chartService;

    public void setChartService( ChartService chartService )
    {
        this.chartService = chartService;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    // -------------------------------------------------------------------------
    // ImportObjectManager implementation
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void importConstants()
    {
        BatchHandler<Constant> batchHandler = batchHandlerFactory.createBatchHandler( ConstantBatchHandler.class )
            .init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( Constant.class );

        Importer<Constant> importer = new ConstantImporter( batchHandler, constantService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (Constant) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( Constant.class );

        log.info( "Imported Constants" );
    }

    @Override
    @Transactional
    public void importCategoryOptions()
    {
        BatchHandler<DataElementCategoryOption> batchHandler = batchHandlerFactory.createBatchHandler(
            DataElementCategoryOptionBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( DataElementCategoryOption.class );

        Importer<DataElementCategoryOption> importer = new DataElementCategoryOptionImporter( batchHandler,
            categoryService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (DataElementCategoryOption) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( DataElementCategoryOption.class );

        log.info( "Imported DataElementCategoryOptions" );
    }

    @Override
    @Transactional
    public void importCategories()
    {
        BatchHandler<DataElementCategory> batchHandler = batchHandlerFactory.createBatchHandler(
            DataElementCategoryBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( DataElementCategory.class );

        Importer<DataElementCategory> importer = new DataElementCategoryImporter( batchHandler, categoryService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (DataElementCategory) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( DataElementCategory.class );

        log.info( "Imported DataElementCategories" );
    }

    @Override
    @Transactional
    public void importCategoryCombos()
    {
        BatchHandler<DataElementCategoryCombo> batchHandler = batchHandlerFactory.createBatchHandler(
            DataElementCategoryComboBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( DataElementCategoryCombo.class );

        Importer<DataElementCategoryCombo> importer = new DataElementCategoryComboImporter( batchHandler,
            categoryService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (DataElementCategoryCombo) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( DataElementCategoryCombo.class );

        log.info( "Imported DataElementCategoryCombos" );
    }

    @Override
    @Transactional
    public void importCategoryOptionCombos() // TODO reuse importer
    {
        Collection<ImportObject> importObjects = importObjectStore
            .getImportObjects( DataElementCategoryOptionCombo.class );

        Map<Object, Integer> categoryComboMapping = objectMappingGenerator.getCategoryComboMapping( false );
        Map<Object, Integer> categoryOptionMapping = objectMappingGenerator.getCategoryOptionMapping( false );

        for ( ImportObject importObject : importObjects )
        {
            DataElementCategoryOptionCombo object = (DataElementCategoryOptionCombo) importObject.getObject();

            int categoryOptionComboId = object.getId();

            if ( importObject.getStatus() == ImportObjectStatus.UPDATE )
            {
                DataElementCategoryOptionCombo compareObject = (DataElementCategoryOptionCombo) importObject
                    .getCompareObject();

                object.setId( compareObject.getId() );
            }

            int categoryComboId = categoryComboMapping.get( object.getCategoryCombo().getId() );

            object.setCategoryCombo( categoryService.getDataElementCategoryCombo( categoryComboId ) );

            Set<DataElementCategoryOption> categoryOptions = new HashSet<>();

            for ( DataElementCategoryOption categoryOption : object.getCategoryOptions() )
            {
                int categoryOptionId = categoryOptionMapping.get( categoryOption.getId() );

                categoryOptions.add( categoryService.getDataElementCategoryOption( categoryOptionId ) );
            }

            object.setCategoryOptions( categoryOptions );

            NameMappingUtil.addCategoryOptionComboMapping( categoryOptionComboId, object );

            if ( importObject.getStatus() == ImportObjectStatus.NEW )
            {
                categoryService.addDataElementCategoryOptionCombo( object );
            }
            else if ( importObject.getStatus() == ImportObjectStatus.UPDATE )
            {
                categoryService.updateDataElementCategoryOptionCombo( object );
            }
        }

        importObjectStore.deleteImportObjects( DataElementCategoryOptionCombo.class );

        log.info( "Imported DataElementCategoryOptionCombos" );
    }

    @Override
    @Transactional
    public void importCategoryCategoryOptionAssociations()
    {
        BatchHandler<GroupMemberAssociation> batchHandler = batchHandlerFactory
            .createBatchHandler( CategoryCategoryOptionAssociationBatchHandler.class );

        importGroupMemberAssociation( batchHandler, GroupMemberType.CATEGORY_CATEGORYOPTION, objectMappingGenerator
            .getCategoryMapping( false ), objectMappingGenerator.getCategoryOptionMapping( false ) );

        log.info( "Imported CategoryCategoryOption associations" );
    }

    @Override
    @Transactional
    public void importCategoryComboCategoryAssociations()
    {
        BatchHandler<GroupMemberAssociation> batchHandler = batchHandlerFactory
            .createBatchHandler( CategoryComboCategoryAssociationBatchHandler.class );

        importGroupMemberAssociation( batchHandler, GroupMemberType.CATEGORYCOMBO_CATEGORY, objectMappingGenerator
            .getCategoryComboMapping( false ), objectMappingGenerator.getCategoryMapping( false ) );

        log.info( "Imported CategoryComboCategory associations" );
    }

    @Override
    @Transactional
    public void importDataElements()
    {
        BatchHandler<DataElement> batchHandler = batchHandlerFactory.createBatchHandler( DataElementBatchHandler.class )
            .init();

        Map<Object, Integer> categoryComboMapping = objectMappingGenerator.getCategoryComboMapping( false );

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( DataElement.class );

        Importer<DataElement> importer = new DataElementImporter( batchHandler, dataElementService );

        for ( ImportObject importObject : importObjects )
        {
            DataElement object = (DataElement) importObject.getObject();
            object.getCategoryCombo().setId( categoryComboMapping.get( object.getCategoryCombo().getId() ) );
            importer.importObject( object, params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( DataElement.class );

        log.info( "Imported DataElements" );
    }

    @Override
    @Transactional
    public void importDataElementGroups()
    {
        BatchHandler<DataElementGroup> batchHandler = batchHandlerFactory.createBatchHandler(
            DataElementGroupBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( DataElementGroup.class );

        Importer<DataElementGroup> importer = new DataElementGroupImporter( batchHandler, dataElementService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (DataElementGroup) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( DataElementGroup.class );

        log.info( "Imported DataElementGroups" );
    }

    @Override
    @Transactional
    public void importDataElementGroupMembers()
    {
        BatchHandler<GroupMemberAssociation> batchHandler = batchHandlerFactory
            .createBatchHandler( DataElementGroupMemberBatchHandler.class );

        importGroupMemberAssociation( batchHandler, GroupMemberType.DATAELEMENTGROUP, objectMappingGenerator
            .getDataElementGroupMapping( false ), objectMappingGenerator.getDataElementMapping( false ) );

        log.info( "Imported DataElementGroup members" );
    }

    @Override
    @Transactional
    public void importDataElementGroupSets()
    {
        BatchHandler<DataElementGroupSet> batchHandler = batchHandlerFactory.createBatchHandler(
            DataElementGroupSetBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( DataElementGroupSet.class );

        Importer<DataElementGroupSet> importer = new DataElementGroupSetImporter( batchHandler, dataElementService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (DataElementGroupSet) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( DataElementGroupSet.class );

        log.info( "Imported DataElementGroupSets" );
    }

    @Override
    @Transactional
    public void importDataElementGroupSetMembers()
    {
        BatchHandler<GroupMemberAssociation> batchHandler = batchHandlerFactory
            .createBatchHandler( DataElementGroupSetMemberBatchHandler.class );

        importGroupMemberAssociation( batchHandler, GroupMemberType.DATAELEMENTGROUPSET, objectMappingGenerator
            .getDataElementGroupSetMapping( false ), objectMappingGenerator.getDataElementGroupMapping( false ) );

        log.info( "Imported DataElementGroupSet members" );
    }

    @Override
    @Transactional
    public void importIndicatorTypes()
    {
        BatchHandler<IndicatorType> batchHandler = batchHandlerFactory.createBatchHandler(
            IndicatorTypeBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( IndicatorType.class );

        Importer<IndicatorType> importer = new IndicatorTypeImporter( batchHandler, indicatorService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (IndicatorType) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( IndicatorType.class );

        log.info( "Imported IndicatorTypes" );
    }

    @Override
    @Transactional
    public void importIndicators()
    {
        BatchHandler<Indicator> batchHandler = batchHandlerFactory.createBatchHandler( IndicatorBatchHandler.class )
            .init();

        Map<Object, Integer> indicatorTypeMapping = objectMappingGenerator.getIndicatorTypeMapping( false );

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( Indicator.class );

        Importer<Indicator> importer = new IndicatorImporter( batchHandler, indicatorService );

        for ( ImportObject importObject : importObjects )
        {
            Indicator object = (Indicator) importObject.getObject();
            object.getIndicatorType().setId( indicatorTypeMapping.get( object.getIndicatorType().getId() ) );
            importer.importObject( object, params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( Indicator.class );

        log.info( "Imported Indicators" );
    }

    @Override
    @Transactional
    public void importIndicatorGroups()
    {
        BatchHandler<IndicatorGroup> batchHandler = batchHandlerFactory.createBatchHandler(
            IndicatorGroupBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( IndicatorGroup.class );

        Importer<IndicatorGroup> importer = new IndicatorGroupImporter( batchHandler, indicatorService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (IndicatorGroup) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( IndicatorGroup.class );

        log.info( "Imported IndicatorGroups" );
    }

    @Override
    @Transactional
    public void importIndicatorGroupMembers()
    {
        BatchHandler<GroupMemberAssociation> batchHandler = batchHandlerFactory
            .createBatchHandler( IndicatorGroupMemberBatchHandler.class );

        importGroupMemberAssociation( batchHandler, GroupMemberType.INDICATORGROUP, objectMappingGenerator
            .getIndicatorGroupMapping( false ), objectMappingGenerator.getIndicatorMapping( false ) );

        log.info( "Imported IndicatorGroup members" );
    }

    @Override
    @Transactional
    public void importIndicatorGroupSets()
    {
        BatchHandler<IndicatorGroupSet> batchHandler = batchHandlerFactory.createBatchHandler(
            IndicatorGroupSetBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( IndicatorGroupSet.class );

        Importer<IndicatorGroupSet> importer = new IndicatorGroupSetImporter( batchHandler, indicatorService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (IndicatorGroupSet) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( IndicatorGroupSet.class );

        log.info( "Imported IndicatorGroupSets" );
    }

    @Override
    @Transactional
    public void importIndicatorGroupSetMembers()
    {
        BatchHandler<GroupMemberAssociation> batchHandler = batchHandlerFactory
            .createBatchHandler( IndicatorGroupSetMemberBatchHandler.class );

        importGroupMemberAssociation( batchHandler, GroupMemberType.INDICATORGROUPSET, objectMappingGenerator
            .getIndicatorGroupSetMapping( false ), objectMappingGenerator.getIndicatorGroupMapping( false ) );

        log.info( "Imported IndicatorGroupSet members" );
    }
    
    @Override
    @Transactional
    public void importDataSets()
    {
        BatchHandler<DataSet> batchHandler = batchHandlerFactory.createBatchHandler( DataSetBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( DataSet.class );

        Importer<DataSet> importer = new DataSetImporter( batchHandler, dataSetService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (DataSet) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( DataSet.class );

        log.info( "Imported DataSets" );
    }

    @Override
    @Transactional
    public void importDataSetMembers()
    {
        BatchHandler<GroupMemberAssociation> batchHandler = batchHandlerFactory
            .createBatchHandler( DataSetMemberBatchHandler.class );

        importGroupMemberAssociation( batchHandler, GroupMemberType.DATASET, objectMappingGenerator
            .getDataSetMapping( false ), objectMappingGenerator.getDataElementMapping( false ) );

        log.info( "Imported DataSet members" );
    }

    @Override
    @Transactional
    public void importOrganisationUnits()
    {
        BatchHandler<OrganisationUnit> organisationUnitBatchHandler = batchHandlerFactory.createBatchHandler(
            OrganisationUnitBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( OrganisationUnit.class );

        Importer<OrganisationUnit> importer = new OrganisationUnitImporter( organisationUnitBatchHandler,
            organisationUnitService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (OrganisationUnit) importObject.getObject(), params );
        }

        organisationUnitBatchHandler.flush();

        importObjectStore.deleteImportObjects( OrganisationUnit.class );

        log.info( "Imported OrganisationUnits" );
    }

    @Override
    @Transactional
    public void importOrganisationUnitRelationships()
    {
        Map<Object, Integer> organisationUnitMapping = objectMappingGenerator.getOrganisationUnitMapping( false );

        BatchHandler<OrganisationUnit> batchHandler = batchHandlerFactory.createBatchHandler(
            OrganisationUnitBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore
            .getImportObjects( GroupMemberType.ORGANISATIONUNITRELATIONSHIP );

        for ( ImportObject importObject : importObjects )
        {
            GroupMemberAssociation object = (GroupMemberAssociation) importObject.getObject();

            OrganisationUnit child = organisationUnitService.getOrganisationUnit( organisationUnitMapping.get( object
                .getMemberId() ) );
            OrganisationUnit parent = organisationUnitService.getOrganisationUnit( organisationUnitMapping.get( object
                .getGroupId() ) );
            child.setParent( parent );

            batchHandler.updateObject( child );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( GroupMemberType.ORGANISATIONUNITRELATIONSHIP );

        log.info( "Imported OrganisationUnit relationships" );
    }

    @Override
    @Transactional
    public void importOrganisationUnitGroups()
    {
        BatchHandler<OrganisationUnitGroup> batchHandler = batchHandlerFactory.createBatchHandler(
            OrganisationUnitGroupBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( OrganisationUnitGroup.class );

        Importer<OrganisationUnitGroup> importer = new OrganisationUnitGroupImporter( batchHandler,
            organisationUnitGroupService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (OrganisationUnitGroup) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( OrganisationUnitGroup.class );

        log.info( "Imported OrganisationUnitGroups" );
    }

    @Override
    @Transactional
    public void importOrganisationUnitGroupMembers()
    {
        BatchHandler<GroupMemberAssociation> batchHandler = batchHandlerFactory
            .createBatchHandler( OrganisationUnitGroupMemberBatchHandler.class );

        importGroupMemberAssociation( batchHandler, GroupMemberType.ORGANISATIONUNITGROUP, objectMappingGenerator
            .getOrganisationUnitGroupMapping( false ), objectMappingGenerator.getOrganisationUnitMapping( false ) );

        log.info( "Imported OrganissationUnitGroup members" );
    }

    @Override
    @Transactional
    public void importOrganisationUnitGroupSets()
    {
        BatchHandler<OrganisationUnitGroupSet> batchHandler = batchHandlerFactory.createBatchHandler(
            GroupSetBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( OrganisationUnitGroupSet.class );

        Importer<OrganisationUnitGroupSet> importer = new GroupSetImporter( batchHandler, organisationUnitGroupService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (OrganisationUnitGroupSet) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( OrganisationUnitGroupSet.class );

        log.info( "Imported OrganisationUnitGroupSets" );
    }

    @Override
    @Transactional
    public void importOrganisationUnitGroupSetMembers()
    {
        BatchHandler<GroupMemberAssociation> batchHandler = batchHandlerFactory
            .createBatchHandler( GroupSetMemberBatchHandler.class );

        importGroupMemberAssociation( batchHandler, GroupMemberType.ORGANISATIONUNITGROUPSET, objectMappingGenerator
            .getOrganisationUnitGroupSetMapping( false ), objectMappingGenerator
            .getOrganisationUnitGroupMapping( false ) );

        log.info( "Imported OrganisationUnitGroupSet members" );
    }

    @Override
    @Transactional
    public void importOrganisationUnitLevels()
    {
        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( OrganisationUnitLevel.class );

        Importer<OrganisationUnitLevel> importer = new OrganisationUnitLevelImporter( organisationUnitService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (OrganisationUnitLevel) importObject.getObject(), params );
        }

        importObjectStore.deleteImportObjects( OrganisationUnitLevel.class );

        log.info( "Imported OrganisationUnitLevels" );
    }

    @Override
    @Transactional
    public void importDataSetSourceAssociations()
    {
        BatchHandler<GroupMemberAssociation> batchHandler = batchHandlerFactory
            .createBatchHandler( DataSetSourceAssociationBatchHandler.class );

        importGroupMemberAssociation( batchHandler, GroupMemberType.DATASET_SOURCE, objectMappingGenerator
            .getDataSetMapping( false ), objectMappingGenerator.getOrganisationUnitMapping( false ) );

        log.info( "Imported DataSet Source associations" );
    }

    @Override
    @Transactional
    public void importValidationRules()
    {
        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( ValidationRule.class );

        Importer<ValidationRule> importer = new ValidationRuleImporter( validationRuleService, expressionService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (ValidationRule) importObject.getObject(), params );
        }

        importObjectStore.deleteImportObjects( ValidationRule.class );

        log.info( "Imported ValidationRules" );
    }

    @Override
    @Transactional
    public void importPeriods()
    {
        BatchHandler<Period> batchHandler = batchHandlerFactory.createBatchHandler( PeriodBatchHandler.class ).init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( Period.class );

        Importer<Period> importer = new PeriodImporter( batchHandler, periodService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (Period) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( Period.class );

        log.info( "Imported Periods" );
    }

    @Override
    @Transactional
    public void importReports()
    {
        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( Report.class );

        Importer<Report> importer = new ReportImporter( reportService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (Report) importObject.getObject(), params );
        }

        importObjectStore.deleteImportObjects( Report.class );

        log.info( "Imported Reports" );
    }

    @Override
    @Transactional
    public void importReportTables()
    {
        BatchHandler<ReportTable> batchHandler = batchHandlerFactory.createBatchHandler( ReportTableBatchHandler.class )
            .init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( ReportTable.class );

        Importer<ReportTable> importer = new ReportTableImporter( reportTableService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (ReportTable) importObject.getObject(), params );
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( ReportTable.class );

        log.info( "Imported ReportTables" );
    }

    @Override
    @Transactional
    public void importCharts()
    {
        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( Chart.class );

        Importer<Chart> importer = new ChartImporter( chartService );

        for ( ImportObject importObject : importObjects )
        {
            importer.importObject( (Chart) importObject.getObject(), params );
        }

        importObjectStore.deleteImportObjects( Report.class );

        log.info( "Imported Reports" );
    }

    @Override
    @Transactional
    public void importDataValues()
    {
        Integer importedObjects = 0;
        Integer failedObjects = 0;
        Integer ignoredObjects = 0;

        BatchHandler<DataValue> batchHandler = batchHandlerFactory.createBatchHandler( DataValueBatchHandler.class )
            .init();

        Map<Object, Integer> dataElementMapping = objectMappingGenerator.getDataElementMapping( false );
        Map<Object, Integer> periodMapping = objectMappingGenerator.getPeriodMapping( false );
        Map<Object, Integer> sourceMapping = objectMappingGenerator.getOrganisationUnitMapping( false );
        Map<Object, Integer> categoryOptionComboMapping = objectMappingGenerator.getCategoryOptionComboMapping( false );

        Collection<ImportDataValue> importValues = importDataValueService.getImportDataValues( ImportObjectStatus.NEW );

        Importer<DataValue> importer = new DataValueImporter( batchHandler, dataValueService, params );

        for ( ImportDataValue importValue : importValues )
        {
            DataValue value = importValue.getDataValue();

            try
            {
                if  ( dataElementMapping.containsKey( value.getDataElement().getId() )
                        && periodMapping.containsKey( value.getPeriod().getId())
                        && sourceMapping.containsKey( value.getSource().getId())
                        && categoryOptionComboMapping.containsKey(value.getCategoryOptionCombo().getId()))
                {

                    value.getDataElement().setId( dataElementMapping.get( value.getDataElement().getId() ) );
                    value.getPeriod().setId( periodMapping.get( value.getPeriod().getId() ) );
                    value.getSource().setId( sourceMapping.get( value.getSource().getId() ) );
                    value.getCategoryOptionCombo().setId( categoryOptionComboMapping.get( value.getCategoryOptionCombo().getId() ) );
                    importer.importObject( value, params );
                    importedObjects++;
                }
                else
                {
                    ignoredObjects++;
                }

            }
            catch ( Exception e )
            {
                importedObjects--;
                failedObjects++;
                log.error( "Object import failed" + e );
            }
        }

        batchHandler.flush();

        importDataValueService.deleteImportDataValues();

        log.info( importReport( importedObjects, failedObjects, ignoredObjects ) );
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void importGroupMemberAssociation( BatchHandler<GroupMemberAssociation> batchHandler, GroupMemberType type,
        Map<Object, Integer> groupMapping, Map<Object, Integer> memberMapping )
    {
        GroupMemberAssociationVerifier.clear();

        batchHandler.init();

        Collection<ImportObject> importObjects = importObjectStore.getImportObjects( type );

        for ( ImportObject importObject : importObjects )
        {
            GroupMemberAssociation object = (GroupMemberAssociation) importObject.getObject();

            object.setGroupId( groupMapping.get( object.getGroupId() ) );
            object.setMemberId( memberMapping.get( object.getMemberId() ) );

            if ( GroupMemberAssociationVerifier.isUnique( object, type ) && !batchHandler.objectExists( object ) )
            {
                batchHandler.addObject( object );
            }
        }

        batchHandler.flush();

        importObjectStore.deleteImportObjects( type );
    }

    private String importReport( Integer importedObjects, Integer failedObjects, Integer ignoredObjects )
    {
        Integer totalObjects = importedObjects + failedObjects + ignoredObjects;
        String importReportString = "";
        if ( failedObjects > 0 || ignoredObjects > 0 )
        {
            importReportString = totalObjects.toString() + " values handled.\n"
                + importedObjects.toString() + " new values successfully imported.\n"
                + ignoredObjects.toString() + " were ignored due to validitiy errors.\n"
                +  failedObjects.toString()+ " were not imported due to other errors.";
            return importReportString;
        }
        else
        {
            importReportString = importedObjects.toString() + " values were imported.";
            return importReportString;
        }

    }

}
