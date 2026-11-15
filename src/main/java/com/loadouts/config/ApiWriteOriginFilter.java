package com.loadouts.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** This cookie-based API is served on the same origin as the UI. */
@Component
public class ApiWriteOriginFilter extends OncePerRequestFilter {
  private final boolean requireHttps;
  public ApiWriteOriginFilter(@Value("${loadouts.auth.require-https:true}") boolean requireHttps){
    this.requireHttps=requireHttps;
  }
  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    if(request.getRequestURI().startsWith("/api/")){
      response.setHeader("Cache-Control","no-store");
      if(!Set.of("GET","HEAD","OPTIONS").contains(request.getMethod())){
        if(requireHttps && !request.isSecure() && request.getRequestURI().startsWith("/api/auth/")){
          response.setStatus(403);
          response.setContentType("application/json");
          response.getWriter().write("{\"error\":\"Sign in and registration require HTTPS.\"}");
          return;
        }
        String origin=request.getHeader("Origin");
        String scheme=request.getScheme();
        int port=request.getServerPort();
        String expected=scheme+"://"+request.getServerName()
            +((port==80 && scheme.equals("http")) || (port==443 && scheme.equals("https"))?"":":"+port);
        if("cross-site".equals(request.getHeader("Sec-Fetch-Site")) || (origin!=null && !origin.equalsIgnoreCase(expected))){
          response.setStatus(403);
          response.setContentType("application/json");
          response.getWriter().write("{\"error\":\"This action must come from Loadouts.\"}");
          return;
        }
      }
    }
    chain.doFilter(request,response);
  }
}
