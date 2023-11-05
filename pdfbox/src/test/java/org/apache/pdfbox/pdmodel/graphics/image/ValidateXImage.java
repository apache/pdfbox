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
package org.apache.pdfbox.pdmodel.graphics.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;

/**
 * Helper class to do some validations for PDImageXObject.
 *
 * @author Tilman Hausherr
 */
public class ValidateXImage
{
    public static void validate(PDImageXObject ximage, int bpc, int width, int height, String format, String colorSpaceName) throws IOException
    {
        // check the dictionary
        assertNotNull(ximage);
        COSStream cosStream = ximage.getCOSObject();
        assertNotNull(cosStream);
        assertEquals(COSName.XOBJECT, cosStream.getItem(COSName.TYPE));
        assertEquals(COSName.IMAGE, cosStream.getItem(COSName.SUBTYPE));
        assertTrue(ximage.getCOSObject().getLength() > 0);
        assertEquals(bpc, ximage.getBitsPerComponent());
        assertEquals(width, ximage.getWidth());
        assertEquals(height, ximage.getHeight());
        assertEquals(format, ximage.getSuffix());
        assertEquals(colorSpaceName, ximage.getColorSpace().getName());

        // check the image
        assertNotNull(ximage.getImage());
        assertEquals(ximage.getWidth(), ximage.getImage().getWidth());
        assertEquals(ximage.getHeight(), ximage.getImage().getHeight());
        WritableRaster rawRaster = ximage.getRawRaster();
        assertNotNull(rawRaster);
        assertEquals(rawRaster.getWidth(), ximage.getWidth());
        assertEquals(rawRaster.getHeight(), ximage.getHeight());
        if (colorSpaceName.equals("ICCBased"))
        {
            BufferedImage rawImage = ximage.getRawImage();
            assertNotNull(rawImage);
            assertEquals(rawImage.getWidth(), ximage.getWidth());
            assertEquals(rawImage.getHeight(), ximage.getHeight());
        }

        boolean canEncode = true;
        boolean writeOk;
        // jdk11+ no longer encodes ARGB jpg
        // https://bugs.openjdk.java.net/browse/JDK-8211748
        if ("jpg".equals(format) &&
            ximage.getImage().getType() == BufferedImage.TYPE_INT_ARGB)
        {
            ImageWriter writer = ImageIO.getImageWritersBySuffix(format).next();
            ImageWriterSpi originatingProvider = writer.getOriginatingProvider();
            canEncode = originatingProvider.canEncodeImage(ximage.getImage());
        }
        if (canEncode)
        {
            writeOk = ImageIO.write(ximage.getImage(), format, OutputStream.nullOutputStream());
            assertTrue(writeOk);
        }
        writeOk = ImageIO.write(ximage.getOpaqueImage(null, 1), format, OutputStream.nullOutputStream());
        assertTrue(writeOk);
    }

