/*
 * Copyright 2017 The Apache Software Foundation.
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
package org.apache.fontbox.encoding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EncodingTest
{
    @Test
    void testStandardEncoding()
    {
        StandardEncoding standardEncoding = StandardEncoding.INSTANCE;
        // check some randomly chosen mappings
        assertEquals(".notdef", standardEncoding.getName(0));
        assertEquals("space", standardEncoding.getName(32));
        assertEquals("p", standardEncoding.getName(112));
        assertEquals("guilsinglleft", standardEncoding.getName(172));
        assertEquals(32, standardEncoding.getCode("space"));
        assertEquals(112, standardEncoding.getCode("p"));
        assertEquals(172, standardEncoding.getCode("guilsinglleft"));
    }

    @Test
    void testMacRomanEncoding()
    {
        MacRomanEncoding macRomanEncoding = MacRomanEncoding.INSTANCE;
        // check some randomly chosen mappings
        assertEquals(".notdef", macRomanEncoding.getName(0));
        assertEquals("space", macRomanEncoding.getName(32));
        assertEquals("p", macRomanEncoding.getName(112));
        assertEquals("germandbls", macRomanEncoding.getName(167));
        assertEquals(32, macRomanEncoding.getCode("space"));
        assertEquals(112, macRomanEncoding.getCode("p"));
        assertEquals(167, macRomanEncoding.getCode("germandbls"));
    }

}
