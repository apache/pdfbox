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
package org.apache.pdfbox.debugger.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;

/**
 * A class to render tree cells for the pdfviewer.
 *
 * @author Ben Litchfield
 */
public class PDFTreeCellRenderer extends DefaultTreeCellRenderer
{
    private static final ImageIcon ICON_ARRAY = new ImageIcon(getImageUrl("array"));
    private static final ImageIcon ICON_BOOLEAN = new ImageIcon(getImageUrl("boolean"));
    private static final ImageIcon ICON_DICT = new ImageIcon(getImageUrl("dict"));
    private static final ImageIcon ICON_HEX = new ImageIcon(getImageUrl("hex"));
    private static final ImageIcon ICON_INDIRECT = new ImageIcon(getImageUrl("indirect"));
    private static final ImageIcon ICON_INTEGER = new ImageIcon(getImageUrl("integer"));
    private static final ImageIcon ICON_NAME = new ImageIcon(getImageUrl("name"));
    private static final ImageIcon ICON_NULL = new ImageIcon(getImageUrl("null"));
    private static final ImageIcon ICON_REAL = new ImageIcon(getImageUrl("real"));
    private static final ImageIcon ICON_STREAM_DICT = new ImageIcon(getImageUrl("stream-dict"));
    private static final ImageIcon ICON_STRING = new ImageIcon(getImageUrl("string"));
    private static final ImageIcon ICON_PDF = new ImageIcon(getImageUrl("pdf"));
    private static final ImageIcon ICON_PAGE = new ImageIcon(getImageUrl("page"));

    private static URL getImageUrl(String name)
    {
        String fullName = "/org/apache/pdfbox/debugger/" + name + ".png";
        return PDFTreeCellRenderer.class.getResource(fullName);
    }
    
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
        Component component = super.getTreeCellRendererComponent(tree,
                toTreeObject(nodeValue),
                isSelected, expanded, leaf, row, componentHasFocus);
        
        setIcon(lookupIconWithOverlay(nodeValue));

