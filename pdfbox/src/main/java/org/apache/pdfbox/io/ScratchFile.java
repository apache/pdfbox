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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A temporary file which can hold multiple buffers of temporary data. A new temporary file is created for each new
 * {@link ScratchFile} instance, and is deleted when the {@link ScratchFile} is closed.
 * <p>
 * Multiple buffers can be creating by calling the {@link #createBuffer()} method.
 * <p>
 * The file is split into pages, each page containing a pointer to the previous and next pages. This allows for
 * multiple, separate streams in the same file.
 *
 * @author Jesse Long
 */
public class ScratchFile implements Closeable
{
    private static final Log LOG = LogFactory.getLog(ScratchFile.class);
    private File file;
    private java.io.RandomAccessFile raf;

    /**
     * Creates a new scratch file. If a {code scratchFileDirectory} is supplied, then the scratch file is created in
     * that directory.
     * 
     * @param scratchFileDirectory The directory in which to create the scratch file, or {code null} if the scratch
     * should be created in the default temporary directory.
     * @throws IOException If there was a problem creating a temporary file.
     */
    public ScratchFile(File scratchFileDirectory) throws IOException
    {
        file = File.createTempFile("PDFBox", ".tmp", scratchFileDirectory);
        try
        {
            raf = new java.io.RandomAccessFile(file, "rw");
        }
        catch (IOException e)
        {
            if (!file.delete())
            {
                LOG.warn("Error deleting scratch file: " + file.getAbsolutePath());
            }
            throw e;
        }
    }

    /**
     * Returns the underlying {@link java.io.RandomAccessFile}.
     * 
     * @return The underlying {@link java.io.RandomAccessFile}.
     */
    java.io.RandomAccessFile getRandomAccessFile()
    {
        return raf;
    }

    /**
     * Checks if this scratch file has already been closed. If the file has been closed, an {@link IOException} is
     * thrown.
     * 
     * @throws IOException If the file has already been closed.
     */
    void checkClosed() throws IOException
    {
        if (raf == null)
        {
            throw new IOException("Scratch file already closed");
        }
    }

    /**
     * Creates a new buffer in the scratch file.
     * 
     * @return A new buffer.
     * @throws IOException If an error occurred.
     */
    public RandomAccess createBuffer() throws IOException
    {
        return new ScratchFileBuffer(this);
    }

    /**
     * Closes and deletes the temporary file. No further interaction with the scratch file or associated buffers can
     * happen after this method is called.
     * 
     * @throws IOException If there was a problem closing or deleting the temporary file.
     */
    @Override
    public void close() throws IOException
    {
        if (raf != null)
        {
            raf.close();
            raf = null;
        }

        if (file != null)
        {
            if (file.delete())
            {
                file = null;
            }
            else
            {
                throw new IOException("Error deleting scratch file: " + file.getAbsolutePath());
            }
        }
    }
}
