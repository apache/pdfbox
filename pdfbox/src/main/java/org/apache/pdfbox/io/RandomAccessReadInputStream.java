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

import java.io.InputStream;
import java.io.IOException;

/**
 * This class allows a section of a RandomAccessRead to be accessed as an
 * input stream.
 *
 * @author Ben Litchfield
 */
public class RandomAccessReadInputStream extends InputStream
{
    private final RandomAccessRead input;
    private long currentPosition;
    private final long endPosition;

    /**
     * Constructor.
     *
     * @param randomAccessRead The file to read the data from.
     * @param startPosition The position in the file that this stream starts.
     * @param length The length of the input stream.
     */
    public RandomAccessReadInputStream( RandomAccessRead randomAccessRead, long startPosition, long length )
    {
        input = randomAccessRead;
        currentPosition = startPosition;
        endPosition = currentPosition+length;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int available()
    {
        return (int)(endPosition - currentPosition);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        //do nothing because we want to leave the random access file open.
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        synchronized(input)
        {
            int retval = -1;
            if( currentPosition < endPosition )
            {
                input.seek( currentPosition );
                currentPosition++;
                retval = input.read();
            }
            return retval;
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
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
            synchronized(input)
            {
                input.seek( currentPosition );
                amountRead = input.read( b, offset, length );
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
    @Override
    public long skip( long amountToSkip )
    {
        long amountSkipped = Math.min( amountToSkip, available() );
        currentPosition+= amountSkipped;
        return amountSkipped;
    }
}
