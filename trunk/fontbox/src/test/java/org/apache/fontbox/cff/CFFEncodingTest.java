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
package org.apache.fontbox.cff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CFFEncodingTest
{
    @Test
    void testCFFExpertEncoding()
    {
        CFFExpertEncoding cffExpertEncoding = CFFExpertEncoding.getInstance();
        // check some randomly chosen mappings
        assertEquals(".notdef", cffExpertEncoding.getName(0));
        assertEquals("space", cffExpertEncoding.getName(32));
        assertEquals("Psmall", cffExpertEncoding.getName(112));
        assertEquals("Ucircumflexsmall", cffExpertEncoding.getName(251));
        assertEquals(32, cffExpertEncoding.getCode("space"));
        assertEquals(112, cffExpertEncoding.getCode("Psmall"));
        assertEquals(251, cffExpertEncoding.getCode("Ucircumflexsmall"));
    }

    @Test
    void testCFFStandardEncoding()
    {
        CFFStandardEncoding cffStandardEncoding = CFFStandardEncoding.getInstance();
        // check some randomly chosen mappings
        assertEquals(".notdef", cffStandardEncoding.getName(0));
        assertEquals("space", cffStandardEncoding.getName(32));
        assertEquals("p", cffStandardEncoding.getName(112));
        assertEquals("germandbls", cffStandardEncoding.getName(251));
        assertEquals(32, cffStandardEncoding.getCode("space"));
        assertEquals(112, cffStandardEncoding.getCode("p"));
        assertEquals(251, cffStandardEncoding.getCode("germandbls"));
    }

}
