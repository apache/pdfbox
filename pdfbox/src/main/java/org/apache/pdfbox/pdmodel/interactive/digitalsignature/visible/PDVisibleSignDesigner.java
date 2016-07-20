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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible;

import java.awt.image.BufferedImage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Class for visible signature design properties. Setters use param() instead of setParam() to allow
 * chaining.
 *
 * @author Vakhtang Koroghlishvili
 */
public class PDVisibleSignDesigner
{
    private Float imageWidth;
    private Float imageHeight;
    private float xAxis;
    private float yAxis;
    private float pageHeight;
    private float pageWidth;
    private BufferedImage image;
    private String signatureFieldName = "sig";
    private byte[] formatterRectangleParams = { 0, 0, 100, 50 };
    private byte[] AffineTransformParams = { 1, 0, 0, 1, 0, 0 };
    private float imageSizeInPercents;

    /**
     * Constructor.
     *
     * @param filename Path of the PDF file
     * @param imageStream image as a stream
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IOException
     */
    public PDVisibleSignDesigner(String filename, InputStream imageStream, int page)
            throws IOException
    {
        // set visible signature image Input stream
        readImageStream(imageStream);

        // calculate height and width of document page
        calculatePageSizeFromFile(filename, page);
    }

    /**
     * Constructor.
     *
     * @param documentStream Original PDF document as stream
     * @param imageStream Image as a stream
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IOException
     */
    public PDVisibleSignDesigner(InputStream documentStream, InputStream imageStream, int page)
            throws IOException
    {
        // set visible signature image Input stream
        readImageStream(imageStream);

        // calculate height and width of document page
        calculatePageSizeFromStream(documentStream, page);
    }

    /**
     * Constructor.
     *
     * @param document Already created PDDocument of your PDF document.
     * @param imageStream Image as a stream.
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IOException If we can't read, flush, or can't close stream.
     */
    public PDVisibleSignDesigner(PDDocument document, InputStream imageStream, int page) throws IOException
    {
        readImageStream(imageStream);
        calculatePageSize(document, page);
    }

    /**
     * Constructor.
     *
     * @param filename Path of the PDF file
     * @param image
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IOException
     */
    public PDVisibleSignDesigner(String filename, BufferedImage image, int page)
            throws IOException
    {
        // set visible signature image
        setImage(image);

        // calculate height and width of document page
        calculatePageSizeFromFile(filename, page);
    }

    /**
     * Constructor.
     *
     * @param documentStream Original PDF document as stream
     * @param image
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IOException
     */
    public PDVisibleSignDesigner(InputStream documentStream, BufferedImage image, int page)
            throws IOException
    {
        // set visible signature image
        setImage(image);

        // calculate height and width of document page
        calculatePageSizeFromStream(documentStream, page);
    }

    /**
     * Constructor.
     *
     * @param document Already created PDDocument of your PDF document.
     * @param image
     * @param page The 1-based page number for which the page size should be calculated.
     */
    public PDVisibleSignDesigner(PDDocument document, BufferedImage image, int page)
    {
        setImage(image);
        calculatePageSize(document, page);
    }

    private void calculatePageSizeFromFile(String filename, int page) throws IOException
    {
        // create PD document
        PDDocument document = PDDocument.load(new File(filename));

        // calculate height and width of document page
        calculatePageSize(document, page);

        document.close();
    }

    private void calculatePageSizeFromStream(InputStream documentStream, int page) throws IOException
    {
        // create PD document
        PDDocument document = PDDocument.load(documentStream);

        // calculate height and width of document page
        calculatePageSize(document, page);

        document.close();
    }

    /**
     * Each page of document can be different sizes. This method calculates the page size based on
     * the page media box.
     * 
     * @param document
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IllegalArgumentException if the page argument is lower than 0.
     */
    private void calculatePageSize(PDDocument document, int page)
    {
        if (page < 1)
        {
            throw new IllegalArgumentException("First page of pdf is 1, not " + page);
        }

        PDPage firstPage = document.getPage(page - 1);
        PDRectangle mediaBox = firstPage.getMediaBox();
        pageHeight(mediaBox.getHeight());
        pageWidth = mediaBox.getWidth();
        imageSizeInPercents = 100;
    }

