/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.core.internal.listener;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.event.processor.api.receive.BasicEventListener;
import org.wso2.carbon.event.processor.core.internal.stream.EventJunction;
import org.wso2.carbon.event.processor.core.internal.stream.EventProducer;

public class ExternalStreamListener implements BasicEventListener, EventProducer {
    private EventJunction eventJunction;
    private Object owner;

    public ExternalStreamListener(EventJunction eventJunction, Object owner) {
        this.eventJunction = eventJunction;
        this.owner = owner;
    }

    @Override
    public void onEvent(Object[] eventData) {
        eventJunction.dispatchEvent(eventData);
    }

    @Override
    public void onAddDefinition(Attribute[] definitionAttributes) {
        // ignore
    }

    @Override
    public void onRemoveDefinition(Attribute[] definitionAttributes) {
        // ignore
    }

    @Override
    public Object getOwner() {
        return owner;
    }
}
