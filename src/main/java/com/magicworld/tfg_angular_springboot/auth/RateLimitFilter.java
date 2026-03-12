package com.magicworld.tfg_angular_springboot.auth;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private static final int LIMIT = 5;
    private static final Duration DURATION = Duration.ofMinutes(1);

    public void clearBuckets() {
        buckets.clear();
    }

    private Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(LIMIT, Refill.intervally(LIMIT, DURATION)))
                .build());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();
        if (path.matches("^/api/v1/auth/(login|register|forgot-password|reset-password)$")) {
            String ip = req.getRemoteAddr();
            Bucket bucket = resolveBucket(ip);
            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                res.setStatus(429);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"code\":\"error.too.many.requests\"}");
                res.getWriter().flush();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
