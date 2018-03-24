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
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helps to autodetect/locate available operating system fonts. This class is based on a class provided by Apache FOP.
 * see org.apache.fop.fonts.autodetect.FontFileFinder
 */
public class FontFileFinder
{
    private static final Log LOG = LogFactory.getLog(FontFileFinder.class);

    private FontDirFinder fontDirFinder = null;

    /**
     * Default constructor.
     */
    public FontFileFinder()
    {
    }

    private FontDirFinder determineDirFinder()
    {
        final String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows"))
        {
            return new WindowsFontDirFinder();
        }
        else if (osName.startsWith("Mac"))
        {
            return new MacFontDirFinder();
        }
        else if (osName.startsWith("OS/400"))
        {
            return new OS400FontDirFinder();
        }
        else
        {
            return new UnixFontDirFinder();
        }
    }

    /**
     * Automagically finds a list of font files on local system.
     * 
     * @return List&lt;URI&gt; of font files
     */
    public List<URI> find()
    {
        if (fontDirFinder == null)
        {
            fontDirFinder = determineDirFinder();
        }
        List<File> fontDirs = fontDirFinder.find();
        List<URI> results = new java.util.ArrayList<URI>();
        for (File dir : fontDirs)
        {
            walk(dir, results);
        }
        return results;
    }

    /**
     * Searches a given directory for font files.
     * 
     * @param dir directory to search
     * @return list&lt;URI&gt; of font files
     */
    public List<URI> find(String dir)
    {
        List<URI> results = new java.util.ArrayList<URI>();
        File directory = new File(dir);
        if (directory.isDirectory())
        {
            walk(directory, results);
        }
        return results;
    }
    
    /**
     * walk down the directory tree and search for font files.
     * 
     * @param directory the directory to start at
     * @param results names of all found font files
     */
    private void walk(File directory, List<URI> results)
    {
        // search for font files recursively in the given directory
        if (directory.isDirectory())
        {
            File[] filelist = directory.listFiles();
            if (filelist != null)
            {
                for (File file : filelist)
                {
                    if (file.isDirectory())
                    {
                        // skip hidden directories
                        if (file.getName().startsWith("."))
                        {
                            continue;
                        }
                        walk(file, results);
                    }
                    else
                    {
                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("checkFontfile check " + file);
                        }
                        if (checkFontfile(file))
                        {
                            if (LOG.isDebugEnabled())
                            {
                                LOG.debug("checkFontfile found " + file);
                            }
                            results.add(file.toURI());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Check if the given name belongs to a font file.
     * 
     * @param file the given file
     * @return true if the given filename has a typical font file ending
     */
    private boolean checkFontfile(File file)
    {
        String name = file.getName().toLowerCase(Locale.US);
        return (name.endsWith(".ttf") || name.endsWith(".otf") || name.endsWith(".pfb") || name.endsWith(".ttc")) 
                // PDFBOX-3377 exclude weird files in AIX
                && !name.startsWith("fonts.");
    }
}
