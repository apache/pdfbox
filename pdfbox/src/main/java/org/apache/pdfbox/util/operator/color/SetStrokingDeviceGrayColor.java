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
package org.apache.pdfbox.util.operator.color;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.util.operator.Operator;

import java.io.IOException;
import java.util.List;

/**
 * G: Set the stroking colour space to DeviceGray and set the gray level to use for stroking
 * operations.
 *
 * @author John Hewson
 */
public class SetStrokingDeviceGrayColor extends SetStrokingColor
{
    public void process(Operator operator, List<COSBase> arguments) throws IOException
    {
        context.getGraphicsState().setStrokingColorSpace(PDDeviceGray.INSTANCE);
        super.process(operator, arguments);
    }
}
