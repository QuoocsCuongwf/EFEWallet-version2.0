package com.QuoocsCuongwf.EFEWallet.WalletService.payload.KafkaMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TransferMessage {
    private UUID transactionId;
    private String walletSend;
    private String walletRecive;
    private BigDecimal amount;
    private LocalDateTime time;
    private String description;
}
