package com.glavsoft.rfb.protocol.auth;

import com.glavsoft.exceptions.CryptoException;
import com.glavsoft.exceptions.FatalException;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.rfb.CapabilityContainer;
import com.glavsoft.rfb.IPasswordRetriever;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class VncAuthentication extends AuthHandler {
    public VncAuthentication() {
    }

    public SecurityType getType() {
        return SecurityType.VNC_AUTHENTICATION;
    }

    public boolean authenticate(Reader reader, Writer writer, CapabilityContainer authCaps, IPasswordRetriever passwordRetriever) throws TransportException, FatalException {
        byte[] challenge = reader.readBytes(16);
        String password = passwordRetriever.getPassword();
        if (null == password) {
            return false;
        } else {
            byte[] key = new byte[8];
            System.arraycopy(password.getBytes(), 0, key, 0, Math.min(key.length, password.getBytes().length));
            writer.write(this.encrypt(challenge, key));
            return false;
        }
    }

    public byte[] encrypt(byte[] challenge, byte[] key) throws CryptoException {
        try {
            DESKeySpec desKeySpec = new DESKeySpec(this.mirrorBits(key));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            Cipher desCipher = Cipher.getInstance("DES/ECB/NoPadding");
            desCipher.init(1, secretKey);
            return desCipher.doFinal(challenge);
        } catch (NoSuchAlgorithmException var7) {
            throw new CryptoException("Cannot encrypt challenge", var7);
        } catch (NoSuchPaddingException var8) {
            throw new CryptoException("Cannot encrypt challenge", var8);
        } catch (IllegalBlockSizeException var9) {
            throw new CryptoException("Cannot encrypt challenge", var9);
        } catch (BadPaddingException var10) {
            throw new CryptoException("Cannot encrypt challenge", var10);
        } catch (InvalidKeyException var11) {
            throw new CryptoException("Cannot encrypt challenge", var11);
        } catch (InvalidKeySpecException var12) {
            throw new CryptoException("Cannot encrypt challenge", var12);
        }
    }

    private byte[] mirrorBits(byte[] k) {
        byte[] key = new byte[8];

        for (int i = 0; i < 8; ++i) {
            byte s = k[i];
            s = (byte) (s >> 1 & 85 | s << 1 & 170);
            s = (byte) (s >> 2 & 51 | s << 2 & 204);
            s = (byte) (s >> 4 & 15 | s << 4 & 240);
            key[i] = s;
        }

        return key;
    }
}
