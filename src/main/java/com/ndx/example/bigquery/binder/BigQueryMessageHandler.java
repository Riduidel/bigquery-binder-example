package com.ndx.example.bigquery.binder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.converter.CompositeMessageConverterFactory;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.MimeType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.TableId;
import com.ndx.example.bigquery.logic.WithTable;

public class BigQueryMessageHandler implements MessageHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(BigQueryMessageHandler.class.getName());

	private WithTable withTable;
	private BigQuery bigQuery;
	private TableId tableId;

	private BigQueryConfiguration configuration;

	private ProducerDestination destination;

	private ProducerProperties properties;

	private CompositeMessageConverterFactory converterFactory;

	private BigQueryConverter bigQueryConverter = new JacksonConverter();

	public BigQueryMessageHandler(BigQueryConfiguration configuration, ProducerDestination destination, ProducerProperties producerProperties,
			MessageChannel errorChannel) {
		this.configuration = configuration;
		this.destination = destination;
		this.properties = producerProperties;
		bigQuery = configuration.getBigQuery();
		withTable = new WithTable(configuration, destination.getName());
		tableId = withTable.getTableId();
		this.converterFactory = configuration.getConverterFactory();
	}

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		MessageConverter json = converterFactory.getMessageConverterForType(MimeType.valueOf("application/json"));
		final JsonNode jsonMessage = (JsonNode) json.fromMessage(message, JsonNode.class);
		withTable.whenTableExists(jsonMessage, table -> {
			Map<String, Object> bigQueryRow = bigQueryConverter.toBigQueryRow(jsonMessage);
			InsertAllResponse response = bigQuery.insertAll(InsertAllRequest.newBuilder(tableId)
							.addRow(bigQueryRow)
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

}
