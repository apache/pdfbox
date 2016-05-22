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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.pdfbox.contentstream.PDAbstractContentStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;

/**
 * Provides the ability to write to a page content stream.
 *
 * @author Ben Litchfield
 */
public final class PDAppearanceContentStream extends PDAbstractContentStream implements Closeable
{
    
    /**
     * This is to choose what to do with the stream: overwrite, append or prepend.
     */
    public static enum AppendMode
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
    

    // number format
    private final NumberFormat formatDecimal = NumberFormat.getNumberInstance(Locale.US);

    /**
     * Create a new appearance stream.
     *
     * @param doc The document the page is part of.
     * @param appearance The appearance stream to write to.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDAppearanceContentStream(PDAppearanceStream appearance) throws IOException
    {
        this (appearance, appearance.getStream().createOutputStream()); 
    }
    
    /**
     * Create a new appearance stream. Note that this is not actually a "page" content stream.
     *
     * @param doc The document the appearance is part of.
     * @param appearance The appearance stream to add to.
     * @param outputStream The appearances output stream to write to.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDAppearanceContentStream(PDAppearanceStream appearance, OutputStream outputStream)
            throws IOException
    {
        super(appearance, outputStream);
        setResources(appearance.getResources());
        
        formatDecimal.setMaximumFractionDigits(4);
        formatDecimal.setGroupingUsed(false);
    }

    public void setStrokingColor(float[] components) throws IOException
    {
        for (float value : components)
        {
            writeOperand(value);
        }
        
        int numComponents = components.length;
        switch(numComponents)
        {
            case 1:
                writeOperator("G");
                break;
            case 3:
                writeOperator("RG");
                break;
            case 4:
                writeOperator("K");
        }
    }
}
