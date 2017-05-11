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
package org.apache.pdfbox.debugger.colorpane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;

/**
 * @author Khyrul Bashar.
 */

/**
 * A class that provides the necessary UI and functionalities to show the Indexed colorspace.
 */
public class CSIndexed
{
    private PDIndexed indexed;
    private JPanel panel;
    private int colorCount;

    /**
     * Constructor.
     * @param array COSArray instance for Indexed Colorspace.
     */
    public CSIndexed(COSArray array)
    {
        try
        {
            indexed = new PDIndexed(array);
            colorCount = getHival(array) + 1;
            initUI(getColorantData());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the colorant data from the array and return.
     *
     * @return the colorant data
     */
    private IndexedColorant[] getColorantData()
    {
        IndexedColorant[] colorants = new IndexedColorant[colorCount];
        for (int i = 0; i < colorCount; i++)
        {
            IndexedColorant colorant = new IndexedColorant();
            colorant.setIndex(i);

            float[] rgbValues = indexed.toRGB(new float[]{i});
            colorant.setRgbValues(rgbValues);
            colorants[i] = colorant;
        }
        return colorants;
    }

    private void initUI(IndexedColorant[] colorants)
    {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(300, 500));

        JLabel colorSpaceLabel = new JLabel("Indexed colorspace");
        colorSpaceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        colorSpaceLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        JPanel colorspaceLabelPanel = new JPanel();
        colorspaceLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        colorspaceLabelPanel.add(colorSpaceLabel);

        JLabel colorCountLabel = new JLabel(" Total Color Count: " + colorCount);
        colorCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        colorCountLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));

        IndexedTableModel tableModel = new IndexedTableModel(colorants);
        JTable table = new JTable(tableModel);
        table.setDefaultRenderer(Color.class, new ColorBarCellRenderer());
        table.setRowHeight(40);
        table.getColumnModel().getColumn(0).setMinWidth(30);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(1).setMaxWidth(100);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        Box box = Box.createVerticalBox();
        box.add(colorCountLabel);
        box.add(scrollPane);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_START;

        panel.add(colorspaceLabelPanel, gbc);

        gbc.gridy = 2;
        gbc.weighty=0.9;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE;

        panel.add(box, gbc);
    }

    /**
     * return the main panel that hold all the UI elements.
     *
     * @return JPanel instance
     */
    public Component getPanel()
    {
        return panel;
    }

    private int getHival(COSArray array)
    {
        int hival = ((COSNumber) array.getObject(2).getCOSObject()).intValue();
        return Math.min(hival, 255);
    }
}
