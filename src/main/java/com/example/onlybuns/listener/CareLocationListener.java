package com.example.onlybuns.listener;

import com.example.onlybuns.model.CareLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.example.onlybuns.repository.CareLocationRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class CareLocationListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CareLocationRepository repository;

    public CareLocationListener(CareLocationRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = "care_locations_queue")
    public void handleMessage(String poruka) {
        try {
            CareLocation lokacija = objectMapper.readValue(poruka, CareLocation.class);
            System.out.println("[🟢] Primljena lokacija: " + lokacija.getNaziv());

            repository.save(lokacija); // snimi u bazu
        } catch (Exception e) {
            System.err.println("[🔴] Greska pri obradi poruke: " + poruka);
            e.printStackTrace();
        }
    }
}

