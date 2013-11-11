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
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.cos.COSString;

/**
 * Test the date conversion utility.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author <a href="mailto:zweibieren@ahoo.com">Fred Hansen</a>
 * 
 */
public class TestDateUtil extends TestCase
{
    private static final int MINS = 60*1000, HRS = 60*MINS;
    // expect parse fail
    private static final int BAD = -666;  
    
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

    
    ////////////////////////////////////////////////////
    // Test body follows
    
    
    /**
     * Test common date formats.
     *
     * @throws Exception when there is an exception
     */
    public void testExtract() throws Exception
    {
        TimeZone timezone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try 
        {
            assertCalendarEquals( new GregorianCalendar( 2005, 4, 12 ), 
                    DateConverter.toCalendar( "D:05/12/2005" ) );
            assertCalendarEquals( new GregorianCalendar( 2005, 4,12,15,57,16 ), 
                    DateConverter.toCalendar( "5/12/2005 15:57:16" ) );
        }
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
        finally 
        {
            TimeZone.setDefault(timezone);
        }
        // check that new toCalendar gives NullPointer for a null arg
        try 
        { 
            DateConverter.toCalendar(null, null);
            assertNotNull(null);    // failed to have expected exception
        } 
        catch (NullPointerException ex) 
        {
            // expected outcome
        }   
    }
    
    /**
     * Calendar.equals test case.
     * 
     * @param expect the expected calendar value
     * @param was the calendar value to be checked
     */
    public void assertCalendarEquals(Calendar expect, Calendar was) 
    {
        assertEquals( expect.getTimeInMillis(), was.getTimeInMillis() );
        assertEquals( expect.getTimeZone().getRawOffset(), 
                was.getTimeZone().getRawOffset() );
    }
    
    /**
     * Test case for
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-598">PDFBOX-598</a>.
     * 
     * @throws IOException if something went wrong.
     */
    public void testDateConversion() throws IOException 
    { 
        Calendar c = DateConverter.toCalendar("D:20050526205258+01'00'"); 
        assertEquals(2005, c.get(Calendar.YEAR)); 
        assertEquals(05-1, c.get(Calendar.MONTH)); 
        assertEquals(26, c.get(Calendar.DAY_OF_MONTH)); 
        assertEquals(20, c.get(Calendar.HOUR_OF_DAY)); 
        assertEquals(52, c.get(Calendar.MINUTE)); 
        assertEquals(58, c.get(Calendar.SECOND)); 
        assertEquals(0, c.get(Calendar.MILLISECOND)); 
    }

    /**
     * Check toCalendar.
     * @param yr expected year value
     *  If an IOException is the expected result, yr should be null
     * @param mon expected month value
     * @param day expected dayofmonth value
     * @param hr expected hour value
     * @param min expected minute value
     * @param sec expected second value
     * @param tz represents expected timezone offset 
     * @param orig  A date to be parsed.
     * @throws Exception If an unexpected error occurs.
     */
    private static void checkParse(int yr, int mon, int day, 
                int hr, int min, int sec, int offset,  
                String orig) throws Exception 
    {
        String pdfDate = String.format("D:%04d%02d%02d%02d%02d%02d%+03d'00'", 
                yr,mon,day,hr,min,sec,offset);
        String iso8601Date = String.format("%04d-%02d-%02d"
                + "T%02d:%02d:%02d%+03d:00", 
                yr,mon,day,hr,min,sec,offset);
        Calendar cal = null;
        try 
        {
            cal = DateConverter.toCalendar(orig);
        }
        catch (IOException ex) 
        {
            assertEquals(yr, BAD);
        }
        if (cal != null) 
        {
            assertEquals(iso8601Date, DateConverter.toISO8601(cal));
            assertEquals(pdfDate, DateConverter.toString(cal));
        }
        // new toCalendar()
        cal = DateConverter.toCalendar(orig, null);
        if (yr == BAD) 
        {
            assertEquals(cal.get(Calendar.YEAR), DateConverter.INVALID_YEAR);
        }
        else
        {
            assertEquals(pdfDate, DateConverter.toString(cal));
        }
    }

