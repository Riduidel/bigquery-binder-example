package com.ndx.example.bigquery.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Field.Builder;
import com.google.cloud.bigquery.Field.Mode;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableDefinition;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ndx.example.bigquery.binder.BigQueryConfiguration;

public class BigQueryTableDefinitionBuilder {
	public static interface TableDefinitionBuilder {
		public default Schema build(JsonElement source) {
			return Schema.of(asFieldList(source));
		}

		public default Collection<Field> asFieldList(JsonElement source) {
			return asFieldList(source.getAsJsonObject());
		}

		public Collection<Field> asFieldList(JsonObject source);
	}

	public class InferFromExample implements TableDefinitionBuilder {

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

	public class ConvertSchemaToTable implements TableDefinitionBuilder {

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

	private BigQueryConfiguration bigQuery;

	public BigQueryTableDefinitionBuilder(BigQueryConfiguration configuration) {
		this.bigQuery = configuration;
	}

	public TableDefinition createDefinition(JsonObject example) {
		Schema schema = null;
		if (bigQuery.getTableDefinition().isPresent()) {
			schema = parseTableDefiniton(bigQuery.getTableDefinition().get());
		} else if (bigQuery.getSchema().isPresent()) {
			schema = new ConvertSchemaToTable().build(bigQuery.getSchema().get());
		} else {
			schema = new InferFromExample().build(example);
		}
		return StandardTableDefinition.of(schema);
	}

	protected Schema parseTableDefiniton(JsonObject string) {
		throw new UnsupportedOperationException("parsing table definition is not yet implemented");
	}
}
