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
package org.apache.pdfbox.cos;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import org.apache.pdfbox.util.SmallMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

class COSDictionaryTest
{
    private final String property = "org.apache.pdfbox.cosdictionary.smallmap.threshold";

    @AfterEach
    void tearDown()
    {
        System.clearProperty(property);
    }

    @Test
    void testCOSDictionaryNotEqualsCOSStream()
    {
        COSDictionary cosDictionary = new COSDictionary();
        COSStream cosStream = new COSStream();
        cosDictionary.setItem(COSName.BE, COSName.BE);
        cosDictionary.setInt(COSName.LENGTH, 0);
        cosStream.setItem(COSName.BE, COSName.BE);
        assertNotEquals(cosDictionary, cosStream,
                "a COSDictionary shall not be equal to a COSStream with the same dictionary entries");
        assertNotEquals(cosStream, cosDictionary,
                "a COSStream shall not be equal to a COSDictionary with the same dictionary entries");
    }

    @Test
    void testCOSDictionarySystemPropertyDefined()
    {
        System.setProperty(property, "0");
        assertInstanceOf(LinkedHashMap.class, new COSDictionary().items);

        System.clearProperty(property);
        assertInstanceOf(SmallMap.class, new COSDictionary().items);

        System.setProperty(property, "abc");
        assertThrows(NumberFormatException.class, COSDictionary::new);
    }
}
