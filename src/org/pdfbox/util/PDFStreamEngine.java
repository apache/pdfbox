/**
 * Copyright (c) 2003-2007, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.util;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.pdfbox.cos.COSObject;
import org.pdfbox.cos.COSStream;
import org.pdfbox.exceptions.WrappedIOException;

import org.pdfbox.pdmodel.PDPage;
import org.pdfbox.pdmodel.PDResources;

import org.pdfbox.pdmodel.font.PDFont;

import org.pdfbox.pdmodel.graphics.PDGraphicsState;

import org.pdfbox.util.operator.OperatorProcessor;

/**
 * This class will run through a PDF content stream and execute certain operations
 * and provide a callback interface for clients that want to do things with the stream.
 * See the PDFTextStripper class for an example of how to use this class.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.38 $
 */
public class PDFStreamEngine
{
    private static final byte[] SPACE_BYTES = { (byte)32 };

    private PDGraphicsState graphicsState = null;

    private Matrix textMatrix = null;
    private Matrix textLineMatrix = null;
    private Stack graphicsStack = new Stack();
    //private PDResources resources = null;
    
    private Map operators = new HashMap();
    
    private Stack streamResourcesStack = new Stack();
    
    private PDPage page;
    
    private Map documentFontCache = new HashMap();
    
    /**
     * This is a simple internal class used by the Stream engine to handle the 
     * resources stack.
     */
    private static class StreamResources
    {
        private Map fonts;
        private Map colorSpaces;
        private Map xobjects;
        private Map graphicsStates;
        private PDResources resources;
    }

    /**
     * Constructor.
     */
    public PDFStreamEngine()
    {
        //default constructor
    }
    
    /**
     * Constructor with engine properties.  The property keys are all
     * PDF operators, the values are class names used to execute those
     * operators.
     * 
     * @param properties The engine properties.
     * 
     * @throws IOException If there is an error setting the engine properties.
     */
    public PDFStreamEngine( Properties properties ) throws IOException
    {
        if( properties == null ) {
            throw new NullPointerException( "properties cannot be null" );
        }
        try
        {
            Iterator keys = properties.keySet().iterator();
            while( keys.hasNext() )
            {
                String operator = (String)keys.next();
                String operatorClass = properties.getProperty( operator );
                OperatorProcessor op = (OperatorProcessor)Class.forName( operatorClass ).newInstance();
                registerOperatorProcessor(operator, op);
            }
        }
        catch( Exception e )
        {
            throw new WrappedIOException( e );
        }
    }
    
    /**
     * Register a custom operator processor with the engine.
     * 
     * @param operator The operator as a string.
     * @param op Processor instance.
     */
    public void registerOperatorProcessor( String operator, OperatorProcessor op )
    {
        op.setContext( this );
        operators.put( operator, op );
    }
    
    /**
     * This method must be called between processing documents.  The 
     * PDFStreamEngine caches information for the document between pages
     * and this will release the cached information.  This only needs
     * to be called if processing a new document.
     *
     */
    public void resetEngine()
    {
        documentFontCache.clear();
    }

    /**
     * This will process the contents of the stream.
     *
     * @param aPage The page.
     * @param resources The location to retrieve resources.
     * @param cosStream the Stream to execute.
     * 
     *
     * @throws IOException if there is an error accessing the stream.
     */
    public void processStream( PDPage aPage, PDResources resources, COSStream cosStream ) throws IOException
    {
        graphicsState = new PDGraphicsState();
        textMatrix = null;
        textLineMatrix = null;
        graphicsStack.clear();
        streamResourcesStack.clear();
        
        processSubStream( aPage, resources, cosStream );
    }
    