    /**
     * Test dates in various formats.
     * Years differ to make it easier to find failures.
     * @throws Exception none expected
     */
    public void testDateConverter() throws Exception 
    {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            checkParse(2010, 4,23, 0, 0, 0, 0, "D:20100423");
            checkParse(2011, 4,23, 0, 0, 0, 0, "20110423");
            checkParse(2012, 1, 1, 0, 0, 0, 0, "D:2012");
            checkParse(2013, 1, 1, 0, 0, 0, 0, "2013");

            // PDFBOX-1219
            checkParse(2001, 1,31,10,33, 0, +1,  "2001-01-31T10:33+01:00  ");   
            // PDFBOX-465
            checkParse(2002, 5,12, 9,47, 0, 0, "9:47 5/12/2002");  
            // PDFBOX-465
            checkParse(2003,12,17, 2, 2, 3, 0, "200312172:2:3"); 
            // PDFBOX-465
            checkParse(2009, 3,19,20, 1,22, 0, "  20090319 200122");  

            checkParse(2014, 4, 1, 0, 0, 0, +2, "20140401+0200");
            // "EEEE, MMM dd, yy",
            checkParse(2115, 1,11, 0, 0, 0, 0, "Friday, January 11, 2115");  
            // "EEEE, MMM dd, yy",
            checkParse(1915, 1,11, 0, 0, 0, 0, "Monday, Jan 11, 1915");  
            // "EEEE, MMM dd, yy",
            checkParse(2215, 1,11, 0, 0, 0, 0, "Wed, January 11, 2215");  
            // "EEEE, MMM dd, yy",
            checkParse(2015, 1,11, 0, 0, 0, 0, " Sun, January 11, 2015 ");  
            checkParse(2016, 4, 1, 0, 0, 0, +4, "20160401+04'00'");
            checkParse(2017, 4, 1, 0, 0, 0, +9, "20170401+09'00'");
            checkParse(2018, 4, 1, 0, 0, 0, -2, "20180401-02'00'");
            checkParse(2019, 4, 1, 6, 1, 1, -11, "20190401 6:1:1 -1100");
            checkParse(2020, 5,26,11,25,10, 0, "26 May 2020 11:25:10");
            checkParse(2021, 5,26,11,23, 0, 0, "26 May 2021 11:23");

            // try dates invalid due to out of limit values
            checkParse(BAD, 0, 0, 0, 0, 0,  0,  "Tuesday, May 32 2000 11:27 UCT");
            checkParse(BAD, 0, 0, 0, 0, 0,  0,  "32 May 2000 11:25");
            checkParse(BAD, 0, 0, 0, 0, 0,  0,  "Tuesday, May 32 2000 11:25");
            checkParse(BAD, 0, 0, 0, 0, 0,  0,  "19921301 11:25");
            checkParse(BAD, 0, 0, 0, 0, 0,  0,  "19921232 11:25");
            checkParse(BAD, 0, 0, 0, 0, 0,  0,  "19921001 11:60");
            checkParse(BAD, 0, 0, 0, 0, 0,  0,  "19920401 24:25");
            
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 
            "20070430193647+713'00' illegal tz hr");  // PDFBOX-465
            checkParse(BAD, 0, 0, 0, 0, 0,  0, "nodigits");
            checkParse(BAD, 0, 0, 0, 0, 0,  0, "Unknown"); // PDFBOX-465
            checkParse(BAD, 0, 0, 0, 0, 0,  0, "333three digit year");
            
            checkParse(2000, 2,29, 0, 0, 0, 0, "2000 Feb 29"); // valid date
            checkParse(2000, 2,29, 0, 0, 0,+11, " 2000 Feb 29 GMT + 11:00"); // valid date
            checkParse(BAD, 0, 0, 0, 0, 0,  0, "2100 Feb 29 GMT+11"); // invalid date
            checkParse(2012, 2,29, 0, 0, 0,+11, "2012 Feb 29 GMT+11"); // valid date
            checkParse(BAD, 0, 0, 0, 0, 0,  0, "2012 Feb 30 GMT+11"); // invalid date

            checkParse(1970,12,23, 0, 8, 0,  0, "1970 12 23:08");  // test ambiguous date 
            
