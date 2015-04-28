/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.messageconsole.ui.beans;


/**
 * This class represent user's analytics data service level permissions.
 */
public class Permissions {

    private boolean createTable;
    private boolean listTable;
    private boolean dropTable;
    private boolean searchRecord;
    private boolean listRecord;
    private boolean putRecord;
    private boolean deleteRecord;

    public Permissions() {
    }

    public boolean isCreateTable() {
        return createTable;
    }

    public void setCreateTable(boolean createTable) {
        this.createTable = createTable;
    }

    public boolean isListTable() {
        return listTable;
    }

    public void setListTable(boolean listTable) {
        this.listTable = listTable;
    }

    public boolean isDropTable() {
        return dropTable;
    }

    public void setDropTable(boolean dropTable) {
        this.dropTable = dropTable;
    }

    public boolean isSearchRecord() {
        return searchRecord;
    }

    public void setSearchRecord(boolean searchRecord) {
        this.searchRecord = searchRecord;
    }

    public boolean isListRecord() {
        return listRecord;
    }

    public void setListRecord(boolean listRecord) {
        this.listRecord = listRecord;
    }

    public boolean isPutRecord() {
        return putRecord;
    }

    public void setPutRecord(boolean putRecord) {
        this.putRecord = putRecord;
    }

    public boolean isDeleteRecord() {
        return deleteRecord;
    }

    public void setDeleteRecord(boolean deleteRecord) {
        this.deleteRecord = deleteRecord;
    }
}
