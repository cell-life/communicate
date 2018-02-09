package org.celllife.mobilisr.servletFilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.celllife.mobilisr.service.security.MobilisrUserDetails;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.helpers.MDCInsertingServletFilter;

/**
 * A servlet filter that inserts various values retrieved from the incoming http
 * request into the MDC.
 * 
 * <p>
 * The values are removed after the request is processed.
 * 
 * @author Simon Kelly
 * @See {@link MDCInsertingServletFilter}
 */
public class MobilisrMDCServletFilter implements Filter {

	private static final String USER_KEY = "user";

	public void destroy() {
		// do nothing
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		insertIntoMDC(request);

		try {
			chain.doFilter(request, response);
		} finally {
			clearMDC();
		}
	}

	void insertIntoMDC(ServletRequest request) {
		MDC.put(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY,
				request.getRemoteHost());

		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			MDC.put(ClassicConstants.REQUEST_REQUEST_URI,
					httpServletRequest.getRequestURI());
			StringBuffer requestURL = httpServletRequest.getRequestURL();
			if (requestURL != null) {
				MDC.put(ClassicConstants.REQUEST_REQUEST_URL,
						requestURL.toString());
			}
			MDC.put(ClassicConstants.REQUEST_QUERY_STRING,
					httpServletRequest.getQueryString());
			MDC.put(ClassicConstants.REQUEST_USER_AGENT_MDC_KEY,
					httpServletRequest.getHeader("User-Agent"));
			MDC.put(ClassicConstants.REQUEST_X_FORWARDED_FOR,
					httpServletRequest.getHeader("X-Forwarded-For"));

			SecurityContext context = SecurityContextHolder.getContext();
			if (context != null) {
				Authentication authentication = context.getAuthentication();
				if (authentication != null){
					Object principal = authentication.getPrincipal();
					if (principal != null){
						if (principal instanceof MobilisrUserDetails){
							MobilisrUserDetails user = (MobilisrUserDetails) principal;
							String username = user.getUsername();
							if (username != null && username.trim().length() > 0) {
								MDC.put(USER_KEY, username);
							}
						} else if (principal instanceof String){
							MDC.put(USER_KEY, (String) principal);
						}
					}
				}
			}
		}

	}

	void clearMDC() {
		MDC.remove(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY);
		MDC.remove(ClassicConstants.REQUEST_REQUEST_URI);
		MDC.remove(ClassicConstants.REQUEST_QUERY_STRING);
		// removing possibly inexistent item is OK
		MDC.remove(ClassicConstants.REQUEST_REQUEST_URL);
		MDC.remove(ClassicConstants.REQUEST_USER_AGENT_MDC_KEY);
		MDC.remove(ClassicConstants.REQUEST_X_FORWARDED_FOR);
		MDC.remove(USER_KEY);
	}

	public void init(FilterConfig arg0) throws ServletException {
		// do nothing
	}
}