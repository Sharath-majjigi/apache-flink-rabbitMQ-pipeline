package com.iudx.eventstream.controller;

import com.iudx.eventstream.service.impl.EventProcessingServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consume-queue")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TestController {

    EventProcessingServiceImpl eventProcessingService;

    @GetMapping
    public void consumeEvent() throws Exception {
        eventProcessingService.processEvents();
    }

}
