package io.openur.domain.user.exception;

public class InvalidSignatureException extends RuntimeException {

    public InvalidSignatureException(String address, String nonce) {
        super("Invalid signature of wallet address: " + address + " with nonce: " + nonce);
    }
}
