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
import java.util.Map;

import org.pdfbox.cos.COSName;
import org.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;
import org.pdfbox.pdmodel.graphics.color.PDColorSpaceInstance;
import org.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.pdfbox.util.PDFOperator;
import org.pdfbox.pdfviewer.PageDrawer;

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

        //logger().info("Set NonStrokingColorSpace to have instance of type " + colorInstance.toString() + " with component count " + numComponents);

        if (context instanceof PageDrawer){
            PageDrawer drawer = (PageDrawer)context;
            drawer.ColorChanged (false);
        }
    }
}
