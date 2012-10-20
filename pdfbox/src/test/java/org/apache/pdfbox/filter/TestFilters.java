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
import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.pdfbox.cos.COSDictionary;


/**
 * This will test all of the filters in the PDFBox system.
 */
public class TestFilters extends TestCase
{

    /**
     * This will test all of the filters in the system.
     *
     * @throws IOException If there is an exception while encoding.
     */
    public void testFilters() throws IOException
    {
        for(int iter=0;iter<10;iter++) 
        {
            final long seed = new Random().nextLong();
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
                        // Fill w/ truly random bytes:
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
        
                FilterManager manager = new FilterManager();
                for( Filter filter : manager.getFilters() ) 
                {
                    // Skip filters that don't currently support roundtripping
                    if( filter instanceof DCTFilter ||
                        filter instanceof CCITTFaxDecodeFilter ||
                        filter instanceof JPXFilter ||
                        filter instanceof JBIG2Filter ||
                        filter instanceof RunLengthDecodeFilter )
                        {
                            continue;
                        }

                    ByteArrayOutputStream encoded = new ByteArrayOutputStream();
                    filter.encode(
                                  new ByteArrayInputStream( original ),
                                  encoded, new COSDictionary(), 0 );

                    ByteArrayOutputStream decoded = new ByteArrayOutputStream();
                    filter.decode(
                                  new ByteArrayInputStream( encoded.toByteArray() ),
                                  decoded, new COSDictionary(), 0 );

                    assertTrue(
                               "Data that is encoded and then decoded through "
                               + filter.getClass() + " does not match the original data",
                               Arrays.equals( original, decoded.toByteArray() ) );
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

}
