/**
 * Copyright (c) 2003-2006, www.pdfbox.org
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
package org.pdfbox.pdfviewer;

import java.awt.Component;

import javax.swing.JTree;

import javax.swing.tree.DefaultTreeCellRenderer;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSNull;
import org.pdfbox.cos.COSFloat;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.cos.COSStream;
import org.pdfbox.cos.COSString;

/**
 * A class to render tree cells for the pdfviewer.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class PDFTreeCellRenderer extends DefaultTreeCellRenderer
{
    /**
     * {@inheritDoc}
     */
    public Component getTreeCellRendererComponent(
        JTree tree,
        Object nodeValue,
        boolean isSelected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean componentHasFocus)
    {
        nodeValue = convertToTreeObject( nodeValue );
        return super.getTreeCellRendererComponent( tree, nodeValue, isSelected, expanded, leaf, 
                row, componentHasFocus );
    }

    private Object convertToTreeObject( Object nodeValue )
    {
        if( nodeValue instanceof MapEntry )
        {
            MapEntry entry = (MapEntry)nodeValue;
            COSName key = (COSName)entry.getKey();
            COSBase value = (COSBase)entry.getValue();
            nodeValue = key.getName() + ":" + convertToTreeObject( value );
        }
        else if( nodeValue instanceof COSFloat )
        {
            nodeValue = "" + ((COSFloat)nodeValue).floatValue();
        }
        else if( nodeValue instanceof COSInteger )
        {
            nodeValue = "" + ((COSInteger)nodeValue).intValue();
        }
        else if( nodeValue instanceof COSString )
        {
            nodeValue = ((COSString)nodeValue).getString();
        }
        else if( nodeValue instanceof COSName )
        {
            nodeValue = ((COSName)nodeValue).getName();
        }
        else if( nodeValue instanceof ArrayEntry )
        {
            ArrayEntry entry = (ArrayEntry)nodeValue;
            nodeValue = "[" + entry.getIndex() + "]" + convertToTreeObject( entry.getValue() );
        }
        else if( nodeValue instanceof COSNull )
        {
            nodeValue = "null";
        }
        else if( nodeValue instanceof COSDictionary )
        {
            COSDictionary dict = (COSDictionary)nodeValue;
            if( nodeValue instanceof COSStream )
            {
                nodeValue = "Stream";
            }
            else
            {
                nodeValue = "Dictionary";
            }

            COSName type = (COSName)dict.getDictionaryObject( COSName.TYPE );
            if( type != null )
            {
                nodeValue = nodeValue + "(" + type.getName();
                COSName subType = (COSName)dict.getDictionaryObject( COSName.SUBTYPE );
                if( subType != null )
                {
                    nodeValue = nodeValue + ":" + subType.getName();
                }

                nodeValue = nodeValue + ")";
            }
        }
        else if( nodeValue instanceof COSArray )
        {
            nodeValue="Array";
        }
        else if( nodeValue instanceof COSString )
        {
            nodeValue = ((COSString)nodeValue).getString();
        }
        return nodeValue;

    }
}