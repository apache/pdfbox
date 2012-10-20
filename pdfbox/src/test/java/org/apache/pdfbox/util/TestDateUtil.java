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

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
        TimeZone timezone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            assertEquals( DateConverter.toCalendar( "D:05/12/2005" ), new GregorianCalendar( 2005, 4, 12 ) );
            assertEquals( DateConverter.toCalendar( "5/12/2005 15:57:16" ), new GregorianCalendar( 2005, 4,12,15,57,16 ) );
        } finally {
            TimeZone.setDefault(timezone);
        }
    }

    /**
     * Test case for
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-598">PDFBOX-598</a>
     */
    public void testDateConversion() throws Exception { 
        Calendar c = DateConverter.toCalendar("D:20050526205258+01'00'"); 
        assertEquals(2005, c.get(Calendar.YEAR)); 
        assertEquals(05-1, c.get(Calendar.MONTH)); 
        assertEquals(26, c.get(Calendar.DAY_OF_MONTH)); 
        assertEquals(20, c.get(Calendar.HOUR_OF_DAY)); 
        assertEquals(52, c.get(Calendar.MINUTE)); 
        assertEquals(58, c.get(Calendar.SECOND)); 
        assertEquals(0, c.get(Calendar.MILLISECOND)); 
    }

    public void testDateConverter() throws Exception {
        TimeZone timezone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            assertDate("2010-01-01T00:00:00+00:00", "D:2010");
            assertDate("2010-01-01T00:00:00+00:00", "2010");
            assertDate("2010-04-23T00:00:00+00:00", "D:20100423");
            assertDate("2010-04-23T00:00:00+00:00", "20100423");

            // assertDate("2007-04-30T19:36:47+????", "20070430193647+713'00'");
            // assertDate("2007-08-21T10:35:22+00:00", "Tue Aug 21 10:35:22 2007");
            assertDate("2008-11-04T00:00:00+00:00", "Tuesday, November 04, 2008");
            // assertDate("2007-12-17T02:02:03+00:00", "200712172:2:3");
            // assertDate("????", "Unknown");
            // assertDate("2009-03-19T20:01:22+00:00", "20090319 200122");
            //  assertDate("2008-05-12T09:47:00+00:00", "9:47 5/12/2008");

            // assertDate("2009-04-01T00:00:00+02:00", "20090401+0200");
            assertDate("2008-01-11T00:00:00+00:00", "Friday, January 11, 2008");
            // assertDate("2009-04-01T00:00:00+04:00", "20090401+04'00'");
            // assertDate("2009-04-01T00:00:00+09:00", "20090401+09'00'");
            // assertDate("2009-04-01T00:00:00-02:00", "20090401-02'00'");
            // assertDate("2009-04-01T06:01:01+00:00", "20090401 01:01:01 -0500");
            // assertDate("2000-05-26T11:25:10+00:00", "26 May 2000 11:25:10");
            // assertDate("2000-05-26T11:25:00+00:00", "26 May 2000 11:25");
        } finally {
            TimeZone.setDefault(timezone);
        }
    }

    private void assertDate(String expected, String date) throws Exception {
        Calendar calendar = DateConverter.toCalendar(date);
        assertEquals(expected, DateConverter.toISO8601(calendar));
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
