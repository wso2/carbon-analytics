/*
 * Copyright 2004-2014 The Apache Software Foundation.
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

package org.wso2.carbon.event.input.adaptor.file.internal.util;


public final class FileEventAdaptorConstants {

    private FileEventAdaptorConstants() {
    }

    public static final String EVENT_ADAPTOR_TYPE_FILE = "file";
    public static final String EVENT_ADAPTOR_CONF_FILEPATH = "filepath";
    public static final String EVENT_ADAPTOR_CONF_FILEPATH_HINT = "Absolute path of the file";
    public static final int MIN_THREAD = 8;
    public static final int MAX_THREAD = 100;

}
