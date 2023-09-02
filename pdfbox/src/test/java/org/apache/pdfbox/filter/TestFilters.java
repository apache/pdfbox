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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Random;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.junit.jupiter.api.Test;

/**
 * This will test all of the filters in the PDFBox system.
 */
class TestFilters
{
    /**
     * This will test all of the filters in the system. There will be COUNT
     * of deterministic tests and COUNT of non-deterministic tests, see also
     * the discussion in PDFBOX-1977.
     *
     * @throws IOException If there is an exception while encoding.
     */
    @Test
    void testFilters() throws IOException
    {
        final int COUNT = 10;
        Random rd = new Random(123456);
        for (int iter = 0; iter < COUNT * 2; iter++)
        {
            long seed;
            if (iter < COUNT)
            {
                // deterministic seed
                seed = rd.nextLong();
            }
            else
            {
                // non-deterministic seed
                seed = new Random().nextLong();
            }
            boolean success = false;
            try 
            {
                final Random random = new Random(seed);
                final int numBytes = 10000 + random.nextInt(20000);
                byte[] original = new byte[numBytes];

                int upto = 0;
                while(upto < numBytes) 
                {
                    final int left = numBytes - upto;
                    if (random.nextBoolean() || left < 2) 
                    {
                        // Fill w/ pseudo-random bytes:
                        final int end = upto + Math.min(left, 10+random.nextInt(100));
                        while(upto < end) 
                        {
                            original[upto++] = (byte) random.nextInt();
                        }
                    } 
                    else 
                    {
                        // Fill w/ very predictable bytes:
                        final int end = upto + Math.min(left, 2+random.nextInt(10));
                        final byte value = (byte) random.nextInt(4);
                        while(upto < end) 
                        {
                            original[upto++] = value;
                        }
                    }
                }

                for( Filter filter : FilterFactory.INSTANCE.getAllFilters() )
                {
                    // Skip filters that don't currently support roundtripping
                    if( filter instanceof DCTFilter ||
                        filter instanceof CCITTFaxFilter ||
                        filter instanceof JPXFilter ||
                        filter instanceof JBIG2Filter)
                        {
                            continue;
                        }

                    checkEncodeDecode(filter, original);
                }
                success = true;
            } 
            finally 
            {
                if (!success) 
                {
                    System.err.println("NOTE: test failed with seed=" + seed);
                }
            }
        }
    }
    
    /**
     * This will test the use of identity filter to decode stream and string.
     * This test threw an IOException before the correction.
     * 
     * @throws IOException
     */
    @Test
    void testPDFBOX4517() throws IOException
    {
        Loader.loadPDF(new File("target/pdfs/PDFBOX-4517-cryptfilter.pdf"),
                "userpassword1234");
    }

    /**
     * This will test the LZW filter with the sequence that failed in PDFBOX-1977.
     * To check that the test itself is legit, revert LZWFilter.java to rev 1571801,
     * which should fail this test.
     * 
     * @throws IOException 
     */
    @Test
    void testPDFBOX1977() throws IOException
    {
        Filter lzwFilter = FilterFactory.INSTANCE.getFilter(COSName.LZW_DECODE);
        InputStream in = this.getClass().getResourceAsStream("PDFBOX-1977.bin");
        byte[] byteArray = in.readAllBytes();
        checkEncodeDecode(lzwFilter, byteArray);
    }

    /**
     * Test simple and corner cases (128 identical, 128 identical at the end) of RLE implementation.
     * 128 non identical bytes likely to be caught in random testing.
     *
     * @throws IOException
     */
    @Test
    void testRLE() throws IOException
    {
        Filter rleFilter = FilterFactory.INSTANCE.getFilter(COSName.RUN_LENGTH_DECODE);
        byte[] input0 = new byte[0];
        checkEncodeDecode(rleFilter, input0);
        byte[] input1 = new byte[] { 1, 2, 3, 4, 5, (byte) 128, (byte) 140, (byte) 180, (byte) 0xFF};
        checkEncodeDecode(rleFilter, input1);
        byte[] input2 = new byte[10];
        checkEncodeDecode(rleFilter, input2);
        byte[] input3 = new byte[128];
        checkEncodeDecode(rleFilter, input3);
        byte[] input4 = new byte[129];
        checkEncodeDecode(rleFilter, input4);
        byte[] input5 = new byte[128 + 128];
        checkEncodeDecode(rleFilter, input5);
        byte[] input6 = new byte[1];
        checkEncodeDecode(rleFilter, input6);
        byte[] input7 = new byte[] {1, 2};
        checkEncodeDecode(rleFilter, input7);
        byte[] input8 = new byte[2];
        checkEncodeDecode(rleFilter, input8);
    }

    private void checkEncodeDecode(Filter filter, byte[] original) throws IOException
    {
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        filter.encode(new ByteArrayInputStream(original), encoded, new COSDictionary());
        ByteArrayOutputStream decoded = new ByteArrayOutputStream();
        filter.decode(new ByteArrayInputStream(encoded.toByteArray()),
                decoded, new COSDictionary(), 0);

        assertArrayEquals(original, decoded.toByteArray(),
                "Data that is encoded and then decoded through " + filter.getClass()
                        + " does not match the original data");
    }
}
