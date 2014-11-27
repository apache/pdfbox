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
package org.apache.pdfbox.contentstream.operator.color;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.contentstream.operator.Operator;

import java.io.IOException;
import java.util.List;

/**
 * RG: Set the stroking colour space to DeviceRGB and set the colour to use for stroking operations.
 *
 * @author John Hewson
 */
public class SetStrokingDeviceRGBColor extends SetStrokingColor
{
    /**
     * RG Set the stroking colour space to DeviceRGB and set the colour to
     * use for stroking operations.
     *
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If the color space cannot be read.
     */
    public void process(Operator operator, List<COSBase> arguments) throws IOException
    {
        PDColorSpace cs = context.getResources().getColorSpace(COSName.DEVICERGB);
        context.getGraphicsState().setStrokingColorSpace(cs);
        super.process(operator, arguments);
    }

    @Override
    public String getName()
    {
        return "RG";
    }
}
