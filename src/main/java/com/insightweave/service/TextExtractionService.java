package com.insightweave.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service for extracting text content from various document formats using Apache Tika.
 * Supports PDF, DOCX, TXT, and other common document formats.
 */
@Service
public class TextExtractionService {

  private static final Logger logger = LoggerFactory.getLogger(TextExtractionService.class);

  // Maximum characters to extract (100k chars to prevent memory issues)
  private static final int MAX_TEXT_LENGTH = 100_000;

  /**
   * Extracts text content from a file input stream.
   *
   * @param inputStream the file input stream
   * @param filename the original filename (used for logging)
   * @return extracted plain text, or empty string if extraction fails
   */
  public String extractText(InputStream inputStream, String filename) {
    try {
      // Create a handler with character limit
      BodyContentHandler handler = new BodyContentHandler(MAX_TEXT_LENGTH);

      // Create metadata object
      Metadata metadata = new Metadata();
      if (filename != null) {
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, filename);
      }

      // Create auto-detect parser
      AutoDetectParser parser = new AutoDetectParser();
      ParseContext context = new ParseContext();

      // Parse the document
      parser.parse(inputStream, handler, metadata, context);

      String extractedText = handler.toString().trim();

      logger.info("Successfully extracted {} characters from file: {}",
        extractedText.length(), filename);

      return extractedText;

    } catch (TikaException e) {
      logger.error("Tika parsing error for file {}: {}", filename, e.getMessage());
      return "";
    } catch (IOException e) {
      logger.error("IO error reading file {}: {}", filename, e.getMessage());
      return "";
    } catch (SAXException e) {
      logger.error("SAX parsing error for file {}: {}", filename, e.getMessage());
      return "";
    } catch (Exception e) {
      logger.error("Unexpected error extracting text from file {}: {}",
        filename, e.getMessage());
      return "";
    }
  }

  /**
   * Checks if a file type is supported for text extraction.
   *
   * @param contentType the MIME type of the file
   * @return true if the file type is supported
   */
  public boolean isTextExtractable(String contentType) {
    if (contentType == null) {
      return false;
    }

    // Common extractable types
    return contentType.startsWith("text/") ||
           contentType.equals("application/pdf") ||
           contentType.equals("application/msword") ||
           contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
           contentType.equals("application/vnd.ms-excel") ||
           contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
           contentType.equals("application/vnd.ms-powerpoint") ||
           contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
           contentType.equals("application/rtf");
  }
}
