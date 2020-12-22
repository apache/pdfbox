/*
 * Copyright 2016 The Apache Software Foundation.
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

import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Custom log dialog.
 *
 * @author John Hewson
 */
@SuppressWarnings({"serial","squid:MaximumInheritanceDepth"})
public class LogDialog extends JDialog
{
    private static LogDialog instance;
    private final JLabel logLabel;
    private final JTextPane textPane;
    private final JScrollPane scrollPane;
    private int fatalCount = 0;
    private int errorCount = 0;
    private int warnCount = 0;
    private int otherCount = 0;
    private int exceptionCount = 0;

    private LogDialog(final Frame owner, final JLabel logLabel)
    {
        super(owner);
        this.logLabel = logLabel;
        
        textPane = new JTextPane();
        scrollPane = new JScrollPane(textPane);
        getContentPane().add(scrollPane);
        
        this.pack();
    }

    public static void init(final Frame owner, final JLabel logLabel)
    {
        instance = new LogDialog(owner, logLabel);
    }

    public static LogDialog instance()
    {
        return instance;
    }

    public void log(final String name, final String level, final Object o, final Throwable throwable)
    {
        final StyledDocument doc = textPane.getStyledDocument();
        
        final String levelText;
        final SimpleAttributeSet levelStyle = new SimpleAttributeSet();
        switch (level)
        {
            case "fatal":
                levelText = "Fatal";
                StyleConstants.setForeground(levelStyle, Color.WHITE);
                StyleConstants.setBackground(levelStyle, Color.BLACK);
                fatalCount++;
                break;
            case "error":
                levelText = "Error";
                StyleConstants.setForeground(levelStyle, new Color(0xFF291F));
                StyleConstants.setBackground(levelStyle, new Color(0xFFF0F0));
                errorCount++;
                break;
            case "warn":
                levelText = "Warning";
                StyleConstants.setForeground(levelStyle, new Color(0x614201));
                StyleConstants.setBackground(levelStyle, new Color(0xFFFCE5));
                warnCount++;
                break;
            case "info":
                levelText = "Info";
                StyleConstants.setForeground(levelStyle, new Color(0x203261));
                StyleConstants.setBackground(levelStyle, new Color(0xE2E8FF));
                otherCount++;
                break;
            case "debug":
                levelText = "Debug";
                StyleConstants.setForeground(levelStyle, new Color(0x32612E));
                StyleConstants.setBackground(levelStyle, new Color(0xF4FFEC));
                otherCount++;
                break;
            case "trace":
                levelText = "Trace";
                StyleConstants.setForeground(levelStyle, new Color(0x64438D));
                StyleConstants.setBackground(levelStyle, new Color(0xFEF3FF));
                otherCount++;
                break;
            default:
                throw new Error(level);
        }

        final SimpleAttributeSet nameStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(nameStyle, new Color(0x6A6A6A));

        final String shortName = name.substring(name.lastIndexOf('.') + 1);
        String message = o == null ? "(null)" : o.toString();
        
        if (throwable != null)
        {
            final StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            message += "\n    " + sw.toString();
            exceptionCount++;
        }
        
        try
        {
            doc.insertString(doc.getLength(), " " + levelText + " ", levelStyle);
            doc.insertString(doc.getLength(), " [" + shortName + "]", nameStyle);
            doc.insertString(doc.getLength(), " " + message + "\n", null);
        }
        catch (BadLocationException e)
        {
            throw new Error(e);
        }
        textPane.setCaretPosition(doc.getLength());

        // update status bar with new counts
        updateStatusBar();
    }
    
    private void updateStatusBar()
    {
        final List<String> infos = new ArrayList<>();

        if (exceptionCount > 0)
        {
            infos.add(exceptionCount + " exception" + (errorCount > 1 ? "s" : ""));
        }

        if (fatalCount > 0)
        {
            infos.add(errorCount + " error" + (errorCount > 1 ? "s" : ""));
        }

        if (errorCount > 0)
        {
            infos.add(errorCount + " error" + (errorCount > 1 ? "s" : ""));
        }

        if (warnCount > 0)
        {
            infos.add(warnCount + " warning" + (warnCount > 1 ? "s" : ""));
        }

        if (otherCount > 0)
        {
            infos.add(otherCount + " message" + (otherCount > 1 ? "s" : ""));
        }
        
        String info = "";
        for (final String str : infos)
        {
            if (info.length() > 0)
            {
                info += ", ";
            }
            info += str;
        }
        
        logLabel.setText(info);
    }
    
    public void clear()
    {
        fatalCount = 0;
        errorCount = 0;
        warnCount = 0;
        otherCount = 0;
        exceptionCount = 0;
        textPane.setText("");
        logLabel.setText("");
    }

    // these two just to avoid the "overridable method call in constructor" warning

    @Override
    public final Container getContentPane()
    {
        return super.getContentPane();
    }

    @Override
    public final void pack()
    {
        super.pack();
    }
}
