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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.cff.CFFCIDFont;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.ttf.NamingTable;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.OpenTypeFont;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.type1.Type1Font;
import org.apache.fontbox.util.autodetect.FontFileFinder;
import org.apache.pdfbox.io.IOUtils;

/**
 * A FontProvider which searches for fonts on the local filesystem.
 *
 * @author John Hewson
 */
final class FileSystemFontProvider extends FontProvider
{
    private static final Log LOG = LogFactory.getLog(FileSystemFontProvider.class);
    
    private final List<FSFontInfo> fontInfoList = new ArrayList<FSFontInfo>();
    private final FontCache cache;

    private class FSFontInfo extends FontInfo
    {
        private final String postScriptName;
        private final FontFormat format;
        private final PDCIDSystemInfo cidSystemInfo;
        private final File file;

        private FSFontInfo(File file, FontFormat format, String postScriptName,
                           PDCIDSystemInfo cidSystemInfo)
        {
            this.file = file;
            this.format = format;
            this.postScriptName = postScriptName;
            this.cidSystemInfo = cidSystemInfo;
        }

        @Override
        public String getPostScriptName()
        {
            return postScriptName;
        }

        @Override
        public FontFormat getFormat()
        {
            return format;
        }

        @Override
        public PDCIDSystemInfo getCIDSystemInfo()
        {
            return cidSystemInfo;
        }

        @Override
        public FontBoxFont getFont()
        {
            FontBoxFont cached = cache.getFont(this);
            if (cached != null)
            {
                return cached;
            }
            else
            {
                FontBoxFont font;
                switch (format)
                {
                    case PFB: font = getType1Font(postScriptName, file); break;
                    case TTF: font = getTrueTypeFont(postScriptName, file); break;
                    case OTF: font = getOTFFont(postScriptName, file); break;
                    default: throw new RuntimeException("can't happen");
                }
                cache.addFont(this, font);
                return font;
            }
        }

        @Override
        public String toString()
        {
            return super.toString() + " " + file;
        }
    }

    /**
     * Constructor.
     */
    FileSystemFontProvider(FontCache cache)
    {
        this.cache = cache;

        if (LOG.isTraceEnabled())
        {
            LOG.trace("Will search the local system for fonts");
        }

        List<File> files = new ArrayList<File>();
        FontFileFinder fontFileFinder = new FontFileFinder();
        List<URI> fonts = fontFileFinder.find();
        for (URI font : fonts)
        {
            files.add(new File(font));
        }

        if (LOG.isTraceEnabled())
        {
            LOG.trace("Found " + files.size() + " fonts on the local system");
        }
        
        // todo: loading all of these fonts is slow, can we cache this?
        for (File file : files)
        {
            try
            {
                if (file.getPath().toLowerCase().endsWith(".ttf") ||
                    file.getPath().toLowerCase().endsWith(".otf"))
                {
                    addTrueTypeFont(file);
                }
                else if (file.getPath().toLowerCase().endsWith(".ttc") ||
                         file.getPath().toLowerCase().endsWith(".otc"))
                {
                    addTrueTypeCollection(file);
                }
                else if (file.getPath().toLowerCase().endsWith(".pfb"))
                {
                    addType1Font(file);
                }
            }
            catch (IOException e)
            {
                LOG.error("Error parsing font " + file.getPath(), e);
            }
        }
    }

    /**
     * Adds a TTC or OTC to the file cache. To reduce memory, the parsed font is not cached.
     */
    private void addTrueTypeCollection(File ttcFile) throws IOException
    {
        TrueTypeCollection ttc = null;
        try
        {
            ttc = new TrueTypeCollection(ttcFile);
            for (TrueTypeFont ttf : ttc.getFonts())
            {
                addTrueTypeFontImpl(ttf, ttcFile);
            }
        }
        catch (NullPointerException e) // TTF parser is buggy
        {
            LOG.error("Could not load font file: " + ttcFile, e);
        }
        catch (IOException e)
        {
            LOG.error("Could not load font file: " + ttcFile, e);
        }
        finally
        {
            if (ttc != null)
            {
                ttc.close();
            }
        }
    }

    /**
     * Adds an OTF or TTF font to the file cache. To reduce memory, the parsed font is not cached.
     */
    private void addTrueTypeFont(File ttfFile) throws IOException
    {
        try
        {
            if (ttfFile.getPath().endsWith(".otf"))
            {
                OTFParser parser = new OTFParser(false, true);
                OpenTypeFont otf = parser.parse(ttfFile);
                addTrueTypeFontImpl(otf, ttfFile);
            }
            else
            {
                TTFParser parser = new TTFParser(false, true);
                TrueTypeFont ttf = parser.parse(ttfFile);
                addTrueTypeFontImpl(ttf, ttfFile);
            }
        }
        catch (NullPointerException e) // TTF parser is buggy
        {
            LOG.error("Could not load font file: " + ttfFile, e);
        }
        catch (IOException e)
        {
            LOG.error("Could not load font file: " + ttfFile, e);
        }
    }

