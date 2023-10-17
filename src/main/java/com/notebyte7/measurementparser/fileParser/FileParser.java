package com.notebyte7.measurementparser.fileParser;

import com.notebyte7.measurementparser.model.GPS;
import com.notebyte7.measurementparser.model.Meas;
import com.notebyte7.measurementparser.repository.MeasRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FileParser {
    private static List<Meas> measList = new ArrayList<>();
    private static MeasRepository measRepository;
    private static final String testPath = "/Users/ilya/Downloads/veshki_23Oct09_145535.3.nmf";
    private static final String testPath2 = "/Users/ilya/Downloads/BENCH_MSK_Balashiha_23Jun27 090030.17.nmf";
    public FileParser(MeasRepository measRepository) {
        FileParser.measRepository = measRepository;
    }

    private static void getBufferedReader(String path) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            getEditedList(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getEditedList(BufferedReader reader) throws IOException {
        Map<LocalDateTime, GPS> gpsMap = new HashMap<>();
        List<String> editedList = new ArrayList<>();
        String line;
        List<String> split;
        LocalDate date = null;

        while ((line = reader.readLine()) != null) {
            split = Arrays.asList(line.split(","));
            switch (split.get(0)) {
                case "#START": {
                    String dirtyDate = split.get(3);
                    dirtyDate = dirtyDate.substring(1, dirtyDate.length() - 1);
                    date = LocalDate.parse(dirtyDate, DateTimeFormatter.ofPattern("dd.M.yyy"));
                    break;
                }

                case "GPS": {
                    LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.parse(split.get(1)).withNano(0));
                    GPS gps = new GPS(Double.valueOf(split.get(3)), Double.valueOf(split.get(4)));
                    gpsMap.put(dateTime, gps);
                    editedList.add(line);
                    break;
                }
                case "CELLMEAS": {
                    LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.parse(split.get(1)).withNano(0));
                    GPS gps = null;
                    if (gpsMap.containsKey(dateTime)) {
                        gps = gpsMap.get(dateTime);
                    } else if (gpsMap.containsKey(dateTime.minusSeconds(1))) {
                        gps = gpsMap.get(dateTime.minusSeconds(1));
                    }
                    if (gps != null && split.size() > 16) {
                        getMeas(split, dateTime, gps);
                    }
                    break;
                }
            }
        }
        return editedList;
    }

    private static void getMeas(List<String> split, LocalDateTime dateTime, GPS gps) {
        String system = getSystem(split.get(3));
        Integer cellType;
        String band;
        Integer channel;
        Integer identity;
        Double power;
        Double quality = null;

        switch (split.get(3)) {
            case "7":
            case "8": {
                int numCell = Integer.parseInt(split.get(5));
                int numParam = Integer.parseInt(split.get(6));
                for (int i = 0; i < numCell; i++) {
                    int num = 6 + (i * numParam);
                    if (split.size() > (7 + num)) {
                        cellType = getParseInt(split.get(1 + num));
                        band = getBand(split.get(2 + num));
                        channel = getParseInt(split.get(3 + num));
                        identity = getParseInt(split.get(4 + num));
                        power = getParseDouble(split.get(6 + num));
                        if (split.size() > (11 + num)) {
                            quality = getParseDouble(split.get(11 + num));
                        }
                        saveMeas(dateTime, gps, system, cellType, band, channel, identity, power, quality);
                    }
                }
                break;
            }
            case "5": {
                int tempNum = Integer.parseInt(split.get(5)) * Integer.parseInt(split.get(6));
                int numCell = Integer.parseInt(split.get(7 + tempNum));
                int numParam = Integer.parseInt(split.get(8 + tempNum));
                for (int i = 0; i < numCell; i++) {
                    int num = 8 + tempNum + (i * numParam);
                    if (split.size() > (7 + num)) {
                        cellType = getParseInt(split.get(1 + num));
                        band = getBand(split.get(2 + num));
                        channel = getParseInt(split.get(3 + num));
                        identity = getParseInt(split.get(4 + num));
                        power = getParseDouble(split.get(7 + num));
                        quality = getParseDouble(split.get(5 + num));
                        saveMeas(dateTime, gps, system, cellType, band, channel, identity, power, quality);
                    }
                }
                break;
            }
            case "1": {
                int numCell = Integer.parseInt(split.get(5));
                int numParam = Integer.parseInt(split.get(6));
                for (int i = 0; i < numCell; i++) {
                    int num = 6 + (i * numParam);
                    if (split.size() > (7 + num)) {
                        cellType = getParseInt(split.get(1 + num));
                        band = getBand(split.get(2 + num));
                        channel = getParseInt(split.get(3 + num));
                        identity = getParseInt(split.get(4 + num));
                        power = getParseDouble(split.get(6 + num));
                        saveMeas(dateTime, gps, system, cellType, band, channel, identity, power, quality);
                    }
                }
                break;
            }
        }
    }

    private static void saveMeas(LocalDateTime dateTime, GPS gps, String system, Integer cellType, String band, Integer channel, Integer identity, Double power, Double quality) {
        if (dateTime != null && gps != null && cellType != null && channel != null && identity != null && power != null) {
            Meas meas = new Meas(
                    dateTime,
                    gps.getLongitude(),
                    gps.getLatitude(),
                    system,
                    cellType,
                    band,
                    channel,
                    identity,
                    power,
                    quality
            );
            measList.add(meas);
        }
    }

    private static Double getParseDouble(String s) {
        if (!s.equals("")) {
            return Double.parseDouble(s);
        } else {
            return null;
        }
    }

    private static Integer getParseInt(String s) {
        if (!s.equals("")) {
            return Integer.parseInt(s);
        } else {
            return null;
        }
    }

    private static String getSystem(String num) {
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

    private static String getBand(String band) {
        switch (band) {
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
            case "80034":
                return "LTE TDD 2010-2025";
            case "80038":
                return "LTE TDD 2570-2620";
            case "80040":
                return "LTE TDD 2300-2400";
        }
        throw new IllegalArgumentException("Технология не соответствует принятому списку - " + band);
    }

    public static void parseList() throws IOException {
        long startTime = System.currentTimeMillis();
        getBufferedReader(testPath2);
        System.out.println("Количество измерений: " + measList.size());
        measRepository.saveAll(measList);
        System.out.print("Время парсинга: " + ((double) (System.currentTimeMillis() - startTime) / 1000) + " сек.\n");
    }


}
