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
package org.apache.pdfbox.debugger.streampane;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.text.StyledDocument;

import org.apache.pdfbox.debugger.streampane.tooltip.ToolTipController;

/**
 * @author Khyrul Bashar
 */
class StreamPaneView
{
    private final JPanel contentPanel;

    /**
     * Constructor.
     */
    StreamPaneView()
    {
        contentPanel = new JPanel(new BorderLayout());
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
        contentPanel.validate();
    }


    /**
     * This shows the stream as image.
     * @param image BufferedImage instance that holds the text.
     */
    void showStreamImage(BufferedImage image)
    {
        contentPanel.removeAll();
        contentPanel.add(new StreamImageView(image).getView(), BorderLayout.CENTER);
        contentPanel.validate();
    }

    public JPanel getStreamPanel()
    {
        return contentPanel;
    }
}
