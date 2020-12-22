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
package org.apache.pdfbox.text;

import java.util.Comparator;

/**
 * This class is a comparator for TextPosition operators.  It handles
 * pages with text in different directions by grouping the text based
 * on direction and sorting in that direction. This allows continuous text
 * in a given direction to be more easily grouped together.  
 *
 * @author Ben Litchfield
 */
public class TextPositionComparator implements Comparator<TextPosition>
{
    @Override
    public int compare(final TextPosition pos1, final TextPosition pos2)
    {
        // only compare text that is in the same direction
        final int cmp1 = Float.compare(pos1.getDir(), pos2.getDir());
        if (cmp1 != 0)
        {
            return cmp1;
        }
        
        // get the text direction adjusted coordinates
        final float x1 = pos1.getXDirAdj();
        final float x2 = pos2.getXDirAdj();
        
        final float pos1YBottom = pos1.getYDirAdj();
        final float pos2YBottom = pos2.getYDirAdj();

        // note that the coordinates have been adjusted so 0,0 is in upper left
        final float pos1YTop = pos1YBottom - pos1.getHeightDir();
        final float pos2YTop = pos2YBottom - pos2.getHeightDir();

        final float yDifference = Math.abs(pos1YBottom - pos2YBottom);

        // we will do a simple tolerance comparison
        if (yDifference < .1 ||
            pos2YBottom >= pos1YTop && pos2YBottom <= pos1YBottom ||
            pos1YBottom >= pos2YTop && pos1YBottom <= pos2YBottom)
        {
            return Float.compare(x1, x2);
        }
        else if (pos1YBottom < pos2YBottom)
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }
}
