package com.ndx.example.bigquery.binder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllRequest.RowToInsert;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.TableId;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ndx.example.bigquery.logic.WithTable;

public class BigQueryMessageHandler implements MessageHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(BigQueryMessageHandler.class.getName());

	private WithTable withTable;
	private BigQuery bigQuery;
	private TableId tableId;

	public BigQueryMessageHandler(BigQueryConfiguration configuration, ProducerDestination destination, ProducerProperties producerProperties,
			MessageChannel errorChannel) {
		bigQuery = configuration.getBigQuery();
		withTable = new WithTable(configuration, destination.getName());
		tableId = withTable.getTableId();
	}

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		withTable.whenTableExists(null, table -> {
			InsertAllResponse response = bigQuery.insertAll(InsertAllRequest.newBuilder(tableId).addRow(toBigQueryRow(message))
							// More rows can be added in the same RPC by invoking .addRow() on the builder
							.build());
			boolean transmitted = !response.hasErrors();
			if (transmitted) {
			} else {
				// If any of the insertions failed, this lets you inspect the errors
				for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
					// inspect row error
					String errors = entry.getValue().stream().map(error -> String.format("%s %s %s",
							error.getLocation(), error.getMessage(), error.getReason()))
							.collect(Collectors.joining("\n", "\t", ""));
					logger
							.error(String.format("unable to write entry %s due to errors %s", entry.getKey(), errors));
				}
			}
			
			return null;
		});
	}

	private Map<String, Object> toBigQueryRow(Message<?> message) {
		return toBigQueryRow((JsonObject) message.getPayload());
	}

	private Map<String, Object> toBigQueryRow(JsonObject body) {
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
