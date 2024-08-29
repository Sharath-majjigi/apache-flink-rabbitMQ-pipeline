package com.iudx.eventstream;

import com.iudx.eventstream.config.RabbitMQProperties;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EventstreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventstreamApplication.class, args);
	}

}


