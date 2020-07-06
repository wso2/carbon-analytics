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
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.util.error.handler.model.ErrorEntry;
import io.siddhi.core.util.error.handler.store.ErrorStore;
import io.siddhi.core.util.error.handler.util.ErrorOccurrence;
import org.wso2.carbon.siddhi.error.handler.core.exception.SiddhiErrorHandlerException;
import org.wso2.carbon.siddhi.error.handler.core.internal.SiddhiErrorHandlerDataHolder;

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
                errorStore.discardErroneousEvent(errorEntry.getId());
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
    }

    private static void rePlayComplexEvent(ErrorEntry complexEventErrorEntry, SiddhiAppRuntime siddhiAppRuntime)
        throws SiddhiErrorHandlerException, InterruptedException {
        if (complexEventErrorEntry.getEvent() instanceof ComplexEvent) {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler(complexEventErrorEntry.getStreamName());
            if (inputHandler != null) {
                ComplexEvent current = (ComplexEvent) complexEventErrorEntry.getEvent();
                while (current != null) {
                    inputHandler.send(current.getOutputData());
                    current = current.getNext();
                }
            } else {
                throw new SiddhiErrorHandlerException(String.format("Input handler was not found for stream: %s.",
                    complexEventErrorEntry.getStreamName()));
            }
        } else {
            throw new SiddhiErrorHandlerException("The event present in the entry is invalid.");
        }
    }

    private static void rePlayEvent(ErrorEntry eventErrorEntry, SiddhiAppRuntime siddhiAppRuntime)
        throws SiddhiErrorHandlerException, InterruptedException {
        if (eventErrorEntry.getEvent() instanceof Event) {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler(eventErrorEntry.getStreamName());
            if (inputHandler != null) {
                inputHandler.send((Event) eventErrorEntry.getEvent());
            } else {
                throw new SiddhiErrorHandlerException(
                    String.format("Input handler was not found for stream: %s.", eventErrorEntry.getStreamName()));
            }
        } else {
            throw new SiddhiErrorHandlerException("The event present in the entry is invalid.");
        }
    }

    private static void rePlayEventArray(ErrorEntry eventArrayErrorEntry, SiddhiAppRuntime siddhiAppRuntime)
        throws SiddhiErrorHandlerException, InterruptedException {
        if (eventArrayErrorEntry.getEvent() instanceof Event[]) {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler(eventArrayErrorEntry.getStreamName());
            if (inputHandler != null) {
                inputHandler.send((Event[]) eventArrayErrorEntry.getEvent());
            } else {
                throw new SiddhiErrorHandlerException(
                    String.format("Input handler was not found for stream: %s.", eventArrayErrorEntry.getStreamName()));
            }
        } else {
            throw new SiddhiErrorHandlerException("The event present in the entry is invalid.");
        }
    }

    private static void rePlayEventList(ErrorEntry eventListErrorEntry, SiddhiAppRuntime siddhiAppRuntime)
        throws SiddhiErrorHandlerException, InterruptedException {
        if (eventListErrorEntry.getEvent() instanceof List) {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler(eventListErrorEntry.getStreamName());
            if (inputHandler != null) {
                for (Event event : (List<Event>) eventListErrorEntry.getEvent()) {
                    inputHandler.send(event);
                }
            } else {
                throw new SiddhiErrorHandlerException(
                    String.format("Input handler was not found for stream: %s.", eventListErrorEntry.getStreamName()));
            }
        } else {
            throw new SiddhiErrorHandlerException("The event present in the entry is invalid.");
        }
    }

    private static void rePlayPayloadString(ErrorEntry payloadStringErrorEntry, SiddhiAppRuntime siddhiAppRuntime)
        throws SiddhiErrorHandlerException {
        if (payloadStringErrorEntry.getEvent() instanceof String) {
            if (payloadStringErrorEntry.getErrorOccurrence() == ErrorOccurrence.BEFORE_SOURCE_MAPPING) {
                // Get sources of the appropriate stream.
                Optional<List<Source>> sourcesOfStream = siddhiAppRuntime.getSources().stream().filter(sourceList ->
                    sourceList.stream().anyMatch(source -> Objects.equals(source.getStreamDefinition().getId(),
                        payloadStringErrorEntry.getStreamName()))).findFirst();

                if (sourcesOfStream.isPresent() && sourcesOfStream.get().size() == 1) {
                    sourcesOfStream.get().get(0).getMapper().onEvent(payloadStringErrorEntry.getEvent(), null, null);
                } else {
                    throw new SiddhiErrorHandlerException(
                        "Re-playing can be done only if a single source is associated with the stream.");
                }
            } else {
                throw new SiddhiErrorHandlerException("Unexpected error occurrence for the error.");
            }
        } else {
            throw new SiddhiErrorHandlerException("The event present in the entry is invalid.");
        }
    }
}
