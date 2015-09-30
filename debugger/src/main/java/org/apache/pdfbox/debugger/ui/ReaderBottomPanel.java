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
package org.apache.pdfbox.debugger.ui;

import java.awt.Dimension;

import javax.swing.JPanel;

import javax.swing.JLabel;
import java.awt.FlowLayout;
/**
 * A panel to display at the bottom of the window for status and other stuff.
 *
 * @author Ben Litchfield
 */
public class ReaderBottomPanel extends JPanel
{

    private JLabel statusLabel = null;

    /**
     * This is the default constructor.
     */
    public ReaderBottomPanel()
    {
        FlowLayout flowLayout = new FlowLayout();
        this.setLayout(flowLayout);
        this.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
        this.setPreferredSize(new Dimension(1000, 20));
        flowLayout.setAlignment(FlowLayout.LEFT);
        statusLabel = new JLabel();
        statusLabel.setText("Ready");
        this.add(statusLabel, null);
    }

    /**
     * Return the status label.
     *
     * @return JLabel The status label.
     */
    public JLabel getStatusLabel()
    {
        return statusLabel;
    }
}
