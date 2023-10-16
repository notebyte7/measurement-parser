package com.notebyte7.measurementparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GpsAndTime {
    Double longitude;
    Double latitude;
    LocalDateTime dateTime;
}
