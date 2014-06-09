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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * This will test the TTFParser implementation.
 *
 * @author Tim Allison
 */
public class TestTTFParser extends TestCase
{

    /**
     * Check whether the creation date is UTC
     *
     * @throws IOException If something went wrong
     */
    public void testUTCDate() throws IOException
    {
        // ttf file is from TIKA trunk
        // tika-parsers/src/test/resources/test-documents/testTrueType.ttf
        final File testFile = new File("src/test/resources/ttf/testTrueType.ttf");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        //Before PDFBOX-2122, TTFDataStream was using the default TimeZone
        //Set the default to something not UTC and see if a UTC timeZone is returned
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los Angeles"));
        TTFParser parser = new TTFParser();
        TrueTypeFont ttf = parser.parseTTF(testFile);
        Calendar created = ttf.getHeader().getCreated();
        assertEquals(created.getTimeZone(), utc);

        Calendar target = Calendar.getInstance(utc);
        target.set(1904, 0, 1, 0, 0, 0);
        target.set(Calendar.MILLISECOND, 0);
        assertEquals(created, target);
    }

}
