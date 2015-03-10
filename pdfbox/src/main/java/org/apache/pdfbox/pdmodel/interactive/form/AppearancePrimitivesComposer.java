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
package org.apache.pdfbox.pdmodel.interactive.form;

import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.Charsets;

/**
 * Write the primitives making up a content stream.
 *
 * <strong>This class shall be treated internal use only!</strong>
 */
class AppearancePrimitivesComposer
{
    // the ouput stream to write to
    private final OutputStream outputstream;
    
    // number format for real number output
    private final NumberFormat formatDecimal = NumberFormat.getNumberInstance(Locale.US);
    
    // will be set for operators doing text output
    private boolean inTextMode = false;
    
    AppearancePrimitivesComposer(OutputStream outputstream)
    {
        this.outputstream = outputstream;
    }
    
    /**
     * Add a rectangle to the current path.
     *
     * @param rect the rectangle.
     * @throws IOException If the content stream could not be written.
     */
    public void addRect(PDRectangle rect) throws IOException
    {
        addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
    }
    
    /**
     * Add a rectangle to the current path.
     *
     * @param x The lower left x coordinate.
     * @param y The lower left y coordinate.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     * @throws IOException If the content stream could not be written.
     */
    public void addRect(float x, float y, float width, float height) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: addRect is not allowed within a text block.");
        }
        writeOperand(x);
        writeOperand(y);
        writeOperand(width);
        writeOperand(height);
        writeOperator("re");
    }

    /**
     * Begin some text operations.
     *
     * @throws IOException If there is an error writing to the stream or if you attempt to
     *         nest beginText calls.
     */
    public void beginText() throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: Nested beginText() calls are not allowed.");
        }
        writeOperator("BT");
        inTextMode = true;
    }
    
    /**
     * Intersects the current clipping path with the current path, using the nonzero rule.
     *
     * @throws IOException If the content stream could not be written
     */
    public void clip() throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: clip is not allowed within a text block.");
        }
        writeOperator("W");
        writeOperator("n"); // end path without filling or stroking
    }

    /**
     * End some text operations.
     *
     * @throws IOException If there is an error writing to the stream or if you attempt to
     *         nest endText calls.
     */
    public void endText() throws IOException
    {
        if (!inTextMode)
        {
            throw new IOException("Error: You must call beginText() before calling endText.");
        }
        writeOperator("ET");
        inTextMode = false;
    }
    
    
    /**
     * The Td operator.
     * Move to the start of the next line, offset from the start of the current line by (tx, ty).
     *
     * @param tx The x translation.
     * @param ty The y translation.
     * @throws IOException if there is an error writing to the stream.
     */
    void newLineAtOffset(float tx, float ty) throws IOException
    {
        writeOperand(tx);
        writeOperand(ty);
        writeOperator("Td");
    }
    
    /**
     * Shows the given text at the location specified by the current text matrix.
     *
     * @param text The Unicode text to show.
     * @throws IOException if there is an error writing to the stream.
     */
    void showText(String text, PDFont font) throws IOException
    {
        COSWriter.writeString(font.encode(text), outputstream);
        write(" ");
        writeOperator("Tj");
    }
    
    /**
     * Writes a string to the content stream as ASCII.
     */
    private void write(String text) throws IOException
    {
        outputstream.write(text.getBytes(Charsets.US_ASCII));
    }
    
    /**
     * Writes a string to the content stream as ASCII.
     */
    private void writeOperator(String text) throws IOException
    {
        outputstream.write(text.getBytes(Charsets.US_ASCII));
        outputstream.write('\n');
    }
    
    /**
     * Writes a real real to the content stream.
     */
    private void writeOperand(float real) throws IOException
    {
        write(formatDecimal.format(real));
        outputstream.write(' ');
    }
}
