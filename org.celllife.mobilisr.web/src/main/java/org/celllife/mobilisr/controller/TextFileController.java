package org.celllife.mobilisr.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.celllife.mobilisr.util.CommunicateHome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("textFileController")
@RequestMapping("/forDownload")
public class TextFileController {

	private static Logger log = LoggerFactory.getLogger(TextFileController.class);

	// see http://forum.springsource.org/showthread.php?87351-path-variable-mapping&p=293482#post293482
	@RequestMapping(method = RequestMethod.GET, value = "/{filename:.*}")
	public void downloadTextFile(HttpServletResponse response,
			@PathVariable("filename") String filename,
			@RequestParam(required = false, defaultValue = "false", value = "delete") boolean delete)
			throws IOException {
		
		log.debug("TextFile {} requested", filename);
		
		if (filename.contains("..") || filename.contains(File.separator)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		File downloadFolder = CommunicateHome.getDownloadsFolder();
		
		String path = downloadFolder.getAbsolutePath() + File.separator + filename;
		File file = new File(path);
		if (file == null || !file.exists()){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		response.setContentType ("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.setDateHeader("Expires", -1);
		response.setHeader("Pragma", "no-cache");
		response.addHeader("Cache-Control", "no-cache");
		response.addHeader("Cache-Control", "no-store");
		response.setHeader("Content-Disposition", "attachment;filename=" + filename);
		response.setContentLength((int) file.length()); 
		
		FileInputStream input = new FileInputStream(file);
		IOUtils.copy(input, response.getOutputStream());
		input.close();
		
		if (delete){
			FileUtils.deleteQuietly(file);
		}
	}

}
