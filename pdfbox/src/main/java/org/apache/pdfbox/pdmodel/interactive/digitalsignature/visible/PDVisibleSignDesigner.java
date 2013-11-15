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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.bouncycastle.util.Arrays;

/**
 * 
 * That class is in order to build  your 
 * visible signature design. Because of 
 * this is builder, instead of setParam()
 * we use param() methods.
 * @author <a href="mailto:vakhtang.koroghlishvili@gmail.com"> vakhtang koroghlishvili (gogebashvili) </a>
 */
public class PDVisibleSignDesigner
{

    private Float sigImgWidth;
    private Float sigImgHeight;
    private float xAxis;
    private float yAxis;
    private float pageHeight;
    private float pageWidth;
    private InputStream imgageStream;
    private String signatureFieldName = "sig"; // default
    private byte[] formaterRectangleParams = { 0, 0, 100, 50 }; // default
    private byte[] AffineTransformParams =   { 1, 0, 0, 1, 0, 0 }; // default
    private float imageSizeInPercents;
    private PDDocument document = null;
  
    

    /**
     * 
     * @param originalDocumenStream
     * @param imageStream
     * @param page-which page are you going to add visible signature
     * @throws IOException
     */
    public PDVisibleSignDesigner(InputStream originalDocumenStream, InputStream imageStream, int page) throws IOException
    {
        signatureImageStream(imageStream);
        document = PDDocument.load(originalDocumenStream);
        calculatePageSize(document, page);
    }

    /**
     * 
     * @param documentPath - path of your pdf document
     * @param imageStream - stream of image
     * @param page -which page are you going to add visible signature
     * @throws IOException
     */
    public PDVisibleSignDesigner(String documentPath, InputStream imageStream, int page) throws IOException
    {

        // set visible singature image Input stream
        signatureImageStream(imageStream);

        // create PD document
        document = PDDocument.load(documentPath);

        // calculate height an width of document
        calculatePageSize(document, page);

        document.close();
    }

    /**
     * 
     * @param doc - Already created PDDocument of your PDF document
     * @param imageStream
     * @param page
     * @throws IOException - If we can't read, flush, or can't close stream
     */
    public PDVisibleSignDesigner(PDDocument doc, InputStream imageStream, int page) throws IOException 
    {
        signatureImageStream(imageStream);
        calculatePageSize(doc, page);
    }

    /**
     * Each page of document can be different sizes.
     * 
     * @param document
     * @param page
     */
    private void calculatePageSize(PDDocument document, int page)
    {

        if (page < 1)
        {
            throw new IllegalArgumentException("First page of pdf is 1, not " + page);
        }

        List<?> pages = document.getDocumentCatalog().getAllPages();
        PDPage firstPage =(PDPage) pages.get(page - 1);
        PDRectangle mediaBox = firstPage.findMediaBox();
        this.pageHeight(mediaBox.getHeight());
        this.pageWidth = mediaBox.getWidth();

        float x = this.pageWidth;
        float y = 0;
        this.pageWidth = this.pageWidth + y;
        float tPercent = (100 * y / (x + y));
        this.imageSizeInPercents = 100 - tPercent;

    }

    /**
     * 
     * @param path  of image location
     * @return image Stream
     * @throws IOException
     */
    public PDVisibleSignDesigner signatureImage(String path) throws IOException
    {
        InputStream fin = new FileInputStream(path);
        return signatureImageStream(fin);
    }

    /**
     * zoom signature image with some percent.
     * 
     * @param percent- x % increase image with x percent.
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner zoom(float percent)
    {
        sigImgHeight = sigImgHeight + (sigImgHeight * percent) / 100;
        sigImgWidth = sigImgWidth + (sigImgWidth * percent) / 100;
        return this;
    }

    /**
     * 
     * @param xAxis - x coordinate 
     * @param yAxis - y coordinate
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
        return sigImgWidth;
    }

    /**
     * 
     * @param sets signature image width
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner width(float signatureImgWidth)
    {
        this.sigImgWidth = signatureImgWidth;
        return this;
    }

    /**
     * 
     * @return signature image height
     */
    public float getHeight()
    {
        return sigImgHeight;
    }

    /**
     * 
     * @param set signature image Height
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner height(float signatureImgHeight)
    {
        this.sigImgHeight = signatureImgHeight;
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
     * @return image Stream
     */
    public InputStream getImageStream()
    {
        return imgageStream;
    }

    /**
     * 
     * @param imgageStream- stream of your visible signature image
     * @return Visible Signature Configuration Object
     * @throws IOException - If we can't read, flush, or close stream of image
     */
    private PDVisibleSignDesigner signatureImageStream(InputStream imageStream) throws IOException 
    {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = imageStream.read(buffer)) > -1)
        {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        baos.close();

        byte[] byteArray = baos.toByteArray();
        byte[] byteArraySecond = Arrays.clone(byteArray);

        InputStream inputForBufferedImage = new ByteArrayInputStream(byteArray);
        InputStream revertInputStream = new ByteArrayInputStream(byteArraySecond);

        if (sigImgHeight == null || sigImgWidth == null)
        {
            calcualteImageSize(inputForBufferedImage);
        }

        this.imgageStream = revertInputStream;

        return this;
    }

    /**
     * calculates image width and height. sported formats: all
     * 
     * @param fis - input stream of image
     * @throws IOException - if can't read input stream
     */
    private void calcualteImageSize(InputStream fis) throws IOException 
    {

        BufferedImage bimg = ImageIO.read(fis);
        int width = bimg.getWidth();
        int height = bimg.getHeight();

        sigImgHeight = (float) height;
        sigImgWidth = (float) width;

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
    public byte[] getFormaterRectangleParams()
    {
        return formaterRectangleParams;
    }

    /**
     * sets formatter PDRectangle;
     * 
     * @param formaterRectangleParams
     * @return Visible Signature Configuration Object
     */
    public PDVisibleSignDesigner formaterRectangleParams(byte[] formaterRectangleParams)
    {
        this.formaterRectangleParams = formaterRectangleParams;
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
     * @param sets pageWidth
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
     * @return
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
     * @return
     */
    public String getSignatureText()
    {
        throw new UnsupportedOperationException("That method is not yet implemented");
    }

    /**
     * 
     * @param signatureText - adds the text on visible signature
     * @return
     */
    public PDVisibleSignDesigner signatureText(String signatureText)
    {
        throw new UnsupportedOperationException("That method is not yet implemented");
    }

}
