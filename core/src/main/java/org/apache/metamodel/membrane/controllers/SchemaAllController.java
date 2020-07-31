package org.apache.metamodel.membrane.controllers;


import org.apache.metamodel.DataContext;
import org.apache.metamodel.membrane.app.DataContextTraverser;
import org.apache.metamodel.membrane.app.registry.TenantContext;
import org.apache.metamodel.membrane.app.registry.TenantRegistry;
import org.apache.metamodel.membrane.swagger.model.*;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = {"/{tenant}/{dataContext}/schemas/{schema}",
        "/{tenant}/{dataContext}/s/{schema}/all"}, produces = MediaType.APPLICATION_JSON_VALUE)
public class SchemaAllController {
    private final TenantRegistry tenantRegistry;

    @Autowired
    public SchemaAllController(TenantRegistry tenantRegistry) {
        this.tenantRegistry = tenantRegistry;
    }


    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public GetSchemaAllResponse get(@PathVariable("tenant") String tenantId,
                                    @PathVariable("dataContext") String dataSourceName,
                                    @PathVariable("schema") String schemaId) {
        final TenantContext tenantContext = tenantRegistry.getTenantContext(tenantId);
        final DataContext dataContext = tenantContext.getDataSourceRegistry().openDataContext(dataSourceName);

        final DataContextTraverser traverser = new DataContextTraverser(dataContext);
        final Schema schema = traverser.getSchema(schemaId);
        List<GetSchemaAllResponseTables> tablesList = new ArrayList<GetSchemaAllResponseTables>();
        for (Table table : schema.getTables()) {
            GetSchemaAllResponseTables getSchemaAllResponseTables = new GetSchemaAllResponseTables();
            getSchemaAllResponseTables.setName(table.getName());
            getSchemaAllResponseTables.setType(table.getType().name());
            List<GetSchemaAllResponseColumns> columnsList = new ArrayList<GetSchemaAllResponseColumns>();
            for (Column column : table.getColumns()) {
                GetSchemaAllResponseColumns getSchemaAllResponseColumns = new GetSchemaAllResponseColumns();
                //set col here
                getSchemaAllResponseColumns.setName(column.getName());
                getSchemaAllResponseColumns.setType(column.getType().getName());

                final GetColumnResponseMetadata metadata = new GetColumnResponseMetadata();
                metadata.number(column.getColumnNumber());
                metadata.size(column.getColumnSize());
                metadata.nullable(column.isNullable());
                metadata.primaryKey(column.isPrimaryKey());
                metadata.indexed(column.isIndexed());
                metadata.columnType(column.getType() == null ? null : column.getType().getName());
                metadata.nativeType(column.getNativeType());
                metadata.remarks(column.getRemarks());

                getSchemaAllResponseColumns.setMetadata(metadata);

                //set col here
                columnsList.add(getSchemaAllResponseColumns);
            }
            getSchemaAllResponseTables.setColumns(columnsList);
            tablesList.add(getSchemaAllResponseTables);
        }

        final GetSchemaAllResponse response = new GetSchemaAllResponse();
        response.setTables(tablesList);
        response.setDatasource(dataSourceName);
        response.setTenant(tenantId);
        response.setType("schema");
        response.setName(schemaId);
        return response;

    }

}
