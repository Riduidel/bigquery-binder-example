package com.ndx.example.bigquery.binder;

import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.messaging.MessageChannel;

public class BigQueryConsumerBinding implements Binding<MessageChannel> {

	public BigQueryConsumerBinding(String name, String group, MessageChannel inboundBindTarget,
			ConsumerProperties consumerProperties) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void unbind() {
		// TODO Auto-generated method stub

	}

}
