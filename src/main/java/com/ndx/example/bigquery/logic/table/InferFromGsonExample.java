package com.ndx.example.bigquery.logic.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.gson.JsonObject;

public class InferFromGsonExample extends GsonTableDefinitionBuilder {

	@Override
	public Collection<Field> asFieldList(JsonObject example) {
		Collection<Field> fields = new ArrayList<>();
		for (String s : example.keySet()) {
			Object value = example.get(s);
			if (value instanceof Boolean) {
				fields.add(Field.of(s, LegacySQLTypeName.BOOLEAN));
				// TODO : what are bytes ?
			} else if (value instanceof Date) {
				// TODO when to use Date ?
				fields.add(Field.of(s, LegacySQLTypeName.DATETIME));
			} else if (value instanceof Float) {
				fields.add(Field.of(s, LegacySQLTypeName.FLOAT));
			} else if (value instanceof Integer) {
				fields.add(Field.of(s, LegacySQLTypeName.INTEGER));
			} else if (value instanceof Number) {
				fields.add(Field.of(s, LegacySQLTypeName.NUMERIC));
			} else if (value instanceof JsonObject) {
				fields.add(Field.of(s, LegacySQLTypeName.RECORD, FieldList.of(asFieldList((JsonObject) value))));
			} else if (value instanceof String) {
				fields.add(Field.of(s, LegacySQLTypeName.STRING));
			}
		}
		return fields;
	}
}