/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.io.mgwfile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.extension.siddhi.io.mgwfile.dao.MGWFileSourceDAO;
import org.wso2.extension.siddhi.io.mgwfile.dto.MGWFileInfoDTO;
import org.wso2.extension.siddhi.io.mgwfile.exception.MGWFileSourceException;
import org.wso2.extension.siddhi.io.mgwfile.util.FileDataRetrieverUtil;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class publishes events to streams, which are read from the uploaded usage file
 */
public class MGWFileDataRetriever implements Runnable {

    private static final Log log = LogFactory.getLog(MGWFileDataRetriever.class);

    private MGWFileInfoDTO infoDTO;

    public MGWFileDataRetriever(MGWFileInfoDTO infoDTO) throws MGWFileSourceException {
        this.infoDTO = infoDTO;
    }

    @Override
    public void run() {
        log.info("Started publishing API usage in file : " + infoDTO.toString());
        publishEvents();
    }

    private void publishEvents() {

        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        InputStream fileContentStream = null;
        ZipInputStream zipInputStream = null;
        try {
            //Get Content of the file and start processing
            fileContentStream = MGWFileSourceDAO.getFileContent(infoDTO);
            if (fileContentStream == null) {
                log.warn("No content available in the file : " + infoDTO.toString()
                        + ". Therefore, not publishing the record.");
                MGWFileSourceDAO.updateCompletion(infoDTO);
                return;
            }
            zipInputStream = new ZipInputStream(fileContentStream);
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            if (zipEntry.getName().equals(MGWFileSourceConstants.API_USAGE_OUTPUT_FILE_NAME)) {
                InputStream inputStream = zipInputStream;
                inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                bufferedReader = new BufferedReader(inputStreamReader);
                String readLine;
                String streamId, timeStamp, metaData, correlationData, payloadData;
                String[] elements;
                long lineNumber = 0;
                while ((readLine = bufferedReader.readLine()) != null) {
                    lineNumber++;
                    try {
                        elements = readLine.split(MGWFileSourceConstants.EVENT_SEPARATOR);
                        //StreamID
                        streamId = elements[0].split(MGWFileSourceConstants.KEY_VALUE_SEPARATOR)[1];
                        //Timestamp
                        timeStamp = elements[1].split(MGWFileSourceConstants.KEY_VALUE_SEPARATOR)[1];
                        //MetaData
                        metaData = elements[2].split(MGWFileSourceConstants.KEY_VALUE_SEPARATOR)[1];
                        //correlationData
                        correlationData = elements[3].split(MGWFileSourceConstants.KEY_VALUE_SEPARATOR)[1];
                        //PayloadData
                        payloadData = elements[4].split(MGWFileSourceConstants.KEY_VALUE_SEPARATOR)[1];
                    } catch (RuntimeException e) {
                        log.error("Event format does not match with the expected format in line number : " + lineNumber
                                + " on file : " + infoDTO);
                        // skip this line and continue with the next event in the file
                        continue;
                    }
                    SourceEventListener eventSource = MGWFileSourceRegistrationManager.
                            getStreamSpecificEventListenerMap().get(streamId);
                    if (eventSource != null) {
                        try {
                            eventSource.onEvent(new Event(streamId, Long.parseLong(timeStamp),
                                    (Object[]) FileDataRetrieverUtil.createMetaData(metaData),
                                    (Object[]) FileDataRetrieverUtil.createMetaData(correlationData),
                                    FileDataRetrieverUtil.createPayload(streamId, payloadData)), null);
                        } catch (NumberFormatException e) {
                            log.error("Error occurred while executing onEvent for event : " + Arrays.toString(elements),
                                    e);
                        }
                    } else {
                        log.error("Unable to find eventsource for stream id: " + streamId);
                    }
                }
            }
            //Update the database
            MGWFileSourceDAO.updateCompletion(infoDTO);
            log.info("Completed publishing API Usage from file : " + infoDTO.toString());
        } catch (IOException e) {
            log.error("Error occurred while reading the API Usage file.", e);
        } catch (MGWFileSourceException e) {
            log.error("Error occurred while updating the completion for the processed file.", e);
        } finally {
            IOUtils.closeQuietly(inputStreamReader);
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(fileContentStream);
            IOUtils.closeQuietly(zipInputStream);
        }
    }

}
