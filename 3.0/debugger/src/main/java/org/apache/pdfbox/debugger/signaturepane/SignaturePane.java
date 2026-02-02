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

package org.apache.pdfbox.debugger.signaturepane;

import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.apache.pdfbox.cos.COSString;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.util.ASN1Dump;

/**
 * For displaying the Contents of a digital signature, which is encoded in ASN.1 format.
 */
public class SignaturePane
{
    private static final String TEXT_TAB = "ASN.1 View";
    private static final int FONT_SIZE = ((Font)UIManager.get("Label.font")).getSize();
    private static final Font FONT_MONOSPACED = new Font("monospaced", Font.PLAIN, FONT_SIZE);

    private final JTabbedPane tabbedPane;

    public SignaturePane(COSString cosString)
    {
        tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(300, 500));
        tabbedPane.addTab(TEXT_TAB, new JScrollPane(createTextView(cosString)));
    }

    private JTextPane createTextView(COSString cosString)
    {
        JTextPane textPane = new JTextPane();
        textPane.setText(getTextString(cosString));
        textPane.setEditable(false);
        textPane.setFont(FONT_MONOSPACED);
        textPane.setCaretPosition(0);
        return textPane;
    }

    private String getTextString(COSString cosString)
    {
        String text;
        ASN1StreamParser parser = new ASN1StreamParser(cosString.getBytes());
        try
        {
            ASN1Encodable encodable = parser.readObject();
            text = ASN1Dump.dumpAsString(encodable, true);
        }
        catch (IOException e)
        {
            text = "<" + cosString.toHexString() + ">";
        }
        return text;
    }

    public JTabbedPane getPane()
    {
        return tabbedPane;
    }
}
