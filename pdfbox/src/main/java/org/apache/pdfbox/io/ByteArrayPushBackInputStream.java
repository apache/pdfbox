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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * PushBackInputStream for byte arrays.
 *
 * The inheritance from PushBackInputStream is only to avoid the
 * introduction of an interface with all PushBackInputStream
 * methods. The parent PushBackInputStream is not used in any way and
 * all methods are overridden. (Thus when adding new methods to PushBackInputStream
 * override them in this class as well!)
 * unread() is limited to the number of bytes already read from this stream (i.e.
 * the current position in the array). This limitation usually poses no problem
 * to a parser, but allows for some optimization since only one array has to
 * be dealt with.
 *
 * Note: This class is not thread safe. Clients must provide synchronization
 * if needed.
 *
 * Note: Calling unread() after mark() will cause (part of) the unread data to be
 * read again after reset(). Thus do not call unread() between mark() and reset().
 *
 * @author Andreas Weiss (andreas.weiss@switzerland.org)
 * @version $Revision: 1.2 $
 */
public class ByteArrayPushBackInputStream extends PushBackInputStream
{
    private byte[] data;
    private int datapos;
    private int datalen;
    private int save;

    // dummy for base class constructor
    private static final InputStream DUMMY = new ByteArrayInputStream("".getBytes());

    /**
     * Constructor.
     * @param input Data to read from. Note that calls to unread() will
     * modify this array! If this is not desired, pass a copy.
     *
     * @throws IOException If there is an IO error.
     */
    public ByteArrayPushBackInputStream(byte[] input) throws IOException
    {
        super(DUMMY, 1);
        data = input;
        datapos = 0;
        save = datapos;
        datalen = input != null ? input.length : 0;
    }

    /**
     * This will peek at the next byte.
     *
     * @return The next byte on the stream, leaving it as available to read.
     */
    public int peek()
    {
        try
        {
            // convert negative values to 128..255
            return (data[datapos] + 0x100) & 0xff;
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            // could check this before, but this is a rare case
            // and this method is called sufficiently often to justify this
            // optimization
            return -1;
        }
    }

    /**
     * A simple test to see if we are at the end of the stream.
     *
     * @return true if we are at the end of the stream.
     */
    public boolean isEOF()
    {
        return datapos >= datalen;
    }

    /**
     * Save the state of this stream.
     * @param readlimit Has no effect.
     * @see InputStream#mark(int)
     */
    public void mark(int readlimit)
    {
        if (false)
        {
            ++readlimit; // avoid unused param warning
        }
        save = datapos;
    }

    /**
     * Check if mark is supported.
     * @return Always true.
     * @see InputStream#markSupported()
     */
    public boolean markSupported()
    {
        return true;
    }

    /**
     * Restore the state of this stream to the last saveState call.
     * @see InputStream#reset()
     */
    public void reset()
    {
        datapos = save;
    }

    /** Available bytes.
     * @see InputStream#available()
     * @return Available bytes.
     */
    public int available()
    {
        int av = datalen - datapos;
        return av > 0 ? av : 0;
    }

    /** Totally available bytes in the underlying array.
     * @return Available bytes.
     */
    public int size()
    {
        return datalen;
    }

    /**
     * Pushes back a byte.
     * After this method returns, the next byte to be read will have the value (byte)by.
     * @param by the int value whose low-order byte is to be pushed back.
     * @throws IOException - If there is not enough room in the buffer for the byte.
     * @see java.io.PushbackInputStream#unread(int)
     */
    public void unread(int by) throws IOException
    {
        if (datapos == 0)
        {
            throw new IOException("ByteArrayParserInputStream.unread(int): " +
                                  "cannot unread 1 byte at buffer position " + datapos);
        }
        --datapos;
        data[datapos] = (byte)by;
    }

    /**
     * Pushes back a portion of an array of bytes by copying it to the
     * front of the pushback buffer. After this method returns, the next byte
     * to be read will have the value b[off], the byte after that will have
     * the value b[off+1], and so forth.
     * @param buffer the byte array to push back.
     * @param off the start offset of the data.
     * @param len the number of bytes to push back.
     * @throws IOException If there is not enough room in the pushback buffer
     * for the specified number of bytes.
     * @see java.io.PushbackInputStream#unread(byte[], int, int)
     */
    public void unread(byte[] buffer, int off, int len) throws IOException
    {
        if (len <= 0 || off >= buffer.length)
        {
            return;
        }
        if (off < 0)
        {
            off = 0;
        }
        if (len > buffer.length)
        {
            len = buffer.length;
        }
        localUnread(buffer, off, len);
    }

