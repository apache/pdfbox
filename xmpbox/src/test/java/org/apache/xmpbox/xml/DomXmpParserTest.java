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

package org.apache.xmpbox.xml;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xmpbox.XMPMetadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class DomXmpParserTest
{
    public DomXmpParserTest()
    {
    }

    @Test
    void testPDFBox5649() throws IOException, XmpParsingException
    {
        try (InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/xml/PDFBOX-5649.xml"))
        {
            DomXmpParser dxp = new DomXmpParser();
            dxp.setStrictParsing(false);
            XMPMetadata xmp = dxp.parse(fis);
            Assertions.assertNotNull(xmp);
        }
    }

    @Test
    void testPDFBox5835() throws IOException, XmpParsingException
    {
        try (InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/xml/PDFBOX-5835.xml"))
        {
            DomXmpParser dxp = new DomXmpParser();
            dxp.setStrictParsing(false);
            XMPMetadata xmp = dxp.parse(fis);
            Assertions.assertEquals("A", xmp.getPDFAIdentificationSchema().getConformance());
            Assertions.assertEquals((Integer) 3, xmp.getPDFAIdentificationSchema().getPart());
        }
    }
}
