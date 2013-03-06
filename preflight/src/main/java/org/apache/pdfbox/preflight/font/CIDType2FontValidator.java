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
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.font.container.CIDType2Container;
import org.apache.pdfbox.preflight.font.descriptor.CIDType2DescriptorHelper;
import org.apache.pdfbox.preflight.font.util.CIDToGIDMap;

public class CIDType2FontValidator extends DescendantFontValidator<CIDType2Container>
{

    public CIDType2FontValidator(PreflightContext context, PDFont font)
    {
        super(context, font, new CIDType2Container(font));
    }

    @Override
    protected void checkCIDToGIDMap(COSBase ctog)
    {
        CIDToGIDMap cidToGid = checkCIDToGIDMap(ctog, true);
        this.fontContainer.setCidToGid(cidToGid);
    }

    @Override
    protected void createFontDescriptorHelper()
    {
        this.descriptorHelper = new CIDType2DescriptorHelper(context, font, fontContainer);
    }

}
