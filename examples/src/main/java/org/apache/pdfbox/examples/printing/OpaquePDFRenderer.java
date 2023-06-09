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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.print.PrintServiceLookup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.MissingOperandException;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;
import org.apache.pdfbox.contentstream.operator.graphics.GraphicsOperatorProcessor;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.MissingResourceException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;

/**
 * PDF documents with transparency are sometimes printed slowly and in poor quality, see
 * <a href="https://issues.apache.org/jira/browse/PDFBOX-4123">PDFBOX-4123</a> and
 * <a href="https://issues.apache.org/jira/browse/PDFBOX-5605">PDFBOX-5605</a>. If the transparency
 * isn't really needed (e.g. for most labels), we can use a custom PDFRenderer / PageDrawer that
 * uses a custom DrawObject class which doesn't call showTransparencyGroup() but only showForm() and
 * sets ca and CA to 1.
 * <p>
 * This OpaquePDFRenderer class object can be passed to the "long" constructor of
 * {@link PDFPrintable#PDFPrintable(org.apache.pdfbox.pdmodel.PDDocument, org.apache.pdfbox.printing.Scaling, boolean, float, boolean, org.apache.pdfbox.rendering.PDFRenderer)}.
 *
 * @author Tilman Hausherr
 */
public class OpaquePDFRenderer extends PDFRenderer
{
    @SuppressWarnings("java:S1075")
    public static void main(String[] args) throws IOException, PrinterException, URISyntaxException
    {
        // PDF from the QZ Tray project, who reported this problem.
        // Also test with
        // https://github.com/qzind/tray/files/11432463/sample_file-1.pdf
        // (second page)
        try (PDDocument doc = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(
                        new URI("https://github.com/qzind/tray/files/1749977/test.pdf")
                                .toURL().openStream())))
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
            addOperator(new OpaqueSetGraphicsStateParameters(this));
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

    // copied from org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters()
    // but resets ca and CA
    private static class OpaqueSetGraphicsStateParameters extends OperatorProcessor
    {
        private static final Log LOG = LogFactory.getLog(OpaqueSetGraphicsStateParameters.class);

        public OpaqueSetGraphicsStateParameters(PDFStreamEngine context)
        {
            super(context);
        }

        @Override
        public void process(Operator operator, List<COSBase> arguments) throws IOException
        {
            if (arguments.isEmpty())
            {
                throw new MissingOperandException(operator, arguments);
            }
            COSBase base0 = arguments.get(0);
            if (!(base0 instanceof COSName))
            {
                return;
            }

            // set parameters from graphics state parameter dictionary
            COSName graphicsName = (COSName) base0;
            PDFStreamEngine context = getContext();
            PDExtendedGraphicsState gs = context.getResources().getExtGState(graphicsName);
            if (gs == null)
            {
                LOG.error("name for 'gs' operator not found in resources: /" + graphicsName.getName());
                return;
            }
            gs.setNonStrokingAlphaConstant(1f);
            gs.setStrokingAlphaConstant(1f);
            gs.copyIntoGraphicsState(context.getGraphicsState());
        }

        @Override
        public String getName()
        {
            return OperatorName.SET_GRAPHICS_STATE_PARAMS;
        }
    }
}
