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
package org.apache.fontbox.ttf;

import java.io.IOException;

/**
 * A true type font file parser.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.2 $
 */
public class TTFParser extends AbstractTTFParser
{
    public TTFParser()
    {
        super(false);
    }

    public TTFParser(boolean isEmbedded)
    {
        super(isEmbedded);
    }

    /**
     * A simple command line program to test parsing of a TTF file. <br/>
     * usage: java org.pdfbox.ttf.TTFParser &lt;ttf-file&gt;
     * 
     * @param args The command line arguments.
     * 
     * @throws IOException If there is an error while parsing the font file.
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.err.println("usage: java org.pdfbox.ttf.TTFParser <ttf-file>");
            System.exit(-1);
        }
        TTFParser parser = new TTFParser();
        TrueTypeFont font = parser.parseTTF(args[0]);
        System.out.println("Font:" + font);
    }

    /**
     * {@inheritDoc}
     */
    protected void parseTables(TrueTypeFont font, TTFDataStream raf) throws IOException
    {
        super.parseTables(font, raf);

        // check others mandatory tables
        if (!isEmbedded && font.getCMAP() == null)
        {
            throw new IOException("cmap is mandatory");
        }
    }

}
