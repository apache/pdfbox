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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

/**
 * Test the date conversion utility.
 *
 */
class JavaTimeTest
{

    /**
     * Test java.time instead of jaxb and DateConverter
     * 
     * @throws Exception when there is an exception
     */
    @Test
    void testJavaTimeParsing() throws Exception
    {
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

        testString1 = "2015-02-02T16:37:19.192Z";
        testString2 = "2015-02-02T08:37:19.192PST";

        assertEquals(DateConverter.toCalendar(testString2).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());

        testString1 = "2015-02-02T16:37:19.192+01:00";
        testString2 = "2015-02-02T16:37:19.192Europe/Berlin";

        assertEquals(DateConverter.toCalendar(testString2).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());

        // PDFBOX-4902: half-hour TZ
        testString1 = "2015-02-02T16:37:19.192+05:30";
        assertEquals(DateConverter.toCalendar(testString1).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());

        testString1 = "2015-02-02T16:37:19.192-05:30";
        assertEquals(DateConverter.toCalendar(testString1).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());

        testString1 = "2015-02-02T16:37:19.192+10:30";
        assertEquals(DateConverter.toCalendar(testString1).toInstant(),ZonedDateTime.parse(testString1, dateTimeFormatter).toInstant());
    }   
}