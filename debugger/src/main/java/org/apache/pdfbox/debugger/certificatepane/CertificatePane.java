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
package org.apache.pdfbox.debugger.certificatepane;

import java.awt.Dimension;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;

/**
 * For displaying the Contents of a certificate.
 *
 * @author Tilman Hausherr
 */
public class CertificatePane
{
    private static final String TEXT_TAB = "Certificate View";
    private static final int FONT_SIZE = ((Font)UIManager.get("Label.font")).getSize();
    private static final Font FONT_MONOSPACED = new Font("monospaced", Font.PLAIN, FONT_SIZE);

    private final JTabbedPane tabbedPane;

    public CertificatePane(COSBase base)
    {
        tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(300, 500));
        tabbedPane.addTab(TEXT_TAB, new JScrollPane(createTextView(base)));
    }

    private JTextPane createTextView(COSBase base)
    {
        JTextPane textPane = new JTextPane();
        textPane.setText(getTextString(base));
        textPane.setEditable(false);
        textPane.setFont(FONT_MONOSPACED);
        textPane.setCaretPosition(0);
        return textPane;
    }

    private String getTextString(COSBase base)
    {
        try (InputStream is = createInputStream(base))
        {
            return CertificateFactory.getInstance("X.509").generateCertificate(is).toString();
        }
        catch (CertificateException | IOException ex)
        {
            return ex.getMessage();
        }
    }

    private InputStream createInputStream(COSBase base) throws IOException
    {
        if (base instanceof COSStream)
        {
            return ((COSStream) base).createInputStream();
        }
        if (base instanceof COSString)
        {
            return new ByteArrayInputStream(((COSString) base).getBytes());
        }
        throw new IllegalArgumentException("COSString or COSStream expected here");
    }

    public JTabbedPane getPane()
    {
        return tabbedPane;
    }
}