    /**
     * Adds an OTF or TTF font to the file cache. To reduce memory, the parsed font is not cached.
     */
    private void addTrueTypeFontImpl(TrueTypeFont ttf, File file) throws IOException
    {
        try
        {
            // check for 'name' table
            NamingTable nameTable = null;

            // ttf could still be null
            if (ttf != null)
            {
                // todo: this is a performance bottleneck, we don't actually need to read this table
                nameTable = ttf.getNaming();
            }

            if (nameTable == null)
            {
                LOG.warn("Missing 'name' table in font " + file);
            }
            else
            {
                // read PostScript name, if any
                if (ttf.getName() != null)
                {
                    String format;
                    if (ttf instanceof OpenTypeFont && ((OpenTypeFont)ttf).isPostScript())
                    {
                        format = "OTF";
                        CFFFont cff = ((OpenTypeFont)ttf).getCFF().getFont();
                        PDCIDSystemInfo ros = null;
                        if (cff instanceof CFFCIDFont)
                        {
                            CFFCIDFont cidFont = (CFFCIDFont)cff;
                            String registry = cidFont.getRegistry();
                            String ordering = cidFont.getOrdering();
                            int supplement = cidFont.getSupplement();
                            ros = new PDCIDSystemInfo(registry, ordering, supplement);
                        }
                        fontInfoList.add(new FSFontInfo(file, FontFormat.OTF, ttf.getName(), ros));
                    }
                    else
                    {
                        format = "TTF";
                        fontInfoList.add(new FSFontInfo(file, FontFormat.TTF, ttf.getName(), null));
                    }

                    if (LOG.isTraceEnabled())
                    {
                        LOG.trace(format +": '" + ttf.getName() + "' / '" +
                                nameTable.getFontFamily() + "' / '" +
                                nameTable.getFontSubFamily() + "'");
                    }
                }
                else
                {
                    LOG.warn("Missing 'name' entry for PostScript name in font " + file);
                }
            }
        }
        catch (IOException e)
        {
            LOG.error("Could not load font file: " + file, e);
        }
        finally
        {
            if (ttf != null)
            {
                ttf.close();
            }
        }
    }

    /**
     * Adds a Type 1 font to the file cache. To reduce memory, the parsed font is not cached.
     */
    private void addType1Font(File pfbFile) throws IOException
    {
        InputStream input = new FileInputStream(pfbFile);
        try
        {
            Type1Font type1 = Type1Font.createWithPFB(input);
            fontInfoList.add(new FSFontInfo(pfbFile, FontFormat.PFB, type1.getName(), null));

            if (LOG.isTraceEnabled())
            {
                LOG.trace("PFB: '" + type1.getName() + "' / '" + type1.getFamilyName() + "' / '" +
                        type1.getWeight() + "'");
            }
        }
        catch (IOException e)
        {
            LOG.error("Could not load font file: " + pfbFile, e);
        }
        finally
        {
            input.close();
        }
    }

    private TrueTypeFont getTrueTypeFont(String postScriptName, File file)
    {
        try
        {
            TrueTypeFont ttf = readTrueTypeFont(postScriptName, file);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Loaded " + postScriptName + " from " + file);
            }
            return ttf;
        }
        catch (NullPointerException e) // TTF parser is buggy
        {
            LOG.error("Could not load font file: " + file, e);
        }
        catch (IOException e)
        {
            LOG.error("Could not load font file: " + file, e);
        }
        return null;
    }

    private TrueTypeFont readTrueTypeFont(String postScriptName, File file) throws IOException
    {
        if (file.getName().toLowerCase().endsWith(".ttc"))
        {
            TrueTypeCollection ttc = new TrueTypeCollection(file);
            for (TrueTypeFont ttf : ttc.getFonts())
            {
                if (ttf.getName().equals(postScriptName))
                {
                    return ttf;
                }
            }
            throw new IOException("Font " + postScriptName + " not found in " + file);
        }
        else
        {
            TTFParser ttfParser = new TTFParser(false, true);
            return ttfParser.parse(file);
        }
    }

    private OpenTypeFont getOTFFont(String postScriptName, File file)
    {
        try
        {
            // todo JH: we don't yet support loading CFF fonts from OTC collections 
            OTFParser parser = new OTFParser(false, true);
            OpenTypeFont otf = parser.parse(file);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Loaded " + postScriptName + " from " + file);
            }
            return otf;
        }
        catch (IOException e)
        {
            LOG.error("Could not load font file: " + file, e);
        }
        return null;
    }

    private Type1Font getType1Font(String postScriptName, File file)
    {
        InputStream input = null;
        try
        {
            input = new FileInputStream(file);
            Type1Font type1 = Type1Font.createWithPFB(input);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Loaded " + postScriptName + " from " + file);
            }
            return type1;
        }
        catch (IOException e)
        {
            LOG.error("Could not load font file: " + file, e);
        }
        finally
        {
            IOUtils.closeQuietly(input);
        }
        return null;
    }

    @Override
    public String toDebugString()
    {
        StringBuilder sb = new StringBuilder();
        for (FSFontInfo info : fontInfoList)
        {
            sb.append(info.getFormat());
            sb.append(": ");
            sb.append(info.getPostScriptName());
            sb.append(": ");
            sb.append(info.file.getPath());
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public List<? extends FontInfo> getFontInfo()
    {
        return fontInfoList;
    }
}
