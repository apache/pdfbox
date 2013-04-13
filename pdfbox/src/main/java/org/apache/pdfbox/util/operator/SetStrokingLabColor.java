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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorState;
import org.apache.pdfbox.util.PDFOperator;

/**
 * Sets the stroking color values for Lab colorspace.  
 * 
 */
public class SetStrokingLabColor extends OperatorProcessor 
{

    /**
     * {@inheritDoc}
     */
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        PDColorState color = context.getGraphicsState().getStrokingColor();
        float[] values = new float[3];
        for( int i=0; i<arguments.size(); i++ )
        {
            values[i] = ((COSNumber)arguments.get( i )).floatValue();
        }
        color.setColorSpaceValue( values );
    }

}
