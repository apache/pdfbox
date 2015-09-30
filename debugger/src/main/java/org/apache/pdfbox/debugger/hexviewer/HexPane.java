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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * @author Khyrul Bashar
 *
 * HexPane shows the byte in a Grid table where every row has 16 bytes. It only draws bytes those are
 * only visible at a given time.
 */
class HexPane extends JPanel implements KeyListener, MouseListener, MouseMotionListener, HexModelChangeListener
{
    private final HexModel model;
    private int selectedIndex = -1;
    private static final byte EDIT = 2;
    private static final byte SELECTED = 1;
    private static final byte NORMAL = 0;

    private byte state = NORMAL;
    private int selectedChar = 0;

    private final List<HexChangeListener> hexChangeListeners = new ArrayList<HexChangeListener>();
    private final List<SelectionChangeListener> selectionChangeListeners = new ArrayList<SelectionChangeListener>();

    /**
     * Constructor.
     * @param model HexModel instance.
     */
    HexPane(HexModel model)
    {
        this.model = model;
        model.addHexModelChangeListener(this);
        setPreferredSize(new Dimension(HexView.HEX_PANE_WIDTH, HexView.CHAR_HEIGHT * (model.totalLine()+1)));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
        setAutoscrolls(true);
        setFont(HexView.FONT);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHints(HexView.RENDERING_HINTS);
        
        Rectangle bound = getVisibleRect();
        g.clearRect(bound.x, bound.y, bound.width, bound.height);
        g.setColor(Color.WHITE);
        g.fillRect(bound.x, bound.y, bound.width, bound.height);

        int x = HexView.LINE_INSET;
        int y = bound.y;
        if (y == 0 || y% HexView.CHAR_HEIGHT != 0)
        {
            y += HexView.CHAR_HEIGHT - y% HexView.CHAR_HEIGHT;
        }
        int firstLine = y/ HexView.CHAR_HEIGHT;

        g.setColor(Color.BLACK);
        for (int i = firstLine; i <= firstLine + bound.height/ HexView.CHAR_HEIGHT; i++)
        {
            if (i > model.totalLine())
            {
                break;
            }
            byte[] bytes = model.getBytesForLine(i);
            int index = (i - 1) * 16;
            for (byte by : bytes)
            {
                String str = String.format("%02X", by);
                if (selectedIndex == index && state == SELECTED)
                {
                    g.drawString(getSelectedString(str).getIterator(), x, y);
                }
                else if (selectedIndex == index && state == EDIT)
                {
                    paintInEdit(g, by, x, y);
                }
                else
                {
                    g.drawString(str, x, y);
                }
                x += HexView.CHAR_WIDTH;
                index++;
            }
            x = HexView.LINE_INSET;
            y += HexView.CHAR_HEIGHT;
        }
    }

    private void paintInEdit(Graphics g, byte content, int x, int y)
    {
        g.setFont(HexView.BOLD_FONT);
        g.setColor(Color.white);

        char[] chars = getChars(content);

        if (selectedChar == 0)
        {
            g.setColor(HexView.SELECTED_COLOR);
            g.drawChars(chars, 0, 1, x, y);

            g.setColor(Color.black);
            g.drawChars(chars, 1, 1, x + g.getFontMetrics().charWidth(chars[0]), y);
        }
        else
        {
            g.setColor(Color.black);
            g.drawChars(chars, 0, 1, x, y);

            g.setColor(HexView.SELECTED_COLOR);
            g.drawChars(chars, 1, 1,x + g.getFontMetrics().charWidth(chars[0]), y);
        }
        setDefault(g);
    }

    private AttributedString getSelectedString(String str)
    {
        AttributedString string = new AttributedString(str);
        string.addAttribute(TextAttribute.FONT, HexView.BOLD_FONT);
        string.addAttribute(TextAttribute.FOREGROUND, HexView.SELECTED_COLOR);
        return string;
    }

    private void setDefault(Graphics g)
    {
        g.setColor(Color.black);
        g.setFont(this.getFont());
    }

    /**
     * Returns the index for a given point If there is any byte in there.
     * @param point Point instance.
     * @return index.
     */
    private int getIndexForPoint(Point point)
    {
        if (point.x <= 20 || point.x >= (16 * HexView.CHAR_WIDTH)+20 )
        {
            return -1;
        }
        int y = point.y;
        int lineNumber = (y+ (HexView.CHAR_HEIGHT -(y % HexView.CHAR_HEIGHT)))/ HexView.CHAR_HEIGHT;
        int x = point.x - 20;
        int elementNumber = (x / HexView.CHAR_WIDTH);
        return  (lineNumber-1) * 16 + elementNumber;
    }

