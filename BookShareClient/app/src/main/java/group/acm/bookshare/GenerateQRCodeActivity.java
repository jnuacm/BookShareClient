package group.acm.bookshare;

import group.acm.bookshare.function.TripleDESUtil;

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

        ImageView qrImgImageView = (ImageView) this
                .findViewById(R.id.gen_qr_image);
        try {

            // ---------------取消了公钥加密--------------
			/*
			 * // 模 String modulus =
			 * "1095908257922794133899641353345223659509198870727619" +
			 * "84662925904428324513840234320762060769240802226180024972009" +
			 * "23198993652791393424108233803797411622424439308380949251312" +
			 * "11865875997007206462274689115480894523234426618616006872199" +
			 * "90868747713338468835352980211896324717589079982458697178916" +
			 * "072092088274807099109"; // 公钥指数 String public_exponent =
			 * "65537";
			 * 
			 * RSAPublicKey pubKey = RSAUtils.getPublicKey(modulus,
			 * public_exponent);
			 */

            // 明文
            Intent intent = getIntent();
            JSONObject jsonObject = new JSONObject();
            String contentString = intent.getStringExtra("ContentString");

            // TripleDES加密部分
            String desKey1 = "ASDASDEFRGRHTTGRGEFWSP";
            String desKey2 = "IHDASHKDSJFSDKLJFKOEFJ";

            TripleDESUtil desUtil = new TripleDESUtil(desKey1, desKey2);
            contentString = desUtil.getEnc(contentString);
            jsonObject.put("contentString", contentString);

            // ---------------取消了公钥加密--------------
			/*
			 * desKey1 = RSAUtils.encryptByPublicKey(desKey1, pubKey); desKey2 =
			 * RSAUtils.encryptByPublicKey(desKey2, pubKey);
			 * 
			 * jsonObject.put("desKey1", desKey1); jsonObject.put("desKey2",
			 * desKey2);
			 */

            // 根据字符串生成二维码图片并显示在界面上，第二个参数为图片的大小（500*500）
            Bitmap qrCodeBitmap;
            Log.i("jsonObject.toString() = ", jsonObject.toString());
            qrCodeBitmap = EncodingHandler.createQRCode(jsonObject.toString(),
                    500);
            qrImgImageView.setImageBitmap(qrCodeBitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setResult(RESULT_OK, null);
    }

    public String radomString(int len) {

        Random rand = new Random();

        final int A = 'A', z = 'Z';
        StringBuilder sb = new StringBuilder();
        while (sb.length() < len) {
            int number = rand.nextInt(z + 1);
            if (number >= A) {
                sb.append((char) number);
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
