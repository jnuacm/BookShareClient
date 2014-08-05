package group.acm.bookshare.function;


import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
 
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
 
import android.util.Base64;
 
/**
 * 使用DES加密和解密工具类
 *
 * @author Administrator
 *
 */
public class TripleDESUtil {
 
    private Key key1;// 密钥的key1值
    private Key key2;// 密钥的key2值
    private byte[] DESkey1;
    private byte[] DESkey2;
    private byte[] DESIV = { 0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xAB,
            (byte) 0xCD, (byte) 0xEF };
    private AlgorithmParameterSpec iv = null;// 加密算法的参数接口
 
    public TripleDESUtil(String Key1,String Key2) {
        try {
            this.DESkey1 = Key1.getBytes("UTF-8");// 设置密钥
            this.DESkey2 = Key2.getBytes("UTF-8");// 设置密钥
            DESKeySpec keySpec1 = new DESKeySpec(DESkey1);// 设置密钥参数
            DESKeySpec keySpec2 = new DESKeySpec(DESkey2);// 设置密钥参数
            iv = new IvParameterSpec(DESIV);// 设置向量
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");// 获得密钥工厂
            key1 = keyFactory.generateSecret(keySpec1);// 得到密钥对象
            key2 = keyFactory.generateSecret(keySpec2);// 得到密钥对象
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /**
     * 加密String 明文输入密文输出
     *
     * @param inputString
     *            待加密的明文
     * @return 加密后的字符串
     */
    public String getEnc(String inputString) {
        byte[] byteMi = null;
        byte[] byteMing = null;
        String outputString = "";
        try {
            byteMing = inputString.getBytes("UTF-8");
            byteMi = this.getEncCode(byteMing);
            byte[] temp = Base64.encode(byteMi, Base64.DEFAULT);
            outputString = new String(temp);
        } catch (Exception e) {
        } finally {
            byteMing = null;
            byteMi = null;
        }
        return outputString;
    }
 
    /**
     * 解密String 以密文输入明文输出
     *
     * @param inputString
     *            需要解密的字符串
     * @return 解密后的字符串
     */
    public String getDec(String inputString) {
        byte[] byteMing = null;
        byte[] byteMi = null;
        String strMing = "";
        try {
            byteMi = Base64.decode(inputString.getBytes(), Base64.DEFAULT);
            byteMing = this.getDesCode(byteMi);
            strMing = new String(byteMing, "UTF8");
        } catch (Exception e) {
        } finally {
            byteMing = null;
            byteMi = null;
        }
        return strMing;
    }
 
    /**
     * 加密以byte[]明文输入,byte[]密文输出
     *
     * @param bt
     *            待加密的字节码
     * @return 加密后的字节码
     */
    private byte[] getEncCode(byte[] bt) {
        byte[] byteFina = null;
        Cipher cipher;
        try {
            // 得到Cipher实例
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key1, iv);
            bt = cipher.doFinal(bt);
            cipher.init(Cipher.ENCRYPT_MODE, key2, iv);
            bt = cipher.doFinal(bt);
            cipher.init(Cipher.ENCRYPT_MODE, key1, iv);
            byteFina = cipher.doFinal(bt);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cipher = null;
        }
        return byteFina;
    }
 
    /**
     * 解密以byte[]密文输入,以byte[]明文输出
     *
     * @param bt
     *            待解密的字节码
     * @return 解密后的字节码
     */
    private byte[] getDesCode(byte[] bt) {
        Cipher cipher;
        byte[] byteFina = null;
        try {
            // 得到Cipher实例
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key1, iv);
            bt = cipher.doFinal(bt);
            cipher.init(Cipher.DECRYPT_MODE, key2, iv);
            bt = cipher.doFinal(bt);
            cipher.init(Cipher.DECRYPT_MODE, key1, iv);
            byteFina = cipher.doFinal(bt);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cipher = null;
        }
        return byteFina;
    }
}