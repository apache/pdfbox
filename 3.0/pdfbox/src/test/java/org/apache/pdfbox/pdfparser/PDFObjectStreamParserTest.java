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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObjectKey;
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
        outputStream.write("4 0 6 5 true false".getBytes());
        outputStream.close();
        PDFObjectStreamParser objectStreamParser = new PDFObjectStreamParser(stream, null);
        Map<Long, Integer> objectNumbers = objectStreamParser.readObjectNumbers();
        assertEquals(2, objectNumbers.size());
        assertEquals(0, objectNumbers.get(4L));
        assertEquals(5, objectNumbers.get(6L));
        objectStreamParser = new PDFObjectStreamParser(stream, null);
        assertEquals(COSBoolean.TRUE, objectStreamParser.parseObject(4));
        objectStreamParser = new PDFObjectStreamParser(stream, null);
        assertEquals(COSBoolean.FALSE, objectStreamParser.parseObject(6));
    }

    @Test
    void testParseAllObjects() throws IOException
    {
        COSStream stream = new COSStream();
        stream.setItem(COSName.N, COSInteger.TWO);
        stream.setItem(COSName.FIRST, COSInteger.get(8));
        OutputStream outputStream = stream.createOutputStream();
        outputStream.write("6 0 4 5 true false".getBytes());
        outputStream.close();
        PDFObjectStreamParser objectStreamParser = new PDFObjectStreamParser(stream, null);
        Map<COSObjectKey, COSBase> objectNumbers = objectStreamParser.parseAllObjects();
        assertEquals(2, objectNumbers.size());
        assertEquals(COSBoolean.TRUE, objectNumbers.get(new COSObjectKey(6, 0)));
        assertEquals(COSBoolean.FALSE, objectNumbers.get(new COSObjectKey(4, 0)));
    }

    @Test
    void testParseAllObjectsIndexed() throws IOException
    {
        COSStream stream = new COSStream();
        stream.setItem(COSName.N, COSInteger.THREE);
        stream.setItem(COSName.FIRST, COSInteger.get(13));
        OutputStream outputStream = stream.createOutputStream();
        // use object number 4 for two objects
        outputStream.write("6 0 4 5 4 11 true false true".getBytes());
        outputStream.close();
        COSDocument cosDoc = new COSDocument();
        Map<COSObjectKey, Long> xrefTable = cosDoc.getXrefTable();
        // select the second object from the stream for object number 4 by using 2 as value for the index
        xrefTable.put(new COSObjectKey(6, 0, 0), -1L);
        xrefTable.put(new COSObjectKey(4, 0, 2), -1L);
        PDFObjectStreamParser objectStreamParser = new PDFObjectStreamParser(stream, cosDoc);
        Map<COSObjectKey, COSBase> objectNumbers = objectStreamParser.parseAllObjects();
        assertEquals(2, objectNumbers.size());
        assertEquals(COSBoolean.TRUE, objectNumbers.get(new COSObjectKey(6, 0)));
        assertEquals(COSBoolean.TRUE, objectNumbers.get(new COSObjectKey(4, 0)));

        // select the first object from the stream for object number 4 by using 1 as value for the index
        // remove the old entry first to be sure it is replaced
        xrefTable.remove(new COSObjectKey(4, 0));
        xrefTable.put(new COSObjectKey(4, 0, 1), -1L);
        objectStreamParser = new PDFObjectStreamParser(stream, cosDoc);
        objectNumbers = objectStreamParser.parseAllObjects();
        assertEquals(2, objectNumbers.size());
        assertEquals(COSBoolean.TRUE, objectNumbers.get(new COSObjectKey(6, 0)));
        assertEquals(COSBoolean.FALSE, objectNumbers.get(new COSObjectKey(4, 0)));
    }

    @Test
    void testParseAllObjectsSkipMalformedIndex() throws IOException
    {
        COSStream stream = new COSStream();
        stream.setItem(COSName.N, COSInteger.THREE);
        stream.setItem(COSName.FIRST, COSInteger.get(13));
        OutputStream outputStream = stream.createOutputStream();
        outputStream.write("6 0 4 5 5 11 true false true".getBytes());
        outputStream.close();
        COSDocument cosDoc = new COSDocument();
        Map<COSObjectKey, Long> xrefTable = cosDoc.getXrefTable();
        // add an index for each object key which doesn't match with the index of the object stream
        xrefTable.put(new COSObjectKey(6, 0, 10), -1L);
        xrefTable.put(new COSObjectKey(4, 0, 11), -1L);
        xrefTable.put(new COSObjectKey(5, 0, 12), -1L);
        PDFObjectStreamParser objectStreamParser = new PDFObjectStreamParser(stream, cosDoc);
        // the index isn't taken into account as all object numbers of the stream are unique
        // none of the objects is skipped so that all objects are read and available
        Map<COSObjectKey, COSBase> objectNumbers = objectStreamParser.parseAllObjects();
        assertEquals(3, objectNumbers.size());
        assertEquals(COSBoolean.TRUE, objectNumbers.get(new COSObjectKey(6, 0)));
        assertEquals(COSBoolean.FALSE, objectNumbers.get(new COSObjectKey(4, 0)));
        assertEquals(COSBoolean.TRUE, objectNumbers.get(new COSObjectKey(5, 0)));
    }

    @Test
    void testParseAllObjectsUseMalformedIndex() throws IOException
    {
        COSStream stream = new COSStream();
        stream.setItem(COSName.N, COSInteger.THREE);
        stream.setItem(COSName.FIRST, COSInteger.get(13));
        OutputStream outputStream = stream.createOutputStream();
        outputStream.write("6 0 4 5 4 11 true false true".getBytes());
        outputStream.close();
        COSDocument cosDoc = new COSDocument();
        Map<COSObjectKey, Long> xrefTable = cosDoc.getXrefTable();
        // add an index for each object key which doesn't match with the index of the object stream
        // add two object keys only as the object stream uses one object number for two objects
        xrefTable.put(new COSObjectKey(6, 0, 10), -1L);
        xrefTable.put(new COSObjectKey(4, 0, 11), -1L);
        PDFObjectStreamParser objectStreamParser = new PDFObjectStreamParser(stream, cosDoc);
        // as the used object numbers aren't unique within the object the index of the obejct keys is used
        // All objects are dropped as the malformed index values don't match the index of the object within the stream
        Map<COSObjectKey, COSBase> objectNumbers = objectStreamParser.parseAllObjects();
        assertEquals(0, objectNumbers.size());
    }

}
