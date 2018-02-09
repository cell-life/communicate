package org.celllife.mobilisr.api.validation;

import org.celllife.mobilisr.constants.ErrorCode;

import java.util.List;


public class MsisdnValidator {

	private List<? extends MsisdnRule> rules;
	private String ruleList;

	public MsisdnValidator(List<? extends MsisdnRule> rules) {
		this.rules = rules;
	}

	/**
	 * Validates an MSISDN against the list of NumberInfo objects
	 * 
	 * @param value
	 *            the string to validate
	 * @return null if value is a valid msisdn or an instance of
	 *         {@link ValidationError}
	 */
	public ValidationError validate(String value) {
		for (MsisdnRule info : rules) {
			String prefix = info.getPrefix();
			
			if (value == null || value.isEmpty()){
				return new ValidationError(ErrorCode.INVALID_MSISDN,
						"Phone Number format not valid for "
								+ info.getName());
			}
			
			if (value.startsWith(prefix)) {
				if (value.matches(info.getValidator())) {
					return null;
				} else {
					return new ValidationError(ErrorCode.INVALID_MSISDN,
							"Phone Number format not valid for "
									+ info.getName());
				}
			}
		}

		return new ValidationError(ErrorCode.INVALID_MSISDN, "Unknown number prefix");
	}

	public void setRules(List<? extends MsisdnRule> list) {
		this.rules = list;
	}
	
	public List<? extends MsisdnRule> getRules() {
		return rules;
	}
	
	public String getRuleList(){
		if (ruleList == null){
			StringBuilder sb = new StringBuilder();
			for (MsisdnRule rule : rules) {
				sb.append(rule.getPrefix()).append(", ");
			}

			ruleList = sb.substring(0, sb.length()-2);
		}
		return ruleList;
	}
}
