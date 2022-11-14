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

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
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
    private int[] formatterRectangleParameters = { 0, 0, 100, 50 };
    private AffineTransform affineTransform = new AffineTransform();
    private float imageSizeInPercents;
    private int rotation = 0;

    /**
     * Constructor.
     *
     * @param filename Path of the PDF file
     * @param imageStream image as a stream
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IOException if the new instance of PDVisibleSignDesigner could not be created
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
     * @param documentSource Original PDF document as RandomAccessRead
     * @param imageStream Image as a stream
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IOException if the new instance of PDVisibleSignDesigner could not be created
     */
    public PDVisibleSignDesigner(RandomAccessRead documentSource, InputStream imageStream, int page)
            throws IOException
    {
        // set visible signature image Input stream
        readImageStream(imageStream);

        // calculate height and width of document page
        calculatePageSizeFromRandomAccessRead(documentSource, page);
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
     * @param image the image to be used for the visible signature
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IOException if the new instance of PDVisibleSignDesigner could not be created
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
     * @param documentSource Original PDF document as RandomAccessRead
     * @param image the image to be used for the visible signature
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IOException if the new instance of PDVisibleSignDesigner could not be created
     */
    public PDVisibleSignDesigner(RandomAccessRead documentSource, BufferedImage image, int page)
            throws IOException
    {
        // set visible signature image
        setImage(image);

        // calculate height and width of document page
        calculatePageSizeFromRandomAccessRead(documentSource, page);
    }

    /**
     * Constructor.
     *
     * @param document Already created PDDocument of your PDF document.
     * @param image the image to be used for the visible signature
     * @param page The 1-based page number for which the page size should be calculated.
     */
    public PDVisibleSignDesigner(PDDocument document, BufferedImage image, int page)
    {
        setImage(image);
        calculatePageSize(document, page);
    }

    /**
     * Constructor.
     *
     * @param pageTree Already created DPPageTree of your PDF document.
     * @param image
     * @param page The 1-based page number for which the page size should be calculated.
     * @see PDDocument#getPages()
     */
    public PDVisibleSignDesigner(PDPageTree pageTree, BufferedImage image, int page)
    {
        setImage(image);
        calculatePageSize(pageTree, page);
    }

    /**
     * Constructor usable for signing existing signature fields.
     *
     * @param imageStream image as a stream
     * @throws IOException if the new instance of PDVisibleSignDesigner could not be created
     */
    public PDVisibleSignDesigner(InputStream imageStream) throws IOException
    {
        // set visible signature image Input stream
        readImageStream(imageStream);
    }

    private void calculatePageSizeFromFile(String filename, int page) throws IOException
    {
        try (PDDocument document = Loader.loadPDF(new File(filename)))
        {
            // calculate height and width of document page
            calculatePageSize(document, page);
        }
    }

    private void calculatePageSizeFromRandomAccessRead(RandomAccessRead documentSource, int page)
            throws IOException
    {
        try (PDDocument document = Loader.loadPDF(documentSource))
        {
            // calculate height and width of document page
            calculatePageSize(document, page);
        }
    }

    /**
     * Each page of document can be different sizes. This method calculates the page size based on
     * the page media box.
     *
     * @param pageTree
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IllegalArgumentException if the page argument is lower than 1.
     */
    private void calculatePageSize(PDPageTree pageTree, int page)
    {
        if (page < 1)
        {
            throw new IllegalArgumentException("First page of pdf is 1, not " + page);
        }

        PDPage firstPage = pageTree.get(page - 1);
        PDRectangle mediaBox = firstPage.getMediaBox();
        pageHeight(mediaBox.getHeight());
        pageWidth = mediaBox.getWidth();
        imageSizeInPercents = 100;
        rotation = firstPage.getRotation() % 360;
    }

    /**
     * Each page of document can be different sizes. This method calculates the page size based on
     * the page media box.
     * 
     * @param document
     * @param page The 1-based page number for which the page size should be calculated.
     * @throws IllegalArgumentException if the page argument is lower than 1.
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
        rotation = firstPage.getRotation() % 360;
    }

    /**
     * Adjust signature for page rotation. This is optional, call this after all x and y coordinates
     * have been set if you want the signature to be positioned regardless of page orientation.
     *
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner adjustForRotation()
    {
        switch (rotation)
        {
            case 90:
                // https://stackoverflow.com/a/34359956/535646
                float temp = yAxis;
                yAxis = pageHeight - xAxis - imageWidth;
                xAxis = temp;

                affineTransform = new AffineTransform(
                        0, imageHeight / imageWidth, -imageWidth / imageHeight, 0, imageWidth, 0);

                temp = imageHeight;
                imageHeight = imageWidth;
                imageWidth = temp;
                break;

            case 180:
                float newX = pageWidth - xAxis - imageWidth;
                float newY = pageHeight - yAxis - imageHeight;
                xAxis = newX;
                yAxis = newY;

                affineTransform = new AffineTransform(-1, 0, 0, -1, imageWidth, imageHeight);
                break;

            case 270:
                temp = xAxis;
                xAxis = pageWidth - yAxis - imageHeight;
                yAxis = temp;

                affineTransform = new AffineTransform(
                        0, -imageHeight / imageWidth, imageWidth / imageHeight, 0, 0, imageHeight);

                temp = imageHeight;
                imageHeight = imageWidth;
                imageWidth = temp;
                break;

            case 0:
            default:
                break;
        }
        return this;
    }

    /**
     * Set the image for the signature.
     *
     * @param path Path of the image file.
     * @return Visible Signature Configuration Object
     * @throws IOException if the image for the signature could not be set
     */
    public PDVisibleSignDesigner signatureImage(String path) throws IOException
    {
        try (InputStream in = new BufferedInputStream(new FileInputStream(path)))
        {
            readImageStream(in);
        }
        return this;
    }

    /**
     * Zoom signature image with some percent.
     * 
     * @param percent increase (positive value) or decrease (negative value) image with x percent.
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner zoom(float percent)
    {
        imageHeight += (imageHeight * percent) / 100;
        imageWidth += (imageWidth * percent) / 100;
        formatterRectangleParameters[2] = (int) imageWidth.floatValue();
        formatterRectangleParameters[3] = (int) imageHeight.floatValue();
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
     * @param yAxis y coordinate
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
        this.formatterRectangleParameters[2] = (int) width;
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
        this.formatterRectangleParameters[3] = (int) height;
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
     * @param signatureFieldName the name of the signature field
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
        formatterRectangleParameters[2] = image.getWidth();
        formatterRectangleParameters[3] = image.getHeight();
    }

    /**
     * @return Affine Transform parameters for PDF Matrix
     */
    public AffineTransform getTransform()
    {
        return affineTransform;
    }

    /**
     * 
     * @param affineTransform the affine transformation
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner transform(AffineTransform affineTransform)
    {
        this.affineTransform = new AffineTransform(affineTransform);
        return this;
    }

    /**
     * 
     * @return formatter PDRectangle parameters
     */
    public int[] getFormatterRectangleParameters()
    {
        return formatterRectangleParameters;
    }

    /**
     * Sets formatter PDRectangle
     * 
     * @param formatterRectangleParameters rectangle parameter of the formatter
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner formatterRectangleParameters(int[] formatterRectangleParameters)
    {
        this.formatterRectangleParameters = formatterRectangleParameters;
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
    * @param imageSizeInPercents image size in percents
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
