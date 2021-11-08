/**
 * 
 */
package org.apache.pdfbox.printing;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * @author Kevin
 *
 */
public class SimplifiedPdfBoxPdfPrinter {

    private final String printerName;

    /**
	 * 
	 */
	public SimplifiedPdfBoxPdfPrinter(String printerName) {
		this.printerName = printerName;
	}

    private static void forceFileExist(File f) throws IOException{
        new FileOutputStream(f).close();
    }
	
    private static PrintService getPrintService(String name){
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(name)){
                return service;
            }
        }
        return null;
    }
    
    
	public void print(File pdf) throws Exception {
        if (!pdf.exists())
            throw new IOException(pdf + " does not exist before printing - conversion to prn not possible");

        PrintService printService = getPrintService(printerName);
        
		PrinterJob job = PrinterJob.getPrinterJob();
	    job.setPrintService(printService);
	    
	    try(PDDocument doc = Loader.loadPDF(pdf, MemoryUsageSetting.setupMixed(5000000L))){
	    	PDFPageable pageable = new PDFPageable(doc);
	    	
	    	job.setPageable(pageable);
	
		    // create a new HashPrintRequestAttributeSet and initialize it with the printer's default attributes
		    HashPrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

	        File tempPrn = File.createTempFile("tempPrn", "");
	        
		    // set the output file as a destination 
		    attributes.add(new javax.print.attribute.standard.Destination(tempPrn.toURI()));

	    	
		    // print with the attributes
		    job.print(attributes);
	    }			    
		    
	}


	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		
		SimplifiedPdfBoxPdfPrinter svc = new SimplifiedPdfBoxPdfPrinter("\\\\Moby\\Cust Suc Dell C2660dn");
		svc.print(new File("S:\\ClientData\\ClientData\\XPRIA-TPT100892\\JrachvUniverse_H21.pdf"));

		//svc.print(new File("C:\\Users\\kevin\\Downloads\\gs-bugzilla692158-schleuse-veryslow.pdf"));
		
		System.out.println("Elapsed: " + (System.currentTimeMillis() - start)/1000L + " secs");
	}
}
