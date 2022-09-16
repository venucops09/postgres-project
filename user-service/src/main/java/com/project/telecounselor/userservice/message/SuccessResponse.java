package com.project.telecounselor.userservice.message;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.telecounselor.common.Constants;
import com.project.telecounselor.common.MessageValidator;
import com.project.telecounselor.common.domain.Paged;
import com.project.telecounselor.common.response.SuccessMessage;
import com.project.telecounselor.userservice.message.SuccessCode;

import java.util.List;

/**
 * <p>
 * Generic success response
 * </p>
 *
 * @author Vigneshkumar created on Jun 30, 2022
 *
 * @param <T>
 */

public class SuccessResponse<T> extends ResponseEntity<Object> {

	public SuccessResponse(SuccessCode successCode, Paged<T> paged, HttpStatus responseCode) {
		this(MessageValidator.getInstance().getMessage(Integer.toString(successCode.getKey()), Constants.SUCCESS),
				paged, responseCode);
	}

	public SuccessResponse(String message, Paged<T> paged, HttpStatus responseCode) {
		this(message, null, paged.getList(), responseCode, (int) paged.getCount());
	}

	public SuccessResponse(SuccessCode successCode, List<T> entity, HttpStatus responseCode) {
		this(MessageValidator.getInstance().getMessage(Integer.toString(successCode.getKey()), Constants.SUCCESS), null,
				entity, responseCode, null);
	}

	public SuccessResponse(SuccessCode successCode, List<T> entity, int totalCount, HttpStatus responseCode) {
	    this(MessageValidator.getInstance().getMessage(Integer.toString(successCode.getKey()), Constants.SUCCESS), null,
		        entity, responseCode, totalCount);
	}

	public SuccessResponse(SuccessCode successCode, Object entity, int totalCount, HttpStatus responseCode) {
		this(MessageValidator.getInstance().getMessage(Integer.toString(successCode.getKey()), Constants.SUCCESS),
				entity, null, responseCode, totalCount);
	}

	public SuccessResponse(String message, List<T> entity, HttpStatus responseCode) {
		this(message, null, entity, responseCode, null);
	}

	public SuccessResponse(SuccessCode successCode, Object entity, HttpStatus responseCode) {
		this(MessageValidator.getInstance().getMessage(Integer.toString(successCode.getKey()), Constants.SUCCESS),
				entity, null, responseCode, null);
	}

	public SuccessResponse(String message, Object entity, HttpStatus responseCode) {
		this(message, entity, null, responseCode, null);
	}

	public SuccessResponse(SuccessCode successCode, HttpStatus responseCode) {
		this(MessageValidator.getInstance().getMessage(Integer.toString(successCode.getKey()), Constants.SUCCESS), null,
				null, responseCode, null);
	}

	public SuccessResponse(String message, Object entity, List<T> entityList, HttpStatus httpStatus,
			Integer totalCount) {
		super(new SuccessMessage<T>(Boolean.TRUE, message, entity, entityList, Integer.valueOf(httpStatus.value()),
				totalCount), httpStatus);
	}

	public SuccessResponse(SuccessCode successCode, List<T> entity, HttpStatus responseCode, Integer totalCount) {
		this(MessageValidator.getInstance().getMessage(Integer.toString(successCode.getKey()), Constants.SUCCESS), null,
				entity, responseCode, totalCount);
	}

}
