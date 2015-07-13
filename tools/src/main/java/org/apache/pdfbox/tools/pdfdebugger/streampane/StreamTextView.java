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

package org.apache.pdfbox.tools.pdfdebugger.streampane;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.apache.pdfbox.tools.pdfdebugger.streampane.tooltip.ToolTipController;
import org.apache.pdfbox.tools.pdfdebugger.ui.textsearcher.Searcher;

/**
 * @author Khyrul Bashar
 * A class that provides the container for the texts when stream is shown in stream pane.
 */
class StreamTextView implements MouseMotionListener, AncestorListener
{
    private final ToolTipController tTController;

    private JPanel mainPanel;
    private JTextComponent textComponent;
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

        textComponent = new JTextPane(document);
        textComponent.addMouseMotionListener(this);
        searcher = new Searcher(textComponent);

        JScrollPane scrollPane = new JScrollPane(textComponent);

        BoxLayout boxLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);

        mainPanel.setLayout(boxLayout);

        mainPanel.add(searcher.getSearchPanel());
        mainPanel.add(scrollPane);

        searcher.getSearchPanel().setVisible(false);
        searcher.setFindStroke(mainPanel, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        searcher.setCloseStroke(mainPanel, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));

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
            int offset = textComponent.viewToModel(mouseEvent.getPoint());
            textComponent.setToolTipText(tTController.getToolTip(offset, textComponent));
        }
    }

    @Override
    public void ancestorAdded(AncestorEvent ancestorEvent)
    {
        if (ancestorEvent.getAncestor().equals(mainPanel))
        {
            JFrame frame = (JFrame) SwingUtilities.getRoot(mainPanel);
            frame.getJMenuBar().add(searcher.getMenu());
            SwingUtilities.updateComponentTreeUI(frame.getJMenuBar());
        }
    }

    @Override
    public void ancestorRemoved(AncestorEvent ancestorEvent)
    {
        if (ancestorEvent.getAncestor().equals(mainPanel))
        {
            JFrame frame = (JFrame) SwingUtilities.getRoot(mainPanel);
            frame.getJMenuBar().remove(searcher.getMenu());
            SwingUtilities.updateComponentTreeUI(frame.getJMenuBar());
        }
    }

    @Override
    public void ancestorMoved(AncestorEvent ancestorEvent)
    {

    }
}