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

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import org.apache.pdfbox.rendering.ImageType;

/**
 * @author Tilman Hausherr
 *
 * A singleton class that provides the imagetype menu for the menubar. To act upon the menu item
 * selection, the user of the class must add ActionListener which will check for the action command
 * and act accordingly.
 */
public final class ImageTypeMenu extends MenuBase
{
    public static final String IMAGETYPE_RGB = "RGB";
    public static final String IMAGETYPE_ARGB = "ARGB";
    public static final String IMAGETYPE_GRAY = "Gray";
    public static final String IMAGETYPE_BITONAL = "Bitonal";
    
    private static ImageTypeMenu instance;
    private JRadioButtonMenuItem rgbItem;
    private JRadioButtonMenuItem argbItem;
    private JRadioButtonMenuItem grayItem;
    private JRadioButtonMenuItem bitonalItem;

    /**
     * Constructor.
     */
    private ImageTypeMenu()
    {
        setMenu(createMenu());
    }
  
    /**
     * Provides the ImageTypeMenu instance.
     * @return ImageTypeMenu instance.
     */
    public static ImageTypeMenu getInstance()
    {
        if (instance == null)
        {
            instance = new ImageTypeMenu();
        }
        return instance;
    }

    /**
     * Set the image type selection.
     * @param selection String instance.
     */
    public void setImageTypeSelection(String selection)
    {
        switch (selection)
        {
            case IMAGETYPE_RGB:
                rgbItem.setSelected(true);
                break;
            case IMAGETYPE_ARGB:
                argbItem.setSelected(true);
                break;
            case IMAGETYPE_GRAY:
                grayItem.setSelected(true);
                break;
            case IMAGETYPE_BITONAL:
                bitonalItem.setSelected(true);
                break;
            default:
                throw new IllegalArgumentException("Invalid ImageType selection: " + selection);
        }
    }

    public static boolean isImageTypeMenu(String actionCommand)
    {
        return IMAGETYPE_RGB.equals(actionCommand) || IMAGETYPE_ARGB.equals(actionCommand) ||
                IMAGETYPE_GRAY.equals(actionCommand) || IMAGETYPE_BITONAL.equals(actionCommand);
    }
    
    public static ImageType getImageType()
    {
        if (instance.argbItem.isSelected())
        {
            return ImageType.ARGB;
        }
        if (instance.grayItem.isSelected())
        {
            return ImageType.GRAY;
        }
        if (instance.bitonalItem.isSelected())
        {
            return ImageType.BINARY;
        }
        return ImageType.RGB;
    }

    public static ImageType getImageType(String actionCommand)
    {
        switch (actionCommand)
        {
            case IMAGETYPE_RGB:
                return ImageType.RGB;
            case IMAGETYPE_ARGB:
                return ImageType.ARGB;
            case IMAGETYPE_GRAY:
                return ImageType.GRAY;
            case IMAGETYPE_BITONAL:
                return ImageType.BINARY;
            default:
                throw new IllegalArgumentException("Invalid ImageType actionCommand: " + actionCommand);
        }
    }

    private JMenu createMenu()
    {
        JMenu menu = new JMenu();
        menu.setText("Image type");

        rgbItem = new JRadioButtonMenuItem();
        argbItem = new JRadioButtonMenuItem();
        grayItem = new JRadioButtonMenuItem();
        bitonalItem = new JRadioButtonMenuItem();
        rgbItem.setSelected(true);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rgbItem);
        bg.add(argbItem);
        bg.add(grayItem);
        bg.add(bitonalItem);

        rgbItem.setText(IMAGETYPE_RGB);
        argbItem.setText(IMAGETYPE_ARGB);
        grayItem.setText(IMAGETYPE_GRAY);
        bitonalItem.setText(IMAGETYPE_BITONAL);
        
        menu.add(rgbItem);
        menu.add(argbItem);
        menu.add(grayItem);
        menu.add(bitonalItem);

        return menu;
    }
}
