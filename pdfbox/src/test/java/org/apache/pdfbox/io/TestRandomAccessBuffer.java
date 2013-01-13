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

import junit.framework.TestCase;

/**
 * This is a unit test for {@link RandomAccessBuffer}.
 * 
 */
public class TestRandomAccessBuffer extends TestCase 
{

    private static final int BUFFER_SIZE = 16384;
    
    /**
     * This will test the {@link RandomAccessBuffer#read()} 
     * and {@link RandomAccessBuffer#write(int)} method.
     * 
     * @throws IOException is thrown if something went wrong.
     */
    public void testSimpleReadWrite() throws IOException
    {
        // create a buffer filled with 10 figures from 0 to 9
        RandomAccessBuffer buffer = new RandomAccessBuffer();
        for ( int i=0;i < 10;i++ )
        {
            buffer.write(i);
        }
        // jump back to the beginning of the buffer
        buffer.seek(0);
        // sum up all figures, the result should be 45
        int result = 0;
        for ( int i=0;i < 10;i++ )
        {
            result += buffer.read();
        }
        assertEquals(45, result);
    }

    /**
     * This will test the {@link RandomAccessBuffer#read(byte[], int, int)} 
     * and {@link RandomAccessBuffer#write(byte[], int, int)} method.
     * 
     * @throws IOException is thrown if something went wrong.
     */
    public void testSimpleArrayReadWrite() throws IOException
    {
        // create an array filled with 10 figures from 0 to 9
        byte[] byteArray = new byte[10];
        for ( byte i=0;i < 10;i++ )
        {
            byteArray[i] = i;
        }
        // create an empty buffer and write the array to it
        RandomAccessBuffer buffer = new RandomAccessBuffer();
        buffer.write(byteArray, 0, byteArray.length);
        // jump back to the beginning of the buffer
        buffer.seek(0);
        // read the buffer byte after byte and sum up all figures, 
        // the result should be 45
        int result = 0;
        for ( int i=0;i < 10;i++ )
        {
            result += buffer.read();
        }
        assertEquals(45, result);
        // jump back to the beginning of the buffer
        buffer.seek(0);
        // read the buffer to an array and sum up all figures, 
        // the result should be 45
        buffer.read(byteArray, 0, byteArray.length);
        result = 0;
        for ( int i=0;i < 10;i++ )
        {
            result += byteArray[i];
        }
        assertEquals(45, result);
    }

    /**
     * This will test the {@link RandomAccessBuffer#read(byte[], int, int)} 
     * and {@link RandomAccessBuffer#write(byte[], int, int)} method using
     * a couple of data to create more than one chunk.
     * 
     * @throws IOException is thrown if something went wrong.
     */
    public void testArrayReadWrite() throws IOException
    {
        // create an array filled with 16384 * "0", 16384 * "1" and 100 * "2"
        byte[] byteArray = new byte[ 2 * BUFFER_SIZE + 100];
        for (int i = BUFFER_SIZE;i < 2 * BUFFER_SIZE; i++)
        {
            byteArray[i] = 1;
        }
        for (int i = 2 * BUFFER_SIZE; i < 2 * BUFFER_SIZE + 100; i++)
        {
            byteArray[i] = 2;
        }
        // write the array to a buffer 
        RandomAccessBuffer buffer = new RandomAccessBuffer();
        buffer.write(byteArray, 0, byteArray.length);
        // jump to the beginning
        buffer.seek(0);
        // the first byte should be "0"
        assertEquals(0, buffer.read());
        
        // jump to the last byte of the first chunk, it should be "0"
        buffer.seek(BUFFER_SIZE - 1);
        assertEquals(0, buffer.read());
        
        // jump to the first byte of the second chunk, it should be "1"
        buffer.seek(BUFFER_SIZE);
        assertEquals(1, buffer.read());
        
        // jump to the end-5 of the first chunk
        buffer.seek(BUFFER_SIZE - 5);
        // read the last 5 bytes from the first and the first 5 bytes 
        // from the second chunk and sum them up. The result should be "5"
        byteArray = new byte[10];
        buffer.read(byteArray,0, byteArray.length);
        int result = 0;
        for ( int i=0;i < 10;i++ )
        {
            result += byteArray[i];
        }
        assertEquals(5, result);

        // jump to the end-5 of the second chunk
        buffer.seek(2 * BUFFER_SIZE - 5);
        // read the last 5 bytes from the second and the first 5 bytes 
        // from the third chunk and sum them up. The result should be "15"
        byteArray = new byte[10];
        buffer.read(byteArray,0, byteArray.length);
        result = 0;
        for ( int i=0;i < 10;i++ )
        {
            result += byteArray[i];
        }
        assertEquals(15, result);
    }

    /**
     * This will test if overwriting works.
     * 
     * @throws IOException is thrown if something went wrong.
     */
    public void testOverwrite() throws IOException
    {
        // create a buffer filled with 16384 * "0" and 100 * "1" 
        byte[] byteArray = new byte[ BUFFER_SIZE + 100];
        RandomAccessBuffer buffer = new RandomAccessBuffer();
        for (int i = BUFFER_SIZE;i < BUFFER_SIZE + 100; i++)
        {
            byteArray[i] = 1;
        }
        buffer.write(byteArray, 0, byteArray.length);
        
        // jump to the end-5 of the first chunk
        buffer.seek(BUFFER_SIZE - 5);
        // read the last 5 bytes from the first and the first 5 bytes 
        // from the second chunk and sum them up. The result should be "5"
        byteArray = new byte[10];
        buffer.read(byteArray,0, byteArray.length);
        int result = 0;
        for ( int i=0;i < 10;i++ )
        {
            result += byteArray[i];
        }
        assertEquals(5, result);
        
        // jump to the end-5 of the first chunk
        buffer.seek(BUFFER_SIZE - 5);
        // write 5 "2" and 5 "3" to the buffer
        for ( int i=0;i < 5;i++ )
        {
            buffer.write(2);
        }
        for ( int i=0;i < 5;i++ )
        {
            buffer.write(3);
        }
        // jump to the end-5 of the first chunk
        buffer.seek(BUFFER_SIZE - 5);
        // read the last 5 bytes from the first and the first 5 bytes 
        // from the second chunk and sum them up. The result should be "25"
        byteArray = new byte[10];
        buffer.read(byteArray,0, byteArray.length);
        result = 0;
        for ( int i=0;i < 10;i++ )
        {
            result += byteArray[i];
        }
        assertEquals(25, result);
    }
    
    public void testPDFBOX1490() throws Exception
    {
        // create a buffer filled with 16383 * "0" 
        byte[] byteArray = new byte[ BUFFER_SIZE-1];
        RandomAccessBuffer buffer = new RandomAccessBuffer();
        buffer.write(byteArray,0, byteArray.length);
        // fill the first buffer until the end
        buffer.write(0);
        // seek the current == last position in the first buffer chunk
        buffer.seek(buffer.getPosition());
    }
}
