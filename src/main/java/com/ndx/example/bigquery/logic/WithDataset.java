package com.ndx.example.bigquery.logic;

import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.DatasetListOption;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.TableId;
import com.ndx.example.bigquery.binder.BigQueryConfiguration;

/**
 * A class guaranteeing any code given in will run in an existing dataset provided at creation time
 * @author nicolas-delsaux
 *
 */
public class WithDataset {
	private static final Logger logger = LoggerFactory
			.getLogger(WithDataset.class.getName());

	private BigQuery bigQuery;
	private TableId tableId;
	private Optional<Dataset> dataset = Optional.empty();

	public WithDataset(BigQueryConfiguration configuration) {
		this(configuration.getBigQuery(), configuration.getTableId());
	}

	public WithDataset(BigQuery bigQuery, TableId tableId) {
		this.bigQuery = bigQuery;
		this.tableId = tableId;
	}

	public <Type> Type whenDatasetExists(Function<Dataset, Type> andAfter) {
		if(!dataset.isPresent()) {
			dataset = Optional.of(ensureDatasetExists());
		}
		return andAfter.apply(dataset.get());
	}

	private Dataset ensureDatasetExists() {
		Page<Dataset> datasets = bigQuery.listDatasets(DatasetListOption.all());
		for (Dataset d : datasets.iterateAll()) {
			logger.debug(String.format("testing dataset %s", d));
			if (tableId.getDataset().equals(d.getDatasetId().getDataset())) {
				return d;
			}
		}
		// If we're here, it means the dataset was not found. So let's create it!
		DatasetInfo datasetInfo = DatasetInfo.newBuilder(tableId.getDataset()).build();
		try {
			return bigQuery.create(datasetInfo);
		} catch (BigQueryException e) {
			logger.error(String.format("Unable to create dataset %s", tableId.getDataset()), e);
			throw new UnsupportedOperationException("Can't create dataset. Should shut down", e);
		}
	}

}
