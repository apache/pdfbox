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

import java.awt.Image;

import java.io.IOException;

import java.util.List;

import org.apache.fontbox.util.BoundingBox;

import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;

import org.apache.pdfbox.pdmodel.graphics.xobject.PDInlinedImage;

import org.apache.pdfbox.util.ImageParameters;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFStreamEngine;

/**
 * This class will handle creating an image for a type 3 glyph.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.10 $
 */
public class Type3StreamParser extends PDFStreamEngine
{
    private PDInlinedImage image = null;
    private BoundingBox box = null;


    /**
     * This will parse a type3 stream and create an image from it.
     *
     * @param type3Stream The stream containing the operators to draw the image.
     *
     * @return The image that was created.
     *
     * @throws IOException If there is an error processing the stream.
     */
    public Image createImage( COSStream type3Stream ) throws IOException
    {
        processStream( null, null, type3Stream );
        return image.createImage();
    }

    /**
     * This is used to handle an operation.
     *
     * @param operator The operation to perform.
     * @param arguments The list of arguments.
     *
     * @throws IOException If there is an error processing the operation.
     */
    protected void processOperator( PDFOperator operator, List arguments ) throws IOException
    {
        super.processOperator( operator, arguments );
        String operation = operator.getOperation();
        /**
        if( operation.equals( "b" ) )
        {
            //Close, fill, and stroke path using nonzero winding number rule
        }
        else if( operation.equals( "B" ) )
        {
            //Fill and stroke path using nonzero winding number rule
        }
        else if( operation.equals( "b*" ) )
        {
            //Close, fill, and stroke path using even-odd rule
        }
        else if( operation.equals( "B*" ) )
        {
            //Fill and stroke path using even-odd rule
        }
        else if( operation.equals( "BDC" ) )
        {
            //(PDF 1.2) Begin marked-content sequence with property list
        }
        else **/if( operation.equals( "BI" ) )
        {
            ImageParameters params = operator.getImageParameters();
            image = new PDInlinedImage();
            image.setImageParameters( params );
            image.setImageData( operator.getImageData() );
            //begin inline image object
        }/**
        else if( operation.equals( "BMC" ) )
        {
            //(PDF 1.2) Begin marked-content sequence
        }
        else if( operation.equals( "BT" ) )
        {
            log.debug( "<BT>" );
            textMatrix = new Matrix();
            textLineMatrix = new Matrix();
        }
        else if( operation.equals( "BX" ) )
        {
            //(PDF 1.1) Begin compatibility section
        }
        else if( operation.equals( "c" ) )
        {
            //Append curved segment to path (three control points)
        }
        else if( operation.equals( "cm" ) )
        {
        }
        else if( operation.equals( "cs" ) )
        {
        }
        else if( operation.equals( "CS" ) )
        {
        }
        else if( operation.equals( "d" ) )
        {
            //Set the line dash pattern in the graphics state
        }
        else */if( operation.equals( "d0" ) )
        {
            //set glyph with for a type3 font
            //COSNumber horizontalWidth = (COSNumber)arguments.get( 0 );
            //COSNumber verticalWidth = (COSNumber)arguments.get( 1 );
            //width = horizontalWidth.intValue();
            //height = verticalWidth.intValue();
        }
        else if( operation.equals( "d1" ) )
        {
            //set glyph with and bounding box for type 3 font
            //COSNumber horizontalWidth = (COSNumber)arguments.get( 0 );
            //COSNumber verticalWidth = (COSNumber)arguments.get( 1 );
            COSNumber llx = (COSNumber)arguments.get( 2 );
            COSNumber lly = (COSNumber)arguments.get( 3 );
            COSNumber urx = (COSNumber)arguments.get( 4 );
            COSNumber ury = (COSNumber)arguments.get( 5 );

            //width = horizontalWidth.intValue();
            //height = verticalWidth.intValue();
            box = new BoundingBox();
            box.setLowerLeftX( llx.floatValue() );
            box.setLowerLeftY( lly.floatValue() );
            box.setUpperRightX( urx.floatValue() );
            box.setUpperRightY( ury.floatValue() );
        }/*
        else if( operation.equals( "Do" ) )
        {
            //invoke named object.
        }
        else if( operation.equals( "DP" ) )
        {
            //(PDF 1.2) De.ne marked-content point with property list
        }
        else if( operation.equals( "EI" ) )
        {
            //end inline image object
        }
        else if( operation.equals( "EMC" ) )
        {
            //End inline image object
        }
        else if( operation.equals( "ET" ) )
        {
            log.debug( "<ET>" );
            textMatrix = null;
            textLineMatrix = null;
        }
        else if( operation.equals( "EX" ) )
        {
            //(PDF 1.1) End compatibility section
        }
        else if( operation.equals( "f" ) )
        {
            //Fill the path, using the nonzero winding number rule to determine the region to .ll
        }
        else if( operation.equals( "F" ) )
        {
        }
        else if( operation.equals( "f*" ) )
        {
            //Fill path using even-odd rule
        }
        else if( operation.equals( "g" ) )
        {
        }
        else if( operation.equals( "G" ) )
        {
        }
        else if( operation.equals( "gs" ) )
        {
        }
        else if( operation.equals( "h" ) )
        {
            //close subpath
        }
        else if( operation.equals( "i" ) )
        {
            //set flatness tolerance, not sure what this does
        }
        else if( operation.equals( "ID" ) )
        {
            //begin inline image data
        }
        else if( operation.equals( "j" ) )
        {
            //Set the line join style in the graphics state
            //System.out.println( "<j>" );
        }
        else if( operation.equals( "J" ) )
        {
            //Set the line cap style in the graphics state
            //System.out.println( "<J>" );
        }
        else if( operation.equals( "k" ) )
        {
            //Set CMYK color for nonstroking operations
        }
        else if( operation.equals( "K" ) )
        {
            //Set CMYK color for stroking operations
        }
        else if( operation.equals( "l" ) )
        {
            //append straight line segment from the current point to the point.
            COSNumber x = (COSNumber)arguments.get( 0 );
            COSNumber y = (COSNumber)arguments.get( 1 );
            linePath.lineTo( x.floatValue(), pageSize.getHeight()-y.floatValue() );
        }
        else if( operation.equals( "m" ) )
        {
            COSNumber x = (COSNumber)arguments.get( 0 );
            COSNumber y = (COSNumber)arguments.get( 1 );
            linePath.reset();
            linePath.moveTo( x.floatValue(), pageSize.getHeight()-y.floatValue() );
            //System.out.println( "<m x=\"" + x.getValue() + "\" y=\"" + y.getValue() + "\" >" );
        }
        else if( operation.equals( "M" ) )
        {
            //System.out.println( "<M>" );
        }
        else if( operation.equals( "MP" ) )
        {
            //(PDF 1.2) Define marked-content point
        }
        else if( operation.equals( "n" ) )
        {
            //End path without .lling or stroking
            //System.out.println( "<n>" );
        }
        else if( operation.equals( "q" ) )
        {
            //save graphics state
            if( log.isDebugEnabled() )
            {
                log.debug( "<" + operation + "> - save state" );
            }
            graphicsStack.push(graphicsState.clone());
        }
        else if( operation.equals( "Q" ) )
        {
            //restore graphics state
            if( log.isDebugEnabled() )
            {
                log.debug( "<" + operation + "> - restore state" );
            }
            graphicsState = (PDGraphicsState)graphicsStack.pop();
        }
        else if( operation.equals( "re" ) )
        {
        }
        else if( operation.equals( "rg" ) )
        {
            //Set RGB color for nonstroking operations
        }
        else if( operation.equals( "RG" ) )
        {
            //Set RGB color for stroking operations
        }
        else if( operation.equals( "ri" ) )
        {
            //Set color rendering intent
        }
        else if( operation.equals( "s" ) )
        {
            //Close and stroke path
        }
        else if( operation.equals( "S" ) )
        {
            graphics.draw( linePath );
        }
        else if( operation.equals( "sc" ) )
        {
            //set color for nonstroking operations
            //System.out.println( "<sc>" );
        }
        else if( operation.equals( "SC" ) )
        {
            //set color for stroking operations
            //System.out.println( "<SC>" );
        }
        else if( operation.equals( "scn" ) )
        {
            //set color for nonstroking operations special
        }
        else if( operation.equals( "SCN" ) )
        {
            //set color for stroking operations special
        }
        else if( operation.equals( "sh" ) )
        {
            //(PDF 1.3) Paint area de.ned by shading pattern
        }
        else if( operation.equals( "T*" ) )
        {
            if (log.isDebugEnabled())
            {
                log.debug("<T* graphicsState.getTextState().getLeading()=\"" +
                    graphicsState.getTextState().getLeading() + "\">");
            }
            //move to start of next text line
            if( graphicsState.getTextState().getLeading() == 0 )
            {
                graphicsState.getTextState().setLeading( -.01f );
            }
            Matrix td = new Matrix();
            td.setValue( 2, 1, -1 * graphicsState.getTextState().getLeading() * textMatrix.getValue(1,1));
            textLineMatrix = textLineMatrix.multiply( td );
            textMatrix = textLineMatrix.copy();
        }
        else if( operation.equals( "Tc" ) )
        {
            //set character spacing
            COSNumber characterSpacing = (COSNumber)arguments.get( 0 );
            if (log.isDebugEnabled())
            {
                log.debug("<Tc characterSpacing=\"" + characterSpacing.floatValue() + "\" />");
            }
            graphicsState.getTextState().setCharacterSpacing( characterSpacing.floatValue() );
        }
        else if( operation.equals( "Td" ) )
        {
            COSNumber x = (COSNumber)arguments.get( 0 );
            COSNumber y = (COSNumber)arguments.get( 1 );
            if (log.isDebugEnabled())
            {
                log.debug("<Td x=\"" + x.floatValue() + "\" y=\"" + y.floatValue() + "\">");
            }
            Matrix td = new Matrix();
            td.setValue( 2, 0, x.floatValue() * textMatrix.getValue(0,0) );
            td.setValue( 2, 1, y.floatValue() * textMatrix.getValue(1,1) );
            //log.debug( "textLineMatrix before " + textLineMatrix );
            textLineMatrix = textLineMatrix.multiply( td );
            //log.debug( "textLineMatrix after " + textLineMatrix );
            textMatrix = textLineMatrix.copy();
        }
        else if( operation.equals( "TD" ) )
        {
            //move text position and set leading
            COSNumber x = (COSNumber)arguments.get( 0 );
            COSNumber y = (COSNumber)arguments.get( 1 );
            if (log.isDebugEnabled())
            {
                log.debug("<TD x=\"" + x.floatValue() + "\" y=\"" + y.floatValue() + "\">");
            }
            graphicsState.getTextState().setLeading( -1 * y.floatValue() );
            Matrix td = new Matrix();
            td.setValue( 2, 0, x.floatValue() * textMatrix.getValue(0,0) );
            td.setValue( 2, 1, y.floatValue() * textMatrix.getValue(1,1) );
            //log.debug( "textLineMatrix before " + textLineMatrix );
            textLineMatrix = textLineMatrix.multiply( td );
            //log.debug( "textLineMatrix after " + textLineMatrix );
            textMatrix = textLineMatrix.copy();
        }
        else if( operation.equals( "Tf" ) )
        {
            //set font and size
            COSName fontName = (COSName)arguments.get( 0 );
            graphicsState.getTextState().setFontSize( ((COSNumber)arguments.get( 1 ) ).floatValue() );

            if (log.isDebugEnabled())
            {
                log.debug("<Tf font=\"" + fontName.getName() + "\" size=\"" +
                    graphicsState.getTextState().getFontSize() + "\">");
            }

            //old way
            //graphicsState.getTextState().getFont() = (COSObject)stream.getDictionaryObject( fontName );
            //if( graphicsState.getTextState().getFont() == null )
            //{
            //    graphicsState.getTextState().getFont() = (COSObject)graphicsState.getTextState().getFont()
            //                                           Dictionary.getItem( fontName );
            //}
            graphicsState.getTextState().setFont( (PDFont)fonts.get( fontName.getName() ) );
            if( graphicsState.getTextState().getFont() == null )
            {
                throw new IOException( "Error: Could not find font(" + fontName + ") in map=" + fonts );
            }
            //log.debug( "Font Resource=" + fontResource );
            //log.debug( "Current Font=" + graphicsState.getTextState().getFont() );
            //log.debug( "graphicsState.getTextState().getFontSize()=" + graphicsState.getTextState().getFontSize() );
        }
        else if( operation.equals( "Tj" ) )
        {
            COSString string = (COSString)arguments.get( 0 );
            TextPosition pos = showString( string.getBytes() );
            if (log.isDebugEnabled())
            {
                log.debug("<Tj string=\"" + string.getString() + "\">");
            }
        }
        else if( operation.equals( "TJ" ) )
        {
            Matrix td = new Matrix();

            COSArray array = (COSArray)arguments.get( 0 );
            for( int i=0; i<array.size(); i++ )
            {
                COSBase next = array.get( i );
                if( next instanceof COSNumber )
                {
                    float value = -1*
                                  (((COSNumber)next).floatValue()/1000) *
                                  graphicsState.getTextState().getFontSize() *
                                  textMatrix.getValue(1,1);

                    if (log.isDebugEnabled())
                    {
                        log.debug( "<TJ(" + i + ") value=\"" + value +
                                   "\", param=\"" + ((COSNumber)next).floatValue() +
                                   "\", fontsize=\"" + graphicsState.getTextState().getFontSize() + "\">" );
                    }
                    td.setValue( 2, 0, value );
                    textMatrix = textMatrix.multiply( td );
                }
                else if( next instanceof COSString )
                {
                    TextPosition pos = showString( ((COSString)next).getBytes() );
                    if (log.isDebugEnabled())
                    {
                        log.debug("<TJ(" + i + ") string=\"" + pos.getString() + "\">");
                    }
                }
                else
                {
                    throw new IOException( "Unknown type in array for TJ operation:" + next );
                }
            }
        }
        else if( operation.equals( "TL" ) )
        {
            COSNumber leading = (COSNumber)arguments.get( 0 );
            graphicsState.getTextState().setLeading( leading.floatValue() );
            if (log.isDebugEnabled())
            {
                log.debug("<TL leading=\"" + leading.floatValue() + "\" >");
            }
        }
        else if( operation.equals( "Tm" ) )
        {
            //Set text matrix and text line matrix
            COSNumber a = (COSNumber)arguments.get( 0 );
            COSNumber b = (COSNumber)arguments.get( 1 );
            COSNumber c = (COSNumber)arguments.get( 2 );
            COSNumber d = (COSNumber)arguments.get( 3 );
            COSNumber e = (COSNumber)arguments.get( 4 );
            COSNumber f = (COSNumber)arguments.get( 5 );

            if (log.isDebugEnabled())
            {
                log.debug("<Tm " +
                          "a=\"" + a.floatValue() + "\" " +
                          "b=\"" + b.floatValue() + "\" " +
                          "c=\"" + c.floatValue() + "\" " +
                          "d=\"" + d.floatValue() + "\" " +
                          "e=\"" + e.floatValue() + "\" " +
                          "f=\"" + f.floatValue() + "\" >");
            }

            textMatrix = new Matrix();
            textMatrix.setValue( 0, 0, a.floatValue() );
            textMatrix.setValue( 0, 1, b.floatValue() );
            textMatrix.setValue( 1, 0, c.floatValue() );
            textMatrix.setValue( 1, 1, d.floatValue() );
            textMatrix.setValue( 2, 0, e.floatValue() );
            textMatrix.setValue( 2, 1, f.floatValue() );
            textLineMatrix = textMatrix.copy();
        }
        else if( operation.equals( "Tr" ) )
        {
            //Set text rendering mode
            //System.out.println( "<Tr>" );
        }
        else if( operation.equals( "Ts" ) )
        {
            //Set text rise
            //System.out.println( "<Ts>" );
        }
        else if( operation.equals( "Tw" ) )
        {
            //set word spacing
            COSNumber wordSpacing = (COSNumber)arguments.get( 0 );
            if (log.isDebugEnabled())
            {
                log.debug("<Tw wordSpacing=\"" + wordSpacing.floatValue() + "\" />");
            }
            graphicsState.getTextState().setWordSpacing( wordSpacing.floatValue() );
        }
        else if( operation.equals( "Tz" ) )
        {
            //Set horizontal text scaling
        }
        else if( operation.equals( "v" ) )
        {
            //Append curved segment to path (initial point replicated)
        }
        else if( operation.equals( "w" ) )
        {
            //Set the line width in the graphics state
            //System.out.println( "<w>" );
        }
        else if( operation.equals( "W" ) )
        {
            //Set clipping path using nonzero winding number rule
            //System.out.println( "<W>" );
        }
        else if( operation.equals( "W*" ) )
        {
            //Set clipping path using even-odd rule
        }
        else if( operation.equals( "y" ) )
        {
            //Append curved segment to path (final point replicated)
        }
        else if( operation.equals( "'" ) )
        {
            // Move to start of next text line, and show text
            //
            COSString string = (COSString)arguments.get( 0 );
            if (log.isDebugEnabled())
            {
                log.debug("<' string=\"" + string.getString() + "\">");
            }

            Matrix td = new Matrix();
            td.setValue( 2, 1, -1 * graphicsState.getTextState().getLeading() * textMatrix.getValue(1,1));
            textLineMatrix = textLineMatrix.multiply( td );
            textMatrix = textLineMatrix.copy();

            showString( string.getBytes() );
        }
        else if( operation.equals( "\"" ) )
        {
            //Set word and character spacing, move to next line, and show text
            //
            COSNumber wordSpacing = (COSNumber)arguments.get( 0 );
            COSNumber characterSpacing = (COSNumber)arguments.get( 1 );
            COSString string = (COSString)arguments.get( 2 );

            if (log.isDebugEnabled())
            {
                log.debug("<\" wordSpacing=\"" + wordSpacing +
                          "\", characterSpacing=\"" + characterSpacing +
                          "\", string=\"" + string.getString() + "\">");
            }

            graphicsState.getTextState().setCharacterSpacing( characterSpacing.floatValue() );
            graphicsState.getTextState().setWordSpacing( wordSpacing.floatValue() );

            Matrix td = new Matrix();
            td.setValue( 2, 1, -1 * graphicsState.getTextState().getLeading() * textMatrix.getValue(1,1));
            textLineMatrix = textLineMatrix.multiply( td );
            textMatrix = textLineMatrix.copy();

            showString( string.getBytes() );
        }*/
    }


}
