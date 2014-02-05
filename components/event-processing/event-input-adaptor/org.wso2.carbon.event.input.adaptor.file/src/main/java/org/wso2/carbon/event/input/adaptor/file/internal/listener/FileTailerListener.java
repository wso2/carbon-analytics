package org.wso2.carbon.event.input.adaptor.file.internal.listener;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileTailerListener extends TailerListenerAdapter {

    private String fileName;

    private Map<String, InputEventAdaptorListener> inpuEventListenerMap = new ConcurrentHashMap<String, InputEventAdaptorListener>();
    private static final Log log = LogFactory.getLog(FileTailerListener.class);

    public FileTailerListener(String fileName) {
        this.fileName = fileName;
    }


    @Override
    public void init(Tailer tailer) {
        super.init(tailer);
    }

    @Override
    public void handle(String line) {
        super.handle(line);
        if (line == null || line.isEmpty()) {
            return;
        }
        for (InputEventAdaptorListener id : inpuEventListenerMap.values()) {
            id.onEvent(line + " fileName: " + fileName + "\n");
        }
    }

    @Override
    public void fileNotFound() {
        log.warn("File not found");
        super.fileNotFound();
    }

    @Override
    public void handle(Exception ex) {
        log.error("Exception occurred : ", ex);
        super.handle(ex);
    }

    public void addListener(String id, InputEventAdaptorListener inputListener) {
        inpuEventListenerMap.put(id, inputListener);

    }

    public void removeListener(String id) {
        inpuEventListenerMap.remove(id);
    }

    public boolean hasOneSubscriber() {
        return inpuEventListenerMap.size() == 1;
    }

    public boolean hasNoSubscriber() {
        return inpuEventListenerMap.isEmpty();
    }

}
