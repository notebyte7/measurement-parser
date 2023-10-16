package com.notebyte7.measurementparser.storage;

import com.notebyte7.measurementparser.model.Meas;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasStorage extends JpaRepository<Meas, Integer> {
}
