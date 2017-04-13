/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cluster.coordinator.rdbms.util;

/**
 * Contains constant values needed for the coordination mechanism
 */
public class RDBMSConstants {

    // Coordination related tables
    public static final String LEADER_STATUS_TABLE = "LEADER_STATUS_TABLE";
    public static final String CLUSTER_NODE_STATUS_TABLE = "CLUSTER_NODE_STATUS_TABLE";
    //Cluster membership table
    public static final String MEMBERSHIP_EVENT_TABLE = "MEMBERSHIP_EVENT_TABLE";
    public static final String REMOVED_MEMBERS_TABLE = "REMOVED_MEMBERS_TABLE";

    // Cluster node status table columns
    public static final String NODE_ID = "NODE_ID";
    public static final String GROUP_ID = "GROUP_ID";
    public static final String PROPERTY_MAP = "PROPERTY_MAP";
    public static final String LAST_HEARTBEAT = "LAST_HEARTBEAT";
    public static final String IS_NEW_NODE = "IS_NEW_NODE";
    public static final String TASK_MARK_NODE_NOT_NEW = "marking node as not new";

    //columns for cluster membership communication
    public static final String MEMBERSHIP_CHANGE_TYPE = "CHANGE_TYPE";
    public static final String MEMBERSHIP_CHANGED_MEMBER_ID = "CHANGED_MEMBER_ID";
    public static final String REMOVED_MEMBER_ID = "REMOVED_MEMBER_ID";
    public static final String MEMBERSHIP_CHANGED_GROUP_ID = "CHANGED_GROUP_ID";

    public static final String PS_GET_COORDINATOR_NODE_ID =
            "SELECT " + NODE_ID + " FROM " + LEADER_STATUS_TABLE + " WHERE " + GROUP_ID + "=?";

    public static final String PS_GET_COORDINATOR_NODE =
            "SELECT " + PROPERTY_MAP + " FROM " + CLUSTER_NODE_STATUS_TABLE + " WHERE " + GROUP_ID
                    + "=? AND" + NODE_ID + "=?";

    public static final String PS_MARK_NODE_NOT_NEW =
            "UPDATE " + CLUSTER_NODE_STATUS_TABLE + " SET " + IS_NEW_NODE + " =0 " + " WHERE "
                    + NODE_ID + "=? AND " + GROUP_ID + "=?";

    /**
     * Prepared statement to insert coordinator row
     */
    public static final String PS_INSERT_COORDINATOR_ROW =
            "INSERT INTO " + LEADER_STATUS_TABLE + "(" + GROUP_ID + "," + NODE_ID + ","
                    + LAST_HEARTBEAT + ") VALUES (?,?,?)";

    /**
     * Prepared statement to insert coordinator row
     */
    public static final String PS_INSERT_NODE_HEARTBEAT_ROW =
            "INSERT INTO " + CLUSTER_NODE_STATUS_TABLE + "(" + NODE_ID + "," + LAST_HEARTBEAT + ","
                    + GROUP_ID + "," + PROPERTY_MAP + "," + IS_NEW_NODE + ")"
                    + " VALUES (?,?,?,?,1)";

    /**
     * Prepared statement to check if coordinator
     */
    public static final String PS_GET_COORDINATOR_ROW_FOR_NODE_ID =
            "SELECT " + LAST_HEARTBEAT + " FROM " + LEADER_STATUS_TABLE + " WHERE " + NODE_ID + "=?"
                    + " AND " + GROUP_ID + "=?";

    /**
     * Prepared statement to check if still coordinator
     */
    public static final String PS_GET_COORDINATOR_HEARTBEAT =
            "SELECT " + LAST_HEARTBEAT + " FROM " + LEADER_STATUS_TABLE + " WHERE " + GROUP_ID
                    + "=?";

    /**
     * Prepared statement to update coordinator heartbeat
     */
    public static final String PS_UPDATE_COORDINATOR_HEARTBEAT =
            "UPDATE " + LEADER_STATUS_TABLE + " SET " + LAST_HEARTBEAT + " =? " + " WHERE "
                    + NODE_ID + "=?" + " AND " + GROUP_ID + "=?";

