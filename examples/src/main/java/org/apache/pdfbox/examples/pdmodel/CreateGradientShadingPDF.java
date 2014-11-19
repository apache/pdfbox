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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.function.PDFunctionType2;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType2;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType3;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * This example creates a PDF with type 2 (axial) and 3 (radial) shadings with a
 * type 2 (exponential) function.
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

            // type 2 (exponential) function with attributes
            // can be used by both shadings
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
            PDFunctionType2 func = new PDFunctionType2(fdict);

            // axial shading with attributes
            PDShadingType2 axialShading = new PDShadingType2(new COSDictionary());
            axialShading.setColorSpace(PDDeviceRGB.INSTANCE);
            axialShading.setShadingType(PDShading.SHADING_TYPE2);
            COSArray coords1 = new COSArray();
            coords1.add(COSInteger.get(100));
            coords1.add(COSInteger.get(400));
            coords1.add(COSInteger.get(400));
            coords1.add(COSInteger.get(600));
            axialShading.setCoords(coords1);
            axialShading.setFunction(func);

            // radial shading with attributes
            PDShadingType3 radialShading = new PDShadingType3(new COSDictionary());
            radialShading.setColorSpace(PDDeviceRGB.INSTANCE);
            radialShading.setShadingType(PDShading.SHADING_TYPE3);
            COSArray coords2 = new COSArray();
            coords2.add(COSInteger.get(100));
            coords2.add(COSInteger.get(400));
            coords2.add(COSInteger.get(50)); // radius1
            coords2.add(COSInteger.get(400));
            coords2.add(COSInteger.get(600));
            coords2.add(COSInteger.get(150)); // radius2
            radialShading.setCoords(coords2);
            radialShading.setFunction(func);

            // create resources
            PDResources resources = new PDResources();
            page.setResources(resources);
            
            // add shading to resources

            // use put() if you want a specific name
            resources.put(COSName.getPDFName("shax"), axialShading);
            
            // use add() if you want PDFBox to decide the name for you
            COSName radialShadingName = resources.add(radialShading);

            // invoke shading from content stream
            // the raw command is "/name sh"
            // replace "name" with the name of the shading
            // compress parameter is set to false so that you can see the stream in a text editor
            PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
            contentStream.appendRawCommands("/shax sh\n");
            contentStream.appendRawCommands("/" + radialShadingName.getName() + " sh\n");
            contentStream.close();
            
            document.save(file);
            document.close();
            
            // render the PDF and save it into a PNG file
            document = PDDocument.load(new File(file));
            BufferedImage bim = new PDFRenderer(document).renderImageWithDPI(0, 300);
            ImageIO.write(bim, "png", new File(file + ".png"));
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
