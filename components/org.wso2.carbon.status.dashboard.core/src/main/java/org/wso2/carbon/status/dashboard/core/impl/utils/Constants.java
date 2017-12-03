/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.status.dashboard.core.impl.utils;

/**
 * Class to define SQL queries and constants.
 */
public class Constants {
    public static final String WORKER_JVM_CLASS_LOADING_LOADED_CURRENT = "jvm.class-loading.loaded.current";
    public static final String WORKER_JVM_CLASS_LOADING_LOADED_TOTAL = "jvm.class-loading.loaded.total";
    public static final String WORKER_JVM_CLASS_LOADING_UNLOADED_TOTAL = "jvm.class-loading.unloaded.total ";
    public static final String WORKER_JVM_GC_PS_MARKSWEEP_COUNT = "jvm.gc.PS-MarkSweep.count";
    public static final String WORKER_JVM_GC_PS_MARKSWEEP_TIME = "jvm.gc.PS-MarkSweep.time";
    public static final String WORKER_JVM_GC_PS_SCAVENGE_COUNT = "jvm.gc.PS-Scavenge.count";
    public static final String WORKER_JVM_GC_PS_SCAVENGE_TIME = "jvm.gc.PS-Scavenge.time";
    public static final String WORKER_JVM_MEMORY_HEAP_COMMITTED = "jvm.memory.heap.committed";
    public static final String WORKER_JVM_MEMORY_HEAP_INIT = "jvm.memory.heap.init";
    public static final String WORKER_JVM_MEMORY_HEAP_MAX = "jvm.memory.heap.max";
    public static final String WORKER_JVM_MEMORY_HEAP_USAGE = "jvm.memory.heap.usage";
    public static final String WORKER_JVM_MEMORY_HEAP_USED= "jvm.memory.heap.used";
    public static final String WORKER_JVM_MEMORY_NON_HEAP_COMMITTED = "jvm.memory.non-heap.committed";
    public static final String WORKER_JVM_MEMORY_NON_HEAP_INIT = "jvm.memory.non-heap.init";
    public static final String WORKER_JVM_MEMORY_NON_HEAP_MAX = "jvm.memory.non-heap.max";
    public static final String WORKER_JVM_MEMORY_NON_HEAP_USAGE = "jvm.memory.non-heap.usage";
    public static final String WORKER_JVM_MEMORY_NON_HEAP_USED = "jvm.memory.non-heap.used";
    public static final String WORKER_JVM_MEMORY_TOTAL_COMMITTED = "jvm.memory.total.committed";
    public static final String WORKER_JVM_MEMORY_TOTAL_INIT = "jvm.memory.total.init";
    public static final String WORKER_JVM_MEMORY_TOTAL_MAX = "jvm.memory.total.max";
    public static final String WORKER_JVM_MEMORY_TOTAL_USED = "jvm.memory.total.used";
    public static final String WORKER_JVM_OS_CPU_LOAD_PROCESS = "jvm.os.cpu.load.process";
    public static final String WORKER_JVM_OS_CPU_LOAD_SYSTEM = "jvm.os.cpu.load.system";
    public static final String WORKER_JVM_OS_FILE_DESCRIPTOR_MAX_COUNT = "jvm.os.file.descriptor.max.count";
    public static final String WORKER_JVM_OS_FILE_DESCRIPTOR_OPEN_COUNT = "jvm.os.file.descriptor.open.count";
    public static final String WORKER_JVM_OS_PHYSICAL_MEMORY_FREE_SIZE = "jvm.os.physical.memory.free.size";
    public static final String WORKER_JVM_OS_PHYSICAL_MEMORY_TOTAL_SIZE= "jvm.os.physical.memory.total.size";
    public static final String WORKER_JVM_OS_SWAP_SPACE_FREE_SIZE = "jvm.os.swap.space.free.size";
    public static final String WORKER_JVM_OS_SWAP_SPACE_TOTAL_SIZE = "jvm.os.swap.space.total.size";
    public static final String WORKER_JVM_OS_SYSTEM_LOAD_AVERAGE = "jvm.os.system.load.average";
    public static final String WORKER_JVM_OS_VIRTUAL_MEMORY_COMMITTED_SIZE = "jvm.os.virtual.memory.committed.size";
    public static final String WORKER_JVM_THREADS_COUNT = "jvm.threads.count";
    public static final String WORKER_JVM_THREADS_DAEMON_COUNT = "jvm.threads.daemon.count";


    //Default not open
    public static final String WORKER_JVM_MEMORY_POOL = "jvm.memory.pools";
    public static final String WORKER_JVM_BLOCKED_THREADS_COUNT= "jvm.threads.blocked.count";
    public static final String WORKER_JVM_DEADLOCKED_THREADS_COUNT = "jvm.threads.deadlock.count";
    public static final String WORKER_JVM_NEW_THREADS_COUNT = "jvm.threads.new.count";
    public static final String WORKER_JVM_RUNNABLE_THREADS_COUNT = "jvm.threads.runnable.count";
    public static final String WORKER_JVM_TERMINATED_THREADS_COUNT = "jvm.threads.terminated.count";
    public static final String WORKER_JVM_TIMD_WATING_THREADS_COUNT = "jvm.threads.timed_waiting.count";
    public static final String WORKER_JVM_WAITING_THREADS_COUNT = "jvm.threads.waiting.count";

    private Constants() {
        //preventing initialization
    }

}
