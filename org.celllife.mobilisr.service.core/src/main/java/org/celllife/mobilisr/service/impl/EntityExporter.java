package org.celllife.mobilisr.service.impl;

import java.util.Collection;

import liquibase.csv.opencsv.CSVWriter;

/**
 * Defines a functor interface implemented by classes that serves as the model
 * class for exporting data.
 * 
 * 
 * @author Simon Kelly
 * 
 * @param <T>
 *            the type of object to be transformed
 */
abstract class EntityExporter<T> {
	private Collection<T> entities;
	private Batcher<T> batcher;
	private String errorMessage;
	private String[] columnHeaders;

	/**
	 * This method should be overridden if implementors want to write content
	 * above the data (in a header section).
	 * 
	 * @param csvWriter
	 */
	public void writeHeading(CSVWriter csvWriter) {
	};

	/**
	 * This method is called for each object to be exported. The method should
	 * convert the object into an array of string which will be writted to the
	 * output file.
	 * 
	 * @param o
	 *            the object to transform
	 * @return a String[] to write to the output file
	 */
	public abstract String[] transform(T o);

	/**
	 * A batcher is used to allow batched export of data. The
	 * {@link Batcher#getBatch(int)} method will be called successively until no
	 * more data is returned.
	 * 
	 * @param batcher
	 */
	public void setBatcher(Batcher<T> batcher) {
		this.batcher = batcher;
	}

	/**
	 * Set the list of entity objects to export.
	 * 
	 * If this collection is set then it takes precedence over the batcher.
	 * 
	 * @param entities
	 */
	public void setEntities(Collection<T> entities) {
		this.entities = entities;
	}

	/**
	 * Returns a batch of objects to export.
	 * 
	 * @param batch
	 * @return 
	 */
	public Collection<T> getEntityBatch(int batch) {
		if (entities != null) {
			return batch == 0 ? entities : null;
		} else if (batcher != null) {
			return batcher.getBatch(batch);
		}
		return null;
	};

	/**
	 * An error message to write above the exported data.
	 * 
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * The column headers for the exported data.
	 * 
	 * @param columnHeaders
	 */
	public void setColumnHeaders(String[] columnHeaders) {
		this.columnHeaders = columnHeaders;
	}

	public String[] getColumnHeaders() {
		return columnHeaders;
	}
}