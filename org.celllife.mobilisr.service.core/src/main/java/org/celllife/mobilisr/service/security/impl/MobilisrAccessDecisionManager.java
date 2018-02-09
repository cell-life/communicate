package org.celllife.mobilisr.service.security.impl;

import java.util.Collection;

import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.exception.MobilisrSecurityException;
import org.celllife.mobilisr.service.exception.MobilisrSessionExpiredException;
import org.celllife.mobilisr.service.security.MobilisrUserDetails;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Simon Kelly
 * @author Dagmar Timler
 */
public class MobilisrAccessDecisionManager extends AffirmativeBased {
	
	private org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
	
	@Override
	public void decide(Authentication auth, Object obj,
			Collection<ConfigAttribute> config) throws AccessDeniedException {
		try {
			super.decide(auth, obj, config);
		} catch (AccessDeniedException exception) {
		        if (isUserLoggedIn()) {
		        	//We handle a security exception after
		        	//confirming the User is still logged in
		        	//But lacks permission to accomplish operation.
		        	logAndFireSecurityException(exception);
		        } else {
		        	//We handle a session expiry after affirming
		        	//that the User is no longer in the Security Context.
		        	logAndFireSessionExpiredException(exception);
		        }
		}
	}

	/**
	 * Handles the {@link MobilisrSecurityException}.
	 * 
	 * @param exception <tt>Exception</tt> thrown.
	 * @throws MobilisrSecurityException With meaningful <tt>Message</tt> to the <tt>User.</tt>
	 */
	private void logAndFireSecurityException(Exception exception) throws MobilisrSecurityException {
		
		String exMsg = "Access to restricted operation is denied";
		
		// log the error on the server so it is not lost
		log.debug("Caught server side Access Denied exception, throwing new exception to the client '"+ 
				exception.getMessage() +"'", exception);
		
		//Re throw known exception to the User.
		throw new MobilisrSecurityException(exMsg, exception);
	}

	/**
	 * Handles the {@link MobilisrSessionExpiredException}.
	 * 
	 * @param exception <tt>Exception</tt> thrown.
	 * @throws MobilisrSessionExpiredException With meaningful <tt>Message</tt> to the <tt>User.</tt>
	 */
	private void logAndFireSessionExpiredException(Exception exception) throws MobilisrSessionExpiredException {
		
		
		String exMsg = "Your session has expired. Re-Login to proceed.";
		
		// log the error on the server so it is not lost
		log.debug("Caught server side Session Expired exception, throwing new exception to the client '"+ 
				exception.getMessage()+"'", exception);
		
		//Re throw know exception to the User.
		throw new MobilisrSessionExpiredException(exMsg, exception);
	}
	
	/**
	 * Checks if the <code>User</code> is still 
	 * registered in the <code>Spring security context.</code>
	 * 
	 * @return 
	 * 		<code>true</code> 
	 * <code>if(auth instanceof MobilisrUserDetails)</code>
	 * <p>
	 * else
	 * 	<code>false</code>
	 * </p>
	 */
	private boolean isUserLoggedIn() {
		
		// see if the authentication object is intact.
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
        	
        	// Get the User's authentication if it is still intact.
            Authentication auth = context.getAuthentication();            
            if (auth != null) {
            	
            	// Check if the security context is still intact.
                if (auth.getPrincipal() instanceof MobilisrUserDetails
                		|| auth.getPrincipal() instanceof User) {
                    return true;
                }
            }
        }
     
        // Return false if Security Context has been invalidated.
        return false;
	}
}