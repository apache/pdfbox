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

package org.apache.pdfbox.tools.pdfdebugger.hexviewer;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * @author Khyrul Bashar
 *
 * This class builds the upper banner in the hex view.
 */
class UpperPane extends JPanel
{
    UpperPane()
    {
        setFont(HexView.BOLD_FONT);
        setPreferredSize(new Dimension(HexView.TOTAL_WIDTH, 20));
        setBorder(new BevelBorder(BevelBorder.RAISED));
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        int x = HexView.LINE_INSET-2;
        int y = 16;

        g.drawString("Offset", x, y);

        x += HexView.ADDRESS_PANE_WIDTH;

        for (int i = 0; i <= 15; i++)
        {
            g.drawString(String.format("%02X", i), x, y);
            x += HexView.CHAR_WIDTH;
        }

        x+=HexView.LINE_INSET*2;
        g.drawString("Dump", x, y);
    }
}
