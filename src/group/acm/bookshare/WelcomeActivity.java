package group.acm.bookshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;

public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//»•µÙ±ÍÃ‚¿∏
		
		setContentView(R.layout.activity_welcome);
		 new Handler().postDelayed(new Runnable() {  
			 @Override
			 public void run() {
				 Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class); 
				 startActivity(intent);
				 WelcomeActivity.this.finish();  
				 }  
			 }, 2000);  

	}
	
	public void Enter(View v)
	{
		 Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class); 
		 startActivity(intent);
		 WelcomeActivity.this.finish();  
	}

}
