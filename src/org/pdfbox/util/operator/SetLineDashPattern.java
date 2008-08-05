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

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSNumber;
import org.pdfbox.pdmodel.graphics.PDLineDashPattern;
import org.pdfbox.util.PDFOperator;

import java.io.IOException;

/**
 * Implementation of content stream operator for page drawer.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.1 $
 */
public class SetLineDashPattern extends org.pdfbox.util.operator.SetLineWidth
{

    /**
     * Set the line dash pattern.
     * @param operator The operator that is being executed.
     * @param arguments List
     *
     * @throws IOException If an error occurs while processing the font.
     */
    public void process(PDFOperator operator, List arguments) throws IOException
    {
        COSArray dashArray = (COSArray)arguments.get( 0 );
        int dashPhase = ((COSNumber)arguments.get( 1 )).intValue();
        PDLineDashPattern lineDash = new PDLineDashPattern( dashArray, dashPhase );
        context.getGraphicsState().setLineDashPattern( lineDash );
    }
}