    /**
     * Process a sub stream of the current stream.
     * 
     * @param aPage The page used for drawing.
     * @param resources The resources used when processing the stream.
     * @param cosStream The stream to process.
     * 
     * @throws IOException If there is an exception while processing the stream.
     */
    public void processSubStream( PDPage aPage, PDResources resources, COSStream cosStream ) throws IOException
    {
        page = aPage;
        if( resources != null )
        {
            StreamResources sr = new StreamResources();
            sr.fonts = resources.getFonts( documentFontCache );
            sr.colorSpaces = resources.getColorSpaces();
            sr.xobjects = resources.getXObjects();
            sr.graphicsStates = resources.getGraphicsStates();
            sr.resources = resources;
            streamResourcesStack.push(sr);
        }
        try
        {
            List arguments = new ArrayList();
            List tokens = cosStream.getStreamTokens();
            if( tokens != null )
            {
                Iterator iter = tokens.iterator();
                while( iter.hasNext() )
                {
                    Object next = iter.next();
                    if( next instanceof COSObject )
                    {
                        arguments.add( ((COSObject)next).getObject() );
                    }
                    else if( next instanceof PDFOperator )
                    {
                        processOperator( (PDFOperator)next, arguments );
                        arguments = new ArrayList();
                    }
                    else
                    {
                        arguments.add( next );
                    }
                }
            }
        }
        finally
        {
            if( resources != null )
            {
                streamResourcesStack.pop();
            }
        }
        
    }

    /**
     * A method provided as an event interface to allow a subclass to perform
     * some specific functionality when a character needs to be displayed.
     *
     * @param text The character to be displayed.
     */
    protected void showCharacter( TextPosition text )
    {
        //subclasses can override to provide specific functionality.
    }

    /**
     * You should override this method if you want to perform an action when a
     * string is being shown.
     *
     * @param string The string to display.
     *
     * @throws IOException If there is an error showing the string
     */
    
