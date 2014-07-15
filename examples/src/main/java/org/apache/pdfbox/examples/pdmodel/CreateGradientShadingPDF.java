/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType2;

/**
 * This example creates a PDF with type 2 (axial) shading with a type 2
 * (exponential) function.
 *
 * @author Tilman Hausherr
 */
public class CreateGradientShadingPDF
{

    /**
     * This will create the PDF and write the contents to a file.
     *
     * @param file The name of the file to write to.
     *
     * @throws IOException If there is an error writing the data.
     */
    public void create(String file) throws IOException
    {
        PDDocument document = null;
        try
        {
            document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            // shading attributes
            COSDictionary dict = new COSDictionary();
            dict.setInt(COSName.SHADING_TYPE, 2);
            dict.setName(COSName.COLORSPACE, "DeviceRGB");
            COSArray coords = new COSArray();
            coords.add(COSInteger.get(100));
            coords.add(COSInteger.get(400));
            coords.add(COSInteger.get(400));
            coords.add(COSInteger.get(600));
            dict.setItem(COSName.COORDS, coords);

            // function with attributes
            COSDictionary fdict = new COSDictionary();
            fdict.setInt(COSName.FUNCTION_TYPE, 2);
            COSArray domain = new COSArray();
            domain.add(COSInteger.get(0));
            domain.add(COSInteger.get(1));
            COSArray c0 = new COSArray();
            c0.add(COSFloat.get("1"));
            c0.add(COSFloat.get("0"));
            c0.add(COSFloat.get("0"));
            COSArray c1 = new COSArray();
            c1.add(COSFloat.get("0.5"));
            c1.add(COSFloat.get("1"));
            c1.add(COSFloat.get("0.5"));
            fdict.setItem(COSName.DOMAIN, domain);
            fdict.setItem(COSName.C0, c0);
            fdict.setItem(COSName.C1, c1);
            fdict.setInt(COSName.N, 1);
            dict.setItem(COSName.FUNCTION, fdict);

            PDShadingType2 shading = new PDShadingType2(dict);

            // create and add to shading resources
            page.setResources(new PDResources());
            Map<String, PDShading> shadings = new HashMap<String, PDShading>();
            shadings.put("sh1", shading);
            page.getResources().setShadings(shadings);

            // invoke shading from content stream
            PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
            contentStream.appendRawCommands("/sh1 sh");
            contentStream.close();
            
            document.save(file);
            document.close();
        }
        finally
        {
            if (document != null)
            {
                document.close();
            }
        }
    }

    /**
     * This will create a blank document.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error writing the document data.
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            usage();
        }
        else
        {
            CreateGradientShadingPDF creator = new CreateGradientShadingPDF();
            creator.create(args[0]);
        }
    }

    /**
     * This will print the usage of this class.
     */
    private static void usage()
    {
        System.err.println("usage: java org.apache.pdfbox.examples.pdmodel.CreateGradientShadingPDF <outputfile.pdf>");
    }
}
