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

package org.apache.fontbox.util.autodetect;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helps to autodetect/locate available operating system fonts. This class is based on a class provided by Apache FOP.
 * see org.apache.fop.fonts.autodetect.FontFileFinder
 */
public class FontFileFinder extends DirectoryWalker<URL>
{

    /**
     * logging instance.
     */
    private final Log log = LogFactory.getLog(FontFileFinder.class);

    private FontDirFinder fontDirFinder = null;

    /**
     * default depth limit of recursion when searching for font files.
     */
    public static final int DEFAULT_DEPTH_LIMIT = -1;

    /**
     * Default constructor.
     */
    public FontFileFinder()
    {
        this(DEFAULT_DEPTH_LIMIT);
    }

    /**
     * Constructor.
     * 
     * @param depthLimit recursion depth limit
     * 
     */
    public FontFileFinder(int depthLimit)
    {
        super(getDirectoryFilter(), getFileFilter(), depthLimit);
    }

    private FontDirFinder determineDirFinder()
    {
        final String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows"))
        {
            return new WindowsFontDirFinder();
        }
        else
        {
            if (osName.startsWith("Mac"))
            {
                return new MacFontDirFinder();
            }
            else
            {
                return new UnixFontDirFinder();
            }
        }
    }

    /**
     * Font directory filter. Currently ignores hidden directories.
     * 
     * @return IOFileFilter font directory filter
     */
    protected static IOFileFilter getDirectoryFilter()
    {
        return FileFilterUtils.and(FileFilterUtils.directoryFileFilter(),
                FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter(".")));
    }

    /**
     * Font file filter. Currently searches for files with .ttf, .ttc, .otf, and .pfb extensions.
     * 
     * @return IOFileFilter font file filter
     */
    protected static IOFileFilter getFileFilter()
    {
        return FileFilterUtils.and(FileFilterUtils.fileFileFilter(), new WildcardFileFilter(
                new String[] { "*.ttf", "*.otf", "*.pfb", "*.ttc" }, IOCase.INSENSITIVE));
    }

    /**
     * @param directory directory to handle
     * @param depth recursion depth
     * @param results collection
     * @return whether directory should be handled {@inheritDoc}
     */
    @Override
    protected boolean handleDirectory(File directory, int depth, Collection<URL> results)
    {
        return true;
    }

    /**
     * @param file file to handle
     * @param depth recursion depth
     * @param results collection {@inheritDoc}
     */
    @Override
    protected void handleFile(File file, int depth, Collection<URL> results)
    {
        try
        {
            // Looks Strange, but is actually recommended over just .URL()
            results.add(file.toURI().toURL());
        }
        catch (MalformedURLException e)
        {
            log.debug("MalformedURLException" + e.getMessage());
        }
    }

    /**
     * @param directory the directory being processed
     * @param depth the current directory level
     * @param results the collection of results objects {@inheritDoc}
     */
    @Override
    protected void handleDirectoryEnd(File directory, int depth, Collection<URL> results)
    {
        if (log.isDebugEnabled())
        {
            log.debug(directory + ": found " + results.size() + " font"
                    + ((results.size() == 1) ? "" : "s"));
        }
    }

    /**
     * Automagically finds a list of font files on local system.
     * 
     * @return List&lt;URL&gt; of font files
     * @throws IOException io exception {@inheritDoc}
     */
    public List<URL> find() throws IOException
    {
        if (fontDirFinder == null)
        {
            fontDirFinder = determineDirFinder();
        }
        List<File> fontDirs = fontDirFinder.find();
        List<URL> results = new java.util.ArrayList<URL>();
        for (File dir : fontDirs)
        {
            super.walk(dir, results);
        }
        return results;
    }

    /**
     * Searches a given directory for font files.
     * 
     * @param dir directory to search
     * @return list of font files
     * @throws IOException thrown if an I/O exception of some sort has occurred
     */
    public List<URL> find(String dir) throws IOException
    {
        List<URL> results = new java.util.ArrayList<URL>();
        File directory = new File(dir);
        if (directory.isDirectory())
        {
            super.walk(directory, results);
        }
        return results;
    }

    /**
     * Provides a list of platform specific ttf name mappings.
     * 
     * @return a font name mapping
     */
    public Map<String, String> getCommonTTFMapping()
    {
        if (fontDirFinder == null)
        {
            fontDirFinder = determineDirFinder();
        }
        return fontDirFinder.getCommonTTFMapping();
    }
}
