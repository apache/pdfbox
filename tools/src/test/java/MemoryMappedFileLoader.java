import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.ExtractText;

public class MemoryMappedFileLoader
{

    public static void main(String[] args) throws IOException
    {
        String filename1 = "/home/lehmi/Documents/Specs+Docs/PDF_specs/PDF32000_2008.pdf";
        String filename2 = "/home/lehmi/workspace/pdfs/0.pdf";
        // extractTextNew(filename2);
        // System.out.println("================================================");
        // System.gc();
        extractTextNew(filename1);
        System.out.println("================================================");
        // System.gc();
        extractTextOld(filename1);
        System.out.println("================================================");
    }

    private static void extractTextOld(String filename) throws IOException
    {
        for (int i = 0; i < 5; i++)
        {
            ExtractText.main(new String[] { "-sort", "-debug", filename });
        }
    }

    private static void extractTextNew(String filename)
    {
        for (int i = 0; i < 5; i++)
        {
            long startTime = startProcessing("Loading PDF " + filename);
            try (PDDocument document = Loader.loadPDF(new File(filename)))
            {
                stopProcessing("Time for loading: ", startTime);
                PDFTextStripper pdfTextStripper = new PDFTextStripper();
                pdfTextStripper.setSortByPosition(true);
                pdfTextStripper.setShouldSeparateByBeads(true);
                String outputFile = new File(filename.substring(0, filename.length() - 4) + ".txt")
                        .getAbsolutePath();
                Writer output = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
                startTime = startProcessing("Starting text extraction");
                pdfTextStripper.writeText(document, output);
                output.flush();
                output.close();
                stopProcessing("Time for extraction: ", startTime);
            }
            catch (IOException exception)
            {

            }
            finally
            {
                System.out.println("Parsed " + (i + 1) + ". time");
            }
        }
    }

    private static long startProcessing(String message)
    {
        System.err.println(message);
        return System.currentTimeMillis();
    }

    private static void stopProcessing(String message, long startTime)
    {
        long stopTime = System.currentTimeMillis();
        float elapsedTime = ((float) (stopTime - startTime)) / 1000;
        System.err.println(message + elapsedTime + " seconds");
    }

}
