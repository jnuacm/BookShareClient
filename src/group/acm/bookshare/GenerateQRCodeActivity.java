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
	        
	        // TripleDES���ܲ���
	        String desKey1 = "BIOGXVLBJLOQODARLCJB";
	        String desKey2 = "ORQCSRROXMVQXTIKSRGZ";

	        String ran = radomString(5);
	        Log.i("DES��Կ",desKey1);
	        Log.i("DES��Կ",desKey2);
	        TripleDESUtil desUtil = new TripleDESUtil(desKey1,desKey2);
	        contentString = desUtil.getEnc(contentString);
	        jsonObject.put("id", contentString);
	        jsonObject.put("random", ran);
	        
	        
	      //�����ַ������ɶ�ά��ͼƬ����ʾ�ڽ����ϣ��ڶ�������ΪͼƬ�Ĵ�С��400*400��
			Bitmap qrCodeBitmap;
			
			qrCodeBitmap = EncodingHandler.createQRCode(jsonObject.toString(), 300);
				
			qrImgImageView.setImageBitmap(qrCodeBitmap);
	        
			
		}catch (WriterException e) {
			e.printStackTrace();
		}catch (Exception e) {
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
