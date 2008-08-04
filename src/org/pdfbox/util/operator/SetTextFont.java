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
package org.pdfbox.util.operator;

import java.util.List;

import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSNumber;
import org.pdfbox.pdmodel.font.PDFont;
import org.pdfbox.util.PDFOperator;

import java.io.IOException;

/**
 * <p>Titre : PDFEngine Modification.</p>
 * <p>Description : Structal modification of the PDFEngine class :
 * the long sequence of conditions in processOperator is remplaced by
 * this strategy pattern</p>
 * <p>Copyright : Copyright (c) 2004</p>
 * <p>Société : DBGS</p>
 * @author Huault : huault@free.fr
 * @version $Revision: 1.5 $
 */

public class SetTextFont extends OperatorProcessor 
{
    /**
     * Tf selectfont Set text font and size.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If an error occurs while processing the font.
     */
    public void process(PDFOperator operator, List arguments) throws IOException
    {
        //there are some documents that are incorrectly structured and 
        //arguments are in the wrong spot, so we will silently ignore them
        //if there are no arguments
        if( arguments.size() >= 2 )
        {
            //set font and size
            COSName fontName = (COSName)arguments.get( 0 );
            float fontSize = ((COSNumber)arguments.get( 1 ) ).floatValue();
            context.getGraphicsState().getTextState().setFontSize( fontSize );
    
            //old way
            //graphicsState.getTextState().getFont() = (COSObject)stream.getDictionaryObject( fontName );
            //if( graphicsState.getTextState().getFont() == null )
            //{
            //    graphicsState.getTextState().getFont() = (COSObject)graphicsState.getTextState().getFont()
            //                                           Dictionary.getItem( fontName );
            //}
            context.getGraphicsState().getTextState().setFont( (PDFont)context.getFonts().get( fontName.getName() ) );
            if( context.getGraphicsState().getTextState().getFont() == null )
            {
                throw new IOException( "Error: Could not find font(" + fontName + ") in map=" + context.getFonts() );
            }
        }
    }

}
