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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.pdfbox.util.Charsets;
import org.apache.pdfbox.util.Hex;

/**
 * Writes ToUnicode Mapping Files.
 *
 * @author John Hewson
 */
final class ToUnicodeWriter
{
    private final Map<Integer, String> cidToUnicode = new TreeMap<Integer, String>();
    private int wMode;

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
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, Charsets.US_ASCII));

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
        List<Integer> srcFrom = new ArrayList<Integer>();
        List<Integer> srcTo = new ArrayList<Integer>();
        List<String> dstString = new ArrayList<String>();

        int srcPrev = -1;
        String dstPrev = null;

        int srcCode1 = -1;

        for (Map.Entry<Integer, String> entry : cidToUnicode.entrySet())
        {
            int cid = entry.getKey();
            String text = entry.getValue();

            if (cid == srcPrev + 1 &&                                 // CID must be last CID + 1
                dstPrev.codePointCount(0, dstPrev.length()) == 1 &&   // no UTF-16 surrogates
                text.codePointAt(0) == dstPrev.codePointAt(0) + 1 &&  // dstString must be prev + 1
                dstPrev.codePointAt(0) + 1 <= 255 - (cid - srcCode1)) // increment last byte only
            {
                // extend range
                srcTo.set(srcTo.size() - 1, cid);
            }
            else
            {
                // begin range
                srcCode1 = cid;
                srcFrom.add(cid);
                srcTo.add(cid);
                dstString.add(text);
            }
            srcPrev = cid;
            dstPrev = text;
        }

        // limit of 100 entries per operator
        int batchCount = (int)Math.ceil(srcFrom.size() / 100.0);
        for (int batch = 0; batch < batchCount; batch++)
        {
            int count = batch == batchCount - 1 ? srcFrom.size() % 100 : 100;
            writer.write(count + " beginbfrange\n");
            for (int j = 0; j < count; j++)
            {
                int index = batch * 100 + j;
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
}
