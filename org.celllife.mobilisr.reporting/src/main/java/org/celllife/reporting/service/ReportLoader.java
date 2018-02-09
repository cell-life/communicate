package org.celllife.reporting.service;

import java.io.File;
import java.util.Collection;

import org.celllife.pconfig.model.Pconfig;
import org.celllife.reporting.util.JaxbUtil;

public interface ReportLoader {

	File getReportFile(Pconfig report, String sourceFolder);

	<T> Collection<T> loadReports(JaxbUtil jaxbUtil, String location, String suffix);

}
