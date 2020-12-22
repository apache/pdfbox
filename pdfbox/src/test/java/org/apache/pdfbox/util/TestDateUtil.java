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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.apache.pdfbox.cos.COSString;
import org.junit.jupiter.api.Test;

/**
 * Test the date conversion utility.
 *
 * @author Ben Litchfield
 * @author Fred Hansen
 * 
 */
class TestDateUtil
{
    private static final int MINS = 60*1000, HRS = 60*MINS;
    // expect parse fail
    private static final int BAD = -666;  
    
    /**
     * Test common date formats.
     *
     * @throws Exception when there is an exception
     */
    @Test
    void testExtract() throws Exception
    {
        final TimeZone timezone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        assertCalendarEquals( new GregorianCalendar( 2005, 4, 12 ),
                DateConverter.toCalendar( "D:05/12/2005" ) );
        assertCalendarEquals( new GregorianCalendar( 2005, 4,12,15,57,16 ),
                DateConverter.toCalendar( "5/12/2005 15:57:16" ) );

        TimeZone.setDefault(timezone);
        // check that new toCalendarSTATIC gives null for a null arg
        assertNull(DateConverter.toCalendar((String)null));
    }
    
    /**
     * Calendar.equals test case.
     * 
     * @param expect the expected calendar value
     * @param was the calendar value to be checked
     */
    private void assertCalendarEquals(final Calendar expect, final Calendar was)
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
    @Test
    void testDateConversion() throws IOException
    { 
        final Calendar c = DateConverter.toCalendar("D:20050526205258+01'00'");
        assertEquals(2005, c.get(Calendar.YEAR)); 
        assertEquals(05-1, c.get(Calendar.MONTH)); 
        assertEquals(26, c.get(Calendar.DAY_OF_MONTH)); 
        assertEquals(20, c.get(Calendar.HOUR_OF_DAY)); 
        assertEquals(52, c.get(Calendar.MINUTE)); 
        assertEquals(58, c.get(Calendar.SECOND)); 
        assertEquals(0, c.get(Calendar.MILLISECOND)); 
    }

