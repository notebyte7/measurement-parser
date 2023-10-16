package com.notebyte7.measurementparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Meas {
    LocalDateTime dateTime;
    Double longitude;
    Double latitude;
    String system;
    Integer cellType;
    String band;
    Integer channel;
    Integer identity;
    Double power;
    Double quality;

    public Meas(LocalDateTime dateTime, Double longitude, Double latitude) {
        this.dateTime = dateTime;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
