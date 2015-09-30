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
import java.util.Arrays;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;


/**
 *@author Khyrul Bashar.
 */

/**
 * A class that provides the necessary UI and functionalities to show the DeviceN color space.
 */
public class CSDeviceN
{
    private PDDeviceN deviceN;
    private JPanel panel;

    /**
     * Constructor
     *
     * @param array COSArray instance that holds DeviceN color space
     */
    public CSDeviceN(COSArray array)
    {
        try
        {
            deviceN = new PDDeviceN(array);
            DeviceNColorant[] colorants = getColorantData();
            initUI(colorants);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the colorant data from the array.
     *
     * @return the parsed colorants.
     * @throws java.io.IOException if the color conversion fails.
     */
    private DeviceNColorant[] getColorantData() throws IOException
    {
        int componentCount = deviceN.getNumberOfComponents();
        DeviceNColorant[] colorants = new DeviceNColorant[componentCount];
        for (int i = 0; i < componentCount; i++)
        {
            DeviceNColorant colorant = new DeviceNColorant();

            colorant.setName(deviceN.getColorantNames().get(i));
            float[] maximum = new float[componentCount];
            Arrays.fill(maximum, 0);
            float[] minimum = new float[componentCount];
            Arrays.fill(minimum, 0);
            maximum[i] = 1;
            colorant.setMaximum(getColorObj(deviceN.toRGB(maximum)));
            colorant.setMinimum(getColorObj(deviceN.toRGB(minimum)));
            colorants[i] = colorant;
        }
        return colorants;
    }

    private void initUI(DeviceNColorant[] colorants)
    {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 500));

        JLabel colorSpaceLabel = new JLabel("DeviceN colorspace");
        colorSpaceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        colorSpaceLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));

        DeviceNTableModel tableModel = new DeviceNTableModel(colorants);
        JTable table = new JTable(tableModel);
        table.setDefaultRenderer(Color.class, new ColorBarCellRenderer());
        table.setRowHeight(60);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);

        panel.add(colorSpaceLabel);
        panel.add(scrollPane);
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

    private Color getColorObj(float[] rgbValues)
    {
        return new Color(rgbValues[0], rgbValues[1], rgbValues[2]);
    }
}
