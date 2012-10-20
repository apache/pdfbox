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

/**
 * Represents an array of bits packed in a byte array of fixed size.
 * @version $Revision$
 */
public class PackedBitArray
{

    private int bitCount;
    private byte[] data;

    /**
     * Constructs a new bit array.
     * @param bitCount the number of bits to maintain
     */
    public PackedBitArray(int bitCount)
    {
        this.bitCount = bitCount;
        int byteCount = (bitCount + 7) / 8;
        this.data = new byte[byteCount];
    }

    private int byteOffset(int offset)
    {
        return offset / 8;
    }

    private int bitOffset(int offset)
    {
        return offset % 8;
    }

    /**
     * Sets a bit at the given offset.
     * @param offset the offset
     */
    public void set(int offset)
    {
        int byteOffset = byteOffset(offset);
        this.data[byteOffset] |= 1 << bitOffset(offset);
    }

    /**
     * Clears a bit at the given offset.
     * @param offset the offset
     */
    public void clear(int offset)
    {
        int byteOffset = byteOffset(offset);
        int bitOffset = bitOffset(offset);
        this.data[byteOffset] &= ~(1 << bitOffset);
    }

    /**
     * Sets a run of bits at the given offset to either 1 or 0.
     * @param offset the offset
     * @param length the number of bits to set
     * @param bit 1 to set the bit, 0 to clear it
     */
    public void setBits(int offset, int length, int bit)
    {
        if (bit == 0)
        {
            clearBits(offset, length);
        }
        else
        {
            setBits(offset, length);
        }
    }

    /**
     * Sets a run of bits at the given offset to either 1.
     * @param offset the offset
     * @param length the number of bits to set
     */
    public void setBits(int offset, int length)
    {
        if (length == 0)
        {
            return;
        }
        int startBitOffset = bitOffset(offset);
        int firstByte = byteOffset(offset);
        int lastBitOffset = offset + length;
        if (lastBitOffset > getBitCount())
        {
            throw new IndexOutOfBoundsException("offset + length > bit count");
        }
        int lastByte = byteOffset(lastBitOffset);
        int endBitOffset = bitOffset(lastBitOffset);

        if (firstByte == lastByte)
        {
            //Only one byte affected
            int mask = (1 << endBitOffset) - (1 << startBitOffset);
            this.data[firstByte] |= mask;
        }
        else
        {
            //Bits spanning multiple bytes
            this.data[firstByte] |= 0xFF << startBitOffset;
            for (int i = firstByte + 1; i < lastByte; i++)
            {
                this.data[i] = (byte)0xFF;
            }
            if (endBitOffset > 0)
            {
                this.data[lastByte] |= 0xFF >> (8 - endBitOffset);
            }
        }
    }

    /**
     * Clears a run of bits at the given offset.
     * @param offset the offset
     * @param length the number of bits to clear
     */
    public void clearBits(int offset, int length)
    {
        if (length == 0)
        {
            return;
        }
        int startBitOffset = offset % 8;
        int firstByte = byteOffset(offset);
        int lastBitOffset = offset + length;
        int lastByte = byteOffset(lastBitOffset);
        int endBitOffset = lastBitOffset % 8;

        if (firstByte == lastByte)
        {
            //Only one byte affected
            int mask = (1 << endBitOffset) - (1 << startBitOffset);
            this.data[firstByte] &= ~mask;
        }
        else
        {
            //Bits spanning multiple bytes
            this.data[firstByte] &= ~(0xFF << startBitOffset);
            for (int i = firstByte + 1; i < lastByte; i++)
            {
                this.data[i] = (byte)0x00;
            }
            if (endBitOffset > 0)
            {
                this.data[lastByte] &= ~(0xFF >> (8 - endBitOffset));
            }
        }
    }

    /**
     * Clear all bits in the array.
     */
    public void clear()
    {
        clearBits(0, getBitCount());
    }

    /**
     * Returns the number of bits maintained by this array.
     * @return the number of bits
     */
    public int getBitCount()
    {
        return this.bitCount;
    }

    /**
     * Returns the size of the byte buffer for this array.
     * @return the size of the byte buffer
     */
    public int getByteCount()
    {
        return this.data.length;
    }

    /**
     * Returns the underlying byte buffer.
     * <p>
     * Note: the actual buffer is returned. If it's manipulated
     * the content of the bit array changes.
     * @return the underlying data buffer
     */
    public byte[] getData()
    {
        return this.data;
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return toBitString(this.data).substring(0, this.bitCount);
    }

    /**
     * Converts a byte to a "binary" String of 0s and 1s.
     * @param data the value to convert
     * @return the binary string
     */
    public static String toBitString(byte data)
    {
        byte[] buf = new byte[] {data};
        return toBitString(buf);
    }

    /**
     * Converts a series of bytes to a "binary" String of 0s and 1s.
     * @param data the data
     * @return the binary string
     */
    public static String toBitString(byte[] data)
    {
        return toBitString(data, 0, data.length);
    }

    /**
     * Converts a series of bytes to a "binary" String of 0s and 1s.
     * @param data the data
     * @param start the start offset
     * @param len the number of bytes to convert
     * @return the binary string
     */
    public static String toBitString(byte[] data, int start, int len)
    {
        StringBuffer sb = new StringBuffer();
        for (int x = start, end = start + len; x < end; x++)
        {
            for (int i = 0; i < 8; i++)
            {
                int mask = 1 << i;
                int value = data[x] & mask;
                sb.append(value != 0 ? '1' : '0');
            }
        }
        return sb.toString();
    }

}
