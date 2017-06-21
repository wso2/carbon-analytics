package org.wso2.carbon.event.simulator.core.internal.util;

import org.apache.commons.fileupload.util.LimitedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.FileLimitExceededException;

import java.io.IOException;
import java.io.InputStream;

/**
 * ValidatedInputStream class validates whether the size of uploaded file is less than the maximum file size allowed.
 */
public class ValidatedInputStream extends LimitedInputStream {
    private static final Logger log = LoggerFactory.getLogger(ValidatedInputStream.class);

    public ValidatedInputStream(InputStream inputStream, long pSizeMax) {
        super(inputStream, pSizeMax);
    }

    /**
     * raiseError() alerts when the input stream exceeds the maximum size specified
     */
    @Override
    protected void raiseError(long sizeLimit, long actualSize) throws IOException {
        in.close();
        throw new FileLimitExceededException("File size exceeds the maximum size limit of " +
                (sizeLimit / 1024 / 1024) + " MB.");
    }
}
