package com.ndx.example.bigquery.logic.table;

import java.util.Collection;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface TableDefinitionBuilder<Type> {
	public default Schema build(Type source) {
		return Schema.of(asFieldList(source));
	}

	public Iterable<Field> asFieldList(Type source);
}