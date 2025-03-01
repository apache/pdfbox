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

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDInlineImage;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.util.Matrix;

import java.awt.Color;
import java.io.Closeable;
import java.io.IOException;

/**
 * Describes the ability to write to a content stream.
 */
public interface PDContentOutputStream extends Closeable
{
    /**
     * Begin some text operations.
     *
     * @throws IOException If there is an error writing to the stream or if you attempt to
     *         nest beginText calls.
     * @throws IllegalStateException If the method was not allowed to be called at this time.
     */
    void beginText() throws IOException;

    /**
     * End some text operations.
     *
     * @throws IOException If there is an error writing to the stream or if you attempt to
     *         nest endText calls.
     * @throws IllegalStateException If the method was not allowed to be called at this time.
     */
    void endText() throws IOException;

    /**
     * Set the font and font size to draw text with.
     *
     * @param font The font to use.
     * @param fontSize The font size to draw the text.
     * @throws IOException If there is an error writing the font information.
     */
    void setFont(PDFont font, float fontSize) throws IOException;

    /**
     * Shows the given text at the location specified by the current text matrix with the given
     * interspersed positioning. This allows the user to efficiently position each glyph or sequence
     * of glyphs.
     *
     * @param textWithPositioningArray An array consisting of String and Float types. Each String is
     * output to the page using the current text matrix. Using the default coordinate system, each
     * interspersed number adjusts the current text matrix by translating to the left or down for
     * horizontal and vertical text respectively. The number is expressed in thousands of a text
     * space unit, and may be negative.
     *
     * @throws IOException if an io exception occurs.
     */
    void showTextWithPositioning(Object[] textWithPositioningArray) throws IOException;

    /**
     * Shows the given text at the location specified by the current text matrix.
     *
     * @param text The Unicode text to show.
     * @throws IOException If an io exception occurs.
     * @throws IllegalArgumentException if a character isn't supported by the current font
     */
    void showText(String text) throws IOException;

    /**
     * Sets the text leading.
     *
     * @param leading The leading in unscaled text units.
     * @throws IOException If there is an error writing to the stream.
     */
    void setLeading(float leading) throws IOException;

    /**
     * Move to the start of the next line of text. Requires the leading (see {@link #setLeading})
     * to have been set.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    void newLine() throws IOException;

    /**
     * The Td operator.
     * Move to the start of the next line, offset from the start of the current line by (tx, ty).
     *
     * @param tx The x translation.
     * @param ty The y translation.
     * @throws IOException If there is an error writing to the stream.
     * @throws IllegalStateException If the method was not allowed to be called at this time.
     */
    void newLineAtOffset(float tx, float ty) throws IOException;

    /**
     * The Tm operator. Sets the text matrix to the given values.
     * A current text matrix will be replaced with the new one.
     *
     * @param matrix the transformation matrix
     * @throws IOException If there is an error writing to the stream.
     * @throws IllegalStateException If the method was not allowed to be called at this time.
     */
    void setTextMatrix(Matrix matrix) throws IOException;

    /**
     * Draw an image at the x,y coordinates, with the default size of the image.
     *
     * @param image The image to draw.
     * @param x The x-coordinate to draw the image.
     * @param y The y-coordinate to draw the image.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    void drawImage(PDImageXObject image, float x, float y) throws IOException;

    /**
     * Draw an image at the x,y coordinates, with the given size.
     *
     * @param image The image to draw.
     * @param x The x-coordinate to draw the image.
     * @param y The y-coordinate to draw the image.
     * @param width The width to draw the image.
     * @param height The height to draw the image.
     *
     * @throws IOException If there is an error writing to the stream.
     * @throws IllegalStateException If the method was called within a text block.
     */
    void drawImage(PDImageXObject image, float x, float y, float width, float height) throws IOException;

    /**
     * Draw an image at the origin with the given transformation matrix.
     *
     * @param image The image to draw.
     * @param matrix The transformation matrix to apply to the image.
     *
     * @throws IOException If there is an error writing to the stream.
     * @throws IllegalStateException If the method was called within a text block.
     */
    void drawImage(PDImageXObject image, Matrix matrix) throws IOException;

