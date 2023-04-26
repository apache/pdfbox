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
package org.apache.pdfbox.examples.printing;

import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.print.PrintServiceLookup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.contentstream.operator.MissingOperandException;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.contentstream.operator.graphics.GraphicsOperatorProcessor;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.MissingResourceException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;

/**
 * PDF documents with transparency groups are sometimes printed slowly and in poor quality, see
 * <a href="https://issues.apache.org/jira/browse/PDFBOX-4123">PDFBOX-4123</a>. If the transparency
 * groups aren't really needed (e.g. for most labels), we can use a custom PDFRenderer / PageDrawer
 * that uses a custom DrawObject class which doesn't call showTransparencyGroup() but only
 * showForm().
 * <p>
 * This OpaquePDFRenderer class object can be passed to the "long" constructor of
 * {@link PDFPrintable#PDFPrintable(org.apache.pdfbox.pdmodel.PDDocument, org.apache.pdfbox.printing.Scaling, boolean, float, boolean, org.apache.pdfbox.rendering.PDFRenderer)}.
 *
 * @author Tilman Hausherr
 */
public class OpaquePDFRenderer extends PDFRenderer
{

    public static void main(String[] args) throws IOException, PrinterException
    {
        // PDF from the QZ Tray project, who reported this problem.
        try (PDDocument doc = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(
                        new URL("https://github.com/qzind/tray/files/1749977/test.pdf")
                                .openStream())))
        {
            PDFRenderer renderer = new OpaquePDFRenderer(doc);
            Printable printable = new PDFPrintable(doc, Scaling.SCALE_TO_FIT, false, 0, true, renderer);
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(PrintServiceLookup.lookupDefaultPrintService());
            job.setPrintable(printable);
            if (job.printDialog())
            {
                job.print();
            }
        }
    }

    public OpaquePDFRenderer(PDDocument document)
    {
        super(document);
    }

    @Override
    protected PageDrawer createPageDrawer(PageDrawerParameters parameters) throws IOException
    {
        return new OpaquePageDrawer(parameters);
    }

    private static class OpaquePageDrawer extends PageDrawer
    {

        public OpaquePageDrawer(PageDrawerParameters parameters) throws IOException
        {
            super(parameters);
            addOperator(new OpaqueDrawObject(this));
        }
    }

    // copied from org.apache.pdfbox.contentstream.operator.graphics.DrawObject()
    // but doesn't call showTransparencyGroup
    private static class OpaqueDrawObject extends GraphicsOperatorProcessor
    {
        public OpaqueDrawObject(PDFGraphicsStreamEngine context)
        {
            super(context);
        }

        private static final Log LOG = LogFactory.getLog(OpaqueDrawObject.class);

        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException
        {
            if (operands.isEmpty())
            {
                throw new MissingOperandException(operator, operands);
            }
            COSBase base0 = operands.get(0);
            if (!(base0 instanceof COSName))
            {
                return;
            }
            COSName objectName = (COSName) base0;
            PDFGraphicsStreamEngine context = getGraphicsContext();
            PDXObject xobject = context.getResources().getXObject(objectName);

            if (xobject == null)
            {
                throw new MissingResourceException("Missing XObject: " + objectName.getName());
            }
            else if (xobject instanceof PDImageXObject)
            {
                PDImageXObject image = (PDImageXObject) xobject;
                context.drawImage(image);
            }
            else if (xobject instanceof PDFormXObject)
            {
                try
                {
                    context.increaseLevel();
                    if (context.getLevel() > 50)
                    {
                        LOG.error("recursion is too deep, skipping form XObject");
                        return;
                    }
                    context.showForm((PDFormXObject) xobject);
                }
                finally
                {
                    context.decreaseLevel();
                }
            }
        }

        @Override
        public String getName()
        {
            return OperatorName.DRAW_OBJECT;
        }
    }
}