    /**
     * Set the image for the signature.
     *
     * @param path Path of the image file.
     * @return Visible Signature Configuration Object
     * @throws IOException
     */
    public PDVisibleSignDesigner signatureImage(String path) throws IOException
    {
        InputStream in = null;
        try
        {
            in = new BufferedInputStream(new FileInputStream(path));
            readImageStream(in);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
        return this;
    }

    /**
     * Zoom signature image with some percent.
     * 
     * @param percent increase image with x percent.
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner zoom(float percent)
    {
        imageHeight += (imageHeight * percent) / 100;
        imageWidth += (imageWidth * percent) / 100;
        return this;
    }

    /**
     *
     * @param x - x coordinate
     * @param y - y coordinate
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner coordinates(float x, float y)
    {
        xAxis(x);
        yAxis(y);
        return this;
    }

    /**
     *
     * @return xAxis - gets x coordinates
     */
    public float getxAxis()
    {
        return xAxis;
    }

    /**
     *
     * @param xAxis  - x coordinate 
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner xAxis(float xAxis)
    {
        this.xAxis = xAxis;
        return this;
    }

    /**
     *
     * @return yAxis
     */
    public float getyAxis()
    {
        return yAxis;
    }

    /**
     *
     * @param yAxis
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner yAxis(float yAxis)
    {
        this.yAxis = yAxis;
        return this;
    }

    /**
     * 
     * @return signature image width
     */
    public float getWidth()
    {
        return imageWidth;
    }

    /**
     * 
     * @param width signature image width
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner width(float width)
    {
        this.imageWidth = width;
        return this;
    }

    /**
     * 
     * @return signature image height
     */
    public float getHeight()
    {
        return imageHeight;
    }

    /**
     * 
     * @param height signature image height
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner height(float height)
    {
        this.imageHeight = height;
        return this;
    }

    /**
     * 
     * @return template height
     */
    protected float getTemplateHeight()
    {
        return getPageHeight();
    }

    /**
     * 
     * @param templateHeight
     * @return Visible Signature Configuration Object
     */
    private PDVisibleSignDesigner pageHeight(float templateHeight)
    {
        this.pageHeight = templateHeight;
        return this;
    }

    /**
     * 
     * @return signature field name
     */
    public String getSignatureFieldName()
    {
        return signatureFieldName;
    }

    /**
     * 
     * @param signatureFieldName
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner signatureFieldName(String signatureFieldName)
    {
        this.signatureFieldName = signatureFieldName;
        return this;
    }

    /**
     * 
     * @return image Image
     */
    public BufferedImage getImage()
    {
        return image;
    }

    /**
     * Read the image stream of the signature and set height and width.
     *
     * @param stream stream of your visible signature image
     * @throws IOException If we can't read, flush, or close stream of image
     */
    private void readImageStream(InputStream stream) throws IOException
    {
        ImageIO.setUseCache(false);
        setImage(ImageIO.read(stream));
    }

    /**
     * Set image and its height and width.
     *
     * @param image
     */
    private void setImage(BufferedImage image)
    {
        this.image = image;
        imageHeight = (float) image.getHeight();
        imageWidth = (float) image.getWidth();
    }

    /**
     * 
     * @return Affine Transform parameters of for PDF Matrix
     */
    public byte[] getAffineTransformParams()
    {
        return AffineTransformParams;
    }

    /**
     * 
     * @param affineTransformParams
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner affineTransformParams(byte[] affineTransformParams)
    {
        AffineTransformParams = affineTransformParams;
        return this;
    }

    /**
     * 
     * @return formatter PDRectanle parameters
     */
    public byte[] getFormatterRectangleParams()
    {
        return formatterRectangleParams;
    }

    /**
     * Sets formatter PDRectangle
     * 
     * @param formatterRectangleParams
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner formatterRectangleParams(byte[] formatterRectangleParams)
    {
        this.formatterRectangleParams = formatterRectangleParams;
        return this;
    }

    /**
     * 
     * @return page width
     */
    public float getPageWidth()
    {
        return pageWidth;
    }

    /**
     * 
     * @param pageWidth pageWidth
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner pageWidth(float pageWidth)
    {
        this.pageWidth = pageWidth;
        return this;
    }

    /**
     * 
     * @return page height
     */
    public float getPageHeight()
    {
        return pageHeight;
    }

    /**
     * get image size in percents
     * @return the image size in percent
     */
    public float getImageSizeInPercents()
    {
        return imageSizeInPercents;
    }

   /**
    * 
    * @param imageSizeInPercents
    */
    public void imageSizeInPercents(float imageSizeInPercents)
    {
        this.imageSizeInPercents = imageSizeInPercents;
    }

    /**
     * returns visible signature text
     * @return the visible signature's text
     */
    public String getSignatureText()
    {
        throw new UnsupportedOperationException("That method is not yet implemented");
    }

    /**
     * 
     * @param signatureText - adds the text on visible signature
     * @return the signature design
     */
    public PDVisibleSignDesigner signatureText(String signatureText)
    {
        throw new UnsupportedOperationException("That method is not yet implemented");
    }
}