    /**
     * Draw an inline image at the x,y coordinates, with the default size of the image.
     *
     * @param inlineImage The inline image to draw.
     * @param x The x-coordinate to draw the inline image.
     * @param y The y-coordinate to draw the inline image.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    void drawImage(PDInlineImage inlineImage, float x, float y) throws IOException;

    /**
     * Draw an inline image at the x,y coordinates and a certain width and height.
     *
     * @param inlineImage The inline image to draw.
     * @param x The x-coordinate to draw the inline image.
     * @param y The y-coordinate to draw the inline image.
     * @param width The width of the inline image to draw.
     * @param height The height of the inline image to draw.
     *
     * @throws IOException If there is an error writing to the stream.
     * @throws IllegalStateException If the method was called within a text block.
     */
    void drawImage(PDInlineImage inlineImage, float x, float y, float width, float height) throws IOException;

    /**
     * Draws the given Form XObject at the current location.
     *
     * @param form Form XObject
     * @throws IOException if the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void drawForm(PDFormXObject form) throws IOException;

    /**
     * The cm operator. Concatenates the given matrix with the current transformation matrix (CTM),
     * which maps user space coordinates used within a PDF content stream into output device
     * coordinates. More details on coordinates can be found in the PDF 32000 specification, 8.3.2
     * Coordinate Spaces.
     *
     * @param matrix the transformation matrix
     * @throws IOException If there is an error writing to the stream.
     */
    void transform(Matrix matrix) throws IOException;

    /**
     * q operator. Saves the current graphics state.
     * @throws IOException If an error occurs while writing to the stream.
     */
    void saveGraphicsState() throws IOException;

    /**
     * Q operator. Restores the current graphics state.
     * @throws IOException If an error occurs while writing to the stream.
     */
    void restoreGraphicsState() throws IOException;

    /**
     * Sets the stroking color and, if necessary, the stroking color space.
     *
     * @param color Color in a specific color space.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    void setStrokingColor(PDColor color) throws IOException;

    /**
     * Set the stroking color using an AWT color. Conversion uses the default sRGB color space.
     *
     * @param color The color to set.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    void setStrokingColor(Color color) throws IOException;

    /**
     * Set the stroking color in the DeviceRGB color space. Range is 0..1.
     *
     * @param r The red value.
     * @param g The green value.
     * @param b The blue value.
     * @throws IOException If an IO error occurs while writing to the stream.
     * @throws IllegalArgumentException If the parameters are invalid.
     */
    void setStrokingColor(float r, float g, float b) throws IOException;

    /**
     * Set the stroking color in the DeviceCMYK color space. Range is 0..1
     *
     * @param c The cyan value.
     * @param m The magenta value.
     * @param y The yellow value.
     * @param k The black value.
     * @throws IOException If an IO error occurs while writing to the stream.
     * @throws IllegalArgumentException If the parameters are invalid.
     */
    void setStrokingColor(float c, float m, float y, float k) throws IOException;

    /**
     * Set the stroking color in the DeviceGray color space. Range is 0..1.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     * @throws IllegalArgumentException If the parameter is invalid.
     */
    void setStrokingColor(float g) throws IOException;

    /**
     * Sets the non-stroking color and, if necessary, the non-stroking color space.
     *
     * @param color Color in a specific color space.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    void setNonStrokingColor(PDColor color) throws IOException;

    /**
     * Set the non-stroking color using an AWT color. Conversion uses the default sRGB color space.
     *
     * @param color The color to set.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    void setNonStrokingColor(Color color) throws IOException;

    /**
     * Set the non-stroking color in the DeviceRGB color space. Range is 0..1.
     *
     * @param r The red value.
     * @param g The green value.
     * @param b The blue value.
     * @throws IOException If an IO error occurs while writing to the stream.
     * @throws IllegalArgumentException If the parameters are invalid.
     */
    void setNonStrokingColor(float r, float g, float b) throws IOException;

    /**
     * Set the non-stroking color in the DeviceCMYK color space. Range is 0..1.
     *
     * @param c The cyan value.
     * @param m The magenta value.
     * @param y The yellow value.
     * @param k The black value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    void setNonStrokingColor(float c, float m, float y, float k) throws IOException;

    /**
     * Set the non-stroking color in the DeviceGray color space. Range is 0..1.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     * @throws IllegalArgumentException If the parameter is invalid.
     */
    void setNonStrokingColor(float g) throws IOException;

    /**
     * Add a rectangle to the current path.
     *
     * @param x The lower left x coordinate.
     * @param y The lower left y coordinate.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     * @throws IOException If the content stream could not be written.
     * @throws IllegalStateException If the method was called within a text block.
     */
    void addRect(float x, float y, float width, float height) throws IOException;

