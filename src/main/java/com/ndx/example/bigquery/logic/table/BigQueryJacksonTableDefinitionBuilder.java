package com.ndx.example.bigquery.logic.table;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableDefinition;
import com.google.gson.JsonObject;
import com.ndx.example.bigquery.binder.BigQueryConfiguration;

public class BigQueryJacksonTableDefinitionBuilder {
	private BigQueryConfiguration bigQuery;

	public BigQueryJacksonTableDefinitionBuilder(BigQueryConfiguration configuration) {
		this.bigQuery = configuration;
	}

	public TableDefinition createDefinition(JsonNode example) {
		Schema schema = null;
		if (bigQuery.getTableDefinition().isPresent()) {
			schema = parseTableDefiniton(bigQuery.getTableDefinition().get());
		} else if (bigQuery.getSchema().isPresent()) {
			schema = new ConvertSchemaToTable().build(bigQuery.getSchema().get());
		} else {
			schema = new InferFromJacksonExample().build(example);
		}
		return StandardTableDefinition.of(schema);
	}

	protected Schema parseTableDefiniton(JsonObject string) {
		throw new UnsupportedOperationException("parsing table definition is not yet implemented");
	}
}
