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