    public void showString( byte[] string ) throws IOException
    {
        float[] individualWidths = new float[2048];
        float spaceWidth = 0;
        float spacing = 0;
        StringBuffer stringResult = new StringBuffer(string.length);
        
        float characterHorizontalDisplacement = 0;
        float characterVerticalDisplacement = 0;
        float spaceDisplacement = 0;
        float fontSize = graphicsState.getTextState().getFontSize();
        float horizontalScaling = graphicsState.getTextState().getHorizontalScalingPercent()/100f;
        float verticalScaling = horizontalScaling;//not sure if this is right but what else to do???
        float rise = graphicsState.getTextState().getRise();
        final float wordSpacing = graphicsState.getTextState().getWordSpacing();
        final float characterSpacing = graphicsState.getTextState().getCharacterSpacing();
        float wordSpacingDisplacement = 0;
        //We won't know the actual number of characters until
        //we process the byte data(could be two bytes each) but
        //it won't ever be more than string.length*2(there are some cases
        //were a single byte will result in two output characters "fi"
        
        
        PDFont font = graphicsState.getTextState().getFont();
        
        //This will typically be 1000 but in the case of a type3 font
        //this might be a different number
        float glyphSpaceToTextSpaceFactor = 1f/font.getFontMatrix().getValue( 0, 0 );
        float averageWidth = font.getAverageFontWidth();

        Matrix initialMatrix = new Matrix();
        initialMatrix.setValue(0,0,1);
        initialMatrix.setValue(0,1,0);
        initialMatrix.setValue(0,2,0);
        initialMatrix.setValue(1,0,0);
        initialMatrix.setValue(1,1,1);
        initialMatrix.setValue(1,2,0);
        initialMatrix.setValue(2,0,0);
        initialMatrix.setValue(2,1,rise);
        initialMatrix.setValue(2,2,1);


        //this
        int codeLength = 1;
        Matrix ctm = graphicsState.getCurrentTransformationMatrix();
        
        //lets see what the space displacement should be
        spaceDisplacement = (font.getFontWidth( SPACE_BYTES, 0, 1 )/glyphSpaceToTextSpaceFactor);
        if( spaceDisplacement == 0 )
        {
            spaceDisplacement = (averageWidth/glyphSpaceToTextSpaceFactor);
            //The average space width appears to be higher than necessary
            //so lets make it a little bit smaller.
            spaceDisplacement *= .80f;
        }
        int pageRotation = page.findRotation();
        Matrix trm = initialMatrix.multiply( textMatrix ).multiply( ctm );
        float x = trm.getValue(2,0);
        float y = trm.getValue(2,1);
        if( pageRotation == 0 )
        {
            trm.setValue( 2,1, -y + page.findMediaBox().getHeight() );
        }
        else if( pageRotation == 90 || pageRotation == -270 )
        {
            trm.setValue( 2,0, y );
            trm.setValue( 2,1, x );
        }
        else if( pageRotation == 270 || pageRotation == -90 )
        {
            trm.setValue( 2,0, -y  + page.findMediaBox().getHeight() );
            trm.setValue( 2,1, x );
        }
        float xScale = trm.getXScale();
        float yScale = trm.getYScale(); 
        float xPos = trm.getXPosition();
        float yPos = trm.getYPosition();
        spaceWidth = spaceDisplacement * xScale * fontSize;
        wordSpacingDisplacement = wordSpacing*xScale * fontSize;
        float totalStringWidth = 0;
        for( int i=0; i<string.length; i+=codeLength )
        {
            
            codeLength = 1;

            String c = font.encode( string, i, codeLength );
            if( c == null && i+1<string.length)
            {
                //maybe a multibyte encoding
                codeLength++;
                c = font.encode( string, i, codeLength );
            }

            //todo, handle horizontal displacement
            characterHorizontalDisplacement = (font.getFontWidth( string, i, codeLength )/glyphSpaceToTextSpaceFactor); 
            characterVerticalDisplacement = 
                Math.max( 
                    characterVerticalDisplacement, 
                    font.getFontHeight( string, i, codeLength)/glyphSpaceToTextSpaceFactor);


            // PDF Spec - 5.5.2 Word Spacing
            //
            // Word spacing works the same was as character spacing, but applies
            // only to the space character, code 32.
            //
            // Note: Word spacing is applied to every occurrence of the single-byte
            // character code 32 in a string.  This can occur when using a simple
            // font or a composite font that defines code 32 as a single-byte code.
            // It does not apply to occurrences of the byte value 32 in multiple-byte
            // codes.
            //
            // RDD - My interpretation of this is that only character code 32's that
            // encode to spaces should have word spacing applied.  Cases have been
            // observed where a font has a space character with a character code
            // other than 32, and where word spacing (Tw) was used.  In these cases,
            // applying word spacing to either the non-32 space or to the character
            // code 32 non-space resulted in errors consistent with this interpretation.
            //
            if( (string[i] == 0x20) && c.equals( " " ) )
            {
                spacing = wordSpacing + characterSpacing;
            }
            else
            {
                spacing = characterSpacing;
            }
            
            // We want to update the textMatrix using the width, in text space units.
            //
            //The adjustment will always be zero.  The adjustment as shown in the
            //TJ operator will be handled separately.
            float adjustment=0;
            //todo, need to compute the vertical displacement
            float ty = 0;
            float tx = ((characterHorizontalDisplacement-adjustment/glyphSpaceToTextSpaceFactor)*fontSize + spacing)
                       *horizontalScaling;
            
            
            Matrix td = new Matrix();
            td.setValue( 2, 0, tx );
            td.setValue( 2, 1, ty );            
            
            float xPosBefore = textMatrix.getXPosition();
            float yPosBefore = textMatrix.getYPosition();
            textMatrix = td.multiply( textMatrix );

            float width = 0;
            if( pageRotation == 0 )
            {
                width = (textMatrix.getXPosition() - xPosBefore);
            }
            else if( pageRotation == 90 || pageRotation == -270)
            {
                width = (textMatrix.getYPosition() - yPosBefore);
            }
            else if( pageRotation == 270 || pageRotation == -90 )
            {
                width = (yPosBefore - textMatrix.getYPosition());
            }
            //there are several cases where one character code will
            //output multiple characters.  For example "fi" or a
            //glyphname that has no mapping like "visiblespace"
            if( c != null )
            {
                float widthOfEachCharacterForCode = width/c.length();
                for( int j=0; j<c.length(); j++)
                {
                    if( stringResult.length()+j <individualWidths.length )
                    {
                        if( c.equals("-"))
                        {
                            //System.out.println( "stringResult.length()+j=" + (widthOfEachCharacterForCode));
                        }
                        individualWidths[stringResult.length()+j] = widthOfEachCharacterForCode;
                    }
                }
            }
            
            totalStringWidth += width;
            stringResult.append( c );
        }
        float totalStringHeight = characterVerticalDisplacement * fontSize * yScale;
        String resultingString = stringResult.toString();
        
        if( individualWidths.length != resultingString.length() )
        {
            float[] tmp = new float[resultingString.length()];
            System.arraycopy( individualWidths, 0, tmp, 0, Math.min( individualWidths.length, resultingString.length() ));
            individualWidths = tmp;
            if( resultingString.equals( "- " ))
            {
                //System.out.println( "EQUALS " + individualWidths[0] );
            }
        }
        showCharacter(
                new TextPosition(
                    xPos,
                    yPos,
                    xScale,
                    yScale,
                    totalStringWidth,
                    individualWidths,
                    totalStringHeight,
                    spaceWidth,
                    stringResult.toString(),
                    font,
                    fontSize,
                    wordSpacingDisplacement ));
    }
    