            // cannot have P for PM
            // cannot have Sat. instead of Sat
            // EST works, but EDT does not; EST is a special kludge in Java
            
            // test cases for all entries on old formats list
            //  "E, dd MMM yyyy hh:mm:ss a"  
            checkParse(1971, 7, 6, 17, 22, 1, 0, "Tuesday, 6 Jul 1971 5:22:1 PM"); 
            //  "EE, MMM dd, yyyy hh:mm:ss a"
            checkParse(1972, 7, 6, 17, 22, 1, 0, "Thu, July 6, 1972 5:22:1 pm");   
            //  "MM/dd/yyyy hh:mm:ss"
            checkParse(1973, 7, 6, 17, 22, 1, 0, "7/6/1973 17:22:1");   
            //  "MM/dd/yyyy"
            checkParse(1974, 7, 6, 0, 0, 0, 0, "7/6/1974");   
            //  "yyyy-MM-dd'T'HH:mm:ss'Z'"
            checkParse(1975, 7, 6, 17, 22, 1, -10, "1975-7-6T17:22:1-1000");   
            //  "yyyy-MM-dd'T'HH:mm:ssz"
            checkParse(1976, 7, 6, 17, 22, 1, -4, "1976-7-6T17:22:1GMT-4");   
            //  "yyyy-MM-dd'T'HH:mm:ssz"
            checkParse(BAD, 7, 6, 17, 22, 1, -4, "2076-7-6T17:22:1EDT");   // "EDT" is not a known tz ID
            //  "yyyy-MM-dd'T'HH:mm:ssz"
            checkParse(1960, 7, 6, 17, 22, 1, -5, "1960-7-6T17:22:1EST");   // "EST" does not have a DST rule
           //  "EEEE, MMM dd, yyyy"
            checkParse(1977, 7, 6, 0, 0, 0, 0, "Wednesday, Jul 6, 1977");   
            //  "EEEE MMM dd, yyyy HH:mm:ss"
            checkParse(1978, 7, 6, 17, 22, 1, 0, "Thu Jul 6, 1978 17:22:1");   
            //  "EEEE MMM dd HH:mm:ss z yyyy"
            checkParse(1979, 7, 6, 17, 22, 1, +8, "Friday July 6 17:22:1 GMT+08:00 1979");   
            //  "EEEE, MMM dd, yyyy 'at' hh:mma"
            checkParse(1980, 7, 6, 16, 23, 0, 0, "Sun, Jul 6, 1980 at 4:23pm");   
            //  "EEEEEEEEEE, MMMMMMMMMMMM dd, yyyy"
            checkParse(1981, 7, 6, 0, 0, 0, 0, "Monday, July 6, 1981");   
            //  "dd MMM yyyy hh:mm:ss"
            checkParse(1982, 7, 6, 17, 22, 1, 0, "6 Jul 1982 17:22:1");   
            //  "M/dd/yyyy hh:mm:ss"
            checkParse(1983, 7, 6, 17, 22, 1, 0, "7/6/1983 17:22:1");   
            //  "MM/d/yyyy hh:mm:ss"
            checkParse(1984, 7, 6, 17, 22, 1, 0, "7/6/1984 17:22:01");   
            //  "M/dd/yyyy"
            checkParse(1985, 7, 6, 0, 0, 0, 0, "7/6/1985");   
            //  "MM/d/yyyy"
            checkParse(1986, 7, 6, 0, 0, 0, 0, "07/06/1986");   
            //  "M/d/yyyy hh:mm:ss"
            checkParse(1987, 7, 6, 17, 22, 1, 0, "7/6/1987 17:22:1");   
            //  "M/d/yyyy"
            checkParse(1988, 7, 6, 0, 0, 0, 0, "7/6/1988");   

            // test ends of range of two digit years
            checkParse(year-79, 1, 1, 0, 0, 0, 0, "1/1/" + ((year-79)%100)
                    + " 00:00:00");   //  "M/d/yy hh:mm:ss"
            //  "M/d/yy"
            checkParse(year+19, 1, 1, 0, 0, 0, 0, "1/1/" + ((year+19)%100));   
            
