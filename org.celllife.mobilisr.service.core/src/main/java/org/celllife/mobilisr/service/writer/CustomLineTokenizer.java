package org.celllife.mobilisr.service.writer;

import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.stereotype.Service;

@Service("customTokenizer")
public class CustomLineTokenizer extends DelimitedLineTokenizer {

	private String inputFile;

	private String fieldOrder;

	public String getFieldOrder() {
		return fieldOrder;
	}

	public void setFieldOrder(String fieldOrder) {
		this.fieldOrder = fieldOrder;
	}

	@Override
	public void setNames(String[] names) {
		super.setNames(names);
	}
	
	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
}
