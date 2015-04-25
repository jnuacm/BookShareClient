package group.acm.bookshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import group.acm.bookshare.function.http.UrlStringFactory;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_welcome);
        // FIXME : 暂时不自动跳过该界面
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
//                startActivity(intent);
//                WelcomeActivity.this.finish();
//            }
//        }, 2000);

    }

    public void Enter(View v) {
        // FIXME:暂时获取ip
        EditText input = (EditText) findViewById(R.id.input_host);
        UrlStringFactory.URL_HOST = input.getText().toString();
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        WelcomeActivity.this.finish();
    }

}
