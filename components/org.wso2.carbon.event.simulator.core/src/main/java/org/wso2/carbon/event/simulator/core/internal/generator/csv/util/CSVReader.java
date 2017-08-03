/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.simulator.core.internal.generator.csv.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;
import org.wso2.carbon.event.simulator.core.internal.bean.CSVSimulationDTO;
import org.wso2.carbon.event.simulator.core.internal.util.EventConverter;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;


/**
 * CSVReader class is used to read csv files
 */
public class CSVReader {
    private final Logger log = LoggerFactory.getLogger(CSVReader.class);
    private Reader fileReader = null;
    private BufferedReader bufferedReader = null;
    private CSVParser csvParser = null;
    private long lineNumber = 0;
    private long eventNumber = 0;

    /**
     * Constructor CSVReader is used to initialize an instance of class CSVReader
     * Initialize a file reader for the CSV file.
     * If the CSV file is ordered by timestamp it will create a bufferedReader for the file reader.
     * @throws ResourceNotFoundException if the CSV file is not found
     */
    public CSVReader(String fileName, boolean isOrdered) throws ResourceNotFoundException {
        try {
            String csvFileDirectory = EventSimulatorDataHolder.getInstance().getCsvFileDirectory();
            File csvFile = new File(Paths.get(csvFileDirectory, fileName).toString());
            if (csvFile.exists()) {
                if (csvFile.length() != 0) {
                    fileReader = new InputStreamReader(new FileInputStream(Paths.get(csvFileDirectory,
                            fileName).toString()), StandardCharsets.UTF_8);
                    if (log.isDebugEnabled()) {
                        log.debug("Initialize a File reader for CSV file '" + fileName + "'.");
                    }
                    if (isOrdered) {
                        bufferedReader = new BufferedReader(fileReader);
                    }
                } else {
                    throw new EventGenerationException("File '" + fileName + "' is empty.");
                }
            } else {
                throw new ResourceNotFoundException("File '" + fileName + "' cannot be found.",
                        ResourceNotFoundException.ResourceType.CSV_FILE, fileName);
            }
        } catch (IOException e) {
            log.error("Error occurred when initializing file reader for CSV file '" + fileName + "' : ", e);
            closeParser(fileName, isOrdered);
            throw new SimulatorInitializationException("Error occurred when initializing file reader for CSV file '" +
                    fileName + "' : ", e);
        }
    }

