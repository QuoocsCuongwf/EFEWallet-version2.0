package com.QuoocsCuongwf.EFEWallet.WalletService.e2e;

import com.QuoocsCuongwf.EFEWallet.WalletService.Repository.IdempotencyKeyRepository;
import com.QuoocsCuongwf.EFEWallet.WalletService.entity.IdempotencyKey;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.request.TransferRequest;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.TransferResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.service.TransactionService;
import com.QuoocsCuongwf.EFEWallet.WalletService.service.WalletService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class WalletTransferIdempotencyE2ETest {

    private static final String USER_ID = "11111111-1111-1111-1111-111111111111";
    private static final String IDEM_KEY = "idem-e2e-1";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(USER_ID, null, List.of())
        );

        AtomicReference<IdempotencyKey> store = new AtomicReference<>();

        when(idempotencyKeyRepository.findForUpdate(anyString(), anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(store.get()));

        when(idempotencyKeyRepository.save(any(IdempotencyKey.class)))
                .thenAnswer(invocation -> {
                    IdempotencyKey source = invocation.getArgument(0);
                    IdempotencyKey snapshot = IdempotencyKey.builder()
                            .idempotencyKey(source.getIdempotencyKey())
                            .userId(source.getUserId())
                            .requestHash(source.getRequestHash())
                            .status(source.getStatus())
                            .responseBody(source.getResponseBody())
                            .responseStatus(source.getResponseStatus())
                            .build();
                    store.set(snapshot);
                    return source;
                });

        when(transactionService.transfer(any(TransferRequest.class), eq(IDEM_KEY)))
                .thenReturn(TransferResponse.builder()
                        .transactionId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                        .status(TransactionStatus.SUCCESS)
                        .amount(new java.math.BigDecimal("10.5"))
                        .fromWalletId(UUID.fromString(USER_ID))
                        .toWalletId(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                        .createdAt(LocalDateTime.now())
                        .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReplayCachedResponseOnSecondRequestWithSameIdempotencyKey() throws Exception {
        String requestBody = """
                {
                  "toWalletAddress": "wallet-abc",
                  "amount": 10.5,
                  "description": "test",
                  "referenceCode": "ref-1"
                }
                """;

        mockMvc.perform(post("/api/v1/wallet/transfer")
                        .header("Idempotency-Key", IDEM_KEY)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionId").value("22222222-2222-2222-2222-222222222222"));

        mockMvc.perform(post("/api/v1/wallet/transfer")
                        .header("Idempotency-Key", IDEM_KEY)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).transfer(any(TransferRequest.class), eq(IDEM_KEY));
        verify(idempotencyKeyRepository, times(2)).findForUpdate(eq(IDEM_KEY), eq(USER_ID));
    }
}