    /**
     * Returns the starting point in the view for any index.
     * @param index int.
     * @return Point instance.
     */
    private Point getPointForIndex(int index)
    {
        int x = HexView.LINE_INSET + HexModel.elementIndexInLine(index)* HexView.CHAR_WIDTH;
        int y = HexModel.lineNumber(index) * HexView.CHAR_HEIGHT;
        return new Point(x, y);
    }

    /**
     * Puts an index in selected state or in other word it selects the byte of the index.
     * @param index
     */
    private void putInSelected(int index)
    {
        state = SELECTED;
        selectedChar = 0;
        Point point = getPointForIndex(index);
        //for column one
        if (index%16 == 0)
        {
            scrollRectToVisible(new Rectangle(0, HexModel.lineNumber(index) * HexView.CHAR_HEIGHT, 1, 1));
        }
        else if (!getVisibleRect().intersects(point.x, point.y, HexView.CHAR_WIDTH, HexView.CHAR_HEIGHT))
        {
            scrollRectToVisible(new Rectangle(point.x, point.y, HexView.CHAR_WIDTH, HexView.CHAR_HEIGHT));
        }
        selectedIndex = index;
        repaint();
        requestFocusInWindow();
    }

    private void fireSelectionChanged(SelectEvent event)
    {
        for (SelectionChangeListener listener:selectionChangeListeners)
        {
            listener.selectionChanged(event);
        }
    }

    private void fireHexValueChanged(byte value, int index)
    {
        for (HexChangeListener listener:hexChangeListeners)
        {
            listener.hexChanged(new HexChangedEvent(value, index));
        }
    }

    public void addSelectionChangeListener(SelectionChangeListener listener)
    {
        selectionChangeListeners.add(listener);
    }

    public void addHexChangeListeners(HexChangeListener listener)
    {
        hexChangeListeners.add(listener);
    }

    @Override
    public void keyTyped(KeyEvent keyEvent)
    {
        if (selectedIndex != -1)
        {
            char c = keyEvent.getKeyChar();
            if (isHexChar(c))
            {
                byte previousByte = model.getByte(selectedIndex);
                char[] chars = getChars(previousByte);
                chars[selectedChar] = c;
                byte editByte = getByte(chars);
                if (selectedChar == 0)
                {
                    state = EDIT;
                    selectedChar = 1;
                    fireHexValueChanged(editByte, selectedIndex);
                }
                else
                {
                    fireHexValueChanged(editByte, selectedIndex);
                    fireSelectionChanged(new SelectEvent(selectedIndex, SelectEvent.NEXT));
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent keyEvent)
    {
        if (state == SELECTED || state == EDIT)
        {
            switch (keyEvent.getKeyCode())
            {
                case 37:
                    if (state == EDIT && selectedChar == 1)
                    {
                        selectedChar = 0;
                        repaint();
                    }
                    else
                    {
                        fireSelectionChanged(new SelectEvent(selectedIndex, SelectEvent.PREVIOUS));
                    }
                    break;
                case 39:
                    fireSelectionChanged(new SelectEvent(selectedIndex, SelectEvent.NEXT));
                    break;
                case 38:
                    fireSelectionChanged(new SelectEvent(selectedIndex, SelectEvent.UP));
                    break;
                case 40:
                    fireSelectionChanged(new SelectEvent(selectedIndex, SelectEvent.DOWN));
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void keyReleased(KeyEvent keyEvent)
    {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent)
    {
        int index = getIndexForPoint(mouseEvent.getPoint());
        if (index == -1)
        {
            fireSelectionChanged(new SelectEvent(-1, SelectEvent.NONE));
            return;
        }
        fireSelectionChanged(new SelectEvent(index, SelectEvent.IN));
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent)
    {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent)
    {
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent)
    {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent)
    {

    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent)
    {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent)
    {

    }

    private static boolean isHexChar(char c)
    {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private char[] getChars(byte b)
    {
        return String.format("%02X", b & 0XFF).toCharArray();
    }

    private byte getByte(char[] chars)
    {
        return (byte) (Integer.parseInt(new String(chars), 16) & 0XFF);
    }

    public void setSelected(int index)
    {
        if (index != selectedIndex)
        {
            putInSelected(index);
        }
    }

    @Override
    public void hexModelChanged(HexModelChangedEvent event)
    {
        repaint();
    }
}
