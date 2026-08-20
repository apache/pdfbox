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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;

import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.io.RandomAccessStreamCache.StreamCacheCreateFunction;

/**
 * This class contains various I/O-related methods.
 */
public final class IOUtils
{
    /**
     * Log instance.
     */
    private static final Logger LOG = LogManager.getLogger(IOUtils.class);

    private static final StreamCacheCreateFunction streamCache = RandomAccessStreamCacheImpl::new;

    //TODO PDFBox should really use Apache Commons IO.
    private static final Optional<Consumer<ByteBuffer>> UNMAPPER;

    // POSIX file permissions for temporary files and directories (owner read/write/execute only)
    private static final Set<PosixFilePermission> POSIX_DIR_PERMS =
        PosixFilePermissions.fromString("rwx------");
    private static final Set<PosixFilePermission> POSIX_FILE_PERMS =
        PosixFilePermissions.fromString("rw-------");

    // Derived FileAttribute wrappers for creation-time use
    private static final FileAttribute<Set<PosixFilePermission>> POSIX_DIR_PERMISSIONS =
        PosixFilePermissions.asFileAttribute(POSIX_DIR_PERMS);
    private static final FileAttribute<Set<PosixFilePermission>> POSIX_FILE_PERMISSIONS =
        PosixFilePermissions.asFileAttribute(POSIX_FILE_PERMS);

