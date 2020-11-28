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

    public static final int DEFAULT_OBJECT_STREAM_SIZE = 200;

    private int objectStreamSize = DEFAULT_OBJECT_STREAM_SIZE;

    /**
     * Sets the number of objects, that can be contained in compressed object streams. Higher object stream sizes may
     * cause PDF readers to slow down during the rendering of PDF documents, therefore a reasonable value should be
     * selected.
     *
     * @param objectStreamSize The number of objects, that can be contained in compressed object streams.
     * @return The current instance, to allow method chaining.
     */
    public CompressParameters setObjectStreamSize(int objectStreamSize)
    {
        this.objectStreamSize = objectStreamSize <= 0 ? DEFAULT_OBJECT_STREAM_SIZE
                : objectStreamSize;
        return this;
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

}
