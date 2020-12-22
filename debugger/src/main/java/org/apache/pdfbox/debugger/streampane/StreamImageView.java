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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.pdfbox.debugger.ui.HighResolutionImageIcon;
import org.apache.pdfbox.debugger.ui.ImageUtil;
import org.apache.pdfbox.debugger.ui.RotationMenu;
import org.apache.pdfbox.debugger.ui.ZoomMenu;

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
    private RotationMenu rotationMenu;

    /**
     * constructor.
     * @param image instance of BufferedImage.
     */
    StreamImageView(final BufferedImage image)
    {
        this.image = image;
        initUI();
    }

    private void initUI()
    {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        zoomMenu = ZoomMenu.getInstance();
        zoomMenu.changeZoomSelection(zoomMenu.getImageZoomScale());

        label = new JLabel();
        label.setBorder(new LineBorder(Color.BLACK));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        addImage(zoomImage(image, zoomMenu.getImageZoomScale(), RotationMenu.getRotationDegrees()));

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

    private Image zoomImage(final BufferedImage origin, final float scale, final int rotation)
    {
        final BufferedImage rotatedImage = ImageUtil.getRotatedImage(origin, rotation);
        final int resizedWidth = (int) (rotatedImage.getWidth() * scale);
        final int resizedHeight = (int) (rotatedImage.getHeight() * scale);
        return rotatedImage.getScaledInstance(resizedWidth, resizedHeight, Image.SCALE_SMOOTH);
    }

    @Override
    public void actionPerformed(final ActionEvent actionEvent)
    {
        final String actionCommand = actionEvent.getActionCommand();
        if (ZoomMenu.isZoomMenu(actionCommand) || RotationMenu.isRotationMenu(actionCommand))
        {
            addImage(zoomImage(image, ZoomMenu.getZoomScale(), RotationMenu.getRotationDegrees()));
            zoomMenu.setImageZoomScale(ZoomMenu.getZoomScale());
        }
    }

    private void addImage(final Image img)
    {
        // for JDK9; see explanation in PagePane
        final AffineTransform tx = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
        label.setSize((int) Math.ceil(img.getWidth(null) / tx.getScaleX()), 
                      (int) Math.ceil(img.getHeight(null) / tx.getScaleY()));
        label.setIcon(new HighResolutionImageIcon(img, label.getWidth(), label.getHeight()));
        label.revalidate();
    }

    @Override
    public void ancestorAdded(final AncestorEvent ancestorEvent)
    {
        zoomMenu.addMenuListeners(this);
        zoomMenu.setEnableMenu(true);
        
        rotationMenu = RotationMenu.getInstance();
        rotationMenu.addMenuListeners(this);
        rotationMenu.setRotationSelection(RotationMenu.ROTATE_0_DEGREES);
        rotationMenu.setEnableMenu(true);
    }

    @Override
    public void ancestorRemoved(final AncestorEvent ancestorEvent)
    {
        zoomMenu.setEnableMenu(false);
        rotationMenu.setEnableMenu(false);
    }

    @Override
    public void ancestorMoved(final AncestorEvent ancestorEvent)
    {
    }
}