    /**
     * Append a cubic Bézier curve to the current path. The curve extends from the current point to
     * the point (x3, y3), using (x1, y1) and (x2, y2) as the Bézier control points.
     *
     * @param x1 x coordinate of the point 1
     * @param y1 y coordinate of the point 1
     * @param x2 x coordinate of the point 2
     * @param y2 y coordinate of the point 2
     * @param x3 x coordinate of the point 3
     * @param y3 y coordinate of the point 3
     * @throws IOException If the content stream could not be written.
     * @throws IllegalStateException If the method was called within a text block.
     */
    void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException;

    /**
     * Append a cubic Bézier curve to the current path. The curve extends from the current point to
     * the point (x3, y3), using the current point and (x2, y2) as the Bézier control points.
     *
     * @param x2 x coordinate of the point 2
     * @param y2 y coordinate of the point 2
     * @param x3 x coordinate of the point 3
     * @param y3 y coordinate of the point 3
     * @throws IllegalStateException If the method was called within a text block.
     * @throws IOException If the content stream could not be written.
     */
    void curveTo2(float x2, float y2, float x3, float y3) throws IOException;

    /**
     * Append a cubic Bézier curve to the current path. The curve extends from the current point to
     * the point (x3, y3), using (x1, y1) and (x3, y3) as the Bézier control points.
     *
     * @param x1 x coordinate of the point 1
     * @param y1 y coordinate of the point 1
     * @param x3 x coordinate of the point 3
     * @param y3 y coordinate of the point 3
     * @throws IOException If the content stream could not be written.
     * @throws IllegalStateException If the method was called within a text block.
     */
    void curveTo1(float x1, float y1, float x3, float y3) throws IOException;

    /**
     * Move the current position to the given coordinates.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @throws IOException If the content stream could not be written.
     * @throws IllegalStateException If the method was called within a text block.
     */
    void moveTo(float x, float y) throws IOException;

    /**
     * Draw a line from the current position to the given coordinates.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @throws IOException If the content stream could not be written.
     * @throws IllegalStateException If the method was called within a text block.
     */
    void lineTo(float x, float y) throws IOException;

    /**
     * Stroke the path.
     *
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void stroke() throws IOException;

    /**
     * Close and stroke the path.
     *
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void closeAndStroke() throws IOException;

    /**
     * Fills the path using the nonzero winding number rule.
     *
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void fill() throws IOException;

    /**
     * Fills the path using the even-odd winding rule.
     *
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void fillEvenOdd() throws IOException;

    /**
     * Fill and then stroke the path, using the nonzero winding number rule to determine the region
     * to fill. This shall produce the same result as constructing two identical path objects,
     * painting the first with {@link #fill() } and the second with {@link #stroke() }.
     *
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void fillAndStroke() throws IOException;

    /**
     * Fill and then stroke the path, using the even-odd rule to determine the region to
     * fill. This shall produce the same result as constructing two identical path objects, painting
     * the first with {@link #fillEvenOdd() } and the second with {@link #stroke() }.
     *
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void fillAndStrokeEvenOdd() throws IOException;

    /**
     * Close, fill, and then stroke the path, using the nonzero winding number rule to determine the
     * region to fill. This shall have the same effect as the sequence {@link #closePath() }
     * and then {@link #fillAndStroke() }.
     *
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void closeAndFillAndStroke() throws IOException;

    /**
     * Close, fill, and then stroke the path, using the even-odd rule to determine the region to
     * fill. This shall have the same effect as the sequence {@link #closePath() }
     * and then {@link #fillAndStrokeEvenOdd() }.
     *
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void closeAndFillAndStrokeEvenOdd() throws IOException;

    /**
     * Fills the clipping area with the given shading.
     *
     * @param shading Shading resource
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void shadingFill(PDShading shading) throws IOException;

    /**
     * Closes the current subpath.
     *
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void closePath() throws IOException;

    /**
     * Intersects the current clipping path with the current path, using the nonzero rule.
     *
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void clip() throws IOException;

    /**
     * Intersects the current clipping path with the current path, using the even-odd rule.
     *
     * @throws IOException If the content stream could not be written
     * @throws IllegalStateException If the method was called within a text block.
     */
    void clipEvenOdd() throws IOException;

    /**
     * Set line width to the given value.
     *
     * @param lineWidth The width which is used for drawing.
     * @throws IOException If the content stream could not be written
     */
    void setLineWidth(float lineWidth) throws IOException;

    /**
     * Set the line join style.
     *
     * @param lineJoinStyle 0 for miter join, 1 for round join, and 2 for bevel join.
     * @throws IOException If the content stream could not be written.
     * @throws IllegalArgumentException If the parameter is not a valid line join style.
     */
    void setLineJoinStyle(int lineJoinStyle) throws IOException;

