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
import java.util.Iterator;
import java.util.Map;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;

/**
 * This will extract all true type-fonts of a pdf.
 * 
 */
public class ExtractTTFFonts
{
    private int fontCounter = 1;

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
     * @throws Exception If there is an error decrypting the document.
     */
    public static void main(String[] args) throws Exception
    {
        ExtractTTFFonts extractor = new ExtractTTFFonts();
        extractor.extractFonts(args);
    }

    private void extractFonts(String[] args) throws Exception
    {
        if (args.length < 1 || args.length > 4)
        {
            usage();
        }
        else
        {
            String pdfFile = null;
            String password = "";
            String prefix = null;
            boolean addKey = false;
            for (int i = 0; i < args.length; i++)
            {
                if (args[i].equals(PASSWORD))
                {
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    password = args[i];
                }
                else if (args[i].equals(PREFIX))
                {
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    prefix = args[i];
                }
                else if (args[i].equals(ADDKEY))
                {
                    addKey = true;
                }
                else
                {
                    if (pdfFile == null)
                    {
                        pdfFile = args[i];
                    }
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

                PDDocument document = null;

                try
                {
                    document = PDDocument.load(pdfFile);

                    if (document.isEncrypted())
                    {
                        document.decrypt(password);
                    }
                    Iterator<PDPage> iter = document.getDocumentCatalog().getAllPages().iterator();
                    while (iter.hasNext())
                    {
                        PDPage page = iter.next();
                        PDResources resources = page.getResources();
                        // extract all fonts which are part of the page resources
                        processResources(resources, prefix, addKey);
                    }
                }
                finally
                {
                    if (document != null)
                    {
                        document.close();
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
        Map<String, PDFont> fonts = resources.getFonts();
        if (fonts != null)
        {
            Iterator<String> fontIter = fonts.keySet().iterator();
            while (fontIter.hasNext())
            {
                String key = fontIter.next();
                PDFont font = fonts.get(key);
                // write the font
                if (font instanceof PDTrueTypeFont)
                {
                    String name = null;
                    if (addKey)
                    {
                        name = getUniqueFileName(prefix + "_" + key, "ttf");
                    }
                    else
                    {
                        name = getUniqueFileName(prefix, "ttf");
                    }
                    writeFont(font, name);
                }
                else if (font instanceof PDType0Font)
                {
                    PDFont descendantFont = ((PDType0Font) font).getDescendantFont();
                    if (descendantFont instanceof PDCIDFontType2Font)
                    {
                        String name = null;
                        if (addKey)
                        {
                            name = getUniqueFileName(prefix + "_" + key, "ttf");
                        }
                        else
                        {
                            name = getUniqueFileName(prefix, "ttf");
                        }
                        writeFont(descendantFont, name);
                    }
                }
            }
        }
        Map<String, PDXObject> xobjects = resources.getXObjects();
        if (xobjects != null)
        {
            Iterator<String> xobjectIter = xobjects.keySet().iterator();
            while (xobjectIter.hasNext())
            {
                String key = xobjectIter.next();
                PDXObject xobject = xobjects.get(key);
                if (xobject instanceof PDXObjectForm)
                {
                    PDXObjectForm xObjectForm = (PDXObjectForm) xobject;
                    PDResources formResources = xObjectForm.getResources();
                    processResources(formResources, prefix, addKey);
                }
            }
        }

    }

    private void writeFont(PDFont font, String name) throws IOException
    {
        PDFontDescriptorDictionary fd = (PDFontDescriptorDictionary) font.getFontDescriptor();
        if (fd != null)
        {
            PDStream ff2Stream = fd.getFontFile2();
            if (ff2Stream != null)
            {
                System.out.println("Writing font:" + name);
                FileOutputStream fos = new FileOutputStream(new File(name + ".ttf"));
                IOUtils.copy(ff2Stream.createInputStream(), fos);
                fos.close();
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
        System.err.println("Usage: java org.apache.pdfbox.ExtractTTFFonts [OPTIONS] <PDF file>\n"
                + "  -password  <password>        Password to decrypt document\n"
                + "  -prefix  <font-prefix>       Font prefix(default to pdf name)\n"
                + "  -addkey                      add the internal font key to the file name\n"
                + "  <PDF file>                   The PDF document to use\n");
        System.exit(1);
    }

}
