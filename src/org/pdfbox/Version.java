/**
 * Copyright (c) 2005, www.pdfbox.org
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
 *
 */
package org.pdfbox;

import java.io.IOException;

import java.util.Properties;

import org.pdfbox.util.ResourceLoader;


/**
 * A simple command line utility to get the version of PDFBox.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class Version
{    
    private static final String PDFBOX_VERSION_PROPERTIES = "Resources/pdfbox.version";
    
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