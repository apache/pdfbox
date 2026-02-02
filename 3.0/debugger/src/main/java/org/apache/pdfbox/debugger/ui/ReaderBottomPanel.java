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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * A panel to display at the bottom of the window for status and other stuff.
 *
 * @author Ben Litchfield
 */
public class ReaderBottomPanel extends JPanel
{
    private JLabel statusLabel = null;
    private JLabel logLabel = null;
    
    public ReaderBottomPanel()
    {
        BorderLayout layout = new BorderLayout();
        this.setLayout(layout);
        
        statusLabel = new JLabel();
        statusLabel.setText("Ready");
        this.add(statusLabel, BorderLayout.WEST);

        logLabel = new JLabel();
        logLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Window viewer = LogDialog.instance().getOwner();
                
                // show the log window
                LogDialog.instance().setSize(800, 400);
                LogDialog.instance().setVisible(true);
                LogDialog.instance().setLocation(viewer.getLocationOnScreen().x + viewer.getWidth() / 2,
                                                 viewer.getLocationOnScreen().y + viewer.getHeight() / 2);
            }
        });
        this.add(logLabel, BorderLayout.EAST);

        this.setBorder(new EmptyBorder(0, 5, 0, 5));
        this.setPreferredSize(new Dimension(1000, 24));
    }
    
    public JLabel getStatusLabel()
    {
        return statusLabel;
    }

    public JLabel getLogLabel()
    {
        return logLabel;
    }
}
