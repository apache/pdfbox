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
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;
import javax.swing.ImageIcon;
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
        public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int row, int col)
        {
            if (o instanceof GeneralPath)
            {
                GeneralPath path = (GeneralPath) o;
                Rectangle2D bounds2D = path.getBounds2D();
                if (bounds2D.isEmpty())
                {
                    JLabel label = new JLabel(SimpleFont.NO_GLYPH, SwingConstants.CENTER);
                    label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 25));
                    label.setForeground(Color.RED);
                    return label;
                }
                Rectangle cellRect = jTable.getCellRect(row, col, false);
                BufferedImage bim = renderGlyph(path, bounds2D, cellRect);
                return new JLabel(new ImageIcon(bim));

                //TODO possible improvement? render all glyphs with the same scale in the same bounding box
            }
            if (o != null)
            {
                JLabel label = new JLabel(o.toString(), SwingConstants.CENTER);
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

        private BufferedImage renderGlyph(GeneralPath path, Rectangle2D bounds2D, Rectangle cellRect)
        {
            path.transform(AffineTransform.getTranslateInstance(-bounds2D.getMinX(), -bounds2D.getMinY()));
            double scaleX = bounds2D.getWidth() / cellRect.getWidth();
            double scaleY = bounds2D.getHeight() / cellRect.getHeight();
            double scale = 1 / Math.ceil(Math.max(scaleX, scaleY)) / 2;
            BufferedImage bim = new BufferedImage((int) cellRect.getWidth(), (int) cellRect.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = (Graphics2D) bim.getGraphics();
            g.setBackground(Color.white);
            g.clearRect(0, 0, bim.getWidth(), bim.getHeight());

            // flip
            g.scale(1, -1);
            g.translate(0, -bim.getHeight());

            // center
            g.translate((cellRect.getWidth() - bounds2D.getWidth() * scale) / 2, cellRect.getHeight() / 4);

            g.scale(scale, scale);
            g.setColor(Color.black);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.fill(path);
            g.dispose();
            return bim;
        }
    }
}