    /**
     * Set the line cap style.
     *
     * @param lineCapStyle 0 for butt cap, 1 for round cap, and 2 for projecting square cap.
     * @throws IOException If the content stream could not be written.
     * @throws IllegalArgumentException If the parameter is not a valid line cap style.
     */
    void setLineCapStyle(int lineCapStyle) throws IOException;

    /**
     * Set the line dash pattern.
     *
     * @param pattern The pattern array
     * @param phase The phase of the pattern
     * @throws IOException If the content stream could not be written.
     */
    void setLineDashPattern(float[] pattern, float phase) throws IOException;

    /**
     * Set the miter limit.
     *
     * @param miterLimit the new miter limit.
     * @throws IOException If the content stream could not be written.
     * @throws IllegalArgumentException If the parameter is \u2264 0.
     */
    void setMiterLimit(float miterLimit) throws IOException;

    /**
     * Begin a marked content sequence.
     *
     * @param tag the tag to be added to the content stream
     * @throws IOException If the content stream could not be written
     */
    void beginMarkedContent(COSName tag) throws IOException;

    /**
     * Begin a marked content sequence with a reference to the marked content identifier (MCID).
     *
     * @param tag the tag to be added to the content stream
     * @param mcid the marked content identifier (MCID)
     * @throws IOException If the content stream could not be written
     */
    void beginMarkedContent(COSName tag, int mcid) throws IOException;

    /**
     * Begin a marked content sequence with a reference to an entry in the page resources' Properties dictionary.
     *
     * @param tag the tag to be added to the content stream
     * @param propertyList property list to be added to the content stream
     * @throws IOException If the content stream could not be written
     */
    void beginMarkedContent(COSName tag, PDPropertyList propertyList) throws IOException;

    /**
     * End a marked content sequence.
     *
     * @throws IOException If the content stream could not be written
     */
    void endMarkedContent() throws IOException;

    /**
     * Set an extended graphics state.
     *
     * @param state The extended graphics state to be added to the content stream
     * @throws IOException If the content stream could not be written.
     */
    void setGraphicsStateParameters(PDExtendedGraphicsState state) throws IOException;

    /**
     * Write a comment line.
     *
     * @param comment the comment to be added to the content stream
     *
     * @throws IOException If the content stream could not be written.
     * @throws IllegalArgumentException If the comment contains a newline. This is not allowed, because the next line
     * could be ordinary PDF content.
     */
    void addComment(String comment) throws IOException;

    /**
     * Close the content stream.  This must be called when you are done with this object.
     *
     * @throws IOException If the underlying stream has a problem being written to.
     */
    @Override
    void close() throws IOException;

    /**
     * Set the character spacing. The value shall be added to the horizontal or vertical component
     * of the glyph's displacement, depending on the writing mode.
     *
     * @param spacing character spacing
     * @throws IOException If the content stream could not be written.
     */
    void setCharacterSpacing(float spacing) throws IOException;

    /**
     * Set the word spacing. The value shall be added to the horizontal or vertical component of the
     * ASCII SPACE character, depending on the writing mode.
     * <p>
     * This will have an effect only with Type1 and TrueType fonts, not with Type0 fonts. The PDF
     * specification tells why: "Word spacing shall be applied to every occurrence of the
     * single-byte character code 32 in a string when using a simple font or a composite font that
     * defines code 32 as a single-byte code. It shall not apply to occurrences of the byte value 32
     * in multiple-byte codes."
     *
     * @param spacing word spacing
     * @throws IOException If the content stream could not be written.
     */
    void setWordSpacing(float spacing) throws IOException;

    /**
     * Set the horizontal scaling to scale / 100.
     *
     * @param scale number specifying the percentage of the normal width. Default value: 100 (normal
     * width).
     * @throws IOException If the content stream could not be written.
     */
    void setHorizontalScaling(float scale) throws IOException;

    /**
     * Set the text rendering mode. This determines whether showing text shall cause glyph outlines
     * to be stroked, filled, used as a clipping boundary, or some combination of the three.
     *
     * @param rm The text rendering mode.
     * @throws IOException If the content stream could not be written.
     */
    void setRenderingMode(RenderingMode rm) throws IOException;

    /**
     * Set the text rise value, i.e. move the baseline up or down. This is useful for drawing superscripts or
     * subscripts.
     *
     * @param rise Specifies the distance, in unscaled text space units, to move the baseline up or down from its
     * default location. 0 restores the default location.
     * @throws IOException If the content stream could not be written.
     */
    void setTextRise(float rise) throws IOException;
}
