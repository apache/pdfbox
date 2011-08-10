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

package org.apache.pdfbox.io.ccitt;

import junit.framework.TestCase;

/**
 * Abstract base class for testing CCITT fax encoding.
 * @version $Revision$
 */
public abstract class AbstractCCITTFaxTestCase extends TestCase
{

    /**
     * Visualizes a packed bitmap and dumps it on System.out.
     * @param data the bitmap
     * @param columns the number of columns
     */
    protected void dumpBitmap(byte[] data, int columns)
    {
        int lineBytes = columns / 8;
        if (columns % 8 != 0)
        {
            lineBytes++;
        }
        int lines = data.length / lineBytes;
        for (int y = 0; y < lines; y++)
        {
            int start = y * lineBytes;
            for (int x = 0; x < columns; x++)
            {
                int index = start + (x / 8);
                int mask = 1 << (7 - (x % 8));
                int value = data[index] & mask;
                System.out.print(value != 0 ? 'X' : '_');
            }
            System.out.println();
        }
    }

    /**
     * Converts a series of bytes to a "binary" String of 0s and 1s.
     * @param data the data
     * @return the binary string
     */
    protected String toBitString(byte[] data)
    {
        return PackedBitArray.toBitString(data);
    }

}
