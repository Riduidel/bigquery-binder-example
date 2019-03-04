package com.ndx.example.bigquery.binder;

import org.springframework.cloud.stream.binder.Binding;
import org.springframework.messaging.MessageChannel;

public class BigQueryProducerBinding implements Binding<MessageChannel> {

	@Override
	public void unbind() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method Binding<MessageChannel>#unbind(...) has not yet been implemented!");
	}
}
