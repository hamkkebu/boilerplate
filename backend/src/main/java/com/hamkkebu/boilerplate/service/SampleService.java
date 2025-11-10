package com.hamkkebu.boilerplate.service;

import java.util.List;

import com.hamkkebu.boilerplate.data.dto.RequestSample;
import com.hamkkebu.boilerplate.data.dto.ResponseSample;
import com.hamkkebu.boilerplate.data.entity.Sample;
import com.hamkkebu.boilerplate.data.event.SampleEvent;
import com.hamkkebu.boilerplate.data.mapper.SampleMapper;
import com.hamkkebu.boilerplate.repository.SampleJpaRepository;
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

    @Transactional
    public void deleteSample(String sampleId) {
        repository.deleteBySampleId(sampleId);
    }
}
