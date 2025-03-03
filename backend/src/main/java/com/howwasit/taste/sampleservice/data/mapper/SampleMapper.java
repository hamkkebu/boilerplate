package com.howwasit.taste.sampleservice.data.mapper;

import com.howwasit.taste.sampleservice.data.dto.RequestSample;
import com.howwasit.taste.sampleservice.data.dto.ResponseSample;
import com.howwasit.taste.sampleservice.data.entity.Sample;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SampleMapper {

    Sample toEntity(RequestSample dto);

    ResponseSample toDto(Sample entity);
}
