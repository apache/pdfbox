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
package org.apache.pdfbox.pdmodel.font;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.NoninvertibleTransformException;
import java.io.IOException;

import java.io.InputStream;
import java.util.HashMap;

import org.apache.fontbox.afm.FontMetric;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.encoding.DictionaryEncoding;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.EncodingManager;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * This class contains implementation details of the simple pdf fonts.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.18 $
 */
public abstract class PDSimpleFont extends PDFont
{
    private final HashMap<Integer, Float> mFontSizes =
        new HashMap<Integer, Float>(128);

    private float avgFontWidth = 0.0f;
    private float avgFontHeight = 0.0f;
    private float fontWidthOfSpace = -1f; 

    private static final byte[] SPACE_BYTES = { (byte)32 };


    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDSimpleFont.class);
    
    /**
     * Constructor.
     */
    public PDSimpleFont()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDSimpleFont( COSDictionary fontDictionary )
    {
        super( fontDictionary );
    }

    /**
    * Looks up, creates, returns  the AWT Font.
    * 
    * @return returns the awt font to bes used for rendering 
    * @throws IOException if something went wrong.
    */
    public Font getawtFont() throws IOException
    {
        LOG.error("Not yet implemented:" + getClass().getName() );
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public void drawString( String string, int[] codePoints, Graphics g, float fontSize, 
            AffineTransform at, float x, float y ) throws IOException
    {
        Font awtFont = getawtFont();
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        GlyphVector glyphs = null;
        boolean useCodepoints = codePoints != null && isType0Font();
        PDFont descendantFont = useCodepoints ? ((PDType0Font)this).getDescendantFont() : null;
        // symbolic fonts may trigger the same fontmanager.so/dll error as described below
        if (useCodepoints && !descendantFont.getFontDescriptor().isSymbolic())
        {
            PDCIDFontType2Font cid2Font = null;
            if (descendantFont instanceof PDCIDFontType2Font)
            {
                cid2Font = (PDCIDFontType2Font)descendantFont;
            }
            if((cid2Font != null && cid2Font.hasCIDToGIDMap()) || isFontSubstituted)
            {
                // we still have to use the string if a CIDToGIDMap is used 
                glyphs = awtFont.createGlyphVector(frc, string);
            }
            else
            {
                glyphs = awtFont.createGlyphVector(frc, codePoints);
            }
        }
        else 
        {
            // mdavis - fix fontmanager.so/dll on sun.font.FileFont.getGlyphImage
            // for font with bad cmaps?
            // Type1 fonts are not affected as they don't have cmaps
            if (!isType1Font() && awtFont.canDisplayUpTo(string) != -1) 
            {
                LOG.warn("Changing font on <" + string + "> from <"
                        + awtFont.getName() + "> to the default font");
                awtFont = Font.decode(null).deriveFont(1f);
            }
            glyphs = awtFont.createGlyphVector(frc, string);
        }
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        writeFont(g2d, at, x, y, glyphs);
    }

    /**
     * This will get the font height for a character.
     *
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getFontHeight( byte[] c, int offset, int length ) throws IOException
    {
        // maybe there is already a precalculated value
        if (avgFontHeight > 0)
        {
            return avgFontHeight;
        }
        float retval = 0;
        FontMetric metric = getAFM();
        if( metric != null )
        {
            int code = getCodeFromArray( c, offset, length );
            Encoding encoding = getFontEncoding();
            String characterName = encoding.getName( code );
            retval = metric.getCharacterHeight( characterName );
        }
        else
        {
            PDFontDescriptor desc = getFontDescriptor();
            if( desc != null )
            {
                // the following values are all more or less accurate
                // at least all are average values. Maybe we'll find
                // another way to get those value for every single glyph
                // in the future if needed
                PDRectangle fontBBox = desc.getFontBoundingBox();
                if (fontBBox != null)
                {
                    retval = fontBBox.getHeight() / 2;
                }
                if( retval == 0 )
                {
                    retval = desc.getCapHeight();
                }
                if( retval == 0 )
                {
                    retval = desc.getAscent();
                }
                if( retval == 0 )
                {
                    retval = desc.getXHeight();
                    if (retval > 0)
                    {
                        retval -= desc.getDescent();
                    }
                }
                avgFontHeight = retval;
            }
        }
        return retval;
    }

    /**
     * This will get the font width for a character.
     *
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getFontWidth( byte[] c, int offset, int length ) throws IOException
    {
        int code = getCodeFromArray( c, offset, length );
        Float fontWidth = mFontSizes.get(code);
        if (fontWidth == null)
        {
            fontWidth = getFontWidth(code);
            if (fontWidth <= 0)
            {
                //hmm should this be in PDType1Font??
                fontWidth = getFontWidthFromAFMFile( code );
            }
            mFontSizes.put(code, fontWidth);
        }
        return fontWidth;
    }

    /**
     * This will get the average font width for all characters.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getAverageFontWidth() throws IOException
    {
        float average = 0.0f;

        //AJW
        if (avgFontWidth != 0.0f)
        {
            average = avgFontWidth;
        }
        else
        {
            float totalWidth = 0.0f;
            float characterCount = 0.0f;
            COSArray widths = (COSArray)font.getDictionaryObject( COSName.WIDTHS );
            if( widths != null )
            {
                for( int i=0; i<widths.size(); i++ )
                {
                    COSNumber fontWidth = (COSNumber)widths.getObject( i );
                    if( fontWidth.floatValue() > 0 )
                    {
                        totalWidth += fontWidth.floatValue();
                        characterCount += 1;
                    }
                }
            }

            if( totalWidth > 0 )
            {
                average = totalWidth / characterCount;
            }
            else
            {
                average = getAverageFontWidthFromAFMFile();
            }
            avgFontWidth = average;
        }
        return average;
    }

    
    /**
     * This will get the ToUnicode object.
     *
     * @return The ToUnicode object.
     */
    public COSBase getToUnicode()
    {
        return font.getDictionaryObject( COSName.TO_UNICODE );
    }

    /**
     * This will set the ToUnicode object.
     *
     * @param unicode The unicode object.
     */
    public void setToUnicode( COSBase unicode )
    {
        font.setItem( COSName.TO_UNICODE, unicode );
    }

    /**
     * This will get the fonts bounding box.
     *
     * @return The fonts bouding box.
     *
     * @throws IOException If there is an error getting the bounding box.
     */
    public PDRectangle getFontBoundingBox() throws IOException
    {
        return getFontDescriptor().getFontBoundingBox();
    }

    /**
     * This will draw a string on a canvas using the font.
     *
     * @param g2d The graphics to draw onto.
     * @param at The transformation matrix with all information for scaling and shearing of the font.
     * @param x The x coordinate to draw at.
     * @param y The y coordinate to draw at.
     * @param glyphs The GlyphVector containing the glyphs to be drawn.
     *
     */
    protected void writeFont(final Graphics2D g2d, final AffineTransform at, 
            final float x, final float y, final GlyphVector glyphs) 
    {
        // check if we have a rotation
        if (!at.isIdentity()) 
        {
            try 
            {
                AffineTransform atInv = at.createInverse();
                // do only apply the size of the transform, rotation will be realized by rotating the graphics,
                // otherwise the hp printers will not render the font
                // apply the transformation to the graphics, which should be the same as applying the
                // transformation itself to the text
                g2d.transform(at);
                // translate the coordinates
                Point2D.Float newXy = new  Point2D.Float(x,y);
                atInv.transform(new Point2D.Float( x, y), newXy);
                g2d.drawGlyphVector(glyphs, (float)newXy.getX(), (float)newXy.getY() );
                // restore the original transformation
                g2d.transform(atInv);
            }
            catch (NoninvertibleTransformException e) 
            {
                LOG.error("Error in "+getClass().getName()+".writeFont",e);
            }
        }
        else 
        {
            g2d.drawGlyphVector(glyphs, x, y);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void determineEncoding()
    {
        String cmapName = null;
        COSName encodingName = null;
        COSBase encoding = getEncoding(); 
        Encoding fontEncoding = null;
        if (encoding != null) 
        {
            if (encoding instanceof COSName) 
            {
                if (cmap == null)
                {
                    encodingName = (COSName)encoding;
                    cmap = cmapObjects.get( encodingName.getName() );
                    if (cmap == null) 
                    {
                        cmapName = encodingName.getName();
                    }
                }
                if (cmap == null && cmapName != null)
                {
                    try 
                    {
                        fontEncoding =
                            EncodingManager.INSTANCE.getEncoding(encodingName);
                    }
                    catch(IOException exception) 
                    {
                        LOG.debug("Debug: Could not find encoding for " + encodingName );
                    }
                }
            }
            else if(encoding instanceof COSStream )
            {
                if (cmap == null)
                {
                    COSStream encodingStream = (COSStream)encoding;
                    try 
                    {
                        cmap = parseCmap( null, encodingStream.getUnfilteredStream() );
                    }
                    catch(IOException exception) 
                    {
                        LOG.error("Error: Could not parse the embedded CMAP" );
                    }
                }
            }
            else if (encoding instanceof COSDictionary) 
            {
                try 
                {
                    fontEncoding = new DictionaryEncoding((COSDictionary)encoding);
                }
                catch(IOException exception) 
                {
                    LOG.error("Error: Could not create the DictionaryEncoding" );
                }
            }
        }
        setFontEncoding(fontEncoding);
        extractToUnicodeEncoding();

        if (cmap == null && cmapName != null) 
        {
        	InputStream cmapStream = null;
            try 
            {
                // look for a predefined CMap with the given name
                cmapStream = ResourceLoader.loadResource(resourceRootCMAP + cmapName);
                if (cmapStream != null)
                {
                	cmap = parseCmap(resourceRootCMAP, cmapStream);
                	if (cmap == null && encodingName == null)
                	{
                		LOG.error("Error: Could not parse predefined CMAP file for '" + cmapName + "'");
                	}
                }
                else
                {
            		LOG.debug("Debug: '" + cmapName + "' isn't a predefined map, most likely it's embedded in the pdf itself.");
                }
            }
            catch(IOException exception) 
            {
                LOG.error("Error: Could not find predefined CMAP file for '" + cmapName + "'" );
            }
            finally
            {
            	IOUtils.closeQuietly(cmapStream);
            }
        }
    }

    private void extractToUnicodeEncoding()
    {
        COSName encodingName = null;
        String cmapName = null;
        COSBase toUnicode = getToUnicode();
        if( toUnicode != null )
        {
            setHasToUnicode(true);
            if ( toUnicode instanceof COSStream )
            {
                try 
                {
                    toUnicodeCmap = parseCmap( resourceRootCMAP, ((COSStream)toUnicode).getUnfilteredStream());
                }
                catch(IOException exception) 
                {
                    LOG.error("Error: Could not load embedded ToUnicode CMap" );
                }
            }
            else if ( toUnicode instanceof COSName)
            {
                encodingName = (COSName)toUnicode;
                toUnicodeCmap = cmapObjects.get( encodingName.getName() );
                if (toUnicodeCmap == null) 
                {
                    cmapName = encodingName.getName();
                    String resourceName = resourceRootCMAP + cmapName;
                    try 
                    {
                        toUnicodeCmap = parseCmap( resourceRootCMAP, ResourceLoader.loadResource( resourceName ));
                    }
                    catch(IOException exception) 
                    {
                        LOG.error("Error: Could not find predefined ToUnicode CMap file for '" + cmapName + "'" );
                    }
                    if( toUnicodeCmap == null)
                    {
                        LOG.error("Error: Could not parse predefined ToUnicode CMap file for '" + cmapName + "'" );
                    }
                }
            }
        }
    }
    
    private boolean isFontSubstituted = false;
    
    /**
     * This will get the value for isFontSubstituted, which indicates
     * if the font was substituted due to a problem with the embedded one.
     * 
     * @return true if the font was substituted
     */
    protected boolean isFontSubstituted()
    {
        return isFontSubstituted;
    }
    
    /**
     * This will set  the value for isFontSubstituted.
     * 
     * @param isSubstituted true if the font was substituted
     */
    protected void setIsFontSubstituted(boolean isSubstituted)
    {
        isFontSubstituted = isSubstituted;
    }

    /**
     * {@inheritDoc}
     */
    public float getSpaceWidth()
    {
        if (fontWidthOfSpace == -1f)
        {
            COSBase toUnicode = getToUnicode();
            try 
            {
                if (toUnicode != null) 
                {
                    int spaceMapping = toUnicodeCmap.getSpaceMapping();
                    if (spaceMapping > -1)
                    {
                        fontWidthOfSpace = getFontWidth(spaceMapping);
                    }
                }
                else
                {
                    fontWidthOfSpace = getFontWidth( SPACE_BYTES, 0, 1 );
                }
                // use the average font width as fall back
                if (fontWidthOfSpace <= 0)
                {
                    fontWidthOfSpace = getAverageFontWidth();
                }
            }
            catch (Exception e) 
            {
                LOG.error("Can't determine the width of the space character using 250 as default", e);
                fontWidthOfSpace = 250f;
            }
        }
        return fontWidthOfSpace;
    }

}
