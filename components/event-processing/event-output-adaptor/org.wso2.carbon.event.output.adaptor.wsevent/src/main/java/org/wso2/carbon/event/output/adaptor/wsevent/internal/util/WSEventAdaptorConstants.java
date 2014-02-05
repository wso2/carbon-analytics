/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.output.adaptor.wsevent.internal.util;


public final class WSEventAdaptorConstants {


    private WSEventAdaptorConstants() {
    }

    public static final String ADAPTOR_TYPE_WSEVENT = "ws-event";

    public static final String ADAPTOR_CONF_WSEVENT_URI = "uri";
    public static final String ADAPTOR_CONF_WSEVENT_URI_HINT = "uri.hint";
    public static final String ADAPTOR_CONF_WSEVENT_USERNAME = "username";
    public static final String ADAPTOR_CONF_WSEVENT_PASSWORD = "password";

    public static final String ADAPTOR_MESSAGE_TOPIC_NAME = "topic";
    public static final String ADAPTOR_MESSAGE_HINT_TOPIC_NAME = "topic.hint";

}
