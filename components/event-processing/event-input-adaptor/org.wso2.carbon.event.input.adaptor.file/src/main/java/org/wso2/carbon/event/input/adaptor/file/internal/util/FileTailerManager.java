package org.wso2.carbon.event.input.adaptor.file.internal.util;

import org.apache.commons.io.input.Tailer;
import org.wso2.carbon.event.input.adaptor.file.internal.listener.FileTailerListener;

public class FileTailerManager {

    private Tailer tailer;
    private FileTailerListener listener;


    public FileTailerManager(Tailer tailer, FileTailerListener listener) {
        super();
        this.tailer = tailer;
        this.listener = listener;
    }

    public Tailer getTailer() {
        return tailer;
    }

    public FileTailerListener getListener() {
        return listener;
    }

}
