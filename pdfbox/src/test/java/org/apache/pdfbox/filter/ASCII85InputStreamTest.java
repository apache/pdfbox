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
package org.apache.pdfbox.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ASCII85InputStreamTest
{
    @Test
    void testPDFBOX6317() throws IOException
    {
        String endMarker = "~>";
        String s1 = "87cURD]i,\"Ebo80";
        String s2 = "87cURD]i";
        String s3 = "z";
        assertEquals("Hello World!", decodeString(s1 + endMarker));
        assertEquals("Hello World!", decodeString(s1));
        assertEquals("Hello ", decodeString(s2 + endMarker));
        assertEquals("Hello ", decodeString(s2));
        assertEquals("\000\000\000\000", decodeString(s3));
    }

    @Test
    void testPDFBox6330() throws IOException
    {
        String s1 = "6<#'\\7PQ#@\t1a#b0+?)%u\t2_Zp.<+I+\"\t87cURD]hkE\t7RD$<.3MT)\t+@T6:~>";
        assertEquals("BT /F1 24 Tf 72 700 Td (Hello PDFBox) Tj ET\n", decodeString(s1));
    }

    private String decodeString(String inputString) throws IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.US_ASCII));
        InputStream in = new ASCII85InputStream(bais);
        return new String(in.readAllBytes(), StandardCharsets.US_ASCII);
    }
}
