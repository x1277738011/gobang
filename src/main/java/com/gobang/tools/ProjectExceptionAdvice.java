package com.gobang.tools;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProjectExceptionAdvice {
    @ExceptionHandler
    public R doException(Exception ex){
        ex.printStackTrace();
        return R.fail(500,"错误");
    }
}
