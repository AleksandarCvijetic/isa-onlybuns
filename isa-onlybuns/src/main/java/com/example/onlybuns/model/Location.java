package com.example.onlybuns.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Location {

    private String address;
    private String city;
    private String country;
    private double latitude;
    private double longitude;

}
