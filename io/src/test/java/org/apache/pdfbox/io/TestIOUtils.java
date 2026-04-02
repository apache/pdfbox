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

/* $Id$ */

package org.apache.pdfbox.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

/**
 * This is a unit test for {@link IOUtils}.
 */
class TestIOUtils
{

    /**
     * Tests {@link IOUtils#populateBuffer(java.io.InputStream, byte[]).
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testPopulateBuffer() throws IOException
    {
        byte[] data = "Hello World!".getBytes();
        byte[] buffer = new byte[data.length];
        long count = IOUtils.populateBuffer(new ByteArrayInputStream(data), buffer);
        assertEquals(12, count);

        buffer = new byte[data.length - 2]; //Buffer too small
        InputStream in = new ByteArrayInputStream(data);
        count = IOUtils.populateBuffer(in, buffer);
        assertEquals(10, count);
        byte[] leftOver = in.readAllBytes();
        assertEquals(2, leftOver.length);

        buffer = new byte[data.length + 2]; //Buffer too big
        in = new ByteArrayInputStream(data);
        count = IOUtils.populateBuffer(in, buffer);
        assertEquals(12, count);
        assertEquals(-1, in.read()); //EOD reached
    }

    /**
     * Tests {@link IOUtils#populateBuffer(java.io.InputStream, byte[])} with empty stream.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testPopulateBufferEmpty() throws IOException
    {
        byte[] buffer = new byte[10];
        InputStream in = new ByteArrayInputStream(new byte[0]);
        long count = IOUtils.populateBuffer(in, buffer);
        assertEquals(0, count);
    }

    /**
     * Tests {@link IOUtils#toByteArray(InputStream)}.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testToByteArray() throws IOException
    {
        byte[] data = "Test Data".getBytes();
        byte[] result = IOUtils.toByteArray(new ByteArrayInputStream(data));
        assertEquals(data.length, result.length);
        assertEquals(new String(data), new String(result));
    }

    /**
     * Tests {@link IOUtils#toByteArray(InputStream)} with empty stream.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testToByteArrayEmpty() throws IOException
    {
        byte[] result = IOUtils.toByteArray(new ByteArrayInputStream(new byte[0]));
        assertEquals(0, result.length);
    }

    /**
     * Tests {@link IOUtils#toByteArray(InputStream)} with large data.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testToByteArrayLarge() throws IOException
    {
        byte[] data = new byte[10000];
        for (int i = 0; i < data.length; i++)
        {
            data[i] = (byte)(i % 256);
        }
        byte[] result = IOUtils.toByteArray(new ByteArrayInputStream(data));
        assertEquals(data.length, result.length);
    }

    /**
     * Tests {@link IOUtils#copy(InputStream, OutputStream)}.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testCopy() throws IOException
    {
        byte[] data = "Copy Test Content".getBytes();
        InputStream input = new ByteArrayInputStream(data);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        long copied = IOUtils.copy(input, output);
        
        assertEquals(data.length, copied);
        assertEquals(new String(data), output.toString());
    }

    /**
     * Tests {@link IOUtils#copy(InputStream, OutputStream)} with empty stream.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testCopyEmpty() throws IOException
    {
        InputStream input = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        long copied = IOUtils.copy(input, output);
        
        assertEquals(0, copied);
        assertEquals(0, output.size());
    }

    /**
     * Tests {@link IOUtils#copy(InputStream, OutputStream)} with large data.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testCopyLarge() throws IOException
    {
        byte[] data = new byte[50000];
        for (int i = 0; i < data.length; i++)
        {
            data[i] = (byte)(i % 256);
        }
        InputStream input = new ByteArrayInputStream(data);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        long copied = IOUtils.copy(input, output);
        
        assertEquals(data.length, copied);
        assertEquals(data.length, output.size());
    }

    /**
     * Tests {@link IOUtils#closeQuietly(Closeable)} with null closeable.
     */
    @Test
    void testCloseQuietlyNull()
    {
        // Should not throw exception
        assertDoesNotThrow(() -> IOUtils.closeQuietly(null));
    }

    /**
     * Tests {@link IOUtils#closeQuietly(Closeable)} with valid closeable.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testCloseQuietly() throws IOException
    {
        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[10]);
        assertDoesNotThrow(() -> IOUtils.closeQuietly(stream));
    }

    /**
     * Tests {@link IOUtils#closeQuietly(Closeable)} suppresses exceptions.
     */
    @Test
    void testCloseQuietlySuppressesException()
    {
        // Should not throw exception even if close() throws
        assertDoesNotThrow(() -> {
            Closeable failingCloseable = () -> {
                throw new IOException("Test IOException");
            };
            IOUtils.closeQuietly(failingCloseable);
        });
    }

