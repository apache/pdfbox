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

package org.apache.pdfbox.debugger.hexviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * @author Khyrul Bashar
 *
 * HexView takes a byte array or Stream instance and shows them in Hex viewer.
 */
public class HexView
{
    private final JComponent mainPane;
    
    static final int FONT_SIZE = ((Font)UIManager.get("Label.font")).getSize();
    static final Font FONT = new Font("monospaced", Font.PLAIN, FONT_SIZE);
    static final int CHAR_HEIGHT = 20;
    static final int CHAR_WIDTH = 35;
    static final int LINE_INSET = 20;
    static final Color SELECTED_COLOR = UIManager.getColor("textHighlight");
    static final Font BOLD_FONT = new Font("monospaced", Font.BOLD, FONT_SIZE);
    static final int HEX_PANE_WIDTH = 600;
    static final int ADDRESS_PANE_WIDTH = 120;
    static final int ASCII_PANE_WIDTH = 270;
    static final int TOTAL_WIDTH = HEX_PANE_WIDTH + ADDRESS_PANE_WIDTH +ASCII_PANE_WIDTH;
    static final Map<RenderingHints.Key, Object> RENDERING_HINTS = new HashMap<RenderingHints.Key, Object>();
    static
    {
        RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RENDERING_HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
    }

    public HexView()
    {
        mainPane = new JPanel(new BorderLayout());
    }

    /**
     * Constructor.
     * @param bytes takes a byte array.
     */
    public HexView(byte[] bytes)
    {
        mainPane = new JPanel(new BorderLayout());
        mainPane.add(new HexEditor(new HexModel(bytes)));
    }

    public void changeData(byte[] bytes)
    {
        if (mainPane.getComponentCount() > 0)
        {
            mainPane.removeAll();
        }
        HexModel model = new HexModel(bytes);
        mainPane.add(new HexEditor(model));
        mainPane.validate();
    }

    public JComponent getPane()
    {
        return mainPane;
    }
}
