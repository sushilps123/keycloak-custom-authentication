package com.dehaat.spi.authentication;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.managers.AuthenticationManager;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sushil
 */
public class MobileForm extends UsernamePasswordForm {
	public MobileForm() {
	}

	protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
		return this.validateUser(context, formData);
	}


	public boolean validateUser(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
		context.clearUser();
		String mobile_num = inputData.getFirst(AuthenticationManager.FORM_USERNAME).trim();
		UserModel user = null;
		if (mobile_num.length() != 10) {
			challengeMessage(context, "Invalid Mobile Number","username");
		}else {
			Stream<UserModel> userStream = context.getSession().users().searchForUserByUserAttributeStream(context.getRealm(), "mobile_number", mobile_num);
			List<UserModel> usersList = userStream.collect(Collectors.toList());
			if (usersList.size() > 0) {
				user = usersList.get(0);
				context.setUser(user);
				context.success();
			}else{
				challengeMessage(context, "Invalid Mobile Number","username");
			}
		}
		return user!=null && user.isEnabled();
	}


	protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
		LoginFormsProvider forms = context.form();
		if (!formData.isEmpty()) {
			forms.setFormData(formData);
		}

		return forms.createLoginUsername();
	}

	protected Response createLoginForm(LoginFormsProvider form) {
		return form.createLoginUsername();
	}

	protected Response challengeMessage(AuthenticationFlowContext context, String error, String field) {

		LoginFormsProvider form = context.form().setExecution(context.getExecution().getId());
		if (error != null) {
			if (field != null) {
				form.addError(new FormMessage(field, error));
			} else {
				form.setError(error, new Object[0]);
			}
		}

		return form.createLoginUsername();
	}

}
