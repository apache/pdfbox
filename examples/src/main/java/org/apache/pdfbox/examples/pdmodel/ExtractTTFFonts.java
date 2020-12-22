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

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
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

/**
 * This will extract all true type-fonts of a pdf.
 * 
 */
public final class ExtractTTFFonts
{
    private int fontCounter = 1;

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
    public static void main(final String[] args) throws IOException
    {
        final ExtractTTFFonts extractor = new ExtractTTFFonts();
        extractor.extractFonts(args);
    }

    private void extractFonts(final String[] args) throws IOException
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
                    for (final PDPage page : document.getPages())
                    {
                        final PDResources resources = page.getResources();
                        // extract all fonts which are part of the page resources
                        processResources(resources, prefix, addKey);
                    }
                }
            }
        }
    }

    private void processResources(final PDResources resources, final String prefix, final boolean addKey) throws IOException
    {
        if (resources == null)
        {
            return;
        }

        for (final COSName key : resources.getFontNames())
        {
            final PDFont font = resources.getFont(key);
            // write the font
            if (font instanceof PDTrueTypeFont)
            {
                final String name;
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
                final PDCIDFont descendantFont = ((PDType0Font) font).getDescendantFont();
                if (descendantFont instanceof PDCIDFontType2)
                {
                    final String name;
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

        for (final COSName name : resources.getXObjectNames())
        {
            final PDXObject xobject = resources.getXObject(name);
            if (xobject instanceof PDFormXObject)
            {
                final PDFormXObject xObjectForm = (PDFormXObject) xobject;
                final PDResources formResources = xObjectForm.getResources();
                processResources(formResources, prefix, addKey);
            }
        }

    }

    private void writeFont(final PDFontDescriptor fd, final String name) throws IOException
    {
        if (fd != null)
        {
            final PDStream ff2Stream = fd.getFontFile2();
            if (ff2Stream != null)
            {
                System.out.println("Writing font: " + name);
                try (OutputStream os = new FileOutputStream(new File(name + ".ttf"));
                     final InputStream is = ff2Stream.createInputStream())
                {
                    IOUtils.copy(is, os);
                }
            }
        }
    }

    private String getUniqueFileName(final String prefix, final String suffix)
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
