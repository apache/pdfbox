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
package org.apache.pdfbox.pdfviewer;

import java.awt.Component;

import javax.swing.JTree;

import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;

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
