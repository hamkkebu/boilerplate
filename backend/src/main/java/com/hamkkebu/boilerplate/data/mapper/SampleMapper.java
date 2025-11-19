package com.hamkkebu.boilerplate.data.mapper;

import com.hamkkebu.boilerplate.data.dto.SampleRequest;
import com.hamkkebu.boilerplate.data.dto.SampleResponse;
import com.hamkkebu.boilerplate.data.entity.Sample;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SampleMapper {

    @Mapping(source = "username", target = "sampleId")
    @Mapping(source = "firstName", target = "sampleFirstName")
    @Mapping(source = "lastName", target = "sampleLastName")
    @Mapping(source = "email", target = "sampleEmail")
    @Mapping(source = "phone", target = "samplePhone")
    @Mapping(source = "password", target = "samplePassword")
    @Mapping(source = "nickname", target = "sampleNickname")
    @Mapping(source = "country", target = "sampleCountry")
    @Mapping(source = "city", target = "sampleCity")
    @Mapping(source = "state", target = "sampleState")
    @Mapping(source = "street1", target = "sampleStreet1")
    @Mapping(source = "street2", target = "sampleStreet2")
    @Mapping(source = "zip", target = "sampleZip")
    Sample toEntity(SampleRequest dto);

    @Mapping(source = "sampleNum", target = "id")
    @Mapping(source = "sampleId", target = "username")
    @Mapping(source = "sampleFirstName", target = "firstName")
    @Mapping(source = "sampleLastName", target = "lastName")
    @Mapping(source = "sampleEmail", target = "email")
    @Mapping(source = "samplePhone", target = "phone")
    @Mapping(source = "sampleNickname", target = "nickname")
    @Mapping(source = "sampleCountry", target = "country")
    @Mapping(source = "sampleCity", target = "city")
    @Mapping(source = "sampleState", target = "state")
    @Mapping(source = "sampleStreet1", target = "street1")
    @Mapping(source = "sampleStreet2", target = "street2")
    @Mapping(source = "sampleZip", target = "zip")
    SampleResponse toDto(Sample entity);
}
