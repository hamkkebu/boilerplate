package com.hamkkebu.boilerplate.listener;

import com.hamkkebu.boilerplate.data.event.SampleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SampleListener {

    @Async
    @EventListener
    public void getEvent(SampleEvent event) {
        log.info("[Sample Service] Event 수신 완료");
    }
}
