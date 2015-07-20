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

package org.apache.pdfbox.pdfparser;

import java.io.Closeable;
import java.io.IOException;

/**
 * A SequentialSource provides access to sequential data for parsing.
 */
interface SequentialSource extends Closeable
{
    /**
     * Read a single byte of data.
     *
     * @return The byte of data that is being read.
     * @throws IOException If there is an error while reading the data.
     */
    int read() throws IOException;
    
    /**
     * Read a buffer of data.
     *
     * @param b The buffer to write the data to.
     * @return The number of bytes that were actually read.
     * @throws IOException If there was an error while reading the data.
     */
    int read(byte[] b) throws IOException;

    /**
     * Read a buffer of data.
     *
     * @param b The buffer to write the data to.
     * @param offset Offset into the buffer to start writing.
     * @param length The amount of data to attempt to read.
     * @return The number of bytes that were actually read.
     * @throws IOException If there was an error while reading the data.
     */
    int read(byte[] b, int offset, int length) throws IOException;

    /**
     * Returns offset of next byte to be returned by a read method.
     *
     * @return offset of next byte which will be returned with next {@link #read()} (if no more 
     * bytes are left it returns a value &gt;= length of source).
     * @throws IOException If there was an error while reading the data.
     */
    long getPosition() throws IOException;

    /**
     * This will peek at the next byte.
     *
     * @return The next byte on the stream, leaving it as available to read.
     * @throws IOException If there is an error reading the next byte.
     */
    int peek() throws IOException;

    /**
     * Unreads a single byte.
     *
     * @param b byte array to push back
     * @throws IOException If there is an error while seeking
     */
    void unread(int b) throws IOException;

    /**
     * Unreads an array of bytes.
     *
     * @param bytes byte array to push back
     * @throws IOException If there is an error while seeking
     */
    void unread(byte[] bytes) throws IOException;

    /**
     * Reads a given number of bytes in its entirety.
     *
     * @param length the number of bytes to be read
     * @return a byte array containing the bytes just read
     * @throws IOException if an I/O error occurs while reading data
     */
    byte[] readFully(int length) throws IOException;

    /**
     * Returns true if the end of the data source has been reached.
     *
     * @return true if we are at the end of the data.
     * @throws IOException If there is an error reading the next byte.
     */
    boolean isEOF() throws IOException;
}
