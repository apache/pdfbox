/**
 * Copyright (c) 2003-2006, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 */
package org.pdfbox.util;

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