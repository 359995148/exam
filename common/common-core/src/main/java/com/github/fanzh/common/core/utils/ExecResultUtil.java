package com.github.fanzh.common.core.utils;

import com.github.fanzh.common.core.constant.ApiMsg;
import com.github.fanzh.common.core.entity.ExecResult;

/**
 * @author fanzh
 * @date 2019-10-08 12:03
 */
public class ExecResultUtil {

	private ExecResultUtil() {
	}

	public static <T> ExecResult<T> success() {
		ExecResult<T> er = new ExecResult();
		er.setCode(ApiMsg.KEY_SUCCESS);
		er.setMsg(ApiMsg.msg(ApiMsg.KEY_SUCCESS));
		er.setSuccess(true);
		return er;
	}

	public static <T> ExecResult<T> success(T data) {
		ExecResult<T> er = new ExecResult();
		er.setCode(ApiMsg.KEY_SUCCESS);
		er.setMsg(ApiMsg.msg(ApiMsg.KEY_SUCCESS));
		er.setData(data);
		er.setSuccess(true);
		return er;
	}

	public static <T> ExecResult<T> success(T data, String msg) {
		ExecResult<T> er = new ExecResult();
		er.setCode(ApiMsg.KEY_SUCCESS);
		er.setMsg(msg);
		er.setData(data);
		er.setSuccess(true);
		return er;
	}

	public static <T> ExecResult<T> error(Integer code, String msg) {
		ExecResult<T> result = new ExecResult();
		result.setCode(code);
		result.setMsg(msg);
		result.setSuccess(false);
		return result;
	}

	public static <T> ExecResult<T> error(Integer code, String msg, T data) {
		ExecResult<T> result = new ExecResult();
		result.setCode(code);
		result.setMsg(msg);
		result.setData(data);
		result.setSuccess(false);
		return result;
	}

	public static <T> ExecResult<T> error() {
		ExecResult<T> result = new ExecResult();
		result.setCode(ApiMsg.KEY_ERROR);
		result.setMsg(ApiMsg.msg(ApiMsg.KEY_ERROR));
		result.setSuccess(false);
		return result;
	}
}
