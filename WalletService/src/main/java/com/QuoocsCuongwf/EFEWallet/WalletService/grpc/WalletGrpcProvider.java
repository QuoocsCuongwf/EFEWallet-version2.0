package com.QuoocsCuongwf.EFEWallet.WalletService.grpc;

import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.BalanceResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.WalletResponse;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;


import java.time.ZoneOffset;
import java.util.UUID;


@GrpcService
@RequiredArgsConstructor
public class WalletGrpcProvider extends WalletServiceGrpc.WalletServiceImplBase {

    private final com.QuoocsCuongwf.EFEWallet.WalletService.service.WalletService walletService;

    @Override
    public void generation(GenReq request, StreamObserver<GenRes> responseObserver) {
        try {
            // Gọi service xử lý logic
            WalletResponse result = walletService.generation(UUID.fromString(request.getUserId()));

            // Build response (Đảm bảo các trường result.get... không bị null)
            GenRes.Builder responseBuilder = GenRes.newBuilder()
                    .setUserId(request.getUserId())
                    .setWalletId(result.getWalletId().toString())
                    .setWalletAddress(result.getWalletAddress())
                    .setStatus(result.getStatus().name())
                    .setBalance(result.getBalance() != null ? result.getBalance().toPlainString() : "0");

            if (result.getCreateAt() != null) {
                responseBuilder.setCreateAt(Timestamp.newBuilder()
                        .setSeconds(result.getCreateAt().toEpochSecond(ZoneOffset.UTC))
                        .setNanos(result.getCreateAt().getNano())
                        .build());
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            // Trả về lỗi chi tiết để AuthService biết chuyện gì đang xảy ra
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Lỗi tại Wallet Provider: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void balance(BalReq request, StreamObserver<BalRes> responseObserver) {
        BalanceResponse result = walletService.balance(UUID.fromString(request.getWalletId()));

        BalRes response = BalRes.newBuilder()
                .setBalance(result.getBalance().toPlainString())
                .setTime(Timestamp.newBuilder()
                        .setSeconds(result.getTime().toEpochSecond(ZoneOffset.UTC))
                        .setNanos(result.getTime().getNano())
                        .build())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}