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
package org.pdfbox.util;

import java.util.Comparator;

import org.pdfbox.pdmodel.PDPage;

/**
 * This class is a comparator for TextPosition operators.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public class TextPositionComparator implements Comparator
{
    private PDPage thePage = null;
    
    /**
     * Constuctor, comparison of TextPosition depends on the rotation
     * of the page.
     * @param page The page that the text position is on.
     */
    public TextPositionComparator( PDPage page )
    {
        thePage = page;
    }
    
    /**
     * {@inheritDoc}
     */
    public int compare(Object o1, Object o2)
    {
        int retval = 0;
        TextPosition pos1 = (TextPosition)o1;
        TextPosition pos2 = (TextPosition)o2;
        int rotation = thePage.findRotation();
        float x1 = 0;
        float x2 = 0;
        float pos1YBottom = 0;
        float pos2YBottom = 0;
        if( rotation == 0 )
        {
            x1 = pos1.getX();
            x2 = pos2.getX();
            pos1YBottom = pos1.getY();
            pos2YBottom = pos2.getY();
        }
        else if( rotation == 90 )
        {
            x1 = pos1.getY();
            x2 = pos2.getX();
            pos1YBottom = pos1.getX();
            pos2YBottom = pos2.getY();
        }
        else if( rotation == 180 )
        {
            x1 = -pos1.getX();
            x2 = -pos2.getX();
            pos1YBottom = -pos1.getY();
            pos2YBottom = -pos2.getY();
        }
        else if( rotation == 270 )
        {
            x1 = -pos1.getY();
            x2 = -pos2.getY();
            pos1YBottom = -pos1.getX();
            pos2YBottom = -pos2.getX();
        }
        float pos1YTop = pos1YBottom - pos1.getHeight();
        float pos2YTop = pos2YBottom - pos2.getHeight();

        float yDifference = Math.abs( pos1YBottom-pos2YBottom);
        //we will do a simple tolerance comparison.
        if( yDifference < .1 || 
            (pos2YBottom >= pos1YTop && pos2YBottom <= pos1YBottom) ||
            (pos1YBottom >= pos2YTop && pos1YBottom <= pos2YBottom))
        {
            if( x1 < x2 )
            {
                retval = -1;
            }
            else if( x1 > x2 )
            {
                retval = 1;
            }
            else
            {
                retval = 0;
            }
        }
        else if( pos1YBottom < pos2YBottom )
        {
            retval = -1;
        }
        else
        {
            return 1;
        }
        
        return retval;
    }
    
}