            //  "yyyyMMdd hh:mm:ss Z"  
            checkParse(1991, 7, 6, 17, 7, 1, +6, "19910706 17:7:1 Z+0600");   
            //  "yyyyMMdd hh:mm:ss"
            checkParse(1992, 7, 6, 17, 7, 1, 0, "19920706 17:07:01");   
            //  "yyyyMMdd'+00''00'''"
            checkParse(1993, 7, 6, 0, 0, 0, 0, "19930706+00'00'");   
            //  "yyyyMMdd'+01''00'''"
            checkParse(1994, 7, 6, 0, 0, 0, 1, "19940706+01'00'");   
            //  "yyyyMMdd'+02''00'''"
            checkParse(1995, 7, 6, 0, 0, 0, 2, "19950706+02'00'");   
            //  "yyyyMMdd'+03''00'''"
            checkParse(1996, 7, 6, 0, 0, 0, 3, "19960706+03'00'");   
             //   . . .
            // "yyyyMMdd'-10''00'''"
            checkParse(1997, 7, 6, 0, 0, 0, -10, "19970706-10'00'");   
            // "yyyyMMdd'-11''00'''"
            checkParse(1998, 7, 6, 0, 0, 0, -11, "19980706-11'00'");   
            //  "yyyyMMdd"
            checkParse(1999, 7, 6, 0, 0, 0, 0, "19990706");   
            // ambiguous big-endian date
            checkParse(2073,12,25, 0, 8, 0, 0, "2073 12 25:08"); 
            
    }

    private static void checkToString(int yr, int mon, int day, 
                int hr, int min, int sec, TimeZone tz, int off) throws Exception 
    {
        // construct a GregoreanCalendar from args
        GregorianCalendar cal = new GregorianCalendar(tz, Locale.ENGLISH);
        cal.set(yr, mon-1, day, hr, min, sec);
        // create expected strings
        String pdfDate = String.format("D:%04d%02d%02d%02d%02d%02d%+03d'00'", 
                yr,mon,day,hr,min,sec,off);
        String iso8601Date = String.format("%04d-%02d-%02d"
                + "T%02d:%02d:%02d%+03d:00", 
                yr,mon,day,hr,min,sec,off);
        // compare outputs from toString and toISO8601 with expected values
        assertEquals(pdfDate, DateConverter.toString(cal));
        assertEquals(iso8601Date, DateConverter.toISO8601(cal));
    }
    
    /** 
     * Test toString() and toISO8601() for various dates.
     * 
     * @throws Exception if something went wrong.
     */
    public void testToString() throws Exception 
    {                                                              // std DST
        TimeZone tzPgh = TimeZone.getTimeZone("America/New_York");   // -5 -4
        TimeZone tzBerlin = TimeZone.getTimeZone("Europe/Berlin");   // +1 +2
        TimeZone tzMaputo = TimeZone.getTimeZone("Africa/Maputo");   // +2 +2
        TimeZone tzAruba = TimeZone.getTimeZone("America/Aruba");    // -4 -4
        TimeZone tzJamaica = TimeZone.getTimeZone("America/Jamaica");// -5 -5
        TimeZone tzMcMurdo = TimeZone.getTimeZone("Antartica/McMurdo");// +12 +13
        
        assertNull(DateConverter.toCalendar((COSString) null));
        assertNull(DateConverter.toCalendar((String) null));
        
        checkToString(2013, 8, 28, 3, 14, 15, tzPgh, -4);
        checkToString(2014, 2, 28, 3, 14, 15, tzPgh, -5);
        checkToString(2015, 8, 28, 3, 14, 15, tzBerlin, +2);
        checkToString(2016, 2, 28, 3, 14, 15, tzBerlin, +1);
        checkToString(2017, 8, 28, 3, 14, 15, tzAruba, -4);
        checkToString(2018, 1, 1, 1, 14, 15, tzJamaica, -5);
        checkToString(2019, 12, 31, 12, 59, 59, tzJamaica, -5);
        checkToString(2020, 2, 29, 0, 0, 0, tzMaputo, +2);
        // McMurdo has a daylightsavings rule, but it seems never to apply
        checkToString(1981, 1, 1, 1, 14, 15, tzMcMurdo, +0);
        checkToString(1982, 2, 1, 1, 14, 15, tzMcMurdo, +0);
        checkToString(1983, 3, 1, 1, 14, 15, tzMcMurdo, +0);
        checkToString(1984, 4, 1, 1, 14, 15, tzMcMurdo, +0);
        checkToString(1985, 5, 1, 1, 14, 15, tzMcMurdo, +0);
        checkToString(1986, 6, 1, 1, 14, 15, tzMcMurdo, +0);
        checkToString(1987, 7, 1, 1, 14, 15, tzMcMurdo, +0);
        checkToString(1988, 8, 1, 1, 14, 15, tzMcMurdo, +0);
        checkToString(1989, 9, 1, 1, 14, 15, tzMcMurdo, +0);
        checkToString(1990, 10, 1, 1, 14, 15, tzMcMurdo, +0);
        checkToString(1991, 11, 1, 1, 14, 15, tzMcMurdo, +0);
        checkToString(1992, 12, 1, 1, 14, 15, tzMcMurdo, +0);
    }
   
    private static void checkParseTZ(int expect, String src) 
    {
        GregorianCalendar dest = DateConverter.newGreg();
        DateConverter.parseTZoffset(src, dest, new ParsePosition(0));
        assertEquals(expect, dest.get(Calendar.ZONE_OFFSET));
    }

    /**
     * Timezone testcase.
     */
    public void testParseTZ() 
    {
        checkParseTZ(0*HRS+0*MINS, "+00:00");
        checkParseTZ(0*HRS+0*MINS, "-0000");
        checkParseTZ(1*HRS+0*MINS, "+1:00");
        checkParseTZ(-(1*HRS+0*MINS), "-1:00");
        checkParseTZ(-(1*HRS+30*MINS), "-0130");
        checkParseTZ(11*HRS+59*MINS, "1159");
        checkParseTZ(-(11*HRS+30*MINS), "1230");
        checkParseTZ(11*HRS+30*MINS, "-12:30");
        checkParseTZ(0*HRS+0*MINS, "Z");
        checkParseTZ(-(8*HRS+0*MINS), "PST");
        checkParseTZ(0*HRS+0*MINS, "EDT");  // EDT does not parse
        checkParseTZ(-(3*HRS+0*MINS), "GMT-0300");
        checkParseTZ(+(11*HRS+0*MINS), "GMT+11:00");
        checkParseTZ(-(6*HRS+0*MINS), "America/Chicago");
        checkParseTZ(+(4*HRS+0*MINS), "Europe/Moscow");
        checkParseTZ((5*HRS+0*MINS), "0500");
        checkParseTZ((5*HRS+0*MINS), "+0500");
        checkParseTZ((11*HRS+0*MINS), "+11'00'");
        checkParseTZ(0, "Z");
    }
    
    private static void checkFormatOffset(double off, String expect) 
    {
        TimeZone tz = new SimpleTimeZone((int)(off*60*60*1000), "junkID");
        String got = DateConverter.formatTZoffset(tz.getRawOffset(), ":");
        assertEquals(expect, got);
    }
    
    /**
     * Timezone offset testcase.
     * 
     * @throws Exception
     */
    public void testFormatTZoffset()
    {
        checkFormatOffset(-12.1, "+11:54");
        checkFormatOffset(12.1, "-11:54");
        checkFormatOffset(0, "+00:00");
        checkFormatOffset(-1, "-01:00");
        checkFormatOffset(.5, "+00:30");
        checkFormatOffset(-0.5, "-00:30");
        checkFormatOffset(.1, "+00:06");
        checkFormatOffset(-0.1, "-00:06");
        checkFormatOffset(-12, "+00:00");
        checkFormatOffset(12, "+00:00");
        checkFormatOffset(-11.5, "-11:30");
        checkFormatOffset(11.5, "+11:30");
        checkFormatOffset(11.9, "+11:54");
        checkFormatOffset(11.1, "+11:06");
        checkFormatOffset(-11.9, "-11:54");
        checkFormatOffset(-11.1, "-11:06");
    }
    
    // testbody precedes
    ////////////////////////////////////////////////////
    
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
        String[] arg = 
        {
            TestDateUtil.class.getName() 
        };
        junit.textui.TestRunner.main( arg );
    }
}
