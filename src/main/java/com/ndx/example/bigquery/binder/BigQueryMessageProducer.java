package com.ndx.example.bigquery.binder;

import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;

public class BigQueryMessageProducer implements MessageProducer {
	
	private MessageChannel channel;

	public BigQueryMessageProducer(ConsumerDestination destination, String group,
			ConsumerProperties properties) {
	}

	@Override
	public void setOutputChannel(MessageChannel outputChannel) {
		this.channel = outputChannel;
	}

	@Override
	public MessageChannel getOutputChannel() {
		return channel;
	}
}
