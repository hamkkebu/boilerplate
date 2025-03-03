package com.howwasit.taste.sampleservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.howwasit.taste.sampleservice.data.dto.RequestSample;
import com.howwasit.taste.sampleservice.data.dto.ResponseSample;
import com.howwasit.taste.sampleservice.service.SampleService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SampleController.class)
class SampleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private SampleService service;

    private static final String BASE_URL = "/taste/sample";

    @Test
    @DisplayName("회원 가입 테스트")
    void createSample() throws Exception {

        // given
        String sampleId = anyString();

        RequestSample requestDto = RequestSample.builder()
                .sampleId(sampleId)
                .build();

        ResponseSample responseDto = ResponseSample.builder()
                .sampleId(sampleId)
                .build();

        // when
        when(service.createSample(requestDto)).thenReturn(responseDto);

        // then
        String body = mapper.writeValueAsString(requestDto);
        mockMvc.perform(post(BASE_URL + "/signup")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("특정 회원 정보 조회 테스트 - 성공")
    void getSampleInfoSuccess() throws Exception {

        // given
        String userId = "k1m743hyun";

        ResponseSample responseDto = ResponseSample.builder()
                .sampleId(userId)
                .build();

        // when
        when(service.getSampleInfo(userId)).thenReturn(responseDto);

        // then
        mockMvc.perform(get(BASE_URL + "/info/" + userId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 회원 정보 조회 테스트 - 실패 - null")
    void getSampleInfoFailureNull() throws Exception {

        // given
        String userId = "";
        ResponseSample responseDto = ResponseSample.builder()
                .sampleId(userId)
                .build();

        // when
        when(service.getSampleInfo(userId)).thenReturn(responseDto);

        // then
        mockMvc.perform(get(BASE_URL + "/info/" + userId))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("특정 회원 정보 조회 테스트 - 실패 - notnull")
    void getSampleInfoFailureNotNull() throws Exception {

        // given
        String userId = "hello";

        // when
        when(service.getSampleInfo(userId)).thenThrow(NoSuchElementException.class);

        // then
        Assertions.assertThatThrownBy(() -> mockMvc.perform(get(BASE_URL + "/info/" + userId))
                        .andDo(print())
                        .andExpect(status().is5xxServerError()))
                .hasCause(new NoSuchElementException());
    }

    @Test
    @DisplayName("전체 회원 정보 조회 테스트")
    void getAllSampleInfo() throws Exception {

        // given

        // when

        // then
        mockMvc.perform(get(BASE_URL + "/info/all"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}