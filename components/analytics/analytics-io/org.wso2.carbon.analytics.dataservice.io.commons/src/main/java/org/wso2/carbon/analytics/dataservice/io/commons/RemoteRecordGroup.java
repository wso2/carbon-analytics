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
package org.wso2.carbon.analytics.dataservice.io.commons;

import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class RemoteRecordGroup implements RecordGroup {
    private String[] locations;
    private byte[] binaryRecordGroup;

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    public byte[] getBinaryRecordGroup() {
        return binaryRecordGroup;
    }

    public void setBinaryRecordGroup(byte[] binaryRecordGroup) {
        this.binaryRecordGroup = binaryRecordGroup;
    }

    @Override
    public String[] getLocations() throws AnalyticsException {
        return this.locations;
    }

    public RecordGroup getRecordGroupFromBinary() throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(binaryRecordGroup);
        ObjectInputStream is = new ObjectInputStream(in);
        RecordGroup recordGroup = (RecordGroup) is.readObject();
        is.close();
        in.close();
        return recordGroup;
    }
}
