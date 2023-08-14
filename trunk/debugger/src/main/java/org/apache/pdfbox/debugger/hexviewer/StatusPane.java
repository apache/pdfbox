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

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Khyrul Bashar
 *
 * This shows the currently selected bytes, index, line number and column number.
 */
class StatusPane extends JPanel
{
    private static final int HEIGHT = 20;
    private JLabel lineLabel;
    private JLabel colLabel;
    private JLabel indexLabel;

    StatusPane()
    {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        createView();
    }

    private void createView()
    {
        JLabel line = new JLabel("Line:");
        JLabel column = new JLabel("Column:");
        lineLabel = new JLabel("");
        lineLabel.setPreferredSize(new Dimension(100, HEIGHT));
        colLabel = new JLabel("");
        colLabel.setPreferredSize(new Dimension(100, HEIGHT));
        JLabel index = new JLabel("Index:");
        indexLabel = new JLabel("");

        add(line);
        add(lineLabel);
        add(column);
        add(colLabel);
        add(index);
        add(indexLabel);
    }

    void updateStatus(int index)
    {
        lineLabel.setText(String.valueOf(HexModel.lineNumber(index)));
        colLabel.setText(String.valueOf(HexModel.elementIndexInLine(index)+1));
        indexLabel.setText(String.valueOf(index));
    }
}
