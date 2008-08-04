/**
 * Copyright (c) 2003-2006, www.pdfbox.org
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
import java.io.OutputStream;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSObject;
import org.pdfbox.cos.COSNumber;

/**
 * This will write to a RandomAccessFile in the filesystem and keep track
 * of the position it is writing to and the length of the stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class RandomAccessFileOutputStream extends OutputStream
{
    private RandomAccess file;
    private long position;
    private long lengthWritten = 0;
    private COSBase expectedLength = null;

    /**
     * Constructor to create an output stream that will write to the end of a
     * random access file.
     *
     * @param raf The file to write to.
     *
     * @throws IOException If there is a problem accessing the raf.
     */
    public RandomAccessFileOutputStream( RandomAccess raf ) throws IOException
    {
        file = raf;
        //first get the position that we will be writing to
        position = raf.length();
    }

    /**
     * This will get the position in the RAF that the stream was written
     * to.
     *
     * @return The position in the raf where the file can be obtained.
     */
    public long getPosition()
    {
        return position;
    }
    
    /**
     * Get the amount of data that was actually written to the stream, in theory this
     * should be the same as the length specified but in some cases it doesn't match.
     * 
     * @return The number of bytes actually written to this stream.
     */
    public long getLengthWritten()
    {
        return lengthWritten;
    }

    /**
     * The number of bytes written to the stream.
     *
     * @return The number of bytes read to the stream.
     */
    public long getLength()
    {
        long length = -1;
        if( expectedLength instanceof COSNumber )
        {
            length = ((COSNumber)expectedLength).intValue();
        }
        else if( expectedLength instanceof COSObject &&
                 ((COSObject)expectedLength).getObject() instanceof COSNumber )
        {
            length = ((COSNumber)((COSObject)expectedLength).getObject()).intValue();
        }
        if( length == -1 )
        {
            length = lengthWritten;
        }
        return length;
    }

    /**
     * {@inheritDoc}
     */
    public void write( byte[] b, int offset, int length ) throws IOException
    {
        file.seek( position+lengthWritten );
        lengthWritten += length;
        file.write( b, offset, length );

    }
    /**
     * {@inheritDoc}
     */
    public void write( int b ) throws IOException
    {
        file.seek( position+lengthWritten );
        lengthWritten++;
        file.write( b );
    }

    /**
     * This will get the length that the PDF document specified this stream
     * should be.  This may not match the number of bytes read.
     *
     * @return The expected length.
     */
    public COSBase getExpectedLength()
    {
        return expectedLength;
    }

    /**
     * This will set the expected length of this stream.
     *
     * @param value The expected value.
     */
    public void setExpectedLength(COSBase value)
    {
        expectedLength = value;
    }
}