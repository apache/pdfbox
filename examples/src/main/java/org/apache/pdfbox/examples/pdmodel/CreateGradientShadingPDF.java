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
import java.io.OutputStream;

import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.function.PDFunctionType2;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType2;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType3;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType4;

/**
 * This example creates a PDF with type 2 (axial) and type 3 (radial) shadings with a type 2
 * (exponential) function, and a type 4 (gouraud triangle shading) without function.
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
        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage();
            document.addPage(page);

            // type 2 (exponential) function with attributes
            // can be used by both shadings
            COSDictionary fdict = new COSDictionary();
            fdict.setInt(COSName.FUNCTION_TYPE, 2);
            COSArray domain = new COSArray();
            domain.add(COSInteger.ZERO);
            domain.add(COSInteger.ONE);
            COSArray c0 = new COSArray();
            c0.add(COSInteger.ONE);
            c0.add(COSInteger.ZERO);
            c0.add(COSInteger.ZERO);
            COSArray c1 = new COSArray();
            c1.add(COSNumber.get("0.5"));
            c1.add(COSInteger.ONE);
            c1.add(COSNumber.get("0.5"));
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

            // Gouraud shading
            // See PDF 32000 specification,
            // 8.7.4.5.5 Type 4 Shadings (Free-Form Gouraud-Shaded Triangle Meshes)
            PDShadingType4 gouraudShading = new PDShadingType4(document.getDocument().createCOSStream());
            gouraudShading.setShadingType(PDShading.SHADING_TYPE4);
            // we use multiple of 8, so that no padding is needed
            gouraudShading.setBitsPerFlag(8);
            gouraudShading.setBitsPerCoordinate(16);
            gouraudShading.setBitsPerComponent(8);
            COSArray decodeArray = new COSArray();
            // coordinates x y map 16 bits 0..FFFF to 0..FFFF to make your life easy
            // so no calculation is needed, but you can only use integer coordinates
            // for real numbers, you'll need smaller bounds, e.g. 0xFFFF / 0xA = 0x1999
            // would allow 1 point decimal result coordinate.
            // See in PDF specification: 8.9.5.2 Decode Arrays
            decodeArray.add(COSInteger.ZERO);
            decodeArray.add(COSInteger.get(0xFFFF));
            decodeArray.add(COSInteger.ZERO);
            decodeArray.add(COSInteger.get(0xFFFF));
            // colors r g b map 8 bits from 0..FF to 0..1
            decodeArray.add(COSInteger.ZERO);
            decodeArray.add(COSInteger.ONE);
            decodeArray.add(COSInteger.ZERO);
            decodeArray.add(COSInteger.ONE);
            decodeArray.add(COSInteger.ZERO);
            decodeArray.add(COSInteger.ONE);
            gouraudShading.setDecodeValues(decodeArray);
            gouraudShading.setColorSpace(PDDeviceRGB.INSTANCE);
            
            // Function is not required for type 4 shadings and not really useful, 
            // because if a function would be used, each corner "color" of a triangle would be one value, 
            // which would then transformed into n color components by the function so it is 
            // difficult to get 3 "extremes".

            // fill the vertex stream
            try (OutputStream os = ((COSStream) gouraudShading.getCOSObject()).createOutputStream();
                 MemoryCacheImageOutputStream mcos = new MemoryCacheImageOutputStream(os))
            {
                // Vertex 1, starts with flag1
                // (flags always 0 for vertices of start triangle)
                mcos.writeByte(0);
                // x1 y1 (left corner)
                mcos.writeShort(0);
                mcos.writeShort(0);
                // r1 g1 b1 (red)
                mcos.writeByte(0xFF);
                mcos.writeByte(0);
                mcos.writeByte(0);

                // Vertex 2, starts with flag2
                mcos.writeByte(0);
                // x2 y2 (top corner)
                mcos.writeShort(100);
                mcos.writeShort(100);
                // r2 g2 b2 (green)
                mcos.writeByte(0);
                mcos.writeByte(0xFF);
                mcos.writeByte(0);

                // Vertex 3, starts with flag3
                mcos.writeByte(0);
                // x3 y3 (right corner)
                mcos.writeShort(200);
                mcos.writeShort(0);
                // r3 g3 b3 (blue)
                mcos.writeByte(0);
                mcos.writeByte(0);
                mcos.writeByte(0xFF);
            }

            // invoke shading from content stream
            // compress parameter is set to false so that you can see the stream in a text editor
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false))
            {
                contentStream.shadingFill(axialShading);
                contentStream.shadingFill(radialShading);
                contentStream.shadingFill(gouraudShading);
            }
            
            document.save(file);
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
        System.err.println("usage: java " + CreateGradientShadingPDF.class.getName() + " <outputfile.pdf>");
    }
}
