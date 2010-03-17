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
package org.apache.fontbox.ttf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * An implementation of the TTFDataStream that goes against a RAF.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.2 $
 */
public class RAFDataStream extends TTFDataStream 
{
    private RandomAccessFile raf = null;
    private File ttfFile = null;
    
    /**
     * Constructor.
     * 
     * @param name The raf file.
     * @param mode The mode to open the RAF.
     * 
     * @throws FileNotFoundException If there is a problem creating the RAF.
     * 
     * @see RandomAccessFile#RandomAccessFile( String, String )
     */
    public RAFDataStream(String name, String mode) throws FileNotFoundException
    {
        this( new File( name ), mode );
    }
    
    /**
     * Constructor.
     * 
     * @param file The raf file.
     * @param mode The mode to open the RAF.
     * 
     * @throws FileNotFoundException If there is a problem creating the RAF.
     * 
     * @see RandomAccessFile#RandomAccessFile( File, String )
     */
    public RAFDataStream(File file, String mode) throws FileNotFoundException
    {
        raf = new RandomAccessFile( file, mode );
        ttfFile = file;
    }
    
    /**
     * Read an signed short.
     * 
     * @return An signed short.
     * @throws IOException If there is an error reading the data.
     */
    public short readSignedShort() throws IOException
    {
        return raf.readShort();
    }
    
    /**
     * Get the current position in the stream.
     * @return The current position in the stream.
     * @throws IOException If an error occurs while reading the stream.
     */
    public long getCurrentPosition() throws IOException
    {
        return raf.getFilePointer();
    }
    
    /**
     * Close the underlying resources.
     * 
     * @throws IOException If there is an error closing the resources.
     */
    public void close() throws IOException
    {
        raf.close();
        raf = null;
    }
    
    /**
     * Read an unsigned byte.
     * @return An unsigned byte.
     * @throws IOException If there is an error reading the data.
     */
    public int read() throws IOException
    {
        return raf.read();
    }
    
    /**
     * Read an unsigned short.
     * 
     * @return An unsigned short.
     * @throws IOException If there is an error reading the data.
     */
    public int readUnsignedShort() throws IOException
    {
        return raf.readUnsignedShort();
    }
    
    /**
     * Read an unsigned byte.
     * @return An unsigned byte.
     * @throws IOException If there is an error reading the data.
     */
    public long readLong() throws IOException
    {
        return raf.readLong();
    }
    
    /**
     * Seek into the datasource.
     * 
     * @param pos The position to seek to.
     * @throws IOException If there is an error seeking to that position.
     */
    public void seek(long pos) throws IOException
    {
        raf.seek( pos );
    }
    
    /**
     * @see java.io.InputStream#read( byte[], int, int )
     * 
     * @param b The buffer to write to.
     * @param off The offset into the buffer.
     * @param len The length into the buffer.
     * 
     * @return The number of bytes read.
     * 
     * @throws IOException If there is an error reading from the stream.
     */
    public int read(byte[] b,
            int off,
            int len)
     throws IOException
     {
        return raf.read(b,off,len);
     }
    
    /**
     * {@inheritDoc}
     */
    public InputStream getOriginalData() throws IOException
    {
        return new FileInputStream( ttfFile );
    }
}
