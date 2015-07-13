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

package org.apache.pdfbox.tools.pdfdebugger.ui.textsearcher;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;

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
    private JMenu searchMenu;
    private KeyStroke findStroke;
    private KeyStroke closeStroke;
    private KeyStroke nextStroke;
    private KeyStroke previousStroke;
    private Action closeAction = new AbstractAction()
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            panel.setVisible(false);
            closeAction.setEnabled(false);
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
        counterLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));

        JButton nextButton = new JButton();
        nextButton.setAction(nextAction);
        nextButton.setText("Next");
        nextButton.setToolTipText("Find next");

        JButton previousButton = new JButton();
        previousButton.setAction(previousAction);
        previousButton.setText("Previous");
        previousButton.setToolTipText("Find previous");

        caseSensitive = new JCheckBox("Match case");
        caseSensitive.setSelected(false);
        caseSensitive.addChangeListener(changeListener);
        caseSensitive.setToolTipText("Check for case sensitive search");

        JButton crossButton = new JButton();
        crossButton.setAction(closeAction);
        closeAction.setEnabled(false);
        crossButton.setHideActionText(true);
        crossButton.setText("X");
        crossButton.setToolTipText("Close");


        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(searchField);
        panel.add(counterLabel);
        panel.add(nextButton);
        panel.add(previousButton);
        panel.add(caseSensitive);
        panel.add(crossButton);

        panel.addComponentListener(compListener);

        setNextFindStroke(KeyStroke.getKeyStroke("F3"));
        setPreviousStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_DOWN_MASK));
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

    private JMenu createSearchMenu()
    {
        JMenuItem findMenuItem = new JMenuItem();
        findMenuItem.setAction(findAction);
        findMenuItem.setText("Find");
        findMenuItem.setAccelerator(findStroke);

        JMenuItem closeMenuItem = new JMenuItem();
        closeMenuItem.setAction(closeAction);
        closeMenuItem.setText("Close");
        closeMenuItem.setAccelerator(closeStroke);

        JMenuItem nextFindMenuItem = new JMenuItem();
        nextFindMenuItem.setAction(nextAction);
        nextFindMenuItem.setText("Find next");
        nextFindMenuItem.setAccelerator(nextStroke);

        JMenuItem previousMenuItem = new JMenuItem();
        previousMenuItem.setAction(previousAction);
        previousMenuItem.setText("Find previous");
        previousMenuItem.setAccelerator(previousStroke);

        JMenu menu = new JMenu("Search");

        menu.add(findMenuItem);
        menu.addSeparator();
        menu.add(nextFindMenuItem);
        menu.add(previousMenuItem);
        menu.addSeparator();
        menu.add(closeMenuItem);

        return menu;
    }

    JMenu getSearchMenu()
    {
        if (searchMenu == null)
        {
            searchMenu = createSearchMenu();
        }
        return searchMenu;
    }

    void setFindStroke(JComponent parent, KeyStroke keyStroke)
    {
        if (findStroke != null)
        {
            parent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(findStroke);
        }
        findStroke = keyStroke;
        addActionToInput(parent, "showPanle", JComponent.WHEN_IN_FOCUSED_WINDOW, findStroke, findAction);
    }

    void setCloseStroke(JComponent parent, KeyStroke keyStroke)
    {
        if (closeStroke != null)
        {
            parent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(closeStroke);
        }
        closeStroke = keyStroke;
        addActionToInput(parent, "closePanel", JComponent.WHEN_IN_FOCUSED_WINDOW, closeStroke, closeAction);
    }

    void setNextFindStroke(KeyStroke keyStroke)
    {
        if (nextStroke != null)
        {
            panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(nextStroke);
        }
        nextStroke = keyStroke;
        addActionToInput(panel, "nextFind", JComponent.WHEN_IN_FOCUSED_WINDOW, nextStroke, nextAction);
    }

    void setPreviousStroke(KeyStroke keyStroke)
    {
        if (previousStroke != null)
        {
            panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(previousStroke);
        }
        previousStroke = keyStroke;
        addActionToInput(panel, "previousFind", JComponent.WHEN_IN_FOCUSED_WINDOW, previousStroke, previousAction);
    }

    private void addActionToInput(JComponent component, Object key, int state, KeyStroke keyStroke, Action action)
    {
        component.getInputMap(state).put(keyStroke, key);
        component.getActionMap().put(key, action);
    }
}
