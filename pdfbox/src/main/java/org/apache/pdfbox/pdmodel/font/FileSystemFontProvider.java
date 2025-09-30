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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.ttf.FontHeaders;
import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.OpenTypeFont;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.type1.Type1Font;
import org.apache.fontbox.util.autodetect.FontFileFinder;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

/**
 * A FontProvider which searches for fonts on the local filesystem.
 *
 * @author John Hewson
 */
final class FileSystemFontProvider extends FontProvider
{
    private static final Log LOG = LogFactory.getLog(FileSystemFontProvider.class);

    /**
     * This option changes publicly visible behaviour: ".pdfbox.cache" file will have hash="-" for all files.
     * After implementing {@link FontHeaders}, parsing font headers is faster than checksumming anyway.
     */
    private static final boolean SKIP_CHECKSUMS = "true".equals(System.getProperty("pdfbox.fontcache.skipchecksums"));
    private static final String CHECKSUM_PLACEHOLDER = "-";
    
    private final List<FSFontInfo> fontInfoList = new ArrayList<>();
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
        private final FileSystemFontProvider parent;
        private final String hash;
        private final long lastModified;

        private FSFontInfo(File file, FontFormat format, String postScriptName,
                           CIDSystemInfo cidSystemInfo, int usWeightClass, int sFamilyClass,
                           int ulCodePageRange1, int ulCodePageRange2, int macStyle, byte[] panose,
                           FileSystemFontProvider parent, String hash, long lastModified)
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
            this.panose = panose != null && panose.length >= PDPanoseClassification.LENGTH ?
                    new PDPanoseClassification(panose) : null;
            this.parent = parent;
            this.hash = hash;
            this.lastModified = lastModified;
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

