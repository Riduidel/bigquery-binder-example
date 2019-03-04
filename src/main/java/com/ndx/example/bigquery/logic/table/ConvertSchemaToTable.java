package com.ndx.example.bigquery.logic.table;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Field.Builder;
import com.google.cloud.bigquery.Field.Mode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ConvertSchemaToTable extends GsonTableDefinitionBuilder {

	@Override
	public Collection<Field> asFieldList(JsonObject source) {
		Collection<Field> returned = new LinkedList<>();
		Set<String> required = findRequiredIn(source);
		JsonObject properties = source.getAsJsonObject("properties");
		for (String name : properties.keySet()) {
			JsonObject p = properties.getAsJsonObject(name);
			Builder builder = Field.newBuilder(name, LegacySQLTypeName.STRING);
			if (required.contains(name)) {
				builder.setMode(Mode.REQUIRED);
			} else {
				builder.setMode(Mode.NULLABLE);
			}
			computeTypeOf(p, builder);
			if (p.keySet().contains("description")) {
				builder.setDescription(p.get("description").getAsString());
			}
			returned.add(builder.build());
		}
		return returned;
	}

	private void computeTypeOf(JsonObject p, Builder builder) {
		if (p.keySet().contains("type")) {
			String type = p.get("type").getAsString();
			switch (type) {
			case "array":
				builder.setMode(Mode.REPEATED);
				JsonObject items = p.getAsJsonObject("items");
				computeTypeOf(items, builder);
				break;
			case "boolean":
				builder.setType(LegacySQLTypeName.BOOLEAN);
				break;
			case "integer":
				builder.setType(LegacySQLTypeName.INTEGER);
				break;
			case "number":
				builder.setType(LegacySQLTypeName.NUMERIC);
				break;
			case "object":
				builder.setType(LegacySQLTypeName.RECORD, FieldList.of(asFieldList(p)));
				break;
			case "string":
				builder.setType(LegacySQLTypeName.STRING);
				break;
			default:
				throw new UnsupportedOperationException(
						String.format("I don't have any mapping for JSON type %s", type));
			}
		}
	}

	private Set<String> findRequiredIn(JsonObject source) {
		Set<String> returned = new TreeSet<>();
		if (source.keySet().contains("required")) {
			JsonArray values = source.getAsJsonArray("required");
			values.forEach(v -> returned.add(v.toString()));
		}
		return returned;
	}

}