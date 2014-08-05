package group.acm.bookshare.function;


import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
 
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
 
import android.util.Base64;
 
/**
 * ʹ��DES���ܺͽ��ܹ�����
 *
 * @author Administrator
 *
 */
public class TripleDESUtil {
 
    private Key key1;// ��Կ��key1ֵ
    private Key key2;// ��Կ��key2ֵ
    private byte[] DESkey1;
    private byte[] DESkey2;
    private byte[] DESIV = { 0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xAB,
            (byte) 0xCD, (byte) 0xEF };
    private AlgorithmParameterSpec iv = null;// �����㷨�Ĳ����ӿ�
 
    public TripleDESUtil(String Key1,String Key2) {
        try {
            this.DESkey1 = Key1.getBytes("UTF-8");// ������Կ
            this.DESkey2 = Key2.getBytes("UTF-8");// ������Կ
            DESKeySpec keySpec1 = new DESKeySpec(DESkey1);// ������Կ����
            DESKeySpec keySpec2 = new DESKeySpec(DESkey2);// ������Կ����
            iv = new IvParameterSpec(DESIV);// ��������
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");// �����Կ����
            key1 = keyFactory.generateSecret(keySpec1);// �õ���Կ����
            key2 = keyFactory.generateSecret(keySpec2);// �õ���Կ����
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /**
     * ����String ���������������
     *
     * @param inputString
     *            �����ܵ�����
     * @return ���ܺ���ַ���
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
     * ����String �����������������
     *
     * @param inputString
     *            ��Ҫ���ܵ��ַ���
     * @return ���ܺ���ַ���
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
     * ������byte[]��������,byte[]�������
     *
     * @param bt
     *            �����ܵ��ֽ���
     * @return ���ܺ���ֽ���
     */
    private byte[] getEncCode(byte[] bt) {
        byte[] byteFina = null;
        Cipher cipher;
        try {
            // �õ�Cipherʵ��
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
     * ������byte[]��������,��byte[]�������
     *
     * @param bt
     *            �����ܵ��ֽ���
     * @return ���ܺ���ֽ���
     */
    private byte[] getDesCode(byte[] bt) {
        Cipher cipher;
        byte[] byteFina = null;
        try {
            // �õ�Cipherʵ��
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