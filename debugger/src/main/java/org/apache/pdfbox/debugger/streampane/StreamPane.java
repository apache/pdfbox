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

package org.apache.pdfbox.debugger.streampane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.debugger.hexviewer.HexView;
import org.apache.pdfbox.debugger.streampane.tooltip.ToolTipController;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.util.XMLUtil;
import org.w3c.dom.Document;

/**
 * @author Khyrul Bashar
 *
 * A class that shows the COSStream.
 */
public class StreamPane implements ActionListener
{
    private static final Log LOG = LogFactory.getLog(StreamPane.class);

    private static final StyleContext CONTEXT = StyleContext.getDefaultStyleContext();
    private static final Style OPERATOR_STYLE = CONTEXT.addStyle("operator", null);
    private static final Style NUMBER_STYLE = CONTEXT.addStyle("number", null);
    private static final Style STRING_STYLE = CONTEXT.addStyle("string", null);
    private static final Style ESCAPE_STYLE = CONTEXT.addStyle("escape", null);
    private static final Style NAME_STYLE = CONTEXT.addStyle("name", null);
    private static final Style INLINE_IMAGE_STYLE = CONTEXT.addStyle("inline_image", null);

    static
    {
        StyleConstants.setForeground(OPERATOR_STYLE, new Color(25, 55, 156));
        StyleConstants.setForeground(NUMBER_STYLE, new Color(51, 86, 18));
        StyleConstants.setForeground(STRING_STYLE, new Color(128, 35, 32));
        StyleConstants.setForeground(ESCAPE_STYLE, new Color(179, 49, 36));
        StyleConstants.setForeground(NAME_STYLE, new Color(140, 38, 145));
        StyleConstants.setForeground(INLINE_IMAGE_STYLE, new Color(116, 113, 39));
    }

    private final JPanel panel;
    private final HexView hexView;
    private final JTabbedPane tabbedPane;
    private final StreamPaneView rawView;
    private final StreamPaneView niceView;
    private final Stream stream;
    private ToolTipController tTController;
    private PDResources resources;

