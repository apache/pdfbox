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
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
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
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.debugger.hexviewer.HexView;
import org.apache.pdfbox.debugger.streampane.tooltip.ToolTipController;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.util.Charsets;

/**
 * @author Khyrul Bashar
 *
 * A class that shows the COSStream.
 */
public class StreamPane implements ActionListener
{
    public static final String BEGIN_TEXT_OBJECT = "BT";
    public static final String END_TEXT_OBJECT = "ET";
    public static final String SAVE_GRAPHICS_STATE = "q";
    public static final String RESTORE_GRAPHICS_STATE = "Q";
    public static final String INLINE_IMAGE_BEGIN = "BI";
    public static final String IMAGE_DATA = "ID";
    public static final String INLINE_IMAGE_END = "EI";
    public static final String BEGIN_MARKED_CONTENT1 = "BMC";
    public static final String BEGIN_MARKED_CONTENT2 = "BDC";
    public static final String END_MARKED_CONTENT = "EMC";

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
    private final StreamPaneView view;
    private final Stream stream;
    private ToolTipController tTController;
    private PDResources resources;
    private final boolean isContentStream;

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
        this.isContentStream = isContentStream;

        this.stream = new Stream(cosStream, isThumb);
        if (resourcesDic != null)
        {
            resources = new PDResources(resourcesDic);
            tTController = new ToolTipController(resources);
        }

        panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 500));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        view = new StreamPaneView();
        hexView = new HexView();

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
        tabbedPane.add("Text view", view.getStreamPanel());
        tabbedPane.add("Hex view", hexView.getPane());

        panel.add(tabbedPane);
    }

    public JComponent getPanel()
    {
        return panel;
    }

    private JPanel createHeaderPanel(List<String> availableFilters, String i, ActionListener actionListener)
    {
        JComboBox filters = new JComboBox(availableFilters.toArray());
        filters.setSelectedItem(i);
        filters.addActionListener(actionListener);

        JPanel headerPanel = new JPanel(new FlowLayout());
        headerPanel.add(filters);

        return headerPanel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        if (actionEvent.getActionCommand().equals("comboBoxChanged"))
        {
            JComboBox comboBox = (JComboBox) actionEvent.getSource();
            String currentFilter = (String) comboBox.getSelectedItem();

            try
            {
                if (currentFilter.equals(Stream.IMAGE))
                {
                    requestImageShowing();
                    return;
                }
                requestStreamText(currentFilter);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void requestImageShowing() throws IOException
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
            view.showStreamImage(image);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            baos.flush();
            byte[] bytes = baos.toByteArray();
            baos.close();
            hexView.changeData(bytes);
        }
    }

    private void requestStreamText(String command) throws IOException
    {
        new DocumentCreator(command).execute();
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
        private final String filterKey;
        private int indent;
        private boolean needIndent;

        private DocumentCreator(String filterKey)
        {
            this.filterKey = filterKey;
        }

        @Override
        protected StyledDocument doInBackground()
        {
            // default encoding to use when reading text base content
            String encoding = "ISO-8859-1";
            synchronized (stream)
            {
                if (stream.isXmlMetadata()) {
                    encoding = "UTF-8";
                }
                InputStream inputStream = stream.getStream(filterKey);
                if (isContentStream && Stream.DECODED.equals(filterKey))
                {
                    StyledDocument document = getContentStreamDocument(inputStream);
                    if (document != null)
                    {
                        return document;
                    }
                    return getDocument(stream.getStream(filterKey), encoding);
                }
                return getDocument(inputStream, encoding);
            }
        }

        @Override
        protected void done()
        {
            try
            {
                view.showStreamText(get(), tTController);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
            }
        }

        private String getStringOfStream(InputStream ioStream, String encoding)
        {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int amountRead;
            try
            {
                while ((amountRead = ioStream.read(buffer, 0, buffer.length)) != -1)
                {
                    byteArray.write(buffer, 0, amountRead);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            try
            {
                return byteArray.toString(encoding);
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
                return null;
            }
        }

        private StyledDocument getDocument(InputStream inputStream, String encoding)
        {
            StyledDocument docu = new DefaultStyledDocument();
            if (inputStream != null)
            {
                String data = getStringOfStream(inputStream, encoding);
                try
                {
                    docu.insertString(0, data, null);
                }
                catch (BadLocationException e)
                {
                    e.printStackTrace();
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
                parser.parse();
            }
            catch (IOException e)
            {
                return null;
            }

            for (Object obj : parser.getTokens())
            {
                writeToken(obj, docu);
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
                e.printStackTrace();
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
                    if (chr < 0x20 || chr > 0x7e)
                    {
                        // non-printable ASCII is shown as an octal escape
                        String str = String.format("\\%03o", chr);
                        docu.insertString(docu.getLength(), str, ESCAPE_STYLE);
                    }
                    else if (chr == '(' || chr == ')' || chr == '\n' || chr == '\r' ||
                             chr == '\t' || chr == '\b' || chr == '\f' || chr == '\\')
                    {
                        // PDF reserved characters must be escaped
                        String str = "\\" + (char)chr;
                        docu.insertString(docu.getLength(), str, ESCAPE_STYLE);
                    }
                    else
                    {
                        String str = "" + (char)chr;
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

            if (op.getName().equals(END_TEXT_OBJECT)
                    || op.getName().equals(RESTORE_GRAPHICS_STATE)
                    || op.getName().equals(END_MARKED_CONTENT))
            {
                indent--;
            }
            writeIndent(docu);

            if (op.getName().equals(INLINE_IMAGE_BEGIN))
            {
                docu.insertString(docu.getLength(), INLINE_IMAGE_BEGIN + "\n", OPERATOR_STYLE);
                COSDictionary dic = op.getImageParameters();
                for (COSName key : dic.keySet())
                {
                    Object value = dic.getDictionaryObject(key);
                    docu.insertString(docu.getLength(), "/" + key.getName() + " ", null);
                    writeToken(value, docu);
                    docu.insertString(docu.getLength(), "\n", null);
                }
                String imageString = new String(op.getImageData(), Charsets.ISO_8859_1);
                docu.insertString(docu.getLength(), IMAGE_DATA + "\n", INLINE_IMAGE_STYLE);
                docu.insertString(docu.getLength(), imageString, null);
                docu.insertString(docu.getLength(), "\n", null);
                docu.insertString(docu.getLength(), INLINE_IMAGE_END + "\n", OPERATOR_STYLE);
            }
            else
            {
                String operator = ((Operator) obj).getName();
                docu.insertString(docu.getLength(), operator + "\n", OPERATOR_STYLE);

                // nested opening operators
                if (op.getName().equals(BEGIN_TEXT_OBJECT) ||
                    op.getName().equals(SAVE_GRAPHICS_STATE) ||
                    op.getName().equals(BEGIN_MARKED_CONTENT1) ||
                    op.getName().equals(BEGIN_MARKED_CONTENT2))
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
