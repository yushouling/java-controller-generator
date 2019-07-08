package com.daoshu.yapi.codegen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class CommonExceptionHandler {

    /**
     * 处理所有 Exception.class异常
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public String handler500(Exception e) {
        return "解析json文件异常：json文件不是YApi导出的文件或文件已被修改。<a href=\"../\">重试</a>";
    }
}
