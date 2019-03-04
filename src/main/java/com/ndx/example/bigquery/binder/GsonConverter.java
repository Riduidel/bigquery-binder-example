package com.ndx.example.bigquery.binder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GsonConverter implements BigQueryConverter<JsonObject> {

	@Override
	public Map<String, Object> toBigQueryRow(JsonObject body) {
		Map<String, Object> returned = new LinkedHashMap<>();
		for (String key : body.keySet()) {
			Object value = body.get(key);
			returned.put(key, toBigQueryValue(value));
		}
		return returned;
	}

	private Object toBigQueryValue(Object value) {
		if (value instanceof JsonObject) {
			JsonObject obj = (JsonObject) value;
			return toBigQueryRow(obj);
		} else if (value instanceof JsonArray) {
			JsonArray array = (JsonArray) value;
			List<Object> returned = new ArrayList<>();
			for (Object o : array) {
				returned.add(toBigQueryValue(o));
			}
			return returned;
		} else {
			return value;
		}
	}

}
