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
package org.apache.pdfbox.pdmodel.edit;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSStreamArray;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.MapUtil;


/**
 * This class will is a convenience for creating page content streams.  You MUST
 * call close() when you are finished with this object.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.19 $
 */
public class PDPageContentStream
{
    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDPageContentStream.class);

    private PDPage page;
    private OutputStream output;
    private boolean inTextMode = false;
    private Map<PDFont,String> fontMappings;
    private Map<PDXObject,String> xobjectMappings;
    private PDResources resources;
    private Map fonts;
    private Map xobjects;

    private PDColorSpace currentStrokingColorSpace = new PDDeviceGray();
    private PDColorSpace currentNonStrokingColorSpace = new PDDeviceGray();

    //cached storage component for getting color values
    private float[] colorComponents = new float[4];

    private NumberFormat formatDecimal = NumberFormat.getNumberInstance( Locale.US );

    private static final String BEGIN_TEXT = "BT\n";
    private static final String END_TEXT = "ET\n";
    private static final String SET_FONT = "Tf\n";
    private static final String MOVE_TEXT_POSITION = "Td\n";
    private static final String SET_TEXT_MATRIX = "Tm\n";
    private static final String SHOW_TEXT = "Tj\n";

    private static final String SAVE_GRAPHICS_STATE = "q\n";
    private static final String RESTORE_GRAPHICS_STATE = "Q\n";
    private static final String CONCATENATE_MATRIX = "cm\n";
    private static final String XOBJECT_DO = "Do\n";
    private static final String RG_STROKING = "RG\n";
    private static final String RG_NON_STROKING = "rg\n";
    private static final String K_STROKING = "K\n";
    private static final String K_NON_STROKING = "k\n";
    private static final String G_STROKING = "G\n";
    private static final String G_NON_STROKING = "g\n";
    private static final String RECTANGLE = "re\n";
    private static final String FILL_NON_ZERO = "f\n";
    private static final String FILL_EVEN_ODD = "f*\n";
    private static final String LINE_TO = "l\n";
    private static final String MOVE_TO = "m\n";
    private static final String CLOSE_STROKE = "s\n";
    private static final String STROKE = "S\n";
    private static final String LINE_WIDTH = "w\n";
    private static final String CLOSE_SUBPATH = "h\n";
    private static final String CLIP_PATH_NON_ZERO = "W\n";
    private static final String CLIP_PATH_EVEN_ODD = "W*\n";
    private static final String NOP = "n\n";
    private static final String BEZIER_312 = "c\n";
    private static final String BEZIER_32 = "v\n";
    private static final String BEZIER_313 = "y\n";

    private static final String MP = "MP\n";
    private static final String DP = "DP\n";
    private static final String BMC = "BMC\n";
    private static final String BDC = "BDC\n";
    private static final String EMC = "EMC\n";

    private static final String SET_STROKING_COLORSPACE = "CS\n";
    private static final String SET_NON_STROKING_COLORSPACE = "cs\n";

    private static final String SET_STROKING_COLOR_SIMPLE="SC\n";
    private static final String SET_STROKING_COLOR_COMPLEX="SCN\n";
    private static final String SET_NON_STROKING_COLOR_SIMPLE="sc\n";
    private static final String SET_NON_STROKING_COLOR_COMPLEX="scn\n";



    private static final int SPACE = 32;


    /**
     * Create a new PDPage content stream.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream( PDDocument document, PDPage sourcePage ) throws IOException
    {
        this(document,sourcePage,false,true);
    }

    /**
     * Create a new PDPage content stream.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @param appendContent Indicates whether content will be overwritten. If false all previous content is deleted.
     * @param compress Tell if the content stream should compress the page contents.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream( PDDocument document, PDPage sourcePage, boolean appendContent, boolean compress )
        throws IOException
    {
        this(document,sourcePage,appendContent,compress,false);
    }
    /**
     * Create a new PDPage content stream.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @param appendContent Indicates whether content will be overwritten. If false all previous content is deleted.
     * @param compress Tell if the content stream should compress the page contents.
     * @param resetContext Tell if the graphic context should be reseted.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream( PDDocument document, PDPage sourcePage, boolean appendContent, boolean compress, boolean resetContext )
            throws IOException
    {
        
        page = sourcePage;
        resources = page.getResources();
        if( resources == null )
        {
            resources = new PDResources();
            page.setResources( resources );
        }

        //Fonts including reverse lookup
        fonts = resources.getFonts();
        fontMappings = reverseMap(fonts, PDFont.class);

        //XObjects including reverse lookup
        xobjects = resources.getXObjects();
        xobjectMappings = reverseMap(xobjects, PDXObject.class);

        // Get the pdstream from the source page instead of creating a new one
        PDStream contents = sourcePage.getContents();
        boolean hasContent = contents != null;

        // If request specifies the need to append to the document
        if(appendContent && hasContent)
        {
            
            // Create a pdstream to append new content
            PDStream contentsToAppend = new PDStream( document );

            // This will be the resulting COSStreamArray after existing and new streams are merged
            COSStreamArray compoundStream = null;

            // If contents is already an array, a new stream is simply appended to it
            if(contents.getStream() instanceof COSStreamArray)
            {
                compoundStream = (COSStreamArray)contents.getStream();
                compoundStream.appendStream( contentsToAppend.getStream());
            }
            else
            {
                // Creates the COSStreamArray and adds the current stream plus a new one to it
                COSArray newArray = new COSArray();
                newArray.add(contents.getCOSObject());
                newArray.add(contentsToAppend.getCOSObject());
                compoundStream = new COSStreamArray(newArray);
            }

            if( compress )
            {
                List<COSName> filters = new ArrayList<COSName>();
                filters.add( COSName.FLATE_DECODE );
                contentsToAppend.setFilters( filters );
            }

            if (resetContext)
            {
                // create a new stream to encapsulate the existing stream 
                PDStream saveGraphics = new PDStream( document );
                output = saveGraphics.createOutputStream();
                // save the initial/unmodified graphics context
                saveGraphicsState();
                close();
                if( compress )
                {
                    List<COSName> filters = new ArrayList<COSName>();
                    filters.add( COSName.FLATE_DECODE );
                    saveGraphics.setFilters( filters );
                }
                // insert the new stream at the beginning
                compoundStream.insertCOSStream(saveGraphics);
            }

            // Sets the compoundStream as page contents
            sourcePage.setContents( new PDStream(compoundStream) );
            output = contentsToAppend.createOutputStream();
            if (resetContext)
            {
                // restore the initial/unmodified graphics context
                restoreGraphicsState();
            }
        }
        else
        {
            if (hasContent)
            {
                log.warn("You are overwriting an existing content, you should use the append mode");
            }
            contents = new PDStream( document );
            if( compress )
            {
                List<COSName> filters = new ArrayList<COSName>();
                filters.add( COSName.FLATE_DECODE );
                contents.setFilters( filters );
            }
            sourcePage.setContents( contents );
            output = contents.createOutputStream();
        }
        formatDecimal.setMaximumFractionDigits( 10 );
        formatDecimal.setGroupingUsed( false );
    }

    private <T> Map<T, String> reverseMap(Map map, Class<T> keyClass)
    {
        Map<T, String> reversed = new java.util.HashMap<T, String>();
        for (Object o : map.entrySet())
        {
            Map.Entry entry = (Map.Entry)o;
            reversed.put(keyClass.cast(entry.getValue()), (String)entry.getKey());
        }
        return reversed;
    }

    /**
     * Begin some text operations.
     *
     * @throws IOException If there is an error writing to the stream or if you attempt to
     *         nest beginText calls.
     */
    public void beginText() throws IOException
    {
        if( inTextMode )
        {
            throw new IOException( "Error: Nested beginText() calls are not allowed." );
        }
        appendRawCommands( BEGIN_TEXT );
        inTextMode = true;
    }

    /**
     * End some text operations.
     *
     * @throws IOException If there is an error writing to the stream or if you attempt to
     *         nest endText calls.
     */
    public void endText() throws IOException
    {
        if( !inTextMode )
        {
            throw new IOException( "Error: You must call beginText() before calling endText." );
        }
        appendRawCommands( END_TEXT );
        inTextMode = false;
    }

    /**
     * Set the font to draw text with.
     *
     * @param font The font to use.
     * @param fontSize The font size to draw the text.
     * @throws IOException If there is an error writing the font information.
     */
    public void setFont( PDFont font, float fontSize ) throws IOException
    {
        String fontMapping = fontMappings.get( font );
        if( fontMapping == null )
        {
            fontMapping = MapUtil.getNextUniqueKey( fonts, "F" );
            fontMappings.put( font, fontMapping );
            fonts.put( fontMapping, font );
        }
        appendRawCommands( "/");
        appendRawCommands( fontMapping );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( fontSize ) );
        appendRawCommands( SPACE );
        appendRawCommands( SET_FONT );
    }

    /**
     * Draw an image at the x,y coordinates, with the default size of the image.
     *
     * @param image The image to draw.
     * @param x The x-coordinate to draw the image.
     * @param y The y-coordinate to draw the image.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    public void drawImage( PDXObjectImage image, float x, float y ) throws IOException
    {
        drawXObject( image, x, y, image.getWidth(), image.getHeight() );
    }

    /**
     * Draw an xobject(form or image) at the x,y coordinates and a certain width and height.
     *
     * @param xobject The xobject to draw.
     * @param x The x-coordinate to draw the image.
     * @param y The y-coordinate to draw the image.
     * @param width The width of the image to draw.
     * @param height The height of the image to draw.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    public void drawXObject( PDXObject xobject, float x, float y, float width, float height ) throws IOException
    {
        AffineTransform transform = new AffineTransform(width, 0, 0, height, x, y);
        drawXObject(xobject, transform);
    }

    /**
     * Draw an xobject(form or image) using the given {@link AffineTransform} to position
     * the xobject.
     *
     * @param xobject The xobject to draw.
     * @param transform the transformation matrix
     * @throws IOException If there is an error writing to the stream.
     */
    public void drawXObject( PDXObject xobject, AffineTransform transform ) throws IOException
    {
        String xObjectPrefix = null;
        if( xobject instanceof PDXObjectImage )
        {
            xObjectPrefix = "Im";
        }
        else
        {
            xObjectPrefix = "Form";
        }

        String objMapping = xobjectMappings.get( xobject );
        if( objMapping == null )
        {
            objMapping = MapUtil.getNextUniqueKey( xobjects, xObjectPrefix );
            xobjectMappings.put( xobject, objMapping );
            xobjects.put( objMapping, xobject );
        }
        saveGraphicsState();
        appendRawCommands( SPACE );
        concatenate2CTM(transform);
        appendRawCommands( SPACE );
        appendRawCommands( "/" );
        appendRawCommands( objMapping );
        appendRawCommands( SPACE );
        appendRawCommands( XOBJECT_DO );
        restoreGraphicsState();
    }


    /**
     * The Td operator.
     * A current text matrix will be replaced with a new one (1 0 0 1 x y).
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @throws IOException If there is an error writing to the stream.
     */
    public void moveTextPositionByAmount( float x, float y ) throws IOException
    {
        if( !inTextMode )
        {
            throw new IOException( "Error: must call beginText() before moveTextPositionByAmount");
        }
        appendRawCommands( formatDecimal.format( x ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y ) );
        appendRawCommands( SPACE );
        appendRawCommands( MOVE_TEXT_POSITION );
    }

    /**
     * The Tm operator. Sets the text matrix to the given values.
     * A current text matrix will be replaced with the new one.
     * @param a The a value of the matrix.
     * @param b The b value of the matrix.
     * @param c The c value of the matrix.
     * @param d The d value of the matrix.
     * @param e The e value of the matrix.
     * @param f The f value of the matrix.
     * @throws IOException If there is an error writing to the stream.
     */
    public void setTextMatrix( double a, double b, double c, double d, double e, double f ) throws IOException
    {
        if( !inTextMode )
        {
            throw new IOException( "Error: must call beginText() before setTextMatrix");
        }
        appendRawCommands( formatDecimal.format( a ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( b ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( c ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( e ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( f ) );
        appendRawCommands( SPACE );
        appendRawCommands( SET_TEXT_MATRIX );
    }

    /**
    * The Tm operator. Sets the text matrix to the given values.
    * A current text matrix will be replaced with the new one.
    * @param matrix the transformation matrix
    * @throws IOException If there is an error writing to the stream.
    */
    public void setTextMatrix(AffineTransform matrix) throws IOException
    {
        appendMatrix(matrix);
        appendRawCommands(SET_TEXT_MATRIX);
    }

    /**
     * The Tm operator. Sets the text matrix to the given scaling and translation values.
     * A current text matrix will be replaced with the new one.
     * @param sx The scaling factor in x-direction.
     * @param sy The scaling factor in y-direction.
     * @param tx The translation value in x-direction.
     * @param ty The translation value in y-direction.
     * @throws IOException If there is an error writing to the stream.
     */
    public void setTextScaling( double sx, double sy, double tx, double ty ) throws IOException
    {
        setTextMatrix(sx, 0, 0, sy, tx, ty);
    }

    /**
     * The Tm operator. Sets the text matrix to the given translation values.
     * A current text matrix will be replaced with the new one.
     * @param tx The translation value in x-direction.
     * @param ty The translation value in y-direction.
     * @throws IOException If there is an error writing to the stream.
     */
    public void setTextTranslation( double tx, double ty ) throws IOException
    {
        setTextMatrix(1, 0, 0, 1, tx, ty);
    }

    /**
     * The Tm operator. Sets the text matrix to the given rotation and translation values.
     * A current text matrix will be replaced with the new one.
     * @param angle The angle used for the counterclockwise rotation in radians.
     * @param tx The translation value in x-direction.
     * @param ty The translation value in y-direction.
     * @throws IOException If there is an error writing to the stream.
     */
    public void setTextRotation( double angle, double tx, double ty ) throws IOException
    {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);
        setTextMatrix( angleCos, angleSin, -angleSin, angleCos, tx, ty);
    }

    /**
     * The Cm operator. Concatenates the current transformation matrix with the given values.
     * @param a The a value of the matrix.
     * @param b The b value of the matrix.
     * @param c The c value of the matrix.
     * @param d The d value of the matrix.
     * @param e The e value of the matrix.
     * @param f The f value of the matrix.
     * @throws IOException If there is an error writing to the stream.
     */
    public void concatenate2CTM( double a, double b, double c, double d, double e, double f ) throws IOException
    {
        appendRawCommands( formatDecimal.format( a ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( b ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( c ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( e ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( f ) );
        appendRawCommands( SPACE );
        appendRawCommands( CONCATENATE_MATRIX );
    }

    /**
     * The Cm operator. Concatenates the current transformation matrix with the given
     * {@link AffineTransform}.
     * @param at the transformation matrix
     * @throws IOException If there is an error writing to the stream.
     */
    public void concatenate2CTM(AffineTransform at) throws IOException
    {
        appendMatrix(at);
        appendRawCommands(CONCATENATE_MATRIX);
    }

    /**
     * This will draw a string at the current location on the screen.
     *
     * @param text The text to draw.
     * @throws IOException If an io exception occurs.
     */
    public void drawString( String text ) throws IOException
    {
        if( !inTextMode )
        {
            throw new IOException( "Error: must call beginText() before drawString");
        }
        COSString string = new COSString( text );
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        string.writePDF( buffer );
        appendRawCommands( new String( buffer.toByteArray(), "ISO-8859-1"));
        appendRawCommands( SPACE );
        appendRawCommands( SHOW_TEXT );
    }

    /**
     * Set the stroking color space.  This will add the colorspace to the PDResources
     * if necessary.
     *
     * @param colorSpace The colorspace to write.
     * @throws IOException If there is an error writing the colorspace.
     */
    public void setStrokingColorSpace( PDColorSpace colorSpace ) throws IOException
    {
        currentStrokingColorSpace = colorSpace;
        writeColorSpace( colorSpace );
        appendRawCommands( SET_STROKING_COLORSPACE );
    }

    /**
     * Set the stroking color space.  This will add the colorspace to the PDResources
     * if necessary.
     *
     * @param colorSpace The colorspace to write.
     * @throws IOException If there is an error writing the colorspace.
     */
    public void setNonStrokingColorSpace( PDColorSpace colorSpace ) throws IOException
    {
        currentNonStrokingColorSpace = colorSpace;
        writeColorSpace( colorSpace );
        appendRawCommands( SET_NON_STROKING_COLORSPACE );
    }

    private void writeColorSpace( PDColorSpace colorSpace ) throws IOException
    {
        COSName key = null;
        if( colorSpace instanceof PDDeviceGray ||
            colorSpace instanceof PDDeviceRGB ||
            colorSpace instanceof PDDeviceCMYK )
        {
            key = COSName.getPDFName( colorSpace.getName() );
        }
        else
        {
            COSDictionary colorSpaces =
                (COSDictionary)resources.getCOSDictionary().getDictionaryObject(COSName.COLORSPACE);
            if( colorSpaces == null )
            {
                colorSpaces = new COSDictionary();
                resources.getCOSDictionary().setItem( COSName.COLORSPACE, colorSpaces );
            }
            key = colorSpaces.getKeyForValue( colorSpace.getCOSObject() );

            if( key == null )
            {
                int counter = 0;
                String csName = "CS";
                while( colorSpaces.containsValue( csName + counter ) )
                {
                    counter++;
                }
                key = COSName.getPDFName( csName + counter );
                colorSpaces.setItem( key, colorSpace );
            }
        }
        key.writePDF( output );
        appendRawCommands( SPACE );
    }

    /**
     * Set the color components of current stroking colorspace.
     *
     * @param components The components to set for the current color.
     * @throws IOException If there is an error while writing to the stream.
     */
    public void setStrokingColor( float[] components ) throws IOException
    {
        for( int i=0; i< components.length; i++ )
        {
            appendRawCommands( formatDecimal.format( components[i] ) );
            appendRawCommands( SPACE );
        }
        if( currentStrokingColorSpace instanceof PDSeparation ||
            currentStrokingColorSpace instanceof PDPattern ||
            currentStrokingColorSpace instanceof PDDeviceN ||
            currentStrokingColorSpace instanceof PDICCBased )
        {
            appendRawCommands( SET_STROKING_COLOR_COMPLEX );
        }
        else
        {
            appendRawCommands( SET_STROKING_COLOR_SIMPLE );
        }
    }

    /**
     * Set the stroking color, specified as RGB.
     *
     * @param color The color to set.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor( Color color ) throws IOException
    {
        ColorSpace colorSpace = color.getColorSpace();
        if( colorSpace.getType() == ColorSpace.TYPE_RGB )
        {
            setStrokingColor( color.getRed(), color.getGreen(), color.getBlue() );
        }
        else if( colorSpace.getType() == ColorSpace.TYPE_GRAY )
        {
            color.getColorComponents( colorComponents );
            setStrokingColor( colorComponents[0] );
        }
        else if( colorSpace.getType() == ColorSpace.TYPE_CMYK )
        {
            color.getColorComponents( colorComponents );
            setStrokingColor( colorComponents[0], colorComponents[2], colorComponents[2], colorComponents[3] );
        }
        else
        {
            throw new IOException( "Error: unknown colorspace:" + colorSpace );
        }
    }

    /**
     * Set the non stroking color, specified as RGB.
     *
     * @param color The color to set.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor( Color color ) throws IOException
    {
        ColorSpace colorSpace = color.getColorSpace();
        if( colorSpace.getType() == ColorSpace.TYPE_RGB )
        {
            setNonStrokingColor( color.getRed(), color.getGreen(), color.getBlue() );
        }
        else if( colorSpace.getType() == ColorSpace.TYPE_GRAY )
        {
            color.getColorComponents( colorComponents );
            setNonStrokingColor( colorComponents[0] );
        }
        else if( colorSpace.getType() == ColorSpace.TYPE_CMYK )
        {
            color.getColorComponents( colorComponents );
            setNonStrokingColor( colorComponents[0], colorComponents[2], colorComponents[2], colorComponents[3] );
        }
        else
        {
            throw new IOException( "Error: unknown colorspace:" + colorSpace );
        }
    }

    /**
     * Set the stroking color, specified as RGB, 0-255.
     *
     * @param r The red value.
     * @param g The green value.
     * @param b The blue value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor( int r, int g, int b ) throws IOException
    {
        appendRawCommands( formatDecimal.format( r/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( g/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( b/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( RG_STROKING );
    }

    /**
     * Set the stroking color, specified as CMYK, 0-255.
     *
     * @param c The cyan value.
     * @param m The magenta value.
     * @param y The yellow value.
     * @param k The black value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor( int c, int m, int y, int k) throws IOException
    {
        appendRawCommands( formatDecimal.format( c/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( m/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( k/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( K_STROKING );
    }

    /**
     * Set the stroking color, specified as CMYK, 0.0-1.0.
     *
     * @param c The cyan value.
     * @param m The magenta value.
     * @param y The yellow value.
     * @param k The black value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor( double c, double m, double y, double k) throws IOException
    {
        appendRawCommands( formatDecimal.format( c ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( m ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( k ) );
        appendRawCommands( SPACE );
        appendRawCommands( K_STROKING );
    }

    /**
     * Set the stroking color, specified as grayscale, 0-255.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor( int g ) throws IOException
    {
        appendRawCommands( formatDecimal.format( g/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( G_STROKING );
    }

    /**
     * Set the stroking color, specified as Grayscale 0.0-1.0.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor( double g ) throws IOException
    {
        appendRawCommands( formatDecimal.format( g ) );
        appendRawCommands( SPACE );
        appendRawCommands( G_STROKING );
    }

    /**
     * Set the color components of current non stroking colorspace.
     *
     * @param components The components to set for the current color.
     * @throws IOException If there is an error while writing to the stream.
     */
    public void setNonStrokingColor( float[] components ) throws IOException
    {
        for( int i=0; i< components.length; i++ )
        {
            appendRawCommands( formatDecimal.format( components[i] ) );
            appendRawCommands( SPACE );
        }
        if( currentNonStrokingColorSpace instanceof PDSeparation ||
            currentNonStrokingColorSpace instanceof PDPattern ||
            currentNonStrokingColorSpace instanceof PDDeviceN ||
            currentNonStrokingColorSpace instanceof PDICCBased )
        {
            appendRawCommands( SET_NON_STROKING_COLOR_COMPLEX );
        }
        else
        {
            appendRawCommands( SET_NON_STROKING_COLOR_SIMPLE );
        }
    }

    /**
     * Set the non stroking color, specified as RGB, 0-255.
     *
     * @param r The red value.
     * @param g The green value.
     * @param b The blue value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor( int r, int g, int b ) throws IOException
    {
        appendRawCommands( formatDecimal.format( r/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( g/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( b/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( RG_NON_STROKING );
    }

    /**
     * Set the non stroking color, specified as CMYK, 0-255.
     *
     * @param c The cyan value.
     * @param m The magenta value.
     * @param y The yellow value.
     * @param k The black value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor( int c, int m, int y, int k) throws IOException
    {
        appendRawCommands( formatDecimal.format( c/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( m/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( k/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( K_NON_STROKING );
    }

    /**
     * Set the non stroking color, specified as CMYK, 0.0-1.0.
     *
     * @param c The cyan value.
     * @param m The magenta value.
     * @param y The yellow value.
     * @param k The black value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor( double c, double m, double y, double k) throws IOException
    {
        appendRawCommands( formatDecimal.format( c ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( m ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( k ) );
        appendRawCommands( SPACE );
        appendRawCommands( K_NON_STROKING );
    }

    /**
     * Set the non stroking color, specified as grayscale, 0-255.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor( int g ) throws IOException
    {
        appendRawCommands( formatDecimal.format( g/255d ) );
        appendRawCommands( SPACE );
        appendRawCommands( G_NON_STROKING );
    }

    /**
     * Set the non stroking color, specified as Grayscale 0.0-1.0.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor( double g ) throws IOException
    {
        appendRawCommands( formatDecimal.format( g ) );
        appendRawCommands( SPACE );
        appendRawCommands( G_NON_STROKING );
    }

    /**
     * Add a rectangle to the current path.
     *
     * @param x The lower left x coordinate.
     * @param y The lower left y coordinate.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void addRect( float x, float y, float width, float height ) throws IOException
    {
        appendRawCommands( formatDecimal.format( x ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( width ) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( height ) );
        appendRawCommands( SPACE );
        appendRawCommands( RECTANGLE );
    }

    /**
     * Draw a rectangle on the page using the current non stroking color.
     *
     * @param x The lower left x coordinate.
     * @param y The lower left y coordinate.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void fillRect( float x, float y, float width, float height ) throws IOException
    {
        addRect(x, y, width, height);
        fill(PathIterator.WIND_NON_ZERO);
    }

    /**
     * Append a cubic Bézier curve to the current path. The curve extends from the current
     * point to the point (x3 , y3 ), using (x1 , y1 ) and (x2 , y2 ) as the Bézier control points
     * @param x1 x coordinate of the point 1
     * @param y1 y coordinate of the point 1
     * @param x2 x coordinate of the point 2
     * @param y2 y coordinate of the point 2
     * @param x3 x coordinate of the point 3
     * @param y3 y coordinate of the point 3
     * @throws IOException If there is an error while adding the .
     */
    public void addBezier312(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
    {
        appendRawCommands( formatDecimal.format( x1) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y1) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( x2) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y2) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( x3) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y3) );
        appendRawCommands( SPACE );
        appendRawCommands( BEZIER_312 );
    }

    /**
     * Append a cubic Bézier curve to the current path. The curve extends from the current
     * point to the point (x3 , y3 ), using the current point and (x2 , y2 ) as the Bézier control points
     * @param x2 x coordinate of the point 2
     * @param y2 y coordinate of the point 2
     * @param x3 x coordinate of the point 3
     * @param y3 y coordinate of the point 3
     * @throws IOException If there is an error while adding the .
     */
    public void addBezier32(float x2, float y2, float x3, float y3) throws IOException
    {
        appendRawCommands( formatDecimal.format( x2) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y2) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( x3) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y3) );
        appendRawCommands( SPACE );
        appendRawCommands( BEZIER_32 );
    }

    /**
     * Append a cubic Bézier curve to the current path. The curve extends from the current
     * point to the point (x3 , y3 ), using (x1 , y1 ) and (x3 , y3 ) as the Bézier control points
     * @param x1 x coordinate of the point 1
     * @param y1 y coordinate of the point 1
     * @param x3 x coordinate of the point 3
     * @param y3 y coordinate of the point 3
     * @throws IOException If there is an error while adding the .
     */
    public void addBezier31(float x1, float y1, float x3, float y3) throws IOException
    {
        appendRawCommands( formatDecimal.format( x1) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y1) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( x3) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y3) );
        appendRawCommands( SPACE );
        appendRawCommands( BEZIER_313 );
    }


    /**
     * Add a line to the given coordinate.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @throws IOException If there is an error while adding the line.
     */
    public void moveTo( float x, float y) throws IOException
    {
        // moveTo
        appendRawCommands( formatDecimal.format( x) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y) );
        appendRawCommands( SPACE );
        appendRawCommands( MOVE_TO );
    }

    /**
     * Add a move to the given coordinate.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @throws IOException If there is an error while adding the line.
     */
    public void lineTo( float x, float y) throws IOException
    {
        // moveTo
        appendRawCommands( formatDecimal.format( x) );
        appendRawCommands( SPACE );
        appendRawCommands( formatDecimal.format( y) );
        appendRawCommands( SPACE );
        appendRawCommands( LINE_TO );
    }
    /**
     * add a line to the current path.
     *
     * @param xStart The start x coordinate.
     * @param yStart The start y coordinate.
     * @param xEnd The end x coordinate.
     * @param yEnd The end y coordinate.
     * @throws IOException If there is an error while adding the line.
     */
    public void addLine( float xStart, float yStart, float xEnd, float yEnd ) throws IOException
    {
        // moveTo
        moveTo(xStart, yStart);
        // lineTo
        lineTo(xEnd, yEnd);
    }

    /**
     * Draw a line on the page using the current non stroking color and the current line width.
     *
     * @param xStart The start x coordinate.
     * @param yStart The start y coordinate.
     * @param xEnd The end x coordinate.
     * @param yEnd The end y coordinate.
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void drawLine( float xStart, float yStart, float xEnd, float yEnd ) throws IOException
    {
        addLine(xStart, yStart, xEnd, yEnd);
        // stroke
        stroke();
    }

    /**
     * Add a polygon to the current path.
     * @param x x coordinate of each points
     * @param y y coordinate of each points
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void addPolygon(float[] x, float[] y) throws IOException
    {
        if (x.length != y.length)
        {
            throw new IOException( "Error: some points are missing coordinate" );
        }
        for (int i = 0; i < x.length; i++)
        {
            if (i == 0)
            {
                moveTo(x[i], y[i]);
            }
            else
            {
                lineTo(x[i], y[i]);
            }
        }
        closeSubPath();
    }

    /**
     * Draw a polygon on the page using the current non stroking color.
     * @param x x coordinate of each points
     * @param y y coordinate of each points
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void drawPolygon(float[] x, float[] y) throws IOException
    {
        addPolygon(x, y);
        stroke();
    }

    /**
     * Draw and fill a polygon on the page using the current non stroking color.
     * @param x x coordinate of each points
     * @param y y coordinate of each points
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void fillPolygon(float[] x, float[] y) throws IOException
    {
        addPolygon(x, y);
        fill(PathIterator.WIND_NON_ZERO);
    }

    /**
     * Stroke the path.
     */
    public void stroke() throws IOException
    {
        appendRawCommands( STROKE );
    }

    /**
     * Close and stroke the path.
     */
    public void closeAndStroke() throws IOException
    {
        appendRawCommands( CLOSE_STROKE );
    }

    /**
     * Fill the path.
     */
    public void fill(int windingRule) throws IOException
    {
        if (windingRule == PathIterator.WIND_NON_ZERO)
        {
            appendRawCommands( FILL_NON_ZERO );
        }
        else if (windingRule == PathIterator.WIND_EVEN_ODD)
        {
            appendRawCommands( FILL_EVEN_ODD );
        }
        else
        {
            throw new IOException( "Error: unknown value for winding rule" );
        }

    }

    /**
     * Close subpath.
     */
    public void closeSubPath() throws IOException
    {
        appendRawCommands( CLOSE_SUBPATH );
    }

    /**
     * Clip path.
     */
    public void clipPath(int windingRule) throws IOException
    {
        if (windingRule == PathIterator.WIND_NON_ZERO)
        {
            appendRawCommands( CLIP_PATH_NON_ZERO );
            appendRawCommands( NOP );
        }
        else if (windingRule == PathIterator.WIND_EVEN_ODD)
        {
            appendRawCommands( CLIP_PATH_EVEN_ODD );
            appendRawCommands( NOP );
        }
        else
        {
            throw new IOException( "Error: unknown value for winding rule" );
        }
    }

    /**
     * Set linewidth to the given value.
     *
     * @param lineWidth The width which is used for drwaing.
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void setLineWidth(float lineWidth) throws IOException
    {
        appendRawCommands( formatDecimal.format( lineWidth ) );
        appendRawCommands( SPACE );
        appendRawCommands( LINE_WIDTH );
    }

    /**
     * Begin a marked content sequence.
     * @param tag the tag
     * @throws IOException if an I/O error occurs
     */
    public void beginMarkedContentSequence(COSName tag) throws IOException
    {
        appendCOSName(tag);
        appendRawCommands(SPACE);
        appendRawCommands(BMC);
    }

    /**
     * Begin a marked content sequence with a reference to an entry in the page resources'
     * Properties dictionary.
     * @param tag the tag
     * @param propsName the properties reference
     * @throws IOException if an I/O error occurs
     */
    public void beginMarkedContentSequence(COSName tag, COSName propsName) throws IOException
    {
        appendCOSName(tag);
        appendRawCommands(SPACE);
        appendCOSName(propsName);
        appendRawCommands(SPACE);
        appendRawCommands(BDC);
    }

    /**
     * End a marked content sequence.
     * @throws IOException if an I/O error occurs
     */
    public void endMarkedContentSequence() throws IOException
    {
        appendRawCommands(EMC);
    }

    /**
     * q operator. Saves the current graphics state.
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void saveGraphicsState() throws IOException
    {
        appendRawCommands( SAVE_GRAPHICS_STATE);
    }

    /**
     * Q operator. Restores the current graphics state.
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void restoreGraphicsState() throws IOException
    {
        appendRawCommands( RESTORE_GRAPHICS_STATE );
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param commands The commands to append to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void appendRawCommands( String commands ) throws IOException
    {
        appendRawCommands( commands.getBytes( "ISO-8859-1" ) );
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param commands The commands to append to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void appendRawCommands( byte[] commands ) throws IOException
    {
        output.write( commands );
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a raw byte to the stream.
     *
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void appendRawCommands( int data ) throws IOException
    {
        output.write( data );
    }

    /**
     * This will append a {@link COSName} to the content stream.
     * @param name the name
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void appendCOSName(COSName name) throws IOException
    {
        name.writePDF(output);
    }

    private void appendMatrix(AffineTransform transform) throws IOException
    {
        double[] values = new double[6];
        transform.getMatrix(values);
        for (double v : values)
        {
            appendRawCommands(formatDecimal.format(v));
            appendRawCommands(SPACE);
        }
    }

    /**
     * Close the content stream.  This must be called when you are done with this
     * object.
     * @throws IOException If the underlying stream has a problem being written to.
     */
    public void close() throws IOException
    {
        output.close();
    }
}
