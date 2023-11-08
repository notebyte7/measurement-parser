package com.notebyte7.measurementparser.service;

import com.notebyte7.measurementparser.exception.NotFoundException;
import com.notebyte7.measurementparser.model.GPS;
import com.notebyte7.measurementparser.model.Meas;
import com.notebyte7.measurementparser.repository.MeasRepository;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MeasService {
    private static MeasRepository measRepository;

    public MeasService(MeasRepository measRepository) {
        MeasService.measRepository = measRepository;
    }

    public void parseAllFilesFromFolder(String folder) {
        long startTime = System.currentTimeMillis();
        List<File> filesList = getAllFilesFromFolder(folder);
        System.out.println(filesList);
        System.out.println("\n");
        unzipFilesFromFolder(folder);
        filesList = getAllFilesFromFolder(folder);
        System.out.println(filesList);

        filesList
                .forEach(this::parseFile);
        System.out.print("\nОбщее время: " + ((double) (System.currentTimeMillis() - startTime) / 1000) + " сек.\n");

    }


    private void unzipFilesFromFolder(String measFolder) {
        try {
            Files.walk(Paths.get(measFolder))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> (file.getName().endsWith(".zip")))
                    .forEach(this::unzipFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void unzipFile(File file) {
        String[] name = file.getName().split(".zip");
        String folder = file.getParent();
        try {
            ZipFile zipFile = new ZipFile(file);
            List<FileHeader> fileList= zipFile.getFileHeaders();
            if (fileList.size() <= 2) {
                zipFile.extractAll(folder + "/");
            } else {
                zipFile.extractAll(folder + "/" + name[0]);
            }

        } catch (IOException e) {
            throw new NotFoundException("Ошибка разархивирования");
        }
    }

    private List<File> getAllFilesFromFolder(String measFolder){
        List<File> allFiles;
        try {
            allFiles = Files.walk(Paths.get(measFolder))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> (file.getName().endsWith(".nmf")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new NotFoundException("Ошибка в адресе каталога");
        }

        return allFiles;
    }

    private void parseFile(File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            long startTime = System.currentTimeMillis();
            System.out.println("\nИмя файла: " + file.getName());

            List<Meas> measList = getMeasList(reader);
            System.out.println("Количество измерений: " + measList.size());
            System.out.print("Время парсинга: " + ((double) (System.currentTimeMillis() - startTime) / 1000) + " сек.\n");
            System.out.print("Время сохранения: ");

            measRepository.saveAll(measList);
            System.out.print(((double) (System.currentTimeMillis() - startTime) / 1000) + " сек.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Meas> getMeasList(BufferedReader reader) throws IOException {
        List<Meas> measList = new ArrayList<>();
        Map<LocalDateTime, GPS> gpsMap = new HashMap<>();
        String line;
        List<String> split;
        LocalDate date = null;

        outerLoop:
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
                        Meas meas = getMeas(split, dateTime, gps);
                        if (meas != null) {
                            measList.add(meas);
                        }
                    }
                    break;
                }
                case "OFDMSCAN": {
                    break outerLoop;
                }
            }
        }
        return measList;
    }

    private Meas getMeas(List<String> split, LocalDateTime dateTime, GPS gps) {
        Integer system = Integer.parseInt(split.get(3));
        Integer cellType;
        String band;
        Integer channel;
        Integer identity;
        Double power = null;
        Double quality = null;
        int numCell = 0;
        int numParam = 0;
        int firstValue = 0;

        switch (system) {
            case 5: {
                int tempNum = Integer.parseInt(split.get(5)) * Integer.parseInt(split.get(6));
                numCell = Integer.parseInt(split.get(7 + tempNum));
                numParam = Integer.parseInt(split.get(8 + tempNum));
                firstValue = 9 + tempNum;
                break;
            }
            case 7:
            case 8:
            case 1: {
                numCell = Integer.parseInt(split.get(5));
                numParam = Integer.parseInt(split.get(6));
                firstValue = 7;
                break;
            }
        }

        for (int i = 0; i < numCell; i++) {
            int num = firstValue + (i * numParam);
            if (split.size() > (7 + num)) {
                cellType = getParseInt(split.get(num));
                band = getBand(split.get(1 + num));
                channel = getParseInt(split.get(2 + num));
                identity = getParseInt(split.get(3 + num));
                switch (split.get(3)) {
                    case "7":
                    case "8": {
                        power = getParseDouble(split.get(5 + num));
                        if (split.size() > (10 + num)) {
                            quality = getParseDouble(split.get(10 + num));
                        }
                        break;
                    }
                    case "5": {
                        power = getParseDouble(split.get(6 + num));
                        quality = getParseDouble(split.get(4 + num));
                        break;
                    }
                    case "1": {
                        power = getParseDouble(split.get(5 + num));
                        break;
                    }
                }
                if (dateTime != null && gps != null && cellType != null && channel != null && identity != null
                        && power != null) {
                    if (system.equals(7) && cellType.equals(0) ||
                            system.equals(8) && cellType.equals(0) ||
                            system.equals(5) && cellType.equals(0) ||
                            system.equals(1) && cellType.equals(1)) {
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
                        return meas;
                    }
                }
            }
        }
        return null;
    }

    private Double getParseDouble(String s) {
        if (!s.equals("")) {
            return Double.parseDouble(s);
        } else {
            return null;
        }
    }

    private Integer getParseInt(String s) {
        if (!s.equals("")) {
            return Integer.parseInt(s);
        } else {
            return null;
        }
    }

    private String getBand(String band) {
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
}
