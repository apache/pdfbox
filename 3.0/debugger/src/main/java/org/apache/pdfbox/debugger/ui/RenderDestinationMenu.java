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

import org.apache.pdfbox.rendering.RenderDestination;

/**
 * @author Tilman Hausherr
 *
 * A singleton class that provides the RenderDestination menu for the menubar. To act upon the menu
 * item selection, the user of the class must add ActionListener which will check for the action
 * command and act accordingly.
 */
public final class RenderDestinationMenu extends MenuBase
{
    public static final String RENDER_DESTINATION_EXPORT = "Export";
    public static final String RENDER_DESTINATION_PRINT = "Print";
    public static final String RENDER_DESTINATION_VIEW = "View";
    
    private static RenderDestinationMenu instance;
    private JRadioButtonMenuItem exportItem;
    private JRadioButtonMenuItem printItem;
    private JRadioButtonMenuItem viewItem;

    /**
     * Constructor.
     */
    private RenderDestinationMenu()
    {
        setMenu(createMenu());
    }
  
    /**
     * Provides the RenderDestination instance.
     * @return RenderDestination instance.
     */
    public static RenderDestinationMenu getInstance()
    {
        if (instance == null)
        {
            instance = new RenderDestinationMenu();
        }
        return instance;
    }

    /**
     * Set the render destination selection.
     * @param selection String instance.
     */
    public void setRenderDestinationSelection(String selection)
    {
        switch (selection)
        {
            case RENDER_DESTINATION_EXPORT:
                exportItem.setSelected(true);
                break;
            case RENDER_DESTINATION_PRINT:
                printItem.setSelected(true);
                break;
            case RENDER_DESTINATION_VIEW:
                viewItem.setSelected(true);
                break;
            default:
                throw new IllegalArgumentException("Invalid RenderDestination selection: " + selection);
        }
    }

    public static boolean isRenderDestinationMenu(String actionCommand)
    {
        return RENDER_DESTINATION_EXPORT.equals(actionCommand) || RENDER_DESTINATION_PRINT.equals(actionCommand) ||
                RENDER_DESTINATION_VIEW.equals(actionCommand);
    }
    
    public static RenderDestination getRenderDestination()
    {
        if (instance.printItem.isSelected())
        {
            return RenderDestination.PRINT;
        }
        if (instance.viewItem.isSelected())
        {
            return RenderDestination.VIEW;
        }
        return RenderDestination.EXPORT;
    }

    public static RenderDestination getRenderDestination(String actionCommand)
    {
        switch (actionCommand)
        {
            case RENDER_DESTINATION_EXPORT:
                return RenderDestination.EXPORT;
            case RENDER_DESTINATION_PRINT:
                return RenderDestination.PRINT;
            case RENDER_DESTINATION_VIEW:
                return RenderDestination.VIEW;
            default:
                throw new IllegalArgumentException("Invalid RenderDestination actionCommand: " + actionCommand);
        }
    }

    private JMenu createMenu()
    {
        JMenu menu = new JMenu();
        menu.setText("Render destination");

        exportItem = new JRadioButtonMenuItem();
        printItem = new JRadioButtonMenuItem();
        viewItem = new JRadioButtonMenuItem();
        exportItem.setSelected(true);

        ButtonGroup bg = new ButtonGroup();
        bg.add(exportItem);
        bg.add(printItem);
        bg.add(viewItem);

        exportItem.setText(RENDER_DESTINATION_EXPORT);
        printItem.setText(RENDER_DESTINATION_PRINT);
        viewItem.setText(RENDER_DESTINATION_VIEW);
        
        menu.add(exportItem);
        menu.add(printItem);
        menu.add(viewItem);

        return menu;
    }
}
