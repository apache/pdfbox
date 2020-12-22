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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
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
import org.apache.pdfbox.debugger.PDFDebugger;
import org.apache.pdfbox.debugger.ui.HighResolutionImageIcon;

/**
 * @author Khyrul Bashar
 * @author Tilman Hausherr
 * A class that creates the UI for font encoding pane.
 */
class FontEncodingView
{
    private JPanel panel;

    private static final AffineTransform DEFAULT_TRANSFORM = GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();

    /**
     * Constructor.
     * @param tableData Object[][] instance as table data.
     * @param headerAttributes Map<String, String> instance which contains info for showing in header
     *                         panel. Here keys will be info type.
     * @param columnNames String array containing the columns name.
     * @param yBounds min low and max high bound of all glyphs.
     */
    FontEncodingView(final Object[][] tableData, final Map<String, String> headerAttributes, final String[] columnNames, final double[] yBounds)
    {
        createView(getHeaderPanel(headerAttributes), getTable(tableData, columnNames, yBounds));
    }

    private void createView(final JPanel headerPanel, final JTable table)
    {
        panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(300, 500));

        final JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        final GridBagConstraints gbc = new GridBagConstraints();
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

    private JTable getTable(final Object[][] tableData, final String[] columnNames, final double[] yBounds)
    {
        final JTable table = new JTable(tableData, columnNames);
        table.setRowHeight(40);
        table.setDefaultRenderer(Object.class, new GlyphCellRenderer(yBounds));
        return table;
    }

    private JPanel getHeaderPanel(final Map<String, String> attributes)
    {
        final JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        if (attributes != null)
        {
            final Iterator<String> keys = attributes.keySet().iterator();
            int row = 0;
            while (keys.hasNext())
            {
                final int fontSize = Integer.parseInt(PDFDebugger.configuration.getProperty(
                                    "headerFontSize", Integer.toString(headerPanel.getFont().getSize())));
                final String key = keys.next();
                final JLabel encodingNameLabel = new JLabel(key + ": " + attributes.get(key));
                encodingNameLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, fontSize));
                encodingNameLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

                final GridBagConstraints gbc = new GridBagConstraints();
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
        private final double[] yBounds;

        private GlyphCellRenderer(final double[] yBounds)
        {
            this.yBounds = yBounds;
        }

        @Override
        public Component getTableCellRendererComponent(final JTable jTable, final Object o, final boolean b, final boolean b1, final int row, final int col)
        {
            if (o instanceof GeneralPath)
            {
                final GeneralPath path = (GeneralPath) o;
                final Rectangle2D bounds2D = path.getBounds2D();
                if (bounds2D.isEmpty())
                {
                    final JLabel label = new JLabel(SimpleFont.NO_GLYPH, SwingConstants.CENTER);
                    final int fontSize = Integer.parseInt(PDFDebugger.configuration.getProperty(
                                        "encodingFontSize", Integer.toString(label.getFont().getSize())));
                    label.setFont(new Font(Font.DIALOG, Font.PLAIN, fontSize));
                    label.setForeground(Color.GRAY);
                    return label;
                }
                final Rectangle cellRect = jTable.getCellRect(row, col, false);
                final BufferedImage bim = renderGlyph(path, bounds2D, cellRect);
                return new JLabel(new HighResolutionImageIcon(
                                   bim, 
                                   (int) Math.ceil(bim.getWidth() / DEFAULT_TRANSFORM.getScaleX()), 
                                   (int) Math.ceil(bim.getHeight() / DEFAULT_TRANSFORM.getScaleY())), 
                                  SwingConstants.CENTER);
            }
            if (o instanceof BufferedImage)
            {
                final Rectangle cellRect = jTable.getCellRect(row, col, false);
                final BufferedImage glyphImage = (BufferedImage) o;
                final BufferedImage cellImage = new BufferedImage(
                        (int) (cellRect.getWidth() * DEFAULT_TRANSFORM.getScaleX()),
                        (int) (cellRect.getHeight() * DEFAULT_TRANSFORM.getScaleY()),
                        BufferedImage.TYPE_INT_RGB);
                final Graphics2D g = (Graphics2D) cellImage.getGraphics();
                g.setBackground(Color.white);
                g.clearRect(0, 0, cellImage.getWidth(), cellImage.getHeight());

                final double scale = 1 / (glyphImage.getHeight() / cellRect.getHeight());

                // horizontal center
                g.translate((cellRect.getWidth() - glyphImage.getWidth() * scale) / 2 * DEFAULT_TRANSFORM.getScaleX(), 0);

                // scale from the glyph to the cell
                g.scale(scale * DEFAULT_TRANSFORM.getScaleX(), scale * DEFAULT_TRANSFORM.getScaleY());

                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);              
                g.drawImage(glyphImage, 0, 0, null);
                g.dispose();
                return new JLabel(new HighResolutionImageIcon(
                                   cellImage,
                                   (int) Math.ceil(cellImage.getWidth() / DEFAULT_TRANSFORM.getScaleX()), 
                                   (int) Math.ceil(cellImage.getHeight() / DEFAULT_TRANSFORM.getScaleY())));
            }
            if (o != null)
            {
                final JLabel label = new JLabel(o.toString(), SwingConstants.CENTER);
                final int fontSize = Integer.parseInt(PDFDebugger.configuration.getProperty(
                        "encodingFontSize", Integer.toString(label.getFont().getSize())));
                label.setFont(new Font(Font.DIALOG, Font.PLAIN, fontSize));
                if (SimpleFont.NO_GLYPH.equals(o) || ".notdef".equals(o))
                {
                    label.setText(o.toString());
                    label.setForeground(Color.GRAY);
                }
                return label;
            }
            return new JLabel();
        }

        private BufferedImage renderGlyph(final GeneralPath path, final Rectangle2D bounds2D, final Rectangle cellRect)
        {
            final BufferedImage bim = new BufferedImage(
                    (int) (cellRect.getWidth() * DEFAULT_TRANSFORM.getScaleX()),
                    (int) (cellRect.getHeight() * DEFAULT_TRANSFORM.getScaleY()),
                    BufferedImage.TYPE_INT_RGB);
            final Graphics2D g = (Graphics2D) bim.getGraphics();
            g.setBackground(Color.white);
            g.clearRect(0, 0, bim.getWidth(), bim.getHeight());

            final double scale = 1 / ((yBounds[1] - yBounds[0]) / cellRect.getHeight());

            // flip
            g.scale(1, -1);
            g.translate(0, -bim.getHeight());

            // horizontal center
            g.translate((cellRect.getWidth() - bounds2D.getWidth() * scale) / 2 * DEFAULT_TRANSFORM.getScaleX(), 0);

            // scale from the glyph to the cell
            g.scale(scale * DEFAULT_TRANSFORM.getScaleX(), scale * DEFAULT_TRANSFORM.getScaleY());

            // Adjust for negative y min bound
            g.translate(0, -yBounds[0]);

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
