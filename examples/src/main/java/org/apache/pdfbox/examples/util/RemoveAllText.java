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
package org.apache.pdfbox.examples.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;

/**
 * This is an example on how to remove all text from PDF document.
 *
 * @author Ben Litchfield
 */
public final class RemoveAllText
{
    /**
     * Default constructor.
     */
    private RemoveAllText()
    {
        // example class should not be instantiated
    }

    /**
     * This will remove all text from a PDF document.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main(final String[] args) throws IOException
    {
        if (args.length != 2)
        {
            usage();
        }
        else
        {
            try (PDDocument document = Loader.loadPDF(new File(args[0])))
            {
                if (document.isEncrypted())
                {
                    System.err.println(
                            "Error: Encrypted documents are not supported for this example.");
                    System.exit(1);
                }
                for (final PDPage page : document.getPages())
                {
                    final List<Object> newTokens = createTokensWithoutText(page);
                    final PDStream newContents = new PDStream(document);
                    writeTokensToStream(newContents, newTokens);
                    page.setContents(newContents);
                    processResources(page.getResources());
                }
                document.save(args[1]);
            }
        }
    }

    private static void processResources(final PDResources resources) throws IOException
    {
        for (final COSName name : resources.getXObjectNames())
        {
            final PDXObject xobject = resources.getXObject(name);
            if (xobject instanceof PDFormXObject)
            {
                final PDFormXObject formXObject = (PDFormXObject) xobject;
                writeTokensToStream(formXObject.getContentStream(),
                        createTokensWithoutText(formXObject));
                processResources(formXObject.getResources());
            }
        }
        for (final COSName name : resources.getPatternNames())
        {
            final PDAbstractPattern pattern = resources.getPattern(name);
            if (pattern instanceof PDTilingPattern)
            {
                final PDTilingPattern tilingPattern = (PDTilingPattern) pattern;
                writeTokensToStream(tilingPattern.getContentStream(),
                        createTokensWithoutText(tilingPattern));
                processResources(tilingPattern.getResources());
            }
        }
    }

    private static void writeTokensToStream(final PDStream newContents, final List<Object> newTokens) throws IOException
    {
        try (OutputStream out = newContents.createOutputStream(COSName.FLATE_DECODE))
        {
            final ContentStreamWriter writer = new ContentStreamWriter(out);
            writer.writeTokens(newTokens);
        }
    }

    private static List<Object> createTokensWithoutText(final PDContentStream contentStream) throws IOException
    {
        final PDFStreamParser parser = new PDFStreamParser(contentStream);
        Object token = parser.parseNextToken();
        final List<Object> newTokens = new ArrayList<>();
        while (token != null)
        {
            if (token instanceof Operator)
            {
                final Operator op = (Operator) token;
                final String opName = op.getName();
                if (OperatorName.SHOW_TEXT_ADJUSTED.equals(opName)
                        || OperatorName.SHOW_TEXT.equals(opName)
                        || OperatorName.SHOW_TEXT_LINE.equals(opName))
                {
                    // remove the argument to this operator
                    newTokens.remove(newTokens.size() - 1);

                    token = parser.parseNextToken();
                    continue;
                }
                else if (OperatorName.SHOW_TEXT_LINE_AND_SPACE.equals(opName))
                {
                    // remove the 3 arguments to this operator
                    newTokens.remove(newTokens.size() - 1);
                    newTokens.remove(newTokens.size() - 1);
                    newTokens.remove(newTokens.size() - 1);

                    token = parser.parseNextToken();
                    continue;
                }
            }
            newTokens.add(token);
            token = parser.parseNextToken();
        }
        return newTokens;
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println(
                "Usage: java " + RemoveAllText.class.getName() + " <input-pdf> <output-pdf>");
    }

}
