package com.howwasit.taste.sampleservice.service;

import java.util.List;

import com.howwasit.taste.sampleservice.data.dto.RequestSample;
import com.howwasit.taste.sampleservice.data.dto.ResponseSample;
import com.howwasit.taste.sampleservice.data.entity.Sample;
import com.howwasit.taste.sampleservice.data.event.SampleEvent;
import com.howwasit.taste.sampleservice.data.mapper.SampleMapper;
import com.howwasit.taste.sampleservice.repository.SampleJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SampleService {

    private final SampleMapper mapper;
    private final SampleJpaRepository repository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public ResponseSample createSample(RequestSample requestDto) {

        Sample entity = mapper.toEntity(requestDto);
        repository.save(entity);
        publisher.publishEvent(new SampleEvent(entity.getSampleId(), entity.getSampleFname(), entity.getSampleLname()));

        return mapper.toDto(entity);
    }

    public ResponseSample getSampleInfo(String sampleId) {
        return mapper.toDto(repository.findBySampleId(sampleId).orElseThrow());
    }

    public List<ResponseSample> getAllSampleInfo() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }
}
