package com.QuoocsCuongwf.EFEWallet.WalletService.grpc;

import com.QuoocsCuongwf.EFEWallet.AuthService.grpc.AuthServiceGrpc;
import com.QuoocsCuongwf.EFEWallet.AuthService.grpc.VerifyTokenRequest;
import com.QuoocsCuongwf.EFEWallet.AuthService.grpc.VerifyTokenResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletGrpcClient {
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub;
    @Value("${spring.grpc.client.wallet-service.negotiation-type:plaintext}")
    private String negotiationType;
    @Value("${spring.grpc.client.auth-service.address:static://localhost:9091}")
    private String authServiceAddress;
    private ManagedChannel authChannel;
    @PostConstruct
    void initAuthStub() {
        String endpoint = authServiceAddress.replace("static://", "");
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(endpoint);
        if ("plaintext".equalsIgnoreCase(negotiationType)) {
            builder.usePlaintext();
        }
        authChannel = builder.build();
        authServiceBlockingStub = AuthServiceGrpc.newBlockingStub(authChannel);
    }
    public VerifyTokenResponse verifyToken(VerifyTokenRequest verifyTokenRequest) {
        if (authServiceBlockingStub == null) {
            throw new IllegalStateException("gRPC auth stub is not initialized");
        }
        try {
            return authServiceBlockingStub.verify(verifyTokenRequest);
        } catch (StatusRuntimeException e) {
            System.err.println("gRPC Call Failed: " + e.getStatus());
            throw e;
        }
    }
}
