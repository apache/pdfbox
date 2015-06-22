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
package org.apache.pdfbox.tools.gui;

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
 * @author Ben Litchfield
 */
public class PDFTreeCellRenderer extends DefaultTreeCellRenderer
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object nodeValue,
            boolean isSelected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean componentHasFocus)
    {
        return super.getTreeCellRendererComponent(tree,
                convertToTreeObject(nodeValue),
                isSelected, expanded, leaf, row, componentHasFocus);
    }

    private Object convertToTreeObject(Object nodeValue)
    {
        Object result = nodeValue;
        if (nodeValue instanceof MapEntry)
        {
            MapEntry entry = (MapEntry) nodeValue;
            COSName key = (COSName) entry.getKey();
            COSBase value = (COSBase) entry.getValue();
            result = key.getName() + ":" + convertToTreeObject(value);
        }
        else if (nodeValue instanceof COSFloat)
        {
            result = "" + ((COSFloat) nodeValue).floatValue();
        }
        else if (nodeValue instanceof COSInteger)
        {
            result = "" + ((COSInteger) nodeValue).intValue();
        }
        else if (nodeValue instanceof COSString)
        {
            String text = ((COSString) nodeValue).getString();
            // display unprintable strings as hex
            for (char c : text.toCharArray())
            {
                if (Character.isISOControl(c))
                {
                    text = "<" + ((COSString) nodeValue).toHexString() + ">";
                    break;
                }
            }
            result = text;
        }
        else if (nodeValue instanceof COSName)
        {
            result = ((COSName) nodeValue).getName();
        }
        else if (nodeValue instanceof ArrayEntry)
        {
            ArrayEntry entry = (ArrayEntry) nodeValue;
            result = "[" + entry.getIndex() + "]" + convertToTreeObject(entry.getValue());
        }
        else if (nodeValue instanceof COSNull)
        {
            result = "null";
        }
        else if (nodeValue instanceof COSDictionary)
        {
            COSDictionary dict = (COSDictionary) nodeValue;
            if (nodeValue instanceof COSStream)
            {
                result = "Stream";
            }
            else
            {
                result = "Dictionary";
            }

            COSName type = (COSName) dict.getDictionaryObject(COSName.TYPE);
            if (type != null)
            {
                result = result + "(" + type.getName();
                COSName subType = (COSName) dict.getDictionaryObject(COSName.SUBTYPE);
                if (subType != null)
                {
                    result = result + ":" + subType.getName();
                }

                result = result + ")";
            }
        }
        else if (nodeValue instanceof COSArray)
        {
            result = "Array";
        }
        return result;

    }
}
