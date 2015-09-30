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

package org.apache.pdfbox.debugger.hexviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

/**
 * @author Khyrul Bashar
 *
 * This class hosts all the UI components of Hex view and cordinate among them.
 */
class HexEditor extends JPanel implements SelectionChangeListener
{
    private final HexModel model;
    private HexPane hexPane;
    private ASCIIPane asciiPane;
    private AddressPane addressPane;
    private StatusPane statusPane;
    private final Action jumpToIndex;

    private int selectedIndex = -1;

    /**
     * Constructor.
     * @param model HexModel instance.
     */
    HexEditor(HexModel model)
    {
        super();
        this.jumpToIndex = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                createJumpDialog().setVisible(true);
            }
        };
        this.model = model;
        createView();
    }

    private void createView()
    {
        setLayout(new GridBagLayout());

        addressPane = new AddressPane(model.totalLine());
        hexPane = new HexPane(model);
        hexPane.addHexChangeListeners(model);
        asciiPane = new ASCIIPane(model);
        UpperPane upperPane = new UpperPane();
        statusPane = new StatusPane();

        model.addHexModelChangeListener(hexPane);
        model.addHexModelChangeListener(asciiPane);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setPreferredSize(new Dimension(HexView.TOTAL_WIDTH, HexView.CHAR_HEIGHT * (model.totalLine() + 1)));
        panel.add(addressPane);
        panel.add(hexPane);
        panel.add(asciiPane);

        JScrollPane scrollPane = getScrollPane();
        scrollPane.setViewportView(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.02;
        add(upperPane, gbc);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollPane, gbc);
        gbc.gridy = 2;
        gbc.weightx = 0.1;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(statusPane, gbc);

        hexPane.addSelectionChangeListener(this);

        KeyStroke jumpKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK);
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(jumpKeyStroke, "jump");
        this.getActionMap().put("jump", jumpToIndex);
    }

    private JScrollPane getScrollPane()
    {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(new LineBorder(Color.LIGHT_GRAY));

        Action blankAction = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
            }
        };

        scrollPane.getActionMap().put("unitScrollDown", blankAction);
        scrollPane.getActionMap().put("unitScrollLeft", blankAction);
        scrollPane.getActionMap().put("unitScrollRight", blankAction);
        scrollPane.getActionMap().put("unitScrollUp", blankAction);

        JScrollBar verticalScrollBar = scrollPane.createVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(HexView.CHAR_HEIGHT);
        verticalScrollBar.setBlockIncrement(HexView.CHAR_HEIGHT * 20);
        verticalScrollBar.setValues(0, 1, 0, HexView.CHAR_HEIGHT * (model.totalLine()+1));
        scrollPane.setVerticalScrollBar(verticalScrollBar);

        return scrollPane;
    }

    @Override
    public void selectionChanged(SelectEvent event)
    {
        int index = event.getHexIndex();

        if (event.getNavigation().equals(SelectEvent.NEXT))
        {
            index += 1;
        }
        else if (event.getNavigation().equals(SelectEvent.PREVIOUS))
        {
            index -= 1;
        }
        else if (event.getNavigation().equals(SelectEvent.UP))
        {
            index -= 16;

        }
        else if (event.getNavigation().equals(SelectEvent.DOWN))
        {
            index += 16;
        }
        if (index >= 0 && index <= model.size() - 1)
        {
            hexPane.setSelected(index);
            addressPane.setSelected(index);
            asciiPane.setSelected(index);
            statusPane.updateStatus(index);
            selectedIndex = index;
        }
    }

    private JDialog createJumpDialog()
    {
        final JDialog dialog = new JDialog(SwingUtilities.windowForComponent(this), "Jump to index");
        dialog.setLocationRelativeTo(this);
        final JLabel nowLabel = new JLabel("Present index: " + selectedIndex);
        final JLabel label = new JLabel("Index to go:");
        final JTextField field = new JFormattedTextField(NumberFormat.getIntegerInstance());
        field.setPreferredSize(new Dimension(100, 20));

        field.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                int index = Integer.parseInt(field.getText(), 10);
                if (index >= 0 && index <= model.size() - 1)
                {
                    selectionChanged(new SelectEvent(index, SelectEvent.IN));
                    dialog.dispose();
                }
            }
        });

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(nowLabel);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(label);
        inputPanel.add(field);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(panel);
        contentPanel.add(inputPanel);
        dialog.getContentPane().add(contentPanel);
        dialog.pack();
        return dialog;
    }

}
