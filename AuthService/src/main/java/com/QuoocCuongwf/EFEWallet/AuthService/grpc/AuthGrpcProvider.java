package com.QuoocCuongwf.EFEWallet.AuthService.grpc;

import com.QuoocCuongwf.EFEWallet.AuthService.service.AuthService;
import com.QuoocsCuongwf.EFEWallet.AuthService.grpc.AuthServiceGrpc;
import com.QuoocsCuongwf.EFEWallet.AuthService.grpc.VerifyTokenRequest;
import com.QuoocsCuongwf.EFEWallet.AuthService.grpc.VerifyTokenResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AuthGrpcProvider extends AuthServiceGrpc.AuthServiceImplBase {
    private final AuthService authService;
    @Override
    public void verify(VerifyTokenRequest request, StreamObserver<VerifyTokenResponse> observer) {
        try{
            Boolean isSuccess = authService.verifyTransactionToken(request.getToken(),request.getUserId());
            String message = isSuccess ? "Token available" : "Token unavailable";
            VerifyTokenResponse.Builder responseBulder = VerifyTokenResponse.newBuilder()
                    .setIsValid(isSuccess)
                    .setMessage(message);
            observer.onNext(responseBulder.build());
            observer.onCompleted();

        } catch (Exception e) {
            throw new RuntimeException("Grpc error "+ e);
        }

    }
    
}
