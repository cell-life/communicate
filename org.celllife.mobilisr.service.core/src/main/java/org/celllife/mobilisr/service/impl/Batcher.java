package org.celllife.mobilisr.service.impl;

import java.util.Collection;

/**
 * This interface is used by the {@link EntityExporter} to get batches of objects
 * for export.
 * 
 * The <code>getBatch</code> method will be called with incremental values of <code>batch</code> until the value returned is null
 * or an empty Collection.
 * 
 * @author Simon Kelly
 *
 * @param <T>
 */
public interface Batcher<T> {
	public Collection<T> getBatch(int batch);
}