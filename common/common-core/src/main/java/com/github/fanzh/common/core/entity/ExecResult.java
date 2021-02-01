package com.github.fanzh.common.core.entity;

import com.github.fanzh.common.core.constant.ApiMsg;
import lombok.Data;

import java.io.Serializable;

/**
 * 封装返回数据
 *
 * @author fanzh
 * @date 2019/3/17 12:08
 */
@Data
public class ExecResult<T> implements Serializable {

    public static final long serialVersionUID = 42L;

    private String msg = ApiMsg.msg(ApiMsg.KEY_SUCCESS);

    private int code = ApiMsg.KEY_SUCCESS;

    private T data;

    private boolean success;

    public ExecResult() {
        super();
    }

    public ExecResult(T data) {
        super();
        this.data = data;
    }

    public ExecResult(T data, int keyCode, int msgCode) {
        super();
        this.data = data;
        this.code = Integer.parseInt(keyCode + "" + msgCode);
        this.msg = ApiMsg.code2Msg(keyCode, msgCode);
    }

    public ExecResult(T data, String msg) {
        super();
        this.data = data;
        this.msg = msg;
    }

    /**
     * 是否成功
     * @return boolean
     */
    public boolean isSuccess() {
        return this.getCode() == ApiMsg.KEY_SUCCESS;
    }

    /**
     * 是否成功
     * @return boolean
     */
    public boolean isError() {
        return !isSuccess();
    }
}
