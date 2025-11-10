package com.hamkkebu.boilerplate.controller;

import com.hamkkebu.boilerplate.data.dto.RequestSample;
import com.hamkkebu.boilerplate.data.dto.ResponseSample;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.hamkkebu.boilerplate.service.SampleService;

import java.util.List;

@RequestMapping("/boilerplate/sample")
@RequiredArgsConstructor
@RestController
public class SampleController {

    private final SampleService service;

    @PostMapping("/signup")
    public @ResponseBody ResponseSample createSample(@RequestBody RequestSample requestDto) {
        return service.createSample(requestDto);
    }

    @GetMapping("/info/{sampleId}")
    public @ResponseBody ResponseSample getSampleInfo(@PathVariable String sampleId) {
        return service.getSampleInfo(sampleId);
    }

    @GetMapping("/info/all")
    public @ResponseBody List<ResponseSample> getAllSampleInfo() {
        return service.getAllSampleInfo();
    }

    @DeleteMapping("/deactivate/{sampleId}")
    public @ResponseBody void deleteSample(@PathVariable String sampleId) {
        service.deleteSample(sampleId);
    }

}
