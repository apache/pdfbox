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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.apache.fontbox.ttf.TrueTypeCollection.TrueTypeFontProcessor;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.type1.Type1Font;
import org.apache.fontbox.util.autodetect.FontFileFinder;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.util.Charsets;

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

    private static class FSFontInfo extends FontInfo
    {
        private final String postScriptName;
        private final FontFormat format;
        private final CIDSystemInfo cidSystemInfo;
        private final int usWeightClass;
        private final int sFamilyClass;
        private final int ulCodePageRange1;
        private final int ulCodePageRange2;
        private final int macStyle;
        private final PDPanoseClassification panose;
        private final File file;
        private transient FileSystemFontProvider parent;

        private FSFontInfo(File file, FontFormat format, String postScriptName,
                           CIDSystemInfo cidSystemInfo, int usWeightClass, int sFamilyClass,
                           int ulCodePageRange1, int ulCodePageRange2, int macStyle, byte[] panose,
                           FileSystemFontProvider parent)
        {
            this.file = file;
            this.format = format;
            this.postScriptName = postScriptName;
            this.cidSystemInfo = cidSystemInfo;
            this.usWeightClass = usWeightClass;
            this.sFamilyClass = sFamilyClass;
            this.ulCodePageRange1 = ulCodePageRange1;
            this.ulCodePageRange2 = ulCodePageRange2;
            this.macStyle = macStyle;
            this.panose = panose != null ? new PDPanoseClassification(panose) : null;
            this.parent = parent;
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
        public CIDSystemInfo getCIDSystemInfo()
        {
            return cidSystemInfo;
        }

        @Override
        public FontBoxFont getFont()
        {
            FontBoxFont cached = parent.cache.getFont(this);
            if (cached != null)
            {
                return cached;
            }
            else
            {
                FontBoxFont font;
                switch (format)
                {
                    case PFB: font = parent.getType1Font(postScriptName, file); break;
                    case TTF: font = parent.getTrueTypeFont(postScriptName, file); break;
                    case OTF: font = parent.getOTFFont(postScriptName, file); break;
                    default: throw new RuntimeException("can't happen");
                }
                parent.cache.addFont(this, font);
                return font;
            }
        }

        @Override
        public int getFamilyClass()
        {
            return sFamilyClass;
        }

        @Override
        public int getWeightClass()
        {
            return usWeightClass;
        }

        @Override
        public int getCodePageRange1()
        {
            return ulCodePageRange1;
        }

        @Override
        public int getCodePageRange2()
        {
            return ulCodePageRange2;
        }

        @Override
        public int getMacStyle()
        {
            return macStyle;
        }

        @Override
        public PDPanoseClassification getPanose()
        {
            return panose;
        }

        @Override
        public String toString()
        {
            return super.toString() + " " + file;
        }
    }

    /**
     * Represents ignored fonts (i.e. bitmap fonts).
     */
    private static final class FSIgnored extends FSFontInfo
    {
        private FSIgnored(File file, FontFormat format, String postScriptName)
        {
            super(file, format, postScriptName, null, 0, 0, 0, 0, 0, null, null);
        }
    }

    /**
     * Constructor.
     */
    FileSystemFontProvider(FontCache cache)
    {
        this.cache = cache;
        try
        {
            if (LOG.isTraceEnabled())
            {
                LOG.trace("Will search the local system for fonts");
            }

            // scan the local system for font files
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

            // load cached FontInfo objects
            List<FSFontInfo> cachedInfos = loadDiskCache(files);
            if (cachedInfos != null && cachedInfos.size() > 0)
            {
                fontInfoList.addAll(cachedInfos);
            }
            else
            {
                LOG.warn("Building on-disk font cache, this may take a while");
                scanFonts(files);
                saveDiskCache();
                LOG.warn("Finished building on-disk font cache, found " +
                        fontInfoList.size() + " fonts");
            }
        }
        catch (AccessControlException e)
        {
            LOG.error("Error accessing the file system", e);
        }
    }
    
    private void scanFonts(List<File> files)
    {
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

    private File getDiskCacheFile()
    {
        String path = System.getProperty("pdfbox.fontcache");
        if (path == null)
        {
            path = System.getProperty("user.home");
            if (path == null)
            {
                path = System.getProperty("java.io.tmpdir");
            }
        }
        return new File(path, ".pdfbox.cache");
    }

    /**
     * Saves the font metadata cache to disk.
     */
    private void saveDiskCache()
    {
        BufferedWriter writer = null;
        try
        {
            File file = getDiskCacheFile();
            try
            {
                writer = new BufferedWriter(new FileWriter(file));
            }
            catch (SecurityException e)
            {
                return;
            }

            for (FSFontInfo fontInfo : fontInfoList)
            {
                writer.write(fontInfo.postScriptName.trim());
                writer.write("|");
                writer.write(fontInfo.format.toString());
                writer.write("|");
                if (fontInfo.cidSystemInfo != null)
                {
                    writer.write(fontInfo.cidSystemInfo.getRegistry() + '-' +
                                 fontInfo.cidSystemInfo.getOrdering() + '-' +
                                 fontInfo.cidSystemInfo.getSupplement());
                }
                writer.write("|");
                if (fontInfo.usWeightClass > -1)
                {
                    writer.write(Integer.toHexString(fontInfo.usWeightClass));
                }
                writer.write("|");
                if (fontInfo.sFamilyClass > -1)
                {
                    writer.write(Integer.toHexString(fontInfo.sFamilyClass));
                }
                writer.write("|");
                writer.write(Integer.toHexString(fontInfo.ulCodePageRange1));
                writer.write("|");
                writer.write(Integer.toHexString(fontInfo.ulCodePageRange2));
                writer.write("|");
                if (fontInfo.macStyle > -1)
                {
                    writer.write(Integer.toHexString(fontInfo.macStyle));
                }
                writer.write("|");
                if (fontInfo.panose != null)
                {
                    byte[] bytes = fontInfo.panose.getBytes();
                    for (int i = 0; i < 10; i ++)
                    {
                        String str = Integer.toHexString(bytes[i]);
                        if (str.length() == 1)
                        {
                            writer.write('0');
                        }
                        writer.write(str);
                    }
                }
                writer.write("|");
                writer.write(fontInfo.file.getAbsolutePath());
                writer.newLine();
            }
        }
        catch (IOException e)
        {
            LOG.error("Could not write to font cache", e);
        }
        finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * Loads the font metadata cache from disk.
     */
    private List<FSFontInfo> loadDiskCache(List<File> files)
    {
        Set<String> pending = new HashSet<String>();
        for (File file : files)
        {
            pending.add(file.getAbsolutePath());
        }
        
        List<FSFontInfo> results = new ArrayList<FSFontInfo>();
        File file = getDiskCacheFile();
        boolean fileExists = false;
        try
        {
            fileExists = file.exists();
        }
        catch (SecurityException e)
        {
        }
        if (fileExists)
        {
            BufferedReader reader = null;
            try
            {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    String[] parts = line.split("\\|", 10);
                    if (parts.length < 10)
                    {
                        LOG.error("Incorrect line '" + line + "' in font disk cache is skipped");
                        continue;
                    }

                    String postScriptName;
                    FontFormat format;
                    CIDSystemInfo cidSystemInfo = null;
                    int usWeightClass = -1;
                    int sFamilyClass = -1;
                    int ulCodePageRange1;
                    int ulCodePageRange2;
                    int macStyle = -1;
                    byte[] panose = null;
                    File fontFile;
                    
                    postScriptName = parts[0];
                    format = FontFormat.valueOf(parts[1]);
                    if (parts[2].length() > 0)
                    {
                        String[] ros = parts[2].split("-");
                        cidSystemInfo = new CIDSystemInfo(ros[0], ros[1], Integer.parseInt(ros[2]));
                    }
                    if (parts[3].length() > 0)
                    {
                        usWeightClass = (int)Long.parseLong(parts[3], 16);
                    }
                    if (parts[4].length() > 0)
                    {
                        sFamilyClass = (int)Long.parseLong(parts[4], 16);
                    }
                    ulCodePageRange1 = (int)Long.parseLong(parts[5], 16);
                    ulCodePageRange2 = (int)Long.parseLong(parts[6], 16);
                    if (parts[7].length() > 0)
                    {
                        macStyle = (int)Long.parseLong(parts[7], 16);
                    }
                    if (parts[8].length() > 0)
                    {
                        panose = new byte[10];
                        for (int i = 0; i < 10; i ++)
                        {
                            String str = parts[8].substring(i * 2, i * 2 + 2);
                            int b = Integer.parseInt(str, 16);
                            panose[i] = (byte)(b & 0xff);
                        }
                    }
                    fontFile = new File(parts[9]);
                    
                    FSFontInfo info = new FSFontInfo(fontFile, format, postScriptName,
                            cidSystemInfo, usWeightClass, sFamilyClass, ulCodePageRange1,
                            ulCodePageRange2, macStyle, panose, this);
                    results.add(info);
                    pending.remove(fontFile.getAbsolutePath());
                }
            }
            catch (IOException e)
            {
                LOG.error("Error loading font cache, will be re-built", e);
                return null;
            }
            finally
            {
                IOUtils.closeQuietly(reader);
            }
        }
        
        if (pending.size() > 0)
        {
            // re-build the entire cache if we encounter un-cached fonts (could be optimised)
            LOG.warn("New fonts found, font cache will be re-built");
            return null;
        }
        
        return results;
    }

    /**
     * Adds a TTC or OTC to the file cache. To reduce memory, the parsed font is not cached.
     */
    private void addTrueTypeCollection(final File ttcFile) throws IOException
    {
        TrueTypeCollection ttc = null;
        try
        {
            ttc = new TrueTypeCollection(ttcFile);
            ttc.processAllFonts(new TrueTypeFontProcessor()
            {
                @Override
                public void process(TrueTypeFont ttf) throws IOException
                {
                    addTrueTypeFontImpl(ttf, ttcFile);
                }
            });
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
            // read PostScript name, if any
            if (ttf.getName() != null && ttf.getName().contains("|"))
            {
                fontInfoList.add(new FSIgnored(file, FontFormat.TTF, "*skippipeinname*"));
                LOG.warn("Skipping font with '|' in name " + ttf.getName() + " in file " + file);
            }
            else if (ttf.getName() != null)
            {
                // ignore bitmap fonts
                if (ttf.getHeader() == null)
                {
                    fontInfoList.add(new FSIgnored(file, FontFormat.TTF, ttf.getName()));
                    return;
                }
                int macStyle = ttf.getHeader().getMacStyle();

                int sFamilyClass = -1;
                int usWeightClass = -1;
                int ulCodePageRange1 = 0;
                int ulCodePageRange2 = 0;
                byte[] panose = null;
                // Apple's AAT fonts don't have an OS/2 table
                if (ttf.getOS2Windows() != null)
                {
                    sFamilyClass = ttf.getOS2Windows().getFamilyClass();
                    usWeightClass = ttf.getOS2Windows().getWeightClass();
                    ulCodePageRange1 = (int)ttf.getOS2Windows().getCodePageRange1();
                    ulCodePageRange2 = (int)ttf.getOS2Windows().getCodePageRange2();
                    panose = ttf.getOS2Windows().getPanose();
                }

                String format;
                if (ttf instanceof OpenTypeFont && ((OpenTypeFont)ttf).isPostScript())
                {
                    format = "OTF";
                    CFFFont cff = ((OpenTypeFont)ttf).getCFF().getFont();
                    CIDSystemInfo ros = null;
                    if (cff instanceof CFFCIDFont)
                    {
                        CFFCIDFont cidFont = (CFFCIDFont)cff;
                        String registry = cidFont.getRegistry();
                        String ordering = cidFont.getOrdering();
                        int supplement = cidFont.getSupplement();
                        ros = new CIDSystemInfo(registry, ordering, supplement);
                    }
                    fontInfoList.add(new FSFontInfo(file, FontFormat.OTF, ttf.getName(), ros,
                            usWeightClass, sFamilyClass, ulCodePageRange1, ulCodePageRange2,
                            macStyle, panose, this));
                }
                else
                {
                    CIDSystemInfo ros = null;
                    if (ttf.getTableMap().containsKey("gcid"))
                    {
                        // Apple's AAT fonts have a "gcid" table with CID info
                        byte[] bytes = ttf.getTableBytes(ttf.getTableMap().get("gcid"));
                        String reg = new String(bytes, 10, 64, Charsets.US_ASCII);
                        String registryName = reg.substring(0, reg.indexOf('\0'));
                        String ord = new String(bytes, 76, 64, Charsets.US_ASCII);
                        String orderName = ord.substring(0, ord.indexOf('\0'));
                        int supplementVersion = bytes[140] << 8 & bytes[141];
                        ros = new CIDSystemInfo(registryName, orderName, supplementVersion);
                    }
                    
                    format = "TTF";
                    fontInfoList.add(new FSFontInfo(file, FontFormat.TTF, ttf.getName(), ros,
                            usWeightClass, sFamilyClass, ulCodePageRange1, ulCodePageRange2,
                            macStyle, panose, this));
                }

                if (LOG.isTraceEnabled())
                {
                    NamingTable name = ttf.getNaming();
                    if (name != null)
                    {
                        LOG.trace(format +": '" + name.getPostScriptName() + "' / '" +
                                  name.getFontFamily() + "' / '" +
                                  name.getFontSubFamily() + "'");
                    }
                }
            }
            else
            {
                fontInfoList.add(new FSIgnored(file, FontFormat.TTF, "*skipnoname*"));
                LOG.warn("Missing 'name' entry for PostScript name in font " + file);
            }
        }
        catch (IOException e)
        {
            fontInfoList.add(new FSIgnored(file, FontFormat.TTF, "*skipexception*"));
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
            if (type1.getName() != null && type1.getName().contains("|"))
            {
                fontInfoList.add(new FSIgnored(pfbFile, FontFormat.PFB, "*skippipeinname*"));
                LOG.warn("Skipping font with '|' in name " + type1.getName() + " in file " + pfbFile);
                return;
            }
            fontInfoList.add(new FSFontInfo(pfbFile, FontFormat.PFB, type1.getName(),
                                            null, -1, -1, 0, 0, -1, null, this));

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
            TrueTypeFont ttf = ttc.getFontByName(postScriptName);
            if (ttf == null)
            {
                ttc.close();
                throw new IOException("Font " + postScriptName + " not found in " + file);
            }
            return ttf;
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
