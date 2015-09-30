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

package org.apache.pdfbox.debugger.ui.textsearcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;

import org.apache.pdfbox.debugger.PDFDebugger;

/**
 * @author Khyrul Bashar
 * A class that provides the search pane.
 */
class SearchPanel
{
    private final Action nextAction;
    private final Action previousAction;
    private JCheckBox caseSensitive;
    private JTextField searchField;
    private JLabel counterLabel;
    private JPanel panel;
    
    private Action closeAction = new AbstractAction()
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            panel.setVisible(false);
            closeAction.setEnabled(false);
            panel.getParent().transferFocus();
        }
    };
    
    private final Action findAction = new AbstractAction()
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            if (!panel.isVisible())
            {
                panel.setVisible(true);
                panel.getParent().validate();
                return;
            }
            reFocus();
        }
    };

    /**
     * Constructor.
     * @param documentListener DocumentListener instance.
     * @param changeListener ChangeListener instance.
     * @param compListener ComponentListener instance.
     * @param nextAction Action instance for next find.
     * @param previousAction Action instance for previous find.
     */
    SearchPanel(DocumentListener documentListener, ChangeListener changeListener,
                ComponentListener compListener, Action nextAction, Action previousAction)
    {
        this.nextAction = nextAction;
        this.previousAction = previousAction;
        initUI(documentListener, changeListener, compListener);
    }

    private void initUI(DocumentListener documentListener, ChangeListener changeListener,
                        ComponentListener compListener)
    {
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(documentListener);

        counterLabel = new JLabel();
        counterLabel.setVisible(false);

        JButton nextButton = new JButton();
        nextButton.setAction(nextAction);
        nextButton.setText("Next");

        JButton previousButton = new JButton();
        previousButton.setAction(previousAction);
        previousButton.setText("Previous");

        caseSensitive = new JCheckBox("Match case");
        caseSensitive.setSelected(false);
        caseSensitive.addChangeListener(changeListener);
        caseSensitive.setToolTipText("Check for case sensitive search");

        JButton crossButton = new JButton();
        crossButton.setAction(closeAction);
        crossButton.setText("Done");
        closeAction.setEnabled(false);
        
        panel = new JPanel();
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        panel.setBackground(new Color(230, 230, 230));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(Box.createHorizontalGlue());
        panel.add(searchField);
        panel.add(counterLabel);
        panel.add(previousButton);
        panel.add(nextButton);
        panel.add(caseSensitive);
        panel.add(Box.createRigidArea(new Dimension(5, 0)));
        panel.add(crossButton);

        panel.addComponentListener(compListener);

        searchField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), closeAction);
    }

    boolean isCaseSensitive()
    {
        return caseSensitive.isSelected();
    }

    String getSearchWord()
    {
        return searchField.getText();
    }

    void reset()
    {
        counterLabel.setVisible(false);
    }

    void updateCounterLabel(int now, int total)
    {
        if (!counterLabel.isVisible())
        {
            counterLabel.setVisible(true);
        }
        if (total == 0)
        {
            counterLabel.setText(" No match found ");
            nextAction.setEnabled(false);
            return;
        }
        counterLabel.setText(" " + now + " of " + total + " ");
    }

    JPanel getPanel()
    {
        return panel;
    }

    public void reFocus()
    {
        searchField.requestFocus();
        String searchKey = searchField.getText();
        searchField.setText(searchKey);
        searchField.setSelectionStart(0);
        searchField.setSelectionEnd(searchField.getText().length());

        closeAction.setEnabled(true);
    }

    public void addMenuListeners(PDFDebugger frame)
    {
        frame.getFindMenu().setEnabled(true);
        frame.getFindMenuItem().addActionListener(findAction);
        frame.getFindNextMenuItem().addActionListener(nextAction);
        frame.getFindPreviousMenuItem().addActionListener(previousAction);
    }

    public void removeMenuListeners(PDFDebugger frame)
    {
        frame.getFindMenu().setEnabled(false);
        frame.getFindMenuItem().removeActionListener(findAction);
        frame.getFindNextMenuItem().removeActionListener(nextAction);
        frame.getFindPreviousMenuItem().removeActionListener(previousAction);
    }
}