    private static final List<Path> TEMP_DIRS_TO_DELETE = Collections.synchronizedList(new ArrayList<>());
    private static final AtomicBoolean SHUTDOWN_HOOK_REGISTERED = new AtomicBoolean(false);


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
     * @deprecated use {@link InputStream#readAllBytes()} instead
     */
    @Deprecated(since="4.0.0", forRemoval=true)
    public static byte[] toByteArray(InputStream in) throws IOException
    {
        return in.readAllBytes();
    }

    /**
     * Copies all the contents from the given input stream to the given output stream.
     * @param input the input stream
     * @param output the output stream
     * @return the number of bytes that have been copied
     * @throws IOException if an I/O error occurs
     * @deprecated use {@link InputStream#transferTo(OutputStream)} instead
     */
    @Deprecated(since="4.0.0", forRemoval=true)
    public static long copy(InputStream input, OutputStream output) throws IOException
    {
        return input.transferTo(output);
    }

    /**
     * Populates the given buffer with data read from the input stream. If the data doesn't
     * fit the buffer, only the data that fits in the buffer is read. If the data is less than
     * fits in the buffer, the buffer is not completely filled.
     * @param in the input stream to read from
     * @param buffer the buffer to fill
     * @return the number of bytes written to the buffer
     * @throws IOException if an I/O error occurs
     * @deprecated use {@link InputStream#readNBytes(byte[], int, int)} or {@link InputStream#readNBytes(int)} instead
     */
    @Deprecated(since="4.0.0", forRemoval=true)
    public static long populateBuffer(InputStream in, byte[] buffer) throws IOException
    {
        return in.readNBytes(buffer, 0, buffer.length);
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
    public static IOException closeAndLogException(Closeable closeable, Logger logger, String resourceName, IOException initialException)
    {
        try
        {
            closeable.close();
        }
        catch (IOException ioe)
        {
            logger.warn("Error closing {}", resourceName, ioe);
            if (initialException == null)
            {
                return ioe;
            }
        }
        return initialException;
    }

    /**
     * Unmap memory mapped byte buffers. This is a hack waiting for a proper JVM provided solution
     * mentioned in
     * <a href="https://bugs.openjdk.java.net/browse/JDK-4724038">JDK-4724038: Add unmap method to
     * MappedByteBuffer</a>. The issue here is that even when closed, memory mapped byte buffers
     * hold a lock on the underlying file until GC is executing and this in turns result in an error
     * if the user tries to move or delete the file.
     *
     * @param buf the buffer to be unmapped
     */
    public static void unmap(ByteBuffer buf)
    {
        try
        {
            // HeapByteBuffers don't need to be unmapped, and unmapping only works for direct buffers,
            //  so we can skip it in that case.
            if (buf != null && buf.isDirect())
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

                final MethodType objectType = methodType(boolean.class, Object.class);
                final MethodType cleanerType = methodType(boolean.class, cleanerClass);
                final MethodHandle nonNullMethodHandle = lookup
                        .findStatic(Objects.class, "nonNull", objectType);
                final MethodHandle nonNullTest = nonNullMethodHandle.asType(cleanerType);

                final MethodHandle noop = dropArguments(
                        constant(Void.class, null).asType(methodType(void.class)), 0, cleanerClass);
                final MethodType voidBufferType = methodType(void.class, ByteBuffer.class);
                final MethodHandle guardMethodHandle = guardWithTest(nonNullTest, cleanMethod, noop);
                final MethodHandle unmapper = filterReturnValue(directBufferCleanerMethod, guardMethodHandle)
                        .asType(voidBufferType);

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
                // defensive check, should not happen as we only call this method with direct buffers
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

    /**
     * Creates a temporary directory in the default temporary-file directory 
     * with owner-only permissions and registers a shutdown hook to delete it on JVM exit.
     * 
     * <p>Note: This method is designed to be used for storing temporary files that may contain sensitive data
     * in a temporary directories with restricted permissions, to mitigate the risk of unauthorized access by
     * other users or processes on the same system. Used e.g. by PDFDebugger.</p>
     * 
     * @return the path to the created temporary directory
     * @throws IOException if an I/O error occurs during directory creation or permission setting
     */
    public static Path createProtectedTempDir() throws IOException
    {
        Path tempPath;
        // Set owner-only permissions at file creation time if possible, to minimize the time window where
        // the file has default permissions.
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix"))
        {
            tempPath = Files.createTempDirectory("pdfbox-", POSIX_DIR_PERMISSIONS);
        }
        else
        {
            // S5443: permissions are immediately restricted to owner-only by
            // applyOwnerOnlyPermissions(), mitigating the default-permission risk.
            @SuppressWarnings("java:S5443")
            Path p = Files.createTempDirectory("pdfbox-");
            tempPath = p;
            applyOwnerOnlyPermissions(tempPath, true);
        }

        registerForDeletion(tempPath);

        return tempPath;
    }

    private static void registerForDeletion(Path path) {
        TEMP_DIRS_TO_DELETE.add(path);
        // use shutdown hook instead of deleteOnExit() to ensure deletion
        // of the entire directory in case of not automatically deleted on 
        // JVM exit (e.g. due to open file handles or when the temp directory is not empty)
        if (SHUTDOWN_HOOK_REGISTERED.compareAndSet(false, true))
        {
            Runtime.getRuntime().addShutdownHook(new Thread(() ->
                TEMP_DIRS_TO_DELETE.forEach(IOUtils::deletePathRecursively)
            ));
        }
    }

    private static void deletePathRecursively(Path path) {
        try (Stream<Path> entries = Files.walk(path))
        {
            entries.sorted(Comparator.reverseOrder())
                // we are using File.delete() on purpose over Files.deleteIfExists() which would be prefered in general, 
                // as it's throwing a checked exception. As we are doing that in a shutdown hook there is not much we can
                // do about it and a logger might no longer be available.
                .forEach(p -> p.toFile().delete());
        }
        catch (IOException ignored) {}
    }

    /**
     * Creates a temporary file in the specified directory (or default temporary-file directory 
     * if null) with owner-only permissions.
     * 
     * <p>This method attempts to set owner-only permissions at file creation time when supported,
     * to minimize the time window during which the file may have default (world-readable) permissions.
     * On POSIX systems (Linux, macOS), permissions are set during creation. On Windows, permissions
     * are set after file creation via ACL.</p>
     * 
     * <p>Note: This method is designed for storing temporary files that may contain sensitive data
     * in a temporary directory with restricted permissions, to mitigate the risk of unauthorized 
     * access by other users or processes on the same system. However, unlike {@link #createProtectedTempDir()},
     * this method does NOT automatically delete the file on JVM shutdown. The caller is responsible 
     * for deleting the temporary file when no longer needed. Used e.g. by PDFDebugger.</p>
     * 
     * @param dir the directory in which to create the temporary file, or null to use the default 
     *            temporary-file directory
     * @param prefix the prefix string to be used in generating the file's name; may be null
     * @param suffix the suffix string to be used in generating the file's name; may be null
     * @return the path to the created temporary file with owner-only permissions
     * @throws IOException if an I/O error occurs during file creation or permission setting
     * @throws SecurityException if a security manager is installed and denies access
     * @see #createProtectedTempDir()
     * @see Files#createTempFile(Path, String, String, FileAttribute[])
     */
    public static Path createProtectedTempFile(Path dir, String prefix, String suffix) throws IOException
    {
        // Set owner-only permissions at file creation time if possible, to minimize the time window where
        // the file has default permissions.
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix"))
        {
            return dir == null 
                ? Files.createTempFile(prefix, suffix, POSIX_FILE_PERMISSIONS) 
                : Files.createTempFile(dir, prefix, suffix, POSIX_FILE_PERMISSIONS);
        }            
        // S5443: permissions are immediately restricted to owner-only by
        // applyOwnerOnlyPermissions(), mitigating the default-permission risk.
        @SuppressWarnings("java:S5443")
        Path tempFile = dir == null 
            ? Files.createTempFile(prefix, suffix) 
            : Files.createTempFile(dir, prefix, suffix);
        applyOwnerOnlyPermissions(tempFile, false);
        return tempFile;
    }

    /**
     * Applies owner-only permissions to a file or directory in a platform-specific manner.
     * 
     * <p>This method ensures that the specified file or directory is readable and writable only by its owner,
     * with no permissions granted to group or others. The implementation differs based on the underlying filesystem:</p>
     * 
     * <ul>
     *   <li><b>POSIX systems (Linux, macOS, Unix):</b> Sets permissions to {@code rwx------} for directories
     *       or {@code rw-------} for files using POSIX file attributes.</li>
     *   <li><b>Windows systems:</b> Replaces the entire ACL with a single owner-only ALLOW entry granting full control.
     *       If ACL is not supported, falls back to using {@link File#setReadable(boolean, boolean)},
     *       {@link File#setWritable(boolean, boolean)}, and {@link File#setExecutable(boolean, boolean)}.</li>
     * </ul>
     * 
     * <p>If permissions cannot be set successfully on Windows systems, a warning is logged but no exception is thrown.</p>
     * 
     * @param path the file or directory to apply owner-only permissions to
     * @param isDirectory {@code true} if the path is a directory and should have execute permissions;
     *                    {@code false} if it is a file
     * @throws IOException if an I/O error occurs while setting POSIX permissions or accessing the file
     * @throws SecurityException if a security manager is installed and denies access to the file
     * @see Files#setPosixFilePermissions(Path, Set)
     * @see Files#getFileAttributeView(Path, Class, LinkOption...)
     */
    private static void applyOwnerOnlyPermissions(Path path, boolean isDirectory) throws IOException
    {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix"))
        {
            Set<PosixFilePermission> permissions = isDirectory ? POSIX_DIR_PERMS : POSIX_FILE_PERMS;
            Files.setPosixFilePermissions(path, permissions);
        }
        else
        {
            // Windows — replace the entire ACL with a single owner-only ALLOW entry
            AclFileAttributeView aclView =
            Files.getFileAttributeView(path, AclFileAttributeView.class);

            if (aclView == null)
            {
                File pathAsFile = path.toFile();
                boolean isReadable = pathAsFile.setReadable(true, true);
                boolean isWritable = pathAsFile.setWritable(true, true);
                boolean isProtected = isReadable && isWritable;
                if (isDirectory)
                {
                    isProtected &= pathAsFile.setExecutable(true, true);
                } 
                if (!isProtected)
                {
                    LOG.warn("Unable to set owner-only permissions on: {}. " +
                            "Please ensure that the file or directory is protected against unauthorized access.",
                            path);
                }
                return;
            }

            UserPrincipal owner = aclView.getOwner();

            Set<AclEntryPermission> aclPermissions = new HashSet<>(Set.of(
                AclEntryPermission.READ_DATA,
                AclEntryPermission.WRITE_DATA,
                AclEntryPermission.APPEND_DATA,
                AclEntryPermission.READ_NAMED_ATTRS,
                AclEntryPermission.WRITE_NAMED_ATTRS,
                AclEntryPermission.READ_ATTRIBUTES,
                AclEntryPermission.WRITE_ATTRIBUTES,
                AclEntryPermission.DELETE,
                AclEntryPermission.READ_ACL,
                AclEntryPermission.WRITE_ACL,
                AclEntryPermission.SYNCHRONIZE
            ));

            if (isDirectory)
            {
                aclPermissions.add(AclEntryPermission.EXECUTE);
                aclPermissions.add(AclEntryPermission.DELETE_CHILD);
            }

            AclEntry ownerFullControl = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(aclPermissions)
                .build();

            // Set so that only the owner has permissions, and remove any inherited ACL entries
            aclView.setAcl(Collections.singletonList(ownerFullControl));
        }
    }
}
