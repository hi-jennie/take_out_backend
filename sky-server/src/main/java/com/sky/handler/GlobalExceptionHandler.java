package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 *  Global exception handler
 * → Controller throws an exception
 * → DispatcherServlet catches it
 * → looks for a local @ExceptionHandler
 * → if none, checks global @ControllerAdvice → executes the matching exception handler method.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        // database throw the error with msg: Duplicate entry 'zhangsan' for key 'employee.idx_username'
        if(ex.getMessage().contains("Duplicate entry")){
            log.error("duplicate user：{}", ex.getMessage());
            String duplicateName = ex.getMessage().split(" ")[2];
            String msg = duplicateName + MessageConstant.ACCOUNT_EXISTED;
            return Result.error(msg);
        }else{
            log.error("error：{}", ex.getMessage());
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
