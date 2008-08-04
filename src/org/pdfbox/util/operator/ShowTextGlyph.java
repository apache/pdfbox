/**
 * Copyright (c) 2004, www.pdfbox.org
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
