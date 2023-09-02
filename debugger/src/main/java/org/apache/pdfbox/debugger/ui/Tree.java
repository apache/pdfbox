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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.debugger.treestatus.TreeStatus;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDStream;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.apache.pdfbox.debugger.PDFDebugger;

/**
 * @author Khyrul Bashar
 *
 * A customized tree for PDFDebugger.
 */
@SuppressWarnings({"serial","squid:S1948"})
public class Tree extends JTree
{
    // No logging possible in this class because it is created before the "LogDialog.init()" call
    private final JPopupMenu treePopupMenu;
    private final Object rootNode;

    /**
     * Constructor.
     */
    public Tree()
    {
        treePopupMenu = new JPopupMenu();
        setComponentPopupMenu(treePopupMenu);
        rootNode = getModel().getRoot();
        int treeRowHeight = Integer.parseInt(PDFDebugger.configuration.getProperty(
                                    "treeRowHeight", Integer.toString(getRowHeight())));
        setRowHeight(treeRowHeight);
    }

    @Override
    public Point getPopupLocation(MouseEvent event)
    {
        if (event != null)
        {
            TreePath path = getClosestPathForLocation(event.getX(), event.getY());
            if (path == null)
            {
                return null;
            }
            setSelectionPath(path);
            treePopupMenu.removeAll();
            addPopupMenuItems(path);
            return event.getPoint();
        }
        return null;
    }

    /**
     * Produce the popup menu items depending on the node of a certain TreePath.
     * @param nodePath is instance of TreePath of the specified Node.
     */
    private void addPopupMenuItems(TreePath nodePath)
    {
        Object obj = nodePath.getLastPathComponent();

        treePopupMenu.add(getTreePathMenuItem(nodePath));

        if (obj instanceof MapEntry)
        {
            obj = ((MapEntry) obj).getValue();
        }
        else if (obj instanceof ArrayEntry)
        {
            obj = ((ArrayEntry) obj).getValue();
        }
        else if (obj instanceof XrefEntry)
        {
            obj = ((XrefEntry) obj).getObject();
        }

        if (!(obj instanceof COSStream))
        {
            return;
        }

        treePopupMenu.addSeparator();

        COSStream stream = (COSStream) obj;
        treePopupMenu.add(getStreamSaveMenu(stream, nodePath));

        List<COSName> filters = new PDStream(stream).getFilters();
        if (!filters.isEmpty())
        {
            if (filters.size() >= 2)
            {
                getPartiallyDecodedStreamSaveMenu(stream).forEach(treePopupMenu::add);
            }
            treePopupMenu.add(getRawStreamSaveMenu(stream));
        }

        JMenuItem open = getFileOpenMenu(stream, nodePath);
        if (open != null)
        {
            treePopupMenu.addSeparator();
            treePopupMenu.add(open);
        }
    }

