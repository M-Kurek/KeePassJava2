package org.linguafranca.pwdb.security;

import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;

import java.security.MessageDigest;
import java.util.UUID;

import static org.linguafranca.pwdb.security.Encryption.getSha256MessageDigestInstance;

/**
 * @author jo
 */
public class Aes {

    public static final UUID KDF = UUID.fromString("C9D9F39A-628A-4460-BF74-0D08C18A4FEA");

    public static class KdfKeys {
        public static final String ParamRounds = "R"; // UInt64
        public static final String ParamSeed = "S"; // Byte[32]
    }

    public static PaddedBufferedBlockCipher getCipher() {
        return new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
    }
    /**
     * Create a final key from the parameters passed
     */
    public static byte[] getFinalKeyDigest(byte[] key, byte[] masterSeed, byte[] transformSeed, long transformRounds) {

        AESEngine engine = new AESEngine();
        engine.init(true, new KeyParameter(transformSeed));

        // copy input key
        byte[] transformedKey = new byte[key.length];
        System.arraycopy(key, 0, transformedKey, 0, transformedKey.length);

        // transform rounds times
        for (long rounds = 0; rounds < transformRounds; rounds++) {
            engine.processBlock(transformedKey, 0, transformedKey, 0);
            engine.processBlock(transformedKey, 16, transformedKey, 16);
        }

        MessageDigest md = getSha256MessageDigestInstance();
        byte[] transformedKeyDigest = md.digest(transformedKey);

        md.update(masterSeed);
        return md.digest(transformedKeyDigest);
    }
}