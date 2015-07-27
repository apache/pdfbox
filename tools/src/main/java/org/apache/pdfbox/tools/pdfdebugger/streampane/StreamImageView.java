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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.apache.pdfbox.tools.pdfdebugger.ui.ZoomMenu;

/**
 * @author Khyrul Bashar
 *
 * A class that provides the container for the image in case of image showing in stream pane.
 */
class StreamImageView implements ActionListener, AncestorListener
{
    private final BufferedImage image;
    private JScrollPane scrollPane;
    private JLabel label;
    private ZoomMenu zoomMenu;

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
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        label = new JLabel();
        label.setBorder(new LineBorder(Color.BLACK));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setIcon(new ImageIcon(image));

        panel.add(Box.createVerticalGlue());
        panel.add(label);
        panel.add(Box.createVerticalGlue());

        scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(300, 400));
        scrollPane.addAncestorListener(this);
        scrollPane.setViewportView(panel);
    }

    /**
     * Returns the view i.e container containing image.
     * @return A JComponent instance.
     */
    JComponent getView()
    {
        return scrollPane;
    }

    private Image zoomImage(BufferedImage origin, float scale)
    {
        int resizedWidth = (int) (origin.getWidth()*scale);
        int resizedHeight = (int) (origin.getHeight()*scale);
        return origin.getScaledInstance(resizedWidth, resizedHeight, BufferedImage.SCALE_SMOOTH);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        String actionCommand = actionEvent.getActionCommand();
        if (ZoomMenu.isZoomMenu(actionCommand))
        {
            addImage(zoomImage(image, ZoomMenu.getZoomScale(actionCommand)));
        }
    }

    private void addImage(Image img)
    {
        label.setIcon(new ImageIcon(img));
        label.revalidate();
    }

    @Override
    public void ancestorAdded(AncestorEvent ancestorEvent)
    {
        zoomMenu = ZoomMenu.getInstance().menuListeners(this);
        zoomMenu.setZoomSelection(ZoomMenu.ZOOM_100_PERCENT);
        zoomMenu.setEnableMenu(true);
    }

    @Override
    public void ancestorRemoved(AncestorEvent ancestorEvent)
    {
        zoomMenu.setEnableMenu(false);
    }

    @Override
    public void ancestorMoved(AncestorEvent ancestorEvent)
    {

    }
}
