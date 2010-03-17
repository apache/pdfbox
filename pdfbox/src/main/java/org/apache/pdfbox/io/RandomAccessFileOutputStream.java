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
import java.io.OutputStream;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSNumber;

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
