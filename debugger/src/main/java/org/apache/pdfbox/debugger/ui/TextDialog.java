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

import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Window for text extraction result.
 *
 * @author Tilman Hausherr
 * @author John Hewson
 */
@SuppressWarnings({"serial","squid:MaximumInheritanceDepth"})
public class TextDialog extends JDialog
{
    private static TextDialog instance;
    private final JTextPane textPane;
    private final JScrollPane scrollPane;

    private TextDialog(Frame owner)
    {
        super(owner);

        textPane = new JTextPane();
        Font font = textPane.getFont();
        textPane.setFont(font.deriveFont(font.getSize2D() * 1.5f));
        scrollPane = new JScrollPane(textPane);
        getContentPane().add(scrollPane);
        pack();
    }

    public static void init(Frame owner)
    {
        instance = new TextDialog(owner);
    }

    public static TextDialog instance()
    {
        return instance;
    }

    public void clear()
    {
        textPane.setText("");
    }

    public void setText(String text)
    {
        textPane.setText(text);
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
