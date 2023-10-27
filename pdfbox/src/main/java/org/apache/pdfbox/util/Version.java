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

package org.apache.pdfbox.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Exposes PDFBox version.
 */
public final class Version
{
    private static final Logger LOG = LogManager.getLogger(Version.class);

    private static final String PDFBOX_VERSION_PROPERTIES =
            "/org/apache/pdfbox/resources/version.properties";

    private Version()
    {
        // static helper
    }

    /**
     * Returns the version of PDFBox.
     * 
     * @return the version of PDFBox
     */
    public static String getVersion()
    {
        try (InputStream resourceAsStream = Version.class.getResourceAsStream(PDFBOX_VERSION_PROPERTIES);
             InputStream is = new BufferedInputStream(resourceAsStream))
        {
            Properties properties = new Properties();
            properties.load(is);
            return properties.getProperty("pdfbox.version", null);
        }
        catch (IOException io)
        {
            LOG.debug("Unable to read version from properties - returning null", io);
            return null;
        }
    }
}
