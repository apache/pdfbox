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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorSpace;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceRGBColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColorSpace;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceRGBColor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * This is an example on how to get the colors of text. Note that this will not tell the background,
 * and will only work properly if the text is not overwritten later, and only if the text rendering
 * modes are 0, 1 or 2. In the PDF 32000 specification, please read 9.3.6 "Text Rendering Mode" to
 * know more. Mode 0 (FILL) is the default. Mode 1 (STROKE) will make glyphs look "hollow". Mode 2
 * (FILL_STROKE) will make glyphs look "fat".
 *
 * @author Ben Litchfield
 * @author Tilman Hausherr
 */
public class PrintTextColors extends PDFTextStripper
{
    /**
     * Instantiate a new PDFTextStripper object.
     *
     * @throws IOException If there is an error loading the properties.
     */
    public PrintTextColors() throws IOException
    {
        addOperator(new SetStrokingColorSpace());
        addOperator(new SetNonStrokingColorSpace());
        addOperator(new SetStrokingDeviceCMYKColor());
        addOperator(new SetNonStrokingDeviceCMYKColor());
        addOperator(new SetNonStrokingDeviceRGBColor());
        addOperator(new SetStrokingDeviceRGBColor());
        addOperator(new SetNonStrokingDeviceGrayColor());
        addOperator(new SetStrokingDeviceGrayColor());
        addOperator(new SetStrokingColor());
        addOperator(new SetStrokingColorN());
        addOperator(new SetNonStrokingColor());
        addOperator(new SetNonStrokingColorN());
    }

    /**
     * This will print the documents data.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            usage();
        }
        else
        {
            PDDocument document = null;
            try
            {
                document = PDDocument.load(new File(args[0]));

                PDFTextStripper stripper = new PrintTextColors();
                stripper.setSortByPosition(true);
                stripper.setStartPage(0);
                stripper.setEndPage(document.getNumberOfPages());

                Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
                stripper.writeText(document, dummy);
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

    @Override
    protected void processTextPosition(TextPosition text)
    {
        super.processTextPosition(text);

        PDColor strokingColor = getGraphicsState().getStrokingColor();
        PDColor nonStrokingColor = getGraphicsState().getNonStrokingColor();
        String unicode = text.getUnicode();
        RenderingMode renderingMode = getGraphicsState().getTextState().getRenderingMode();
        System.out.println("Unicode:            " + unicode);
        System.out.println("Rendering mode:     " + renderingMode);
        System.out.println("Stroking color:     " + strokingColor);
        System.out.println("Non-Stroking color: " + nonStrokingColor);
        System.out.println("Non-Stroking color: " + nonStrokingColor);
        System.out.println();

        // See the PrintTextLocations for more attributes
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println("Usage: java " + PrintTextColors.class.getName() + " <input-pdf>");
    }
}
