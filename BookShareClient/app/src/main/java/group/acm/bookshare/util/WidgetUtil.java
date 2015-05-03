package group.acm.bookshare.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;

import group.acm.bookshare.R;

/**
 * Created by toufu on 15-5-4.
 */
public class WidgetUtil {

    public static AlertDialog createApmDialog(Context context, final ApmConfirm confirmListener) {
        View addCreateApMView = LayoutInflater.from(
                context).inflate(
                R.layout.apointment_dialog, null);
        final EditText timeInput = (EditText) addCreateApMView
                .findViewById(R.id.apointment_input_time);
        final EditText locationInput = (EditText) addCreateApMView
                .findViewById(R.id.apointment_input_location);
        AlertDialog apmDialog = null;
        AlertDialog.Builder builder = null;

        builder = new AlertDialog.Builder(context);
        builder.setTitle("预约");
        builder.setView(addCreateApMView);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String date = timeInput.getText().toString();
                String location = locationInput.getText().toString();
                confirmListener.onInput(date, location);
            }
        });
        builder.setNegativeButton("取消", null);
        apmDialog = builder.create();
        return apmDialog;
    }

    public static interface ApmConfirm {
        public void onInput(String time, String location);
    }

    public static AlertDialog createContentDialog(Context context, String hint, final ContentConfirm confirmListener) {
        View addCreateApMView = LayoutInflater.from(
                context).inflate(
                R.layout.content_dialog, null);
        final EditText contentInput = (EditText) addCreateApMView
                .findViewById(R.id.content_input);
        AlertDialog apmDialog = null;
        AlertDialog.Builder builder = null;

        builder = new AlertDialog.Builder(context);
        builder.setTitle("请输入");
        builder.setView(addCreateApMView);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String content = contentInput.getText().toString();
                confirmListener.onInput(content);
            }
        });
        builder.setNegativeButton("取消", null);
        apmDialog = builder.create();
        return apmDialog;
    }

    public static interface ContentConfirm {
        public void onInput(String content);
    }
}
