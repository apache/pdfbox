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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.cos.COSDictionary;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ASCIIHexFilterTest
{
    @Test
    void testPDFBox6330() throws IOException
    {
        String s =
                "4254202F46312032342054662037322\n" +
                "0373030205464202848656C6C6F2050\n" +
                "4446426F782920546A2045540A>";
        ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes(StandardCharsets.US_ASCII));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ASCIIHexFilter().decode(bais, baos, new COSDictionary(), 0);
        String decoded = baos.toString(StandardCharsets.US_ASCII);
        assertEquals("BT /F1 24 Tf 72 700 Td (Hello PDFBox) Tj ET\n", decoded);
    }
}
