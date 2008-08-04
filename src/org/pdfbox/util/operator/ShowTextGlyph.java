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

import org.pdfbox.util.Matrix;
import org.pdfbox.util.PDFOperator;
import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSNumber;
import java.io.IOException;
import org.pdfbox.cos.COSString;

/**
 * <p>Titre : PDFEngine Modification.</p>
 * <p>Description : Structal modification of the PDFEngine class : the long sequence of 
 *    conditions in processOperator is remplaced by this strategy pattern</p>
 * <p>Copyright : Copyright (c) 2004</p>
 * <p>Société : DBGS</p>
 * @author Huault : huault@free.fr
 * @version $Revision: 1.6 $
 */

public class ShowTextGlyph extends OperatorProcessor 
{
    /**
     * TJ Show text, allowing individual glyph positioning.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If there is an error processing this operator.
     */
    public void process(PDFOperator operator, List arguments) throws IOException 
    {
        COSArray array = (COSArray)arguments.get( 0 );
        float adjustment=0;
        for( int i=0; i<array.size(); i++ )
        {
            COSBase next = array.get( i );
            if( next instanceof COSNumber )
            {
                adjustment = ((COSNumber)next).floatValue();

                Matrix adjMatrix = new Matrix();
                adjustment=(-adjustment/1000)*context.getGraphicsState().getTextState().getFontSize() *
                    (context.getGraphicsState().getTextState().getHorizontalScalingPercent()/100);
                adjMatrix.setValue( 2, 0, adjustment );
                context.setTextMatrix( adjMatrix.multiply( context.getTextMatrix() ) );
            }
            else if( next instanceof COSString )
            {
                context.showString( ((COSString)next).getBytes() );
            }
            else
            {
                throw new IOException( "Unknown type in array for TJ operation:" + next );
            }
        }
    }

}