    /**
     * If the CSV file is ordered by timestamp, this method reads the next line and produces an event
     *
     * @param csvConfig        configuration of CSV simulation
     * @param streamAttributes list of attributes of the stream to which events are produced
     * @param startTimestamp   start timestamp of event simulation
     * @param endTimestamp     end timestamp of event simulation
     * @return event produced
     */
    public Event getNextEvent(CSVSimulationDTO csvConfig, List<Attribute> streamAttributes, long startTimestamp,
                              long endTimestamp) {
        Event event = null;
        int timestampPosition = Integer.parseInt(csvConfig.getTimestampAttribute());
        List<Integer> indices = csvConfig.getIndices();
        try {
            while (true) {
                lineNumber++;
                String line = bufferedReader.readLine();
                if (line != null) {
                    ArrayList<String> attributes = new ArrayList<>(Arrays.asList(line.split(csvConfig.getDelimiter())));
                    long timestamp;
//                    if the line does not have sufficient data to produce an event, move to next line
                    if (timestampPosition == -1) {
                        /*
                         * if timestamp attribute is not specified, take startTimestamp as the first event
                         * timestamp and the successive timestamps will be lastTimetstamp + timeInterval
                         * */
                        timestamp = startTimestamp + eventNumber * csvConfig.getTimestampInterval();
                        if (endTimestamp != -1 && timestamp > endTimestamp) {
                            break;
                        }
                    } else {
                        /*
                         * retrieve the value at the position specified by timestamp attribute as the timestamp
                         * if the timestamp is within the range specified by the startTimestamp and endTimestamp,
                         * proceed to creating an event, else ignore record and proceed to next record
                         * */
                        try {
                            timestamp = Long.parseLong(attributes.get(timestampPosition));
                            if (timestamp >= startTimestamp) {
                                if (endTimestamp == -1 || timestamp <= endTimestamp) {
                                    attributes.remove(timestampPosition);
                                } else {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            log.warn("Invalid data '" + attributes.get(timestampPosition) + "' provided for timestamp" +
                                    "attribute in line " + lineNumber + ". Line content : " + attributes + ". " +
                                    "Ignore line and read next line. Source configuration : " + csvConfig.toString());
                            continue;
                        } catch (IndexOutOfBoundsException e) {
                            log.warn("Cannot retrieve data elements from line " + lineNumber + " for all indices " +
                                    indices + ". Line content : " + attributes + ". Ignore line and read next line." +
                                    " Source configuration : " + csvConfig.toString());
                            continue;
                        }
                    }
                    try {
                        List<String> eventData = new ArrayList<>();
//                    retrieve the data elements required for event using record using the indices specified
                        indices.forEach(index -> eventData.add(attributes.get(index)));
                        event = EventConverter.eventConverter(streamAttributes, eventData.toArray(), timestamp);
                        eventNumber++;
                        break;
                    } catch (IndexOutOfBoundsException e) {
                        log.warn("Cannot retrieve data elements from line " + lineNumber + " for all indices "  +
                                indices + ". Line content : " + attributes + ". Ignore line and read next line." +
                                " Source configuration : " + csvConfig.toString());
                    } catch (EventGenerationException e) {
                        log.error("Error occurred when generating event using CSV event " +
                                "generator to simulate stream '" + csvConfig.getStreamName() + "' using source " +
                                "configuration : " + csvConfig.toString() + "Drop event and create next event.", e);
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Error occurred when reading CSV file '" + csvConfig.getFileName() + "' to simulate stream '" +
                    csvConfig.getStreamName() + "' using source configuration : " + csvConfig.toString(), e);
            closeParser(csvConfig.getFileName(), true);
            throw new EventGenerationException("Error occurred when reading CSV file '" + csvConfig.getFileName() +
                    "' to simulate stream '" + csvConfig.getStreamName() + "' using source configuration : " +
                    csvConfig.toString(), e);
        }
        return event;
    }


    /**
     * If the CSV is not ordered by timestamp, getEventsMap() method is used to create a treeMap of events.
     *
     * @param csvConfig        configuration of csv simulation
     * @param streamAttributes list of attributes of the stream to which events are produced
     * @param startTimestamp   start timestamp of event simulation
     * @param endTimestamp     end timestamp of event simulation
     * @return treeMap of events
     */
    public TreeMap<Long, ArrayList<Event>> getEventsMap(CSVSimulationDTO csvConfig, List<Attribute> streamAttributes,
                                                        long startTimestamp, long endTimestamp) {
        try {
            csvParser = parseFile(csvConfig.getDelimiter());
            return createEventsMap(csvConfig, streamAttributes, startTimestamp,
                    endTimestamp);
        } catch (IOException e) {
            log.error("Error occurred when initializing CSVParser for CSV file '" + csvConfig.getFileName() + "' to " +
                    "simulate stream '" + csvConfig.getStreamName() + "' using source configuration : " +
                    csvConfig.toString(), e);
            throw new EventGenerationException("Error occurred when initializing CSVParser for CSV file '" +
                    csvConfig.getFileName() + "' to simulate stream '" + csvConfig.getStreamName() + "' using source " +
                    "configuration : " + csvConfig.toString(), e);
        } finally {
            closeParser(csvConfig.getFileName(), false);
        }
    }


    /**
     * parseFile() method is used to parse the CSV file using the delimiter specified in CSV simulation Configuration
     *
     * @param delimiter delimiter to be used when parsing CSV file
     * @throws IOException if an error occurs when creating a CSVReader
     */
    private CSVParser parseFile(String delimiter) throws IOException {
        switch (delimiter) {
            case ",":
                return new CSVParser(fileReader, CSVFormat.DEFAULT);
            case ";":
                return new CSVParser(fileReader, CSVFormat.EXCEL);
            case "\\t":
                return new CSVParser(fileReader, CSVFormat.TDF);
            default:
                return new CSVParser(fileReader, CSVFormat.newFormat(delimiter.charAt(0)));
        }
    }


    /**
     * createEventsMap() methods creates a treeMap of events using the data in the CSV file.
     * The key of the treeMap will be the event timestamp and the value will be an array list of events belonging to
     * the timestamp.
     *
     * @param csvConfig        configuration of csv simulation
     * @param streamAttributes list of attributes of the stream to which events are produced
     * @param startTimestamp   start timestamp of event simulation
     * @param endTimestamp     end timestamp of event simulation
     * @return a treeMap of events
     */
    private TreeMap<Long, ArrayList<Event>> createEventsMap(CSVSimulationDTO csvConfig,
                                                            List<Attribute> streamAttributes,
                                                            long startTimestamp, long endTimestamp) {
        TreeMap<Long, ArrayList<Event>> eventsMap = new TreeMap<>();
        int timestampPosition = Integer.parseInt(csvConfig.getTimestampAttribute());
        long lineNumber;
        long timestamp;
        List<Integer> indices = csvConfig.getIndices();
        Event event;
        if (csvParser != null) {
            for (CSVRecord record : csvParser) {
                lineNumber = csvParser.getCurrentLineNumber();
                ArrayList<String> attributes = new ArrayList<>();
                for (String attribute : record) {
                    attributes.add(attribute);
                }
                /*
                 * retrieve the value at the position specified by timestamp attribute as the timestamp
                 * if the timestamp is within the range specified by the startTimestamp and endTimestamp, proceed to
                 * creating an event, else ignore record and proceed to next record
                 * retrieve the data elements required for event using record using the indices specified
                 * */
                try {
                    timestamp = Long.parseLong(attributes.get(timestampPosition));
                    if (timestamp >= startTimestamp) {
                        if (endTimestamp == -1 || timestamp <= endTimestamp) {
                            List<String> eventData = new ArrayList<>();
                            indices.forEach(index -> eventData.add(attributes.get(index)));
                            try {
                                event = EventConverter.eventConverter(streamAttributes, eventData.toArray(), timestamp);
                            } catch (EventGenerationException e) {
                                log.error("Error occurred when generating event using CSV event generator to simulate" +
                                        " stream '" + csvConfig.getStreamName() + "' using source configuration : " +
                                        csvConfig.toString() + "Drop event and create next event.", e);
                                continue;
                            }
                            if (!eventsMap.containsKey(timestamp)) {
                                eventsMap.put(timestamp, new ArrayList<>(Collections.singletonList(event)));
                            } else {
                                eventsMap.get(timestamp).add(event);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid data '" + attributes.get(timestampPosition) + "' provided for timestamp" +
                            "attribute in line " + lineNumber + ". Line content : " + attributes + ". " +
                            "Ignore line and read next line. Source configuration : " + csvConfig.toString());
                } catch (IndexOutOfBoundsException e) {
                    log.warn("Cannot retrieve data elements from line " + lineNumber + " for all indices "  +
                            indices + ". Line content : " + attributes + ". Ignore line and read next line." +
                            " Source configuration : " + csvConfig.toString());
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Create an ordered events map from CSV file '" + csvConfig.getFileName() + "' to simulate " +
                    "stream '" + csvConfig.getStreamName() + "'.");
        }
        return eventsMap;
    }


    /**
     * closeParser() method is used to release resources created to read the CSV file
     *
     * @param isOrdered bool indicating whether the entries in CSV file are ordered or not
     */
    public void closeParser(String fileName, boolean isOrdered) {
        try {
            if (fileReader != null) {
                fileReader.close();
            }
            if (isOrdered) {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } else {
                if (csvParser != null && !csvParser.isClosed()) {
                    csvParser.close();
                }
            }
        } catch (IOException e) {
            log.error("Error occurred when closing CSV resources used for CSV file '" + fileName + "'", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Close resources used for CSV file '" + fileName + "'.");
        }
    }

}
