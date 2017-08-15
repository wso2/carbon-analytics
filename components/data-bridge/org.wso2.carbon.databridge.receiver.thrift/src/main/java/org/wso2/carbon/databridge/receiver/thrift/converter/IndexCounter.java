/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.databridge.receiver.thrift.converter;

/**
 * Counter used to dematerialise event bundle
 */
public class IndexCounter {

    private int intCount = 0;
    private int longCount = 0;
    private int boolCount = 0;
    private int stringCount = 0;
    private int doubleCount = 0;

    public int getIntCount() {
        return intCount;
    }

    public int getLongCount() {
        return longCount;
    }

    public int getBoolCount() {
        return boolCount;
    }

    public int getStringCount() {
        return stringCount;
    }

    public int getDoubleCount() {
        return doubleCount;
    }


    public void incrementIntCount() {
        intCount++;
    }

    public void incrementLongCount() {
        longCount++;
    }

    public void incrementBoolCount() {
        boolCount++;
    }

    public void incrementStringCount() {
        stringCount++;
    }

    public void incrementDoubleCount() {
        doubleCount++;
    }
}
