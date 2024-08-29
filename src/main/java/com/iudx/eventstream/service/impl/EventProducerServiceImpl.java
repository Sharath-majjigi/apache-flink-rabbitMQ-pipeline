package com.iudx.eventstream.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iudx.eventstream.model.Event;
import com.iudx.eventstream.service.EventProducerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class EventProducerServiceImpl implements EventProducerService {

    RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 5000) // produce event every 5 seconds
    public void produceEvents(){
        produceEvent();
    }


    @Override
    public void produceEvent(){
        Event event = Event.builder()
        .userId(getUserId())
        .productId(UUID.randomUUID().toString())
        .timestamp(Instant.now())
        .sessionDuration(RandomUtils.nextInt(10,100))
        .eventType("pageView")
        .build();

        rabbitTemplate.convertAndSend("event_queue",event);
        System.out.println("Event pushed to queue");
    }


    /*
    * This is to test the result, if we produce random uuid everytime
    * we wil not be able to test the result.
    * */
    private final List<String> userIds = Arrays.asList(
            "b85ae944-3c64-4665-aea3-19d63d31abed",
            "51e5f9f9-4c19-4b58-ad93-4eda56d1ab10",
            "67a51318-ca28-4f39-bf30-7452fa31139b",
            "16b054f0-0494-47d5-9764-d05d3b5b51ba",
            "e32e081d-7443-4cb1-bc1e-c444f93583f0",
            "8c31fdea-53f9-4fb1-9c2f-f0b9cba632be",
            "7d5c1c13-4e8a-4b7e-b8e4-4d3f7688a786",
            "b029c670-1f39-487f-9d3c-61e3d7f6c8ef",
            "2f44186d-9631-4f59-ae50-7eecbe256e32",
            "a4bc1db9-0a95-43ea-9c4f-47c9c8f78c74"
    );

    private String getUserId(){
        Random random = new Random();
        int idx =  random.nextInt(userIds.size());
        return userIds.get(idx);
    }
}
