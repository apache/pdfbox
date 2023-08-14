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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CFFCharsetTest
{

    @Test
    void testEmbeddedCharset()
    {
        // true -> CFFCharsetCID
        EmbeddedCharset embeddedCharsetCID = new EmbeddedCharset(true);
        assertTrue(embeddedCharsetCID.isCIDFont());
        embeddedCharsetCID.addCID(10, 20);
        // test existing mapping
        assertEquals(10, embeddedCharsetCID.getGIDForCID(20));
        assertEquals(20, embeddedCharsetCID.getCIDForGID(10));
        // test not existing mapping
        assertEquals(0, embeddedCharsetCID.getGIDForCID(99));
        assertEquals(0, embeddedCharsetCID.getCIDForGID(99));
        // test not allowed method calls
        assertThrows(IllegalStateException.class, () -> embeddedCharsetCID.getSIDForGID(0));
        assertThrows(IllegalStateException.class, () -> embeddedCharsetCID.getGIDForSID(0));
        assertThrows(IllegalStateException.class, () -> embeddedCharsetCID.addSID(0, 0, "test"));
        assertThrows(IllegalStateException.class, () -> embeddedCharsetCID.getSID("test"));
        assertThrows(IllegalStateException.class, () -> embeddedCharsetCID.getNameForGID(0));
        // false -> CFFCharsetType1
        EmbeddedCharset embeddedCharsetType1 = new EmbeddedCharset(false);
        assertFalse(embeddedCharsetType1.isCIDFont());
        embeddedCharsetType1.addSID(10, 20, "test");
        // test existing mapping
        assertEquals(20, embeddedCharsetType1.getSID("test"));
        assertEquals(10, embeddedCharsetType1.getGIDForSID(20));
        assertEquals(20, embeddedCharsetType1.getSIDForGID(10));
        // test not existing mapping
        assertEquals(0, embeddedCharsetType1.getGIDForSID(99));
        assertEquals(0, embeddedCharsetType1.getSIDForGID(99));
        // test not allowed method calls
        assertThrows(IllegalStateException.class, () -> embeddedCharsetType1.getCIDForGID(0));
        assertThrows(IllegalStateException.class, () -> embeddedCharsetType1.getGIDForCID(0));
        assertThrows(IllegalStateException.class, () -> embeddedCharsetType1.addCID(0, 0));
    }

    @Test
    void testCFFCharsetCID()
    {
        CFFCharsetCID cffCharsetCID = new CFFCharsetCID();
        assertTrue(cffCharsetCID.isCIDFont());
        cffCharsetCID.addCID(10, 20);
        // test existing mapping
        assertEquals(10, cffCharsetCID.getGIDForCID(20));
        assertEquals(20, cffCharsetCID.getCIDForGID(10));
        // test not existing mapping
        assertEquals(0, cffCharsetCID.getGIDForCID(99));
        assertEquals(0, cffCharsetCID.getCIDForGID(99));
        // test not allowed method calls
        assertThrows(IllegalStateException.class, () -> cffCharsetCID.getSIDForGID(0));
        assertThrows(IllegalStateException.class, () -> cffCharsetCID.getGIDForSID(0));
        assertThrows(IllegalStateException.class, () -> cffCharsetCID.addSID(0, 0, "test"));
        assertThrows(IllegalStateException.class, () -> cffCharsetCID.getSID("test"));
        assertThrows(IllegalStateException.class, () -> cffCharsetCID.getNameForGID(0));
    }

    @Test
    void testCFFCharsetType1()
    {
        CFFCharsetType1 cffCharsetType1 = new CFFCharsetType1();
        assertFalse(cffCharsetType1.isCIDFont());
        cffCharsetType1.addSID(10, 20, "test");
        // test existing mapping
        assertEquals(20, cffCharsetType1.getSID("test"));
        assertEquals(10, cffCharsetType1.getGIDForSID(20));
        assertEquals(20, cffCharsetType1.getSIDForGID(10));
        // test not existing mapping
        assertEquals(0, cffCharsetType1.getGIDForSID(99));
        assertEquals(0, cffCharsetType1.getSIDForGID(99));
        // test not allowed method calls
        assertThrows(IllegalStateException.class, () -> cffCharsetType1.getCIDForGID(0));
        assertThrows(IllegalStateException.class, () -> cffCharsetType1.getGIDForCID(0));
        assertThrows(IllegalStateException.class, () -> cffCharsetType1.addCID(0, 0));
    }

    @Test
    void testCFFExpertCharset()
    {
        CFFExpertCharset cffExpertCharset = CFFExpertCharset.getInstance();
        // check .notdef mapping
        assertEquals(0, cffExpertCharset.getSIDForGID(0));
        assertEquals(0, cffExpertCharset.getSID(".notdef"));
        assertEquals(".notdef", cffExpertCharset.getNameForGID(0));
        // check some randomly chosen mappings
        assertEquals(253, cffExpertCharset.getSIDForGID(32));
        assertEquals(253, cffExpertCharset.getSID("asuperior"));
        assertEquals("asuperior", cffExpertCharset.getNameForGID(32));

        assertEquals(240, cffExpertCharset.getSIDForGID(17));
        assertEquals(240, cffExpertCharset.getSID("oneoldstyle"));
        assertEquals("oneoldstyle", cffExpertCharset.getNameForGID(17));

        assertEquals(347, cffExpertCharset.getSIDForGID(134));
        assertEquals(347, cffExpertCharset.getSID("Agravesmall"));
        assertEquals("Agravesmall", cffExpertCharset.getNameForGID(134));

    }

    @Test
    void testCFFExpertSubsetCharset()
    {
        CFFExpertSubsetCharset cffExpertSubsetCharset = CFFExpertSubsetCharset.getInstance();
        // check .notdef mapping
        assertEquals(0, cffExpertSubsetCharset.getSIDForGID(0));
        assertEquals(0, cffExpertSubsetCharset.getSID(".notdef"));
        assertEquals(".notdef", cffExpertSubsetCharset.getNameForGID(0));
        // check some randomly chosen mappings
        assertEquals(246, cffExpertSubsetCharset.getSIDForGID(19));
        assertEquals(246, cffExpertSubsetCharset.getSID("sevenoldstyle"));
        assertEquals("sevenoldstyle", cffExpertSubsetCharset.getNameForGID(19));

        assertEquals(324, cffExpertSubsetCharset.getSIDForGID(61));
        assertEquals(324, cffExpertSubsetCharset.getSID("onethird"));
        assertEquals("onethird", cffExpertSubsetCharset.getNameForGID(61));

        assertEquals(345, cffExpertSubsetCharset.getSIDForGID(85));
        assertEquals(345, cffExpertSubsetCharset.getSID("periodinferior"));
        assertEquals("periodinferior", cffExpertSubsetCharset.getNameForGID(85));

    }

    @Test
    void testCFFISOAdobeCharset()
    {
        CFFISOAdobeCharset cffISOAdobeCharset = CFFISOAdobeCharset.getInstance();
        // check .notdef mapping
        assertEquals(0, cffISOAdobeCharset.getSIDForGID(0));
        assertEquals(0, cffISOAdobeCharset.getSID(".notdef"));
        assertEquals(".notdef", cffISOAdobeCharset.getNameForGID(0));

        // check some randomly chosen mappings
        assertEquals(32, cffISOAdobeCharset.getSIDForGID(32));
        assertEquals(32, cffISOAdobeCharset.getSID("question"));
        assertEquals("question", cffISOAdobeCharset.getNameForGID(32));

        assertEquals(76, cffISOAdobeCharset.getSIDForGID(76));
        assertEquals(76, cffISOAdobeCharset.getSID("k"));
        assertEquals("k", cffISOAdobeCharset.getNameForGID(76));

        assertEquals(218, cffISOAdobeCharset.getSIDForGID(218));
        assertEquals(218, cffISOAdobeCharset.getSID("odieresis"));
        assertEquals("odieresis", cffISOAdobeCharset.getNameForGID(218));

    }

}
