package group.acm.bookshare;

import group.acm.bookshare.function.RSAUtils;
import group.acm.bookshare.function.TripleDESUtil;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONObject;

import com.google.zxing.WriterException;
import com.zxing.encoding.EncodingHandler;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;

public class GenerateQRCodeActivity extends Activity {
	
	HashMap<String, Object> map;
	
     

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_generate_qrcode);
		
		ImageView qrImgImageView = (ImageView) this.findViewById(R.id.gen_qr_image);
		try {
			
			//����  
	        Intent intent = getIntent();
	        JSONObject jsonObject = new JSONObject();
	        String contentString = intent.getStringExtra("ContentString");
	        
			//���ɹ�Կ��˽Կ 
			//map = RSAUtils.getKeys();
			//RSAPublicKey publicKey = (RSAPublicKey) map.get("public");  
			//RSAPrivateKey privateKey = (RSAPrivateKey) map.get("private");
			//ģ  
	        //String modulus = publicKey.getModulus().toString();  
	        //��Կָ��  
	        //String public_exponent = publicKey.getPublicExponent().toString();  
	        //˽Կָ��  
	        //String private_exponent = privateKey.getPrivateExponent().toString();
			

			//ģ 
			String modulus = "1342178680163122288334135585244412587094902706870597407686197582687531494789933"
					+ "85246009382974176202673425333737700725579112713332360668443009882107179274910464650698"
					+ "80929584502832391497033125849992285341218988289801502354908712667651437506714728106047"
					+ "3659828465972338681421950511104511172957148096843143165953";  
	        //��Կָ��  
	        String public_exponent = "65537"; 
	        //˽Կָ��  
	        String private_exponent = "2339397144132832735651743410630166925917431927091999052138400443572283"
	        		+ "178202452720852594994757182603931424366824473790820764"
	        		+ "643477052528532740106062695663575106992841755917852920"
	        		+ "811920437845960617055842926574173587617727934596713682"
	        		+ "832055832582548878334670846844359231907945702517343526"
	        		+ "3134780530125944042045";
	        
	       
	        
	        // TripleDES���ܲ���
	        String desKey1 = radomString(20);
	        String desKey2 = radomString(20);
	        Log.i("DES��Կ",desKey1);
	        Log.i("DES��Կ",desKey2);
	        TripleDESUtil desUtil = new TripleDESUtil(desKey1,desKey2);
	        contentString = desUtil.getEnc(contentString);
	        jsonObject.put("id", contentString);
	        
	        
	        //ʹ��ģ��ָ�����ɹ�Կ��˽Կ 
	       
	        RSAPublicKey pubKey = RSAUtils.getPublicKey(modulus, public_exponent);

	        

	        //����desKey1 
	        desKey1 = RSAUtils.encryptByPublicKey(desKey1, pubKey);
	        jsonObject.put("desKey1",desKey1);
	        //����desKey2
	        desKey2 = RSAUtils.encryptByPublicKey(desKey2, pubKey);
	        jsonObject.put("desKey2",desKey2);
	        
	        
	      //�����ַ������ɶ�ά��ͼƬ����ʾ�ڽ����ϣ��ڶ�������ΪͼƬ�Ĵ�С��400*400��
			Bitmap qrCodeBitmap;
			
			qrCodeBitmap = EncodingHandler.createQRCode(jsonObject.toString(), 500);
				
			qrImgImageView.setImageBitmap(qrCodeBitmap);
	        
	        //���ܺ������  
	        //contentString = RSAUtils.decryptByPrivateKey(contentString, priKey);
	        //Log.i("RSA����",contentString);
	        //TripleDESUtil desUtil2 = new TripleDESUtil(desKey1,desKey2);
	        //contentString = desUtil2.getDec(contentString);
	        //Log.i("DES����",contentString);
			
		}catch (WriterException e) {
			e.printStackTrace();
		}catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}
	
	
	public String radomString(int len) {

		Random rand = new Random();
		
		final int A = 'A', z = 'Z';
		StringBuilder sb = new StringBuilder();
		while(sb.length() < len){
			int number = rand.nextInt(z + 1);
			if(number >= A){
				sb.append((char)number);
			}
		}
					
		return sb.toString();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.generate_qrcode, menu);
		return true;
	}

}
