/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*
*/
package org.wso2.carbon.analytics.io.commons;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class GenericUtils {

    private static ThreadLocal<Kryo> kryoTL = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            return new Kryo();
        }
    };

    /* do not touch if you do not know what you're doing, critical for serialize/deserialize
     * implementation to be stable to retain backward compatibility */
    public static byte[] serializeObject(Object obj) {
        Kryo kryo = kryoTL.get();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (Output out = new Output(byteOut)) {
            kryo.writeClassAndObject(out, obj);
            out.flush();
            byte[] data = byteOut.toByteArray();
            ByteBuffer result = ByteBuffer.allocate(data.length + Integer.SIZE / 8);
            result.putInt(data.length);
            result.put(data);
            return result.array();
        }
    }

    /* do not touch, @see serializeObject(Object) */
    public static void serializeObject(Object obj, OutputStream out) throws IOException {
        byte[] data = serializeObject(obj);
        out.write(data, 0, data.length);
    }

    /* do not touch, @see serializeObject(Object) */
    public static Object deserializeObject(byte[] source) {
        if (source == null) {
            return null;
        }
        /* skip the object size integer */
        try (Input input = new Input(Arrays.copyOfRange(source, Integer.SIZE / 8, source.length))) {
            Kryo kryo = kryoTL.get();
            return kryo.readClassAndObject(input);
        }
    }

    /* do not touch, @see serializeObject(Object) */
    public static Object deserializeObject(InputStream in) throws IOException {
        if (in == null) {
            return null;
        }
        in = checkAndGetAvailableStream(in);
        DataInputStream dataIn = new DataInputStream(in);
        int size = dataIn.readInt();
        byte[] buff = new byte[size];
        dataIn.readFully(buff);
        Kryo kryo = kryoTL.get();
        try (Input input = new Input(buff)) {
            return kryo.readClassAndObject(input);
        }
    }

    private static InputStream checkAndGetAvailableStream(InputStream in) throws IOException {
        InputStream result;
        int n = in.available();
        if (n == 0) {
            PushbackInputStream pin = new PushbackInputStream(in, 1);
            int data = pin.read();
            if (data == -1) {
                throw new EOFException();
            } else {
                pin.unread(data);
                result = pin;
            }
        } else {
            result = in;
        }
        return result;
    }
}
