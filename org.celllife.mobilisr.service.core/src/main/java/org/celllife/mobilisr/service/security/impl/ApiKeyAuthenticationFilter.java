/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.celllife.mobilisr.service.security.impl;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;


/**
 * Processes a HTTP request's BASIC authorization headers, putting the result into the
 * <code>SecurityContextHolder</code>.
 *
 * <p>
 * If authentication is successful, the resulting {@link Authentication} object will be placed into the
 * <code>SecurityContextHolder</code>.
 *
 * <p>
 * If authentication fails an {@link AuthenticationEntryPoint} implementation is called. 
 * Usually this should be {@link BasicAuthenticationEntryPoint}, which will prompt the user to
 * authenticate again via BASIC authentication.
 *
 * @author Simon Kelly
 */
public class ApiKeyAuthenticationFilter extends GenericFilterBean {

    //~ Instance fields ================================================================================================

    private static final String APIKEY = "apikey";
	private AuthenticationDetailsSource authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private AuthenticationEntryPoint authenticationEntryPoint;
    private AuthenticationManager authenticationManager;

    //~ Methods ========================================================================================================

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.authenticationManager, "An AuthenticationManager is required");
        Assert.notNull(this.authenticationEntryPoint, "An AuthenticationEntryPoint is required");
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        final boolean debug = logger.isDebugEnabled();
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        String apikey = request.getParameter(APIKEY);

        if (apikey != null) {
            if (debug) {
                logger.debug("ApiKey Authentication Authorization parameter found: '" + apikey + "'");
            }

            if (authenticationIsRequired()) {
                ApiKeyAuthenticationToken authRequest =
                        new ApiKeyAuthenticationToken(apikey);
                authRequest.setDetails(authenticationDetailsSource.buildDetails(request));

                Authentication authResult;

                try {
                    authResult = authenticationManager.authenticate(authRequest);
                } catch (AuthenticationException failed) {
                    // Authentication failed
                    if (debug) {
                        logger.debug("Authentication request for key: " + apikey + " failed: " + failed.toString());
                    }

                    SecurityContextHolder.getContext().setAuthentication(null);

                    authenticationEntryPoint.commence(request, response, failed);

                    return;
                }

                // Authentication success
                if (debug) {
                    logger.debug("Authentication success: " + authResult.toString());
                }

                SecurityContextHolder.getContext().setAuthentication(authResult);

            }
        }

        chain.doFilter(request, response);
    }

    private boolean authenticationIsRequired() {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if(existingAuth == null || !existingAuth.isAuthenticated()) {
            return true;
        }

        return false;
    }

    protected AuthenticationEntryPoint getAuthenticationEntryPoint() {
        return authenticationEntryPoint;
    }

    public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    protected AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setAuthenticationDetailsSource(AuthenticationDetailsSource authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }
}
