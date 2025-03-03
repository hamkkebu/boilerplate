package com.howwasit.taste.sampleservice.controller;

import com.howwasit.taste.sampleservice.data.dto.RequestSample;
import com.howwasit.taste.sampleservice.data.dto.ResponseSample;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.howwasit.taste.sampleservice.service.SampleService;

import java.util.List;

@RequestMapping("/taste/sample")
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

}
