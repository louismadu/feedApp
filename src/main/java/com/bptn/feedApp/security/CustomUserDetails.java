package com.bptn.feedApp.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bptn.feedApp.jpa.User;

public class CustomUserDetails implements UserDetails {

 private static final long serialVersionUID = 1L;

 @Override
 public Collection<? extends GrantedAuthority> getAuthorities() {
     
     return null;
 }

 @Override
 public String getPassword() {
     
     return null;
 }

 @Override
 public String getUsername() {
     
     return null;
 }

 @Override
 public boolean isAccountNonExpired() {
     
     return false;
 }

 @Override
 public boolean isAccountNonLocked() {
     
     return false;
 }

 @Override
 public boolean isCredentialsNonExpired() {
     
     return false;
 }

 @Override
 public boolean isEnabled() {
     
     return false;
 }
 
 User user;

public CustomUserDetails(User user) {
	super();
	this.user = user;
}

}
