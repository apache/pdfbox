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

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.StyledDocument;

import org.apache.pdfbox.debugger.PDFDebugger;
import org.apache.pdfbox.debugger.streampane.tooltip.ToolTipController;
import org.apache.pdfbox.debugger.ui.textsearcher.Searcher;

/**
 * @author Khyrul Bashar
 * A class that provides the container for the texts when stream is shown in stream pane.
 */
class StreamTextView implements MouseMotionListener, AncestorListener
{
    private final ToolTipController tTController;

    private JPanel mainPanel;
    private JTextPane textPane;
    private Searcher searcher;

    /**
     * Constructor.
     * @param document StyledDocument instance which is supposed to be shown in the pane.
     * @param controller ToolTipController instance.
     */
    StreamTextView(StyledDocument document, ToolTipController controller)
    {
        tTController = controller;
        initUI(document);
    }

    private void initUI(StyledDocument document)
    {
        mainPanel = new JPanel();

        textPane = new JTextPane(document);
        textPane.addMouseMotionListener(this);
        textPane.setFont(new Font("monospaced", Font.PLAIN, 13));
        searcher = new Searcher(textPane);

        JScrollPane scrollPane = new JScrollPane(textPane);

        BoxLayout boxLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);

        mainPanel.setLayout(boxLayout);

        mainPanel.add(searcher.getSearchPanel());
        mainPanel.add(scrollPane);

        searcher.getSearchPanel().setVisible(false);

        mainPanel.addAncestorListener(this);

    }

    JComponent getView()
    {
        return mainPanel;
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent)
    {
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent)
    {
        if (tTController != null)
        {
            int offset = textPane.viewToModel(mouseEvent.getPoint());
            textPane.setToolTipText(tTController.getToolTip(offset, textPane));
        }
    }

    @Override
    public void ancestorAdded(AncestorEvent ancestorEvent)
    {
        if (ancestorEvent.getAncestor().equals(mainPanel))
        {
            PDFDebugger debugger = (PDFDebugger) SwingUtilities.getRoot(mainPanel);
            debugger.getFindMenu().setEnabled(true);
            searcher.addMenuListeners(debugger);
        }
    }

    @Override
    public void ancestorRemoved(AncestorEvent ancestorEvent)
    {
        if (ancestorEvent.getAncestor().equals(mainPanel))
        {
            PDFDebugger debugger = (PDFDebugger) SwingUtilities.getRoot(mainPanel);
            debugger.getFindMenu().setEnabled(false);
            searcher.removeMenuListeners(debugger);
        }
    }

    @Override
    public void ancestorMoved(AncestorEvent ancestorEvent)
    {
    }
}
