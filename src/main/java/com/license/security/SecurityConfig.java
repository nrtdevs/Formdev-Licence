package com.license.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebMvc
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Autowired
	private CustomUserService userDetailsService;

	@Autowired
	private JwtFilter jwtFilter;
	@Autowired
	CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    SecurityFilterChain securityFilterChain1(HttpSecurity http) throws Exception {

		final String PUBLIC_URL[] = {"/users/resetPassword/**","/users/loginUser/**","/login/jwt/**",
				                      "/users/login/**" ,"/users/forgot/**","/users/registration/**",
				                      "/users/verifyOtp/**","/users/updatePassword/**","/users/createUser",
				                      "/users/forgotPage/**","/users/updatePasswordPage/**",};
		http.csrf(csrf -> csrf.disable());
		http
	    .authorizeHttpRequests(
	        (requests) -> requests
	            .requestMatchers(PUBLIC_URL).permitAll()
	            .anyRequest().authenticated()
	    )
	    .formLogin(
	        (formLogin) -> formLogin
	            .loginPage("/users/loginUser").permitAll()
	    )
	    .logout(
	        (logout) -> logout
	            .logoutUrl("/logout")
	            .logoutSuccessUrl("/login")
	            .invalidateHttpSession(true)
	            .deleteCookies("JSESSIONID", "jwtToken")
	    )
	    .exceptionHandling(
	        (exceptionHandling) -> exceptionHandling
	            .accessDeniedHandler(customAccessDeniedHandler) // Set the custom access denied handler here
	  );
		http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();

	}

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService CustumUserDetails) {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    @Bean
    AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration) throws Exception {

		return configuration.getAuthenticationManager();
	}
}