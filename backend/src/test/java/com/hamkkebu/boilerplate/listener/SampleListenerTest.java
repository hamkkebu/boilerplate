package com.hamkkebu.boilerplate.listener;

import com.hamkkebu.boilerplate.data.event.SampleEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SampleListener 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class SampleListenerTest {

    @InjectMocks
    private SampleListener sampleListener;

    @Test
    @DisplayName("샘플 이벤트 처리")
    void handleSampleEvent() {
        // given
        SampleEvent event = SampleEvent.builder()
            .userId("user-123")
            .build();

        // when & then - 예외 발생하지 않음 확인
        sampleListener.handleSampleEvent(event);
    }
}
