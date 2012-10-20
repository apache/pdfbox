/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fontbox.cff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 
 * @author Villu Ruusmann
 * @version $Revision: 1.0 $
 */
public class DataOutput
{

    private ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

    private String outputEncoding = null;

    /**
     * Constructor.
     */
    public DataOutput()
    {
        this("ISO-8859-1");
    }

    /**
     * Constructor with a given encoding.
     * @param encoding the encoding to be used for writing
     */
    public DataOutput(String encoding)
    {
        this.outputEncoding = encoding;
    }

    /**
     * Returns the written data buffer as byte array.
     * @return the data buffer as byte array
     */
    public byte[] getBytes()
    {
        return outputBuffer.toByteArray();
    }

    /**
     * Write an int value to the buffer.
     * @param value the given value
     */
    public void write(int value)
    {
        outputBuffer.write(value);
    }

    /**
     * Write a byte array to the buffer.
     * @param buffer the given byte array
     */
    public void write(byte[] buffer)
    {
        outputBuffer.write(buffer, 0, buffer.length);
    }

    /**
     * Write a part of a byte array to the buffer.
     * @param buffer the given byte buffer
     * @param offset the offset where to start 
     * @param length the amount of bytes to be written from the array
     */
    public void write(byte[] buffer, int offset, int length)
    {
        outputBuffer.write(buffer, offset, length);
    }

    /**
     * Write the given string to the buffer using the given encoding.
     * @param string the given string
     * @throws IOException If an error occurs during writing the data to the buffer
     */
    public void print(String string) throws IOException
    {
        write(string.getBytes(outputEncoding));
    }

    /**
     * Write the given string to the buffer using the given encoding.
     * A newline is added after the given string
     * @param string the given string
     * @throws IOException If an error occurs during writing the data to the buffer
     */
    public void println(String string) throws IOException
    {
        write(string.getBytes(outputEncoding));
        write('\n');
    }

    /**
     * Add a newline to the given string.
     */
    public void println()
    {
        write('\n');
    }
}