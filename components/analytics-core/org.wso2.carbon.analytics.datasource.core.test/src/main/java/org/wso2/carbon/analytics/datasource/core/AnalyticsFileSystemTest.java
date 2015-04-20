/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.datasource.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem.DataInput;

/**
 * This class contains tests related to {@link AnalyticsFileSystem}.
 */
public class AnalyticsFileSystemTest {

    private static final Log log = LogFactory.getLog(AnalyticsFileSystemTest.class);
    
    private AnalyticsFileSystem analyticsFileSystem;
    
    private String implementationName;
    
    public AnalyticsFileSystemTest() {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InMemoryICFactory.class.getName());
    }
    
    public void init(String implementationName, AnalyticsFileSystem analyticsFileSystem) throws AnalyticsException {
        this.implementationName = implementationName;
        this.analyticsFileSystem = analyticsFileSystem;
    }
    
    public String getImplementationName() {
        return implementationName;
    }
    
    public void cleanup() {
        try {
            this.analyticsFileSystem.destroy();
        } catch (IOException e) {
            log.error("Error in cleanup: " + e.getMessage(), e);
        }
    }
    
    private void addFilesToDir(String dir, String...files) throws IOException {
        OutputStream out;
        for (String file : files) {
            out = this.analyticsFileSystem.createOutput(dir + "/" + file);
            out.write(generateData(100));
            out.close();
        }
    }
    
    @Test
    public void testFSDirectoryOperations() throws IOException {
        this.analyticsFileSystem.delete("/d1");
        this.analyticsFileSystem.mkdir("/d1/d2/d3");
        this.analyticsFileSystem.mkdir("/d1/d2/d5");
        Assert.assertTrue(this.analyticsFileSystem.exists("/d1/d2/d3"));
        Assert.assertTrue(this.analyticsFileSystem.exists("/d1/d2/d5"));
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d4"));
        List<String> files = this.analyticsFileSystem.list("/d1/d2");
        Assert.assertEquals(files.size(), 2);
        /* the path must be normalized, can end with "/" or not */
        files = this.analyticsFileSystem.list("/d1/d2/");
        Assert.assertEquals(files.size(), 2);
        Assert.assertEquals(new HashSet<String>(Arrays.asList(new String[] { "d3", "d5" })), 
                new HashSet<String>(files));
        this.addFilesToDir("/d1/d2", "f1", "f2", "f3");
        files = this.analyticsFileSystem.list("/d1/d2");
        Assert.assertEquals(files.size(), 5);
        Assert.assertEquals(new HashSet<String>(Arrays.asList(new String[] { "d3", "d5", "f1", "f2", "f3" })), 
                new HashSet<String>(files));
        this.analyticsFileSystem.delete("/d1");
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d3"));
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d5"));
    }
    
    public static byte[] generateData(int size) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < size; i++) {
            out.write((int) (Math.random() * 127));
        }
        try {
            out.close();
        } catch (IOException ignore) {
            /* never happens */
        }
        return out.toByteArray();
    }
    
    @Test
    public void testFSFileIOOperations() throws IOException {
        this.analyticsFileSystem.delete("/d1");
        OutputStream out = this.analyticsFileSystem.createOutput("/d1/d2/d3/f1");
        byte[] data = generateData(1024 * 1024 + 7);
        long start = System.currentTimeMillis();
        out.write(data, 0, data.length);
        out.flush();
        out.close();
        long end = System.currentTimeMillis();
        System.out.println("File data (1 MB) written in: " + (end - start) + " ms.");
        Assert.assertEquals(this.analyticsFileSystem.length("/d1/d2/d3/f1"), data.length);
        DataInput in = this.analyticsFileSystem.createInput("/d1/d2/d3/f1");
        byte[] dataIn = new byte[data.length];
        start = System.currentTimeMillis();
        int len;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((len = in.read(dataIn, 0, dataIn.length)) != -1) {
            baos.write(dataIn, 0, len);
        }
        in.close();
        int resSize = baos.size();
        byte[] result = baos.toByteArray();
        baos.close();
        end = System.currentTimeMillis();
        System.out.println("File data (1 MB) read in: " + (end - start) + " ms.");
        Assert.assertEquals(resSize, data.length);
        Assert.assertEquals(data, result);
        this.analyticsFileSystem.delete("/d1/d2/d3/f1");
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d3/f1"));
        this.analyticsFileSystem.delete("/d1/d2/d3");
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d3"));
        this.analyticsFileSystem.delete("/d1");
    }
    
    private void fileRandomWriteRead(String path, int writerBufferSize, int readBufferSize, int n) 
            throws IOException {
        OutputStream out = this.analyticsFileSystem.createOutput(path);
        ByteArrayOutputStream byteOut1 = new ByteArrayOutputStream();
        byte[] data = generateData(writerBufferSize);
        for (int i = 0; i < n; i++) {
            out.write(data, 0, data.length);
            if (i % 10 == 0) {
                out.flush();
            }
            byteOut1.write(data, 0, data.length);
        }
        out.close();
        byteOut1.close();
        Assert.assertEquals(this.analyticsFileSystem.length(path), n * writerBufferSize);
        DataInput in = this.analyticsFileSystem.createInput(path);
        byte[] buff = new byte[readBufferSize];
        int j;
        ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
        while ((j = in.read(buff, 0, buff.length)) > 0) {
            byteOut2.write(buff, 0, j);
        }
        byteOut2.close();
        in.close();
        Assert.assertEquals(byteOut1.toByteArray(), byteOut2.toByteArray());
    }
    
    @Test
    public void testFSFileIOOperations2() throws AnalyticsException, IOException {
        this.analyticsFileSystem.delete("/d1");
        this.fileRandomWriteRead("/d1/d2/f1", 1350, 1350, 100);
        this.fileRandomWriteRead("/d1/d2/f2", 1350, 200, 150);
        this.fileRandomWriteRead("/d1/d2/f3", 240, 2000, 50);
        this.fileRandomWriteRead("/d1/d2/f2", 10, 500, 300);
        this.fileRandomWriteRead("/d1/d2/f1", 1, 1, 1);
        this.analyticsFileSystem.delete("/d1");
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d3"));
    }
    
    private void fileReadSeekPosition(String path, int n, int chunk, int... locs) throws IOException {
        byte[] data = generateData(n);
        OutputStream out = this.analyticsFileSystem.createOutput(path);
        out.write(data, 0, data.length);
        out.close();
        byte[] din = new byte[chunk];
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        byte[] din2 = new byte[chunk];
        DataInput in = this.analyticsFileSystem.createInput(path);
        int count, count2;
        for (int i : locs) {
            in.seek(i);
            Assert.assertEquals(in.getPosition(), i);
            count = in.read(din, 0, din.length);
            Assert.assertEquals(in.getPosition(), i + (count < 0 ? 0 : count));
            bin.reset();
            bin.skip(i);
            count2 = bin.read(din2, 0, din2.length);
            Assert.assertEquals(count, count2);
            Assert.assertEquals(din, din2);
        }
    }
    
    @Test
    public void testFSReadSeekPosition() throws IOException {
        this.analyticsFileSystem.delete("/d1");
        this.fileReadSeekPosition("/d1/f1", 2000, 5, 0, 10, 5, 50, 100, 1570, 1998, 0);
        this.fileReadSeekPosition("/d1/f2", 100, 5, 99);
        this.fileReadSeekPosition("/d1/f3", 100, 5, 0, 10, 5, 50, 99, 0, 1, 20);
        this.fileReadSeekPosition("/d1/f4", 10, 1, 0, 10);
        this.fileReadSeekPosition("/d1/f4", 10, 1, 0, 10, 5, 7, 9, 0, 1, 0);
        this.analyticsFileSystem.delete("/d1");
    }
    
    @Test
    public void testFSPerfTest() throws IOException {
        System.out.println("\n************** START FS PERF TEST [" + this.getImplementationName() + "] **************");
        this.analyticsFileSystem.delete("/mydir");
        byte[] data = generateData(2048);
        OutputStream out;
        
        /* warm-up */
        for (int i = 0; i < 100; i++) {
            out = this.analyticsFileSystem.createOutput("/mydir/perf_warmup/file" + i);
            out.write(data, 0, data.length);
            out.close();
        }
        
        long start = System.currentTimeMillis();
        int count = 1000;
        for (int i = 0; i < count; i++) {
            out = this.analyticsFileSystem.createOutput("/mydir/perf/file" + i);
            out.write(data, 0, data.length);
            out.close();
        }
        System.out.println();
        long end = System.currentTimeMillis();
        System.out.println("* " + count + " 2K files written in: " + (end - start) + " ms. " + (count / (double) (end - start) * 1000.0) + " FPS.");
        DataInput in;
        byte[] dataIn = new byte[data.length];
        int len;
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            in = this.analyticsFileSystem.createInput("/mydir/perf/file" + i);
            len = in.read(dataIn, 0, dataIn.length);
            in.close();
            Assert.assertEquals(len, dataIn.length);
        }
        end = System.currentTimeMillis();
        System.out.println("* " + count + " 2K files read in: " + (end - start) + " ms. " + (count / (double) (end - start) * 1000.0) + " FPS.");
        this.analyticsFileSystem.delete("/mydir");
        System.out.println("\n************** END FS PERF TEST [" + this.getImplementationName() + "] **************");
    }
    
}
