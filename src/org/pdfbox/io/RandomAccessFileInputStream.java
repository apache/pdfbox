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

import java.io.InputStream;
import java.io.IOException;

/**
 * This class allows a section of a RandomAccessFile to be accessed as an
 * input stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class RandomAccessFileInputStream extends InputStream
{
    private RandomAccess file;
    private long currentPosition;
    private long endPosition;

    /**
     * Constructor.
     *
     * @param raFile The file to read the data from.
     * @param startPosition The position in the file that this stream starts.
     * @param length The length of the input stream.
     */
    public RandomAccessFileInputStream( RandomAccess raFile, long startPosition, long length )
    {
        file = raFile;
        currentPosition = startPosition;
        endPosition = currentPosition+length;
    }
    /**
     * {@inheritDoc}
     */
    public int available()
    {
        return (int)(endPosition - currentPosition);
    }
    /**
     * {@inheritDoc}
     */
    public void close()
    {
        //do nothing because we want to leave the random access file open.
    }
    /**
     * {@inheritDoc}
     */
    public int read() throws IOException
    {
        synchronized(file)
        {
            int retval = -1;
            if( currentPosition < endPosition )
            {
                file.seek( currentPosition );
                currentPosition++;
                retval = file.read();
            }
            return retval;
        }
    }
    /**
     * {@inheritDoc}
     */
    public int read( byte[] b, int offset, int length ) throws IOException
    {
        //only allow a read of the amount available.
        if( length > available() )
        {
            length = available();
        }
        int amountRead = -1;
        //only read if there are bytes actually available, otherwise
        //return -1 if the EOF has been reached.
        if( available() > 0 )
        {
            synchronized(file)
            {
                file.seek( currentPosition );
                amountRead = file.read( b, offset, length );
            }
        }
        //update the current cursor position.
        if( amountRead > 0 )
        {
            currentPosition += amountRead;
        }
        return amountRead;
    }

    /**
     * {@inheritDoc}
     */
    public long skip( long amountToSkip )
    {
        long amountSkipped = Math.min( amountToSkip, available() );
        currentPosition+= amountSkipped;
        return amountSkipped;
    }
}