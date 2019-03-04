package com.ndx.example.application;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@SpringBootApplication
@EnableBinding(Processor.class)
public class BigQueryBinderExampleApplication {
	private static final Logger logger = LoggerFactory.getLogger(BigQueryBinderExampleApplication.class);

	@Autowired
	MessageChannel output;

	/*
	 * @Bean public SchemaRegistryClient schemaRegistryClient(
	 * 
	 * @Value("${spring.cloud.stream.schemaRegistryClient.endpoint}") String
	 * endpoint) { ConfluentSchemaRegistryClient client = new
	 * ConfluentSchemaRegistryClient(); client.setEndpoint(endpoint); return client;
	 * }
	 */

	public static void main(String[] args) {
		SpringApplication.run(BigQueryBinderExampleApplication.class, args);
	}

	@StreamListener(Processor.INPUT)
	public void transmit(Message<?> message) {
		logger.info("#######################################################################################\n"
				+ "Received: " + message + "\n"
				+ "#######################################################################################\n");
		output.send(message);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			StringBuilder sout = new StringBuilder();
			sout.append("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				sout.append("\n\t").append(beanName);
			}
			
			logger.info(sout.toString());
		};
	}
}
