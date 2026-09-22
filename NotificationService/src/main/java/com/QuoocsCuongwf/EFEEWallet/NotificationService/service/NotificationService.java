package com.QuoocsCuongwf.EFEEWallet.NotificationService.service;

import com.QuoocsCuongwf.EFEEWallet.NotificationService.dto.OtpMessage;
import com.QuoocsCuongwf.EFEEWallet.NotificationService.dto.TransferMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private KafkaTemplate<Object,Object> kafkaTemplate;
    private final JavaMailSender mailSender;

    @KafkaListener(
            topics = "wallet-transactions",
            groupId = "notification-group"
    )
    public void consume(String message) {
        System.out.println("Received message: " + message);
    }
    @KafkaListener(topics = "wallet-transactions", groupId = "notification-group")
    public void consumeTransactionEvent(TransferMessage message) {
        log.info("🔔 [NOTIFICATION EVENT] Nhận thành công gói tin giao dịch: {}", message.getTransactionId());
        log.info("Ví gửi: {} -> Ví nhận: {} | Số tiền: {} EFE",
                message.getWalletSend(), message.getWalletRecive(), message.getAmount());
    }
    @KafkaListener(topics = "auth-otp-events", groupId = "notification-group")
    public void sendOtp(String messageJson) {
        ObjectMapper objectMapper=new ObjectMapper();
        try {
            OtpMessage otpMessage = objectMapper.readValue(messageJson, OtpMessage.class);
            String action = otpMessage.getAction();
            if ("REGISTER".equals(action)) {
                otpRegister(otpMessage.getOtp(), otpMessage.getIdentifier());
            } else if ("TRANSFER".equals(action)) {
                otpTransfer(otpMessage.getOtp(), otpMessage.getIdentifier());
            } else {
                log.warn("Unsupported OTP action: {}", action);
            }
        } catch (Exception e) {
            log.error("Lỗi tự map JSON: " + e.getMessage());
        }

    }
    public void otpRegister(String otp, String identifier){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(identifier);
        message.setSubject("EFEWallet - Mã xác thực đăng ký tài khoản");

        String mailContent = "Chào bạn,\n\n"
                + "Bạn đang thực hiện đăng ký tài khoản trên hệ thống ví điện tử EFEWallet.\n"
                + "Mã xác thực (OTP) của bạn là: " + otp + "\n\n"
                + "Mã này có hiệu lực trong vòng 5 phút. Vui lòng tuyệt đối KHÔNG chia sẻ mã này cho bất kỳ ai, kể cả nhân viên hỗ trợ.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ EFEWallet.";
        message.setText(mailContent);
        mailSender.send(message);
        log.info(" Successfully sent otp to mail "+ identifier);
    }

    public void otpTransfer(String otp, String identifier) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(identifier);
        message.setSubject("EFEWallet - Mã xác thực giao dịch chuyển tiền");

        String mailContent = "Chào bạn,\n\n"
                + "Bạn đang thực hiện giao dịch chuyển tiền trên hệ thống ví điện tử EFEWallet.\n"
                + "Mã xác thực (OTP) của bạn là: " + otp + "\n\n"
                + "Mã này có hiệu lực trong vòng 5 phút. Vui lòng tuyệt đối KHÔNG chia sẻ mã này cho bất kỳ ai, kể cả nhân viên hỗ trợ.\n"
                + "Nếu bạn không thực hiện giao dịch này, hãy bỏ qua email và đổi mật khẩu ngay.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ EFEWallet.";
        message.setText(mailContent);
        mailSender.send(message);
        log.info("Successfully sent transfer OTP to mail {}", identifier);
    }
}
