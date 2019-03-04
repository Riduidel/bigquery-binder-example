package com.ndx.example.bigquery.binder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.LegacySQLTypeName;

public class JacksonConverter implements BigQueryConverter<JsonNode> {

	@Override
	public Map<String, Object> toBigQueryRow(JsonNode jsonMessage) {
		Map<String, Object> returned = new LinkedHashMap<>();
		if(jsonMessage instanceof ObjectNode) {
			Iterator<Entry<String, JsonNode>> children = jsonMessage.fields();
			while(children.hasNext()) {
				Entry<String, JsonNode> child = children.next();
				returned.put(child.getKey(), toBigQueryValue(child.getValue()));
			}
		} else {
			returned.put("root", toBigQueryValue(jsonMessage));
		}
		return returned;
	}

	private Object toBigQueryValue(JsonNode value) {
		if (value instanceof BooleanNode) {
			return value.booleanValue();
/*		} else if (value instanceof Date) {
			// TODO when to use Date ?
			fields.add(Field.of(s, LegacySQLTypeName.DATETIME));
*/		} else if (value instanceof FloatNode) {
			return value.floatValue();
		} else if (value instanceof IntNode) {
			return value.intValue();
		} else if (value instanceof NumericNode) {
			return value.doubleValue();
		} else if (value instanceof ObjectNode) {
			return toBigQueryRow((ObjectNode) value);
		} else if (value instanceof TextNode) {
			return value.textValue();
		} else if (value instanceof ArrayNode) {
			ArrayNode array = (ArrayNode) value;
			List<Object> returned = new ArrayList<>();
			for (int i = 0; i < array.size(); i++) {
				returned.add(toBigQueryValue(array.get(i)));
			}
			return returned;
		} else {
			throw new UnsupportedOperationException(String.format("Node type %s is not supported", value.getNodeType()));
		}
	}
}
