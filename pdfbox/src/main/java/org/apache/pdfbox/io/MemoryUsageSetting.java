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

import java.io.File;

/**
 * Controls how memory/temporary files are used for
 * buffering streams etc.
 */
public final class MemoryUsageSetting
{
    private final boolean useMainMemory;
    private final boolean useTempFile;
    
    /** maximum number of main-memory bytes allowed to be used;
     *  <code>-1</code> means 'unrestricted' */
    private final long maxMainMemoryBytes;
    
    /** maximum number of bytes allowed for storage at all (main-memory+file);
     *  <code>-1</code> means 'unrestricted' */
    private final long maxStorageBytes;
    
    /** directory to be used for scratch file */
    private File tempDir;
    
    /**
     * Private constructor for setup buffering memory usage called by one of the setup methods.
     * 
     * @param useMainMemory if <code>true</code> main memory usage is enabled; in case of
     *                      <code>false</code> and <code>useTempFile</code> is <code>false</code> too
     *                      we set this to <code>true</code>
     * @param useTempFile if <code>true</code> using of temporary file(s) is enabled
     * @param maxMainMemoryBytes maximum number of main-memory to be used;
     *                           if <code>-1</code> means 'unrestricted';
     *                           if <code>0</code> we only use temporary file if <code>useTempFile</code>
     *                           is <code>true</code> otherwise main-memory usage will have restriction
     *                           defined by maxStorageBytes
     * @param maxStorageBytes maximum size the main-memory and temporary file(s) may have all together;
     *                        <code>0</code>  or less will be ignored; if it is less than
     *                        maxMainMemoryBytes we use maxMainMemoryBytes value instead 
     */
    private MemoryUsageSetting(boolean useMainMemory, boolean useTempFile,
                        long maxMainMemoryBytes, long maxStorageBytes)
    {
        // do some checks; adjust values as needed to get consistent setting
        boolean locUseMainMemory = useTempFile ? useMainMemory : true;
        long    locMaxMainMemoryBytes = useMainMemory ? maxMainMemoryBytes : -1;
        long    locMaxStorageBytes = maxStorageBytes > 0 ? maxStorageBytes : -1;
        
        if (locMaxMainMemoryBytes < -1)
        {
            locMaxMainMemoryBytes = -1;
        }
        
        if (locUseMainMemory && (locMaxMainMemoryBytes == 0))
        {
            if (useTempFile) {
                locUseMainMemory = false;
            }
            else
            {
                locMaxMainMemoryBytes = locMaxStorageBytes;
            }
        }
        
        if (locUseMainMemory && (locMaxStorageBytes > -1) &&
            ((locMaxMainMemoryBytes == -1) || (locMaxMainMemoryBytes > locMaxStorageBytes)))
        {
            locMaxStorageBytes = locMaxMainMemoryBytes;
        }
            
        
        this.useMainMemory = locUseMainMemory;
        this.useTempFile = useTempFile;
        this.maxMainMemoryBytes = locMaxMainMemoryBytes;
        this.maxStorageBytes = locMaxStorageBytes;
    }
    
    /**
     * Setups buffering memory usage to only use main-memory (no temporary file)
     * which is not restricted in size.
     */
    public static MemoryUsageSetting setupMainMemoryOnly()
    {
        return setupMainMemoryOnly(-1);
    }

    /**
     * Setups buffering memory usage to only use main-memory with the defined maximum.
     * 
     * @param maxMainMemoryBytes maximum number of main-memory to be used;
     *                           <code>-1</code> for no restriction;
     *                           <code>0</code> will also be interpreted here as no restriction
     */
    public static MemoryUsageSetting setupMainMemoryOnly(long maxMainMemoryBytes)
    {
        return new MemoryUsageSetting(true, false, maxMainMemoryBytes, maxMainMemoryBytes);
    }

    /**
     * Setups buffering memory usage to only use temporary file(s) (no main-memory)
     * with not restricted size.
     */
    public static MemoryUsageSetting setupTempFileOnly()
    {
        return setupTempFileOnly(-1);
    }
    
    /**
     * Setups buffering memory usage to only use temporary file(s) (no main-memory)
     * with the specified maximum size.
     * 
     * @param maxStorageBytes maximum size the temporary file(s) may have all together;
     *                        <code>-1</code> for no restriction;
     *                        <code>0</code> will also be interpreted here as no restriction
     */
    public static MemoryUsageSetting setupTempFileOnly(long maxStorageBytes)
    {
        return new MemoryUsageSetting(false, true, 0, maxStorageBytes);
    }
    
