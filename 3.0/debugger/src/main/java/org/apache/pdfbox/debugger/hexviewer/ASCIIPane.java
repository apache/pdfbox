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
 * This class shows corresponding ASCII characters for bytes. For every 16 byte there is one line.
 * This paints the only visible contents at one time.
 */
@SuppressWarnings({"serial","squid:S1948"})
class ASCIIPane extends JComponent implements HexModelChangeListener
{
    private final HexModel model;
    private int selectedLine = -1;
    private int selectedIndexInLine;

    /**
     * Constructor.
     * @param model HexModel instance.
     */
    ASCIIPane(HexModel model)
    {
        this.model = model;
        setPreferredSize(new Dimension(HexView.ASCII_PANE_WIDTH, HexView.CHAR_HEIGHT * (model.totalLine()+1)));
        model.addHexModelChangeListener(this);
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
            if (line > model.totalLine())
            {
                break;
            }
            if (line == selectedLine)
            {
                paintInSelected(g, x, y);
            }
            else
            {
                char[] chars = model.getLineChars(line);
                g.drawChars(chars, 0, chars.length, x, y);
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
    private void paintInSelected(Graphics g, int x, int y)
    {
        g.setFont(HexView.BOLD_FONT);
        char[] content = model.getLineChars(selectedLine);
        g.drawChars(content, 0, selectedIndexInLine - 0, x, y);

        g.setColor(HexView.SELECTED_COLOR);
        x += g.getFontMetrics().charsWidth(content, 0, selectedIndexInLine-0);
        g.drawChars(content, selectedIndexInLine, 1, x, y);

        g.setColor(Color.black);
        x += g.getFontMetrics().charWidth(content[selectedIndexInLine]);
        g.drawChars(content, selectedIndexInLine+1, (content.length-1)-selectedIndexInLine, x, y);
        g.setFont(HexView.FONT);
    }
    
    @Override
    public void hexModelChanged(HexModelChangedEvent event)
    {
        repaint();
    }

    /**
     * Updates the line text for a given index. It is used when a byte is
     * selected in hex pane.
     * @param index int.
     */
    void setSelected(int index)
    {
            selectedLine = HexModel.lineNumber(index);
            selectedIndexInLine = HexModel.elementIndexInLine(index);
            repaint();
    }
}
