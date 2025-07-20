package com.example.onlybuns.repository;

import com.example.onlybuns.model.CareLocation;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CareLocationRepository extends JpaRepository<CareLocation, Long>{
    
}
