/*
 * Copyright 2022 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * An interface describing a StreamCache to be used when creating/writing streams of a PDF.
 * 
 */
public interface RandomAccessStreamCache extends Closeable
{

    @FunctionalInterface
    public interface StreamCacheCreateFunction
    {
        /**
         * Creates an instance of a RandomAccessStreamCache.
         *
         * @return the stream cache.
         * @throws IOException if something went wrong
         */
        RandomAccessStreamCache create() throws IOException;
    }

    /**
     * Creates an instance of a buffer implementing the interface org.apache.pdfbox.io.RandomAccess. The caller should
     * close the buffer after usage otherwise the buffer shall be closed once the underlying RandomAccessStreamCache is
     * closed.
     * 
     * @return the instance of the buffer
     * @throws IOException if something went wrong
     */
    public abstract RandomAccess createBuffer() throws IOException;

}
