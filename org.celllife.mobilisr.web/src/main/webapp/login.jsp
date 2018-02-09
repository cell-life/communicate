<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@page import="org.springframework.security.core.context.SecurityContextHolder"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-gb" lang="en-gb" dir="ltr">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	  
		<meta name="robots" content="noindex"/>
		<title>Communicate</title>
		<link href="favicon.ico" rel="shortcut icon" type="image/x-icon"/>
		
		<link href="login.css" rel="stylesheet" type="text/css"/>
		<link href="rounded.css" rel="stylesheet" type="text/css"/>
		
		<script language="javascript" type="text/javascript">
			function setFocus() {
				document.login.username.select();
				document.login.username.focus();
			}
		</script>
	</head>
	<body onload="javascript:setFocus()">
		<div id="content-box">
			<div class="padding">
				<div id="element-box" class="login">
					<div class="t"> 
						<div class="t"> 
							<div class="t"></div> 
						</div> 
					</div> 
					<div class="m">
						<h1 id="login_heading">Communicate Login</h1>
						
						<div id="section-box">
							<div class="t"> 
								<div class="t"> 
									<div class="t"></div> 
								</div> 
							</div> 
							<div class="m">
								<form action="j_spring_security_check" method="post" name="login" id="form-login" style="clear: both;">
									<p id="form-login-username">
										<label>Username</label>
										<input name="j_username" id="username" type="text" class="inputbox" size="15"/>
									</p>
									<p id="form-login-password">
										<label>Password</label>
										<input name="j_password" id="password" type="password" class="inputbox" size="15"/>
									</p>
									<div class="button_holder">
										<div class="button1">
											<div class="next">
												<a href="#" style="text-decoration: none;" onclick="login.submit();">Login</a>
											</div>	
										</div>
									</div>
									<div class="clr"></div>
									<input type="submit" style="border: 0; padding: 0; margin: 0; width: 0px; height: 0px;" value="Login"/>
									<div class="clr"></div>
								</form>
							</div>
							<div class="b"> 
								<div class="b"> 
									<div class="b"></div> 
								</div> 
							</div> 
						</div>
			
						<%
				    		String errorVal = request.getParameter("error");
							if(errorVal != null ){
								if(errorVal.equals("1") || errorVal.equals("2")){
									%> <p id="message" class="error">Please enter the correct username and password.</p> <%
								}else if( errorVal.equals("3")){
									%> <p id="message" class="error">A user with entered credentials is already logged in.</p> <%
								}else if( errorVal.equals("4")){
									%> <p id="message" class="success">Your password has been successfully reset. Check your email for your new password.</p> <%
								}else if( errorVal.equals("5")){
									%> <p id="message" class="error">You account has been disabled. Please contact Cell-Life support if you think this is an error.</p> <%
								}else{	
								}
							} else {
								%> <p id="message">Use a valid username and password to gain access to Communicate.</p> <%
							}
						%>
						
						<div class="clr"></div>	 
						<p id="forgot-password">
							<span><a href="passwordrecovery.jsp">Forgot password?</a></span>
						</p>
						<div class="clr"></div>
					</div>
					<div class="b"> 
						<div class="b"> 
							<div class="b"></div> 
						</div> 
					</div>
				</div>
	
			<noscript>Warning! JavaScript must be enabled for proper operation of Communicate.</noscript>
			<div class="clr"></div>
			</div>
		</div>
		<div id="footer">
			<p class="copyright">&copy; Copyright <a href="http://www.cell-life.org" target="_blank" title="Cell-Life" style="text-decoration: none;">Cell-life</a> 2011</p>
		</div>
	</body>
</html>