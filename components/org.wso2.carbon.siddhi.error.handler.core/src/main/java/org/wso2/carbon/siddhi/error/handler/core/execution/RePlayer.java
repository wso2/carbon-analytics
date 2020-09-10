/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.error.handler.core.execution;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.event.ComplexEvent;
import io.siddhi.core.event.ComplexEventChunk;
import io.siddhi.core.event.Event;
import io.siddhi.core.event.stream.StreamEvent;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.util.error.handler.model.ErrorEntry;
import io.siddhi.core.util.error.handler.store.ErrorStore;
import io.siddhi.core.util.error.handler.util.*;
import org.wso2.carbon.siddhi.error.handler.core.exception.SiddhiErrorHandlerException;
import org.wso2.carbon.siddhi.error.handler.core.internal.SiddhiErrorHandlerDataHolder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Contains methods to re-play collected erroneous events.
 */
public class RePlayer {
    private RePlayer() {
    }

    public static void rePlay(List<ErrorEntry> errorEntries) throws SiddhiErrorHandlerException {
        ErrorStore errorStore = SiddhiErrorHandlerDataHolder.getInstance().getErrorStore();
        int failureCount = 0;
        for (ErrorEntry errorEntry : errorEntries) {
            try {
                rePlay(errorEntry);
                errorStore.discardErrorEntry(errorEntry.getId());
            } catch (SiddhiErrorHandlerException | InterruptedException e) {
                failureCount++;
            }
        }
        if (failureCount > 0) {
            throw new SiddhiErrorHandlerException("There were some failures when trying to re-play.");
        }
    }

    private static void rePlay(ErrorEntry errorEntry) throws SiddhiErrorHandlerException, InterruptedException {
        SiddhiAppRuntime siddhiAppRuntime = SiddhiErrorHandlerDataHolder.getInstance()
                .getSiddhiAppRuntimeService().getActiveSiddhiAppRuntimes().get(errorEntry.getSiddhiAppName());
        if (errorEntry.getErrorType() == ErrorType.MAPPING || errorEntry.getErrorType() == ErrorType.TRANSPORT) {
            if (siddhiAppRuntime != null) {
                switch (errorEntry.getEventType()) {
                    case COMPLEX_EVENT:
                        rePlayComplexEvent(errorEntry, siddhiAppRuntime);
                        break;
                    case EVENT:
                        rePlayEvent(errorEntry, siddhiAppRuntime);
                        break;
                    case EVENT_ARRAY:
                        rePlayEventArray(errorEntry, siddhiAppRuntime);
                        break;
                    case EVENT_LIST:
                        rePlayEventList(errorEntry, siddhiAppRuntime);
                        break;
                    case PAYLOAD_STRING:
                        rePlayPayloadString(errorEntry, siddhiAppRuntime);
                        break;
                    default:
                        // Ideally we won't reach here
                }
            }
        } else {
            if (siddhiAppRuntime != null) {
                if (errorEntry.getEventType() == ErroneousEventType.REPLAYABLE_TABLE_RECORD) {
                    rePlayComplexEventChunk(errorEntry, siddhiAppRuntime);
                } else {
                    throw new SiddhiErrorHandlerException("For ErrorType STORE the ErroneousEventType should be " +
                            "COMPLEX_EVENT_CHUNK but found " + errorEntry.getEventType().toString() + " in " +
                            errorEntry.getStreamName());
                }
            }
        }
    }

