/*
 * Copyright 2021 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jempbox.xmp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import junit.framework.TestCase;
import org.apache.jempbox.impl.DateConverter;

/**
 *
 * @author Tilman Hausherr
 */
public class DateConverterTest extends TestCase
{
    public void testDateConverter() throws IOException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z");

        TimeZone timezone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // this hits the NumberFormatException segment
        assertEquals("2020-12-23",
                sdf.format(DateConverter.toCalendar("12/23/2020").getTime()).substring(0, 10));
        
        Calendar cal = DateConverter.toCalendar("2012-10-30T12:24:59+01:00");
        System.out.println("cal1: " + cal);
        System.out.println("cal2: " + cal.getTimeInMillis());
        System.out.println("cal3: " + cal.getTime());

        // happy path
        assertEquals("2012-10-30 12:24:59 +0100",
                sdf.format(DateConverter.toCalendar("2012-10-30T12:24:59+01:00").getTime()));

        TimeZone.setDefault(timezone);
    }
}
