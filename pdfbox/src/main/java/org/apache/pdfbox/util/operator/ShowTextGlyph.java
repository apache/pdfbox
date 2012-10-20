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
package org.apache.pdfbox.util.operator;

import java.util.List;

import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import java.io.IOException;
import org.apache.pdfbox.cos.COSString;

/**
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
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        COSArray array = (COSArray)arguments.get( 0 );
        int arraySize = array.size();
        float fontsize = context.getGraphicsState().getTextState().getFontSize();
        float horizontalScaling = context.getGraphicsState().getTextState().getHorizontalScalingPercent()/100;
        for( int i=0; i<arraySize; i++ )
        {
            COSBase next = array.get( i );
            if( next instanceof COSNumber )
            {
                float adjustment = ((COSNumber)next).floatValue();
                Matrix adjMatrix = new Matrix();
                adjustment=-(adjustment/1000)*horizontalScaling*fontsize;
                // TODO vertical writing mode
                adjMatrix.setValue( 2, 0, adjustment );
                context.setTextMatrix( adjMatrix.multiply(context.getTextMatrix(), adjMatrix) );
            }
            else if( next instanceof COSString )
            {
                context.processEncodedText( ((COSString)next).getBytes() );
            }
            else
            {
                throw new IOException( "Unknown type in array for TJ operation:" + next );
            }
        }
    }

}
