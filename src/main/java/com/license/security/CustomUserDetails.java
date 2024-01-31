package com.license.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.license.entity.User;
import com.license.serviceImpl.PermissionService;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@NoArgsConstructor
public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 8015253558311061965L;

	private User user;

	@Autowired(required = true)
	private PermissionService permissionServices;

	public CustomUserDetails(User user, PermissionService permissionServices) {
		this.user = user;
		this.permissionServices = permissionServices;

	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<SimpleGrantedAuthority> authorities = new HashSet<>();
		List<String> permissions = permissionServices.getPermissionsByUserId(user.getId());
		for (String permission : permissions) {
			authorities.add(new SimpleGrantedAuthority(permission));
			log.debug("permissons assigned to user role " + user.getRole().getName());
		}
		authorities.add(new SimpleGrantedAuthority("ROLE_SUPER ADMIN"));
		log.debug("permissons assigned to user role " + user.getRole().getName());
		return authorities;

	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}