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

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.pdfbox.debugger.PDFDebugger;

public class ViewMenu extends MenuBase
{
    private static ViewMenu instance;
    
    private static final String SHOW_TEXT_STRIPPER = "Show TextStripper TextPositions";
    private static final String SHOW_TEXT_STRIPPER_BEADS = "Show TextStripper Beads";
    private static final String SHOW_FONT_BBOX = "Show Approximate Text Bounds";
    private static final String SHOW_GLYPH_BOUNDS = "Show Glyph Bounds";
    private static final String ALLOW_SUBSAMPLING = "Allow subsampling";            
    private static final String EXTRACT_TEXT = "Extract Text";            
    private static final String REPAIR_ACROFORM = "Repair AcroForm";

    private JCheckBoxMenuItem showTextStripper;
    private JCheckBoxMenuItem showTextStripperBeads;
    private JCheckBoxMenuItem showFontBBox;
    private JCheckBoxMenuItem showGlyphBounds;
    private JCheckBoxMenuItem allowSubsampling;
    private JMenuItem extractTextMenuItem;
    private JCheckBoxMenuItem repairAcroFormMenuItem;

    private final PDFDebugger pdfDebugger;

    /**
     * Constructor.
     */
    private ViewMenu(PDFDebugger pdfDebugger)
    {
        this.pdfDebugger = pdfDebugger;
        setMenu(createViewMenu());
    }

    /**
     * Provides the ViewMenu instance.
     *
     * @param pdfDebugger PDFDebugger instance.
     *
     * @return ViewMenu instance.
     */
    public static ViewMenu getInstance(PDFDebugger pdfDebugger)
    {
        if (instance == null)
        {
            instance = new ViewMenu(pdfDebugger);
        }
        return instance;
    }
    
    /**
     * Test if the one of the rendering options has been selected
     * 
     * @param actionCommand the actioCommand of the menu event 
     * @return true if the actionCommand matches one of the rendering options
     */
    public static boolean isRenderingOption(String actionCommand)
    {
        return SHOW_TEXT_STRIPPER.equals(actionCommand) ||
                SHOW_TEXT_STRIPPER_BEADS.equals(actionCommand) ||
                SHOW_FONT_BBOX.equals(actionCommand) ||
                SHOW_GLYPH_BOUNDS.equals(actionCommand) ||
                ALLOW_SUBSAMPLING.equals(actionCommand);         
    }
    
    /**
     * State if the TextStripper TextPositions shall be shown.
     * 
     * @return the selection state
     */
    public static boolean isShowTextStripper()
    {
        return instance.showTextStripper.isSelected();
    }
    
    /**
     * State if the article beads shall be shown.
     * 
     * @return the selection state
     */
    public static boolean isShowTextStripperBeads()
    {
        return instance.showTextStripperBeads.isSelected();
    }
    
    /**
     * State if the fonts bounding box shall be shown.
     * 
     * @return the selection state
     */
    public static boolean isShowFontBBox()
    {
        return instance.showFontBBox.isSelected();
    }
    
    /**
     * State if the bounds of individual glyphs shall be shown.
     * 
     * @return the selection state
     */
    public static boolean isShowGlyphBounds()
    {
        return instance.showGlyphBounds.isSelected();
    }

    /**
     * Tell whether the "Extract Text" menu entry was hit.
     *
     * @param actionEvent the action event
     * @return true if the "Extract Text" menu entry was hit.
     */
    public static boolean isExtractTextEvent(ActionEvent actionEvent)
    {
        return EXTRACT_TEXT.equals(actionEvent.getActionCommand());
    }

    /**
     * State if subsampling for image rendering shall be used.
     * 
     * @return the selection state
     */
    public static boolean isAllowSubsampling()
    {
        return instance.allowSubsampling.isSelected();
    }

    /**
     * Tell whether the "Repair AcroForm" menu entry was hit.
     *
     * @param actionEvent the action event
     * @return true if the "Repair AcroForm" menu entry was hit.
     */
    public static boolean isRepairAcroformEvent(ActionEvent actionEvent)
    {
        return REPAIR_ACROFORM.equals(actionEvent.getActionCommand());
    }

    /**
     * State if acroForm shall be repaired.
     *
     * @return the repair state
     */
    public static boolean isRepairAcroformSelected()
    {
        return instance.repairAcroFormMenuItem.isSelected();
    }

    private JMenu createViewMenu()
    {
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        
        TreeViewMenu treeViewMenu = TreeViewMenu.getInstance();
        viewMenu.add(treeViewMenu.getMenu());
        treeViewMenu.addMenuListeners(actionEvent ->
        {
            pdfDebugger.setTreeViewMode(treeViewMenu.getTreeViewSelection());
            if (pdfDebugger.hasDocument())
            {
                pdfDebugger.initTree();
            }
        });

        ZoomMenu zoomMenu = ZoomMenu.getInstance();
        zoomMenu.setEnableMenu(false);
        viewMenu.add(zoomMenu.getMenu());

        RotationMenu rotationMenu = RotationMenu.getInstance();
        rotationMenu.setEnableMenu(false);
        viewMenu.add(rotationMenu.getMenu());

        ImageTypeMenu imageTypeMenu = ImageTypeMenu.getInstance();
        imageTypeMenu.setEnableMenu(false);
        viewMenu.add(imageTypeMenu.getMenu());

        RenderDestinationMenu renderDestinationMenu = RenderDestinationMenu.getInstance();
        renderDestinationMenu.setEnableMenu(false);
        viewMenu.add(renderDestinationMenu.getMenu());

        viewMenu.addSeparator();

        showTextStripper = new JCheckBoxMenuItem(SHOW_TEXT_STRIPPER);        
        showTextStripper.setEnabled(false);
        viewMenu.add(showTextStripper);

        showTextStripperBeads = new JCheckBoxMenuItem(SHOW_TEXT_STRIPPER_BEADS);
        showTextStripperBeads.setEnabled(false);
        viewMenu.add(showTextStripperBeads);
        
        showFontBBox = new JCheckBoxMenuItem(SHOW_FONT_BBOX);
        showFontBBox.setEnabled(false);
        viewMenu.add(showFontBBox);
        
        showGlyphBounds = new JCheckBoxMenuItem(SHOW_GLYPH_BOUNDS);
        showGlyphBounds.setEnabled(false);
        viewMenu.add(showGlyphBounds);

        viewMenu.addSeparator();

        allowSubsampling = new JCheckBoxMenuItem(ALLOW_SUBSAMPLING);
        viewMenu.add(allowSubsampling);

        viewMenu.addSeparator();

        extractTextMenuItem = new JMenuItem(EXTRACT_TEXT);
        extractTextMenuItem.setEnabled(false);
        viewMenu.add(extractTextMenuItem);

        viewMenu.addSeparator();

        repairAcroFormMenuItem = new JCheckBoxMenuItem(REPAIR_ACROFORM);
        repairAcroFormMenuItem.setEnabled(false);
        viewMenu.add(repairAcroFormMenuItem);

        return viewMenu;
    }
}
