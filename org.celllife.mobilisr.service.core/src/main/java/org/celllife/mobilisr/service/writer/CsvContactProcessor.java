package org.celllife.mobilisr.service.writer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.celllife.mobilisr.api.validation.ValidationError;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.domain.Contact;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class CsvContactProcessor implements ItemProcessor<Contact, Contact> {

	private JobExecution jobExecution;
	private String filePath;
	private List<Contact> errorContactList;
	
	@Autowired
	private ValidatorFactory validatorFactory;
	
	@SuppressWarnings("unchecked")
	@Override
	public Contact process(Contact contact) throws Exception {
		ValidationError error = validatorFactory.validateMsisdn(contact.getMsisdn());
		
		if (error != null){
			ExecutionContext executionContext = jobExecution.getExecutionContext();
			if(!executionContext.containsKey(filePath)){
				executionContext.put(filePath, 1);
			}else{
				Integer numOfRecordsStored = (Integer) executionContext.get(filePath);
				numOfRecordsStored++;
				executionContext.put(filePath, numOfRecordsStored);				
			}
			if(!executionContext.containsKey("ERR" +  filePath)){
				errorContactList.add(contact);
				executionContext.put("ERR" + filePath, errorContactList);
			}else{
				errorContactList = (List<Contact>) executionContext.get("ERR" + filePath);
				errorContactList.add(contact);
				executionContext.put("ERR" + filePath, errorContactList);
			}
			
			//Returning null will not pass the object to the ItemWriter
			contact = null;
			
		}
		
		return contact;
	}

	@BeforeStep
	public void beforeStep(StepExecution stepExecution){
		
		//errorContactList = new ArrayList<Contact>();
		errorContactList = Collections.synchronizedList(new ArrayList<Contact>());
		jobExecution = stepExecution.getJobExecution();
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	
	
}