    /**
     * This is used to handle an operation.
     *
     * @param operation The operation to perform.
     * @param arguments The list of arguments.
     *
     * @throws IOException If there is an error processing the operation.
     */
    public void processOperator( String operation, List arguments ) throws IOException
    {
        PDFOperator oper = PDFOperator.getOperator( operation );
        processOperator( oper, arguments );
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
        String operation = operator.getOperation();
        OperatorProcessor processor = (OperatorProcessor)operators.get( operation );
        if( processor != null )
        {
            processor.process( operator, arguments );
        }
    } 
   
    /**
     * @return Returns the colorSpaces.
     */
    public Map getColorSpaces() 
    {
        return ((StreamResources) streamResourcesStack.peek()).colorSpaces;
    }
    
    /**
     * @return Returns the colorSpaces.
     */
    public Map getXObjects() 
    {
        return ((StreamResources) streamResourcesStack.peek()).xobjects;
    }
    
    /**
     * @param value The colorSpaces to set.
     */
    public void setColorSpaces(Map value) 
    {
        ((StreamResources) streamResourcesStack.peek()).colorSpaces = value;
    }
    /**
     * @return Returns the fonts.
     */
    public Map getFonts() 
    {
        return ((StreamResources) streamResourcesStack.peek()).fonts;
    }
    /**
     * @param value The fonts to set.
     */
    public void setFonts(Map value) 
    {
        ((StreamResources) streamResourcesStack.peek()).fonts = value;
    }
    /**
     * @return Returns the graphicsStack.
     */
    public Stack getGraphicsStack() 
    {
        return graphicsStack;
    }
    /**
     * @param value The graphicsStack to set.
     */
    public void setGraphicsStack(Stack value) 
    {
        graphicsStack = value;
    }
    /**
     * @return Returns the graphicsState.
     */
    public PDGraphicsState getGraphicsState() 
    {
        return graphicsState;
    }
    /**
     * @param value The graphicsState to set.
     */
    public void setGraphicsState(PDGraphicsState value) 
    {
        graphicsState = value;
    }
    /**
     * @return Returns the graphicsStates.
     */
    public Map getGraphicsStates() 
    {
        return ((StreamResources) streamResourcesStack.peek()).graphicsStates;
    }
    /**
     * @param value The graphicsStates to set.
     */
    public void setGraphicsStates(Map value) 
    {
        ((StreamResources) streamResourcesStack.peek()).graphicsStates = value;
    }
    /**
     * @return Returns the textLineMatrix.
     */
    public Matrix getTextLineMatrix() 
    {
        return textLineMatrix;
    }
    /**
     * @param value The textLineMatrix to set.
     */
    public void setTextLineMatrix(Matrix value) 
    {
        textLineMatrix = value;
    }
    /**
     * @return Returns the textMatrix.
     */
    public Matrix getTextMatrix() 
    {
        return textMatrix;
    }
    /**
     * @param value The textMatrix to set.
     */
    public void setTextMatrix(Matrix value) 
    {
        textMatrix = value;
    }
    /**
     * @return Returns the resources.
     */
    public PDResources getResources()
    {
        return ((StreamResources) streamResourcesStack.peek()).resources;
    }
    
    /**
     * Get the current page that is being processed.
     * 
     * @return The page being processed.
     */
    public PDPage getCurrentPage()
    {
        return page;
    }
}