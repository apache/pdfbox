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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.io.IOUtils;

public class TestCOSStream extends TestCase
{
    /**
     * Tests encoding of a stream without any filter applied.
     *
     * @throws IOException
     */
    public void testUncompressedStreamEncode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes("ASCII");
        COSStream stream = createStream(testString, null);
        validateEncoded(stream, testString);
    }

    /**
     * Tests decoding of a stream without any filter applied.
     *
     * @throws IOException
     */
    public void testUncompressedStreamDecode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes("ASCII");
        COSStream stream = createStream(testString, null);
        validateDecoded(stream, testString);
    }

    /**
     * Tests encoding of a stream with one filter applied.
     *
     * @throws IOException
     */
    public void testCompressedStream1Encode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes("ASCII");
        byte[] testStringEncoded = encodeData(testString, COSName.FLATE_DECODE);
        COSStream stream = createStream(testString, COSName.FLATE_DECODE);
        validateEncoded(stream, testStringEncoded);
    }

    /**
     * Tests decoding of a stream with one filter applied.
     *
     * @throws IOException
     */
    public void testCompressedStream1Decode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes("ASCII");
        byte[] testStringEncoded = encodeData(testString, COSName.FLATE_DECODE);
        COSStream stream = new COSStream();
        
        OutputStream output = stream.createRawOutputStream();
        output.write(testStringEncoded);
        output.close();

        stream.setItem(COSName.FILTER, COSName.FLATE_DECODE);
        validateDecoded(stream, testString);
    }

    /**
     * Tests encoding of a stream with 2 filters applied.
     *
     * @throws IOException
     */
    public void testCompressedStream2Encode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes("ASCII");
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
    public void testCompressedStream2Decode() throws IOException
    {
        byte[] testString = "This is a test string to be used as input for TestCOSStream".getBytes("ASCII");
        byte[] testStringEncoded = encodeData(testString, COSName.FLATE_DECODE);
        testStringEncoded = encodeData(testStringEncoded, COSName.ASCII85_DECODE);
        COSStream stream = new COSStream();
        
        COSArray filters = new COSArray();
        filters.add(COSName.ASCII85_DECODE);
        filters.add(COSName.FLATE_DECODE);
        stream.setItem(COSName.FILTER, filters);
        
        OutputStream output = stream.createRawOutputStream();
        output.write(testStringEncoded);
        output.close();
        
        validateDecoded(stream, testString);
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
        OutputStream output = stream.createOutputStream(filters);
        output.write(testString);
        output.close();
        return stream;
    }

    private void validateEncoded(COSStream stream, byte[] expected) throws IOException
    {
        byte[] decoded = IOUtils.toByteArray(stream.createRawInputStream());
        stream.close();
        assertTrue("Encoded data doesn't match input", Arrays.equals(expected, decoded));
    }

    private void validateDecoded(COSStream stream, byte[] expected) throws IOException
    {
        byte[] encoded = IOUtils.toByteArray(stream.createInputStream());
        stream.close();
        assertTrue("Decoded data doesn't match input", Arrays.equals(expected, encoded));
    }
}
