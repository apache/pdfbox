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

import java.util.List;

import org.apache.fontbox.cff.CharStringCommand;

public class GlyphDescription
{
    private List<Object> operations = null;

    private Integer glyphWidth = null;

    GlyphDescription(List<Object> operations)
    {
        this.operations = operations;
    }

    public int getGlyphWidth()
    {
        if (this.glyphWidth != null)
        {
            return glyphWidth;
        }

        this.glyphWidth = searchWidth();
        return this.glyphWidth;
    }

    private int searchWidth()
    {
        for (int i = 0; operations != null && i < operations.size(); ++i)
        {
            Object obj = operations.get(i);
            if (obj instanceof CharStringCommand)
            {
                CharStringCommand csCmd = (CharStringCommand) obj;
                if ("hsbw".equals(CharStringCommand.TYPE1_VOCABULARY.get(csCmd.getKey())))
                {
                    // Numbers with absolute values greater than 32,000 must be followed
                    // by a div operator such that the result of the div is less than 32,000.
                    if (operations.get(i - 1) instanceof CharStringCommand)
                    {
                        CharStringCommand div = (CharStringCommand) operations.get(i - 1);
                        if ("div".equals(CharStringCommand.TYPE1_VOCABULARY.get(div.getKey())))
                        {
                            return ((Integer) operations.get(i - 3) / (Integer) operations.get(i - 2));
                        }
                    }
                    else
                    {
                        return (Integer) operations.get(i - 1);
                    }
                }
            }
        }
        // 0 is the default value if glyph isn't found.
        return 0;
    }
}
