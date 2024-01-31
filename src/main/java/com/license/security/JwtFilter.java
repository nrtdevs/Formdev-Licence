package com.license.security;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CustomUserService customUserDetailsService;

	private static final String JWT_COOKIE_NAME = "jwtToken";

	private final Logger log = LoggerFactory.getLogger(JwtFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		log.debug("user login request:	" + request.getRemoteUser());

		String jwtToken = null;

		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(JWT_COOKIE_NAME)) {
					jwtToken = cookie.getValue();
					log.debug("token is : " + jwtToken);
					break;
				}
			}
		}
		String userName = null;
		if (jwtToken != null) {
			try {
				userName = this.jwtUtil.getUsernameFromToken(jwtToken);
			} catch (ExpiredJwtException e1) {
				log.error("This token is Expired Please Generate new Token......");

			} catch (MalformedJwtException e2) {
				log.error("Please Inter Valid Token....");

			} catch (RuntimeException e) {
				log.error("Internal Error Please Try Again....." + e.getMessage());

			}
		}

		log.info("userName    ...." + userName);

		if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			UserDetails userDetails = customUserDetailsService.loadUserByUsername(userName);

			if (this.jwtUtil.validateToken(jwtToken, userDetails)) {
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}

		}

		filterChain.doFilter(request, response);

	}

}