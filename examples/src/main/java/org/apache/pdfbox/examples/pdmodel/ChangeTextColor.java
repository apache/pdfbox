package org.apache.pdfbox.examples.pdmodel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.contentstream.operator.Operator;
import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * This is an example that takes an existing PDF document and changes the text color
 *
 * I came up with this example while working on my own command-line tool
 *
 * @author Krish Vij
 */
public class ChangeTextColor {

    /**
     * Change the current Text color to a desired one by the use.
     *
     * @param Document The input PDF Document to change the text color of.
     * @param r,g,b    The color values for the new text to be put in the PDF.
     * @param outFile  The file to write to the pdf to.
     *
     * @throws IOException If there is an error writing the data.
     */
    
    	public void setTextColor(PDDocument document, String r, String g, String b, Path outFile) throws IOException {

		float	RED	  = Float.parseFloat(r)/ 255;
		float	GREEN = Float.parseFloat(g)/ 255;
		float	BLUE  = Float.parseFloat(b)/ 255;

		PDPageTree pages = document.getPages();
		

		for (PDPage page : pages) {

			//For every page we get the content stream
			//and a parser to parse the content stream
			Iterator<PDStream> streams = page.getContentStreams();
			
			PDFStreamParser parser = new PDFStreamParser(page);
			
			//A new list of streams to hold the modified content streams
			List<PDStream> newStreams =  new ArrayList<>();
			while (streams.hasNext()) {
                //We get all the tokens from the content stream
				List<Object> tokens = parser.parse();
				//A new list of tokens to add to the new content stream
				List<Object> newTokens =  new ArrayList<>();

				for (Object token : tokens) {

					if (token instanceof Operator) {

						Operator op = (Operator)token;
						String name = op.getName();
						//We check for the "BT" operator which marks the begining of a text content
						//and we add the new set of RGB values right before it
						if ("BT".equals(name)) {

							newTokens.add(new COSFloat(RED));
							newTokens.add(new COSFloat(GREEN));
							newTokens.add(new COSFloat(BLUE));
							newTokens.add(Operator.getOperator("rg"));
							newTokens.add(new COSFloat(RED));
							newTokens.add(new COSFloat(GREEN));
							newTokens.add(new COSFloat(BLUE));
							newTokens.add(Operator.getOperator("RG"));
						}
					}
					//We add all the old tokens to the new list
					newTokens.add(token);
				}

				//We create a new content stream to rewrite the modified tokens
				PDStream newStream = new PDStream(document);

				try (OutputStream out = newStream.createOutputStream()) {

					ContentStreamWriter writer = new ContentStreamWriter(out);
					writer.writeTokens(newTokens);
				}
                //We add the modified content stream to the list of new streams
				//as a page can have multiple content streams
				newStreams.add(newStream);
				streams.next();

			}
			//we set the modified content streams to the page
			page.setContents(newStreams);
		}
		//Finally we save the document with the changed text color
		document.save(outFile.toFile());
	}

    void main() {
        
		//Input PDF file to be changed
        File pdf = new File("someInputfile");
        //Loading the document
        try (PDDocument document = Loader.loadPDF(pdf) ) {
            //Calling the method to change the text color
            setTextColor(document, "255", "0", "0", Path.of("someOutputFile"));
        } catch (IOException e) {
            
            e.printStackTrace();
        }
    }
}
