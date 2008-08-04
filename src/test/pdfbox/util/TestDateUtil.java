/**
 * Copyright (c) 2006-2006, www.pdfbox.org
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
 * ANY THEORY OF LIABILIT, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 */
package test.pdfbox.util;

import java.io.IOException;
import java.util.GregorianCalendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.pdfbox.util.DateConverter;

/**
 * Test the date conversion utility.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class TestDateUtil extends TestCase
{
    /**
     * Test class constructor.
     *
     * @param name The name of the test class.
     * 
     * @throws IOException If there is an error creating the test.
     */
    public TestDateUtil( String name ) throws IOException
    {
        super( name );
    }

    /**
     * Test common date formats.
     *
     * @throws Exception when there is an exception
     */
    public void testExtract()
        throws Exception
    {
        assertEquals( DateConverter.toCalendar( "D:05/12/2005" ), new GregorianCalendar( 2005, 4, 12 ) );
        assertEquals( DateConverter.toCalendar( "5/12/2005 15:57:16" ), new GregorianCalendar( 2005, 4,12,15,57,16 ) );
    }

    /**
     * Set the tests in the suite for this test class.
     *
     * @return the Suite.
     */
    public static Test suite()
    {
        return new TestSuite( TestDateUtil.class );
    }
    
    /**
     * Command line execution.
     * 
     * @param args Command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestDateUtil.class.getName() };
        junit.textui.TestRunner.main( arg );
    }
}