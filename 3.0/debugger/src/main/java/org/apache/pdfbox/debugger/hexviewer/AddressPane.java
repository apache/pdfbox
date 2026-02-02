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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 * @author Khyrul Bashar
 *
 * This class shows the address of the currently selected byte.
 */
class AddressPane extends JComponent
{
    private final int totalLine;
    private int selectedLine = -1;
    private int selectedIndex = -1;

    /**
     * Constructor.
     * @param total int. Total line number needed to show all the bytes.
     */
    AddressPane(int total)
    {
        totalLine = total;
        setPreferredSize(new Dimension(HexView.ADDRESS_PANE_WIDTH, HexView.CHAR_HEIGHT * (totalLine+1)));
        setFont(HexView.FONT);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHints(HexView.RENDERING_HINTS);
        
        Rectangle bound = getVisibleRect();

        int x = HexView.LINE_INSET;
        int y = bound.y;

        if (y == 0 || y%HexView.CHAR_HEIGHT != 0)
        {
            y += HexView.CHAR_HEIGHT - y%HexView.CHAR_HEIGHT;
        }
        int firstLine = y/HexView.CHAR_HEIGHT;

        for (int line = firstLine; line < firstLine + bound.getHeight()/HexView.CHAR_HEIGHT; line++)
        {
            if (line > totalLine)
            {
                break;
            }
            if (line == selectedLine)
            {
                paintSelected(g, x, y);
            }
            else
            {
                g.drawString(String.format("%08X", (line - 1)*16), x, y);
            }
            x = HexView.LINE_INSET;
            y += HexView.CHAR_HEIGHT;
        }
    }

    /**
     * Paint a selected line
     * @param g Graphics instance.
     * @param x int. x axis value.
     * @param y int. y axis value.
     */
    private void paintSelected(Graphics g, int x, int y)
    {
        g.setColor(HexView.SELECTED_COLOR);
        g.setFont(HexView.BOLD_FONT);

        g.drawString(String.format("%08X", selectedIndex), x, y);

        g.setColor(Color.black);
        g.setFont(HexView.FONT);
    }

    /**
     * Updates the line text (index in hexadecimal) for a given index. It is used when a byte is
     * selected in hex pane.
     * @param index int.
     */
    void setSelected(int index)
    {
        if (index != selectedIndex)
        {
            selectedLine = HexModel.lineNumber(index);
            selectedIndex = index;
            repaint();
        }
    }
}
