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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.Properties;

/**
 * This class will handle loading resource files(AFM/CMAP).
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.9 $
 */
public class ResourceLoader
{

    /**
     * private constructor for utility class.
     */
    private ResourceLoader()
    {
        //private utility class
    }

    /**
     * This will attempt to load the resource given the resource name.
     *
     * @param resourceName The resource to try and load.
     *
     * @return The resource as a stream or null if it could not be found.
     *
     * @throws IOException If there is an error while attempting to load the resource.
     */
    public static InputStream loadResource( String resourceName ) throws IOException
    {
        ClassLoader loader = ResourceLoader.class.getClassLoader();

        InputStream is = null;

        if( loader != null )
        {
            is = loader.getResourceAsStream( resourceName );
        }

        //see sourceforge bug 863053, this is a fix for a user that
        //needed to have PDFBox loaded by the bootstrap classloader
        if( is == null )
        {
            loader = ClassLoader.getSystemClassLoader();
            if( loader != null )
            {
                is = loader.getResourceAsStream( resourceName );
            }
        }

        if( is == null )
        {
            File f = new File( resourceName );
            if( f.exists() )
            {
                is = new FileInputStream( f );
            }
        }

        return is;
    }

    /**
     * This will attempt to load the resource given the resource name.
     *
     * @param resourceName The resource to try and load.
     * @param failIfNotFound Throw an error message if the properties were not found.
     *
     * @return The resource as a stream or null if it could not be found.
     *
     * @throws IOException If there is an error loading the properties.
     */
    public static Properties loadProperties( String resourceName, boolean failIfNotFound ) throws IOException
    {
        Properties properties = null;
        InputStream is = null;
        try
        {
            is = loadResource( resourceName );
            if( is != null )
            {
                properties = new Properties();
                properties.load( is );
            }
            else
            {
                if( failIfNotFound )
                {
                    throw new IOException( "Error: could not find resource '" + resourceName + "' on classpath." );
                }
            }
        }
        finally
        {
            if( is != null )
            {
                is.close();
            }
        }
        return properties;
    }

    /**
     * This will attempt to load the resource given the resource name.
     *
     * @param resourceName The resource to try and load.
     * @param defaults A stream of default properties.
     *
     * @return The resource as a stream or null if it could not be found.
     *
     * @throws IOException If there is an error loading the properties.
     */
    public static Properties loadProperties( String resourceName, Properties defaults ) throws IOException
    {
        InputStream is = null;
        try
        {
            is = loadResource( resourceName );
            if( is != null )
            {
                defaults.load( is );
            }
        }
        finally
        {
            if( is != null )
            {
                is.close();
            }
        }
        return defaults;
    }
}
