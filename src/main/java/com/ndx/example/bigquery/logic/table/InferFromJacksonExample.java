package com.ndx.example.bigquery.logic.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.gson.JsonObject;

public class InferFromJacksonExample implements TableDefinitionBuilder<JsonNode> {

	@Override
	public Collection<Field> asFieldList(JsonNode example) {
		Collection<Field> fields = new ArrayList<>();
		if(example instanceof ObjectNode) {
			Iterator<Entry<String, JsonNode>> children = example.fields();
			while(children.hasNext()) {
				Entry<String, JsonNode> entry = children.next();
				String s = entry.getKey();
				JsonNode value = entry.getValue();
				nodeAsFieldList(fields, s, value);
			}
		} else {
			nodeAsFieldList(fields, "root", example);
		}
		return fields;
	}

	private void nodeAsFieldList(Collection<Field> fields, String s, JsonNode value) {
		if (value instanceof BooleanNode) {
			fields.add(Field.of(s, LegacySQLTypeName.BOOLEAN));
/*		} else if (value instanceof Date) {
			// TODO when to use Date ?
			fields.add(Field.of(s, LegacySQLTypeName.DATETIME));
*/		} else if (value instanceof FloatNode) {
			fields.add(Field.of(s, LegacySQLTypeName.FLOAT));
		} else if (value instanceof IntNode) {
			fields.add(Field.of(s, LegacySQLTypeName.INTEGER));
		} else if (value instanceof NumericNode) {
			fields.add(Field.of(s, LegacySQLTypeName.NUMERIC));
		} else if (value instanceof ObjectNode) {
			fields.add(Field.of(s, LegacySQLTypeName.RECORD, FieldList.of(asFieldList((JsonNode) value))));
		} else if (value instanceof TextNode) {
			fields.add(Field.of(s, LegacySQLTypeName.STRING));
		}
	}
}