package com.team01.backend.global.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

// 요청 파라미터의 XSS 방지 처리를 위한 래퍼 클래스
public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return sanitize(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        for (int i = 0; i < values.length; i++) {
            values[i] = sanitize(values[i]);
        }
        return values;
    }

    // 특수문자 제거로 XSS 방지
    private String sanitize(String value) {
        if (value == null) return null;
        return value.replace("<", "").replace(">", "").replace("&", "");
    }
}
