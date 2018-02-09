package org.celllife.mobilisr.service.filter;

import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.service.exception.TriggerException;
import org.celllife.pconfig.model.Pconfig;

/**
 * Trigger classes match {@link SmsMo} messages to determine whether the
 * {@link Action} associated with this trigger should be executed.
 * 
 * @author Simon Kelly
 */
public interface Filter extends Configurable {

	/**
	 * Property used to order the filter types.
	 * 
	 * <ol>
	 * <li>Keyword
	 * <li>Regex
	 * <li>MatchAll
	 * </ol>
	 */
	public static final String RANK = "rank";

	/**
	 * @param message
	 * @return true if the message is matched by this trigger
	 * @throws TriggerException 
	 */
	public boolean matches(String smsMsg) throws TriggerException;
	
	/**
	 * @return a Pconfig containing the parameters required by the
	 * filter and also the RANK property
	 */
	public Pconfig getConfigDescriptor();
}
