package com.ndx.example.bigquery.binder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.converter.CompositeMessageConverterFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.TransportOptions;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.http.HttpTransportOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@ConfigurationProperties(prefix = BigQueryConfiguration.PREFIX)
public class BigQueryConfiguration {
	static final String PREFIX = "spring.bigquery";
	private Optional<String> auth = Optional.empty();
	@Value("${:classpath:bigquery.json}")
	Resource authResource;
	private Optional<BigQuery> bigQuery= Optional.empty();
	/**
	 * Used dataset
	 */
	@Value("${"+PREFIX+".dataset}")
	private String dataset;
	
	private Optional<String> projectId = Optional.empty();
	private Optional<JsonObject> schema = Optional.empty();
	
	@Value("${:classpath:schema.json}")
	Resource schemaResource;
	private Optional<JsonObject> tableDefinition = Optional.empty();
	
	@Value("${:classpath:table-definition.json}")
	Resource tableDefinitionResource;

	/**
	 * Default table to use, can be overwritten in binding
	 */
	@Value("${"+PREFIX+".table}")
	private String table;

	private Optional<TableId> tableId = Optional.empty();
	private CompositeMessageConverterFactory converterFactory = new CompositeMessageConverterFactory();

	public BigQuery getBigQuery() {
		if(!bigQuery.isPresent()) {
			bigQuery = Optional.of(loadBigQuery());
		}
		return bigQuery.get();
	}
	
	public String getAuth() {
		if(!auth.isPresent()) {
			auth = Optional.of(loadStringFrom(authResource));
		}
		return auth.get();
	}
	
	public String getProjectId() {
		if(!projectId.isPresent()) {
			JsonObject auth = new JsonParser().parse(getAuth()).getAsJsonObject();
			projectId = Optional.of(auth.get("project_id").getAsString());
		}
		return projectId.get();
	}
	
	public Optional<JsonObject> getTableDefinition() {
		if(!tableDefinition.isPresent()) {
			if(tableDefinitionResource.isFile() && tableDefinitionResource.exists() && tableDefinitionResource.isReadable()) {
				tableDefinition = Optional.of(loadJsonFrom(tableDefinitionResource));
			}
		}
		return tableDefinition;
	}
	
	public Optional<JsonObject> getSchema() {
		if(!schema.isPresent()) {
			if(schemaResource.isFile() && schemaResource.exists() && schemaResource.isReadable()) {
				schema = Optional.of(loadJsonFrom(schemaResource));
			}
		}
		return schema;
	}
	
	private BigQuery loadBigQuery() {
		TransportOptions transportOptions = HttpTransportOptions.newBuilder()
				.setHttpTransportFactory(new HttpTransportOptions.DefaultHttpTransportFactory()).build();
		String bigqueryConfig = getAuth();
		try (ByteArrayInputStream credentialsStream = new ByteArrayInputStream(bigqueryConfig.getBytes())) {
			GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
			return BigQueryOptions.newBuilder().setProjectId(getProjectId())
					.setTransportOptions(transportOptions).setCredentials(credentials).build().getService();
		} catch (IOException e) {
			try {
				throw new RuntimeException(String.format("Can't read Google BigQuery credentials from %s", authResource.getFile().getAbsolutePath()), e);
			} catch (IOException e1) {
				throw new RuntimeException("Can't read Google BigQuery credentials from a file which doesn't seems to exist ?", e);
			}
		}
	}

	private JsonObject loadJsonFrom(Resource resource) {
		return new JsonParser().parse(loadStringFrom(resource)).getAsJsonObject();
	}

	private String loadStringFrom(Resource resource) {
		try {
			return StreamUtils.copyToString(resource.getInputStream(), Charset.forName("UTF-8"));
		} catch (IOException e) {
			throw new UnsupportedOperationException("can't read from "+resource.getFilename());
		}
	}

	public TableId getTableId() {
		if(!tableId.isPresent()) {
			tableId = Optional.of(TableId.of(getProjectId(), dataset, table));
		}
		return tableId.get();
	}

	public CompositeMessageConverterFactory getConverterFactory() {
		return converterFactory;
	}
}