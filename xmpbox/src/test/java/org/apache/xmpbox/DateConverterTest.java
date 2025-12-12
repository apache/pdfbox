/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.xmpbox;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

/**
 * Test the date conversion utility.
 *
 */
class DateConverterTest
{

    /**
     * Test parsing several ISO8601 date formats.
     * 
     * Test with additional time zone
     * information normally not supported by ISO8601
     *
     * @throws Exception when there is an exception
     */
    @Test
    void testDateConversion() throws Exception
    {
        // Test partial dates
        Calendar convDate = DateConverter.toCalendar("2015");
        assertEquals(2015, convDate.get(Calendar.YEAR));
        convDate = DateConverter.toCalendar("2015-05");
        assertEquals(4, convDate.get(Calendar.MONTH));
        convDate = DateConverter.toCalendar("2015-05-02");
        assertEquals(2015, convDate.get(Calendar.YEAR));
        assertEquals(4, convDate.get(Calendar.MONTH));
        assertEquals(2, convDate.get(Calendar.DAY_OF_MONTH));

        convDate = DateConverter.toCalendar("D:2015-02-02");
        assertEquals(2015, convDate.get(Calendar.YEAR));

        convDate = DateConverter.toCalendar("D:2015-02-03T10:11:12");
        assertEquals(2015, convDate.get(Calendar.YEAR));
        assertEquals(1, convDate.get(Calendar.MONTH)); // 0-based
        assertEquals(3, convDate.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, convDate.get(Calendar.HOUR_OF_DAY));
        assertEquals(11, convDate.get(Calendar.MINUTE));
        assertEquals(12, convDate.get(Calendar.SECOND));

        convDate = DateConverter.toCalendar("D:2015-02-03T10:11:12Z");
        assertEquals(2015, convDate.get(Calendar.YEAR));
        assertEquals(1, convDate.get(Calendar.MONTH)); // 0-based
        assertEquals(3, convDate.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, convDate.get(Calendar.HOUR_OF_DAY));
        assertEquals(11, convDate.get(Calendar.MINUTE));
        assertEquals(12, convDate.get(Calendar.SECOND));

        convDate = DateConverter.toCalendar("2025-09-03T15:43:47.989082+00:00");
        assertEquals(989, convDate.get(Calendar.MILLISECOND));

        // test some bad strings
        assertThrows(IOException.class, () -> DateConverter.toCalendar("123"));
        assertThrows(IOException.class, () -> DateConverter.toCalendar("2008-12-31T19:48:30+19:00"));
        assertThrows(IOException.class, () -> DateConverter.toCalendar("2008-12-31T19:48:30-19:00"));
        assertThrows(IOException.class, () -> DateConverter.toCalendar("2008-12-02T21:04:0Z"));
        assertThrows(IOException.class, () -> DateConverter.toCalendar("0-01-01T00:00:00Z"));
        assertThrows(IOException.class, () -> DateConverter.toCalendar("2009-03-16T01:15:19-0-4:00"));

        //Test missing seconds
        assertEquals(DateConverter.toCalendar("2015-12-08T12:07:00-05:00"),
                     DateConverter.toCalendar("2015-12-08T12:07-05:00"));
        assertEquals(DateConverter.toCalendar("2011-11-20T10:09:00Z"),
                     DateConverter.toCalendar("2011-11-20T10:09Z"));
        
        // Test some time zone offsets
        String testString1 = "";
        String testString2 = "";

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm[:ss][.SSS][XXX]");

        //Test missing seconds
        testString1 = "2015-12-08T12:07:00-05:00";
        testString2 = "2015-12-08T12:07-05:00";

        assertEquals(DateConverter.toCalendar(testString1), DateConverter.toCalendar(testString2));
        assertEquals(DateConverter.toCalendar(testString1).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());
        assertEquals(DateConverter.toCalendar(testString2).toInstant(),ZonedDateTime.parse(testString2, dateTimeFormatter).toInstant());

        // Test some time zone offsets
        testString1 = "2015-02-02T16:37:19.192Z";
        testString2 = "2015-02-02T16:37:19.192Z";

        assertEquals(DateConverter.toCalendar(testString2).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());

        testString1 = "2015-02-02T16:37:19.192+00:00";
        testString2 = "2015-02-02T16:37:19.192Z";

        assertEquals(DateConverter.toCalendar(testString2).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());

        testString1 = "2015-02-02T16:37:19.192+02:00";
        testString2 = "2015-02-02T16:37:19.192+02:00";

        assertEquals(DateConverter.toCalendar(testString2).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());

        // PDFBOX-4902: half-hour TZ
        testString1 = "2015-02-02T16:37:19.192+05:30";
        assertEquals(DateConverter.toCalendar(testString1).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());

        testString1 = "2015-02-02T16:37:19.192-05:30";
        assertEquals(DateConverter.toCalendar(testString1).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());

        testString1 = "2015-02-02T16:37:19.192+10:30";
        assertEquals(DateConverter.toCalendar(testString1).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());

        testString1 = "2024-04-09T14:41:38";
        assertEquals(DateConverter.toCalendar(testString1).toInstant(),
                LocalDateTime.parse(testString1, ISO_LOCAL_DATE_TIME).atZone(ZoneId.of("UTC"))
                        .toInstant());

        assertNull(DateConverter.toCalendar(null));
        assertNull(DateConverter.toCalendar(""));
    }
    
    /**
     * Test formatting ISO8601 date formats.
     * 
     * Test with additional time zone
     * information normally not supported by ISO8601
     *
     * @throws Exception when there is an exception
     */
    @Test
    void testDateFormatting() throws Exception
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Calendar cal = DateConverter.toCalendar("2015-02-02T16:37:19.192Z");
        assertEquals(dateFormat.format(cal.getTime()), 
                    dateFormat.format(DateConverter.toCalendar(DateConverter.toISO8601(cal,true)).getTime())
                );

        cal = DateConverter.toCalendar("2015-02-02T16:37:19.192+09:09");
        assertEquals(dateFormat.format(cal.getTime()), 
                    dateFormat.format(DateConverter.toCalendar(DateConverter.toISO8601(cal,true)).getTime())
                );

        cal = DateConverter.toCalendar("2015-02-02T16:37:19.192+10:10");
        assertEquals(dateFormat.format(cal.getTime()), 
                    dateFormat.format(DateConverter.toCalendar(DateConverter.toISO8601(cal,true)).getTime())
                );

        // PDFBOX-6107
        cal = DateConverter.toCalendar("0000-01-01");
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("0001-01-01T00:00:00+00:00", DateConverter.toISO8601(cal));
    }
}
