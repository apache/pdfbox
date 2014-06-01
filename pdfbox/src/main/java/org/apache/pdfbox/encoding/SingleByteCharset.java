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
package org.apache.pdfbox.encoding;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * {@link Charset} implementation for the single-byte encodings.
 * @version $Revision$
 */
public class SingleByteCharset extends Charset
{

    /** Unicode replacement character 0xFFFD. */
    protected static final char REPLACEMENT_CHARACTER = '\uFFFD';

    private final char[] toUnicodeMap;
    private byte[][] toByteMap;

    /**
     * Creates a new single-byte charset using an array of unicode characters.
     * @param canonicalName the canonical name
     * @param aliases An array of this charset's aliases, or null if it has no aliases
     * @param toUnicodeMap the array of unicode characters (may have a maximum of 256 characters,
     *          first character must be 0x0000)
     */
    protected SingleByteCharset(String canonicalName, String[] aliases, char[] toUnicodeMap)
    {
        super(canonicalName, aliases);
        if (toUnicodeMap.length > 256)
        {
            throw new IllegalArgumentException("Single-byte encodings may have at most 256 characters.");
        }
        //Copy array so it cannot be changed accidentally from the outside
        this.toUnicodeMap = new char[256];
        System.arraycopy(toUnicodeMap, 0, this.toUnicodeMap, 0, toUnicodeMap.length);
        //build the inverse lookup table
        initInverseMap();
    }

    private void initInverseMap()
    {
        toByteMap = new byte[256][];
        if (toUnicodeMap[0] != '\u0000')
        {
            throw new IllegalArgumentException("First character in map must be a NUL (0x0000) character.");
            //because we're using 0x00 for encoding otherwise unmapped characters
        }

        //we're building a kind of sparse lookup table in which not all subranges are covered.
        for (int i = 1, len = toUnicodeMap.length; i < len; i++)
        {
            char ch = toUnicodeMap[i];
            if (ch == REPLACEMENT_CHARACTER)
            {
                continue; //skip
            }
            int upper = ch >> 8;
            int lower = ch & 0xFF;
            if (upper > 0xFF)
            {
                throw new IllegalArgumentException("Not a compatible character: "
                        + ch + " (" + Integer.toHexString(ch) + ")");
            }
            byte[] map = toByteMap[upper];
            if (map == null)
            {
                map = new byte[256];
                toByteMap[upper] = map;
            }
            map[lower] = (byte)(i & 0xFF);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Charset cs)
    {
        return (cs.getClass() == getClass());
    }

    /** {@inheritDoc} */
    @Override
    public CharsetDecoder newDecoder()
    {
        return new Decoder();
    }

    /** {@inheritDoc} */
    @Override
    public CharsetEncoder newEncoder()
    {
        return new Encoder();
    }

    /** The decoder. */
    private class Decoder extends CharsetDecoder
    {

        protected Decoder()
        {
            super(SingleByteCharset.this, 1, 1);
        }

        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out)
        {
            while (in.hasRemaining())
            {
                byte b = in.get();
                char ch;

                if (!out.hasRemaining())
                {
                    in.position(in.position() - 1);
                    return CoderResult.OVERFLOW;
                }
                ch = toUnicodeMap[b & 0xFF];
                if (ch == REPLACEMENT_CHARACTER)
                {
                    in.position(in.position() - 1);
                    return CoderResult.unmappableForLength(1);
                }
                out.put(ch);
            }
            return CoderResult.UNDERFLOW;
        }

    }

    /** The encoder. */
    private class Encoder extends CharsetEncoder
    {

        protected Encoder()
        {
            super(SingleByteCharset.this, 1, 1);
        }

        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out)
        {
            while (in.hasRemaining())
            {
                int ch = in.get();

                if (!out.hasRemaining())
                {
                    in.position(in.position() - 1);
                    return CoderResult.OVERFLOW;
                }

                int upper = ch >> 8;
                int lower = ch & 0xFF;
                if (upper > 0xFF)
                {
                    in.position(in.position() - 1);
                    return CoderResult.unmappableForLength(1);
                }
                byte[] map = toByteMap[upper];
                if (map == null)
                {
                    in.position(in.position() - 1);
                    return CoderResult.unmappableForLength(1);
                }
                byte b = map[lower];
                if (b == 0x00)
                {
                    in.position(in.position() - 1);
                    return CoderResult.unmappableForLength(1);
                }

                out.put(b);
            }
            return CoderResult.UNDERFLOW;
        }

    }

}