    /**
     * Constructor.
     *
     * @param cosStream COSStream instance.
     * @param isContentStream boolean instance. This says if a stream is content stream or not.
     * @param isThumb This says if a stream is an thumbnail image or not.
     * @param resourcesDic COSDictionary instance that holds the resource dictionary for the stream.
     * @throws IOException if there is an I/O error during internal data transfer.
     */
    public StreamPane(COSStream cosStream, boolean isContentStream, boolean isThumb,
                      COSDictionary resourcesDic) throws IOException
    {
        this.stream = new Stream(cosStream, isThumb);
        if (resourcesDic != null)
        {
            resources = new PDResources(resourcesDic);
            tTController = new ToolTipController(resources);
        }

        panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 500));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        rawView = new StreamPaneView();
        hexView = new HexView();
        if (isContentStream || stream.isXmlMetadata())
        {
            niceView = new StreamPaneView();
        }
        else
        {
            niceView = null;
        }

        if (stream.isImage())
        {
            panel.add(createHeaderPanel(stream.getFilterList(), Stream.IMAGE, this));
            requestImageShowing();
        }
        else
        {
            panel.add(createHeaderPanel(stream.getFilterList(), Stream.DECODED, this));
            requestStreamText(Stream.DECODED);
        }

        tabbedPane = new JTabbedPane();
        if (stream.isImage())
        {
            tabbedPane.add("Image view", rawView.getStreamPanel());
        }
        else if (niceView != null)
        {
            tabbedPane.add("Nice view", niceView.getStreamPanel());
            tabbedPane.add("Raw view", rawView.getStreamPanel());
            tabbedPane.add("Hex view", hexView.getPane());
        }
        else
        {
            tabbedPane.add("Text view", rawView.getStreamPanel());
            tabbedPane.add("Hex view", hexView.getPane());
        }

        panel.add(tabbedPane);
    }

    public JComponent getPanel()
    {
        return panel;
    }

    private JPanel createHeaderPanel(List<String> availableFilters, String i, ActionListener actionListener)
    {
        JComboBox<String> filters = new JComboBox<>(availableFilters.toArray(new String[0]));
        filters.setSelectedItem(i);
        filters.addActionListener(actionListener);

        JPanel headerPanel = new JPanel(new FlowLayout());
        headerPanel.add(filters);

        return headerPanel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        if ("comboBoxChanged".equals(actionEvent.getActionCommand()))
        {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            String currentFilter = (String) comboBox.getSelectedItem();

            try
            {
                if (currentFilter.equals(Stream.IMAGE))
                {
                    requestImageShowing();
                    tabbedPane.removeAll();
                    tabbedPane.add("Image view", rawView.getStreamPanel());
                    return;
                }
                tabbedPane.removeAll();
                if (Stream.DECODED.equals(currentFilter) && niceView != null)
                {
                    tabbedPane.add("Nice view", niceView.getStreamPanel());
                    tabbedPane.add("Raw view", rawView.getStreamPanel());
                    tabbedPane.add("Hex view", hexView.getPane());
                }
                else
                {
                    tabbedPane.add("Text view", rawView.getStreamPanel());
                    tabbedPane.add("Hex view", hexView.getPane());
                }
                requestStreamText(currentFilter);
            }
            catch (IOException e)
            {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void requestImageShowing()
    {
        if (stream.isImage())
        {
            BufferedImage image;
            synchronized (stream)
            {
                image = stream.getImage(resources);
            }
            if (image == null)
            {
                JOptionPane.showMessageDialog(panel, "image not available (filter missing?)");
                return;
            }
            rawView.showStreamImage(image);
        }
    }

    private void requestStreamText(String command) throws IOException
    {
        new DocumentCreator(rawView, command, false).execute();
        if (niceView != null)
        {
            new DocumentCreator(niceView, command, true).execute();
        }
        synchronized (stream)
        {
            InputStream is = stream.getStream(command);
            if (is == null)
            {
                JOptionPane.showMessageDialog(panel, command + " text not available (filter missing?)");
                return;
            }
            hexView.changeData(IOUtils.toByteArray(is));
        }
    }

    /**
     * A SwingWorker extended class that convert the stream to text loads in a document.
     */
    private final class DocumentCreator extends SwingWorker<StyledDocument, Integer>
    {
        private final StreamPaneView targetView;
        private final String filterKey;
        private final boolean nice;
        private int indent;
        private boolean needIndent;

        private DocumentCreator(StreamPaneView targetView, String filterKey, boolean nice)
        {
            this.targetView = targetView;
            this.filterKey = filterKey;
            this.nice = nice;
        }

        @Override
        protected StyledDocument doInBackground()
        {
            // default encoding to use when reading text base content
            String encoding = "ISO-8859-1";
            synchronized (stream)
            {
                if (stream.isXmlMetadata())
                {
                    encoding = "UTF-8";
                }
                InputStream inputStream = stream.getStream(filterKey);
                if (nice && Stream.DECODED.equals(filterKey))
                {
                    if (stream.isXmlMetadata())
                    {
                        return getXMLDocument(inputStream);
                    }
                    StyledDocument document = getContentStreamDocument(inputStream);
                    if (document != null)
                    {
                        return document;
                    }
                    return getDocument(inputStream, encoding);
                }
                return getDocument(inputStream, encoding);
            }
        }

        @Override
        protected void done()
        {
            try
            {
                targetView.showStreamText(get(), tTController);
            }
            catch (InterruptedException | ExecutionException e)
            {
                LOG.error(e.getMessage(), e);
            }
        }

        private String getStringOfStream(InputStream in, String encoding)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                IOUtils.copy(in, baos);
                return baos.toString(encoding);
            }
            catch (IOException e)
            {
                LOG.error(e.getMessage(), e);
                return null;
            }
        }

        private StyledDocument getDocument(InputStream inputStream, String encoding)
        {
            StyledDocument docu = new DefaultStyledDocument();
            if (inputStream != null)
            {
                String data = getStringOfStream(inputStream, encoding);

                // CR is not displayed in the raw view (see file from PDFBOX-4964),
                // but LF is displayed, so lets first replace CR LF with LF and then
                // replace the remaining CRs with LF
                if (data != null)
                {
                    data = data.replaceAll("\\R", "\n");
                }

                try
                {
                    docu.insertString(0, data, null);
                }
                catch (BadLocationException e)
                {
                    LOG.error(e.getMessage(), e);
                }
            }
            return docu;
        }

        private StyledDocument getXMLDocument(InputStream inputStream)
        {
            StyledDocument docu = new DefaultStyledDocument();
            if (inputStream != null)
            {
                try
                {
                    Document doc = XMLUtil.parse(inputStream);
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
                    StringWriter sw = new StringWriter();
                    StreamResult result = new StreamResult(sw);
                    DOMSource source = new DOMSource(doc);
                    transformer.transform(source, result);
                    docu.insertString(0, sw.toString(), null);
                }
                catch (IOException | TransformerFactoryConfigurationError | IllegalArgumentException |
                       TransformerException | BadLocationException ex)
                {
                    LOG.error(ex.getMessage(), ex);
                }
            }
            return docu;
        }

        private StyledDocument getContentStreamDocument(InputStream inputStream)
        {
            StyledDocument docu = new DefaultStyledDocument();

            PDFStreamParser parser;
            try
            {
                parser = new PDFStreamParser(IOUtils.toByteArray(inputStream));
                parser.parse().forEach(obj -> writeToken(obj, docu));
            }
            catch (IOException e)
            {
                return null;
            }
            return docu;
        }

        private void writeToken(Object obj, StyledDocument docu)
        {
            try
            {
                if (obj instanceof Operator)
                {
                    addOperators(obj, docu);
                }
                else
                {
                    writeOperand(obj, docu);
                }
            }
            catch (BadLocationException e)
            {
                LOG.error(e.getMessage(), e);
            }
        }

        private void writeOperand(Object obj, StyledDocument docu) throws BadLocationException
        {
            writeIndent(docu);

            if (obj instanceof COSName)
            {
                String str = "/" + ((COSName) obj).getName();
                docu.insertString(docu.getLength(), str + " ", NAME_STYLE);
            }
            else if (obj instanceof COSBoolean)
            {
                String str = obj.toString();
                docu.insertString(docu.getLength(), str + " ", null);
            }
            else if (obj instanceof COSArray)
            {
                docu.insertString(docu.getLength(), "[ ", null);
                for (COSBase elem : (COSArray) obj)
                {
                    writeOperand(elem, docu);
                }
                docu.insertString(docu.getLength(), "] ", null);
            }
            else if (obj instanceof COSString)
            {
                docu.insertString(docu.getLength(), "(", null);
                byte[] bytes = ((COSString) obj).getBytes();
                for (byte b : bytes)
                {
                    int chr = b & 0xff;
                    if (chr == '(' || chr == ')' || chr == '\\')
                    {
                        // PDF reserved characters must be escaped
                        String str = "\\" + (char)chr;
                        docu.insertString(docu.getLength(), str, ESCAPE_STYLE);
                    }
                    else
                    if (chr < 0x20 || chr > 0x7e)
                    {
                        // non-printable ASCII is shown as an octal escape
                        String str = String.format("\\%03o", chr);
                        docu.insertString(docu.getLength(), str, ESCAPE_STYLE);
                    }
                    else
                    {
                        String str = Character.toString((char) chr);
                        docu.insertString(docu.getLength(), str, STRING_STYLE);
                    }
                }
                docu.insertString(docu.getLength(), ") ", null);
            }
            else if (obj instanceof COSNumber)
            {
                String str;
                if (obj instanceof COSFloat)
                {
                    str = Float.toString(((COSFloat) obj).floatValue());
                }
                else
                {
                    str = Integer.toString(((COSNumber) obj).intValue());
                }
                docu.insertString(docu.getLength(), str + " ", NUMBER_STYLE);
            }
            else if (obj instanceof COSDictionary)
            {
                docu.insertString(docu.getLength(), "<< ", null);
                COSDictionary dict = (COSDictionary) obj;
                for (Map.Entry<COSName, COSBase> entry : dict.entrySet())
                {
                    writeOperand(entry.getKey(), docu);
                    writeOperand(entry.getValue(), docu);
                }
                docu.insertString(docu.getLength(), ">> ", null);
            }
            else if (obj instanceof COSNull)
            {
                docu.insertString(docu.getLength(), "null ", null);
            }
            else
            {
                String str = obj.toString();
                str = str.substring(str.indexOf('{') + 1, str.length() - 1);
                docu.insertString(docu.getLength(), str + " ", null);
            }
        }

        private void addOperators(Object obj, StyledDocument docu) throws BadLocationException
        {
            Operator op = (Operator) obj;

            if (op.getName().equals(OperatorName.END_TEXT)
                    || op.getName().equals(OperatorName.RESTORE)
                    || op.getName().equals(OperatorName.END_MARKED_CONTENT))
            {
                indent--;
            }
            writeIndent(docu);

            if (op.getName().equals(OperatorName.BEGIN_INLINE_IMAGE))
            {
                docu.insertString(docu.getLength(), OperatorName.BEGIN_INLINE_IMAGE + "\n", OPERATOR_STYLE);
                COSDictionary dic = op.getImageParameters();
                for (COSName key : dic.keySet())
                {
                    Object value = dic.getDictionaryObject(key);
                    docu.insertString(docu.getLength(), "/" + key.getName() + " ", null);
                    writeToken(value, docu);
                    docu.insertString(docu.getLength(), "\n", null);
                }
                String imageString = new String(op.getImageData(), StandardCharsets.ISO_8859_1);
                docu.insertString(docu.getLength(), OperatorName.BEGIN_INLINE_IMAGE_DATA + "\n", INLINE_IMAGE_STYLE);
                docu.insertString(docu.getLength(), imageString, null);
                docu.insertString(docu.getLength(), "\n", null);
                docu.insertString(docu.getLength(), OperatorName.END_INLINE_IMAGE + "\n", OPERATOR_STYLE);
            }
            else
            {
                String operator = ((Operator) obj).getName();
                docu.insertString(docu.getLength(), operator + "\n", OPERATOR_STYLE);

                // nested opening operators
                if (op.getName().equals(OperatorName.BEGIN_TEXT) ||
                    op.getName().equals(OperatorName.SAVE) ||
                    op.getName().equals(OperatorName.BEGIN_MARKED_CONTENT) ||
                    op.getName().equals(OperatorName.BEGIN_MARKED_CONTENT_SEQ))
                {
                    indent++;
                }
            }
            needIndent = true;
        }

        void writeIndent(StyledDocument docu) throws BadLocationException
        {
            if (needIndent)
            {
                for (int i = 0; i < indent; i++)
                {
                    docu.insertString(docu.getLength(), "  ", null);
                }
                needIndent = false;
            }
        }
    }
}
