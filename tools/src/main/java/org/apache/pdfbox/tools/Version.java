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
package org.apache.pdfbox.tools;

/**
 * A simple command line utility to get the version of PDFBox.
 *
 * @author Ben Litchfield
 */
final class Version
{
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
        String version = org.apache.pdfbox.util.Version.getVersion();
        if (version != null)
        {
            return version;
        }
        else
        {
            return "unknown";
        }
    }

    /**
     * This will print out the version of PDF to System.out.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

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
        System.err.println("Usage: " + Version.class.getName());
        System.exit(1);
    }
}
