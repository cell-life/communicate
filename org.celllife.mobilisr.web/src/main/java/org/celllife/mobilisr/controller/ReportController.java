package org.celllife.mobilisr.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.celllife.reporting.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller("reportController")
@RequestMapping("/reports")
public class ReportController {

	private static Logger log = LoggerFactory.getLogger(ReportController.class);

	@Autowired
	private ReportService reportService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	public void downloadReport(HttpServletResponse response,
			@PathVariable("id") String reportId) throws IOException {
		
		log.debug("Report {} requested", reportId);
		
		File reportFile = reportService.getGeneratedReportFile(reportId);
		if (reportFile == null || !reportFile.exists()){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		response.setContentType ("application/pdf");
		response.setContentLength((int) reportFile.length()); 
		IOUtils.copy(new FileInputStream(reportFile), response.getOutputStream());
	}

	

}