    /**
     * Produce the JMenuItem that gives way to copy tree path string to clipboard
     * @param path the TreePath instance
     * @return Menu Item
     */
    private JMenuItem getTreePathMenuItem(final TreePath path)
    {
        JMenuItem copyPathMenuItem = new JMenuItem("Copy Tree Path");
        copyPathMenuItem.addActionListener(actionEvent ->
        {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(new TreeStatus(rootNode).getStringForPath(path)), null);
        });
        return copyPathMenuItem;
    }

    /**
     * Produce JMenuItem that saves the raw stream
     * @param cosStream stream to save
     * @return JMenuItem for saving the raw stream
     */
    private JMenuItem getRawStreamSaveMenu(final COSStream cosStream)
    {
        JMenuItem saveMenuItem = new JMenuItem("Save Raw Stream (" + getFilters(cosStream) + ") As...");
        saveMenuItem.addActionListener(actionEvent ->
        {
            try
            {
                InputStream in = cosStream.createRawInputStream();
                byte[] bytes = in.readAllBytes();
                saveStream(bytes, null, null);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
        return saveMenuItem;
    }

    /**
     * Returns the filters used by the given stream.
     */
    private String getFilters(COSStream cosStream)
    {
        StringJoiner sj = new StringJoiner(", ");
        COSBase filters = cosStream.getFilters();
        if (filters instanceof COSName)
        {
            sj.add(((COSName) filters).getName());
        }
        else if (filters instanceof COSArray)
        {
            COSArray filterArray = (COSArray) filters;
            for (COSBase name : filterArray)
            {
                sj.add(((COSName) name).getName());
            }
        }
        return sj.toString();
    }

    /**
     * Produce JMenuItem that saves the stream
     * @param cosStream stream to save
     * @return JMenuItem for saving stream
     */
    private JMenuItem getStreamSaveMenu(final COSStream cosStream, final TreePath nodePath)
    {
        // set file extension based on stream type
        final String extension = getFileExtensionForStream(cosStream, nodePath);
        final FileFilter fileFilter;
        final String format;

        if (extension != null)
        {
            switch (extension)
            {
                case "pdb":
                    fileFilter = new FileNameExtensionFilter("Type 1 Font (*.pfb)", "pfb");
                    break;
                case "ttf":
                    fileFilter = new FileNameExtensionFilter("TrueType Font (*.ttf)", "ttf");
                    break;
                case "cff":
                    fileFilter = new FileNameExtensionFilter("Compact Font Format (*.cff)", "cff");
                    break;
                case "otf":
                    fileFilter = new FileNameExtensionFilter("OpenType Font (*.otf)", "otf");
                    break;
                default:
                    fileFilter = null;
                    break;
            }
            format = " " + extension.toUpperCase();
        }
        else
        {
            fileFilter = null;
            format = "";
        }

        JMenuItem saveMenuItem = new JMenuItem("Save Stream As" + format + "...");
        saveMenuItem.addActionListener(actionEvent ->
        {
            try
            {
                InputStream in = cosStream.createInputStream();
                byte[] bytes = in.readAllBytes();
                saveStream(bytes, fileFilter, extension);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
        return saveMenuItem;
    }

    /**
     * Returns the recommended file extension for the given cos stream.
     */
    private String getFileExtensionForStream(final COSStream cosStream, final TreePath nodePath)
    {
        String name = nodePath.getLastPathComponent().toString();
        switch (name)
        {
            case "FontFile":
                return "pfb";
            case "FontFile2":
                return "ttf";
            case "FontFile3":
                return cosStream.getCOSName(COSName.SUBTYPE) == COSName.OPEN_TYPE ? "otf" : "cff";
            default:
                return null;
        }
    }

    /**
     * Produce JMenuItem that opens the stream with the system's default app.
     */
    private JMenuItem getFileOpenMenu(final COSStream cosStream, final TreePath nodePath)
    {
        // if we know the file type, create a system open menu 
        final String extension = getFileExtensionForStream(cosStream, nodePath);
        if (extension == null)
        {
            return null;
        }

        JMenuItem openMenuItem = new JMenuItem("Open with Default Application");
        openMenuItem.addActionListener(actionEvent ->
        {
            try
            {
                File temp = File.createTempFile("pdfbox", "." + extension);
                temp.deleteOnExit();
                
                try (InputStream is = cosStream.createInputStream();
                        FileOutputStream os = new FileOutputStream(temp))
                {
                    IOUtils.copy(is, os);
                }
                Desktop.getDesktop().open(temp);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
        return openMenuItem;
    }

    /**
     * produce possible partially decoded stream saving menu items
     * @param cosStream stream to save
     * @return JMenuItems for saving partially decoded streams
     */
    private List<JMenuItem> getPartiallyDecodedStreamSaveMenu(final COSStream cosStream)
    {
        List<JMenuItem> menuItems = new ArrayList<>();
        PDStream stream = new PDStream(cosStream);

        List<COSName> filters = stream.getFilters();

        for (int i = filters.size() - 1; i >= 1; i--)
        {
            menuItems.add(getPartialStreamSavingMenuItem(i, stream));
        }
        return menuItems;
    }

    private JMenuItem getPartialStreamSavingMenuItem(final int indexOfStopFilter, final PDStream stream)
    {
        List<COSName> filters = stream.getFilters();

        final List<String> stopFilters = new ArrayList<>(1);
        stopFilters.add(filters.get(indexOfStopFilter).getName());

        StringBuilder nameListBuilder = new StringBuilder();
        for (int i = indexOfStopFilter; i < filters.size(); i++)
        {
            nameListBuilder.append(filters.get(i).getName()).append(" & ");
        }
        nameListBuilder.delete(nameListBuilder.lastIndexOf("&"), nameListBuilder.length());
        JMenuItem menuItem = new JMenuItem("Keep " + nameListBuilder.toString() + "...");

        menuItem.addActionListener(actionEvent ->
        {
            try
            {
                InputStream data = stream.createInputStream(stopFilters);
                saveStream(data.readAllBytes(), null, null);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
        return menuItem;
    }

    /**
     * Save the stream.
     * @param bytes byte array of the stream.
     * @param filter an optional FileFilter
     * @throws IOException if there is an error in creation of the file.
     */
    private void saveStream(byte[] bytes, FileFilter filter, String extension) throws IOException
    {
        FileOpenSaveDialog saveDialog = new FileOpenSaveDialog(getParent(), filter);
        saveDialog.saveFile(bytes, extension);
    }
}
