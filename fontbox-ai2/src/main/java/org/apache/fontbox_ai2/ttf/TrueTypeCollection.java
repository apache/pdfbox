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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A TrueType Collection, now more properly known as a "Font Collection" as it may contain either
 * TrueType or OpenType fonts.
 * 
 * @author John Hewson
 */
public class TrueTypeCollection implements Closeable
{
    private final TTFDataStream stream;
    private final List<TrueTypeFont> fonts;

    /**
     * Creates a new TrueTypeCollection from a .ttc file.
     *
     * @param file The TTC file.
     * @throws IOException If the font could not be parsed.
     */
    public TrueTypeCollection(File file) throws IOException
    {
        this(new RAFDataStream(file, "r"));
    }

    /**
     * Creates a new TrueTypeCollection from a .ttc input stream.
     *
     * @param stream A TTC input stream.
     * @throws IOException If the font could not be parsed.
     */
    public TrueTypeCollection(InputStream stream) throws IOException
    {
        this(new MemoryTTFDataStream(stream));
    }

    /**
     * Creates a new TrueTypeCollection from a TTC stream.
     *
     * @param stream The TTF file.
     * @throws IOException If the font could not be parsed.
     */
    TrueTypeCollection(TTFDataStream stream) throws IOException
    {
        this.stream = stream;
        
        // TTC header
        String tag = stream.readTag();
        if (!tag.equals("ttcf"))
        {
            throw new IOException("Missing TTC header");
        }
        float version = stream.read32Fixed();
        int numFonts = (int)stream.readUnsignedInt();
        long[] fontOffsets = new long[numFonts];
        for (int i = 0; i < numFonts; i++)
        {
            fontOffsets[i] = stream.readUnsignedInt();
        }
        if (version >= 2)
        {
            int ulDsigTag = stream.readUnsignedShort();
            int ulDsigLength = stream.readUnsignedShort();
            int ulDsigOffset = stream.readUnsignedShort();
        }
        
        // lazy-load the fonts
        List<TrueTypeFont> fonts = new ArrayList<TrueTypeFont>();
        for (int i = 0; i < numFonts; i++)
        {
            stream.seek(fontOffsets[i]);
            if (stream.readTag().equals("OTTO"))
            {
                stream.seek(fontOffsets[i]);
                OTFParser parser = new OTFParser(false, true);
                OpenTypeFont otf = parser.parse(new TTCDataStream(stream));
                fonts.add(otf);
            }
            else
            {
                stream.seek(fontOffsets[i]);
                TTFParser parser = new TTFParser(false, true);
                TrueTypeFont ttf = parser.parse(new TTCDataStream(stream));
                fonts.add(ttf);
            }
        }
        this.fonts = Collections.unmodifiableList(fonts);
    }

    /**
     * Returns the fonts in the collection, these may be {@link OpenTypeFont} instances.
     */
    public List<TrueTypeFont> getFonts()
    {
        return fonts;
    }

    @Override
    public void close() throws IOException
    {
        stream.close();
    }
}
