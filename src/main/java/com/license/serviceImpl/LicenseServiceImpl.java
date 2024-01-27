package com.license.serviceImpl;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.license.entity.License;
import com.license.entity.User;
import com.license.repository.LicenseRepository;
import com.license.repository.UserRepository;
import com.license.service.LicenseService;

import jakarta.servlet.http.HttpSession;

@Service
public class LicenseServiceImpl implements LicenseService {

	private static final String SECRET_KEY = "MySecretKey#95457556941234567890";
	private static final String ALGORITHM = "AES"; // Advanced Encryption Standard

	@Autowired
	private UserServiceImpl userServiceImpl;

	@Autowired
	private LicenseRepository licenseRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public List<License> getAllLicenses() {
		return licenseRepository.findAll();
	}

	@Override
	public Optional<License> getLicenseById(Long id) {
		return licenseRepository.findById(id);
	}

	@Override
	public License createLicense(License license, HttpSession session) {
		// Your @PrePersist method will automatically generate the license key,
		// timestamp, and MAC address before saving]

		String currentUser = userServiceImpl.getCurrentUser();

		User user = userRepository.findByEmail(currentUser);

		int duration = license.getDuration();

		Date expiryDate = calculateExpirationDate(duration);
		license.setExpirationDate(convertToSqlDate(expiryDate));

		// Set the license for the user
		user.setLicense(license);

		// Save the license (this will also save the associated user due to cascade
		// settings)
		License savedLicense = licenseRepository.save(license);

		// Save the user (not necessary if cascade is properly set, but won't hurt)
		userRepository.save(user);

		encryptSensitiveInfo(savedLicense);
		generatePlainTextFile(savedLicense);

		return savedLicense;
	}

	@Override
	public License updateLicense(Long id, License updatedLicense) {

		if (licenseRepository.existsById(id)) {
			License license = licenseRepository.findById(id).get();
			if (updatedLicense.getExpirationDate() != null)
				license.setExpirationDate(updatedLicense.getExpirationDate());
			if (updatedLicense.getEmail() != null)
				license.setEmail(updatedLicense.getEmail());
			if (updatedLicense.getName() != null)
				license.setName(updatedLicense.getName());

			return licenseRepository.save(license);
		}
		return null; // or throw an exception indicating that the license doesn't exist
	}

	@Override
	public void deleteLicense(Long id) {
		licenseRepository.deleteById(id);

	}

