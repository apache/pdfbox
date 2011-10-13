/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fontbox.tika;

import java.io.BufferedInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

public class TrueTypeParserTest extends TestCase {

    public void testTrueTypeParsing() throws Exception {
        Tika tika = new Tika();
        String type = "application/x-font-ttf";

        Metadata metadata = new Metadata();
        InputStream stream = new BufferedInputStream(
                TrueTypeParserTest.class.getResourceAsStream(
                        "testTrueType.ttf"));
        assertEquals(type, tika.detect(stream));
        assertEquals("", tika.parseToString(stream, metadata));
        // Disable date tests until timezone handling in Tika is fixed
        // assertEquals("1903-12-31T23:00:00Z", metadata.get(Metadata.DATE));
        // assertEquals("1903-12-31T23:00:00Z", metadata.get(Metadata.MODIFIED));
        assertEquals(type, metadata.get(Metadata.CONTENT_TYPE));
    }

}
