/**
 * Copyright (c) 2006, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.io;

import java.io.IOException;

/**
 * An interface to allow PDF files to be stored completely in memory or
 * to use a scratch file on the disk.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public interface RandomAccess 
{

    /**
     * Release resources that are being held.
     * 
     * @throws IOException If there is an error closing this resource.
     */
    public void close() throws IOException;

    /**
     * Seek to a position in the data.
     * 
     * @param position The position to seek to.
     * @throws IOException If there is an error while seeking.
     */
    public void seek(long position) throws IOException;

    /**
     * Read a single byte of data.
     * 
     * @return The byte of data that is being read.
     * 
     * @throws IOException If there is an error while reading the data.
     */
    public int read() throws IOException;

    /**
     * Read a buffer of data.
     * 
     * @param b The buffer to write the data to.
     * @param offset Offset into the buffer to start writing.
     * @param length The amount of data to attempt to read.
     * @return The number of bytes that were actually read.
     * @throws IOException If there was an error while reading the data.
     */
    public int read(byte[] b, int offset, int length) throws IOException;

    /**
     * The total number of bytes that are available.
     * 
     * @return The number of bytes available.
     * 
     * @throws IOException If there is an IO error while determining the 
     * length of the data stream.
     */
    public long length() throws IOException;

    /**
     * Write a byte to the stream.
     * 
     * @param b The byte to write.
     * @throws IOException If there is an IO error while writing.
     */
    public void write(int b) throws IOException;

    /**
     * Write a buffer of data to the stream.
     * 
     * @param b The buffer to get the data from.
     * @param offset An offset into the buffer to get the data from.
     * @param length The length of data to write.
     * @throws IOException If there is an error while writing the data.
     */
    public void write(byte[] b, int offset, int length) throws IOException;

}