package com.notebyte7.measurementparser.fileParser;

import com.notebyte7.measurementparser.exception.NotFoundException;
import com.notebyte7.measurementparser.model.GPS;
import com.notebyte7.measurementparser.model.GpsAndTime;
import com.notebyte7.measurementparser.model.Meas;
import com.notebyte7.measurementparser.storage.MeasStorage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class FileParser {
    MeasStorage measStorage;
    private static final String testPath = "/Users/ilya/Downloads/veshki_23Oct09_145535.3.nmf";
    private static final String testPath2 = "/Users/ilya/Downloads/BENCH_MSK_Balashiha_23Jun27 090030.17.nmf";

    private BufferedReader getBufferedReader(String path) throws FileNotFoundException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            return reader;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new FileNotFoundException("Файл не найден");
    }

    private LocalDate getMeasurementDate(BufferedReader reader) throws IOException {
        String line;
        List<String> split;
        LocalDate date = null;
        while ((line = reader.readLine()) != null) {
            split = Arrays.asList(line.split(","));
            if (split.get(0).equals("#START")) {
                date = LocalDate.parse(split.get(3));
                return date;
            }
            break;
        }
        throw new NotFoundException("Дата не найдена");
    }

    public List<String> getEditedList(BufferedReader reader) throws IOException {
        LocalDate date = getMeasurementDate(reader);
        Map<LocalDateTime, GPS> gpsMap = new HashMap<>();
        List<String> editedList = new ArrayList<>();
        String line;
        List<String> split;
        while ((line = reader.readLine()) != null) {
            split = Arrays.asList(line.split(","));
            if (split.get(0).equals("GPS")) {
                LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.parse(split.get(1)).withNano(0));
                GPS gps = new GPS(Double.valueOf(split.get(3)), Double.valueOf(split.get(4)));
                gpsMap.put(dateTime, gps);
                editedList.add(line);
            } else if (split.get(0).equals("CELLMEAS")) {
                LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.parse(split.get(1)).withNano(0));
                int numCell = Integer.parseInt(split.get(5));
                int numParam = Integer.parseInt(split.get(6));
                String system = getSystem(split.get(3));
                if (gpsMap.containsKey(dateTime)) {
                    GPS gps = gpsMap.get(dateTime);
                    for (int i = 1; i <= numCell; i = i + numParam) {
                        Meas meas = new Meas(
                                dateTime,
                                gps.getLongitude(),
                                gps.getLatitude(),
                                system,
                                Integer.parseInt(split.get(6 + i)),
                                getBand(split.get(7 + i)),
                                Integer.parseInt(split.get(8 + i)),
                                Integer.parseInt(split.get(9 + i)),
                                Double.parseDouble(split.get(10 + i)),
                                Double.parseDouble(split.get(16 + i))
                        );
                        System.out.println(meas);
                    }
                }

            }
        }
        return editedList;
    }

    private String getSystem(String num) {
        switch (num) {
            case "1":
                return "GSM";
            case "5":
                return "UMTS";
            case "7":
                return "LTE FDD";
            case "8":
                return "LTE TDD";
        }
        throw new IllegalArgumentException("Технология не соответствует  принятому списку - " + num);
    }

    private String getBand(String num) {
        switch (num) {
            case "10900":
                return "GSM 900";
            case "11800":
                return "DCS 1800";
            case "50001":
                return "UMTS 2100";
            case "50008":
                return "UMTS 900";
            case "70001":
                return "LTE 2100";
            case "70003":
                return "LTE 1800";
            case "70007":
                return "LTE 2600";
            case "70008":
                return "LTE 900";
            case "70020":
                return "LTE 800";
            case "70034":
                return "LTE TDD 2010-2025";
            case "70038":
                return "LTE TDD 2570-2620";
            case "70040":
                return "LTE TDD 2300-2400";
        }
        throw new IllegalArgumentException("Технология не соответствует принятому списку - " + num);
    }

    public static void parseList() throws IOException {
        BufferedReader reader = getBufferedReader(testPath);
        long startTime = System.currentTimeMillis();
        List<String> editedList = getEditedList(reader);

        System.out.print("Время парсинга: " + ((double) (System.currentTimeMillis() - startTime) / 1000) + " сек.\n" + editedList.size());
    }


}
