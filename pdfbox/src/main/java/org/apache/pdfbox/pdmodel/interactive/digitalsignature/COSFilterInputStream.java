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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * A filtered stream that includes the bytes that are in the (begin,length) intervals passed in the
 * constructor.
 *
 * @author boix_jor
 *
 */
public class COSFilterInputStream extends FilterInputStream
{
    private int[][] ranges;
    private int range;
    private long position = 0;

    public COSFilterInputStream(InputStream in, int[] byteRange)
    {
        super(in);
        calculateRanges(byteRange);
    }

    public COSFilterInputStream(byte[] in, int[] byteRange)
    {
        this(new ByteArrayInputStream(in), byteRange);
    }

    @Override
    public int read() throws IOException
    {
        if ((this.range == -1 || getRemaining() <= 0) && !nextRange())
        {
            return -1; // EOF
        }
        int result = super.read();
        this.position++;
        return result;
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if ((this.range == -1 || getRemaining() <= 0) && !nextRange())
        {
            return -1; // EOF
        }
        int bytesRead = super.read(b, off, (int) Math.min(len, getRemaining()));
        this.position += bytesRead;
        return bytesRead;
    }

    public byte[] toByteArray() throws IOException
    {
        return readAllBytes();
    }

    private void calculateRanges(int[] byteRange)
    {
        this.ranges = new int[byteRange.length / 2][];
        for (int i = 0; i < byteRange.length / 2; i++)
        {
            this.ranges[i] = new int[] { byteRange[i * 2], byteRange[i * 2] + byteRange[i * 2 + 1] };
        }
        this.range = -1;
    }

    private long getRemaining()
    {
        return this.ranges[this.range][1] - this.position;
    }

    private boolean nextRange() throws IOException
    {
        if (this.range + 1 < this.ranges.length)
        {
            this.range++;
            while (this.position < this.ranges[this.range][0])
            {
                long skipped = super.skip(this.ranges[this.range][0] - this.position);
                if (skipped == 0)
                {
                    throw new IOException("FilterInputStream.skip() returns 0, range: " +
                            Arrays.toString(this.ranges[this.range]));
                }
                this.position += skipped;
            }
            return true;
        }
        else
        {
            return false;
        }
    }
}
