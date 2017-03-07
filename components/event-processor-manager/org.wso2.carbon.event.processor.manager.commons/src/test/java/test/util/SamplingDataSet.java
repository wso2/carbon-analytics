/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package test.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SamplingDataSet {
    public static final List<String> SEARCH_TERMS = Collections.unmodifiableList(new ArrayList<String>() {{
        add("CEP");
        add("WSO2");
        add("Complex Event Processing");
        add("ESB");
        add("Open Source");
        add("Middleware");
        add("jaggery");
        add(".jag files");
        add("Siddhi");
        add("siddhi query language");
        add("CEP performance numbers");
        add("Complex Eventing");
        add("CEP throughput");
        add("WSO2Con");
        add("WSO2 Roadmap");
    }});

    public static final List<String> IP_ADDRESSES = Collections.unmodifiableList(new ArrayList<String>() {{
        add("192.168.1.100");
        add("192.168.1.23");
        add("192.168.1.240");
        add("192.168.0.2");
        add("10.8.0.1");
        add("10.0.1.75");
        add("10.8.8.23");
        add("10.8.1.224");
        add("203.94.106.11");
        add("203.94.10.15");
        add("203.90.106.110");
        add("203.93.106.23");
        add("192.248.8.68");
        add("116.24.5.63");
        add("124.14.5.135");
    }});

    public static final List<String> USER_IDS = Collections.unmodifiableList(new ArrayList<String>() {{
        add("ann@org1.com");
        add("sam@org2.com");
        add("jeff@wso2.com");
        add("gayan@org5.com");
        add("lang@wso2.org");
        add("manjula@org5.org");
        add("john@org3.com");
        add("chris@org1.com");
        add("dennis@org7.com");
        add("mary@org2.com");
        add("gavin@wso2.org");
        add("oleg@org6.com");
        add("marvin@org4.com");
        add("naveen@wso2.com");
        add("praveen@org2.org");
    }});


}
