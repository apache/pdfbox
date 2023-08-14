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
package org.apache.pdfbox.pdfwriter.compress;

/**
 * An instance of this class centralizes and provides the configuration for a PDF compression.
 * 
 * @author Christian Appl
 */
public class CompressParameters
{
    public static final CompressParameters DEFAULT_COMPRESSION = new CompressParameters();
    public static final CompressParameters NO_COMPRESSION = new CompressParameters(0);

    public static final int DEFAULT_OBJECT_STREAM_SIZE = 200;

    private final int objectStreamSize;

    public CompressParameters()
    {
        this(DEFAULT_OBJECT_STREAM_SIZE);
    }

    /**
     * Sets the number of objects, that can be contained in compressed object streams. Higher object stream sizes may
     * cause PDF readers to slow down during the rendering of PDF documents, therefore a reasonable value should be
     * selected. A value of 0 disables the compression.
     *
     * @param objectStreamSize The number of objects, that can be contained in compressed object streams.
     * 
     */
    public CompressParameters(int objectStreamSize)
    {
        if (objectStreamSize < 0)
        {
            throw new IllegalArgumentException("Object stream size can't be a negative value");
        }
        this.objectStreamSize = objectStreamSize;
    }

    /**
     * Returns the number of objects, that can be contained in compressed object streams. Higher object stream sizes may
     * cause PDF readers to slow down during the rendering of PDF documents, therefore a reasonable value should be
     * selected.
     *
     * @return The number of objects, that can be contained in compressed object streams.
     */
    public int getObjectStreamSize()
    {
        return objectStreamSize;
    }

    /**
     * Indicates whether the creation of compressed object streams is enabled or not.
     * 
     * @return true if compression is enabled.
     */
    public boolean isCompress()
    {
        return objectStreamSize > 0;
    }
}
