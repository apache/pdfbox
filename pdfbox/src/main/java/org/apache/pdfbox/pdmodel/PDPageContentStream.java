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
package org.apache.pdfbox.pdmodel;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;

/**
 * Provides the ability to write to a page content stream.
 *
 * @author Ben Litchfield
 */
public final class PDPageContentStream extends PDAbstractContentStream implements Closeable
{
    /**
     * This is to choose what to do with the stream: overwrite, append or prepend.
     */
    public enum AppendMode
    {
        /**
         * Overwrite the existing page content streams.
         */
        OVERWRITE, 
        /**
         * Append the content stream after all existing page content streams.
         */
        APPEND, 
        /**
         * Insert before all other page content streams.
         */
        PREPEND;

        public boolean isOverwrite()
        {
            return this == OVERWRITE;
        }

        public boolean isPrepend()
        {
            return this == PREPEND;
        }
    }
  
    private static final Logger LOG = LogManager.getLogger(PDPageContentStream.class);

    private boolean sourcePageHadContents = false;

    /**
     * Create a new PDPage content stream. This constructor overwrites all existing content streams
     * of this page.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(PDDocument document, PDPage sourcePage) throws IOException
    {
        this(document, sourcePage, AppendMode.OVERWRITE, true, false);
        if (sourcePageHadContents)
        {
            LOG.warn("You are overwriting an existing content, you should use the append mode");
        }
    }

    /**
     * Create a new PDPage content stream. If the appendContent parameter is set to
     * {@link AppendMode#APPEND}, you may want to use
     * {@link #PDPageContentStream(PDDocument, PDPage, PDPageContentStream.AppendMode, boolean, boolean)}
     * instead, with the fifth parameter set to true.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @param appendContent Indicates whether content will be overwritten, appended or prepended.
     * @param compress Tell if the content stream should compress the page contents.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(PDDocument document, PDPage sourcePage, AppendMode appendContent,
                               boolean compress) throws IOException
    {
        this(document, sourcePage, appendContent, compress, false);
    }
    
    /**
     * Create a new PDPage content stream.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @param appendContent Indicates whether content will be overwritten, appended or prepended.
     * @param compress Tell if the content stream should compress the page contents.
     * @param resetContext Tell if the graphic context should be reset. This is only relevant when
     * the appendContent parameter is set to {@link AppendMode#APPEND}. You should use this when
     * appending to an existing stream, because the existing stream may have changed graphic
     * properties (e.g. scaling, rotation).
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(PDDocument document, PDPage sourcePage, AppendMode appendContent,
                               boolean compress, boolean resetContext) throws IOException
    {
        this(document, sourcePage, appendContent, compress, resetContext, new PDStream(document),
                sourcePage.getResources() != null ? sourcePage.getResources() : new PDResources());
    }

    private PDPageContentStream(PDDocument document, PDPage sourcePage, AppendMode appendContent,
                                boolean compress, boolean resetContext,PDStream stream,
                                PDResources resources) throws IOException
    {
        super(document, stream.createOutputStream(compress ? COSName.FLATE_DECODE : null), resources);

        // propagate resources to the page
        if (sourcePage.getResources() == null)
        {
            sourcePage.setResources(resources);
        }

        // If request specifies the need to append/prepend to the document
        if (!appendContent.isOverwrite() && sourcePage.hasContents())
        {
            // Add new stream to contents array
            COSBase contents = sourcePage.getCOSObject().getDictionaryObject(COSName.CONTENTS);
            COSArray array;
            if (contents instanceof COSArray)
            {
                // If contents is already an array, a new stream is simply appended to it
                array = (COSArray) contents;
            }
            else
            {
                // Creates a new array and adds the current stream plus a new one to it
                array = new COSArray();
                array.add(contents);
            }

            if (appendContent.isPrepend())
            {
                array.add(0, stream.getCOSObject());
            }
            else
            {
                array.add(stream);
            }

            // save the initial/unmodified graphics context
            if (resetContext)
            {
                // create a new stream to prefix existing stream
                PDStream prefixStream = new PDStream(document);

                // save the pre-append graphics state
                try (OutputStream prefixOut = prefixStream.createOutputStream())
                {
                    prefixOut.write("q".getBytes(StandardCharsets.US_ASCII));
                    prefixOut.write('\n');
                }

                // insert the new stream at the beginning
                array.add(0, prefixStream.getCOSObject());
            }

            // Sets the compoundStream as page contents
            sourcePage.getCOSObject().setItem(COSName.CONTENTS, array);

            // restore the pre-append graphics state
            if (resetContext)
            {
                restoreGraphicsState();
            }
        }
        else
        {
            sourcePageHadContents = sourcePage.hasContents();
            sourcePage.setContents(stream);
        }

        // configure NumberFormat
        setMaximumFractionDigits(5);
    }

    /**
     * Create a new appearance stream. Note that this is not actually a "page" content stream.
     *
     * @param doc The document the page is part of.
     * @param appearance The appearance stream to write to.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(PDDocument doc, PDAppearanceStream appearance) throws IOException
    {
        this (doc, appearance, appearance.getStream().createOutputStream()); 
    }
    
    /**
     * Create a new appearance stream. Note that this is not actually a "page" content stream.
     *
     * @param doc The document the appearance is part of.
     * @param appearance The appearance stream to add to.
     * @param outputStream The appearances output stream to write to.
     */
    public PDPageContentStream(PDDocument doc, PDAppearanceStream appearance, OutputStream outputStream)
    {
        super(doc, outputStream, appearance.getResources());
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param commands The commands to append to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated Usage of this method is discouraged.
     */
    @Deprecated
    public void appendRawCommands(String commands) throws IOException
    {
        write(commands);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param commands The commands to append to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated Usage of this method is discouraged.
     */
    @Deprecated
    public void appendRawCommands(byte[] commands) throws IOException
    {
        writeBytes(commands);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a raw byte to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated Usage of this method is discouraged.
     */
    @Deprecated
    public void appendRawCommands(int data) throws IOException
    {
        writeOperand(data);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a formatted double value to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated Usage of this method is discouraged.
     */
    @Deprecated
    public void appendRawCommands(double data) throws IOException
    {
        writeOperand((float) data);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a formatted float value to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated Usage of this method is discouraged.
     */
    @Deprecated
    public void appendRawCommands(float data) throws IOException
    {
        writeOperand(data);
    }
}