        return component;
    }

    private Object toTreeObject(Object nodeValue)
    {
        Object result = nodeValue;
        if (nodeValue instanceof MapEntry || nodeValue instanceof ArrayEntry)
        {
            String key;
            Object object;
            Object value;
            COSBase item;
            if (nodeValue instanceof MapEntry)
            {
                MapEntry entry = (MapEntry) nodeValue;
                key = entry.getKey().getName();
                object = toTreeObject(entry.getValue());
                value = entry.getValue();
                item = entry.getItem();
            }
            else
            {
                ArrayEntry entry = (ArrayEntry) nodeValue;
                key = "" + entry.getIndex();
                object = toTreeObject(entry.getValue());
                value = entry.getValue();
                item = entry.getItem();
            }
            
            String stringResult = key;
            if (object instanceof String && ((String)object).length() > 0)
            {
                stringResult += ":  " + object;
                if (item instanceof COSObject)
                {
                    COSObject indirect = (COSObject)item;
                    stringResult += " [" + indirect.getObjectNumber() + " " +
                                           indirect.getGenerationNumber() + " R]";
                }
                stringResult += toTreePostfix(value);
                
            }
            result = stringResult;
        }
        else if (nodeValue instanceof COSBoolean)
        {
            result = "" + ((COSBoolean) nodeValue).getValue();
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
        else if (nodeValue instanceof COSNull || nodeValue == null)
        {
            result = "";
        }
        else if (nodeValue instanceof COSDictionary)
        {
            COSDictionary dict = (COSDictionary) nodeValue;
            if (COSName.XREF.equals(dict.getCOSName(COSName.TYPE)))
            {
                result = "";
            }
            else
            {
                result = "(" + dict.size() + ")";
            }
        }
        else if (nodeValue instanceof COSArray)
        {
            COSArray array = (COSArray) nodeValue;
            result = "(" + array.size() + ")";
        }
        else if (nodeValue instanceof DocumentEntry)
        {
            result = nodeValue.toString();
        }
        return result;
    }

    private String toTreePostfix(Object nodeValue)
    {
        if (nodeValue instanceof COSDictionary)
        {
            StringBuilder sb = new StringBuilder();
            
            COSDictionary dict = (COSDictionary)nodeValue;
            if (dict.containsKey(COSName.TYPE))
            {
                COSName type = dict.getCOSName(COSName.TYPE);
                sb.append("   /T:").append(type.getName());
            }
            
            if (dict.containsKey(COSName.SUBTYPE))
            {
                COSName subtype = dict.getCOSName(COSName.SUBTYPE);
                sb.append("  /S:").append(subtype.getName());
            }
            return sb.toString();
        }
        else
        {
            return "";
        }
    }

    private ImageIcon lookupIconWithOverlay(Object nodeValue)
    {
        ImageIcon icon = lookupIcon(nodeValue);
        boolean isIndirect = false;
        boolean isStream = false;
        
        if (nodeValue instanceof MapEntry)
        {
            MapEntry entry = (MapEntry) nodeValue;
            if (entry.getItem() instanceof COSObject)
            {
                isIndirect = true;
                isStream = entry.getValue() instanceof COSStream;
            }
        }
        else if (nodeValue instanceof ArrayEntry)
        {
            ArrayEntry entry = (ArrayEntry) nodeValue;
            if (entry.getItem() instanceof COSObject)
            {
                isIndirect = true;
                isStream = entry.getValue() instanceof COSStream;
            }
        }
        
        if (isIndirect && !isStream)
        {
            OverlayIcon overlay = new OverlayIcon(icon);
            overlay.add(ICON_INDIRECT);
            return overlay;
        }
        return icon;
    }
    
    private ImageIcon lookupIcon(Object nodeValue)
    {
        if (nodeValue instanceof MapEntry)
        {
            MapEntry entry = (MapEntry) nodeValue;
            return lookupIcon(entry.getValue());
        }
        else if (nodeValue instanceof ArrayEntry)
        {
            ArrayEntry entry = (ArrayEntry) nodeValue;
            return lookupIcon(entry.getValue());
        }
        else if (nodeValue instanceof COSBoolean)
        {
            return ICON_BOOLEAN;
        }
        else if (nodeValue instanceof COSFloat)
        {
            return ICON_REAL;
        }
        else if (nodeValue instanceof COSInteger)
        {
            return ICON_INTEGER;
        }
        else if (nodeValue instanceof COSString)
        {
            String text = ((COSString) nodeValue).getString();
            // display unprintable strings as hex
            for (char c : text.toCharArray())
            {
                if (Character.isISOControl(c))
                {
                    return ICON_HEX;
                }
            }
            return ICON_STRING;
        }
        else if (nodeValue instanceof COSName)
        {
            return ICON_NAME;
        }
        else if (nodeValue instanceof COSNull || nodeValue == null)
        {
            return ICON_NULL;
        }
        else if (nodeValue instanceof COSStream)
        {
            return ICON_STREAM_DICT;
        }
        else if (nodeValue instanceof COSDictionary)
        {
            return ICON_DICT;
        }
        else if (nodeValue instanceof COSArray)
        {
            return ICON_ARRAY;
        }
        else if (nodeValue instanceof DocumentEntry)
        {
            return ICON_PDF;
        }
        else if (nodeValue instanceof PageEntry)
        {
            return ICON_PAGE;
        }
        else
        {
            return null;
        }
    }

    /**
     * An ImageIcon which allows other ImageIcon overlays.
     */
    private class OverlayIcon extends ImageIcon
    {
        private final ImageIcon base;
        private final List<ImageIcon> overlays;

        OverlayIcon(ImageIcon base)
        {
            super(base.getImage());
            this.base = base;
            this.overlays = new ArrayList<ImageIcon>();
        }

        void add(ImageIcon overlay)
        {
            overlays.add(overlay);
        }

        @Override
        public synchronized void paintIcon(Component c, Graphics g, int x, int y)
        {
            base.paintIcon(c, g, x, y);
            for (ImageIcon icon: overlays)
            {
                icon.paintIcon(c, g, x, y);
            }
        }
    }
}
