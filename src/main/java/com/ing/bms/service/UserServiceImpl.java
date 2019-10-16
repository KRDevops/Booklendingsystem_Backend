package com.ing.bms.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ing.bms.dto.UserRegisterRequestDTO;
import com.ing.bms.dto.UserRegisterResponseDTO;
import com.ing.bms.entity.User;
import com.ing.bms.exception.EmailException;
import com.ing.bms.exception.InvalidMobileNumberException;
import com.ing.bms.repository.UserRepository;
import com.ing.bms.util.BMSUtil;
import com.ing.bms.util.EmailValidator;

/**
 * @since 2019-10-16 This class includes methods for registering into book
 *        management system, login to bms
 */
@Service
public class UserServiceImpl implements UserService {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
	private static final String MOBILE_VALIDATION = "^[0-9]{10}$";

	@Autowired
	UserRepository userRepository;

	@Value("${statusCode.success}")
	private Integer statusCode;

	@Value("${apiKey}")
	private String apiKey;

	@Value("${message.part1}")
	private String msgPart1;

	@Value("${message.part2}")
	private String msgPart2;

	@Value("${senderName}")
	private String senderName;

	/**
	 * @param userRegisterRequest
	 * 
	 * @return ResponseMessage which includes success code, success/failure message.
	 *         This method will accept all newly registering users and saves them
	 *         into database.
	 */
	public UserRegisterResponseDTO register(UserRegisterRequestDTO userRegisterRequest)
			throws NoSuchAlgorithmException {
		LOGGER.info("register method in UserService started");

		if (!new EmailValidator().validateEmail(userRegisterRequest.getEmailId()))
			throw new EmailException(BMSUtil.EMAIL_EXCEPTION);

		if (!isValidPhoneNumber(userRegisterRequest.getPhoneNumber()))
			throw new InvalidMobileNumberException(BMSUtil.INVALID_MOBILE_NUMBER_EXCEPTION);

		User register = new User();
		UserRegisterResponseDTO responseDTO = new UserRegisterResponseDTO();
		BeanUtils.copyProperties(userRegisterRequest, register);
		register.setPassword(generatePassword(userRegisterRequest.getUserName()));

		userRepository.save(register);
		responseDTO.setMessage(BMSUtil.SUCCESS);
		responseDTO.setStatusCode(statusCode);

		sendSms(register.getUserName(), register.getPassword(), register.getPhoneNumber());
		LOGGER.info("register method in UserService ended");
		return responseDTO;
	}

	/**
	 * 
	 * @return This method checks if the user has entered a valid mobile number or
	 *         not
	 * 
	 */
	private boolean isValidPhoneNumber(Long number) {
		String num = number.toString();
		Pattern p = Pattern.compile(MOBILE_VALIDATION);
		Matcher m = p.matcher(num);
		return (m.find() && m.group().equals(num));
	}

	/**
	 * 
	 * @return This method returns the uniquely generated password for every user
	 *         registering.
	 * 
	 */
	private String generatePassword(String passwordToHash) throws NoSuchAlgorithmException {
		String generatedPassword = null;

		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(passwordToHash.getBytes());
		byte[] bytes = md.digest();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		// Get complete hashed password in hexadecimal format
		generatedPassword = sb.toString();
		return generatedPassword.substring(0, 5);
	}

	public String sendSms(String userName, String passWord, Long phoneNumber) {
		try {
			// Construct data
			String message = msgPart1 + userName + msgPart2 + passWord;
			String sender = "&sender=" + senderName;
			String numbers = "&numbers=" + "91" + phoneNumber.toString();

			// Send data
			HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
			String data = apiKey + numbers + message + sender;
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
			conn.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
			final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final StringBuilder stringBuffer = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				stringBuffer.append(line);
			}
			rd.close();
			LOGGER.info("SMS sent");
			return stringBuffer.toString();
		} catch (Exception e) {
			LOGGER.error("Error SMS " + e);
			return "Error " + e;
		}
	}
}