/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

/**
 * A dialog to display a runtime exception stack trace.
 *
 * @author Pinaki Poddar
 *
 * Modified from ErrorDialog.java and SwingHelper.java of the Apache OpenJPA
 * <a href="https://svn.apache.org/repos/asf/openjpa/trunk/openjpa-examples/openbooks/src/main/java/jpa/tools/swing/">jpa.tools.swing
 * package</a>.
 *
 */
@SuppressWarnings({"serial","squid:MaximumInheritanceDepth"})
public class ErrorDialog extends JDialog
{
    private static final List<String> FILTERS = Arrays.asList(
            "java.awt.",
            "javax.swing.",
            "sun.reflect.",
            "java.util.concurrent.");
    private static final Dimension MESSAGE_SIZE = new Dimension(800, 200);
    private static final Dimension STACKTRACE_SIZE = new Dimension(800, 300);
    private static final Dimension TOTAL_SIZE = new Dimension(800, 500);
    private static final int BORDER_SIZE = 20;

    private static final String NEWLINE = "\r\n";
    private static final String INDENT = "    ";

    private boolean showingDetails;
    private boolean isFiltering = true;
    private JComponent message;
    private JComponent main;
    private JScrollPane details;
    private JTextPane stacktrace;
    private final Throwable error;

    /**
     * Creates a modal dialog to display the given exception message.
     *
     * @param t the exception to display
     */
    public ErrorDialog(Throwable t)
    {
        this(null, null, t);
    }

    /**
     * Creates a modal dialog to display the given exception message.
     *
     * @param owner if non-null, then the dialog is positioned (centered) w.r.t. this component
     * @param t the exception to display
     */
    public ErrorDialog(JComponent owner, Throwable t)
    {
        this(owner, null, t);
    }

    /**
     * Creates a modal dialog to display the given exception message.
     *
     * @param owner if non-null, then the dialog is positioned (centered) w.r.t. this component
     * @param icon the icon to display
     * @param t the exception to display
     */
    public ErrorDialog(JComponent owner, Icon icon, Throwable t)
    {
        setTitle(t.getClass().getName());
        setModal(true);
        if (icon instanceof ImageIcon)
        {
            setIconImage(((ImageIcon) icon).getImage());
        }
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        error = t;
        message = createErrorMessage(error);
        main = createContent();
        getContentPane().add(main);

        pack();
        position(this, owner);
    }

    /**
     * Position the given component at the center of the given parent component or physical screen.
     *
     * @param c the component to be positioned
     * @param parent the component whose center will match the center of the given component. If
     * null, the given component will match the screen center.
     *
     */
    static void position(Component c, Component parent)
    {
        Dimension d = c.getPreferredSize();
        if (parent == null)
        {
            Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
            c.setLocation(s.width / 2 - d.width / 2, s.height / 2 - d.height / 2);
        }
        else
        {
            Point p = parent.getLocationOnScreen();
            int pw = parent.getWidth();
            int ph = parent.getHeight();
            c.setLocation(p.x + pw / 2 - d.width / 2, p.y + ph / 2 - d.height / 2);
        }
    }

    /**
     * Creates the display with the top-level exception message followed by a pane (that toggles)
     * for detailed stack traces.
     */
    final JComponent createContent()
    {
        final JButton showDetails = new JButton("Show Details >>");
        showDetails.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (showingDetails)
                {
                    main.remove(details);
                    main.validate();
                    main.setPreferredSize(MESSAGE_SIZE);
                }
                else
                {
                    if (details == null)
                    {
                        details = createDetailedMessage();
                        StringBuilder buffer = new StringBuilder();
                        stacktrace.setText(generateStackTrace(error, buffer).toString());
                        stacktrace.setCaretPosition(0);
                        stacktrace.setBackground(main.getBackground());
                        stacktrace.setPreferredSize(STACKTRACE_SIZE);
                    }
                    main.add(details, BorderLayout.CENTER);
                    main.validate();
                    main.setPreferredSize(TOTAL_SIZE);
                }
                showingDetails = !showingDetails;
                showDetails.setText(showingDetails ? "<< Hide Details" : "Show Details >>");
                ErrorDialog.this.pack();
            }
        });
        JPanel messagePanel = new JPanel();

        final JCheckBox filter = new JCheckBox("Filter stack traces");
        filter.setSelected(isFiltering);
        filter.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                isFiltering = filter.isSelected();
                StringBuilder buffer = new StringBuilder();
                stacktrace.setText(generateStackTrace(error, buffer).toString());
                stacktrace.setCaretPosition(0);
                stacktrace.repaint();
            }
        });
        message.setBackground(messagePanel.getBackground());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(Box.createHorizontalStrut(BORDER_SIZE));
        buttonPanel.add(showDetails);
        buttonPanel.add(filter);
        buttonPanel.add(Box.createHorizontalGlue());
        messagePanel.setLayout(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        messagePanel.add(message, BorderLayout.CENTER);
        messagePanel.add(buttonPanel, BorderLayout.SOUTH);
        messagePanel.setPreferredSize(MESSAGE_SIZE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(messagePanel, BorderLayout.NORTH);
        
        // allow closing with ESC
        ActionListener actionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                dispose();
            }
        };
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        panel.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);        
        
        return panel;
    }

    /**
     * Creates a non-editable widget to display the error message.
     *
     */
    final JComponent createErrorMessage(Throwable t)
    {
        String txt = t.getLocalizedMessage();
        JEditorPane msg = new JEditorPane();
        msg.setContentType("text/plain");
        msg.setEditable(false);
        msg.setText(txt);
        return msg;
    }

    /**
     * Creates a non-editable widget to display the detailed stack trace.
     */
    JScrollPane createDetailedMessage()
    {
        stacktrace = new JTextPane();
        stacktrace.setEditable(false);
        return new JScrollPane(stacktrace,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * Recursively print the stack trace on the given buffer.
     */
    StringBuilder generateStackTrace(Throwable t, StringBuilder buffer)
    {
        buffer.append(t.getClass().getName()).append(": ").append(t.getMessage()).append(NEWLINE);
        buffer.append(toString(t.getStackTrace()));
        Throwable cause = t.getCause();
        if (cause != null && !cause.equals(t))
        {
            buffer.append("Caused by: ");
            generateStackTrace(cause, buffer);
        }
        return buffer;
    }

    StringBuilder toString(StackTraceElement[] traces)
    {
        StringBuilder err = new StringBuilder();
        for (StackTraceElement e : traces)
        {
            if (!isFiltering || !isSuppressed(e.getClassName()))
            {
                String str = e.toString();
                err.append(INDENT).append(str).append(NEWLINE);
            }
        }
        return err;
    }

    /**
     * Affirms if the error messages from the given class name is to be suppressed.
     */
    private boolean isSuppressed(String className)
    {
        for (String s : FILTERS)
        {
            if (className.startsWith(s))
            {
                return true;
            }
        }
        return false;
    }
}
