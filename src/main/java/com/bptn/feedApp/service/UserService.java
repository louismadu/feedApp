package com.bptn.feedApp.service;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bptn.feedApp.exception.domain.EmailExistException;
import com.bptn.feedApp.exception.domain.EmailNotVerifiedException;
import com.bptn.feedApp.exception.domain.UserNotFoundException;
import com.bptn.feedApp.exception.domain.UsernameExistException;
import com.bptn.feedApp.jpa.User;
import com.bptn.feedApp.provider.ResourceProvider;
import com.bptn.feedApp.repository.UserRepository;
import com.bptn.feedApp.security.JwtService;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	EmailService emailService; // Autowire EmailService

	@Autowired
	PasswordEncoder passwordEncoder; // Autowire PasswordEncoder

	// Method to list all users
	public List<User> listUsers() {
		return this.userRepository.findAll();
	}

	// Method to find a user by username
	public Optional<User> findByUsername(String username) {
		return this.userRepository.findByUsername(username);
	}

	// Method to create a new user
	public void createUser(User user) {
		this.userRepository.save(user);
	}

	// New method to validate username and email for duplication
	private void validateUsernameAndEmail(String username, String emailId) {
		this.userRepository.findByUsername(username).ifPresent(u -> {
			throw new UsernameExistException(String.format("Username already exists, %s", u.getUsername()));
		});

		this.userRepository.findByEmailId(emailId).ifPresent(u -> {
			throw new EmailExistException(String.format("Email already exists, %s", u.getEmailId()));
		});
	}

	// Updated signup method with validation and password encryption
	public User signup(User user) {
		// Convert username and emailId to lowercase
		user.setUsername(user.getUsername().toLowerCase());
		user.setEmailId(user.getEmailId().toLowerCase());

		// Validate username and email for duplication
		this.validateUsernameAndEmail(user.getUsername(), user.getEmailId());

		// Set emailVerified to false and encrypt the password
		user.setEmailVerified(false);
		user.setPassword(this.passwordEncoder.encode(user.getPassword()));
		user.setCreatedOn(Timestamp.from(Instant.now()));

		// Save the user to the database
		this.userRepository.save(user);

		// Send the verification email after saving the user
		this.emailService.sendVerificationEmail(user);

		// Return the saved user object
		return user;
	}

	public void verifyEmail() {

		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));

		user.setEmailVerified(true);

		this.userRepository.save(user);
	}

	private static User isEmailVerified(User user) {

		if (user.getEmailVerified().equals(false)) {
			throw new EmailNotVerifiedException(String.format("Email requires verification, %s", user.getEmailId()));
		}

		return user;
	}

	@Autowired
	AuthenticationManager authenticationManager;

	private Authentication authenticate(String username, String password) {
		return this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	}

	public User authenticate(User user) {

		/* Spring Security Authentication. */
		this.authenticate(user.getUsername(), user.getPassword());

		/* Get User from the DB. */
		return this.userRepository.findByUsername(user.getUsername()).map(UserService::isEmailVerified).get();
	}

	@Autowired
	JwtService jwtService;

	@Autowired
	ResourceProvider provider;

	public HttpHeaders generateJwtHeader(String username) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(AUTHORIZATION, this.jwtService.generateJwtToken(username, this.provider.getJwtExpiration()));

		return headers;
	}
}