    /**
     * Setups buffering memory usage to use a portion of main-memory and additionally
     * temporary file(s) in case the specified portion is exceeded.
     * 
     * @param maxMainMemoryBytes maximum number of main-memory to be used;
     *                           if <code>-1</code> this is the same as {@link #setupMainMemoryOnly()};
     *                           if <code>0</code> this is the same as {@link #setupTempFileOnly()}
     */
    public static MemoryUsageSetting setupMixed(long maxMainMemoryBytes)
    {
        return setupMixed(maxMainMemoryBytes, -1);
    }
    
    /**
     * Setups buffering memory usage to use a portion of main-memory and additionally
     * temporary file(s) in case the specified portion is exceeded.
     * 
     * @param maxMainMemoryBytes maximum number of main-memory to be used;
     *                           if <code>-1</code> this is the same as {@link #setupMainMemoryOnly()};
     *                           if <code>0</code> this is the same as {@link #setupTempFileOnly()}
     * @param maxStorageBytes maximum size the main-memory and temporary file(s) may have all together;
     *                        <code>0</code>  or less will be ignored; if it is less than
     *                        maxMainMemoryBytes we use maxMainMemoryBytes value instead 
     */
    public static MemoryUsageSetting setupMixed(long maxMainMemoryBytes, long maxStorageBytes)
    {
        return new MemoryUsageSetting(true, true, maxMainMemoryBytes, maxStorageBytes);
    }

    /**
     * Returns a copy of this instance with the maximum memory/storage restriction
     * divided by the provided number of parallel uses.
     * 
     * @param parallelUseCount specifies the number of parallel usages for the setting to
     *                         be returned
     *                         
     * @return a copy from this instance with the maximum memory/storage restrictions
     *         adjusted to the multiple usage
     */
    public MemoryUsageSetting getPartitionedCopy(int parallelUseCount)
    {
        long newMaxMainMemoryBytes = maxMainMemoryBytes <= 0 ? maxMainMemoryBytes : 
                                                               maxMainMemoryBytes / parallelUseCount;
        long newMaxStorageBytes = maxStorageBytes <= 0 ? maxStorageBytes :
                                                         maxStorageBytes / parallelUseCount;
                
        MemoryUsageSetting copy = new MemoryUsageSetting( useMainMemory, useTempFile,
                                                          newMaxMainMemoryBytes, newMaxStorageBytes );
        copy.tempDir = tempDir;
        
        return copy;
    }
    
    /**
     * Sets directory to be used for temporary files.
     * 
     * @param tempDir directory for temporary files
     * 
     * @return this instance
     */
    public MemoryUsageSetting setTempDir(File tempDir)
    {
        this.tempDir = tempDir;
        return this;
    }
    
    /**
     * Returns <code>true</code> if main-memory is to be used.
     * 
     * <p>If this returns <code>false</code> it is ensured {@link #useTempFile()}
     * returns <code>true</code>.</p>
     */
    public boolean useMainMemory()
    {
        return useMainMemory;
    }
    
    /**
     * Returns <code>true</code> if temporary file is to be used.
     * 
     * <p>If this returns <code>false</code> it is ensured {@link #useMainMemory}
     * returns <code>true</code>.</p>
     */
    public boolean useTempFile()
    {
        return useTempFile;
    }
    
    /**
     * Returns <code>true</code> if maximum main memory is restricted to a specific
     * number of bytes.
     */
    public boolean isMainMemoryRestricted()
    {
        return maxMainMemoryBytes >= 0;
    }
    
    /**
     * Returns <code>true</code> if maximum amount of storage is restricted to a specific
     * number of bytes.
     */
    public boolean isStorageRestricted()
    {
        return maxStorageBytes > 0;
    }
    
    /**
     * Returns maximum size of main-memory in bytes to be used.
     */
    public long getMaxMainMemoryBytes()
    {
        return maxMainMemoryBytes;
    }
    
    /**
     * Returns maximum size of storage bytes to be used
     * (main-memory in temporary files all together).
     */
    public long getMaxStorageBytes()
    {
        return maxStorageBytes;
    }
    
    /**
     * Returns directory to be used for temporary files or <code>null</code>
     * if it was not set.
     */
    public File getTempDir()
    {
        return tempDir;
    }
    
    @Override
    public String toString()
    {
        return useMainMemory ?
                   (useTempFile ? "Mixed mode with max. of " + maxMainMemoryBytes + " main memory bytes" +
                                  (isStorageRestricted() ? " and max. of " + maxStorageBytes + " storage bytes" :
                                                           " and unrestricted scratch file size") :
                                  (isMainMemoryRestricted() ? "Main memory only with max. of " + maxMainMemoryBytes + " bytes" :
                                                              "Main memory only with no size restriction")):
                   (isStorageRestricted() ? "Scratch file only with max. of " + maxStorageBytes + " bytes" :
                                            "Scratch file only with no size restriction");
    }
}
