/*
 * Copyright 2014 The Apache Software Foundation.
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
package org.apache.fontbox.ttf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

/**
 * This will test the TTFParser implementation.
 *
 * @author Tim Allison
 */
class TestTTFParser
{

    /**
     * Check whether the creation date is UTC
     *
     * @throws IOException If something went wrong
     */
    @Test
    void testUTCDate() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        final TimeZone utc = TimeZone.getTimeZone("UTC");
        //Before PDFBOX-2122, TTFDataStream was using the default TimeZone
        //Set the default to something not UTC and see if a UTC timeZone is returned
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los Angeles"));
        final TTFParser parser = new TTFParser();
        final TrueTypeFont ttf = parser.parse(testFile);
        final Calendar created = ttf.getHeader().getCreated();
        assertEquals(created.getTimeZone(), utc);

        final Calendar target = Calendar.getInstance(utc);
        target.set(2012, 9, 4, 11, 2, 31);
        target.set(Calendar.MILLISECOND, 0);
        assertEquals(target, created);
    }

}
