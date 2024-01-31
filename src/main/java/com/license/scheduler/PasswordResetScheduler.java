package com.license.scheduler;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.license.entity.User;
import com.license.service.EmailServieces;
import com.license.service.UserService;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;

@Service
public class PasswordResetScheduler {

	@Autowired
	private UserService userService;

	@Autowired
	private EmailServieces emailService;

	public PasswordResetScheduler(UserService userService, EmailServieces emailService) {
		super();
		this.userService = userService;
		this.emailService = emailService;
	}

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void startScheduler() {
		// Schedule the task to run every day
		scheduler.scheduleAtFixedRate(this::checkAndSendPasswordResetEmail, 0, 1, TimeUnit.DAYS);

	}

	@PostConstruct
	public void initializeScheduler() {
		startScheduler();
	}

	private void checkAndSendPasswordResetEmail() {
		List<User> users = userService.getAllUsers();

		for (User user : users) {
			Date passwordUpdatedAt = user.getPasswordUpdatedAt();
			LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);

			System.out.println("user data checked ");

			if (passwordUpdatedAt != null && passwordUpdatedAt.toLocalDate().isBefore(threeMonthsAgo)) {
				// Password needs to be reset, send email
				sendPasswordResetEmail(user);
			}
		}
	}

	private void sendPasswordResetEmail(User user) {
		// Implement your logic to send a password reset email
		// You can use the emailService.sendPasswordResetEmail(user.getEmail()) method
		// or any other logic
		try {
			emailService.sendEmailwithTemplate(user.getEmail(), "resetPasswordTemplate");
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.getMessage();
		}
	}
}
