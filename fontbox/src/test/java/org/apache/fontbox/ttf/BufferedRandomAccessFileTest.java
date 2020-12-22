/*
 * Copyright 2016 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * @author Cameron Rollhieser
 * @author Tilman Hausherr
 */
class BufferedRandomAccessFileTest
{

    /**
     * Before solving PDFBOX-3605, this test never ended.
     * 
     * @throws IOException
     */
    @Test
    void ensureReadFinishes() throws IOException
    {
        final File file = File.createTempFile("apache-pdfbox", ".dat");

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file)))
        {
            final String content = "1234567890";
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        final byte[] readBuffer = new byte[2];
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(file, "r", 4))
        {
            int amountRead;
            int totalAmountRead = 0;
            while ((amountRead = braf.read(readBuffer, 0, 2)) != -1)
            {
                totalAmountRead += amountRead;
            }
            assertEquals(10, totalAmountRead);
        }
        file.delete();
    }

    /**
     * Test several reading patterns, both reading within a buffer and across buffer.
     *
     * @throws IOException
     */
    @Test
    void testReadBuffer() throws IOException
    {
        final File file = File.createTempFile("apache-pdfbox", ".dat");

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file)))
        {
            final String content = "012345678A012345678B012345678C012345678D";
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        final byte[] readBuffer = new byte[40];
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(file, "r", 10))
        {
            int count = 4;
            int bytesRead = braf.read(readBuffer, 0, count);
            assertEquals(4, braf.getFilePointer());
            assertEquals(count, bytesRead);
            assertEquals("0123", new String(readBuffer, 0, count));
            
            count = 6;
            bytesRead = braf.read(readBuffer, 0, count);
            assertEquals(10, braf.getFilePointer());
            assertEquals(count, bytesRead);
            assertEquals("45678A", new String(readBuffer, 0, count));
            
            count = 10;
            bytesRead = braf.read(readBuffer, 0, count);
            assertEquals(20, braf.getFilePointer());
            assertEquals(count, bytesRead);
            assertEquals("012345678B", new String(readBuffer, 0, count));
            
            count = 10;
            bytesRead = braf.read(readBuffer, 0, count);
            assertEquals(30, braf.getFilePointer());
            assertEquals(count, bytesRead);
            assertEquals("012345678C", new String(readBuffer, 0, count));
            
            count = 10;
            bytesRead = braf.read(readBuffer, 0, count);
            assertEquals(40, braf.getFilePointer());
            assertEquals(count, bytesRead);
            assertEquals("012345678D", new String(readBuffer, 0, count));
            
            assertEquals(-1, braf.read());
            
            braf.seek(0);
            braf.read(readBuffer, 0, 7);
            assertEquals(7, braf.getFilePointer());
            
            count = 16;
            bytesRead = braf.read(readBuffer, 0, count);
            assertEquals(23, braf.getFilePointer());
            assertEquals(count, bytesRead);
            assertEquals("78A012345678B012", new String(readBuffer, 0, count));
            
            bytesRead = braf.read(readBuffer, 0, 99);
            assertEquals(40, braf.getFilePointer());
            assertEquals(17, bytesRead);
            assertEquals("345678C012345678D", new String(readBuffer, 0, 17));
            
            assertEquals(-1, braf.read());
            
            braf.seek(0);
            braf.read(readBuffer, 0, 7);
            assertEquals(7, braf.getFilePointer());
            
            count = 23;
            bytesRead = braf.read(readBuffer, 0, count);
            assertEquals(30, braf.getFilePointer());
            assertEquals(count, bytesRead);
            assertEquals("78A012345678B012345678C", new String(readBuffer, 0, count));
            
            braf.seek(0);
            braf.read(readBuffer, 0, 10);
            assertEquals(10, braf.getFilePointer());
            count = 23;
            bytesRead = braf.read(readBuffer, 0, count);
            assertEquals(33, braf.getFilePointer());
            assertEquals(count, bytesRead);
            assertEquals("012345678B012345678C012", new String(readBuffer, 0, count));
        }

        file.delete();
    }
}
