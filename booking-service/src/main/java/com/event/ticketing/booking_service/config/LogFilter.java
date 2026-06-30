package com.event.ticketing.booking_service.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class LogFilter extends OncePerRequestFilter {

	public static final String CORRELATION_ID = "correlationId";
	public static final String HEADER_NAME = "X-Correlation-ID";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String correlationId = request.getHeader(HEADER_NAME);

		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}

		MDC.put(CORRELATION_ID, correlationId);
		response.setHeader(HEADER_NAME, correlationId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}
}