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

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Test;

/**
 * Test the date conversion utility.
 *
 */
public class DateConverterTest
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
    public void testDateConversion() throws Exception
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Calendar jaxbCal;

        // Test partial dates
        Calendar convDate = DateConverter.toCalendar("2015-02-02");
        assertEquals(2015, convDate.get(Calendar.YEAR));

        //Test missing seconds
        assertEquals(DateConverter.toCalendar("2015-12-08T12:07:00-05:00"),
                     DateConverter.toCalendar("2015-12-08T12:07-05:00"));
        assertEquals(DateConverter.toCalendar("2011-11-20T10:09:00Z"),
                     DateConverter.toCalendar("2011-11-20T10:09Z"));
        
        // Test some time zone offsets
        jaxbCal = javax.xml.bind.DatatypeConverter.parseDateTime("2015-02-02T16:37:19.192Z");
        convDate = DateConverter.toCalendar("2015-02-02T16:37:19.192Z");
        assertEquals(dateFormat.format(jaxbCal.getTime()), dateFormat.format(convDate.getTime()));

        jaxbCal = javax.xml.bind.DatatypeConverter.parseDateTime("2015-02-02T16:37:19.192+00:00");
        convDate = DateConverter.toCalendar("2015-02-02T16:37:19.192Z");
        assertEquals(dateFormat.format(jaxbCal.getTime()), dateFormat.format(convDate.getTime()));

        jaxbCal = javax.xml.bind.DatatypeConverter.parseDateTime("2015-02-02T16:37:19.192+02:00");
        convDate = DateConverter.toCalendar("2015-02-02T16:37:19.192+02:00");
        assertEquals(dateFormat.format(jaxbCal.getTime()), dateFormat.format(convDate.getTime()));

        jaxbCal = javax.xml.bind.DatatypeConverter.parseDateTime("2015-02-02T16:37:19.192Z");
        convDate = DateConverter.toCalendar("2015-02-02T08:37:19.192PST");
        assertEquals(dateFormat.format(jaxbCal.getTime()), dateFormat.format(convDate.getTime()));

        jaxbCal = javax.xml.bind.DatatypeConverter.parseDateTime("2015-02-02T16:37:19.192+01:00");
        convDate = DateConverter.toCalendar("2015-02-02T16:37:19.192Europe/Berlin");
        assertEquals(dateFormat.format(jaxbCal.getTime()), dateFormat.format(convDate.getTime()));
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
    public void testDateFormatting() throws Exception
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Calendar cal = DateConverter.toCalendar("2015-02-02T16:37:19.192Z");
        assertEquals(dateFormat.format(cal.getTime()), 
                    dateFormat.format(DateConverter.toCalendar(DateConverter.toISO8601(cal,true)).getTime())
                );
    }
}