    private static void rePlayComplexEventChunk(ErrorEntry complexEventErrorEntry, SiddhiAppRuntime siddhiAppRuntime)
            throws SiddhiErrorHandlerException, InterruptedException {
        try {
            Object deserializedTableRecord = ErrorHandlerUtils.getAsObject(complexEventErrorEntry.getEventAsBytes());
            if (deserializedTableRecord instanceof ReplayableTableRecord) {
                ReplayableTableRecord replayableTableRecord = (ReplayableTableRecord) deserializedTableRecord;
                switch (complexEventErrorEntry.getErrorOccurrence()) {
                    case STORE_ON_TABLE_ADD:
                        ComplexEventChunk<StreamEvent> replayAddEventChunk =
                                replayableTableRecord.getComplexEventChunk();
                        siddhiAppRuntime.getTableInputHandler(complexEventErrorEntry.getStreamName())
                                .add(replayAddEventChunk);
                        break;
                    default:
                        throw new SiddhiErrorHandlerException("Unsupported ErrorOccurenceType of " +
                                complexEventErrorEntry.getErrorOccurrence() + " to replay ComplexEventChunk in "
                                + complexEventErrorEntry.getStreamName());
                }
            } else {
                throw new SiddhiErrorHandlerException(
                        "eventAsBytes present in the entry is invalid. It is expected to represent a " +
                                ReplayableTableRecord.class);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new SiddhiErrorHandlerException("Failed to get bytes as a ComplexEventChunk object.", e);
        }
    }

    private static void rePlayComplexEvent(ErrorEntry complexEventErrorEntry, SiddhiAppRuntime siddhiAppRuntime)
            throws SiddhiErrorHandlerException, InterruptedException {
        try {
            Object complexEvent = ErrorHandlerUtils.getAsObject(complexEventErrorEntry.getEventAsBytes());
            if (complexEvent instanceof ComplexEvent) {
                InputHandler inputHandler = siddhiAppRuntime.getInputHandler(complexEventErrorEntry
                        .getStreamName());
                if (inputHandler != null) {
                    ComplexEvent current = (ComplexEvent) complexEvent;
                    while (current != null) {
                        inputHandler.send(current.getOutputData());
                        current = current.getNext();
                    }
                } else {
                    throw new SiddhiErrorHandlerException(String.format("Input handler was not found for " +
                            "stream: %s.", complexEventErrorEntry.getStreamName()));
                }
            } else {
                throw new SiddhiErrorHandlerException(
                        "eventAsBytes present in the entry is invalid. It is expected to represent a ComplexEvent.");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new SiddhiErrorHandlerException("Failed to get bytes as a ComplexEvent object.", e);
        }
    }

    private static void rePlayEvent(ErrorEntry eventErrorEntry, SiddhiAppRuntime siddhiAppRuntime)
            throws SiddhiErrorHandlerException, InterruptedException {
        try {
            Object event = ErrorHandlerUtils.getAsObject(eventErrorEntry.getEventAsBytes());
            if (event instanceof Event) {
                InputHandler inputHandler = siddhiAppRuntime.getInputHandler(eventErrorEntry.getStreamName());
                if (inputHandler != null) {
                    inputHandler.send((Event) event);
                } else {
                    throw new SiddhiErrorHandlerException(
                            String.format("Input handler was not found for stream: %s.", eventErrorEntry.getStreamName()));
                }
            } else {
                throw new SiddhiErrorHandlerException(
                        "eventAsBytes present in the entry is invalid. It is expected to represent an Event.");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new SiddhiErrorHandlerException("Failed to get bytes as an Event object.", e);
        }
    }

    private static void rePlayEventArray(ErrorEntry eventArrayErrorEntry, SiddhiAppRuntime siddhiAppRuntime)
            throws SiddhiErrorHandlerException, InterruptedException {
        try {
            Object eventArray = ErrorHandlerUtils.getAsObject(eventArrayErrorEntry.getEventAsBytes());
            if (eventArray instanceof Event[]) {
                InputHandler inputHandler = siddhiAppRuntime.getInputHandler(eventArrayErrorEntry.getStreamName());
                if (inputHandler != null) {
                    inputHandler.send((Event[]) eventArray);
                } else {
                    throw new SiddhiErrorHandlerException(String.format("Input handler was not found for stream: %s.",
                            eventArrayErrorEntry.getStreamName()));
                }
            } else {
                throw new SiddhiErrorHandlerException(
                        "eventAsBytes present in the entry is invalid. It is expected to represent an Event[].");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new SiddhiErrorHandlerException("Failed to get bytes as an Event[].", e);
        }
    }

    private static void rePlayEventList(ErrorEntry eventListErrorEntry, SiddhiAppRuntime siddhiAppRuntime)
            throws SiddhiErrorHandlerException, InterruptedException {
        try {
            Object eventList = ErrorHandlerUtils.getAsObject(eventListErrorEntry.getEventAsBytes());
            if (eventList instanceof List) {
                InputHandler inputHandler = siddhiAppRuntime.getInputHandler(eventListErrorEntry.getStreamName());
                if (inputHandler != null) {
                    for (Event event : (List<Event>) eventList) {
                        inputHandler.send(event);
                    }
                } else {
                    throw new SiddhiErrorHandlerException(String.format("Input handler was not found for stream: %s.",
                            eventListErrorEntry.getStreamName()));
                }
            } else {
                throw new SiddhiErrorHandlerException(
                        "eventAsBytes present in the entry is invalid. It is expected to represent an Event List.");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new SiddhiErrorHandlerException("Failed to get bytes as an Event List.", e);
        }
    }

    private static void rePlayPayloadString(ErrorEntry payloadStringErrorEntry, SiddhiAppRuntime siddhiAppRuntime)
            throws SiddhiErrorHandlerException {
        try {
            Object payloadString = ErrorHandlerUtils.getAsObject(payloadStringErrorEntry.getEventAsBytes());
            if (payloadString instanceof String) {
                if (payloadStringErrorEntry.getErrorOccurrence() == ErrorOccurrence.BEFORE_SOURCE_MAPPING) {
                    // Get sources of the appropriate stream.
                    Optional<List<Source>> sourcesOfStream = siddhiAppRuntime.getSources().stream().filter(sourceList ->
                            sourceList.stream().anyMatch(source -> Objects.equals(source.getStreamDefinition().getId(),
                                    payloadStringErrorEntry.getStreamName()))).findFirst();

                    if (sourcesOfStream.isPresent() && sourcesOfStream.get().size() == 1) {
                        sourcesOfStream.get().get(0).getMapper().onEvent(payloadString, null, null);
                    } else {
                        throw new SiddhiErrorHandlerException(
                                "Re-playing can be done only if a single source is associated with the stream.");
                    }
                } else {
                    throw new SiddhiErrorHandlerException("Unexpected error occurrence for the error.");
                }
            } else {
                throw new SiddhiErrorHandlerException(
                        "eventAsBytes present in the entry is invalid. It is expected to represent a String.");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new SiddhiErrorHandlerException("Failed to get bytes as a String.", e);
        }
    }
}
