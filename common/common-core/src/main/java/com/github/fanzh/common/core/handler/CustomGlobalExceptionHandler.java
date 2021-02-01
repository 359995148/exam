package com.github.fanzh.common.core.handler;

import com.github.fanzh.common.core.constant.ApiMsg;
import com.github.fanzh.common.core.exceptions.CommonException;
import com.github.fanzh.common.core.utils.JsonMapper;
import com.github.fanzh.common.core.entity.ExecResult;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 *
 * @author fanzh
 * @date 2019/05/25 15:36
 */
@RestControllerAdvice
public class CustomGlobalExceptionHandler {

    /**
     * 处理参数校验异常
     *
     * @param ex      ex
     * @param headers headers
     * @param status  status
     * @return ResponseEntity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> validationBodyException(MethodArgumentNotValidException ex,
                                                          HttpHeaders headers,
                                                          HttpStatus status) {
        // 获取所有异常信息
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        ExecResult<List<String>> execResult = new ExecResult<>(errors, ApiMsg.KEY_SERVICE, ApiMsg.ERROR);
        return new ResponseEntity<>(execResult, headers, status);
    }

    /**
     * 参数类型转换错误
     *
     * @param exception 错误
     * @return 错误信息 
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ExecResult<String>> parameterTypeException(HttpMessageConversionException exception) {
        ExecResult<String> responseBean = new ExecResult<>(exception.getMessage(), ApiMsg.KEY_PARAM_VALIDATE, ApiMsg.ERROR);
        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

    /**
     * 处理CommonException
     *
     * @param e e
     * @return ResponseEntity
     */
    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ExecResult<String>> handleCommonException(java.lang.Exception e) {
        ExecResult<String> execResult = new ExecResult<>(e.getMessage(), ApiMsg.KEY_SERVICE, ApiMsg.ERROR);
        return new ResponseEntity<>(execResult, HttpStatus.OK);
    }

    /**
     * 捕获@Validate校验抛出的异常
     *
     * @param e e
     * @return ResponseEntity
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> validExceptionHandler(BindException e) {
        java.lang.Exception ex = parseBindingResult(e.getBindingResult());
        ExecResult<String> execResult = new ExecResult<>(ex.getMessage(), ApiMsg.KEY_PARAM_VALIDATE, ApiMsg.ERROR);
        return new ResponseEntity<>(execResult, HttpStatus.OK);
    }

    /*@ExceptionHandler(java.lang.Exception.class)
    public ResponseEntity<ExecResult<String>> handleException(java.lang.Exception e) {
        ExecResult<String> execResult = new ExecResult<>(e.getMessage(), ApiMsg.KEY_ERROR, ApiMsg.ERROR);
        return new ResponseEntity<>(execResult, HttpStatus.OK);
    }*/

    /**
     * 提取Validator产生的异常错误
     *
     * @param bindingResult bindingResult
     * @return Exception
     */
    private java.lang.Exception parseBindingResult(BindingResult bindingResult) {
        Map<String, String> errorMsgs = new HashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            errorMsgs.put(error.getField(), error.getDefaultMessage());
        }
        if (errorMsgs.isEmpty()) {
            return new CommonException(ApiMsg.KEY_PARAM_VALIDATE + "");
        } else {
            return new CommonException(JsonMapper.toJsonString(errorMsgs));
        }
    }
}