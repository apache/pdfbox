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
package org.apache.pdfbox;

import java.io.IOException;

import java.util.Properties;

import org.apache.pdfbox.util.ResourceLoader;


/**
 * A simple command line utility to get the version of PDFBox.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class Version
{
    private static final String PDFBOX_VERSION_PROPERTIES =
        "org/apache/pdfbox/resources/pdfbox.properties";

    private Version()
    {
        //should not be constructed.
    }

    /**
     * Get the version of PDFBox or unknown if it is not known.
     *
     * @return The version of pdfbox that is being used.
     */
    public static String getVersion()
    {
        String version = "unknown";
        try
        {
            Properties props = ResourceLoader.loadProperties( PDFBOX_VERSION_PROPERTIES, false );
            version = props.getProperty( "pdfbox.version", version );
        }
        catch( IOException io )
        {
            //if there is a problem loading the properties then don't throw an
            //exception, 'unknown' will be returned instead.
            io.printStackTrace();
        }
        return version;
    }

    /**
     * This will print out the version of PDF to System.out.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        if( args.length != 0 )
        {
            usage();
            return;
        }
        System.out.println( "Version:" + getVersion() );
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private static void usage()
    {
        System.err.println( "usage: " + Version.class.getName() );
    }

}
