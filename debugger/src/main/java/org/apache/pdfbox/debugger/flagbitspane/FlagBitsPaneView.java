/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.pdfbox.debugger.flagbitspane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * @author Khyrul Bashar
 *
 * A class that create the necessary UI for showing Flag bits.
 */
class FlagBitsPaneView
{
    final private JPanel panel;
    final private String flagHeader;
    final private String flagValue;
    final private Object[][] tableData;
    final private String[] columnNames;

    /**
     * Constructor
     * @param flagHeader String instance. Flag type.
     * @param flagValue String instance. Flag integer value.
     * @param tableRowData Object 2d array for table row data.
     * @param columnNames String array for column names.
     */
    FlagBitsPaneView(String flagHeader, String flagValue, Object[][] tableRowData, String[] columnNames)
    {
        this.flagHeader = flagHeader;
        this.flagValue = flagValue;
        this.tableData = tableRowData;
        this.columnNames = columnNames;
        panel = new JPanel();

        if (flagValue != null && tableData != null)
        {
            createView();
        }
    }

    private void createView()
    {
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(300, 500));

        JLabel flagLabel = new JLabel(flagHeader);
        flagLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        flagLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        JPanel flagLabelPanel = new JPanel();
        flagLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        flagLabelPanel.add(flagLabel);

        JLabel flagValueLabel = new JLabel(flagValue);
        flagValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        flagValueLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));

        JTable table = new JTable(tableData, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        Box box = Box.createVerticalBox();
        box.add(flagValueLabel);
        box.add(scrollPane);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_START;

        panel.add(flagLabelPanel, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.9;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE;

        panel.add(box, gbc);
    }

    /**
     * Returns the view.
     * @return JPanel instance.
     */
    JPanel getPanel()
    {
        return panel;
    }
}
