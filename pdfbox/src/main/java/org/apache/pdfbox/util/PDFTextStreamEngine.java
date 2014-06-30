package org.apache.pdfbox.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.text.TextPosition;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Properties;

/**
 * PDFStreamEngine subclass for advanced processing of text via TextPosition.
 *
 * @see org.apache.pdfbox.text.TextPosition
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDFTextStreamEngine extends PDFStreamEngine
{
    private static final Log log = LogFactory.getLog(PDFStreamEngine.class);

    private int pageRotation;
    private PDRectangle pageSize;

    private PDFTextStreamEngine()
    {
    }

    /**
     * Constructor with engine properties. The property keys are all PDF operators, the values are
     * class names used to execute those operators. An empty value means that the operator will be
     * silently ignored.
     *
     * @param properties The engine properties.
     */
    public PDFTextStreamEngine(Properties properties)
    {
        super(properties);
    }

    /**
     * This will initialise and process the contents of the stream.
     *
     * @param resources The location to retrieve resources.
     * @param cosStream the Stream to execute.
     * @param pageSize the size of the page
     * @param rotation the page rotation
     * @throws java.io.IOException if there is an error accessing the stream.
     */
    public void processStream(PDResources resources, COSStream cosStream, PDRectangle pageSize,
                              int rotation) throws IOException
    {
        this.pageRotation = rotation;
        this.pageSize = pageSize;
        super.processStream(resources, cosStream, pageSize);
    }

    /**
     * This method was originally written by Ben Litchfield for PDFStreamEngine.
     */
    @Override
    protected final void processGlyph(Matrix textMatrix, Point2D.Float end, float maxHeight,
                                      float widthText, String unicode,
                                      int[] charCodes, PDFont font, float fontSize)
                                      throws IOException
    {
        // Note on variable names. There are three different units being used in this code.
        // Character sizes are given in glyph units, text locations are initially given in text
        // units, and we want to save the data in display units. The variable names should end with
        // Text or Disp to represent if the values are in text or disp units (no glyph units are
        // saved).

        float fontSizeText = getGraphicsState().getTextState().getFontSize();
        float horizontalScalingText = getGraphicsState().getTextState().getHorizontalScaling()/100f;
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();

        float glyphSpaceToTextSpaceFactor = 1 / 1000f;
        if (font instanceof PDType3Font)
        {
            // This will typically be 1000 but in the case of a type3 font
            // this might be a different number
            glyphSpaceToTextSpaceFactor = 1f / font.getFontMatrix().getValue(0, 0);
        }

        float spaceWidthText = 0;
        try
        {
            // to avoid crash as described in PDFBOX-614, see what the space displacement should be
            spaceWidthText = font.getSpaceWidth() * glyphSpaceToTextSpaceFactor;
        }
        catch (Throwable exception)
        {
            log.warn(exception, exception);
        }

        if (spaceWidthText == 0)
        {
            spaceWidthText = font.getAverageFontWidth() * glyphSpaceToTextSpaceFactor;
            // the average space width appears to be higher than necessary so make it smaller
            spaceWidthText *= .80f;
        }
        if (spaceWidthText == 0)
        {
            spaceWidthText = 1.0f; // if could not find font, use a generic value
        }

        // the space width has to be transformed into display units
        float spaceWidthDisp = spaceWidthText * fontSizeText * horizontalScalingText *
                textMatrix.getXScale()  * ctm.getXScale();

        // PDFBOX-373: Replace a null entry with "?" so it is not printed as "(null)"
        if (unicode == null)
        {
            unicode = "?";
        }

        processTextPosition(new TextPosition(pageRotation, pageSize.getWidth(),
                pageSize.getHeight(), textMatrix, end.x, end.y, maxHeight, widthText,
                spaceWidthDisp, unicode, charCodes, font, fontSize,
                (int)(fontSize * textMatrix.getXScale())));
    }

    /**
     * A method provided as an event interface to allow a subclass to perform some specific
     * functionality when text needs to be processed.
     *
     * @param text The text to be processed.
     */
    protected void processTextPosition(TextPosition text)
    {
        // subclasses can override to provide specific functionality
    }
}
