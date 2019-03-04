package com.ndx.example.bigquery.binder;

import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

import com.ndx.example.bigquery.logic.WithDataset;

public class BigQueryProvisioningProvider implements ProvisioningProvider<ConsumerProperties, ProducerProperties>{
	private static class BigQueryProducerDestination implements ProducerDestination {

		private String name;

		public BigQueryProducerDestination(String name, ProducerProperties properties) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getNameForPartition(int partition) {
			return name;
		}
		
	}

	private static class BigQueryConsumerDestination implements ConsumerDestination {

		private String name;

		public BigQueryConsumerDestination(String name, ConsumerProperties properties) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
		
	}

	private BigQueryConfiguration configuration;

	public BigQueryProvisioningProvider(BigQueryConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * When producing a producer destination, we make sure the dataset exists.
	 * Table will be generated on first message sending according to format definition 
	 */
	@Override
	public ProducerDestination provisionProducerDestination(String name, ProducerProperties properties)
			throws ProvisioningException {
		return new WithDataset(configuration).whenDatasetExists(d -> new BigQueryProducerDestination(name, properties));
	}

	/**
	 * When producing a producer destination, we make sure the dataset exists.
	 */
	@Override
	public ConsumerDestination provisionConsumerDestination(String name, String group,
			ConsumerProperties properties) throws ProvisioningException {
		return new WithDataset(configuration).whenDatasetExists(d -> new BigQueryConsumerDestination(name, properties));
	}

}
