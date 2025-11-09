package com.hamkkebu.boilerplate.repository;

import com.hamkkebu.boilerplate.data.entity.Sample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SampleJpaRepository extends JpaRepository<Sample, Long> {

    Optional<Sample> findBySampleId(String sampleId);
}
