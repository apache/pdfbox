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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * @author Khyrul Bashar
 *
 * A class that provides the container for the image in case of image showing in stream pane.
 */
class StreamImageView
{
    private final BufferedImage image;
    private JScrollPane scrollPane;

    /**
     * constructor.
     * @param image instance of BufferedImage.
     */
    StreamImageView(BufferedImage image)
    {
        this.image = image;
        initUI();
    }

    private void initUI()
    {
        scrollPane = new JScrollPane();
        JLabel imageLabel = new JLabel(new ImageIcon(image));
        scrollPane.setViewportView(imageLabel);
        scrollPane.setPreferredSize(new Dimension(300, 400));
    }

    /**
     * Returns the view i.e container containing image.
     * @return A JComponent instance.
     */
    JComponent getView()
    {
        return scrollPane;
    }
}