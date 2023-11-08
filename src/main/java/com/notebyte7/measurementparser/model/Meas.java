package com.notebyte7.measurementparser.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import javax.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meas", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Meas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meas_id")
    private Long id;
    @Column(name = "datetime")
    LocalDateTime dateTime;
    @Column(name = "lon")
    Double longitude;
    @Column(name = "lat")
    Double latitude;
    @Column(name = "system")
    Integer system;
    @Column(name = "cell_type")
    Integer cellType;
    @Column(name = "band")
    String band;
    @Column(name = "channel")
    Integer channel;
    @Column(name = "identity")
    Integer identity;
    @Column(name = "power")
    Double power;
    @Column(name = "quality")
    Double quality;

    public Meas(LocalDateTime dateTime, Double longitude, Double latitude, Integer system, Integer cellType, String band, Integer channel, Integer identity, Double power, Double quality) {
        this.dateTime = dateTime;
        this.longitude = longitude;
        this.latitude = latitude;
        this.system = system;
        this.cellType = cellType;
        this.band = band;
        this.channel = channel;
        this.identity = identity;
        this.power = power;
        this.quality = quality;
    }
}
