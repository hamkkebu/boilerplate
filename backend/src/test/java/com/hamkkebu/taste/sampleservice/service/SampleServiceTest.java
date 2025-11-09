package com.hamkkebu.taste.sampleservice.service;

import com.hamkkebu.boilerplate.data.dto.RequestSample;
import com.hamkkebu.boilerplate.data.dto.ResponseSample;
import com.hamkkebu.boilerplate.data.entity.Sample;
import com.hamkkebu.boilerplate.data.mapper.SampleMapper;
import com.hamkkebu.boilerplate.repository.SampleJpaRepository;
import com.hamkkebu.boilerplate.service.SampleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SampleServiceTest {

    @InjectMocks
    SampleService service;

    @Mock
    SampleJpaRepository repository;

    @Mock
    SampleMapper mapper;

    @Test
    @DisplayName("회원 가입 테스트")
    void createSample() {

        // given
        String sampleId = anyString();
        RequestSample requestDto = RequestSample.builder()
                .sampleId(sampleId)
                .build();

        Sample entity = mapper.toEntity(requestDto);

        given(repository.save(entity)).willReturn(entity);

        // when
        ResponseSample responseDto = service.createSample(requestDto);

        // then
        assertThat(responseDto.getSampleId()).isEqualTo(requestDto.getSampleId());
    }

    @Test
    @DisplayName("특정 회원 조회 테스트")
    void getSampleInfo() {

        // given
        String sampleId = anyString();
        Sample entity = Sample.builder()
                .sampleId(sampleId)
                .build();

        given(repository.findBySampleId(sampleId)).willReturn(Optional.of(entity));

        // when
        ResponseSample responseDto = service.getSampleInfo(sampleId);

        // then
        assertThat(responseDto.getSampleId()).isEqualTo(entity.getSampleId());
    }

    @Test
    @DisplayName("전체 회원 조회 테스트")
    void getAllSampleInfo() {

        // given
        List<Sample> entityList = new ArrayList<>();

        Sample entity1 = Sample.builder()
                .sampleId("k1m743hyun")
                .build();
        entityList.add(entity1);

        Sample entity2 = Sample.builder()
                .sampleId("hyun743k1m")
                .build();
        entityList.add(entity2);

        given(repository.findAll()).willReturn(entityList);

        // when
        List<ResponseSample> result = service.getAllSampleInfo();


        // then
        assertThat(result.size()).isEqualTo(entityList.size());
    }
}