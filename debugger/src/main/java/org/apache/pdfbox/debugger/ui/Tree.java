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

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.debugger.treestatus.TreeStatus;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * @author Khyrul Bashar
 *
 * A customized tree for PDFDebugger.
 */
public class Tree extends JTree
{
    private final JPopupMenu treePopupMenu;
    private final Component parent;
    private final Object rootNode;

    /**
     * Constructor.
     * @param parentComponent the main UI where the Tree resides.
     */
    public Tree(Component parentComponent)
    {
        treePopupMenu = new JPopupMenu();
        setComponentPopupMenu(treePopupMenu);
        parent = parentComponent;
        rootNode = this.getModel().getRoot();
    }

    @Override
    public Point getPopupLocation(MouseEvent event)
    {
        if (event != null)
        {
            TreePath path = getClosestPathForLocation(event.getX(), event.getY());
            setSelectionPath(path);
            treePopupMenu.removeAll();
            for (JMenuItem menuItem : getPopupMenuItems(path))
            {
                treePopupMenu.add(menuItem);
            }
            return event.getPoint();
        }
        return null;
    }

    /**
     * Produce the popup menu items depending on the node of a certain TreePath.
     * @param nodePath is instance of TreePath of the specified Node.
     * @return the JMenuItem list for the node
     */
    private List<JMenuItem> getPopupMenuItems(TreePath nodePath)
    {
        Object obj = nodePath.getLastPathComponent();
        List<JMenuItem> menuItems = new ArrayList<JMenuItem>();

        menuItems.add(getTreePathMenuItem(nodePath));

        if (obj instanceof MapEntry)
        {
            obj = ((MapEntry) obj).getValue();
        }
        else if (obj instanceof ArrayEntry)
        {
            obj = ((ArrayEntry) obj).getValue();
        }

        if (obj instanceof COSStream)
        {
            COSStream stream = (COSStream) obj;
            menuItems.add(getUnFilteredStreamSaveMenu(stream));
            if (stream.getFilters() != null)
            {
                if (stream.getFilters() instanceof COSArray && ((COSArray) stream.getFilters()).size() >= 2)
                {
                    for (JMenuItem menuItem : getPartiallyFilteredStreamSaveMenu(stream))
                    {
                        menuItems.add(menuItem);
                    }
                }
                menuItems.add(getFilteredStreamSaveMenu(stream));
            }
        }
        
        return menuItems;
    }

    /**
     * Produce the JMenuItem that gives way to copy tree path string to clipboard
     * @param path the TreePath instance
     * @return Menu Item
     */
    private JMenuItem getTreePathMenuItem(final TreePath path)
    {
        JMenuItem copyPathMenuItem = new JMenuItem("Copy Tree Path");
        copyPathMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(new TreeStatus(rootNode).getStringForPath(path)), null);
            }
        });
        return copyPathMenuItem;
    }

    /**
     * Produce JMenuItem that saves filtered stream
     * @param cosStream stream to save
     * @return JMenuItem for saving filtered stream
     */
    private JMenuItem getFilteredStreamSaveMenu(final COSStream cosStream)
    {
        JMenuItem saveMenuItem = new JMenuItem("Save Filtered Stream (" + getFilters(cosStream) + ")...");
        saveMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    byte[] bytes = IOUtils.toByteArray(cosStream.createRawInputStream());
                    saveStream(bytes);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        return saveMenuItem;
    }

    /**
     * Returns the filters used by the given stream.
     */
    private String getFilters(COSStream cosStream)
    {
        StringBuilder sb = new StringBuilder();
        COSBase filters = cosStream.getFilters();
        if (filters != null)
        {
            if (filters instanceof COSName)
            {
                sb.append(((COSName) filters).getName());
            }
            else if (filters instanceof COSArray)
            {
                COSArray filterArray = (COSArray) filters;
                for (int i = 0; i < filterArray.size(); i++)
                {
                    if (i > 0)
                    {
                        sb.append(", ");
                    }
                    sb.append(((COSName) filterArray.get(i)).getName());
                }
            }
        }
        return sb.toString();
    }

    /**
     * Produce JMenuItem that saves unfiltered stream
     * @param cosStream stream to save
     * @return JMenuItem for saving unfiltered stream
     */
    private JMenuItem getUnFilteredStreamSaveMenu(final COSStream cosStream)
    {
        JMenuItem saveMenuItem = new JMenuItem("Save Unfiltered Stream...");
        saveMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    byte[] bytes = IOUtils.toByteArray(cosStream.createInputStream());
                    saveStream(bytes);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        return saveMenuItem;
    }

    /**
     * produce possible partially filtered stream saving menu items
     * @param cosStream stream to save
     * @return JMenuItems for saving partially filtered streams
     */
    private List<JMenuItem> getPartiallyFilteredStreamSaveMenu(final COSStream cosStream)
    {
        List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
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

        final List<String> stopFilters = new ArrayList<String>(1);
        stopFilters.add(filters.get(indexOfStopFilter).getName());

        StringBuilder nameListBuilder = new StringBuilder();
        for (int i = indexOfStopFilter; i < filters.size(); i++)
        {
            nameListBuilder.append(filters.get(i).getName()).append(" & ");
        }
        nameListBuilder.delete(nameListBuilder.lastIndexOf("&"), nameListBuilder.length());
        JMenuItem menuItem = new JMenuItem("Keep " + nameListBuilder.toString() + "...");

        menuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    InputStream data = stream.createInputStream(stopFilters);
                    saveStream(IOUtils.toByteArray(data));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        return menuItem;
    }

    /**
     * Save the stream.
     * @param bytes byte array of the stream.
     * @throws IOException if there is an error in creation of the file.
     */
    private void saveStream(byte[] bytes) throws IOException
    {
        FileOpenSaveDialog saveDialog = new FileOpenSaveDialog(parent, null);
        saveDialog.saveFile(bytes);
    }
}
