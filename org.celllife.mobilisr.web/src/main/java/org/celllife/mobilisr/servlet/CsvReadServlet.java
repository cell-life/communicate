package org.celllife.mobilisr.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

public class CsvReadServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2727457482724670302L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		CommonsMultipartResolver multipartResover = new CommonsMultipartResolver();
		MultipartHttpServletRequest multipartHttpServletRequest = multipartResover.resolveMultipart(req);
		MultipartFile multipartFile = multipartHttpServletRequest.getFile("csvFile");
		InputStream inputStream = multipartFile.getInputStream();
		LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(inputStream));
		lineNumberReader.setLineNumber(1);
		
		File createTempFile = File.createTempFile("mobilisr", "contact_import");
		FileOutputStream fileOutputStream = new FileOutputStream(createTempFile);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
		IOUtils.copy(lineNumberReader, outputStreamWriter);
		IOUtils.closeQuietly(outputStreamWriter);
		IOUtils.closeQuietly(lineNumberReader);
		IOUtils.closeQuietly(inputStream);
		int numberOfLines = lineNumberReader.getLineNumber();
		
		HttpSession session = req.getSession(false);
		session.setAttribute("CSV_FILE_PATH", createTempFile.getAbsolutePath());
		
		resp.setHeader("Pragma", "no-cache");
		resp.addHeader("Cache-Control", "no-cache");
		resp.addHeader("Cache-Control", "no-store");
		resp.setContentType("text/html");
		resp.getWriter().write(createTempFile.getAbsolutePath() + ";" + numberOfLines);
	}

	
}
