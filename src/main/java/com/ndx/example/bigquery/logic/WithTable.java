package com.ndx.example.bigquery.logic;

import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.gson.JsonObject;
import com.ndx.example.bigquery.binder.BigQueryConfiguration;

public class WithTable {
	private static final Logger logger = LoggerFactory
			.getLogger(WithTable.class.getName());

	private BigQuery bigQuery;
	private TableId tableId;
	private Optional<Table> table = Optional.empty();
	
	private WithDataset datasetCreator;

	private BigQueryConfiguration configuration;

	public WithTable(BigQueryConfiguration configuration) {
		this.configuration = configuration;
		this.bigQuery = configuration.getBigQuery();
		this.tableId = configuration.getTableId();
		datasetCreator = new WithDataset(bigQuery, tableId);
	}

	public WithTable(BigQueryConfiguration configuration, String name) {
		this(configuration);
		this.tableId = TableId.of(tableId.getProject(), tableId.getProject(), name);
	}

	public <Type> Type whenTableExists(JsonObject example, Function<Table, Type> andAfter) {
		return datasetCreator.whenDatasetExists(dataset -> {
			if(!table.isPresent()) {
				table = Optional.of(ensureTableExists(dataset, new BigQueryTableDefinitionBuilder(configuration).createDefinition(example)));
			}
			return (Type) andAfter.apply(table.get());
		});
	}


	private Table ensureTableExists(Dataset dataset, TableDefinition definiton) {
		Page<Table> tables = dataset.list();
		for (Table t : tables.iterateAll()) {
			if (tableId.getTable().equals(t.getTableId().getTable())) {
				return t;
			}
		}
		// If we're here, it means the dataset was not found. So let's create it!
		TableInfo tableInfo = TableInfo.newBuilder(tableId, definiton).build();
		try {
			return bigQuery.create(tableInfo);
		} catch (BigQueryException e) {
			logger.error(String.format("Unable to create table %s/%s", tableId.getDataset(), tableId.getTable()), e);
			throw new UnsupportedOperationException("Can't create table. Should shut down", e);
		}
	}

	public TableId getTableId() {
		return tableId;
	}


}
