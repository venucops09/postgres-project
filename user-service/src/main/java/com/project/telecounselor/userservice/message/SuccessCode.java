package com.project.telecounselor.userservice.message;

/**
 * <p>
 * Success code to fetch message from property. Property
 * file(application.property) present in resource folder.
 * </p>
 * 
 * @author Vigneshkumar created on Jun 30, 2022
 *
 */
public enum SuccessCode {

	USER_SAVE(1001),
	USER_UPDATE(1002),
	GET_USERS(1003),
	GET_USER(1004),
	GET_COUNTRY(2004),
	GET_TIMEZONE(3004),
	USER_DELETE(1005),
	GET_USER_META_DATA(1006),
	SEND_EMAIL_USING_SMTP(16004),
	PASSWORD_UPDATED(19001),
	PASSWORD_DOES_NOT_MATCH(19002),
	DISABLED_ACCOUNT(19003),
	GET_ALL_TIMEZONE(1007),
	GET_ALL_COUNTRY(1008),
	FETCH_ERROR(2006),
	ROLE_SAVE(5001),
	ROLE_UPDATE(5002),
	GET_ROLES(5003),
	GET_ROLE(5004),
	ROLE_DELETE(5005);

	private int key;

	SuccessCode(int key) {
		this.key = key;
	}

	public int getKey() {
		return this.key;
	}
}
