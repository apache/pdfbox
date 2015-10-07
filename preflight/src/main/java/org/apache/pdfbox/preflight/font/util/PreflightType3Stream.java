/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.font.util;

import java.awt.Image;
import java.io.IOException;
import java.util.List;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.graphics.image.PDInlineImage;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.content.PreflightStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;

/**
 * This class is used to parse a glyph of a Type3 font program. If the glyph is parsed without error, the width of the
 * glyph is accessible through the getWidth method.
 */
public class PreflightType3Stream extends PreflightStreamEngine
{
    private final PDType3CharProc charProc;

    private boolean firstOperator = true;
    private float width = 0;

    private PDInlineImage image = null;
    private BoundingBox box = null;

    public PreflightType3Stream(PreflightContext context, PDPage page, PDType3CharProc charProc)
    {
        super(context, page);
        this.charProc = charProc;
    }

    public void showType3Character(PDType3CharProc charProc) throws IOException
    {
        processChildStream(charProc, new PDPage()); // dummy page (resource lookup may fail)
    }

    /**
     * This will parse a type3 stream and create an image from it.
     * 
     * @return The image that was created.
     * 
     * @throws IOException
     *             If there is an error processing the stream.
     */
    public Image createImage() throws IOException
    {
        showType3Character(charProc);
        return image.getImage();
    }

    /**
     * This is used to handle an operation.
     * 
     * @param operator
     *            The operation to perform.
     * @param operands
     *            The list of arguments.
     * 
     * @throws IOException
     *             If there is an error processing the operation.
     */
    @Override
    protected void processOperator(Operator operator, List operands) throws IOException
    {
        super.processOperator(operator, operands);
        String operation = operator.getName();

        if (operation.equals("BI"))
        {
            image = new PDInlineImage(operator.getImageParameters(),
                                      operator.getImageData(),
                                      getResources());

            validateInlineImageFilter(operator);
            validateInlineImageColorSpace(operator);
        }

        if (operation.equals("d0"))
        {
 
            checkType3FirstOperator(operands);

        }
        else if (operation.equals("d1"))
        {
            COSNumber llx = (COSNumber) operands.get(2);
            COSNumber lly = (COSNumber) operands.get(3);
            COSNumber urx = (COSNumber) operands.get(4);
            COSNumber ury = (COSNumber) operands.get(5);

            box = new BoundingBox();
            box.setLowerLeftX(llx.floatValue());
            box.setLowerLeftY(lly.floatValue());
            box.setUpperRightX(urx.floatValue());
            box.setUpperRightY(ury.floatValue());

            checkType3FirstOperator(operands);
        }

        checkColorOperators(operation);
        validateRenderingIntent(operator, operands);
        checkSetColorSpaceOperators(operator, operands);
        validateNumberOfGraphicStates(operator);
        firstOperator = false;
    }

    /**
     * According to the PDF Reference, the first operator in a CharProc of a Type3 font must be "d0" or "d1". This
     * method process this validation. This method is called by the processOperator method.
     * 
     * @param arguments
     * @throws IOException
     */
    private void checkType3FirstOperator(List arguments) throws IOException
    {
        if (!firstOperator)
        {
            throw new IOException("Type3 CharProc : First operator must be d0 or d1");
        }

        Object obj = arguments.get(0);
        if (obj instanceof Number)
        {
            width = ((Number) obj).intValue();
        }
        else if (obj instanceof COSNumber)
        {
            width = ((COSNumber) obj).floatValue();
        }
        else
        {
            throw new IOException("Unexpected argument type. Expected : COSInteger or Number / Received : "
                    + obj.getClass().getName());
        }
    }

    /**
     * @return the width of the CharProc glyph description
     */
    public float getWidth()
    {
        return this.width;
    }
}
