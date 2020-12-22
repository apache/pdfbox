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

package org.apache.pdfbox.preflight.font;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType0;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.font.container.CIDType0Container;
import org.apache.pdfbox.preflight.font.descriptor.CIDType0DescriptorHelper;

public class CIDType0FontValidator extends DescendantFontValidator<CIDType0Container>
{

    public CIDType0FontValidator(final PreflightContext context, final PDCIDFontType0 font)
    {
        super(context, font, new CIDType0Container(font));
    }

    @Override
    protected void checkCIDToGIDMap(final COSBase ctog)
    {
        checkCIDToGIDMap(ctog, false);
    }

    @Override
    protected void createFontDescriptorHelper()
    {
        this.descriptorHelper = new CIDType0DescriptorHelper(context, font, fontContainer);
    }
}
