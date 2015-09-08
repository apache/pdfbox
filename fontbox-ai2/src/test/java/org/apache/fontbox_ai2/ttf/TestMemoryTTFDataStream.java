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
package org.apache.fontbox_ai2.ttf;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import junit.framework.TestCase;

public class TestMemoryTTFDataStream extends TestCase
{
    @Test
    public void testEOF() throws IOException
    {
        byte[] byteArray = new byte[10];
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        MemoryTTFDataStream dataStream = new MemoryTTFDataStream(inputStream);
        int value = dataStream.read();
        try
        {
            while (value > -1)
            {
                value = dataStream.read();
            }
        }
        catch (ArrayIndexOutOfBoundsException exception)
        {
            fail("EOF not detected!");
        }
        finally
        {
            if (dataStream != null)
            {
                dataStream.close();
            }
        }
    }

}
