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
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * FontFinder for native Windows platforms. This class is based on a class provided by Apache FOP. see
 * org.apache.fop.fonts.autodetect.WindowsFontDirFinder
 */
public class WindowsFontDirFinder implements FontDirFinder
{

    private static final Logger LOG = LogManager.getLogger(WindowsFontDirFinder.class);

    /**
     * {@inheritDoc}
     * 
     * @return a list of detected font files
     */
    @Override
    public List<File> find()
    {
        List<File> fontDirList = new java.util.ArrayList<>();
        String windir = null;
        try
        {
            windir = System.getProperty("env.windir");
        }
        catch (SecurityException e)
        {
            LOG.debug("Couldn't get Windows font directories - ignoring", e);
            // should continue if this fails
        }
        String osName = System.getProperty("os.name");
        if (windir == null)
        {
            try
            {
                windir = System.getenv("windir");
            }
            catch (SecurityException e)
            {
                LOG.debug("Couldn't get Windows font directories - ignoring", e);
                // should continue if this fails
            }
        }
        File osFontsDir;
        File psFontsDir;
        if (windir != null && windir.length() > 2)
        {
            // remove any trailing '/'
            if (windir.endsWith("/"))
            {
                windir = windir.substring(0, windir.length() - 1);
            }
            osFontsDir = new File(windir + File.separator + "FONTS");
            if (osFontsDir.exists() && osFontsDir.canRead())
            {
                fontDirList.add(osFontsDir);
            }
            psFontsDir = new File(windir.substring(0, 2) + File.separator + "PSFONTS");
            if (psFontsDir.exists() && psFontsDir.canRead())
            {
                fontDirList.add(psFontsDir);
            }
        }
        else
        {
            String windowsDirName = osName.endsWith("NT") ? "WINNT" : "WINDOWS";
            // look for true type font folder
            for (char driveLetter = 'C'; driveLetter <= 'E'; driveLetter++)
            {
                osFontsDir = new File(driveLetter + ":" + File.separator + windowsDirName
                        + File.separator + "FONTS");
                try
                {
                    if (osFontsDir.exists() && osFontsDir.canRead())
                    {
                        fontDirList.add(osFontsDir);
                        break;
                    }
                }
                catch (SecurityException e)
                {
                    LOG.debug("Couldn't get Windows font directories - ignoring", e);
                    // should continue if this fails
                }
            }
            // look for type 1 font folder
            for (char driveLetter = 'C'; driveLetter <= 'E'; driveLetter++)
            {
                psFontsDir = new File(driveLetter + ":" + File.separator + "PSFONTS");
                try
                {
                    if (psFontsDir.exists() && psFontsDir.canRead())
                    {
                        fontDirList.add(psFontsDir);
                        break;
                    }
                }
                catch (SecurityException e)
                {
                    LOG.debug("Couldn't get Windows font directories - ignoring", e);
                    // should continue if this fails
                }
            }
        }

        try
        {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isEmpty())
            {
                File localFontDir = new File(localAppData + File.separator + "Microsoft" +
                        File.separator + "Windows" + File.separator + "Fonts");
                if (localFontDir.exists() && localFontDir.canRead())
                {
                    fontDirList.add(localFontDir);
                }
            }
        }
        catch (SecurityException e)
        {
            LOG.debug("Couldn't get LOCALAPPDATA directory - ignoring", e);
            // should continue if this fails
        }

        return fontDirList;
    }
}
