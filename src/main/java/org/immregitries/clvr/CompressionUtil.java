package org.immregitries.clvr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.immregitries.clvr.model.AbstractCLVRComponent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionUtil {


	/**
	 * Applies raw RFC1951 INFLATE decompression to a byte array.
	 * This method expects a raw DEFLATE-compressed byte array without zlib headers or footers.
	 *
	 * @param input  The compressed byte array.
	 * @param nowrap
	 * @return The decompressed byte array.
	 * @throws DataFormatException If the input data format is invalid.
	 */
	public static byte[] inflate(byte[] input, boolean nowrap) throws DataFormatException {
		// Create a new Inflater instance with the "nowrap" parameter set to true.
		// This indicates that the input is a raw DEFLATE stream, not a zlib stream.
		Inflater inflater = new Inflater(nowrap);
		inflater.setInput(input);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(input.length * 2); // Initial capacity for efficiency

		try (outputStream) {
			byte[] buffer = new byte[1024];
			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0,  count);
			}
		} catch (IOException e) {
			// Handle the exception appropriately
			e.printStackTrace();
		} finally {
			inflater.end(); // Deallocates native INFLATER resources.
		}

		return outputStream.toByteArray();
	}

	/**
	 * Applies raw RFC1951 DEFLATE compression to a byte array.
	 * This method does not include zlib or gzip headers/footers.
	 *
	 * @param input  The uncompressed byte array.
	 * @param nowrap
	 * @return The compressed byte array.
	 */
	public static byte[] deflate(byte[] input, boolean nowrap) {
		// Create a new Deflater instance with the desired compression level.
		// A level of 9 represents the best compression.
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, nowrap); // The `true` parameter signifies "nowrap" for raw DEFLATE.
		deflater.setInput(input);
		deflater.finish(); // Indicates that no more input data will be provided.

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(input.length);
		try (outputStream) {
			byte[] buffer = new byte[1024];

			while (!deflater.finished()) {
				int count = deflater.deflate(buffer);
				outputStream.write(buffer, 0, count);
			}
		} catch (IOException e) {
			// Handle the exception appropriately
			e.printStackTrace();
		} finally {
			deflater.end(); // Deallocates the native DEFLATER resources.
		}
		return outputStream.toByteArray();
	}

	public static BitMatrix qrCodeBitMatrix(String data, int width, int height) {
		try {
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			return qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
		} catch (WriterException e) {
			throw new RuntimeException("Error generating QR code Bit Matrix", e);
		}
	}
}
