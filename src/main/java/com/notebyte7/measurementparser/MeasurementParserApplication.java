package com.notebyte7.measurementparser;

import com.notebyte7.measurementparser.fileParser.FileParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class MeasurementParserApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(MeasurementParserApplication.class, args);

		FileParser.parseList();
	}

}
