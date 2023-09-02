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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestCOSStream
{
    /**
     * Tests encoding of a stream without any filter applied.
     *
     * @throws IOException
     */
    @Test
    void testUncompressedStreamEncode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes(StandardCharsets.US_ASCII);
        COSStream stream = createStream(testString, null);
        validateEncoded(stream, testString);
    }

    /**
     * Tests decoding of a stream without any filter applied.
     *
     * @throws IOException
     */
    @Test
    void testUncompressedStreamDecode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes(StandardCharsets.US_ASCII);
        COSStream stream = createStream(testString, null);
        validateDecoded(stream, testString);
    }

    /**
     * Tests encoding of a stream with one filter applied.
     *
     * @throws IOException
     */
    @Test
    void testCompressedStream1Encode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes(StandardCharsets.US_ASCII);
        byte[] testStringEncoded = encodeData(testString, COSName.FLATE_DECODE);
        COSStream stream = createStream(testString, COSName.FLATE_DECODE);
        validateEncoded(stream, testStringEncoded);
    }

    /**
     * Tests decoding of a stream with one filter applied.
     *
     * @throws IOException
     */
    @Test
    void testCompressedStream1Decode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes(StandardCharsets.US_ASCII);
        byte[] testStringEncoded = encodeData(testString, COSName.FLATE_DECODE);
        COSStream stream = new COSStream();
        
        try (OutputStream output = stream.createRawOutputStream())
        {
            output.write(testStringEncoded);
        }

        stream.setItem(COSName.FILTER, COSName.FLATE_DECODE);
        validateDecoded(stream, testString);
    }

    /**
     * Tests encoding of a stream with 2 filters applied.
     *
     * @throws IOException
     */
    @Test
    void testCompressedStream2Encode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes(StandardCharsets.US_ASCII);
        byte[] testStringEncoded = encodeData(testString, COSName.FLATE_DECODE);
        testStringEncoded = encodeData(testStringEncoded, COSName.ASCII85_DECODE);
        
        COSArray filters = new COSArray();
        filters.add(COSName.ASCII85_DECODE);
        filters.add(COSName.FLATE_DECODE);
        
        COSStream stream = createStream(testString, filters);
        validateEncoded(stream, testStringEncoded);
    }

    /**
     * Tests decoding of a stream with 2 filters applied.
     *
     * @throws IOException
     */
    @Test
    void testCompressedStream2Decode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes(StandardCharsets.US_ASCII);
        byte[] testStringEncoded = encodeData(testString, COSName.FLATE_DECODE);
        testStringEncoded = encodeData(testStringEncoded, COSName.ASCII85_DECODE);
        COSStream stream = new COSStream();
        
        COSArray filters = new COSArray();
        filters.add(COSName.ASCII85_DECODE);
        filters.add(COSName.FLATE_DECODE);
        stream.setItem(COSName.FILTER, filters);
        
        try (OutputStream output = stream.createRawOutputStream())
        {
            output.write(testStringEncoded);
        }
        
        validateDecoded(stream, testString);
    }

    /**
     * Tests tests that encoding is done correctly even if the the stream is closed twice.
     * Closeable.close() allows streams to be closed multiple times. The second and subsequent
     * close() calls should have no effect.
     *
     * @throws IOException
     */
    @Test
    void testCompressedStreamDoubleClose() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes(StandardCharsets.US_ASCII);
        byte[] testStringEncoded = encodeData(testString, COSName.FLATE_DECODE);
        COSStream stream = new COSStream();
        OutputStream output = stream.createOutputStream(COSName.FLATE_DECODE);
        output.write(testString);
        output.close();
        output.close();
        validateEncoded(stream, testStringEncoded);
    }

    @Test
    void testHasStreamData() throws IOException
    {
        try (COSStream stream = new COSStream())
        {
            assertFalse(stream.hasData());
            Assertions.assertThrows(IOException.class, () -> stream.createInputStream(),
                "createInputStream should have thrown an IOException");

            byte[] testString = "This is a test string to be used as input for TestCOSStream"
                    .getBytes(StandardCharsets.US_ASCII);
            try (OutputStream output = stream.createOutputStream())
            {
                output.write(testString);
            }
            assertTrue(stream.hasData());
        }
    }

    private byte[] encodeData(byte[] original, COSName filter) throws IOException
    {
        Filter encodingFilter = FilterFactory.INSTANCE.getFilter(filter);
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        encodingFilter.encode(new ByteArrayInputStream(original), encoded, new COSDictionary(), 0);
        return encoded.toByteArray();
    }

    private COSStream createStream(byte[] testString, COSBase filters) throws IOException
    {
        COSStream stream = new COSStream();
        try (OutputStream output = stream.createOutputStream(filters))
        {
            output.write(testString);
        }
        return stream;
    }

    private void validateEncoded(COSStream stream, byte[] expected) throws IOException
    {
        InputStream in = stream.createRawInputStream();
        byte[] decoded = in.readAllBytes();
        stream.close();
        assertTrue(Arrays.equals(expected, decoded), "Encoded data doesn't match input");
    }

    private void validateDecoded(COSStream stream, byte[] expected) throws IOException
    {
        InputStream in = stream.createInputStream();
        byte[] encoded = in.readAllBytes();
        stream.close();
        assertTrue(Arrays.equals(expected, encoded), "Decoded data doesn't match input");
    }
}
