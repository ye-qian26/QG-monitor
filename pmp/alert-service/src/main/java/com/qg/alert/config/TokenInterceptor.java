package com.qg.common.config;

import cn.hutool.json.JSONUtil;
import com.qg.common.domain.po.Code;
import com.qg.common.domain.po.Result;
import com.qg.common.utils.CryptoUtils;


import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;



@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    /**
     * 在请求处理之前调用（Controller方法调用之前）
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${rsa.key-pairs.pair2.private-key}") // 从配置读取RSA私钥
    private String rsaPrivateKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        // 放行OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }


        // 2. 验证 Token
        String token = request.getHeader("Authorization");
        String Rsakey = request.getHeader("Rsakey");
        log.info("Rsakey:" + Rsakey);

        //判断有没有密钥
        if (Rsakey == null || Rsakey.isEmpty()) {
            Result result = new Result(Code.UNAUTHORIZED, "没有密钥");
            response.getWriter().write(JSONUtil.toJsonStr(result));
            return false;
        }
        if (token == null || !token.startsWith("Bearer ")) {
            Result result = new Result(Code.CONFLICT, "没有Bearer前缀");
            response.getWriter().write(JSONUtil.toJsonStr(result));
            return false;
        } else {
            token = token.substring(7);
        }

        log.info("token解密前" + token);
        token = CryptoUtils.decryptWithAESAndRSA(token, Rsakey, rsaPrivateKey);


        log.info("redis中的缓存：{}",stringRedisTemplate.hasKey("login:user:" + token));

        log.info("解密后的token：{}", token);
        log.info("redis中查询的key：{}" + stringRedisTemplate.hasKey("login:user:" + token));
        if (!stringRedisTemplate.hasKey("login:user:" + token)) {
            Result result = new Result(Code.UNAUTHORIZED, "token无效");
            response.getWriter().write(JSONUtil.toJsonStr(result));
            log.info("token无效");
            return false;
        }

        // 3. 刷新 Token 有效期
        stringRedisTemplate.expire("login:user:" + token, 30, TimeUnit.MINUTES);
        log.info("token刷新成功");
        return true;
    }

}
