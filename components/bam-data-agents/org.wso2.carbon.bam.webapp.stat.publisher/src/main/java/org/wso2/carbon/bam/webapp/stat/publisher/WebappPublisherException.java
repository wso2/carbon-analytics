/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.bam.webapp.stat.publisher;

/*
 * This class handles the errors
 */
public class WebappPublisherException extends Exception {
    private static final long serialVersionUID = 3048028946241207694L;

    public WebappPublisherException() {
    }

    public WebappPublisherException(String message) {
        super(message);
    }

    public WebappPublisherException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebappPublisherException(Throwable cause) {
        super(cause);
    }

}
