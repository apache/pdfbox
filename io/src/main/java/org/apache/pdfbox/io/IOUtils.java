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

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.filterReturnValue;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Objects.nonNull;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.RandomAccessStreamCache.StreamCacheCreateFunction;

/**
 * This class contains various I/O-related methods.
 */
public final class IOUtils
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(IOUtils.class);

    private static final StreamCacheCreateFunction streamCache = RandomAccessStreamCacheImpl::new;

    //TODO PDFBox should really use Apache Commons IO.
    private static final Optional<Consumer<ByteBuffer>> UNMAPPER;

    static
    {
        UNMAPPER = Optional.ofNullable(AccessController
                .doPrivileged((PrivilegedAction<Consumer<ByteBuffer>>) IOUtils::unmapper));
    }

    private IOUtils()
    {
        //Utility class. Don't instantiate.
    }

    /**
     * Reads the input stream and returns its contents as a byte array.
     * @param in the input stream to read from.
     * @return the byte array
     * @throws IOException if an I/O error occurs
     */
    public static byte[] toByteArray(InputStream in) throws IOException
    {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        copy(in, baout);
        return baout.toByteArray();
    }

    /**
     * Copies all the contents from the given input stream to the given output stream.
     * @param input the input stream
     * @param output the output stream
     * @return the number of bytes that have been copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(InputStream input, OutputStream output) throws IOException
    {
        byte[] buffer = new byte[4096];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer)))
        {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Populates the given buffer with data read from the input stream. If the data doesn't
     * fit the buffer, only the data that fits in the buffer is read. If the data is less than
     * fits in the buffer, the buffer is not completely filled.
     * @param in the input stream to read from
     * @param buffer the buffer to fill
     * @return the number of bytes written to the buffer
     * @throws IOException if an I/O error occurs
     */
    public static long populateBuffer(InputStream in, byte[] buffer) throws IOException
    {
        int remaining = buffer.length;
        while (remaining > 0)
        {
            int bufferWritePos = buffer.length - remaining;
            int bytesRead = in.read(buffer, bufferWritePos, remaining);
            if (bytesRead < 0)
            {
                break; //EOD
            }
            remaining -= bytesRead;
        }
        return (long) buffer.length - remaining;
    }

    /**
     * Null safe close of the given {@link Closeable} suppressing any exception.
     *
     * @param closeable to be closed
     */
    public static void closeQuietly(Closeable closeable)
    {
        try
        {
            if (closeable != null)
            {
                closeable.close();
            }
        }
        catch (IOException ioe)
        {
            LOG.debug("An exception occurred while trying to close - ignoring", ioe);
            // ignore
        }
    }

    /**
     * Try to close an IO resource and log and return if there was an exception.
     *  
     * <p>An exception is only returned if the IOException passed in is null.
     * 
     * @param closeable to be closed
     * @param logger the logger to be used so that logging appears under that log instance
     * @param resourceName the name to appear in the log output
     * @param initialException if set, this exception will be returned even where there is another
     * exception while closing the IO resource
     * @return the IOException is there was any but only if initialException is null
     */
    public static IOException closeAndLogException(Closeable closeable, Log logger, String resourceName, IOException initialException)
    {
        try
        {
            closeable.close();
        }
        catch (IOException ioe)
        {
            logger.warn("Error closing " + resourceName, ioe);
            if (initialException == null)
            {
                return ioe;
            }
        }
        return initialException;
    }

    /**
     * Unmap memory mapped byte buffers. This is a hack waiting for a proper JVM provided solution expected in java 10
     * https://bugs.openjdk.java.net/browse/JDK-4724038 The issue here is that even when closed, memory mapped byte
     * buffers hold a lock on the underlying file until GC is executes and this in turns result in an error if the user
     * tries to move or delete the file.
     * 
     * @param buf the buffer to be unmapped
     */
    public static void unmap(ByteBuffer buf)
    {
        try
        {
            if (buf != null)
            {
                UNMAPPER.ifPresent(u -> u.accept(buf));
            }
        }
        catch (Exception e)
        {
            LOG.error("Unable to unmap ByteBuffer.", e);
        }
    }

    /**
     * This is adapted from org.apache.lucene.store.MMapDirectory
     * 
     * @return
     */
    private static Consumer<ByteBuffer> unmapper()
    {
        final Lookup lookup = lookup();
        try
        {
            try
            {
                // *** sun.misc.Unsafe unmapping (Java 9+) ***
                final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                // first check if Unsafe has the right method, otherwise we can give up
                // without doing any security critical stuff:
                final MethodHandle unmapper = lookup.findVirtual(unsafeClass, "invokeCleaner",
                        methodType(void.class, ByteBuffer.class));
                // fetch the unsafe instance and bind it to the virtual MH:
                final Field f = unsafeClass.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                final Object theUnsafe = f.get(null);
                return newBufferCleaner(ByteBuffer.class, unmapper.bindTo(theUnsafe));
            }
            catch (SecurityException se)
            {
                // rethrow to report errors correctly (we need to catch it here, as we also catch RuntimeException
                // below!):
                throw se;
            }
            catch (ReflectiveOperationException | RuntimeException e)
            {
                // *** sun.misc.Cleaner unmapping (Java 8) ***
                final Class<?> directBufferClass = Class.forName("java.nio.DirectByteBuffer");

                final Method m = directBufferClass.getMethod("cleaner");
                m.setAccessible(true);
                final MethodHandle directBufferCleanerMethod = lookup.unreflect(m);
                final Class<?> cleanerClass = directBufferCleanerMethod.type().returnType();

                /*
                 * "Compile" a MH that basically is equivalent to the following code: void unmapper(ByteBuffer
                 * byteBuffer) { sun.misc.Cleaner cleaner = ((java.nio.DirectByteBuffer) byteBuffer).cleaner(); if
                 * (Objects.nonNull(cleaner)) { cleaner.clean(); } else { noop(cleaner); // the noop is needed because
                 * MethodHandles#guardWithTest always needs ELSE } }
                 */
                final MethodHandle cleanMethod = lookup.findVirtual(cleanerClass, "clean",
                        methodType(void.class));
                final MethodHandle nonNullTest = lookup
                        .findStatic(Objects.class, "nonNull",
                                methodType(boolean.class, Object.class))
                        .asType(methodType(boolean.class, cleanerClass));
                final MethodHandle noop = dropArguments(
                        constant(Void.class, null).asType(methodType(void.class)), 0, cleanerClass);
                final MethodHandle unmapper = filterReturnValue(directBufferCleanerMethod,
                        guardWithTest(nonNullTest, cleanMethod, noop))
                                .asType(methodType(void.class, ByteBuffer.class));
                return newBufferCleaner(directBufferClass, unmapper);
            }
        }
        catch (SecurityException se)
        {
            LOG.error(
                    "Unmapping is not supported because of missing permissions. Please grant at least the following permissions: RuntimePermission(\"accessClassInPackage.sun.misc\") "
                            + " and ReflectPermission(\"suppressAccessChecks\")",
                    se);

        }
        catch (ReflectiveOperationException | RuntimeException e)
        {
            LOG.error("Unmapping is not supported.", e);
        }
        return null;
    }

    private static Consumer<ByteBuffer> newBufferCleaner(final Class<?> unmappableBufferClass,
            final MethodHandle unmapper)
    {
        assert Objects.equals(methodType(void.class, ByteBuffer.class), unmapper.type());
        return (ByteBuffer buffer) -> {
            if (!buffer.isDirect())
            {
                throw new IllegalArgumentException("unmapping only works with direct buffers");
            }
            if (!unmappableBufferClass.isInstance(buffer))
            {
                throw new IllegalArgumentException(
                        "buffer is not an instance of " + unmappableBufferClass.getName());
            }
            final Throwable e = AccessController.doPrivileged((PrivilegedAction<Throwable>) () -> {
                try
                {
                    unmapper.invokeExact(buffer);
                    return null;
                }
                catch (Throwable t)
                {
                    return t;
                }
            });
            if (nonNull(e))
            {
                LOG.error("Unable to unmap the mapped buffer", e);
            }
        };
    }

    /**
     * Provides a function to create an instance of a memory only StreamCache using unrestricted main memory.
     * RandomAccessReadWriteBuffer is used as current default implementation.
     * 
     * @return a function to create an instance of a memory only StreamCache using unrestricted main memory
     */
    public static StreamCacheCreateFunction createMemoryOnlyStreamCache()
    {
        return streamCache;
    }

    /**
     * Provides a function to create an instance of a temp file only StreamCache using unrestricted size. ScratchFile is
     * used as current default implementation.
     * 
     * @return a function to create an instance of a temp file only StreamCache using unrestricted size
     */
    public static StreamCacheCreateFunction createTempFileOnlyStreamCache()
    {
        return MemoryUsageSetting.setupTempFileOnly().streamCache;
    }
}
