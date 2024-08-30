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
package org.apache.pdfbox.examples.pdmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDCIDFont;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDSoftMask;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

/**
 * This will extract all true type-fonts of a pdf.
 * 
 */
public final class ExtractTTFFonts
{
    private int fontCounter = 1;
    private final Set<COSDictionary> fontSet = new HashSet<>();
    private int currentPage;

    @SuppressWarnings({"squid:S2068"})
    private static final String PASSWORD = "-password";
    private static final String PREFIX = "-prefix";
    private static final String ADDKEY = "-addkey";

    private ExtractTTFFonts()
    {
    }

    /**
     * This is the entry point for the application.
     * 
     * @param args The command-line arguments.
     * 
     * @throws IOException If there is an error decrypting the document.
     */
    public static void main(String[] args) throws IOException
    {
        ExtractTTFFonts extractor = new ExtractTTFFonts();
        extractor.extractFonts(args);
    }

    private void extractFonts(String[] args) throws IOException
    {
        if (args.length < 1 || args.length > 4)
        {
            usage();
        }
        else
        {
            String pdfFile = null;
            @SuppressWarnings({"squid:S2068"})
            String password = "";
            String prefix = null;
            boolean addKey = false;
            for (int i = 0; i < args.length; i++)
            {
                switch (args[i])
                {
                    case PASSWORD:
                        i++;
                        if (i >= args.length)
                        {
                            usage();
                        }
                        password = args[i];
                        break;
                    case PREFIX:
                        i++;
                        if (i >= args.length)
                        {
                            usage();
                        }
                        prefix = args[i];
                        break;
                    case ADDKEY:
                        addKey = true;
                        break;
                    default:
                        if (pdfFile == null)
                        {
                            pdfFile = args[i];
                        }
                        break;
                }
            }
            if (pdfFile == null)
            {
                usage();
            }
            else
            {
                if (prefix == null && pdfFile.length() > 4)
                {
                    prefix = pdfFile.substring(0, pdfFile.length() - 4);
                }
                try (PDDocument document = Loader.loadPDF(new File(pdfFile), password))
                {
                    PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
                    if (acroForm != null)
                    {
                        processResources(acroForm.getDefaultResources(), prefix, addKey);
                    }
                    PDPageTree pageTree = document.getPages();
                    for (PDPage page : pageTree)
                    {
                        currentPage = pageTree.indexOf(page) + 1;
                        // extract all fonts which are part of the page resources
                        processResources(page.getResources(), prefix, addKey);
                        
                        for (PDAnnotation ann : page.getAnnotations())
                        {
                            PDAppearanceStream nas = ann.getNormalAppearanceStream();
                            if (nas != null)
                            {
                                processResources(nas.getResources(), prefix, addKey);
                            }
                            PDAppearanceDictionary appearance = ann.getAppearance();
                            if (appearance != null)
                            {
                                PDAppearanceEntry nae = appearance.getNormalAppearance();
                                if (nae != null && nae.isStream())
                                {
                                    nas = nae.getAppearanceStream();
                                    processResources(nas.getResources(), prefix, addKey);
                                }
                                else if (nae != null && nae.isSubDictionary())
                                {
                                    Map<COSName, PDAppearanceStream> subDic = nae.getSubDictionary();
                                    for (PDAppearanceStream as : subDic.values())
                                    {
                                        processResources(as.getResources(), prefix, addKey);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void processResources(PDResources resources, String prefix, boolean addKey) throws IOException
    {
        if (resources == null)
        {
            return;
        }

        processResourcesFonts(resources, addKey, prefix);
        processNestedResources(resources, prefix, addKey);
    }

    private void processResourcesFonts(PDResources resources, boolean addKey, String prefix) throws IOException
    {
        for (COSName key : resources.getFontNames())
        {
            PDFont font = resources.getFont(key);
            if (font == null)
            {
                continue;
            }
            System.out.println((font.getName() == null ? "(null)" : font.getName()) +
                    " on page " + currentPage);
            if (fontSet.contains(font.getCOSObject()))
            {
                continue;
            }
            fontSet.add(font.getCOSObject());
            // write the font
            if (font instanceof PDTrueTypeFont)
            {
                String name;
                if (addKey)
                {
                    name = getUniqueFileName(prefix + "_" + key, "ttf");
                }
                else
                {
                    name = getUniqueFileName(prefix, "ttf");
                }
                writeFont(font.getFontDescriptor(), name);
            }
            else if (font instanceof PDType0Font)
            {
                PDCIDFont descendantFont = ((PDType0Font) font).getDescendantFont();
                if (descendantFont instanceof PDCIDFontType2)
                {
                    String name;
                    if (addKey)
                    {
                        name = getUniqueFileName(prefix + "_" + key, "ttf");
                    }
                    else
                    {
                        name = getUniqueFileName(prefix, "ttf");
                    }
                    writeFont(descendantFont.getFontDescriptor(), name);
                }
            }
        }
    }

    private void processNestedResources(PDResources resources, String prefix, boolean addKey)
            throws IOException
    {
        for (COSName name : resources.getXObjectNames())
        {
            PDXObject xobject = resources.getXObject(name);
            if (xobject instanceof PDFormXObject)
            {
                PDFormXObject xObjectForm = (PDFormXObject) xobject;
                processResources(xObjectForm.getResources(), prefix, addKey);
            }
        }

        for (COSName name : resources.getPatternNames())
        {
            PDAbstractPattern pattern = resources.getPattern(name);
            if (pattern instanceof PDTilingPattern)
            {
                PDTilingPattern tilingPattern = (PDTilingPattern) pattern;
                processResources(tilingPattern.getResources(), prefix, addKey);
            }
        }

        for (COSName name : resources.getExtGStateNames())
        {
            PDExtendedGraphicsState extGState = resources.getExtGState(name);
            PDSoftMask softMask = extGState.getSoftMask();
            if (softMask != null)
            {
                PDTransparencyGroup group = softMask.getGroup();
                if (group != null)
                {
                    processResources(group.getResources(), prefix, addKey);
                }
            }
        }
    }

    private void writeFont(PDFontDescriptor fd, String name) throws IOException
    {
        if (fd != null)
        {
            PDStream ff2Stream = fd.getFontFile2();
            if (ff2Stream != null)
            {
                System.out.println("Writing font: " + name);
                try (OutputStream os = new FileOutputStream(name + ".ttf");
                     InputStream is = ff2Stream.createInputStream())
                {
                    is.transferTo(os);
                }
            }
        }
    }

    private String getUniqueFileName(String prefix, String suffix)
    {
        String uniqueName = null;
        File f = null;
        while (f == null || f.exists())
        {
            uniqueName = prefix + "-" + fontCounter;
            f = new File(uniqueName + "." + suffix);
            fontCounter++;
        }
        return uniqueName;
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println("Usage: java " + ExtractTTFFonts.class.getName() + " [OPTIONS] <PDF file>\n"
                + "  -password  <password>        Password to decrypt document\n"
                + "  -prefix  <font-prefix>       Font prefix(default to pdf name)\n"
                + "  -addkey                      add the internal font key to the file name\n"
                + "  <PDF file>                   The PDF document to use\n");
        System.exit(1);
    }

}
