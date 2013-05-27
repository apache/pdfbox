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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDCalRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;
import org.apache.pdfbox.pdmodel.graphics.color.PDLab;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.util.PDFOperator;
import java.io.IOException;

/**
 * <p>Set the stroking color space.</p>
 * 
 * @version $Revision: 1.0 $
 */
public class SetStrokingColor extends OperatorProcessor 
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(SetStrokingColor.class);

    /**
     * SC,SCN Set color space for stroking operations.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If an error occurs while processing the font.
     */
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        PDColorSpace colorSpace = context.getGraphicsState().getStrokingColor().getColorSpace();
        if (colorSpace != null) 
        {
            OperatorProcessor newOperator = null;
            if (colorSpace instanceof PDDeviceGray)
            {
                newOperator = new SetStrokingGrayColor();
            }
            else if (colorSpace instanceof PDDeviceRGB)
            {
                newOperator = new SetStrokingRGBColor();
            }
            else if (colorSpace instanceof PDDeviceCMYK)
            {
                newOperator = new SetStrokingCMYKColor();
            }
            else if (colorSpace instanceof PDICCBased)
            {
                newOperator = new SetStrokingICCBasedColor();
            }
            else if (colorSpace instanceof PDCalRGB)
            {
                newOperator = new SetStrokingCalRGBColor();
            }   
            else if (colorSpace instanceof PDSeparation)
            {
                newOperator = new SetStrokingSeparation();
            }
            else if (colorSpace instanceof PDDeviceN)
            {
                newOperator = new SetStrokingDeviceN();
            }
            else if (colorSpace instanceof PDPattern)
            {
                newOperator = new SetStrokingPattern();
            }
            else if (colorSpace instanceof PDIndexed)
            {
                newOperator = new SetStrokingIndexed();
            }
            else if (colorSpace instanceof PDLab)
            {
                newOperator = new SetStrokingLabColor();
            }

            if (newOperator != null) 
            {
                newOperator.setContext(getContext());
                newOperator.process(operator, arguments);
            }
            else
            {
                LOG.info("Not supported colorspace "+colorSpace.getName() 
                        + " within operator "+operator.getOperation());
            }
        }
        else 
        {
            LOG.warn("Colorspace not found in "+getClass().getName()+".process!!");
        }
    }
}
