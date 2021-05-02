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
package org.apache.pdfbox.pdfparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.junit.jupiter.api.Test;

/**
 * Test for PDFObjectStreamParser.
 */
class PDFObjectStreamParserTest
{
    @Test
    void testOffsetParsing() throws IOException
    {
        COSStream stream = new COSStream();
        stream.setItem(COSName.N, COSInteger.TWO);
        stream.setItem(COSName.FIRST, COSInteger.get(8));
        OutputStream outputStream = stream.createOutputStream();
        outputStream.write("1 0 2 5 true false".getBytes());
        outputStream.close();
        PDFObjectStreamParser objectStreamParser = new PDFObjectStreamParser(stream, null);
        Map<Long, Integer> objectNumbers = objectStreamParser.readObjectNumbers();
        assertEquals(2, objectNumbers.size());
        Long[] numbers = objectNumbers.keySet().toArray(new Long[0]);
        objectStreamParser = new PDFObjectStreamParser(stream, null);
        assertEquals(COSBoolean.TRUE, objectStreamParser.parseObject(numbers[0]));
        objectStreamParser = new PDFObjectStreamParser(stream, null);
        assertEquals(COSBoolean.FALSE, objectStreamParser.parseObject(numbers[1]));
    }
}
