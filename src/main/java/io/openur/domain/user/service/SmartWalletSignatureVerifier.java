package io.openur.domain.user.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

@Component
public class SmartWalletSignatureVerifier {

    private static final int SIGNATURE_BYTE_LENGTH = 65;

    public boolean verify(String walletAddress, String message, String signature) {
        try {
            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
            if (signatureBytes.length != SIGNATURE_BYTE_LENGTH) {
                return false;
            }

            byte v = signatureBytes[64];
            if (v < 27) {
                v += 27;
            }

            byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
            byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);
            Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
            BigInteger publicKey = Sign.signedPrefixedMessageToKey(
                message.getBytes(StandardCharsets.UTF_8),
                signatureData
            );
            String recoveredAddress = "0x" + Keys.getAddress(publicKey);

            return recoveredAddress.equalsIgnoreCase(walletAddress);
        } catch (IllegalArgumentException | SignatureException e) {
            return false;
        }
    }
}
