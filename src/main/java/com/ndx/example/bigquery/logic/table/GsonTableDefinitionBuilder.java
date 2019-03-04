package com.ndx.example.bigquery.logic.table;

import java.util.Collection;

import com.google.cloud.bigquery.Field;
import com.google.gson.JsonObject;

public class GsonTableDefinitionBuilder implements TableDefinitionBuilder<JsonObject> {

	public Collection<Field> asFieldList(JsonObject source) {
		return asFieldList(source.getAsJsonObject());
	}


}
