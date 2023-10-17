package com.notebyte7.measurementparser.repository;

import com.notebyte7.measurementparser.model.Meas;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasRepository extends JpaRepository<Meas, Long> {
}
