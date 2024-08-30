package com.iudx.eventstream.service.impl;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iudx.eventstream.config.PostgresqlDatasourceProperties;
import com.iudx.eventstream.config.RabbitMQProperties;
import com.iudx.eventstream.model.Event;
import com.iudx.eventstream.service.EventProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class EventProcessingServiceImpl implements EventProcessingService {

    @Autowired
    RabbitMQProperties rabbitMQProperties;

    @Autowired
    PostgresqlDatasourceProperties postgresqlDatasourceProperties;

    @Override
    public void processEvents(){

        try{
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            RMQConnectionConfig connectionConfig = buildRabbitMQProperties();

            DataStream<String> stream = env
                    .addSource(new RMQSource<>(
                            connectionConfig,
                            rabbitMQProperties.getQueueName(),
                            true,
                            new SimpleStringSchema()))
                    .setParallelism(1);

            /*
             * In this pipeline:
             * - The incoming messages from RabbitMQ are mapped to Event objects.
             * - A watermark strategy is applied based on the event's timestamp to handle event time processing.
             * - Events are grouped by the userId to aggregate session durations per user.
             * - The session durations are aggregated within a 5-minute window.
             * - The aggregated results, saved to a PostgresSQL database.
             * - Finally, the Flink job is executed to start processing the events.
             */
            stream
                    .map(message -> {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.registerModule(new JavaTimeModule());
                        Event event = objectMapper.readValue(message, Event.class);
                        // Convert timestamp from seconds to milliseconds
                        event.setTimestamp(Instant.ofEpochSecond(event.getTimestamp().toEpochMilli() * 1000));
                        System.out.println("Event consumed by flink: \n" + event);
                        return event;
                    })
                    .assignTimestampsAndWatermarks(
                            WatermarkStrategy
                                    .<Event>forBoundedOutOfOrderness(Duration.ofSeconds(30))
                                    .withTimestampAssigner((event, timestamp) -> event.getTimestamp().toEpochMilli())
                    )
                    .keyBy(Event::getUserId)
                    .timeWindow(Time.minutes(5))

                    .reduce((e1, e2) -> {
                        e1.setSessionDuration(e1.getSessionDuration() + e2.getSessionDuration());
                        return e1;
                    })

                    .addSink(JdbcSink.sink( "insert into user_sessions (user_id, session_duration) values (?, ?) on conflict(user_id) do update set session_duration = user_sessions.session_duration + excluded.session_duration",
                                    (statement, event) -> {
                                        statement.setString(1, event.getUserId());
                                        statement.setInt(2, event.getSessionDuration());
                                    },
                                    JdbcExecutionOptions.builder()
                                            .withBatchSize(1000)
                                            .withBatchIntervalMs(200)
                                            .withMaxRetries(5)
                                            .build(),
                                    new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                            .withUrl(postgresqlDatasourceProperties.getUrl())
                                            .withDriverName(postgresqlDatasourceProperties.getDriverClassName())
                                            .withUsername(postgresqlDatasourceProperties.getUsername())
                                            .withPassword(postgresqlDatasourceProperties.getPassword())
                                            .build()
                            ));

            env.execute("Flink Event Processing Job");

        }catch (Exception e){
            log.error("Error occurred while executing the pipeline: {}", e.getMessage());
        }

    }

    private  RMQConnectionConfig buildRabbitMQProperties() {
        return new RMQConnectionConfig.Builder()
                .setHost(rabbitMQProperties.getHost())
                .setVirtualHost(rabbitMQProperties.getVirtualHost())
                .setUserName(rabbitMQProperties.getUsername())
                .setPassword(rabbitMQProperties.getPassword())
                .setPort(rabbitMQProperties.getPort())
                .build();
    }

}
