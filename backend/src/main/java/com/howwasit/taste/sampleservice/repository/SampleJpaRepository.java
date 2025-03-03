package com.howwasit.taste.sampleservice.repository;

import com.howwasit.taste.sampleservice.data.entity.Sample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SampleJpaRepository extends JpaRepository<Sample, Long> {

    Optional<Sample> findBySampleId(String sampleId);
}
