package com.QuoocCuongwf.EFEWallet.AuthService.service;

import com.QuoocCuongwf.EFEWallet.AuthService.config.SecurityConstants;
import com.QuoocCuongwf.EFEWallet.AuthService.entity.Roles;
import com.QuoocCuongwf.EFEWallet.AuthService.entity.User;
import com.QuoocCuongwf.EFEWallet.AuthService.enums.WalletStatus;
import com.QuoocCuongwf.EFEWallet.AuthService.exception.EmailNotExistException;
import com.QuoocCuongwf.EFEWallet.AuthService.exception.RolesNotFoundException;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.dto.PendingRegister;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.dto.WalletDto;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.LoginRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.RegisterRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.TransferRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.response.LoginResponse;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.response.RegisterResponse;
import com.QuoocCuongwf.EFEWallet.AuthService.repository.RolesReponsitory;
import com.QuoocCuongwf.EFEWallet.AuthService.repository.UserReponsitory;
import com.QuoocCuongwf.EFEWallet.AuthService.security.CustomUserDetails;
import com.QuoocCuongwf.EFEWallet.AuthService.security.JwtService;
import com.QuoocsCuongwf.EFEWallet.WalletService.grpc.WalletServiceGrpc;
import com.QuoocsCuongwf.EFEWallet.WalletService.grpc.GenReq;
import com.QuoocsCuongwf.EFEWallet.WalletService.grpc.GenRes;
import com.google.common.hash.Hashing;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import tools.jackson.core.io.BigDecimalParser;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserReponsitory userReponsitory;
    private final RolesReponsitory rolesReponsitory;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final RedisTemplate<Object,Object> redisTemplate;
    @Value("${app.security.internal-secret-key}")
    private String internalSecretKey;

    @Value("${spring.grpc.client.wallet-service.address:static://localhost:9090}")
    private String walletServiceAddress;

    @Value("${spring.grpc.client.wallet-service.negotiation-type:plaintext}")
    private String negotiationType;

    private ManagedChannel walletChannel;
    private WalletServiceGrpc.WalletServiceBlockingStub walletStub;

    @PostConstruct
    void initWalletStub() {
        String endpoint = walletServiceAddress.replace("static://", "");
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(endpoint);
        if ("plaintext".equalsIgnoreCase(negotiationType)) {
            builder.usePlaintext();
        }
        walletChannel = builder.build();
        walletStub = WalletServiceGrpc.newBlockingStub(walletChannel);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userReponsitory.findUserByEmail(request.getEmail()).orElseThrow(()->new EmailNotExistException(request.getEmail()));
        System.out.println(request.getEmail());
        System.out.println(user.getPassword());
        System.out.println(passwordEncoder.encode(request.getPassword()));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println(request.getPassword() + " is not match");
            throw new BadCredentialsException("Invalid credentials");
        } else {
            System.out.println(request.getPassword() + " is match");
        }
        if(!user.isEnabled()) {
            throw new DisabledException("User is disable");
        }

        CustomUserDetails details = new CustomUserDetails(user);
        System.out.println(user.getId());
        String token = jwtService.generateToken(details);
        String refeshToken = jwtService.generateToken(details);
        return new LoginResponse(token, refeshToken, jwtService.getExpirationMs());
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        boolean existing = userReponsitory.existsByEmail(request.getEmail());
        if (existing) {
            throw new IllegalArgumentException("Email already in use");
        }


        PendingRegister pending =
                new PendingRegister(
                        request.getEmail(),
                        passwordEncoder.encode(
                                request.getPassword()
                        ),
                        request.getFirstName(),
                        request.getLastName()
                );
        String key = "REGISTER:PENDING:" + request.getEmail();


        redisTemplate.opsForValue().set(key,pending,5, TimeUnit.MINUTES);

        return new RegisterResponse<>(
                null, // ID là null vì chưa lưu xuống Database
                request.getEmail(),
                "Đăng ký bước 1 thành công. Vui lòng kiểm tra Email để lấy mã OTP.",
                null
        );
    }
    @Transactional
    public void verifyAndSaveRegisterUser(String identifier, String otp){
        otpService.verifyOtp(identifier, SecurityConstants.ACTION_REG,otp);

        String redisKey = "REGISTER:PENDING:" + identifier;
        PendingRegister pending =
                (PendingRegister)
                        redisTemplate
                                .opsForValue()
                                .get(redisKey);
        Roles role = rolesReponsitory.findRolesByName("USER")
                .orElseThrow(() -> new RolesNotFoundException("USER"));

        User user = User.builder()
                .email(pending.email())
                .password(pending.password())
                .firstName(pending.firstName())
                .lastName(pending.lastName())
                .role(Set.of(role))
                .enabled(true)
                .build();
        if (user == null) {
            throw new RuntimeException("Register session expired!");
        }
        user.setEnabled(true);
        userReponsitory.save(user);
        GenReq genReq= GenReq.newBuilder().setUserId(user.getId().toString()).build();
        StreamObserver<GenRes> streamObserver=new StreamObserver<GenRes>() {
            @Override
            public void onNext(GenRes genRes) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        };
        log.info(walletStub.generation(genReq).toString());

        redisTemplate.delete(redisKey);
    }
    @Transactional
    public String verifyOtpTransacsion(String identifier, String otp, TransferRequest transfer){
        String tranSactionVerifiedToken;
        otpService.verifyOtp(identifier,SecurityConstants.ACTION_TRANSFER,otp);
        UUID userId = jwtService.getCurrrentUser().getId();
        String redisKey=userId.toString();
        String rawData=userId.toString() + transfer.getToWalletAddress() + transfer.getAmount();
        String hashValue = Hashing.hmacSha256(internalSecretKey.getBytes(StandardCharsets.UTF_8))
                .hashString(rawData, StandardCharsets.UTF_8)
                .toString();
        redisTemplate.opsForValue().set(redisKey,hashValue,5,TimeUnit.MINUTES);
        return hashValue;
    }
    public boolean verifyTransactionToken(String token, String userId){
        Object cachedToken = redisTemplate.opsForValue().get(userId);
        if (cachedToken == null || !cachedToken.toString().equals(token)){
            return false;
        }
        redisTemplate.delete(userId);
        return true;
    }
    public void logout(){
       String token=jwtService.getAccessToken();
        jwtService.addBlackListToken(token);
    }

}