    /**
     * Prepared statement to update coordinator heartbeat
     */
    public static final String PS_UPDATE_NODE_HEARTBEAT =
            "UPDATE " + CLUSTER_NODE_STATUS_TABLE + " SET " + LAST_HEARTBEAT + " =? " + " WHERE "
                    + NODE_ID + "=? AND " + GROUP_ID + "=?";

    /**
     * Prepared statement to check if coordinator
     */
    public static final String PS_GET_ALL_NODE_HEARTBEAT =
            "SELECT " + GROUP_ID + "," + NODE_ID + "," + PROPERTY_MAP + "," + LAST_HEARTBEAT + ","
                    + IS_NEW_NODE + " FROM " + CLUSTER_NODE_STATUS_TABLE + " WHERE " + GROUP_ID
                    + "=?";

    public static final String PS_GET_NODE_DATA =
            "SELECT " + GROUP_ID + "," + NODE_ID + "," + PROPERTY_MAP + "," + LAST_HEARTBEAT + ","
                    + IS_NEW_NODE + " FROM " + CLUSTER_NODE_STATUS_TABLE + " WHERE " + GROUP_ID
                    + "=? AND " + NODE_ID + "=?";

    public static final String PS_DELETE_COORDINATOR =
            "DELETE FROM " + LEADER_STATUS_TABLE + " WHERE " + GROUP_ID + "=? AND " + LAST_HEARTBEAT
                    + "<?";

    public static final String PS_DELETE_NODE_HEARTBEAT =
            "DELETE FROM " + CLUSTER_NODE_STATUS_TABLE + " WHERE " + NODE_ID + "=? AND " + GROUP_ID
                    + "=?";

    public static final String PS_CLEAR_NODE_HEARTBEATS =
            "DELETE FROM " + CLUSTER_NODE_STATUS_TABLE;

    public static final String PS_CLEAR_COORDINATOR_HEARTBEAT =
            "DELETE FROM " + LEADER_STATUS_TABLE;

    /**
     * Prepared statement to insert membership change event.
     */
    public static final String PS_INSERT_MEMBERSHIP_EVENT =
            "INSERT INTO " + MEMBERSHIP_EVENT_TABLE + " (" + NODE_ID + "," + GROUP_ID + ","
                    + MEMBERSHIP_CHANGE_TYPE + "," + MEMBERSHIP_CHANGED_MEMBER_ID + ")"
                    + " VALUES ( ?,?,?,?)";

    public static final String PS_INSERT_REMOVED_MEMBER_DETAILS =
            "INSERT INTO " + REMOVED_MEMBERS_TABLE + " (" + NODE_ID + "," + GROUP_ID + ","
                    + REMOVED_MEMBER_ID + "," + PROPERTY_MAP + ")" + " VALUES ( ?,?,?,?)";

    /**
     * Prepared statement to select membership change event destined to a particular member.
     */
    public static final String PS_SELECT_MEMBERSHIP_EVENT =
            "SELECT " + MEMBERSHIP_CHANGE_TYPE + ", " + MEMBERSHIP_CHANGED_MEMBER_ID + ", "
                    + GROUP_ID + " FROM " + MEMBERSHIP_EVENT_TABLE + " WHERE " + NODE_ID + "=?"
                    + " ORDER BY " + MEMBERSHIP_CHANGE_TYPE;

    public static final String PS_SELECT_REMOVED_MEMBER_DETAILS =
            "SELECT " + REMOVED_MEMBER_ID + ", " + PROPERTY_MAP + ", " + GROUP_ID + " FROM "
                    + REMOVED_MEMBERS_TABLE + " WHERE " + NODE_ID + "=? AND " + REMOVED_MEMBER_ID
                    + "=? AND " + GROUP_ID + "=?";

