package org.apache.pdfbox.examples;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.*;

public class LocalPdfServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("Server started at: http://localhost:" + port);

        server.createContext("/generate-pdf", new PdfGeneratorHandler());
        server.setExecutor(null);
        server.start();
    }

    static class PdfGeneratorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {  // Handle preflight request
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1); // No content for preflight
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
                return;
            }

            // Enable CORS for normal requests
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/pdf");

            // Read request body
            InputStream inputStream = exchange.getRequestBody();
            String requestBody = new String(inputStream.readAllBytes());

            // Extract text and font from request
            String text = requestBody.replaceAll(".*\"text\":\"(.*?)\".*", "$1");
            String fontKey = requestBody.replaceAll(".*\"font\":\"(.*?)\".*", "$1");

            if (text.isEmpty()) {
                exchange.sendResponseHeaders(400, 0);
                exchange.getResponseBody().close();
                return;
            }

            // Get font path
            Map<String, String> fonts = getFontMap();
            String fontPath = fonts.getOrDefault(fontKey, fonts.get("kalimati"));  // Default to Kalimati

            File pdfFile = new File("generated.pdf");
            generatePDF(text, pdfFile, fontPath);

            byte[] pdfData = Files.readAllBytes(pdfFile.toPath());
            exchange.sendResponseHeaders(200, pdfData.length);
            exchange.getResponseBody().write(pdfData);
            exchange.getResponseBody().close();
        }
    }

    private static void generatePDF(String text, File pdfFile, String fontPath) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        PDType0Font pdfFont = PDType0Font.load(document, new File(fontPath));

        float startX = 50, startY = 700, fontSize = 14, leading = 1.5f * fontSize;

        contentStream.beginText();
        contentStream.setFont(pdfFont, fontSize);
        contentStream.newLineAtOffset(startX, startY);

        List<String> wrappedText = wrapText(text, pdfFont, fontSize, page.getMediaBox().getWidth() - 2 * startX);
        for (String line : wrappedText) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -leading);
        }

        contentStream.endText();
        contentStream.close();
        document.save(pdfFile);
        document.close();
    }

    private static Map<String, String> getFontMap() {
        Map<String, String> fontMap = new LinkedHashMap<>();
        fontMap.put("noto", "examples/src/main/resources/org/apache/pdfbox/resources/ttf/NotoSansDevanagariRegular.ttf");
        fontMap.put("noto_the_group", "examples/src/main/resources/org/apache/pdfbox/resources/ttf/NotoTheGroup.ttf");
        fontMap.put("kokila", "examples/src/main/resources/org/apache/pdfbox/resources/ttf/Kokila.ttf");
        fontMap.put("nirmala", "examples/src/main/resources/org/apache/pdfbox/resources/ttf/Nirmala.ttf");
        fontMap.put("nirmala_the_group", "examples/src/main/resources/org/apache/pdfbox/resources/ttf/NirmalaTheGroup.ttf");
        fontMap.put("mangal", "examples/src/main/resources/org/apache/pdfbox/resources/ttf/MangalRegular.ttf");
        fontMap.put("lohit", "examples/src/main/resources/org/apache/pdfbox/resources/ttf/LohitDevanagari.ttf");
        fontMap.put("tiro", "examples/src/main/resources/org/apache/pdfbox/resources/ttf/TiroDevanagariHindiRegular.ttf");
        fontMap.put("kalimati", "examples/src/main/resources/org/apache/pdfbox/resources/ttf/Kalimati.ttf");
        fontMap.put("kanjirowa", "examples/src/main/resources/org/apache/pdfbox/resources/ttf/Kanjirowa.ttf");
        return fontMap;
    }

    private static List<String> wrapText(String text, PDType0Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            if (textWidth > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append(currentLine.length() == 0 ? word : " " + word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}
