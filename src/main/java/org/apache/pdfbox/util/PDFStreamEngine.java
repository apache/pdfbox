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
package org.apache.pdfbox.util;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.WrappedIOException;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;

import org.apache.pdfbox.pdmodel.font.PDFont;

import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;

import org.apache.pdfbox.util.operator.OperatorProcessor;

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

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDFStreamEngine.class);

    private Vector unsupportedOperators = new Vector();
    
    private static final byte[] SPACE_BYTES = { (byte)32 };

    private PDGraphicsState graphicsState = null;

    private Matrix textMatrix = null;
    private Matrix textLineMatrix = null;
    private Stack graphicsStack = new Stack();

    private Map operators = new HashMap();

    private Stack streamResourcesStack = new Stack();

    private PDPage page;

    private Map documentFontCache = new HashMap();
    
    private int validCharCnt;
    private int totalCharCnt;
    
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
        
        private StreamResources()
        {};
    }

    /**
     * Constructor.
     */
    public PDFStreamEngine()
    {
        //default constructor
        validCharCnt = 0;
        totalCharCnt = 0;
        
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
        if( properties == null ) 
        {
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
        validCharCnt = 0;
        totalCharCnt = 0;
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
        validCharCnt = 0;
        totalCharCnt = 0;
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
                    if(log.isDebugEnabled())
                    {
                        log.debug("token: " + next);
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
     * some specific functionality when text needs to be processed.
     *
     * @param text The text to be processed.
     */
    protected void processTextPosition( TextPosition text )
    {
        //subclasses can override to provide specific functionality.
    }

    
    /**
     * Process encoded text from the PDF Stream. 
     * You should override this method if you want to perform an action when 
     * encoded text is being processed.
     *
     * @param string The encoded text
     *
     * @throws IOException If there is an error processing the string
     */
    public void processEncodedText( byte[] string ) throws IOException
    {
        /* Note on variable names.  There are three different units being used
         * in this code.  Character sizes are given in glyph units, text locations
         * are initially given in text units, and we want to save the data in 
         * display units. The variable names should end with Text or Disp to 
         * represent if the values are in text or disp units (no glyph units are saved).
         */
        final float fontSizeText = graphicsState.getTextState().getFontSize();
        final float horizontalScalingText = graphicsState.getTextState().getHorizontalScalingPercent()/100f;
        //float verticalScalingText = horizontalScaling;//not sure if this is right but what else to do???
        final float riseText = graphicsState.getTextState().getRise();
        final float wordSpacingText = graphicsState.getTextState().getWordSpacing();
        final float characterSpacingText = graphicsState.getTextState().getCharacterSpacing();
        
        //We won't know the actual number of characters until
        //we process the byte data(could be two bytes each) but
        //it won't ever be more than string.length*2(there are some cases
        //were a single byte will result in two output characters "fi"
        
        final PDFont font = graphicsState.getTextState().getFont();
        
        //This will typically be 1000 but in the case of a type3 font
        //this might be a different number
        final float glyphSpaceToTextSpaceFactor = 1f/font.getFontMatrix().getValue( 0, 0 );
        

        // lets see what the space displacement should be
        float spaceWidthText = (font.getFontWidth( SPACE_BYTES, 0, 1 )/glyphSpaceToTextSpaceFactor);
        if( spaceWidthText == 0 )
        {
            spaceWidthText = (font.getAverageFontWidth()/glyphSpaceToTextSpaceFactor);
            //The average space width appears to be higher than necessary
            //so lets make it a little bit smaller.
            spaceWidthText *= .80f;
        }
        
        
        /* Convert textMatrix to display units */
        Matrix initialMatrix = new Matrix();
        initialMatrix.setValue(0,0,1);
        initialMatrix.setValue(0,1,0);
        initialMatrix.setValue(0,2,0);
        initialMatrix.setValue(1,0,0);
        initialMatrix.setValue(1,1,1);
        initialMatrix.setValue(1,2,0);
        initialMatrix.setValue(2,0,0);
        initialMatrix.setValue(2,1,riseText);
        initialMatrix.setValue(2,2,1);
    
        final Matrix ctm = graphicsState.getCurrentTransformationMatrix();
        final Matrix textMatrixStDisp = initialMatrix.multiply( textMatrix ).multiply( ctm );
        
        final float xScaleDisp = textMatrixStDisp.getXScale();
        final float yScaleDisp = textMatrixStDisp.getYScale(); 
        
        final float spaceWidthDisp = spaceWidthText * xScaleDisp * fontSizeText;
        final float wordSpacingDisp = wordSpacingText * xScaleDisp * fontSizeText; 
        
        float maxVerticalDisplacementText = 0;
        float[] individualWidthsText = new float[2048];
        StringBuffer stringResult = new StringBuffer(string.length);
        
        int codeLength = 1;
        for( int i=0; i<string.length; i+=codeLength )
        {
            // Decode the value to a Unicode character
            codeLength = 1;
            String c = font.encode( string, i, codeLength );
            if( c == null && i+1<string.length)
            {
                //maybe a multibyte encoding
                codeLength++;
                c = font.encode( string, i, codeLength );
            }

            //todo, handle horizontal displacement
            // get the width and height of this character in text units 
            float characterHorizontalDisplacementText = 
                (font.getFontWidth( string, i, codeLength )/glyphSpaceToTextSpaceFactor); 
            maxVerticalDisplacementText = 
                Math.max( 
                    maxVerticalDisplacementText, 
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
            float spacingText = characterSpacingText;
            if( (string[i] == 0x20) && c != null && c.equals( " " ) )
            {
                spacingText += wordSpacingText;
            }

            // get the X location before we update the text matrix
            float xPosBeforeText = initialMatrix.multiply( textMatrix ).multiply( ctm ).getXPosition();

            /* The text matrix gets updated after each glyph is placed.  The updated
             * version will have the X and Y coordinates for the next glyph.
             */
            
            //The adjustment will always be zero.  The adjustment as shown in the
            //TJ operator will be handled separately.
            float adjustment=0;
            // TODO : tx should be set for horizontal text and ty for vertical text
            // which seems to be specified in the font (not the direction in the matrix).
            float tx = ((characterHorizontalDisplacementText-adjustment/glyphSpaceToTextSpaceFactor)*fontSizeText 
                    + spacingText) * horizontalScalingText;
            float ty = 0;              
            
            Matrix td = new Matrix();
            td.setValue( 2, 0, tx );
            td.setValue( 2, 1, ty );            
            
            textMatrix = td.multiply( textMatrix );

            // determine the width of this character
            // XXX: Note that if we handled vertical text, we should be using Y here
            
            float widthText = initialMatrix.multiply( textMatrix ).multiply( ctm ).getXPosition() - xPosBeforeText;
            
            //there are several cases where one character code will
            //output multiple characters.  For example "fi" or a
            //glyphname that has no mapping like "visiblespace"
            if( c != null )
            {
                // assume each character is the same size
                float widthOfEachCharacterForCode = widthText/c.length();
                
                for( int j=0; j<c.length(); j++)
                {
                    if( stringResult.length()+j <individualWidthsText.length )
                    {
                        if( c.equals("-"))
                        {
                            //System.out.println( "stringResult.length()+j=" + (widthOfEachCharacterForCode));
                        }
                        individualWidthsText[stringResult.length()+j] = widthOfEachCharacterForCode;
                    }
                }
                validCharCnt += c.length();
            }
            else 
            {
                // PDFBOX-373: Replace a null entry with "?" so it is
                // not printed as "(null)"
                c = "?";
            }
            totalCharCnt += c.length();
            
            stringResult.append( c );
        }
        
        
        String resultingString = stringResult.toString();
        
        if( individualWidthsText.length != resultingString.length() )
        {
            float[] tmp = new float[resultingString.length()];
            System.arraycopy( individualWidthsText, 0, tmp, 0, 
                    Math.min( individualWidthsText.length, resultingString.length() ));
            individualWidthsText = tmp;
            if( resultingString.equals( "- " ))
            {
                //System.out.println( "EQUALS " + individualWidths[0] );
            }
        }
        
        float totalVerticalDisplacementDisp = maxVerticalDisplacementText * fontSizeText * yScaleDisp;
        // convert textMatrix at the end of the string to display units
        Matrix textMatrixEndDisp = initialMatrix.multiply( textMatrix ).multiply( ctm );
        
        // process the decoded text
        processTextPosition(
                new TextPosition(
                        page,
                        textMatrixStDisp,
                        textMatrixEndDisp,
                        totalVerticalDisplacementDisp,
                        individualWidthsText,
                        spaceWidthDisp,
                        stringResult.toString(),
                        font,
                        fontSizeText,
                        (int)(fontSizeText * textMatrix.getXScale()),
                        wordSpacingDisp ));
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
        try
        {
            PDFOperator oper = PDFOperator.getOperator( operation );
            processOperator( oper, arguments );
        }
        catch (IOException e)
        {
            log.warn(e, e);
        }
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
        try
        {
            String operation = operator.getOperation();
            OperatorProcessor processor = (OperatorProcessor)operators.get( operation );
            if( processor != null )
            {
                processor.setContext(this);
                processor.process( operator, arguments );
            }
            else
            {
                if (!unsupportedOperators.contains(operation)) 
                {
                    log.info("unsupported/disabled operation: " + operation);
                    unsupportedOperators.add(operation);
                }
            }
        }
        catch (Exception e)
        {
            log.warn(e, e);
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
    
    /** 
     * Get the total number of valid characters in the doc 
     * that could be decoded in processEncodedText(). 
     * @return The number of valid characters. 
     */
    public int getValidCharCnt()
    {
        return validCharCnt;
    }

    /**
     * Get the total number of characters in the doc
     * (including ones that could not be mapped).  
     * @return The number of characters. 
     */
    public int getTotalCharCnt()
    {
        return totalCharCnt;
    }
    
}
