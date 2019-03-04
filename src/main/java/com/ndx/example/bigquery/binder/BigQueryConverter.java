package com.ndx.example.bigquery.binder;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.cloud.bigquery.InsertAllRequest.RowToInsert;

public interface BigQueryConverter<Type> {

	Map<String, Object> toBigQueryRow(Type jsonMessage);

}
