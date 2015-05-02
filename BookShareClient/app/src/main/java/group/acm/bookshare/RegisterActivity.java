package group.acm.bookshare;

import group.acm.bookshare.function.Friend;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess.NetThread;
import group.acm.bookshare.util.Utils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
    private User localUser;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        localUser = ((LocalApp) getApplication()).getUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register, menu);
        return true;
    }

    @SuppressLint("HandlerLeak")
    public void confirm(View v) { // 确定回调函数
        String username, password, email, area;

        EditText edittext = (EditText) findViewById(R.id.registerusername);
        username = edittext.getText().toString();

        edittext = (EditText) findViewById(R.id.registerpassword);
        password = edittext.getText().toString();

        edittext = (EditText) findViewById(R.id.registeremail);
        email = edittext.getText().toString();

        edittext = (EditText) findViewById(R.id.registerarea);
        area = edittext.getText().toString();

        CheckBox check = (CheckBox) findViewById(R.id.checkbox_group);

        if (!(username.length() > 0 && password.length() > 0 && email.length() > 0)) {
            Toast.makeText(this, "请输入完整信息", Toast.LENGTH_LONG).show();
            return;
        }
        if (area.length() <= 0)
            area = "无";

        int is_group;
        if (check.isChecked())
            is_group = Friend.GROUP;
        else
            is_group = Friend.NOT_GROUP;

        localUser.register(username, password, email, is_group, area,
                HttpProcessBase.createShowProgress(this, "注册成功", "注册失败"));
    }

    public void cancel(View v) {
        finish();
    }

}
