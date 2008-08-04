/**
 * Copyright (c) 2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
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