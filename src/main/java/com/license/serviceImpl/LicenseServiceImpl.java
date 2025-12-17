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
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.license.entity.License;
import com.license.entity.User;
import com.license.exception.ModuleExpiryMissingException;
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

		// Validate module expiry if modules are selected
		if (license.getModules() != null && !license.getModules().isEmpty()) {
			if (license.getModuleExpiry() == null || license.getModuleExpiry().isEmpty()) {
				throw new ModuleExpiryMissingException("Module expiry details are required when modules are selected.");
			}
		}

		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		String LicenseFileName = "LicenseFile_" + license.getName() + "_" + license.getLicenseFor() + "_" + timestamp
				+ ".txt";
		String LicenseKeyName = "LicenseKey_" + license.getName() + "_" + license.getLicenseFor() + "_" + timestamp
				+ ".txt";

		// Use a project-relative folder so generated files are stored predictably
		String baseDir = System.getProperty("user.dir") + File.separator + "generated_licenses" + File.separator;
		// ensure directory exists
		File base = new File(baseDir);
		if (!base.exists()) {
			base.mkdirs();
		}

		String LicenseFilePath = baseDir + LicenseFileName;
		String LicenseKeyPath = baseDir + LicenseKeyName;

		String currentUser = userServiceImpl.getCurrentUser();

		User user = userRepository.findByEmail(currentUser);

		int duration = license.getDuration();

		// String selectedOptions= license.getSelectedOptions();

		// These fields are not directly updated via the edit form,
		// so they are not relevant for the updateLicense method.
		// String macModuleName = license.getMacModuleName();
		// Integer macTenureDays = license.getMacTenureDays();
		// String specificEmail = license.getSpecificEmail();
		// String emailModuleName = license.getemail();
		// Integer emailTenureDays = license.get();

		Date expiryDate = calculateExpirationDate(duration);
		license.setExpirationDate(convertToSqlDate(expiryDate));

		// Set the license for the user
		user.setLicense(license);

		license.setFilePath(LicenseFilePath);
		// Save the license (this will also save the associated user due to cascade
		// settings)
		License savedLicense = licenseRepository.save(license);

		createLicenseFile(license, LicenseFilePath);
		createLicenseKey(license, LicenseKeyPath);

		// Save the user (not necessary if cascade is properly set, but won't hurt)
		userRepository.save(user);

		return savedLicense;
	}

	@Override
	public License updateLicense(Long id, License updatedLicense) {
		if (licenseRepository.existsById(id)) {
			License license = licenseRepository.findById(id).get();
			license.setName(updatedLicense.getName());
			license.setEmail(updatedLicense.getEmail());
			license.setCompanyName(updatedLicense.getCompanyName());
			license.setLicenseFor(updatedLicense.getLicenseFor());
			license.setLicenseType(updatedLicense.getLicenseType());
			 license.setDuration(updatedLicense.getDuration());
			license.setMacId(updatedLicense.getMacId());
			license.setModules(updatedLicense.getModules());
			license.setMacUsageCount(updatedLicense.getMacUsageCount());
			license.setUserEmail(updatedLicense.getUserEmail());
			license.setWeeklyLimit(updatedLicense.getWeeklyLimit());
			
			// Validate module expiry if modules are selected
			if (updatedLicense.getModules() != null && !updatedLicense.getModules().isEmpty()) {
				if (updatedLicense.getModuleExpiry() == null || updatedLicense.getModuleExpiry().isEmpty()) {
					throw new ModuleExpiryMissingException("Module expiry details are required when modules are selected.");
				}
			}
			license.setModuleExpiry(updatedLicense.getModuleExpiry()); // Add this line to update moduleExpiry

			// Recalculate expiration date if duration is updated
			if (updatedLicense.getDuration() > 0) {
				Date expiryDate = calculateExpirationDate(updatedLicense.getDuration());
				license.setExpirationDate(convertToSqlDate(expiryDate));
			}

			License savedLicense = licenseRepository.save(license);

			// Regenerate license files after update
			createLicenseFile(savedLicense, savedLicense.getFilePath());
			// Assuming LicenseKeyPath can be derived or is stored similarly,
			// for now, I'll assume it's stored in the license object or can be derived from filePath
			// For simplicity, I'll derive it based on the existing pattern.
			String licenseKeyPath = savedLicense.getFilePath().replace("LicenseFile_", "LicenseKey_");
			createLicenseKey(savedLicense, licenseKeyPath);

			return savedLicense;
		}
		return null; // or throw an exception indicating that the license doesn't exist
	}

	@Override
	public void deleteLicense(Long id) {
		licenseRepository.deleteById(id);
	}

	public static void createLicenseKey(License license, String filePath) {

		System.out.println("createLicenseKey method called");

		try {
			// Generate a timestamp for the file name

			//
			File file = new File(filePath);
			// ensure parent dirs exist (in case filePath points to nested dirs)
			if (file.getParentFile() != null && !file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
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
				String encryptedMacId = "";
				if (license.getMacId() != null) {
					encryptedMacId = Base64.getEncoder()
							.encodeToString(cipher.doFinal(license.getMacId().getBytes(StandardCharsets.UTF_8)));
				}

				String encryptedLicenseKey = Base64.getEncoder()
						.encodeToString(cipher.doFinal(license.getLicenseKey().getBytes(StandardCharsets.UTF_8)));
				String encryptedDuration = Base64.getEncoder().encodeToString(
						cipher.doFinal(Integer.toString(license.getDuration()).getBytes(StandardCharsets.UTF_8)));
				String encryptedExpirationDate = Base64.getEncoder().encodeToString(
						cipher.doFinal(license.getExpirationDate().toString().getBytes(StandardCharsets.UTF_8)));
				String encryptedTimeStamp = Base64.getEncoder().encodeToString(
						cipher.doFinal(license.getTimeStamp().toString().getBytes(StandardCharsets.UTF_8)));

				String encryptedModuleExpiry = "";
				if (license.getModuleExpiryString() != null) {
					encryptedModuleExpiry = Base64.getEncoder().encodeToString(
							cipher.doFinal(license.getModuleExpiryString().getBytes(StandardCharsets.UTF_8)));
				}

				// Append encrypted data to the file
				fileWriter.write(encryptedId + "$" + encryptedName + "$" + encryptedEmail + "$" + encryptedMacId + "$"
						+ encryptedLicenseKey + "$" + encryptedDuration + "$" + encryptedExpirationDate + "$"
						+ encryptedTimeStamp + "$" + encryptedModuleExpiry + "\n");

				System.out.println("File generated successfully: " + filePath);

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

	public static void createLicenseFile(License license, String filePath) {

		System.out.println("Generating plain text file");

		try {
			File file = new File(filePath);
			// ensure parent dirs exist
			if (file.getParentFile() != null && !file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			file.createNewFile();

			try (FileWriter fileWriter = new FileWriter(file)) {

				fileWriter.write("Company Name     = " + license.getCompanyName() + "\n");
				fileWriter.write("Person Name      = " + license.getName() + "\n");
				fileWriter.write("Email            = " + license.getEmail() + "\n");
				fileWriter.write("Phone Number     = " + (license.getPhoneNumber() != null ? license.getPhoneNumber() : "N/A") + "\n");
				fileWriter.write("Primary Contact  = " + (license.getPrimaryContactName() != null ? license.getPrimaryContactName() : "N/A") + " ("
						+ (license.getPrimaryContactNumber() != null ? license.getPrimaryContactNumber() : "N/A") + ")\n");
				fileWriter.write("SCM Contact      = " + (license.getScmContactName() != null ? license.getScmContactName() : "N/A") + " ("
						+ (license.getScmContactNumber() != null ? license.getScmContactNumber() : "N/A") + ")\n");
				fileWriter.write("Date Issued      = " + license.getTimeStamp() + "\n");
				// fileWriter.write("License Duration = " + license.getDuration() + " days\n");
				fileWriter.write("Expiration Date  = " + license.getExpirationDate() + "\n");
				fileWriter.write("License Key      = " + license.getLicenseKey() + "\n");
				fileWriter.write("License For      = " + license.getLicenseFor() + "\n");
				fileWriter.write("License Type     = " + license.getLicenseType() + "\n");
				fileWriter.write("Modules          = " + (license.getModulesString() != null ? license.getModulesString() : "N/A") + "\n");
				fileWriter.write("Module Expiry    = " + (license.getModuleExpiryString() != null ? license.getModuleExpiryString() : "N/A") + "\n");

// Conditional fields based on License Type
if ("MAC_ID".equalsIgnoreCase(license.getLicenseType())) {
    fileWriter.write("MacId Address    = " + (license.getMacId() != null ? license.getMacId() : "N/A") + "\n");
    fileWriter.write("Usage Count      = " + (license.getMacUsageCount() != null ? license.getMacUsageCount() : "N/A") + "\n");
} else if ("EMAIL_ID".equalsIgnoreCase(license.getLicenseType())) {
    fileWriter.write("Email Address    = " + (license.getUserEmail() != null ? license.getUserEmail() : "N/A") + "\n");
    fileWriter.write("Weekly Limit     = " + (license.getWeeklyLimit() != null ? license.getWeeklyLimit() : "N/A") + "\n");
}


				System.out.println("Plain text file generated successfully: " + filePath);
			

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
		return licenseRepository.findByType("purchase").size();
	}

	@Override
	public List<License> getAllDemoLicenses() {

		return licenseRepository.findByType("demo");
	}

	@Override
	public List<License> getAllActualLicenses() {

		return licenseRepository.findByType("purchase");
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

	@Override
public Optional<License> getLicenseByKey(String licenseKey) {
    return licenseRepository.findByLicenseKey(licenseKey);
}

@Override
public Resource getEncryptedLicenseFile(Long id) throws Exception {
    Optional<License> optionalLicense = licenseRepository.findById(id);
    if (optionalLicense.isEmpty()) {
        throw new RuntimeException("License not found with ID: " + id);
    }
    License license = optionalLicense.get();
    String filePath = license.getFilePath();

    if (filePath == null || filePath.isEmpty()) {
        throw new RuntimeException("License file path not found for license ID: " + id);
    }

    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
        throw new RuntimeException("License file not found at path: " + filePath);
    }

    // Read the content of the license file
    String fileContent = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

    // Encrypt the content
    String encryptedContent = encrypt(fileContent);

    // Create a resource from the encrypted content
    ByteArrayResource resource = new ByteArrayResource(encryptedContent.getBytes(StandardCharsets.UTF_8)) {
        @Override
        public String getFilename() {
            return "encrypted_" + path.getFileName().toString();
        }
    };
    return resource;
}

private String encrypt(String data) throws Exception {
    byte[] keyBytes = new byte[16];
    byte[] secretKeyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
    System.arraycopy(secretKeyBytes, 0, keyBytes, 0, Math.min(secretKeyBytes.length, keyBytes.length));
    SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);

    byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encryptedBytes);
}

}
