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

package org.apache.pdfbox.preflight.font.container;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;

public class Type0Container extends FontContainer
{
    private FontContainer delegateFontContainer;

    public Type0Container(PDFont font)
    {
        super(font);
    }

    public void setDelegateFontContainer(FontContainer delegateFontContainer)
    {
        this.delegateFontContainer = delegateFontContainer;
    }

    @Override
    public List<ValidationError> getAllErrors()
    {
        if (this.delegateFontContainer != null)
        {
            this.errorBuffer.addAll(this.delegateFontContainer.getAllErrors());
        }
        return this.errorBuffer;
    }

    @Override
    public boolean isValid()
    {
        boolean result = (this.errorBuffer.isEmpty() && isEmbeddedFont());
        if (this.delegateFontContainer != null)
        {
            result &= this.delegateFontContainer.isValid();
        }
        return result;
    }

    @Override
    public boolean isEmbeddedFont()
    {
        boolean result = embeddedFont;
        if (this.delegateFontContainer != null)
        {
            result &= this.delegateFontContainer.isEmbeddedFont();
        }
        return result;
    }

    @Override
    public boolean hasGlyph(int code) throws IOException
    {
        return this.delegateFontContainer.hasGlyph(code);
    }
}
