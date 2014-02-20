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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.util.PDFOperator;

import java.io.IOException;
import java.util.List;

/**
 * Sets the color to use for stroking or non-stroking operations.
 *
 * @author John Hewson
 */
public abstract class SetColor extends OperatorProcessor
{
    /**
     * sc,scn,SC,SCN Sets the colour to use for stroking or non-stroking operations.
     * @param operator the operator that is being executed.
     * @param arguments list of operands
     * @throws IOException if the color space cannot be read
     */
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        COSArray array = new COSArray();
        array.addAll(arguments);
        setColor(new PDColor(array));
    }

    /**
     * Returns either the stroking or non-stroking color value.
     * @return The stroking or non-stroking color value.
     */
    protected abstract PDColor getColor();

    /**
     * Sets either the stroking or non-stroking color value.
     * @param color The stroking or non-stroking color value.
     */
    protected abstract void setColor(PDColor color);

    /**
     * Returns either the stroking or non-stroking color space.
     * @return The stroking or non-stroking color space.
     */
    protected abstract PDColorSpace getColorSpace();
}
