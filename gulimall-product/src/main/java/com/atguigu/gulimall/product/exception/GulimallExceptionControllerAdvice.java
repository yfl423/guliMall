package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 集中，统一异常处理
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {
    // 数据校验异常
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public R vaildExceptionHandler(MethodArgumentNotValidException e) {

//        log.error(e.getMessage(),e.getClass());
        Map<String, String> errorMap = new LinkedHashMap<>();
        BindingResult bindingResult = e.getBindingResult();
        bindingResult.getFieldErrors().forEach((item) -> {
            String field = item.getField();
            String message = item.getDefaultMessage();
            errorMap.put(field, message);
        });
        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(), "数据校验发生错误").put("data", errorMap);
    }

    // 其他异常
    @ExceptionHandler(value = {Throwable.class})
    public R unknownExceptionHandler(Throwable t) {

        return R.error(BizCodeEnume.UNKNOWN_EXCEPTION.getCode(), "未知错误").put("data", t);
    }

}