    /**
     * Pushes back a portion of an array of bytes by copying it to the
     * front of the pushback buffer. After this method returns, the next byte
     * to be read will have the value buffer[0], the byte after that will have
     * the value buffer[1], and so forth.
     * @param buffer the byte array to push back.
     * @throws IOException If there is not enough room in the pushback buffer
     * for the specified number of bytes.
     * @see java.io.PushbackInputStream#unread(byte[])
     */
    public void unread(byte[] buffer) throws IOException
    {
        localUnread(buffer, 0, buffer.length);
    }

    /**
     * Pushes back a portion of an array of bytes by copying it to the
     * front of the pushback buffer. After this method returns, the next byte
     * to be read will have the value buffer[off], the byte after that will have
     * the value buffer[off+1], and so forth.
     * Internal method that assumes off and len to be valid.
     * @param buffer the byte array to push back.
     * @param off the start offset of the data.
     * @param len the number of bytes to push back.
     * @throws IOException If there is not enough room in the pushback buffer
     * for the specified number of bytes.
     * @see java.io.PushbackInputStream#unread(byte[], int, int)
     */
    private void localUnread(byte[] buffer, int off, int len) throws IOException
    {
        if (datapos < len)
        {
            throw new IOException("ByteArrayParserInputStream.unread(int): " +
                                  "cannot unread " + len +
                                  " bytes at buffer position " + datapos);
        }
        datapos -= len;
        System.arraycopy(buffer, off, data, datapos, len);
    }

    /**
     * Read a byte.
     * @see InputStream#read()
     * @return Byte read or -1 if no more bytes are available.
     */
    public int read()
    {
        try
        {
            // convert negative values to 128..255
            return (data[datapos++] + 0x100) & 0xff;
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            // could check this before, but this is a rare case
            // and this method is called sufficiently often to justify this
            // optimization
            datapos = datalen;
            return -1;
        }
    }

    /**
     * Read a number of bytes.
     * @see InputStream#read(byte[])
     * @param buffer the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or -1 if there
     * is no more data because the end of the stream has been reached.
     */
    public int read(byte[] buffer)
    {
        return localRead(buffer, 0, buffer.length);
    }

    /**
     * Read a number of bytes.
     * @see InputStream#read(byte[], int, int)
     * @param buffer the buffer into which the data is read.
     * @param off the start offset in array buffer at which the data is written.
     * @param len the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or -1 if there
     * is no more data because the end of the stream has been reached.
     */
    public int read(byte[] buffer, int off, int len)
    {
        if (len <= 0 || off >= buffer.length)
        {
            return 0;
        }
        if (off < 0)
        {
            off = 0;
        }
        if (len > buffer.length)
        {
            len = buffer.length;
        }
        return localRead(buffer, off, len);
    }


    /**
     * Read a number of bytes. Internal method that assumes off and len to be
     * valid.
     * @see InputStream#read(byte[], int, int)
     * @param buffer the buffer into which the data is read.
     * @param off the start offset in array buffer at which the data is written.
     * @param len the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or -1 if there
     * is no more data because the end of the stream has been reached.
     */
    public int localRead(byte[] buffer, int off, int len)
    {
        if (len == 0)
        {
            return 0; // must return 0 even if at end!
        }
        else if (datapos >= datalen)
        {
            return -1;
        }
        else
        {
            int newpos = datapos + len;
            if (newpos > datalen)
            {
                newpos = datalen;
                len = newpos - datapos;
            }
            System.arraycopy(data, datapos, buffer, off, len);
            datapos = newpos;
            return len;
        }
    }

    /**
     * Skips over and discards n bytes of data from this input stream.
     * The skip method may, for a variety of reasons, end up skipping over some
     * smaller number of bytes, possibly 0. This may result from any of a number
     * of conditions; reaching end of file before n bytes have been skipped is
     * only one possibility. The actual number of bytes skipped is returned.
     * If n is negative, no bytes are skipped.
     * @param num the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @see InputStream#skip(long)
     */
    public long skip(long num)
    {
        if (num <= 0)
        {
            return 0;
        }
        else
        {
            long newpos = datapos + num;
            if (newpos >= datalen)
            {
                num = datalen - datapos;
                datapos = datalen;
            }
            else
            {
                datapos = (int)newpos;
            }
            return num;
        }
    }

    /** Position the stream at a given index. Positioning the stream
     * at position size() will cause the next call to read() to return -1.
     *
     * @param newpos Position in the underlying array. A negative value will be
     * interpreted as 0, a value greater than size() as size().
     * @return old position.
     */
    public int seek(int newpos)
    {
        if (newpos < 0)
        {
            newpos = 0;
        }
        else if (newpos > datalen)
        {
            newpos = datalen;
        }
        int oldpos = pos;
        pos = newpos;
        return oldpos;
    }

}
