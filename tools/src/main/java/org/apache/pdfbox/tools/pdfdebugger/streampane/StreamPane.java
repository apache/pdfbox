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

package org.apache.pdfbox.tools.pdfdebugger.streampane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.tools.pdfdebugger.streampane.tooltip.ToolTipController;

/**
 * @author Khyrul Bashar
 * 
 * A class that shows the COSStream.
 */
public class StreamPane implements ActionListener
{
    private final StreamPaneView view;
    private final Stream stream;
    private ToolTipController tTController;
    private PDResources resources;
    private final boolean isContentStream;

    /**
     * Constructor.
     *
     * @param cosStream       COSStream instance.
     * @param isContentStream boolean instance. This says if a stream is content stream or not.
     * @param isThumb boolean instance. This says if a stream is an thumbnail image or not.
     * @param resourcesDic    COSDictionary instance that holds the resource dictionary for the stream.
     */
    public StreamPane(COSStream cosStream, boolean isContentStream, boolean isThumb, COSDictionary resourcesDic)
    {
        this.isContentStream = isContentStream;

        this.stream = new Stream(cosStream, isThumb);
        if (resourcesDic != null)
        {
            resources = new PDResources(resourcesDic);
            tTController = new ToolTipController(resources);
        }

        if (stream.isImage())
        {
            view = new StreamPaneView(stream.getFilterList(), Stream.IMAGE, this);
            requestImageShowing();
        }
        else
        {
            view = new StreamPaneView(stream.getFilterList(), Stream.UNFILTERED, this);
            requestStreamText(Stream.UNFILTERED);
        }
    }

    public JPanel getPanel()
    {
        return view.getStreamPanel();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        if (actionEvent.getActionCommand().equals("comboBoxChanged"))
        {
            JComboBox comboBox = (JComboBox) actionEvent.getSource();
            String currentFilter = (String) comboBox.getSelectedItem();

            if (currentFilter.equals(Stream.IMAGE))
            {
                requestImageShowing();
                return;
            }
            requestStreamText(currentFilter);
        }
    }

    private void requestImageShowing()
    {
        if (stream.isImage())
        {
            view.showStreamImage(stream.getImage(resources));
        }
    }

    private void requestStreamText(String command)
    {
        new DocumentCreator(command).execute();
    }

    /**
     * A SwingWorker extended class that convert the stream to text loads in a document.
     */
    private final class DocumentCreator extends SwingWorker<StyledDocument, Integer>
    {

        private final String filterKey;

        private DocumentCreator(String filterKey)
        {
            this.filterKey = filterKey;
        }

        @Override
        protected StyledDocument doInBackground()
        {
            InputStream inputStream = stream.getStream(filterKey);
            if (isContentStream && Stream.UNFILTERED.equals(filterKey))
            {
                StyledDocument document = getContentStreamDocument(inputStream);
                if (document != null)
                {
                    return document;
                }
                return getDocument(stream.getStream(filterKey));
            }
            return getDocument(inputStream);
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

        private String getStringOfStream(InputStream ioStream)
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
                return byteArray.toString("ISO-8859-1");
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
                return null;
            }
        }


        private StyledDocument getDocument(InputStream inputStream)
        {
            String data = getStringOfStream(inputStream);
            StyledDocument docu = new DefaultStyledDocument();
            try
            {
                docu.insertString(0, data, null);
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
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
                writeObject(obj, docu);
            }
            return docu;
        }

        private void writeObject(Object obj, StyledDocument docu)
        {
            try
            {
                if (obj instanceof Operator)
                {
                    addOperators(obj, docu);
                }
                else
                {
                    String str;
                    if (obj instanceof COSName)
                    {
                        str = "/" + ((COSName) obj).getName();
                    }
                    else if (obj instanceof COSBoolean)
                    {
                        str = obj.toString();
                    }
                    else if (obj instanceof COSArray)
                    {
                        StringBuilder builder = new StringBuilder("[ ");
                        for (COSBase base : (COSArray) obj)
                        {
                            builder.append(getCOSValue(base));
                            builder.append(", ");
                        }
                        if (((COSArray) obj).size() > 0)
                        {
                            builder.delete(builder.lastIndexOf(","), builder.length());
                        }
                        builder.append("]");
                        str = builder.toString();
                    }
                    else
                    {
                        str = getCOSValue(obj);
                    }
                    docu.insertString(docu.getLength(), str + " ", null);
                }
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
            }
        }

        private void addOperators(Object obj, StyledDocument docu)
        {
            Operator op = (Operator) obj;
            try
            {
                if (op.getName().equals("BI"))
                {
                    docu.insertString(docu.getLength(), OperatorMarker.INLINE_IMAGE_BEGIN + "\n",
                            OperatorMarker.getStyle(OperatorMarker.INLINE_IMAGE_BEGIN));
                    COSDictionary dic = op.getImageParameters();
                    for (COSName key : dic.keySet())
                    {
                        Object value = dic.getDictionaryObject(key);
                        docu.insertString(docu.getLength(), "/" + key.getName() + " ", null);
                        writeObject(value, docu);
                        docu.insertString(docu.getLength(), "\n", null);
                    }
                    docu.insertString(docu.getLength(), OperatorMarker.IMAGE_DATA + "\n",
                            OperatorMarker.getStyle(OperatorMarker.IMAGE_DATA));
                    docu.insertString(docu.getLength(), new String(op.getImageData(), "ISO-8859-1"), null);
                    docu.insertString(docu.getLength(), "\n", null);
                    docu.insertString(docu.getLength(), OperatorMarker.INLINE_IMAGE_END + "\n",
                            OperatorMarker.getStyle(OperatorMarker.INLINE_IMAGE_END));
                }
                else
                {
                    String operator = ((Operator) obj).getName();
                    docu.insertString(docu.getLength(), operator + "\n", OperatorMarker.getStyle(operator));
                }
            }
            catch (BadLocationException ex)
            {
                ex.printStackTrace();
            }
            catch (UnsupportedEncodingException ex)
            {
                ex.printStackTrace();
            }
        }

        private String getCOSValue(Object obj)
        {
            String str = obj.toString();
            str = str.substring(str.indexOf('{')+1, str.length()-1);
            if (obj instanceof COSString)
            {
                str = "(" + str + ")";
            }
            return str;
        }
    }
}
