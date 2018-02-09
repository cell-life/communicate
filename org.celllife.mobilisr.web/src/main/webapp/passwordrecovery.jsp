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
						<h1>Communicate Password Reset</h1>
						
						<div id="section-box">
							<div class="t"> 
								<div class="t"> 
									<div class="t"></div> 
								</div> 
							</div> 
							<div class="m">
								<form action="/mobilisr/recoverPassword" method="post" name="login" id="form-login" style="clear: both;">
									<p id="form-login-email">
										<label>Email address</label>
										<input name="j_email" id="email" type="text" class="inputbox" size="15"/>
									</p>
									<div class="button_holder">
										<div class="button1">
											<div class="next">
												<a href="#" style="text-decoration: none;" onclick="login.submit();">Reset Password</a>
											</div>	
										</div>
									</div>
									<div class="clr"></div>
									<input type="submit" style="border: 0; padding: 0; margin: 0; width: 0px; height: 0px;" value="Reset"/>
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
								if(errorVal.equals("1")){
									%> <p class="error">Please enter a valid email address.</p> <%
								} else if(errorVal.equals("2")){
									%> <p class="error">No user with that email address could be found.</p> <%
								}
							} else {
								%> <p>Please enter your email address.</p> <%
							}
						%>
						
						<div class="clr"></div>	 
						<p id="forgot-password">
							<span><a href="login.jsp">Return to login screen</a></span>
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