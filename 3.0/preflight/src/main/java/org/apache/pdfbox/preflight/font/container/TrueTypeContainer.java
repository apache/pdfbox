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
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;

public class TrueTypeContainer extends FontContainer<PDTrueTypeFont>
{
    public TrueTypeContainer(PDTrueTypeFont font)
    {
        super(font);
    }

    @Override
    public boolean hasGlyph(int code) throws IOException
    {
        if (font.isEmbedded())
        {
            int gid = font.codeToGID(code);
            return gid != 0;
        }
        return false;
    }
}
