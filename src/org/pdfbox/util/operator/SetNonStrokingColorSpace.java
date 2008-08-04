/**
 * Copyright (c) 2005, www.pdfbox.org
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
import java.util.Map;

import org.pdfbox.cos.COSName;
import org.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;
import org.pdfbox.pdmodel.graphics.color.PDColorSpaceInstance;
import org.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.pdfbox.util.PDFOperator;

import java.io.IOException;

/**
 * <p>Set the non stroking color space.</p>
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class SetNonStrokingColorSpace extends OperatorProcessor 
{
    private static final float[] EMPTY_FLOAT_ARRAY = new float[0];

    /**
     * cs Set color space for non stroking operations.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If an error occurs while processing the font.
     */
    public void process(PDFOperator operator, List arguments) throws IOException
    {
//      (PDF 1.1) Set color space for stroking operations
        COSName name = (COSName)arguments.get( 0 );
        PDColorSpace cs = null;
        Map colorSpaces = context.getColorSpaces();
        if( colorSpaces != null )
        {
             cs = (PDColorSpace)colorSpaces.get( name.getName() );
        }
        if( cs == null )
        {
            cs = PDColorSpaceFactory.createColorSpace( name );
        }
        PDColorSpaceInstance colorInstance = context.getGraphicsState().getNonStrokingColorSpace();
        colorInstance.setColorSpace( cs );
        int numComponents = cs.getNumberOfComponents();
        float[] values = EMPTY_FLOAT_ARRAY;
        if( numComponents >= 0 )
        {
            values = new float[numComponents];
            for( int i=0; i<numComponents; i++ )
            {
                values[i] = 0f;
            }
            if( cs instanceof PDDeviceCMYK )
            {
                values[3] = 1f;
            }
        }
        colorInstance.setColorSpaceValue( values );
    }
}
