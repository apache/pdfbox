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

/**
 * @author Khyrul Bashar
 * @author Tilman Hausherr
 *
 * A singleton class that provides rotation menu which can be used to show rotation menu in the menubar.
 * To act upon the menu item selection user of the class must add ActionListener which will check for
 * the action command and act accordingly.
 */
public final class RotationMenu extends MenuBase
{
    public static final String ROTATE_0_DEGREES = "0°";
    public static final String ROTATE_90_DEGREES = "90°";
    public static final String ROTATE_180_DEGREES = "180°";
    public static final String ROTATE_270_DEGREES = "270°";
    
    private static RotationMenu instance;
    private JRadioButtonMenuItem rotate0Item;
    private JRadioButtonMenuItem rotate90Item;
    private JRadioButtonMenuItem rotate180Item;
    private JRadioButtonMenuItem rotate270Item;

    /**
     * Constructor.
     */
    private RotationMenu()
    {
        setMenu(createRotationMenu());
    }
  
    /**
     * Provides the RotationMenu instance.
     * @return RotationMenu instance.
     */
    public static RotationMenu getInstance()
    {
        if (instance == null)
        {
            instance = new RotationMenu();
        }
        return instance;
    }

    /**
     * Set the rotation selection.
     * @param selection String instance.
     */
    public void setRotationSelection(String selection)
    {
        if (ROTATE_0_DEGREES.equals(selection))
        {
            rotate0Item.setSelected(true);
        }
        else if (ROTATE_90_DEGREES.equals(selection))
        {
            rotate90Item.setSelected(true);
        }
        else if (ROTATE_180_DEGREES.equals(selection))
        {
            rotate180Item.setSelected(true);
        }
        else if (ROTATE_270_DEGREES.equals(selection))
        {
            rotate270Item.setSelected(true);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public static boolean isRotationMenu(String actionCommand)
    {
        return ROTATE_0_DEGREES.equals(actionCommand) || ROTATE_90_DEGREES.equals(actionCommand) ||
                ROTATE_180_DEGREES.equals(actionCommand) || ROTATE_270_DEGREES.equals(actionCommand);
    }
    
    public static int getRotationDegrees()
    {
        if (instance.rotate90Item.isSelected())
        {
            return 90;
        }
        if (instance.rotate180Item.isSelected())
        {
            return 180;
        }
        if (instance.rotate270Item.isSelected())
        {
            return 270;
        }
        return 0;
    }

    public static int getRotationDegrees(String actionCommand)
    {
        if (ROTATE_0_DEGREES.equals(actionCommand))
        {
            return 0;
        }
        else if (ROTATE_90_DEGREES.equals(actionCommand))
        {
            return 90;
        }
        else if (ROTATE_180_DEGREES.equals(actionCommand))
        {
            return 180;
        }
        else if (ROTATE_270_DEGREES.equals(actionCommand))
        {
            return 270;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    private JMenu createRotationMenu()
    {
        JMenu menu = new JMenu();
        menu.setText("Rotation");

        rotate0Item = new JRadioButtonMenuItem();
        rotate90Item = new JRadioButtonMenuItem();
        rotate180Item = new JRadioButtonMenuItem();
        rotate270Item = new JRadioButtonMenuItem();
        rotate0Item.setSelected(true);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rotate0Item);
        bg.add(rotate90Item);
        bg.add(rotate180Item);
        bg.add(rotate270Item);

        rotate0Item.setText(ROTATE_0_DEGREES);
        rotate90Item.setText(ROTATE_90_DEGREES);
        rotate180Item.setText(ROTATE_180_DEGREES);
        rotate270Item.setText(ROTATE_270_DEGREES);
        
        menu.add(rotate0Item);
        menu.add(rotate90Item);
        menu.add(rotate180Item);
        menu.add(rotate270Item);

        return menu;
    }
}
