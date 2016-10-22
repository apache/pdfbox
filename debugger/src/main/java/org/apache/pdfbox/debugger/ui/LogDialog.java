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
public class LogDialog extends JDialog
{
    private static LogDialog instance;
    
    public static void init(Frame owner, JLabel logLabel)
    {
        instance = new LogDialog(owner, logLabel);
    }

    public static LogDialog instance()
    {
        return instance;
    }

    private final JLabel logLabel;
    private final JTextPane textPane;
    private JScrollPane scrollPane;
    private int fatalCount = 0;
    private int errorCount = 0;
    private int warnCount = 0;
    private int otherCount = 0;
    private int exceptionCount = 0;
    
    private LogDialog(Frame owner, JLabel logLabel)
    {
        super(owner);
        this.logLabel = logLabel;
        
        textPane = new JTextPane();
        scrollPane = new JScrollPane(textPane);
        getContentPane().add(scrollPane);
        
        this.pack();
    }
    
    public void log(String name, String level, Object o, Throwable throwable)
    {
        StyledDocument doc = textPane.getStyledDocument();
        
        String levelText;
        SimpleAttributeSet levelStyle = new SimpleAttributeSet();
        if (level.equals("fatal"))
        {
            levelText = "Fatal";
            StyleConstants.setForeground(levelStyle, Color.WHITE);
            StyleConstants.setBackground(levelStyle, Color.BLACK);
            fatalCount++;
        } 
        else if (level.equals("error"))
        {
            levelText = "Error";
            StyleConstants.setForeground(levelStyle, new Color(0xFF291F));
            StyleConstants.setBackground(levelStyle, new Color(0xFFF0F0));
            errorCount++;
        } 
        else if (level.equals("warn"))
        {
            levelText = "Warning";
            StyleConstants.setForeground(levelStyle, new Color(0x614201));
            StyleConstants.setBackground(levelStyle, new Color(0xFFFCE5));
            warnCount++;
        } 
        else if (level.equals("info"))
        {
            levelText = "Info";
            StyleConstants.setForeground(levelStyle, new Color(0x203261));
            StyleConstants.setBackground(levelStyle, new Color(0xE2E8FF));
            otherCount++;
        } 
        else if (level.equals("debug"))
        {
            levelText = "Debug";
            StyleConstants.setForeground(levelStyle, new Color(0x32612E));
            StyleConstants.setBackground(levelStyle, new Color(0xF4FFEC));
            otherCount++;
        } 
        else if (level.equals("trace"))
        {
            levelText = "Trace";
            StyleConstants.setForeground(levelStyle, new Color(0x64438D));
            StyleConstants.setBackground(levelStyle, new Color(0xFEF3FF));
            otherCount++;
        } 
        else
        {
            throw new Error(level);
        }

        SimpleAttributeSet nameStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(nameStyle, new Color(0x6A6A6A));

        String shortName = name.substring(name.lastIndexOf('.') + 1);
        String message = o.toString();
        
        if (throwable != null)
        {
            StringWriter sw = new StringWriter();
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
        List<String> infos = new ArrayList<String>();

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
        for (String str : infos)
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
}
