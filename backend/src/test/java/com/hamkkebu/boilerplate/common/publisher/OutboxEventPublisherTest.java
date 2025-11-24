package com.hamkkebu.boilerplate.common.publisher;

import com.hamkkebu.boilerplate.data.entity.OutboxEvent;
import com.hamkkebu.boilerplate.data.event.SampleEvent;
import com.hamkkebu.boilerplate.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

/**
 * OutboxEventPublisher 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private OutboxEventPublisher outboxEventPublisher;

    @Test
    @DisplayName("이벤트 발행 - Outbox 테이블에 저장")
    void publish() {
        // given
        SampleEvent event = SampleEvent.builder()
            .userId("user-123")
            .build();
        given(outboxEventRepository.save(any(OutboxEvent.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // when
        outboxEventPublisher.publish("sample.events", event);

        // then
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }
}