    /**
     * Check toCalendarSTATIC.
     * @param yr expected year value
     *  If an IOException is the expected result, yr should be null
     * @param mon expected month value
     * @param day expected dayofmonth value
     * @param hr expected hour value
     * @param min expected minute value
     * @param sec expected second value
     * @param offsetHours expected timezone offset in hours (-11..11)
     * @param offsetMinutes expected timezone offset in minutes (0..59)
     * @param orig A date to be parsed.
     * @throws Exception If an unexpected error occurs.
     */
    private static void checkParse(final int yr, final int mon, final int day,
                                   final int hr, final int min, final int sec, final int offsetHours, final int offsetMinutes,
                                   final String orig) throws Exception
    {
        final String pdfDate = String.format(Locale.US, "D:%04d%02d%02d%02d%02d%02d%+03d'%02d'",
                yr,mon,day,hr,min,sec,offsetHours,offsetMinutes);
        final String iso8601Date = String.format(Locale.US, "%04d-%02d-%02d"
                + "T%02d:%02d:%02d%+03d:%02d", 
                yr,mon,day,hr,min,sec,offsetHours,offsetMinutes);
        Calendar cal = DateConverter.toCalendar(orig);
        if (cal != null) 
        {
            assertEquals(iso8601Date, DateConverter.toISO8601(cal));
            assertEquals(pdfDate, DateConverter.toString(cal));
        }
        // new toCalendarSTATIC()
        cal = DateConverter.toCalendar(orig);
        if (yr == BAD) 
        {
            assertEquals(null, cal);
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
    @Test
    void testDateConverter() throws Exception
    {
            final int year = Calendar.getInstance().get(Calendar.YEAR);
            checkParse(2010, 4,23, 0, 0, 0, 0, 0, "D:20100423");
            checkParse(2011, 4,23, 0, 0, 0, 0, 0, "20110423");
            checkParse(2012, 1, 1, 0, 0, 0, 0, 0, "D:2012");
            checkParse(2013, 1, 1, 0, 0, 0, 0, 0, "2013");

            // PDFBOX-1219
            checkParse(2001, 1,31,10,33, 0, +1, 0,  "2001-01-31T10:33+01:00  ");   

            // Same with milliseconds
            checkParse(2001, 1,31,10,33, 0, +1, 0,  "2001-01-31T10:33.123+01:00");

            // PDFBOX-465
            checkParse(2002, 5,12, 9,47, 0, 0, 0, "9:47 5/12/2002");  
            // PDFBOX-465
            checkParse(2003,12,17, 2, 2, 3, 0, 0, "200312172:2:3"); 
            // PDFBOX-465
            checkParse(2009, 3,19,20, 1,22, 0, 0, "  20090319 200122");  

            checkParse(2014, 4, 1, 0, 0, 0, +2, 0, "20140401+0200");
            // "EEEE, MMM dd, yy",
            checkParse(2115, 1,11, 0, 0, 0, 0, 0, "Friday, January 11, 2115");  
            // "EEEE, MMM dd, yy",
            checkParse(1915, 1,11, 0, 0, 0, 0, 0, "Monday, Jan 11, 1915");  
            // "EEEE, MMM dd, yy",
            checkParse(2215, 1,11, 0, 0, 0, 0, 0, "Wed, January 11, 2215");  
            // "EEEE, MMM dd, yy",
            checkParse(2015, 1,11, 0, 0, 0, 0, 0, " Sun, January 11, 2015 ");  
            checkParse(2016, 4, 1, 0, 0, 0, +4, 0, "20160401+04'00'");
            checkParse(2017, 4, 1, 0, 0, 0, +9, 0, "20170401+09'00'");
            checkParse(2017, 4, 1, 0, 0, 0, +9, 30, "20170401+09'30'");            
            checkParse(2018, 4, 1, 0, 0, 0, -2, 0, "20180401-02'00'");
            checkParse(2019, 4, 1, 6, 1, 1, -11, 0, "20190401 6:1:1 -1100");
            checkParse(2020, 5,26,11,25,10, 0, 0, "26 May 2020 11:25:10");
            checkParse(2021, 5,26,11,23, 0, 0, 0, "26 May 2021 11:23");

            // half hour timezones
            checkParse(2016, 4, 1, 0, 0, 0, +4, 30, "20160401+04'30'");
            checkParse(2017, 4, 1, 0, 0, 0, +9, 30, "20170401+09'30'");            
            checkParse(2018, 4, 1, 0, 0, 0, -2, 30, "20180401-02'30'");
            checkParse(2019, 4, 1, 6, 1, 1, -11, 30, "20190401 6:1:1 -1130");
            checkParse(2000, 2,29, 0, 0, 0, +11, 30, " 2000 Feb 29 GMT + 11:30");
            
            // try dates invalid due to out of limit values
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0,  "Tuesday, May 32 2000 11:27 UCT");
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0,  "32 May 2000 11:25");
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0,  "Tuesday, May 32 2000 11:25");
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0,  "19921301 11:25");
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0,  "19921232 11:25");
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0,  "19921001 11:60");
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0,  "19920401 24:25");
            
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0, 
            "20070430193647+713'00' illegal tz hr");  // PDFBOX-465
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0, "nodigits");
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0, "Unknown"); // PDFBOX-465
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0, "333three digit year");
            
            checkParse(2000, 2,29, 0, 0, 0, 0, 0, "2000 Feb 29"); // valid date
            checkParse(2000, 2,29, 0, 0, 0,+11, 0, " 2000 Feb 29 GMT + 11:00"); // valid date
            checkParse(2000, 2,29, 0, 0, 0,+11, 0, " 2000 Feb 29 UTC + 11:00"); // valid date
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0, "2100 Feb 29 GMT+11"); // invalid date
            checkParse(2012, 2,29, 0, 0, 0,+11, 0, "2012 Feb 29 GMT+11"); // valid date
            checkParse(BAD, 0, 0, 0, 0, 0,  0, 0, "2012 Feb 30 GMT+11"); // invalid date

            checkParse(1970,12,23, 0, 8, 0,  0, 0, "1970 12 23:08");  // test ambiguous date 
            
            // cannot have P for PM
            // cannot have Sat. instead of Sat
            // EST works, but EDT does not; EST is a special kludge in Java
            
            // test cases for all entries on old formats list
            //  "E, dd MMM yyyy hh:mm:ss a"  
            checkParse(1971, 7, 6, 17, 22, 1, 0, 0, "Tuesday, 6 Jul 1971 5:22:1 PM"); 
            //  "EE, MMM dd, yyyy hh:mm:ss a"
            checkParse(1972, 7, 6, 17, 22, 1, 0, 0, "Thu, July 6, 1972 5:22:1 pm");   
            //  "MM/dd/yyyy hh:mm:ss"
            checkParse(1973, 7, 6, 17, 22, 1, 0, 0, "7/6/1973 17:22:1");   
            //  "MM/dd/yyyy"
            checkParse(1974, 7, 6, 0, 0, 0, 0, 0, "7/6/1974");   
            //  "yyyy-MM-dd'T'HH:mm:ss'Z'"
            checkParse(1975, 7, 6, 17, 22, 1, -10, 0, "1975-7-6T17:22:1-1000");   
            //  "yyyy-MM-dd'T'HH:mm:ssz"
            checkParse(1976, 7, 6, 17, 22, 1, -4, 0, "1976-7-6T17:22:1GMT-4");   
            //  "yyyy-MM-dd'T'HH:mm:ssz"
            checkParse(BAD, 7, 6, 17, 22, 1, -4, 0, "2076-7-6T17:22:1EDT");   // "EDT" is not a known tz ID
            //  "yyyy-MM-dd'T'HH:mm:ssz"
            checkParse(1960, 7, 6, 17, 22, 1, -5, 0, "1960-7-6T17:22:1EST");   // "EST" does not have a DST rule
           //  "EEEE, MMM dd, yyyy"
            checkParse(1977, 7, 6, 0, 0, 0, 0, 0, "Wednesday, Jul 6, 1977");   
            //  "EEEE MMM dd, yyyy HH:mm:ss"
            checkParse(1978, 7, 6, 17, 22, 1, 0, 0, "Thu Jul 6, 1978 17:22:1");   
            //  "EEEE MMM dd HH:mm:ss z yyyy"
            checkParse(1979, 7, 6, 17, 22, 1, +8, 0, "Friday July 6 17:22:1 GMT+08:00 1979");   
            //  "EEEE, MMM dd, yyyy 'at' hh:mma"
            checkParse(1980, 7, 6, 16, 23, 0, 0, 0, "Sun, Jul 6, 1980 at 4:23pm");   
            //  "EEEEEEEEEE, MMMMMMMMMMMM dd, yyyy"
            checkParse(1981, 7, 6, 0, 0, 0, 0, 0, "Monday, July 6, 1981");   
            //  "dd MMM yyyy hh:mm:ss"
            checkParse(1982, 7, 6, 17, 22, 1, 0, 0, "6 Jul 1982 17:22:1");   
            //  "M/dd/yyyy hh:mm:ss"
            checkParse(1983, 7, 6, 17, 22, 1, 0, 0, "7/6/1983 17:22:1");   
            //  "MM/d/yyyy hh:mm:ss"
            checkParse(1984, 7, 6, 17, 22, 1, 0, 0, "7/6/1984 17:22:01");   
            //  "M/dd/yyyy"
            checkParse(1985, 7, 6, 0, 0, 0, 0, 0, "7/6/1985");   
            //  "MM/d/yyyy"
            checkParse(1986, 7, 6, 0, 0, 0, 0, 0, "07/06/1986");   
            //  "M/d/yyyy hh:mm:ss"
            checkParse(1987, 7, 6, 17, 22, 1, 0, 0, "7/6/1987 17:22:1");   
            //  "M/d/yyyy"
            checkParse(1988, 7, 6, 0, 0, 0, 0, 0, "7/6/1988");   

            // test ends of range of two digit years
            checkParse(year-79, 1, 1, 0, 0, 0, 0, 0, "1/1/" + ((year-79)%100)
                    + " 00:00:00");   //  "M/d/yy hh:mm:ss"
            //  "M/d/yy"
            checkParse(year+19, 1, 1, 0, 0, 0, 0, 0, "1/1/" + ((year+19)%100));   
            
            //  "yyyyMMdd hh:mm:ss Z"  
            checkParse(1991, 7, 6, 17, 7, 1, +6, 0, "19910706 17:7:1 Z+0600");   
            //  "yyyyMMdd hh:mm:ss"
            checkParse(1992, 7, 6, 17, 7, 1, 0, 0, "19920706 17:07:01");   
            //  "yyyyMMdd'+00''00'''"
            checkParse(1993, 7, 6, 0, 0, 0, 0, 0, "19930706+00'00'");   
            //  "yyyyMMdd'+01''00'''"
            checkParse(1994, 7, 6, 0, 0, 0, 1, 0, "19940706+01'00'");   
            //  "yyyyMMdd'+02''00'''"
            checkParse(1995, 7, 6, 0, 0, 0, 2, 0, "19950706+02'00'");   
            //  "yyyyMMdd'+03''00'''"
            checkParse(1996, 7, 6, 0, 0, 0, 3, 0, "19960706+03'00'");   
             //   . . .
            // "yyyyMMdd'-10''00'''"
            checkParse(1997, 7, 6, 0, 0, 0, -10, 0, "19970706-10'00'");   
            // "yyyyMMdd'-11''00'''"
            checkParse(1998, 7, 6, 0, 0, 0, -11, 0, "19980706-11'00'");   
            //  "yyyyMMdd"
            checkParse(1999, 7, 6, 0, 0, 0, 0, 0, "19990706");   
            // ambiguous big-endian date
            checkParse(2073,12,25, 0, 8, 0, 0, 0, "2073 12 25:08"); 
            
            // PDFBOX-3315 GMT+12
            checkParse(2016, 4,11,16,01,15, 12, 0, "D:20160411160115+12'00'");   
    }

    private static void checkToString(final int yr, final int mon, final int day,
                                      final int hr, final int min, final int sec,
                                      final TimeZone tz, final int offsetHours, final int offsetMinutes) throws Exception
    {
        // construct a GregoreanCalendar from args
        final GregorianCalendar cal = new GregorianCalendar(tz, Locale.ENGLISH);
        cal.set(yr, mon-1, day, hr, min, sec);
        // create expected strings
        final String pdfDate = String.format(Locale.US, "D:%04d%02d%02d%02d%02d%02d%+03d'%02d'",
                yr,mon,day,hr,min,sec,offsetHours, offsetMinutes);
        final String iso8601Date = String.format(Locale.US, "%04d-%02d-%02d"
                + "T%02d:%02d:%02d%+03d:%02d", 
                yr,mon,day,hr,min,sec,offsetHours, offsetMinutes);
        // compare outputs from toString and toISO8601 with expected values
        assertEquals(pdfDate, DateConverter.toString(cal));
        assertEquals(iso8601Date, DateConverter.toISO8601(cal));
    }
    
    /** 
     * Test toString() and toISO8601() for various dates.
     * 
     * @throws Exception if something went wrong.
     */
    @Test
    void testToString() throws Exception
    {                                                              // std DST
        final TimeZone tzPgh = TimeZone.getTimeZone("America/New_York");   // -5 -4
        final TimeZone tzBerlin = TimeZone.getTimeZone("Europe/Berlin");   // +1 +2
        final TimeZone tzMaputo = TimeZone.getTimeZone("Africa/Maputo");   // +2 +2
        final TimeZone tzAruba = TimeZone.getTimeZone("America/Aruba");    // -4 -4
        final TimeZone tzJamaica = TimeZone.getTimeZone("America/Jamaica");// -5 -5
        final TimeZone tzMcMurdo = TimeZone.getTimeZone("Antartica/McMurdo");// +12 +13
        final TimeZone tzAdelaide = TimeZone.getTimeZone("Australia/Adelaide");// +9:30 +10:30
        
        assertNull(DateConverter.toCalendar((COSString) null));
        assertNull(DateConverter.toCalendar((String) null));
        assertNull(DateConverter.toCalendar("D:    "));
        assertNull(DateConverter.toCalendar("D:"));
        
        checkToString(2013, 8, 28, 3, 14, 15, tzPgh, -4, 0);
        checkToString(2014, 2, 28, 3, 14, 15, tzPgh, -5, 0);
        checkToString(2015, 8, 28, 3, 14, 15, tzBerlin, +2, 0);
        checkToString(2016, 2, 28, 3, 14, 15, tzBerlin, +1, 0);
        checkToString(2017, 8, 28, 3, 14, 15, tzAruba, -4, 0);
        checkToString(2018, 1, 1, 1, 14, 15, tzJamaica, -5, 0);
        checkToString(2019, 12, 31, 12, 59, 59, tzJamaica, -5, 0);
        checkToString(2020, 2, 29, 0, 0, 0, tzMaputo, +2, 0);
        checkToString(2015, 8, 28, 3, 14, 15, tzAdelaide, +9, 30);
        checkToString(2016, 2, 28, 3, 14, 15, tzAdelaide, +10, 30);
        // McMurdo has a daylightsavings rule, but it seems never to apply
        for (int m = 1; m <= 12; ++m)
        {
            checkToString(1980 + m, m, 1, 1, 14, 15, tzMcMurdo, +0, 0);
        }
    }

    private static void checkParseTZ(final int expect, final String src)
    {
        final GregorianCalendar dest = DateConverter.newGreg();
        DateConverter.parseTZoffset(src, dest, new ParsePosition(0));
        assertEquals(expect, dest.get(Calendar.ZONE_OFFSET));
    }

    /**
     * Timezone testcase.
     */
    @Test
    void testParseTZ()
    {
        // 1st parameter is what to expect
        checkParseTZ(0*HRS+0*MINS, "+00:00");
        checkParseTZ(0*HRS+0*MINS, "-0000");
        checkParseTZ(1*HRS+0*MINS, "+1:00");
        checkParseTZ(-(1*HRS+0*MINS), "-1:00");
        checkParseTZ(-(1*HRS+30*MINS), "-0130");
        checkParseTZ(11*HRS+59*MINS, "1159");
        checkParseTZ(12*HRS+30*MINS, "1230");
        checkParseTZ(-(12*HRS+30*MINS), "-12:30");
        checkParseTZ(0*HRS+0*MINS, "Z");
        checkParseTZ(-(8*HRS+0*MINS), "PST");
        checkParseTZ(0*HRS+0*MINS, "EDT");  // EDT does not parse
        checkParseTZ(-(3*HRS+0*MINS), "GMT-0300");
        checkParseTZ(+(11*HRS+0*MINS), "GMT+11:00");
        checkParseTZ(-(6*HRS+0*MINS), "America/Chicago");
        checkParseTZ(+(3*HRS+0*MINS), "Europe/Moscow");
        checkParseTZ(+(9*HRS+30*MINS), "Australia/Adelaide");
        checkParseTZ((5*HRS+0*MINS), "0500");
        checkParseTZ((5*HRS+0*MINS), "+0500");
        checkParseTZ((11*HRS+0*MINS), "+11'00'");
        checkParseTZ(0, "Z");
        // PDFBOX-3315, PDFBOX-2420
        checkParseTZ(12*HRS+0*MINS, "+12:00");
        checkParseTZ(-(12*HRS+0*MINS), "-12:00");
        checkParseTZ(14*HRS+0*MINS, "1400");
        checkParseTZ(-(14*HRS+0*MINS), "-1400");
    }
    
    private static void checkFormatOffset(final double off, final String expect)
    {
        final TimeZone tz = new SimpleTimeZone((int)(off*60*60*1000), "junkID");
        final String got = DateConverter.formatTZoffset(tz.getRawOffset(), ":");
        assertEquals(expect, got);
    }
    
    /**
     * Timezone offset testcase.
     */
    @Test
    void testFormatTZoffset()
    {
        // 2nd parameter is what to expect
        checkFormatOffset(-12.1, "-12:06");
        checkFormatOffset(12.1, "+12:06");
        checkFormatOffset(0, "+00:00");
        checkFormatOffset(-1, "-01:00");
        checkFormatOffset(.5, "+00:30");
        checkFormatOffset(-0.5, "-00:30");
        checkFormatOffset(.1, "+00:06");
        checkFormatOffset(-0.1, "-00:06");
        checkFormatOffset(-12, "-12:00");
        checkFormatOffset(12, "+12:00");
        checkFormatOffset(-11.5, "-11:30");
        checkFormatOffset(11.5, "+11:30");
        checkFormatOffset(11.9, "+11:54");
        checkFormatOffset(11.1, "+11:06");
        checkFormatOffset(-11.9, "-11:54");
        checkFormatOffset(-11.1, "-11:06");
        // PDFBOX-2420
        checkFormatOffset(14, "+14:00");
        checkFormatOffset(-14, "-14:00");
    }
    
}
