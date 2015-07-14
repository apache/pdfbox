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
package org.apache.pdfbox.tools.pdfdebugger.streampane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.text.StyledDocument;
import org.apache.pdfbox.tools.pdfdebugger.streampane.tooltip.ToolTipController;

/**
 * @author Khyrul Bashar
 */
class StreamPaneView extends JPanel
{
    private final JPanel headerPanel;
    private final JPanel contentPanel;

    /**
     * Constructor.
     * @param filterTypes String array that provides a list of key for available stream versions.
     * @param initialChoice String instance. Initial choice for showing in the pane.
     * @param listener ActionListener that listens for stream version choice changing.
     */
    StreamPaneView(List<String> filterTypes, String initialChoice, ActionListener listener)
    {
        headerPanel = createHeaderPanel(filterTypes, initialChoice, listener);
        contentPanel = new JPanel(new BorderLayout());
        initUI();
    }

    /**
     * This shows the stream in text for any of  it's filtered or unfiltered version.
     * @param document StyledDocument instance that holds the text.
     * @param toolTipController ToolTipController instance.
     */
    void showStreamText(StyledDocument document, ToolTipController toolTipController)
    {
        contentPanel.removeAll();

        StreamTextView textView = new StreamTextView(document, toolTipController);
        contentPanel.add(textView.getView(), BorderLayout.CENTER);
        this.validate();
    }


    /**
     * This shows the stream as image.
     * @param image BufferedImage instance that holds the text.
     */
    void showStreamImage(BufferedImage image)
    {
        contentPanel.removeAll();
        contentPanel.add(new StreamImageView(image).getView(), BorderLayout.CENTER);
        this.validate();
    }

    private JPanel createHeaderPanel(List<String> availableFilters, String i, ActionListener actionListener)
    {
        JComboBox filters = new JComboBox(availableFilters.toArray());
        filters.setSelectedItem(i);
        filters.addActionListener(actionListener);

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(filters);

        return panel;
    }

    public JPanel getStreamPanel()
    {
        return this;
    }

    private void initUI()
    {
        this.setPreferredSize(new Dimension(300, 500));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(headerPanel);
        this.add(contentPanel);
    }
}
