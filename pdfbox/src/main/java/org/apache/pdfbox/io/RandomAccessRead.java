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
package org.apache.pdfbox.io;

import java.io.IOException;

/**
 * An interface allowing random access read operations.
 */
public interface RandomAccessRead extends SequentialRead
{

    /**
     * Returns offset of next byte to be returned by a read method.
     * 
     * @return offset of next byte which will be returned with next {@link #read()}
     *         (if no more bytes are left it returns a value >= length of source)
     *         
     * @throws IOException 
     */
    public long getPosition() throws IOException;
    
    /**
     * Seek to a position in the data.
     *
     * @param position The position to seek to.
     * @throws IOException If there is an error while seeking.
     */
    public void seek(long position) throws IOException;

    /**
     * The total number of bytes that are available.
     *
     * @return The number of bytes available.
     *
     * @throws IOException If there is an IO error while determining the
     * length of the data stream.
     */
    public long length() throws IOException;

}
