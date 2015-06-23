/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.input.adapter.email;

import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.email.internal.util.EmailEventAdapterConstants;

import javax.mail.*;
import java.io.IOException;
import java.util.*;

public class EmailEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdaptorListener;
    private final String id = UUID.randomUUID().toString();
    private static final Logger log = Logger.getLogger(EmailEventAdapter.class);
    private long pollIntervalInSeconds = EmailEventAdapterConstants.DEFAULT_EMAIL_POLL_INTERVAL_IN_MINS;
    private String moveToFolderName;
    private Timer timer;
    private volatile boolean isThreadOccupied;
    private int tenantId;

    public EmailEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                             Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
        timer = new Timer("PollTimer");
        moveToFolderName = globalProperties.get(EmailEventAdapterConstants.ADAPTER_CONF_MOVE_TO_FOLDER_NAME);
        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }


    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
        this.eventAdaptorListener = eventAdaptorListener;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {

        String interval = eventAdapterConfiguration.getProperties().get(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_POLL_INTERVAL);
        if (interval != null) {
            try {
                pollIntervalInSeconds = Long.parseLong(interval);
            } catch (NumberFormatException e) {
                pollIntervalInSeconds = EmailEventAdapterConstants.DEFAULT_EMAIL_POLL_INTERVAL_IN_MINS;
            }
        }
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                PollForMails();
            }
        };

        timer.scheduleAtFixedRate(timerTask, pollIntervalInSeconds * 1000, pollIntervalInSeconds * 1000);

    }

    @Override
    public void disconnect() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    @Override
    public void destroy() {
    }

    public InputEventAdapterListener getEventAdaptorListener() {
        return eventAdaptorListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailEventAdapter)) return false;

        EmailEventAdapter that = (EmailEventAdapter) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean isEventDuplicatedInCluster() {
        return false;
    }

    @Override
    public boolean isPolling() {
        return true;
    }

    private Properties getServerProperties(String protocol, String host,
                                           String port) {
        Properties properties = new Properties();

        // server setting
        properties.put(String.format("mail.%s.host", protocol), host);
        properties.put(String.format("mail.%s.port", protocol), port);

        // SSL setting
        properties.setProperty(
                String.format("mail.%s.socketFactory.class", protocol),
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty(
                String.format("mail.%s.socketFactory.fallback", protocol),
                "false");
        properties.setProperty(
                String.format("mail.%s.socketFactory.port", protocol),
                String.valueOf(port));

        return properties;
    }

    private void PollForMails() {

        if (!isThreadOccupied) {

            isThreadOccupied = true;

            String emailAddress = eventAdapterConfiguration.getProperties().get(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_ADDRESS);
            String userName = eventAdapterConfiguration.getProperties().get(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_USERNAME);
            String password = eventAdapterConfiguration.getProperties().get(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_PASSWORD);
            String subject = eventAdapterConfiguration.getProperties().get(EmailEventAdapterConstants.ADAPTER_MESSAGE_RECEIVING_EMAIL_SUBJECT);
            String host = eventAdapterConfiguration.getProperties().get(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL_HOST);
            String port = eventAdapterConfiguration.getProperties().get(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL_PORT);
            String protocol = eventAdapterConfiguration.getProperties().get(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL);

            Properties properties = getServerProperties(protocol, host, port);


            boolean connected = false;
            Store store = null;
            Folder folder = null;
            boolean mailProcessingStarted = false;
            Session session = Session.getDefaultInstance(properties);

            try {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to connect to POP3/IMAP server for : " +
                            emailAddress + " using " + session.getProperties());
                }

                store = session.getStore(protocol);

                if (userName != null && password != null) {
                    store.connect(userName, password);
                } else {
                    log.error("Unable to locate username and password for mail login");
                }

                // were we able to connect?
                connected = store.isConnected();

                if (connected) {
                    folder = store.getFolder("INBOX");
                }

            } catch (Exception e) {
                log.error("Error connecting to mail server for address : " + emailAddress, e);
            }

            if (!connected) {
                log.warn("Connection to mail server for account : " + emailAddress +
                        " failed. Retrying in : " + pollIntervalInSeconds + " seconds");
            }

            if (connected && folder != null) {
                Runnable onCompletion = new MailCheckCompletionTask(folder, store, emailAddress);

                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Connecting to folder : " + folder.getName() +
                                " of email account : " + emailAddress);
                    }

                    folder.open(Folder.READ_WRITE);
                    int total = folder.getMessageCount();
                    Message[] messages = folder.getMessages();

                    if (log.isDebugEnabled()) {
                        log.debug(messages.length + " messages in folder : " + folder);
                    }

                    for (int i = 0; i < total; i++) {

                        try {
                            String[] status = messages[i].getHeader("Status");
                            if (status != null && status.length == 1 && status[0].equals("RO")) {
                                // some times the mail server sends a special mail message which is
                                // not relavent in processing. ignore this message.
                                if (log.isDebugEnabled()) {
                                    log.debug("Skipping message # : " + messages[i].getMessageNumber()
                                            + " : " + messages[i].getSubject() + " - Status: RO");
                                }

                            } else if (messages[i].isSet(Flags.Flag.SEEN)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Skipping message # : " + messages[i].getMessageNumber()
                                            + " : " + messages[i].getSubject() + " - already marked SEEN");
                                }

                            } else if (messages[i].isSet(Flags.Flag.DELETED)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Skipping message # : " + messages[i].getMessageNumber()
                                            + " : " + messages[i].getSubject() + " - already marked DELETED");
                                }

                            } else {
                                processMessage(messages[i], subject);
                                moveOrDeleteAfterProcessing(moveToFolderName, store, folder, messages[i]);
                                mailProcessingStarted = true;
                            }
                        } catch (MessageRemovedException ignore) {
                            // while reading the meta information, this mail was deleted, thats ok
                            if (log.isDebugEnabled()) {
                                log.debug("Skipping message # : " + messages[i].getMessageNumber() +
                                        " as it has been DELETED by another thread after processing");
                            }
                        }
                    }

                    if (!mailProcessingStarted) {
                        // if we didn't process any mail in this run, the onCompletion will not
                        // run from the mail processor by default
                        onCompletion.run();
                    }

                } catch (MessagingException me) {
                    log.error("Error checking mail for account : " + emailAddress + " :: " + me.getMessage(), me);
                }

                onCompletion.run();
            }
            isThreadOccupied = false;
        }

    }


    /**
     * Handle optional logic of the mail transport, that needs to happen once all messages in
     * a check mail cycle has ended.
     */
    private class MailCheckCompletionTask implements Runnable {
        private final Folder folder;
        private final Store store;
        private final String emailAddress;
        private boolean taskStarted = false;

        public MailCheckCompletionTask(Folder folder, Store store,
                                       String emailAddress) {
            this.folder = folder;
            this.store = store;
            this.emailAddress = emailAddress;
        }

        public void run() {
            synchronized (this) {
                if (taskStarted) {
                    return;
                } else {
                    taskStarted = true;
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Executing onCompletion task for the mail download of : " + emailAddress);
            }

            if (folder != null) {
                try {
                    folder.close(true /** expunge messages flagged as DELETED*/);
                    if (log.isDebugEnabled()) {
                        log.debug("Mail folder closed, and deleted mail expunged");
                    }
                } catch (MessagingException e) {
                    log.warn("Error closing mail folder : " +
                            folder + " for account : " + emailAddress + " :: " + e.getMessage());
                }
            }

            if (store != null) {
                try {
                    store.close();
                    if (log.isDebugEnabled()) {
                        log.debug("Mail store closed for : " + emailAddress);
                    }
                } catch (MessagingException e) {
                    log.warn("Error closing mail store for account : " +
                            emailAddress + " :: " + e.getMessage(), e);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Scheduling next poll for : " + emailAddress);
            }

        }
    }

    private void moveOrDeleteAfterProcessing(String moveToFolder, Store store,
                                             Folder folder, Message message) {
        try {
            if (moveToFolder != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Moving processed email to folder :" + moveToFolder);
                }
                Folder dFolder = store.getFolder(moveToFolder);
                if (!dFolder.exists()) {
                    dFolder.create(Folder.HOLDS_MESSAGES);
                }
                folder.copyMessages(new Message[]{message}, dFolder);
            }

            if (log.isDebugEnabled()) {
                log.debug("Deleting email :" + message.getMessageNumber());
            }

            message.setFlag(Flags.Flag.DELETED, true);

        } catch (MessagingException e) {
            log.error("Error deleting or resolving folder to move after processing : "
                    + moveToFolder, e);
        }
    }

    private void processMessage(Message msg, String expectedSubject) {
        try {
            String mailSubject = msg.getSubject();
            if (mailSubject.equalsIgnoreCase(expectedSubject)) {
                Object content = msg.getContent();
                if (content instanceof String) {
                    pushEvent(msg);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Skipping message because content type is not accepted " + msg);
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping message because subject not matches " + msg);
                }
            }

        } catch (MessagingException e) {
            log.error("Exception when trying to identify the content type", e);
        } catch (IOException e) {
            log.error("Exception when trying to read the mail content", e);
        }
    }

    private void pushEvent(Object obj) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            eventAdaptorListener.onEvent(obj);
        } catch (Throwable e) {
            log.error("Exception when pushing event to CEP ", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

}
