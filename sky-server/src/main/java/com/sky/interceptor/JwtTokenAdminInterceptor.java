package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * verify jwt token
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //determine if the handler is a controller. If so , need to verify the token
        if (!(handler instanceof HandlerMethod)) {
            // not a controller , let it go
            return true;
        }

        //1、get token from header
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、verify token
        try {
            log.info("jwt  verification:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());

            // hold the current emp id in ThreadLocal so that service layer can use it
            BaseContext.setCurrentId(empId);

            log.info("id of current employ：", empId);
            //3、pass verification and let it go to the corresponding controller
            return true;
        } catch (Exception ex) {
            //4、verification failed
            response.setStatus(401);
            return false;
        }
    }
}
