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

package org.apache.pdfbox.pdmodel.font;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.pdfbox.util.Hex;

/**
 * Writes ToUnicode Mapping Files.
 *
 * @author John Hewson
 */
final class ToUnicodeWriter
{
    private final Map<Integer, String> cidToUnicode = new TreeMap<>();
    private int wMode;

    /**
     * To test corner case of PDFBOX-4302.
     */
    static final int MAX_ENTRIES_PER_OPERATOR = 100;

    /**
     * Creates a new ToUnicode CMap writer.
     */
    ToUnicodeWriter()
    {
        this.wMode = 0;
    }

    /**
     * Sets the WMode (writing mode) of this CMap.
     *
     * @param wMode 1 for vertical, 0 for horizontal (default)
     */
    public void setWMode(int wMode)
    {
        this.wMode = wMode;
    }

    /**
     * Adds the given CID to Unicode mapping.
     *
     * @param cid CID
     * @param text Unicode text, up to 512 bytes.
     */
    public void add(int cid, String text)
    {
        if (cid < 0 || cid > 0xFFFF)
        {
            throw new IllegalArgumentException("CID is not valid");
        }

        if (text == null || text.isEmpty())
        {
            throw new IllegalArgumentException("Text is null or empty");
        }

        cidToUnicode.put(cid, text);
    }

    /**
     * Writes the CMap as ASCII to the given output stream.
     *
     * @param out ASCII output stream
     * @throws IOException if the stream could not be written
     */
    public void writeTo(OutputStream out) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.US_ASCII));

        writeLine(writer, "/CIDInit /ProcSet findresource begin");
        writeLine(writer, "12 dict begin\n");

        writeLine(writer, "begincmap");
        writeLine(writer, "/CIDSystemInfo");
        writeLine(writer, "<< /Registry (Adobe)");
        writeLine(writer, "/Ordering (UCS)");
        writeLine(writer, "/Supplement 0");
        writeLine(writer, ">> def\n");

        writeLine(writer, "/CMapName /Adobe-Identity-UCS" + " def");
        writeLine(writer, "/CMapType 2 def\n"); // 2 = ToUnicode

        if (wMode != 0)
        {
            writeLine(writer, "/WMode /" + wMode + " def");
        }

        // ToUnicode always uses 16-bit CIDs
        writeLine(writer, "1 begincodespacerange");
        writeLine(writer, "<0000> <FFFF>");
        writeLine(writer, "endcodespacerange\n");

        // CID -> Unicode mappings, we use ranges to generate a smaller CMap
        List<Integer> srcFrom = new ArrayList<>();
        List<Integer> srcTo = new ArrayList<>();
        List<String> dstString = new ArrayList<>();

        Map.Entry<Integer, String> prev = null;

        for (Map.Entry<Integer, String> next : cidToUnicode.entrySet())
        {
            if (allowCIDToUnicodeRange(prev, next))
            {
                // extend range
                srcTo.set(srcTo.size() - 1, next.getKey());
            }
            else
            {
                // begin range
                srcFrom.add(next.getKey());
                srcTo.add(next.getKey());
                dstString.add(next.getValue());
            }
            prev = next;
        }

        // limit entries per operator
        int batchCount = (int) Math.ceil(srcFrom.size() /
                                         (double) MAX_ENTRIES_PER_OPERATOR);
        for (int batch = 0; batch < batchCount; batch++)
        {
            int count = batch == batchCount - 1 ?
                            srcFrom.size() - MAX_ENTRIES_PER_OPERATOR * batch :
                            MAX_ENTRIES_PER_OPERATOR;
            writer.write(count + " beginbfrange\n");
            for (int j = 0; j < count; j++)
            {
                int index = batch * MAX_ENTRIES_PER_OPERATOR + j;
                writer.write('<');
                writer.write(Hex.getChars(srcFrom.get(index).shortValue()));
                writer.write("> ");

                writer.write('<');
                writer.write(Hex.getChars(srcTo.get(index).shortValue()));
                writer.write("> ");

                writer.write('<');
                writer.write(Hex.getCharsUTF16BE(dstString.get(index)));
                writer.write(">\n");
            }
            writeLine(writer, "endbfrange\n");
        }

        // footer
        writeLine(writer, "endcmap");
        writeLine(writer, "CMapName currentdict /CMap defineresource pop");
        writeLine(writer, "end");
        writeLine(writer, "end");

        writer.flush();
    }

    private void writeLine(BufferedWriter writer, String text) throws IOException
    {
        writer.write(text);
        writer.write('\n');
    }

    // allowCIDToUnicodeRange returns true if the CID and Unicode destination string are allowed to follow one another
    // according to the Adobe 1.7 specification as described in Section 5.9, Example 5.16.
    static boolean allowCIDToUnicodeRange(Map.Entry<Integer, String> prev,
            Map.Entry<Integer, String> next)
    {
        if (prev == null || next == null)
        {
            return false;
        }
        return allowCodeRange(prev.getKey(), next.getKey())
                && allowDestinationRange(prev.getValue(), next.getValue());
    }

    // allowCodeRange returns true if the 16-bit values are sequential and differ only in the low-order byte.
    static boolean allowCodeRange(int prev, int next)
    {
        if ((prev + 1) != next)
        {
            return false;
        }
        int prevH = (prev >> 8) & 0xFF;
        int prevL = prev & 0xFF;
        int nextH = (next >> 8) & 0xFF;
        int nextL = next & 0xFF;

        return prevH == nextH && prevL < nextL;
    }

    // allowDestinationRange returns true if the code points represented by the strings are sequential and differ
    // only in the low-order byte.
    static boolean allowDestinationRange(String prev, String next)
    {
        if (prev.isEmpty() || next.isEmpty())
        {
            return false;
        }
        int prevCode = prev.codePointAt(0);
        int nextCode = next.codePointAt(0);

        // Allow the new destination string if:
        // 1. It is sequential with the previous one and differs only in the low-order byte
        // 2. The previous string does not contain any UTF-16 surrogates
        return allowCodeRange(prevCode, nextCode) && prev.codePointCount(0, prev.length()) == 1;
    }
}
