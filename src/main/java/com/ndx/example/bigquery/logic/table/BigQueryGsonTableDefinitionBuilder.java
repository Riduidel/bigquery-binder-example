package com.ndx.example.bigquery.logic.table;

import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableDefinition;
import com.google.gson.JsonObject;
import com.ndx.example.bigquery.binder.BigQueryConfiguration;

public class BigQueryGsonTableDefinitionBuilder {
	private BigQueryConfiguration bigQuery;

	public BigQueryGsonTableDefinitionBuilder(BigQueryConfiguration configuration) {
		this.bigQuery = configuration;
	}

	public TableDefinition createDefinition(JsonObject example) {
		Schema schema = null;
		if (bigQuery.getTableDefinition().isPresent()) {
			schema = parseTableDefiniton(bigQuery.getTableDefinition().get());
		} else if (bigQuery.getSchema().isPresent()) {
			schema = new ConvertSchemaToTable().build(bigQuery.getSchema().get());
		} else {
			schema = new InferFromGsonExample().build(example);
		}
		return StandardTableDefinition.of(schema);
	}

	protected Schema parseTableDefiniton(JsonObject string) {
		throw new UnsupportedOperationException("parsing table definition is not yet implemented");
	}
}
