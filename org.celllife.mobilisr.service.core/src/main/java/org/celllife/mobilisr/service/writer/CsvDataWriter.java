package org.celllife.mobilisr.service.writer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.Organization;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterWrite;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("unchecked")
public class CsvDataWriter implements ItemWriter<Contact>{

	private String filePath;
	
	@Autowired
	private ContactDAO contactDAO;
	
	private static Map<String,CsvDataJobBean> organizationMap = new HashMap<String, CsvDataJobBean>();
	
	private JobExecution jobExecution;
	
	@Override
	public void write(List<? extends Contact> items) throws Exception {	
		
		CsvDataJobBean csvDataJobBean = organizationMap.get(filePath);
		Organization organization = csvDataJobBean.getOrganization();
		
		contactDAO.batchSaveContact(organization, (List<Contact>) items, csvDataJobBean.getListOfGroups());
	}
	
	@BeforeStep
	public void beforeStep(StepExecution stepExecution){
		//Since here we are the beginning of the step
		//We need to store a data (k,v) in a map that can indicate the progress
		//of the record storage that we can use in progress bar for displaying
		//number of records stored etc
		jobExecution = stepExecution.getJobExecution();
		ExecutionContext executionContext = jobExecution.getExecutionContext();
		if(!executionContext.containsKey(filePath)){
			executionContext.put(filePath, 0);
		}
	}

	@AfterWrite
	public void afterWrite(List<Contact> items){
		ExecutionContext executionContext = jobExecution.getExecutionContext();
		Integer numOfRecordsStored = (Integer) executionContext.get(filePath);
		numOfRecordsStored += items.size();
		executionContext.put(filePath, numOfRecordsStored);
	}

	public static void storeJobData(String fileNamePath, CsvDataJobBean csvDataJobBean){
			
		if(!organizationMap.containsKey(fileNamePath)){
			organizationMap.put(fileNamePath, csvDataJobBean);
		}
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
