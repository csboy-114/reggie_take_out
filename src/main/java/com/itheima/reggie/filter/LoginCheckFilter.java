package com.itheima.reggie.filter;

import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        log.info("拦截到请求:{}",request.getRequestURI());
        log.info("线程id：{}",Thread.currentThread().getId());
        String requestURI = request.getRequestURI();
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };
        boolean check = check(urls, requestURI);
        if(check){
            filterChain.doFilter(request,response);
            return;
        }
        // 后台管理登录
        if(request.getSession().getAttribute("employee")!=null){
            Long empId =(Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }
        // 移动端登录
        if(request.getSession().getAttribute("user")!=null){
            Long userId =(Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }

        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean isMatch = PATH_MATCHER.match(url, requestURI);
            if(isMatch){
                return true;
            }
        }
        return  false;
    }
}
