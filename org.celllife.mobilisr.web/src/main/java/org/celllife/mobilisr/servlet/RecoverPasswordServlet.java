package org.celllife.mobilisr.servlet;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Controller("recoverPasswordController")
@RequestMapping("/recoverpassword")
public class RecoverPasswordServlet extends HttpServlet {

	private static final String email_regex = "^([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}$";

	@Autowired
	private UserService crudUserService;

	@Autowired
	private MailService mailService;

	private WebApplicationContext springContext;

	private static final long serialVersionUID = -2773033834622556881L;

	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		crudUserService = (UserService) getBean("crudUserService");
		mailService = (MailService) getBean("mailService");

		response.setContentType("text/html;charset=UTF-8");
		String email = request.getParameter("j_email");
		if (email == null
				|| email.isEmpty()
				|| !Pattern.matches(email_regex,email)) {
			response.sendRedirect(request.getSession().getServletContext().getContextPath()+ "/passwordrecovery.jsp?error=1");
		} else {
			User user = getUserByEmail(email);
			if (user != null) {
				resetPasswordAndEmailUser(user);
				response.sendRedirect(request.getSession().getServletContext().getContextPath() + "/login.jsp?error=4");
			} else {
				response.sendRedirect(request.getSession().getServletContext().getContextPath()+ "/passwordrecovery.jsp?error=2");
			}
		}
	}

	void resetPasswordAndEmailUser(User user) {
		String newPassword = crudUserService.resetPassword(user);
		mailService.sendResetPasswordEmail(user, newPassword);
	}

	public User getUserByEmail(
			@RequestParam(value = "email", required = true) String email) {
		List<User> users = crudUserService.findUsersByEmail(email);
		if (users.size() > 0)
			return users.get(0);
		return null;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private WebApplicationContext getApplicationContext() {
		if (springContext == null) {
			springContext = WebApplicationContextUtils
					.getWebApplicationContext(getServletContext());
		}
		return springContext;
	}

	protected Object getBean(String beanName) {
		return getApplicationContext().getBean(beanName);
	}
}
