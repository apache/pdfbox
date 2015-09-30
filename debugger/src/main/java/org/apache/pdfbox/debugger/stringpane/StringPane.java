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

package org.apache.pdfbox.debugger.stringpane;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.debugger.hexviewer.HexView;

/**
 * @author Khyrul Bashar
 */
public class StringPane
{
    private static final String TEXT_TAB = "Text View";
    private static final String HEX_TAB = "Hex view";

    private final JTabbedPane tabbedPane;

    public StringPane(COSString cosString)
    {
        tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(300, 500));
        tabbedPane.addTab(TEXT_TAB, createTextView(cosString));
        tabbedPane.addTab(HEX_TAB, createHexView(cosString));
    }

    private JTextPane createTextView(COSString cosString)
    {
        JTextPane textPane = new JTextPane();
        textPane.setText(getTextString(cosString));
        textPane.setEditable(false);

        return textPane;
    }

    private JComponent createHexView(COSString cosString)
    {
        HexView hexView = new HexView(cosString.getBytes());
        return hexView.getPane();
    }

    private String getTextString(COSString cosString)
    {
        String text = cosString.getString();
        for (char c : text.toCharArray())
        {
            if (Character.isISOControl(c))
            {
                text = "<" + cosString.toHexString() + ">";
                break;
            }
        }
        return  "" + text;
    }

    public JTabbedPane getPane()
    {
        return tabbedPane;
    }
}
