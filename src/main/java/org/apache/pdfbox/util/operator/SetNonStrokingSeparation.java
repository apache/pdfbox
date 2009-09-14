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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.graphics.color.PDCalRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorState;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.util.PDFOperator;

/**
 * 
 * @author <a href="mailto:WilliamstonConsulting@GMail.com">Daniel Wilson</a>
 * @version $Revision: 1.0 $
 */
public class SetNonStrokingSeparation extends OperatorProcessor 
{

    /**
     * Log instance.
     */
    private static final Log log =
        LogFactory.getLog(SetNonStrokingSeparation.class);

    /**
     * scn Set color space for non stroking operations.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If an error occurs while processing the font.
     */
    public void process(PDFOperator operator, List arguments) throws IOException
    {
        PDColorState colorInstance = context.getGraphicsState().getNonStrokingColor();
        PDColorSpace colorSpace = colorInstance.getColorSpace();
        List argList = arguments;
        
        if (colorSpace instanceof PDSeparation)
        {
            PDSeparation sep = (PDSeparation) colorSpace;
            colorSpace = sep.getAlternateColorSpace();
            argList = sep.getColorValues().toList();
        }
    
        if (colorSpace != null) 
        {
            OperatorProcessor newOperator = null;
            if (colorSpace instanceof PDDeviceGray)
            {
                newOperator = new SetNonStrokingGrayColor();
            }
            else if (colorSpace instanceof PDDeviceRGB)
            {
                newOperator = new SetNonStrokingRGBColor();
            }
            else if (colorSpace instanceof PDDeviceCMYK)
            {
                newOperator = new SetNonStrokingCMYKColor();
            }
            else if (colorSpace instanceof PDICCBased)
            {
                newOperator = new SetNonStrokingICCBasedColor();
            }
            else if (colorSpace instanceof PDCalRGB)
            {
                newOperator = new SetNonStrokingCalRGBColor();
            }
            else if (colorSpace instanceof PDSeparation)
            {
                newOperator = new SetNonStrokingSeparation();
            }
    
            if (newOperator != null) 
            {
                newOperator.setContext(getContext());
                newOperator.process(operator, argList);
            }
            else
            {
                log.warn("Not supported colorspace "+colorSpace.getName() 
                        + " within operator "+operator.getOperation());
            }
        }
        
    }
}