	public static void encryptSensitiveInfo(License license) {

		System.out.println("Encrypt method called");

		try {
			// Generate a timestamp for the file name
			String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

			// Construct the file name with the timestamp
			// String fileName =
			// "C:\\Users\\lenovo\\Desktop\\generatedFiles\\encrypted_License_Data_" +
			// timestamp + ".txt";

			String fileName = "C:\\Users\\Asus\\OneDrive\\Desktop\\file\\License_" + timestamp + ".txt";

			File file = new File(fileName);
			file.createNewFile();

			try (FileWriter fileWriter = new FileWriter(file)) {
				// Ensure the key is the correct length (16, 24, or 32 bytes)
				byte[] keyBytes = new byte[16];
				byte[] secretKeyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
				System.arraycopy(secretKeyBytes, 0, keyBytes, 0, Math.min(secretKeyBytes.length, keyBytes.length));
				SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

				// Create Cipher instance and initialize with encryption mode
				Cipher cipher = Cipher.getInstance(ALGORITHM);
				cipher.init(Cipher.ENCRYPT_MODE, secretKey);

				// Encrypt and Base64 encode each field(Implementing AES-256 Encryption)
				String encryptedId = Base64.getEncoder()
						.encodeToString(cipher.doFinal(license.getId().toString().getBytes(StandardCharsets.UTF_8)));
				String encryptedName = Base64.getEncoder()
						.encodeToString(cipher.doFinal(license.getName().getBytes(StandardCharsets.UTF_8)));
				String encryptedEmail = Base64.getEncoder()
						.encodeToString(cipher.doFinal(license.getEmail().getBytes(StandardCharsets.UTF_8)));
				String encryptedMacId = Base64.getEncoder()
						.encodeToString(cipher.doFinal(license.getMacId().getBytes(StandardCharsets.UTF_8)));
				String encryptedLicenseKey = Base64.getEncoder()
						.encodeToString(cipher.doFinal(license.getLicenseKey().getBytes(StandardCharsets.UTF_8)));
				String encryptedDuration = Base64.getEncoder().encodeToString(
						cipher.doFinal(Integer.toString(license.getDuration()).getBytes(StandardCharsets.UTF_8)));
				String encryptedExpirationDate = Base64.getEncoder().encodeToString(
						cipher.doFinal(license.getExpirationDate().toString().getBytes(StandardCharsets.UTF_8)));
				String encryptedTimeStamp = Base64.getEncoder().encodeToString(
						cipher.doFinal(license.getTimeStamp().toString().getBytes(StandardCharsets.UTF_8)));

				// Append encrypted data to the file
				fileWriter.write(encryptedId + "$" + encryptedName + "$" + encryptedEmail + "$" + encryptedMacId + "$"
						+ encryptedLicenseKey + "$" + encryptedDuration + "$" + encryptedExpirationDate + "$"
						+ encryptedTimeStamp + "\n");

				System.out.println("File generated successfully: " + fileName);

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error writing to the file");
				// Handle the exception based on your requirements
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error creating the file");
			// Handle the exception based on your requirements
		}
	}

	public static void generatePlainTextFile(License license) {

		System.out.println("Generating plain text file");

		try {
			// Generate a timestamp for the file name
			String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

			// Construct the file name with the timestamp
			String fileName = "C:\\Users\\Asus\\OneDrive\\Desktop\\file\\licenseDetails_" + timestamp + ".txt";

			// String fileName = "C:\\Users\\Mr.Akshay_005\\OneDrive\\Desktop\\New folder
			// (2)\\licenseKey_" + timestamp + ".txt";

			File file = new File(fileName);
			file.createNewFile();

			try (FileWriter fileWriter = new FileWriter(file)) {

				// Write each field in plain text format to the file
				fileWriter.write("Company Name     = " + license.getCompanyName() + "\n");
				fileWriter.write("Person Name      = " + license.getName() + "\n");
				fileWriter.write("Date Issued      = " + license.getTimeStamp() + "\n");
				fileWriter.write("License Duration = " + license.getDuration() + " days\n");
				fileWriter.write("License Key      = " + license.getLicenseKey() + "\n");
				fileWriter.write("MacId Address    = " + license.getMacId() + "\n");

				System.out.println("Plain text file generated successfully: " + fileName);

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error writing to the file");
				// Handle the exception based on your requirements
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error creating the file");
			// Handle the exception based on your requirements
		}
	}

	// check licenseKey is valid or not
	@Override
	public boolean isValidLicenseKey(String licenseKey) {
		// Check if the entered license key matches any existing license key in the
		// database
		return licenseRepository.findByLicenseKey(licenseKey).isPresent();
	}

	// check license valid or not
	@Override
	public boolean isLicenseValid(String licenseKey) {
		Optional<License> optionalLicense = licenseRepository.findByLicenseKey(licenseKey);

		if (optionalLicense.isPresent()) {
			License license = optionalLicense.get();

			// Check if the current date is before the expiration date
			if (new Date().before(license.getExpirationDate())) {

				// Check if the current date is after the timestamp
				if (new Date().after(license.getTimeStamp())) {

					return true; // License is valid
				}
			}
		}

		return false; // License is not valid
	}

	private Date calculateExpirationDate(int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, days);

		// Set the time part to 23:59:59 for the expiration date
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		// ;calendar.set(Calendar.MILLISECOND, 999);

		return calendar.getTime();
	}

	private static java.sql.Date convertToSqlDate(java.util.Date utilDate) {
		// Convert java.util.Date to java.sql.Date
		return new java.sql.Date(utilDate.getTime());
	}

	@Override
	public int getTotalDemoUsers() {
		return licenseRepository.findByType("demo").size();

	}

	@Override
	public int getTotalActualUsers() {
		return licenseRepository.findByType("actual").size();
	}

	@Override
	public List<License> getAllDemoLicenses() {

		return licenseRepository.findByType("demo");
	}

	@Override
	public List<License> getAllActualLicenses() {

		return licenseRepository.findByType("actual");
	}

	@Override
	public License findLicenseBySearchInput(String searchInput) {
		// Use the custom query method in the repository to find the license
		Optional<License> license = licenseRepository.findByNameOrEmailOrMacIdOrLicenseKey(searchInput, searchInput,
				searchInput, searchInput);

		// Case-sensitive comparison
				// Check if the result is present and if the input matches the result
				if (license.isPresent() && searchInput.equals(license.get().getName())) {
					// Case-sensitive match found
					return license.get();
				} else if (license.isPresent() && searchInput.equals(license.get().getEmail())) {
					return license.get();
				} else if (license.isPresent() && searchInput.equals(license.get().getMacId())) {
					return license.get();
				} else if (license.isPresent() && searchInput.equals(license.get().getLicenseKey())) {
					return license.get();
				} else {
					// Case-sensitive match not found
					return null;
				}
	}
}
