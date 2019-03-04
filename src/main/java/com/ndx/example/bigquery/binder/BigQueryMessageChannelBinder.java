package com.ndx.example.bigquery.binder;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

public class BigQueryMessageChannelBinder
	extends AbstractMessageChannelBinder<ConsumerProperties, ProducerProperties, BigQueryProvisioningProvider>
	implements Binder<MessageChannel, ConsumerProperties, ProducerProperties>{

	private BigQueryConfiguration configuration;

	public BigQueryMessageChannelBinder(BigQueryConfiguration configuration, BigQueryProvisioningProvider provisioningProvider) {
		super(new String[0], provisioningProvider);
		this.configuration = configuration;
	}

	@Override
	protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
			ProducerProperties producerProperties, MessageChannel errorChannel) throws Exception {
		return new BigQueryMessageHandler(configuration, destination, producerProperties, errorChannel);
	}

	@Override
	protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
			ConsumerProperties properties) throws Exception {
		return new BigQueryMessageProducer(destination, group, properties);
	}
	
	
}
