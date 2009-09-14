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

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorState;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.util.PDFOperator;

/**
 * <p>Structal modification of the PDFEngine class :
 * the long sequence of conditions in processOperator is remplaced by
 * this strategy pattern.</p>
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */

public class SetStrokingColorSpace extends OperatorProcessor
{
    private static final float[] EMPTY_FLOAT_ARRAY = new float[0];

    /**
     * CS Set color space for stroking operations.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If an error occurs while processing the font.
     */
    public void process(PDFOperator operator, List arguments) throws IOException
    {
        //(PDF 1.1) Set color space for stroking operations
        COSName name = (COSName)arguments.get( 0 );
        PDColorSpace cs = PDColorSpaceFactory.createColorSpace( name, context.getColorSpaces() );
        PDColorState color = context.getGraphicsState().getStrokingColor();
        color.setColorSpace( cs );
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
        color.setColorSpaceValue( values );

        if (context instanceof PageDrawer)
        {
            PageDrawer drawer = (PageDrawer)context;
            drawer.colorChanged(true);
        }
    }
}