    /**
     * Tests {@link IOUtils#closeAndLogException(Closeable, Logger, String, IOException)} 
     * with successful close and no initial exception.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testCloseAndLogExceptionSuccess() throws IOException
    {
        Logger logger = LogManager.getLogger(TestIOUtils.class);
        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[10]);
        
        IOException result = IOUtils.closeAndLogException(stream, logger, "testResource", null);
        
        assertNull(result);
    }

    /**
     * Tests {@link IOUtils#closeAndLogException(Closeable, Logger, String, IOException)} 
     * with close exception and no initial exception.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testCloseAndLogExceptionCloseThrows() throws IOException
    {
        Logger logger = LogManager.getLogger(TestIOUtils.class);
        IOException closeException = new IOException("Close error");
        
        Closeable failingCloseable = () -> {
            throw closeException;
        };
        
        IOException result = IOUtils.closeAndLogException(failingCloseable, logger, "testResource", null);
        
        assertEquals(closeException, result);
    }

    /**
     * Tests {@link IOUtils#closeAndLogException(Closeable, Logger, String, IOException)} 
     * preserves initial exception even if close also throws.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testCloseAndLogExceptionPreservesInitialException() throws IOException
    {
        Logger logger = LogManager.getLogger(TestIOUtils.class);
        IOException initialException = new IOException("Initial error");
        
        Closeable failingCloseable = () -> {
            throw new IOException("Close error");
        };
        
        IOException result = IOUtils.closeAndLogException(failingCloseable, logger, "testResource", initialException);
        
        assertEquals(initialException, result);
    }

    /**
     * Tests {@link IOUtils#unmap(ByteBuffer)} with null buffer.
     */
    @Test
    void testUnmapNull()
    {
        // Should not throw exception
        assertDoesNotThrow(() -> IOUtils.unmap(null));
    }

    /**
     * Tests {@link IOUtils#unmap(ByteBuffer)} with heap buffer.
     */
    @Test
    void testUnmapHeapBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // Should not throw exception - heap buffers can be unmapped safely (no-op)
        assertDoesNotThrow(() -> IOUtils.unmap(buffer));
    }

    /**
     * Tests {@link IOUtils#createMemoryOnlyStreamCache()} returns non-null function.
     */
    @Test
    void testCreateMemoryOnlyStreamCache()
    {
        RandomAccessStreamCache.StreamCacheCreateFunction function = IOUtils.createMemoryOnlyStreamCache();
        assertNotNull(function);
    }

    /**
     * Tests {@link IOUtils#createTempFileOnlyStreamCache()} returns non-null function.
     */
    @Test
    void testCreateTempFileOnlyStreamCache()
    {
        RandomAccessStreamCache.StreamCacheCreateFunction function = IOUtils.createTempFileOnlyStreamCache();
        assertNotNull(function);
    }

    /**
     * Tests {@link IOUtils#createProtectedTempDir()} creates a directory.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testCreateProtectedTempDir() throws IOException
    {
        Path tempDir = IOUtils.createProtectedTempDir();
        try
        {
            assertTrue(Files.exists(tempDir), "Temporary directory should exist");
            assertTrue(Files.isDirectory(tempDir), "Path should be a directory");
            assertTrue(tempDir.getFileName().toString().startsWith("pdfbox-"), 
                    "Directory name should start with 'pdfbox-'");
        }
        finally
        {
            // Cleanup - note: shutdown hook should also handle this
            if (Files.exists(tempDir))
            {
                Files.delete(tempDir);
            }
        }
    }

    /**
     * Tests {@link IOUtils#createProtectedTempDir()} with POSIX permissions.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testCreateProtectedTempDirPermissions() throws IOException
    {
        Path tempDir = IOUtils.createProtectedTempDir();
        try
        {
            // Check if system supports POSIX permissions
            if (Files.getFileStore(tempDir).supportsFileAttributeView("posix"))
            {
                Set<PosixFilePermission> perms = Files.getPosixFilePermissions(tempDir);
                
                // Should have owner read, write, execute
                assertTrue(perms.contains(PosixFilePermission.OWNER_READ));
                assertTrue(perms.contains(PosixFilePermission.OWNER_WRITE));
                assertTrue(perms.contains(PosixFilePermission.OWNER_EXECUTE));
                
                // Should NOT have group or others permissions
                assertFalse(perms.contains(PosixFilePermission.GROUP_READ));
                assertFalse(perms.contains(PosixFilePermission.GROUP_WRITE));
                assertFalse(perms.contains(PosixFilePermission.GROUP_EXECUTE));
                assertFalse(perms.contains(PosixFilePermission.OTHERS_READ));
                assertFalse(perms.contains(PosixFilePermission.OTHERS_WRITE));
                assertFalse(perms.contains(PosixFilePermission.OTHERS_EXECUTE));
            }
        }
        finally
        {
            // Cleanup
            if (Files.exists(tempDir))
            {
                Files.delete(tempDir);
            }
        }
    }

    /**
     * Tests {@link IOUtils#createProtectedTempDir()} creates multiple unique directories.
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testCreateProtectedTempDirMultiple() throws IOException
    {
        Path tempDir1 = IOUtils.createProtectedTempDir();
        Path tempDir2 = IOUtils.createProtectedTempDir();
        
        try
        {
            assertTrue(Files.exists(tempDir1));
            assertTrue(Files.exists(tempDir2));
            // Paths should be different
            assertFalse(tempDir1.equals(tempDir2));
        }
        finally
        {
            // Cleanup
            if (Files.exists(tempDir1))
            {
                Files.delete(tempDir1);
            }
            if (Files.exists(tempDir2))
            {
                Files.delete(tempDir2);
            }
        }
    }

}
