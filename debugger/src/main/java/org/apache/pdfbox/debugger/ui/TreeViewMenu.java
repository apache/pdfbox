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

import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * A singleton class that provides tree view menu which can be used to switch the kind of tree view. To act upon the
 * menu item selection user of the class must add ActionListener which will check for the action command and act
 * accordingly.
 */
public final class TreeViewMenu extends MenuBase
{
    public static final String VIEW_PAGES = "Show pages";
    public static final String VIEW_STRUCTURE = "Internal structure";
    public static final String VIEW_CROSS_REF_TABLE = "Cross reference table";

    private static final List<String> validTreeModes = Arrays.asList(VIEW_PAGES, VIEW_STRUCTURE,
            VIEW_CROSS_REF_TABLE);

    private static TreeViewMenu instance;
    private JRadioButtonMenuItem pagesItem;
    private JRadioButtonMenuItem structureItem;
    private JRadioButtonMenuItem crtItem;

    /**
     * Constructor.
     */
    private TreeViewMenu()
    {
        setMenu(createTreeViewMenu());
    }

    /**
     * Provides the TreeViewMenu instance.
     * 
     * @return TreeViewMenu instance.
     */
    public static TreeViewMenu getInstance()
    {
        if (instance == null)
        {
            instance = new TreeViewMenu();
        }
        return instance;
    }

    /**
     * Set the tree view selection.
     * 
     * @param selection String instance.
     */
    public void setTreeViewSelection(String selection)
    {
        switch (selection)
        {
        case VIEW_PAGES:
            pagesItem.setSelected(true);
            break;
        case VIEW_STRUCTURE:
            structureItem.setSelected(true);
            break;
        case VIEW_CROSS_REF_TABLE:
            crtItem.setSelected(true);
            break;
        default:
            throw new IllegalArgumentException("Invalid tree view selection: " + selection);
        }
    }

    /**
     * Provide the current tree view selection.
     * 
     * @return the selection String instance
     */
    public String getTreeViewSelection()
    {
        if (pagesItem.isSelected())
        {
            return VIEW_PAGES;
        }
        if (structureItem.isSelected())
        {
            return VIEW_STRUCTURE;
        }
        if (crtItem.isSelected())
        {
            return VIEW_CROSS_REF_TABLE;
        }
        return null;
    }

    /**
     * Checks if the given viewMode value is a valid one.
     * 
     * @param viewMode the view mode to be checked
     * @return true if the given value is a valid view mode, otherwise false
     */
    public static boolean isValidViewMode(String viewMode)
    {
        return validTreeModes.contains(viewMode);
    }

    private JMenu createTreeViewMenu()
    {
        JMenu menu = new JMenu();
        menu.setText("Tree view");

        pagesItem = new JRadioButtonMenuItem();
        structureItem = new JRadioButtonMenuItem();
        crtItem = new JRadioButtonMenuItem();
        pagesItem.setSelected(true);

        ButtonGroup bg = new ButtonGroup();
        bg.add(pagesItem);
        bg.add(structureItem);
        bg.add(crtItem);

        pagesItem.setText(VIEW_PAGES);
        structureItem.setText(VIEW_STRUCTURE);
        crtItem.setText(VIEW_CROSS_REF_TABLE);

        menu.add(pagesItem);
        menu.add(structureItem);
        menu.add(crtItem);

        return menu;
    }
}
