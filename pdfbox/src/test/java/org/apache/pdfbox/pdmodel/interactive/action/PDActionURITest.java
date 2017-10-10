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
package org.apache.pdfbox.pdmodel.interactive.action;

import java.io.IOException;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class PDActionURITest
{
    /**
     * PDFBOX-3913: Check that URIs encoded in UTF-8 are also supported.
     * PDFBOX-3946: Check that there is no NPE if URI missing.
     */
    @Test
    public void testUTF8URI()
    {
        PDActionURI actionURI = new PDActionURI();
        assertNull(actionURI.getURI());
        actionURI.setURI("http://çµ„åŒ¶æ›¿ç¶Ž.com/");
        assertEquals("http://経営承継.com/", actionURI.getURI());
    }

    /**
     * PDFBOX-3913: Check that URIs encoded in UTF16 (BE) are also supported.
     *
     * @throws IOException
     */
    @Test
    public void testUTF16BEURI() throws IOException
    {
        PDActionURI actionURI = new PDActionURI();
        
        // found in govdocs file 534948.pdf
        COSString utf16URI = COSString.parseHex("FEFF0068007400740070003A002F002F00770077"
                + "0077002E006E00610070002E006500640075002F0063006100740061006C006F006700"
                + "2F00310031003100340030002E00680074006D006C");
        actionURI.getCOSObject().setItem(COSName.URI, utf16URI);
        assertEquals("http://www.nap.edu/catalog/11140.html", actionURI.getURI());
    }

    /**
     * PDFBOX-3913: Check that URIs encoded in UTF16 (LE) are also supported.
     * 
     * @throws IOException
     */
    @Test
    public void testUTF16LEURI() throws IOException
    {
        PDActionURI actionURI = new PDActionURI();
        
        COSString utf16URI = COSString.parseHex("FFFE68007400740070003A00");
        actionURI.getCOSObject().setItem(COSName.URI, utf16URI);
        assertEquals("http:", actionURI.getURI());
    }

    @Test
    public void testUTF7URI()
    {
        PDActionURI actionURI = new PDActionURI();
        actionURI.setURI("http://pdfbox.apache.org/");
        assertEquals("http://pdfbox.apache.org/", actionURI.getURI());
    }
}
