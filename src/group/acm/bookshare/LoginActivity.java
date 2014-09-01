package group.acm.bookshare;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.LoginUserNameAdapter;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.User;
import group.acm.bookshare.util.Utils;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements Callback{
	private User localUser;
	private PopupWindow selectPopupWindow= null;
	private LoginUserNameAdapter userNameAdapter = null;  
	private ArrayList<String> datas = new ArrayList<String>();
	private HashMap<String,String> userToPwd = new HashMap<String , String>();
	private LinearLayout USERNAME_linear_layout;  
	private int pwidth;   
	private EditText username;  
	private ImageView down_pull_image;;  
	private ListView listView = null;   
	private Handler handler;
	private JSONArray accounts;
	private boolean flag = false;  

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		LocalApp localapp = (LocalApp) getApplication();
		localUser = localapp.getUser();
		//fillInInfo();
	}

	private void fillInInfo() {
		SharedPreferences info = this.getSharedPreferences("user_info",
				Context.MODE_PRIVATE);
		((TextView) findViewById(R.id.USERNAME)).setText(info.getString(
				"username", ""));

		((TextView) findViewById(R.id.PASSWORD)).setText(info.getString(
				"password", ""));
	}
	
	private void recordInfo(String username, String password) {
		SharedPreferences info = this.getSharedPreferences("user_info",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = info.edit();
		editor.putString("username", username);
		editor.putString("password", password);
		editor.commit();
	}

	public void Login(View v) { // 登录回调函数
		if (Utils.isQuickClick())
			return;
		String username, password;
		username = ((TextView) findViewById(R.id.USERNAME)).getText()
				.toString();
		password = ((TextView) findViewById(R.id.PASSWORD)).getText()
				.toString();

		//recordInfo(username, password);
		
		userToPwd.put(username, password);
		updata_accounts();
		initDatas();
		localUser.setUser(username, password);
		localUser.login(new LoginHandler());
	}

	private void updata_accounts() {
		accounts = new JSONArray();
		Iterator<Entry<String, String>> iter = userToPwd.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<String, String> entry = (Entry<String, String>) iter.next();
			JSONObject usr = new JSONObject();
			try {
				usr.put("username",(String)entry.getKey());
				usr.put("password",(String)entry.getValue());
				accounts.put(usr);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		writeFile("accounts.txt",accounts.toString());
	}

	@SuppressLint("HandlerLeak")
	private class LoginHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetAccess.NETMSG_BEFORE:
				findViewById(R.id.loginProgressBar).setVisibility(View.VISIBLE);
				break;
			case NetAccess.NETMSG_AFTER:
				showResponse(msg.getData());
				break;
			case NetAccess.NETMSG_ERROR:
				Toast.makeText(LoginActivity.this,
						msg.getData().getString("error"), Toast.LENGTH_LONG)
						.show();
				break;
			}
		}
	}

	public void Register(View v) { // 注册回调函数
		if (Utils.isQuickClick())
			return;
		Intent intent = new Intent();
		intent.setClass(this, RegisterActivity.class);
		startActivity(intent);
	}

	public void showResponse(Bundle data) {
		int status = data.getInt("status");
		if (status == NetAccess.STATUS_SUCCESS) {
			String response = data.getString("response");
			localUser.clearBookData();
			localUser.addBookDataToList(response);
			localUser.addFriendDataToList(response);
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		} else if (status == NetAccess.STATUS_ERROR) {
			findViewById(R.id.loginProgressBar).setVisibility(View.INVISIBLE);
			Toast.makeText(LoginActivity.this,
					this.getString(R.string.login_error), Toast.LENGTH_LONG)
					.show();
		}
	}
	
	
	/**  
     * 没有在onCreate方法中调用initWedget()，而是在onWindowFocusChanged方法中调用，  
     * 是因为initWedget()中需要获取PopupWindow浮动下拉框依附的组件宽度，在onCreate方法中是无法获取到该宽度的  
     */  
    @Override  
    public void onWindowFocusChanged(boolean hasFocus) {  
        super.onWindowFocusChanged(hasFocus);  
        while(!flag){  
            initWedget();  
            flag = true;  
        }  
          
    }  
      
    /**  
     * 初始化界面控件  
     */  
    private void initWedget(){  
        //初始化Handler,用来处理消息  
        handler = new Handler(LoginActivity.this);  
          
        //初始化界面组件  
        USERNAME_linear_layout = (LinearLayout)findViewById(R.id.USERNAME_linear_layout);  
        username = (EditText)findViewById(R.id.USERNAME);  
        down_pull_image = (ImageView)findViewById(R.id.btn_select);  
          
          
        //获取下拉框依附的组件宽度  
        int width = USERNAME_linear_layout.getWidth();  
        pwidth = width;  
          
        //设置点击下拉箭头图片事件，点击弹出PopupWindow浮动下拉框  
        down_pull_image.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                if(flag){  
                    //显示PopupWindow窗口  
                    popupWindwShowing();  
                }  
            }  
        });
        
        initPopuWindow(); 
    }  
  
    /**  
     * 初始化填充Adapter所用List数据  
     */  
    private void initDatas(){  
          
         datas.clear();  
         try {
        	 String str = readFile("accounts.txt");
        	 if(str.equals(""))
        		 accounts = new JSONArray();
        	 else
        		 accounts = new JSONArray(str);
        	 for (int i = 0; i < accounts.length(); i++) {
 				JSONObject account = accounts.getJSONObject(i);
 				userToPwd.put(account.getString("username"),account.getString("password"));
 				Log.i(account.getString("username"),account.getString("password"));
 		        datas.add(account.getString("username"));
        	 }
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
    }  
      
     /**  
     * 初始化PopupWindow  
     */   
   
	@SuppressWarnings("deprecation")
	@SuppressLint({ "InlinedApi", "InflateParams" })
	private void initPopuWindow(){ 
          
        initDatas();  
          
        View loginwindow = (View)this.getLayoutInflater().inflate(R.layout.user_name_list, null);   
        listView = (ListView) loginwindow.findViewById(R.id.user_name_list);
        
        userNameAdapter = new LoginUserNameAdapter(this, handler,datas);
        listView.setAdapter(userNameAdapter);   
          
        selectPopupWindow = new PopupWindow(loginwindow, pwidth,LayoutParams.WRAP_CONTENT, true);   
          
        selectPopupWindow.setOutsideTouchable(true);
        selectPopupWindow.setBackgroundDrawable(new BitmapDrawable());    
    }   
	
    /**  
     * 显示PopupWindow窗口  
     *   
     * @param popupwindow  
     */   
    public void popupWindwShowing() {   
       selectPopupWindow.showAsDropDown(USERNAME_linear_layout,0,-3);   
    }   
       
    /**  
     * PopupWindow消失  
     */   
    public void dismiss(){   
        selectPopupWindow.dismiss();   
    }  

    //处理下拉框传递过来的信息
	@Override
	public boolean handleMessage(Message message) {
		// TODO Auto-generated method stub
		Bundle data = message.getData();  
        switch(message.what){  
            case 1:  
                //选中下拉项，下拉框消失  
                int selIndex = data.getInt("selIndex");  
                username.setText(datas.get(selIndex));
                ((TextView) findViewById(R.id.PASSWORD)).setText(userToPwd.get(datas.get(selIndex)));
                dismiss();  
                break;  
            case 2:  
                //移除下拉项数据  
                int delIndex = data.getInt("delIndex");
                userToPwd.remove(datas.get(delIndex));
                datas.remove(delIndex);
                updata_accounts();
                //刷新下拉列表  
                userNameAdapter.notifyDataSetChanged();  
                break;  
        }  
		return false;
	}
	
    //写data中文件的数据  
    public void writeFile(String fileName,String writestr){   
      try{  
            FileOutputStream fout =openFileOutput(fileName, MODE_PRIVATE);
            byte [] bytes = writestr.getBytes();
            fout.write(bytes);
            fout.close();   
          }catch(Exception e){   
            e.printStackTrace();   
           }   
    }   
      
    //读data中文件的数据 
    public String readFile(String fileName){   
      String res="";   
      try{   
             FileInputStream fin = openFileInput(fileName);   
             int length = fin.available();   
             byte [] buffer = new byte[length];   
             fin.read(buffer);       
             res = EncodingUtils.getString(buffer, "UTF-8");   
             fin.close();       
         }catch(Exception e){   
             e.printStackTrace();   
         }
      	 Log.i("accounts.txt",res);
         return res;   
      
    }     
	
}
