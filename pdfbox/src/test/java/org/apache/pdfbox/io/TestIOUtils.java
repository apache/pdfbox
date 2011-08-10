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

/* $Id$ */

package org.apache.pdfbox.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * This is a unit test for {@link IOUtils}.
 * @version $Revision$
 */
public class TestIOUtils extends TestCase
{

    /**
     * Tests {@link IOUtils#populateBuffer(java.io.InputStream, byte[]).
     * @throws IOException if an I/O error occursn
     */
    public void testPopulateBuffer() throws IOException
    {
        byte[] data = "Hello World!".getBytes();
        byte[] buffer = new byte[data.length];
        long count = IOUtils.populateBuffer(new ByteArrayInputStream(data), buffer);
        assertEquals(12, count);

        buffer = new byte[data.length - 2]; //Buffer too small
        InputStream in = new ByteArrayInputStream(data);
        count = IOUtils.populateBuffer(in, buffer);
        assertEquals(10, count);
        byte[] leftOver = IOUtils.toByteArray(in);
        assertEquals(2, leftOver.length);

        buffer = new byte[data.length + 2]; //Buffer too big
        in = new ByteArrayInputStream(data);
        count = IOUtils.populateBuffer(in, buffer);
        assertEquals(12, count);
        assertEquals(-1, in.read()); //EOD reached
    }

}