    public static int colorCount(BufferedImage bim)
    {
        Set<Integer> colors = new HashSet<>();
        int w = bim.getWidth();
        int h = bim.getHeight();
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                colors.add(bim.getRGB(x, y));
            }
        }
        return colors.size();
    }

    // write image twice (overlapped) in document, close document and re-read PDF
    static void doWritePDF(PDDocument document, PDImageXObject ximage, File testResultsDir, String filename)
            throws IOException
    {
        File pdfFile = new File(testResultsDir, filename);

        // This part isn't really needed because this test doesn't break
        // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
        // if something goes wrong in the future and we want to have a PDF to open.

        PDPage page = new PDPage();
        document.addPage(page);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false))
        {
            contentStream.drawImage(ximage, 150, 300);
            contentStream.drawImage(ximage, 200, 350);
        }
        
        // check that the resource map is up-to-date
        assertEquals(1, count(document.getPage(0).getResources().getXObjectNames()));

        document.save(pdfFile);
        document.close();

        document = Loader.loadPDF(pdfFile);
        assertEquals(1, count(document.getPage(0).getResources().getXObjectNames()));
        new PDFRenderer(document).renderImage(0);
        document.close();
    }

    private static int count(Iterable<COSName> iterable)
    {
        int count = 0;
        for (COSName name : iterable)
        {
            count++;
        }
        return count;
    }

    /**
     * Check whether the images are identical.
     *
     * @param expectedImage
     * @param actualImage
     */
    public static void checkIdent(BufferedImage expectedImage, BufferedImage actualImage)
    {
        String errMsg = "";

        expectedImage = convertToSRGB(expectedImage);
        actualImage = convertToSRGB(actualImage);

        int w = expectedImage.getWidth();
        int h = expectedImage.getHeight();
        assertEquals(w, actualImage.getWidth());
        assertEquals(h, actualImage.getHeight());
        for (int y = 0; y < h; ++y)
        {
            for (int x = 0; x < w; ++x)
            {
                if (expectedImage.getRGB(x, y) != actualImage.getRGB(x, y))
                {
                    errMsg = String.format("(%d,%d) expected: <%08X> but was: <%08X>; ", x, y, expectedImage.getRGB(x, y), actualImage.getRGB(x, y));
                }
                assertEquals(expectedImage.getRGB(x, y), actualImage.getRGB(x, y), errMsg);
            }
        }
    }
    
    public static BufferedImage convertToSRGB(BufferedImage image)
    {
        // The image is already sRGB - we don't need to do anything
        if (image.getColorModel().getColorSpace().isCS_sRGB())
        {
            return image;
        }
        // 16-Bit images need to converted to 8 bit first, to avoid rounding differences
        if (image.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
        {
            final int width = image.getWidth();
            final boolean hasAlpha = image.getColorModel().hasAlpha();

            final DirectColorModel colorModel = new DirectColorModel(
                    image.getColorModel().getColorSpace(), 32, 0xFF, 0xFF00, 0xFF0000, 0xFF000000,
                    false, DataBuffer.TYPE_INT);
            WritableRaster targetRaster = Raster
                    .createPackedRaster(DataBuffer.TYPE_INT, image.getWidth(), image.getHeight(),
                            colorModel.getMasks(), new Point(0, 0));

            BufferedImage image8Bit = new BufferedImage(colorModel, targetRaster, false,
                    new Hashtable<>());

            WritableRaster sourceRaster = image.getRaster();

            final int numShortPixelElements = hasAlpha ? 3 : 4;
            // 3 or 4 short per pixel
            short[] pixelShort = new short[numShortPixelElements * width];
            // Packed RGB
            int[] pixelInt = new int[width];
            for (int y = 0; y < image.getHeight(); y++)
            {
                sourceRaster.getDataElements(0, y, width, 1, pixelShort);
                int ptrShort = 0;
                for (int x = 0; x < width; x++)
                {
                    int r = pixelShort[ptrShort++] & 0xFFFF;
                    int g = pixelShort[ptrShort++] & 0xFFFF;
                    int b = pixelShort[ptrShort++] & 0xFFFF;
                    if (hasAlpha)
                        ptrShort++;

                    // We devide using a float exactly the same way as SampledImageReader
                    // to get from 16 bit to 8 bit sample values
                    int r8bit = convert16To8Bit(r);
                    int g8bit = convert16To8Bit(g);
                    int b8bit = convert16To8Bit(b);
                    int v = r8bit | (g8bit << 8) | (b8bit << 16) | 0xFF000000;
                    pixelInt[x] = v;
                }
                targetRaster.setDataElements(0, y, width, 1, pixelInt);

            }
            image = image8Bit;

        }

        BufferedImage destination = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null);
        return op.filter(image, destination);
    }

    private static int convert16To8Bit(int v)
    {
        float output = v / (float) 0xFFFF;
        return Math.round(output * 0xFF);
    }
}
