/**
 * Copyright (c) 2003-2004, www.pdfbox.org
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
package test.pdfbox.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.pdfbox.cos.COSDictionary;

import org.pdfbox.filter.DCTFilter;
import org.pdfbox.filter.CCITTFaxDecodeFilter;
import org.pdfbox.filter.Filter;
import org.pdfbox.filter.FilterManager;
import org.pdfbox.filter.RunLengthDecodeFilter;

/**
 * This will test all of the filters in the PDFBox system.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.8 $
 */
public class TestFilters extends TestCase
{
    private static final int BUFFER_SIZE = 2048;
    private static final COSDictionary EMPTY_DICTIONARY = new COSDictionary();

    /**
     * Constructor.
     *
     * @param name The name of the test to run.
     */
    public TestFilters( String name )
    {
        super( name );
    }

    /**
     * This will get the suite of test that this class holds.
     *
     * @return All of the tests that this class holds.
     */
    public static Test suite()
    {
        return new TestSuite( TestFilters.class );
    }

    /**
     * This will test all of the filters in the system.
     *
     * @throws IOException If there is an exception while encoding.
     */
    public void testFilters() throws IOException
    {
        FilterManager manager = new FilterManager();
        Collection filters = manager.getFilters();

        Iterator filterIter = filters.iterator();
        while( filterIter.hasNext() )
        {
            long start = System.currentTimeMillis();
            Filter filter = (Filter)filterIter.next();
            if( !(filter instanceof DCTFilter ||
                  filter instanceof CCITTFaxDecodeFilter ||
                  filter instanceof RunLengthDecodeFilter))
            {
                checkFilter( new File( "classes" ), filter );
                long stop = System.currentTimeMillis();
                System.out.println( "Time for filter " + filter.getClass().getName() + "=" + (stop-start) );
            }
        }
    }

    /**
     * This will check the filter.
     *
     * @param file The file or directory to test.
     * @param filter The filter to check.
     *
     * @throws IOException If there is an exception while encoding.
     */
    private void checkFilter( File file, Filter filter ) throws IOException
    {
        if( file.isDirectory() )
        {
            File[] subFiles = file.listFiles();
            for( int i=0; i<subFiles.length; i++ )
            {
                checkFilter( subFiles[i], filter );
            }
        }
        else
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            ByteArrayOutputStream encoded = new ByteArrayOutputStream();
            ByteArrayOutputStream decoded = new ByteArrayOutputStream();
            FileInputStream fin = new FileInputStream( file );
            int amountRead = 0;
            byte[] buffer = new byte[ BUFFER_SIZE ];
            while( (amountRead = fin.read( buffer, 0, BUFFER_SIZE )) != -1 )
            {
                output.write( buffer, 0, amountRead );
            }
            fin.close();
            byte[] original = output.toByteArray();
            filter.encode( new ByteArrayInputStream( original ), encoded, EMPTY_DICTIONARY,0 );
            filter.decode( new ByteArrayInputStream( encoded.toByteArray() ), decoded, EMPTY_DICTIONARY,0 );

            cmpArray( original, decoded.toByteArray(), filter, file );
        }
    }

    /**
     * This will compare a couple of arrays and fail if they do not match.
     *
     * @param firstArray The first array.
     * @param secondArray The second array.
     * @param filter The filter that did the encoding.
     * @param file The file that was encoded.
     */
    private void cmpArray( byte[] firstArray, byte[] secondArray, Filter filter, File file )
    {
        String fileMsg = filter.getClass().getName() + " " + file.getAbsolutePath();
        if( firstArray.length != secondArray.length )
        {
            fail( "The array lengths do not match for " + fileMsg +
                  ", firstArray length was: " + firstArray.length +
                  ", secondArray length was: " + secondArray.length);
        }

        for( int i=0; i<firstArray.length; i++ )
        {
            if( firstArray[i] != secondArray[i] )
            {
                fail( "Array data does not match " + fileMsg );
            }
        }
    }
}