        /**
         * {@inheritDoc}
         * <p>
         * The method returns null if there was an error opening the font.
         * 
         */
        @Override
        public synchronized FontBoxFont getFont()
        {
            // synchronized to avoid race condition on cache access,
            // which could result in an unreferenced but open font
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
                    case PFB: font = getType1Font(postScriptName, file); break;
                    case TTF: font = getTrueTypeFont(postScriptName, file); break;
                    case OTF: font = getOTFFont(postScriptName, file); break;
                    default: throw new RuntimeException("can't happen");
                }
                if (font != null)
                {
                    parent.cache.addFont(this, font);
                }
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
            return super.toString() + " " + file + " " + hash + " " + lastModified;
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
            catch (IOException e)
            {
                LOG.warn("Could not load font file: " + file, e);
            }
            return null;
        }

        private TrueTypeFont readTrueTypeFont(String postScriptName, File file) throws IOException
        {
            if (file.getName().toLowerCase().endsWith(".ttc"))
            {
                @SuppressWarnings("squid:S2095")
                // ttc not closed here because it is needed later when ttf is accessed,
                // e.g. rendering PDF with non-embedded font which is in ttc file in our font directory
                TrueTypeCollection ttc = new TrueTypeCollection(file);
                TrueTypeFont ttf;
                try
                {
                    ttf = ttc.getFontByName(postScriptName);
                }
                catch (IOException ex)
                {
                    ttc.close();
                    throw ex;
                }
                if (ttf == null)
                {
                    ttc.close();
                    throw new IOException("Font " + postScriptName + " not found in " + file);
                }
                return ttf;
            }
            else
            {
                TTFParser ttfParser = new TTFParser(false);
                return ttfParser.parse(new RandomAccessReadBufferedFile(file));
            }
        }

        private OpenTypeFont getOTFFont(String postScriptName, File file)
        {
            try
            {
                if (file.getName().toLowerCase().endsWith(".ttc"))
                {
                    @SuppressWarnings("squid:S2095")
                    // ttc not closed here because it is needed later when ttf is accessed,
                    // e.g. rendering PDF with non-embedded font which is in ttc file in our font directory
                    TrueTypeCollection ttc = new TrueTypeCollection(file);
                    TrueTypeFont ttf;
                    try
                    {
                        ttf = ttc.getFontByName(postScriptName);
                    }
                    catch (IOException ex)
                    {
                        LOG.error(ex.getMessage(), ex);
                        ttc.close();
                        return null;
                    }
                    if (ttf == null)
                    {
                        ttc.close();
                        throw new IOException("Font " + postScriptName + " not found in " + file);
                    }
                    return (OpenTypeFont) ttf;
                }

                OTFParser parser = new OTFParser(false);
                OpenTypeFont otf = parser.parse(new RandomAccessReadBufferedFile(file));

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Loaded " + postScriptName + " from " + file);
                }
                return otf;
            }
            catch (IOException e)
            {
                LOG.warn("Could not load font file: " + file, e);
            }
            return null;
        }

        private Type1Font getType1Font(String postScriptName, File file)
        {
            try (InputStream input = new FileInputStream(file))
            {
                Type1Font type1 = Type1Font.createWithPFB(input);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Loaded " + postScriptName + " from " + file);
                }
                return type1;
            }
            catch (IOException e)
            {
                LOG.warn("Could not load font file: " + file, e);
            }
            return null;
        }
    }

    private FSFontInfo createFSIgnored(File file, FontFormat format, String postScriptName)
    {
        String hash;
        try
        {
            hash = SKIP_CHECKSUMS ? CHECKSUM_PLACEHOLDER : computeHash(Files.newInputStream(file.toPath()));
        }
        catch (IOException ex)
        {
            hash = "";
        }
        return new FSFontInfo(file, format, postScriptName, null, 0, 0, 0, 0, 0, null, null, hash, file.lastModified());
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
            FontFileFinder fontFileFinder = new FontFileFinder();
            List<URI> fonts = fontFileFinder.find();
            List<File> files = new ArrayList<>(fonts.size());
            for (URI font : fonts)
            {
                files.add(new File(font));
            }

            if (LOG.isTraceEnabled())
            {
                LOG.trace("Found " + files.size() + " fonts on the local system");
            }

            if (!files.isEmpty())
            {
                // load cached FontInfo objects
                List<FSFontInfo> cachedInfos = loadDiskCache(files);
                if (cachedInfos != null && !cachedInfos.isEmpty())
                {
                    fontInfoList.addAll(cachedInfos);
                }
                else
                {
                    LOG.info("Building on-disk font cache, this may take a while");
                    scanFonts(files);
                    saveDiskCache();
                    LOG.info("Finished building on-disk font cache, found " + fontInfoList.size()
                            + " fonts");
                }
            }
        }
        catch (AccessControlException e)
        {
            LOG.error("Error accessing the file system", e);
        }
    }
    
    private void scanFonts(List<File> files)
    {
        // to force a specific font for debug, add code like this here:
        // files = Collections.singletonList(new File("font filename"))

        for (File file : files)
        {
            String filePath = file.getPath().toLowerCase();
            if (filePath.endsWith(".ttf") || filePath.endsWith(".otf"))
            {
                addTrueTypeFont(file);
            }
            else if (filePath.endsWith(".ttc") || filePath.endsWith(".otc"))
            {
                addTrueTypeCollection(file);
            }
            else if (filePath.endsWith(".pfb"))
            {
                addType1Font(file);
            }
        }
    }

    private File getDiskCacheFile()
    {
        String path = System.getProperty("pdfbox.fontcache");
        if (isBadPath(path))
        {
            path = System.getProperty("user.home");
            if (isBadPath(path))
            {
                path = System.getProperty("java.io.tmpdir");
            }
        }
        return new File(path, ".pdfbox.cache");
    }

    private static boolean isBadPath(String path)
    {
        return path == null || !new File(path).isDirectory() || !new File(path).canWrite();
    }

    /**
     * Saves the font metadata cache to disk.
     */
    private void saveDiskCache()
    {
        try
        {
            File file = getDiskCacheFile();

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))
            {
                for (FSFontInfo fontInfo : fontInfoList)
                {
                    writeFontInfo(writer, fontInfo);
                }
            }
            catch (IOException e)
            {
                LOG.warn("Could not write to font cache", e);
                LOG.warn("Installed fonts information will have to be reloaded for each start");
                LOG.warn("You can assign a directory to the 'pdfbox.fontcache' property");
            }
        }
        catch (SecurityException e)
        {
            LOG.debug("Couldn't create writer for font cache file", e);
        }
    }

    private void writeFontInfo(BufferedWriter writer, FSFontInfo fontInfo) throws IOException
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
        writer.write("|");
        writer.write(fontInfo.hash);
        writer.write("|");
        writer.write(Long.toString(fontInfo.file.lastModified()));
        writer.newLine();
    }

    /**
     * Loads the font metadata cache from disk.
     */
    private List<FSFontInfo> loadDiskCache(List<File> files)
    {
        Set<String> pending = new HashSet<>(files.size());
        for (File file : files)
        {
            pending.add(file.getAbsolutePath());
        }
        
        List<FSFontInfo> results = new ArrayList<>();
        
        // Get the disk cache
        File diskCacheFile = null;
        boolean fileExists = false;
        try
        {
            diskCacheFile = getDiskCacheFile();
            fileExists = diskCacheFile.exists();
        }
        catch (SecurityException e)
        {
            LOG.debug("Error checking for file existence", e);
        }

        if (fileExists)
        {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(diskCacheFile), StandardCharsets.UTF_8)))
            {
                // consequent lines usually share the same font file (e.g. "Courier", "Courier-Bold", "Courier-Oblique").
                // unused if SKIP_CHECKSUMS
                File lastFile = null;
                String lastHash = null;
                //
                String line;
                while ((line = reader.readLine()) != null)
                {
                    String[] parts = line.split("\\|", 12);
                    if (parts.length < 10)
                    {
                        LOG.warn("Incorrect line '" + line + "' in font disk cache is skipped");
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
                    String hash = "";
                    long lastModified = 0;
                    
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
                    if (parts.length >= 12 && !parts[10].isEmpty() && !parts[11].isEmpty())
                    {
                        hash = parts[10];
                        lastModified = Long.parseLong(parts[11]);
                    }
                    if (fontFile.exists())
                    {
                        // if the file exists, find out whether it's the same file.
                        // first check whether time is different and if yes, whether hash is different
                        boolean keep = fontFile.lastModified() == lastModified;
                        if (!keep && !SKIP_CHECKSUMS)
                        {
                            String newHash;
                            if (hash.equals(lastHash) && fontFile.equals(lastFile))
                            {
                                newHash = lastHash; // already computed
                            }
                            else
                            {
                                try
                                {
                                    newHash = computeHash(Files.newInputStream(fontFile.toPath()));
                                    lastFile = fontFile;
                                    lastHash = newHash;
                                }
                                catch (IOException ex)
                                {
                                    LOG.debug("Error reading font file " + fontFile.getAbsolutePath(), ex);
                                    newHash = "<err>";
                                }
                            }
                            if (hash.equals(newHash))
                            {
                                keep = true;
                                lastModified = fontFile.lastModified();
                            }
                        }
                        if (keep)
                        {
                            FSFontInfo info = new FSFontInfo(fontFile, format, postScriptName,
                                    cidSystemInfo, usWeightClass, sFamilyClass, ulCodePageRange1,
                                    ulCodePageRange2, macStyle, panose, this, hash, lastModified);
                            results.add(info);
                        }
                        else
                        {
                            LOG.debug("Font file " + fontFile.getAbsolutePath() + " is different");
                            continue; // don't remove from "pending"
                        }
                    }
                    else
                    {
                        LOG.debug("Font file " + fontFile.getAbsolutePath() + " not found, skipped");
                    }
                    pending.remove(fontFile.getAbsolutePath());
                }
            }
            catch (IOException e)
            {
                LOG.warn("Error loading font cache, will be re-built", e);
                return null;
            }
        }
        
        if (!pending.isEmpty())
        {
            // re-build the entire cache if we encounter un-cached fonts (could be optimised)
            LOG.info(pending.size() + " new font files found, font cache will be re-built");
            return null;
        }
        
        return results;
    }

    /**
     * Adds a TTC or OTC to the file cache. To reduce memory, the parsed font is not cached.
     */
    private void addTrueTypeCollection(final File ttcFile)
    {
        try
        {
            String hash = SKIP_CHECKSUMS ? CHECKSUM_PLACEHOLDER : computeHash(Files.newInputStream(ttcFile.toPath()));
            TrueTypeCollection.processAllFontHeaders(ttcFile,
                    fontHeaders -> addTrueTypeFontImpl(fontHeaders, ttcFile, hash));
        }
        catch (IOException e)
        {
            LOG.warn("Could not load font file: " + ttcFile, e);
            fontInfoList.add(createFSIgnored(ttcFile, FontFormat.TTF, "*skipexception*"));
        }
    }

    /**
     * Adds an OTF or TTF font to the file cache. To reduce memory, the parsed font is not cached.
     */
    private void addTrueTypeFont(File ttfFile)
    {
        FontFormat fontFormat = null;
        try
        {
            TTFParser parser;
            if (ttfFile.getPath().toLowerCase().endsWith(".otf"))
            {
                fontFormat = FontFormat.OTF;
                parser = new OTFParser(false);
            }
            else
            {
                fontFormat = FontFormat.TTF;
                parser = new TTFParser(false);
            }
            FontHeaders headers = parser.parseTableHeaders(new RandomAccessReadBufferedFile(ttfFile));
            addTrueTypeFontImpl(headers, ttfFile,
                    SKIP_CHECKSUMS ? CHECKSUM_PLACEHOLDER : computeHash(Files.newInputStream(ttfFile.toPath())));
        }
        catch (IOException e)
        {
            LOG.warn("Could not load font file: " + ttfFile, e);
            fontInfoList.add(createFSIgnored(ttfFile, fontFormat, "*skipexception*"));
        }
    }

    /**
     * Adds an OTF or TTF font to the file cache. To reduce memory, the parsed font is not cached.
     */
    private void addTrueTypeFontImpl(FontHeaders fontHeaders, File file, String hash)
    {
        final String error = fontHeaders.getError();
        if (error == null)
        {
            // read PostScript name, if any
            final String name = fontHeaders.getName();
            if (name != null && name.contains("|"))
            {
                fontInfoList.add(createFSIgnored(file, FontFormat.TTF, "*skippipeinname*"));
                LOG.warn("Skipping font with '|' in name " + name + " in file " + file);
            }
            else if (name != null)
            {
                // ignore bitmap fonts
                Integer macStyle = fontHeaders.getHeaderMacStyle();
                if (macStyle == null)
                {
                    fontInfoList.add(createFSIgnored(file, FontFormat.TTF, name));
                    return;
                }

                int sFamilyClass = -1;
                int usWeightClass = -1;
                int ulCodePageRange1 = 0;
                int ulCodePageRange2 = 0;
                byte[] panose = null;
                OS2WindowsMetricsTable os2WindowsMetricsTable = fontHeaders.getOS2Windows();
                // Apple's AAT fonts don't have an OS/2 table
                if (os2WindowsMetricsTable != null)
                {
                    sFamilyClass = os2WindowsMetricsTable.getFamilyClass();
                    usWeightClass = os2WindowsMetricsTable.getWeightClass();
                    ulCodePageRange1 = (int) os2WindowsMetricsTable.getCodePageRange1();
                    ulCodePageRange2 = (int) os2WindowsMetricsTable.getCodePageRange2();
                    panose = os2WindowsMetricsTable.getPanose();
                }

                FontFormat format;
                CIDSystemInfo ros = null;
                if (fontHeaders.isOpenTypePostScript())
                {
                    format = FontFormat.OTF;
                    String registry = fontHeaders.getOtfRegistry();
                    String ordering = fontHeaders.getOtfOrdering();
                    if (registry != null || ordering != null)
                    {
                        ros = new CIDSystemInfo(registry, ordering, fontHeaders.getOtfSupplement());
                    }
                }
                else
                {
                    byte[] bytes = fontHeaders.getNonOtfTableGCID142();
                    if (bytes != null)
                    {
                        // Apple's AAT fonts have a "gcid" table with CID info
                        String reg = new String(bytes, 10, 64, StandardCharsets.US_ASCII);
                        String registryName = reg.substring(0, reg.indexOf('\0'));
                        String ord = new String(bytes, 76, 64, StandardCharsets.US_ASCII);
                        String orderName = ord.substring(0, ord.indexOf('\0'));
                        int supplementVersion = bytes[140] << 8 & (bytes[141] & 0xFF);
                        ros = new CIDSystemInfo(registryName, orderName, supplementVersion);
                    }
                    format = FontFormat.TTF;
                }
                fontInfoList.add(new FSFontInfo(file, format, name, ros,
                        usWeightClass, sFamilyClass, ulCodePageRange1, ulCodePageRange2,
                        macStyle, panose, this, hash, file.lastModified()));

                if (LOG.isTraceEnabled())
                {
                    LOG.trace(format.name() +": '" + name + "' / '" +
                              fontHeaders.getFontFamily() + "' / '" +
                              fontHeaders.getFontSubFamily() + "'");
                }
            }
            else
            {
                fontInfoList.add(createFSIgnored(file, FontFormat.TTF, "*skipnoname*"));
                LOG.warn("Missing 'name' entry for PostScript name in font " + file);
            }
        }
        else
        {
            fontInfoList.add(createFSIgnored(file, FontFormat.TTF, "*skipexception*"));
            LOG.warn("Could not load font file '" + file + "': " + error);
        }
    }

    /**
     * Adds a Type 1 font to the file cache. To reduce memory, the parsed font is not cached.
     */
    private void addType1Font(File pfbFile)
    {
        try (InputStream input = new FileInputStream(pfbFile))
        {
            Type1Font type1 = Type1Font.createWithPFB(input);
            if (type1.getName() == null)
            {
                fontInfoList.add(createFSIgnored(pfbFile, FontFormat.PFB, "*skipnoname*"));
                LOG.warn("Missing 'name' entry for PostScript name in font " + pfbFile);
                return;
            }
            if (type1.getName().contains("|"))
            {
                fontInfoList.add(createFSIgnored(pfbFile, FontFormat.PFB, "*skippipeinname*"));
                LOG.warn("Skipping font with '|' in name " + type1.getName() + " in file " + pfbFile);
                return;
            }
            String hash = SKIP_CHECKSUMS ? CHECKSUM_PLACEHOLDER : computeHash(Files.newInputStream(pfbFile.toPath()));
            fontInfoList.add(new FSFontInfo(pfbFile, FontFormat.PFB, type1.getName(),
                                            null, -1, -1, 0, 0, -1, null, this, hash, pfbFile.lastModified()));

            if (LOG.isTraceEnabled())
            {
                LOG.trace("PFB: '" + type1.getName() + "' / '" + type1.getFamilyName() + "' / '" +
                        type1.getWeight() + "'");
            }
        }
        catch (IOException e)
        {
            fontInfoList.add(createFSIgnored(pfbFile, FontFormat.PFB, "*skipexception*"));
            LOG.warn("Could not load font file: " + pfbFile, e);
        }
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

    private static String computeHash(InputStream is) throws IOException
    {
        CRC32 crc = new CRC32();

        try
        {
            byte[] buffer = new byte[4096];
            int readBytes;
            while ((readBytes = is.read(buffer)) != -1)
            {
                crc.update(buffer, 0, readBytes);
            }

            long hash = crc.getValue();
            return Long.toHexString(hash);
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
    }
}
