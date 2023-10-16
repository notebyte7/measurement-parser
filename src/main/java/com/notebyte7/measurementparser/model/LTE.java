package com.notebyte7.measurementparser.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LTE {
    Double longitude;
    Double latitude;
    LocalDateTime dateTime;
    String band;
    Integer channel;
    Integer pci;
    Double rsrp;
    Double sinr;
}
