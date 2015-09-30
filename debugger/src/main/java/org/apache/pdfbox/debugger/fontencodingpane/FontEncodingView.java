/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.debugger.fontencodingpane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * @author Khyrul Bashar
 * A class that creates the UI for font encoding pane.
 */
class FontEncodingView
{
    private JPanel panel;

    /**
     * Constructor.
     * @param tableData Object[][] instance as table data.
     * @param headerAttributes Map<String, String> instance which contains info for showing in header
     *                         panel. Here keys will be info type.
     * @param columnNames String array containing the columns name.
     */
    FontEncodingView(Object[][] tableData, Map<String, String> headerAttributes, String[] columnNames)
    {
        createView(getHeaderPanel(headerAttributes), getTable(tableData, columnNames));
    }

    private void createView(JPanel headerPanel, JTable table)
    {
        panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(300, 500));

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_START;

        panel.add(headerPanel, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.9;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE;

        panel.add(scrollPane, gbc);
    }

    private JTable getTable(Object[][] tableData, String[] columnNames)
    {
        JTable table = new JTable(tableData, columnNames);
        table.setRowHeight(40);
        table.setDefaultRenderer(Object.class, new GlyphCellRenderer());
        return table;
    }

    private JPanel getHeaderPanel(Map<String, String> attributes)
    {
        JPanel headerPanel = new JPanel(new GridBagLayout());

        if (attributes != null)
        {
            Iterator<String> keys = attributes.keySet().iterator();
            int row = 0;
            while (keys.hasNext())

            {
                String key = keys.next();
                JLabel encodingNameLabel = new JLabel(key + ": " + attributes.get(key));
                encodingNameLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = row++;
                gbc.weighty = 0.1;
                gbc.anchor = GridBagConstraints.LINE_START;

                headerPanel.add(encodingNameLabel, gbc);

            }
        }
        return headerPanel;
    }

    JPanel getPanel()
    {
        return panel;
    }

    private static final class GlyphCellRenderer implements TableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1)
        {
            if (o != null)
            {
                JLabel label = new JLabel(o.toString());
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 25));
                if (SimpleFont.NO_GLYPH.equals(o) || ".notdef".equals(o))
                {
                    label.setText(o.toString());
                    label.setForeground(Color.RED);
                }
                return label;
            }
            return new JLabel();
        }
    }
}


