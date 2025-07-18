package com.example.onlybuns.controller;

import com.example.onlybuns.listener.CareLocationListener;
import com.example.onlybuns.model.CareLocation;
import org.springframework.web.bind.annotation.*;
import com.example.onlybuns.repository.CareLocationRepository;

import java.util.List;

@RestController
@RequestMapping("/api/care-locations")
public class CareLocationController {

    private final CareLocationRepository repository;

    public CareLocationController(CareLocationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<CareLocation> getSveLokacije() {
        return repository.findAll();
    }
}
