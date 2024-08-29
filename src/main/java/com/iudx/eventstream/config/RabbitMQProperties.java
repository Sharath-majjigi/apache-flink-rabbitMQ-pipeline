package com.iudx.eventstream.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rabbitmq-props")
public class RabbitMQProperties {
    private String host;
    private int port;
    private String username;
    private String password;
    private String virtualHost;
    private String queueName;
}