    public static final String PS_DELETE_REMOVED_MEMBER_DETAIL_FOR_NODE =
            "DELETE FROM " + REMOVED_MEMBERS_TABLE + " WHERE " + NODE_ID + "=? AND "
                    + REMOVED_MEMBER_ID + "=? AND " + GROUP_ID + "=?";

    /**
     * Prepared statement to clear the membership event table.
     */
    public static final String PS_CLEAR_ALL_MEMBERSHIP_EVENTS =
            "DELETE FROM " + MEMBERSHIP_EVENT_TABLE;

    /**
     * Prepared statement to clear membership change events destined to a particular member.
     */
    public static final String PS_CLEAN_MEMBERSHIP_EVENTS_FOR_NODE =
            "DELETE FROM " + MEMBERSHIP_EVENT_TABLE + " WHERE " + NODE_ID + "=?";

    public static final String TASK_ADD_MESSAGE_ID = "adding message id";
    public static final String TASK_GET_ALL_QUEUES = "getting all queues";
    public static final String TASK_ADD_COORDINATOR_ROW = "adding coordinator row";
    public static final String TASK_GET_COORDINATOR_INFORMATION = "reading coordinator information";
    public static final String TASK_CHECK_COORDINATOR_VALIDITY = "checking coordinator validity";
    public static final String TASK_UPDATE_COORDINATOR_HEARTBEAT = "updating coordinator heartbeat";
    public static final String TASK_UPDATE_NODE_HEARTBEAT = "updating node heartbeat";
    public static final String TASK_CREATE_NODE_HEARTBEAT = "creating node heartbeat";
    public static final String TASK_REMOVE_COORDINATOR = "removing coordinator heartbeat";
    public static final String TASK_REMOVE_NODE_HEARTBEAT = "removing node heartbeat entry";

    public static final String CREATE_LEADER_STATUS_TABLE =
            "CREATE TABLE IF NOT EXISTS LEADER_STATUS_TABLE (\n"
                    + "                        GROUP_ID VARCHAR(512) NOT NULL,\n"
                    + "                        NODE_ID VARCHAR(512) NOT NULL,\n"
                    + "                        LAST_HEARTBEAT BIGINT NOT NULL,\n"
                    + "                        PRIMARY KEY (GROUP_ID)\n" + ");\n";
    public static final String CREATE_CLUSTER_NODE_STATUS_TABLE =
            "CREATE TABLE IF NOT EXISTS CLUSTER_NODE_STATUS_TABLE (\n"
                    + "                        GROUP_ID VARCHAR(512) NOT NULL,\n"
                    + "                        NODE_ID VARCHAR(512) NOT NULL,\n"
                    + "                        PROPERTY_MAP BLOB NOT NULL,\n"
                    + "                        IS_NEW_NODE INT NOT NULL,\n"
                    + "                        LAST_HEARTBEAT BIGINT NOT NULL,\n"
                    + "                        PRIMARY KEY (GROUP_ID,NODE_ID)\n" + ");\n";
    public static final String CREATE_MEMBERSHIP_EVENT_TABLE =
            "CREATE TABLE IF NOT EXISTS MEMBERSHIP_EVENT_TABLE (\n"
                    + "                        GROUP_ID VARCHAR(512) NOT NULL,\n"
                    + "                        NODE_ID VARCHAR(512) NOT NULL,\n"
                    + "                        CHANGE_TYPE INT NOT NULL,\n"
                    + "                        CHANGED_MEMBER_ID VARCHAR(512) NOT NULL,\n" + ");\n";

    public static final String CREATE_REMOVED_MEMBERS_TABLE =
            "CREATE TABLE IF NOT EXISTS REMOVED_MEMBERS_TABLE (\n"
                    + "                        GROUP_ID VARCHAR(512) NOT NULL,\n"
                    + "                        NODE_ID VARCHAR(512) NOT NULL,\n"
                    + "                        PROPERTY_MAP BLOB NOT NULL,\n"
                    + "                        REMOVED_MEMBER_ID VARCHAR(512) NOT NULL,\n" + ");\n";

    /**
     * Only public static constants are in this class. No need to instantiate.
     */
    private RDBMSConstants() {
    }
}
