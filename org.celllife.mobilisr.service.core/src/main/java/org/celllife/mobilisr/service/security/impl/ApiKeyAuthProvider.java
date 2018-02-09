package org.celllife.mobilisr.service.security.impl;

import org.celllife.mobilisr.service.security.MobilisrSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.util.Assert;

public class ApiKeyAuthProvider implements AuthenticationProvider {
	
	protected final Logger logger = LoggerFactory.getLogger(ApiKeyAuthProvider.class);

	private MobilisrSecurityService userDetailsService;

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	private UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();
	private UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();
	private UserCache userCache = new NullUserCache();

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		Assert.isInstanceOf(
				ApiKeyAuthenticationToken.class,
				authentication,
				messages.getMessage(
						"AbstractUserDetailsAuthenticationProvider.onlySupports",
						"Only ApiKeyAuthenticationToken is supported"));
		
		 // Determine username
        String apikey = (authentication.getCredentials() == null) ? "NONE_PROVIDED" : authentication.getCredentials().toString();

        boolean cacheWasUsed = true;
        UserDetails user = this.userCache.getUserFromCache(apikey);

        if (user == null) {
            cacheWasUsed = false;

            try {
                user = retrieveUser(apikey, (ApiKeyAuthenticationToken) authentication);
            } catch (UsernameNotFoundException notFound) {
                logger.debug("User with ApiKey '" + apikey + "' not found");

                throw new BadCredentialsException(messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
            }

            Assert.notNull(user, "retrieveUser returned null - a violation of the interface contract");
        }

        try {
            preAuthenticationChecks.check(user);
        } catch (AuthenticationException exception) {
            if (cacheWasUsed) {
                // There was a problem, so try again after checking
                // we're using latest data (i.e. not from the cache)
                cacheWasUsed = false;
                user = retrieveUser(apikey, (ApiKeyAuthenticationToken) authentication);
                preAuthenticationChecks.check(user);
            } else {
                throw exception;
            }
        }

        postAuthenticationChecks.check(user);

        if (!cacheWasUsed) {
            this.userCache.putUserInCache(user);
        }

        Object principalToReturn = user;

        return createSuccessAuthentication(principalToReturn, authentication, user);
	}

	private UserDetails retrieveUser(String apikey,	ApiKeyAuthenticationToken authentication) {
		UserDetails loadedUser;

        try {
            loadedUser = this.getUserDetailsService().loadUserByApiKey(apikey);
        }
        catch (DataAccessException repositoryProblem) {
            throw new AuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
        }

        if (loadedUser == null) {
            throw new AuthenticationServiceException(
                    "UserDetailsService returned null, which is an interface contract violation");
        }
        return loadedUser;
	}

	public final void afterPropertiesSet() throws Exception {
		Assert.notNull(this.userCache, "A user cache must be set");
		Assert.notNull(this.messages, "A message source must be set");
	}

	@Override
	public boolean supports(Class<? extends Object> authentication) {
		return (ApiKeyAuthenticationToken.class
				.isAssignableFrom(authentication));
	}

	/**
	 * Creates a successful {@link Authentication} object.
	 * <p>
	 * Protected so subclasses can override.
	 * </p>
	 * <p>
	 * Subclasses will usually store the original credentials the user supplied
	 * (not salted or encoded passwords) in the returned
	 * <code>Authentication</code> object.
	 * </p>
	 * 
	 * @param principal
	 *            that should be the principal in the returned object (defined
	 *            by the {@link #isForcePrincipalAsString()} method)
	 * @param authentication
	 *            that was presented to the provider for validation
	 * @param user
	 *            that was loaded by the implementation
	 * 
	 * @return the successful authentication token
	 */
	protected Authentication createSuccessAuthentication(Object principal,
			Authentication authentication, UserDetails user) {
		// Ensure we return the original credentials the user supplied,
		// so subsequent attempts are successful even with encoded passwords.
		// Also ensure we return the original getDetails(), so that future
		// authentication events after cache expiry contain the details
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
				principal, authentication.getCredentials(),
				user.getAuthorities());
		result.setDetails(authentication.getDetails());

		return result;
	}

	public void setUserDetailsService(MobilisrSecurityService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	protected MobilisrSecurityService getUserDetailsService() {
		return userDetailsService;
	}
	
	public void setUserCache(UserCache userCache) {
		this.userCache = userCache;
	}
	
	public UserCache getUserCache() {
		return userCache;
	}
	
	private class DefaultPreAuthenticationChecks implements UserDetailsChecker {
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                logger.debug("User account is locked");

                throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked",
                        "User account is locked"), user);
            }

            if (!user.isEnabled()) {
                logger.debug("User account is disabled");

                throw new DisabledException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled",
                        "User is disabled"), user);
            }

            if (!user.isAccountNonExpired()) {
                logger.debug("User account is expired");

                throw new AccountExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.expired",
                        "User account has expired"), user);
            }
        }
    }

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {
        public void check(UserDetails user) {
            if (!user.isCredentialsNonExpired()) {
                logger.debug("User account credentials have expired");

                throw new CredentialsExpiredException(messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.credentialsExpired",
                        "User credentials have expired"), user);
            }
        }
    }
}
