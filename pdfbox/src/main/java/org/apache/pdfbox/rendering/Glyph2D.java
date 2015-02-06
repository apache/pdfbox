/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.pdfbox.rendering;

import java.awt.geom.GeneralPath;
import java.io.IOException;

/**
 * This interface is implemented by several font specific classes which is called to get the
 * general path of a single glyph of the represented font most likely to render it.
 */
interface Glyph2D
{
    /**
     * Returns the path describing the glyph for the given character code.
     * 
     * @param code the character code
     * 
     * @return the GeneralPath for the given character code
     */
    GeneralPath getPathForCharacterCode(int code) throws IOException;

    /**
     * Remove all cached resources.
     */
    void dispose();
}
