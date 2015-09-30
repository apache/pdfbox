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

package org.apache.pdfbox.debugger.treestatus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 * @author Khyrul Bashar
 */
public class TreeStatusPane implements TreeSelectionListener
{
    private TreeStatus statusObj;
    private JTree tree;

    private JTextField statusField;
    private JPanel panel;
    private Border defaultBorder;
    private Border errorBorder;
    private final Action textInputAction = new AbstractAction()
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            TreePath path = statusObj.getPathForString(statusField.getText());
            if (path != null)
            {
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
                tree.requestFocusInWindow();
            }
            else
            {
                statusField.setBorder(errorBorder);
            }
        }
    };

    /**
     * Constructor.
     * @param targetTree The tree instance that this status pane will correspond.
     */
    public TreeStatusPane(JTree targetTree)
    {
        tree = targetTree;
        init();
    }

    private void init()
    {
        panel = new JPanel(new BorderLayout());
        statusField = new JTextField();
        statusField.setEditable(false);
        panel.add(statusField);
        defaultBorder = new BevelBorder(BevelBorder.LOWERED);
        errorBorder = new BevelBorder(BevelBorder.LOWERED, Color.RED, Color.RED);
        statusField.setAction(textInputAction);
        tree.addTreeSelectionListener(this);
    }

    /**
     * Return the panel of this TreeStatusPane.
     * @return JPanel instance.
     */
    public JPanel getPanel()
    {
        return panel;
    }

    /**
     * In case of document changing this should be called to update TreeStatus value of the pane.
     * @param statusObj TreeStatus instance.
     */
    public void updateTreeStatus(TreeStatus statusObj)
    {
        statusField.setEditable(true);
        this.statusObj = statusObj;
        updateText(null);
    }

    private void updateText(String statusString)
    {
        statusField.setText(statusString);
        if (!statusField.getBorder().equals(defaultBorder))
        {
            statusField.setBorder(defaultBorder);
        }
    }

    /**
     * Tree selection change listener which updates status string.
     * @param treeSelectionEvent
     */

    @Override
    public void valueChanged(TreeSelectionEvent treeSelectionEvent)
    {
        TreePath path = treeSelectionEvent.getPath();
        updateText(statusObj.getStringForPath(path));
    }
}
