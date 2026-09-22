package com.QuoocCuongwf.EFEWallet.AuthService.service;

import com.QuoocCuongwf.EFEWallet.AuthService.config.SecurityConstants;
import com.QuoocCuongwf.EFEWallet.AuthService.entity.User;
import com.QuoocCuongwf.EFEWallet.AuthService.exception.EmailNotExistException;
import com.QuoocCuongwf.EFEWallet.AuthService.exception.EmailNotMatchWithUserCurrent;
import com.QuoocCuongwf.EFEWallet.AuthService.exception.EmailUsedException;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.dto.OtpMessage;
import com.QuoocCuongwf.EFEWallet.AuthService.repository.UserReponsitory;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Service
public class OtpService {
    @Autowired
    private final RedisTemplate<Object, Object> redisTemplate;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private UserReponsitory userReponsitory;

    private MessageService messageService;

    private UserService userService;

    public boolean generationOtp( String identifier, String action) {
        if (action.equals(SecurityConstants.ACTION_REG)){
            boolean existing = userReponsitory.existsByEmail(identifier);
            if (existing) {
                throw new EmailUsedException(identifier);
            }
            String redisKey = "REGISTER:PENDING:" + identifier;
            if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
                throw new RuntimeException("email not match or register timeout");
            }

        } else {
            User user = userReponsitory.findUserByEmail(identifier)
                    .orElseThrow(()-> new EmailNotExistException(identifier));
            User userCurrent=userService.getUserCurrent();
            if (!userCurrent.getId().equals(user.getId()))
            {
                throw new EmailNotMatchWithUserCurrent(userCurrent.getId(),identifier);
            }

        }
        SecureRandom random = new SecureRandom();
        String otp = String.valueOf(100000 + random.nextInt(900000));
        String hash = encoder.encode(otp);
        String key = "otp:" + action + ":" + identifier;
        Map<Object,Object> value=new HashMap<>();
        value.put("otpHash", hash);
        value.put("attempt", 0);
        value.put("maxAttempt", 5);

        redisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
        OtpMessage message=OtpMessage.builder()
                .action(action)
                .identifier(identifier)
                .otp(otp)
                .build();
        messageService.sendMessage("auth-otp-events",message);
         return true;

    }
    public boolean verifyOtp(String identifier, String action, String inputOtp) {

        String key = "otp:" + action + ":" + identifier;
        Map<String, Object> data = (Map<String, Object>) redisTemplate.opsForValue().get(key);

        if (data == null) {
            throw new RuntimeException("OTP expired or not found");
        }

        int attempt = (int) data.get("attempt");
        int maxAttempt = (int) data.get("maxAttempt");

        if (attempt >= maxAttempt) {
            redisTemplate.delete(key);
            throw new RuntimeException("Too many attempts");
        }

        String hash = (String) data.get("otpHash");

        if (!encoder.matches(inputOtp, hash)) {
            data.put("attempt", attempt + 1);
            redisTemplate.opsForValue().set(key, data);
            throw new RuntimeException("Invalid OTP");
        }
        redisTemplate.delete(key);
        return true;
    }
}
