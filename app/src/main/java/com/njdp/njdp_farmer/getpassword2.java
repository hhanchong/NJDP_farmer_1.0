package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SQLiteHandler;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;
import com.njdp.njdp_farmer.util.NormalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *  忘记密码时重设密码
 **/

public class getpassword2 extends AppCompatActivity {

    private EditText text_password=null;
    private EditText text_password2=null;
    private ImageButton password_reveal=null;
    private ImageButton password_show = null;
    private ImageButton password_reveal2=null;
    private ImageButton password_show2=null;
    private Button finishEdit=null;
    private String telephone;
    private AwesomeValidation password_Validation=new AwesomeValidation(ValidationStyle.BASIC);
    private AwesomeValidation password2_Validation=new AwesomeValidation(ValidationStyle.BASIC);
    private static final String TAG = getpassword2.class.getSimpleName();
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置沉浸模式
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_getpassword2);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        //获取forgetpassword输入的用户名、手机号.姓名
        telephone = getIntent().getStringExtra("telephone");
        if(telephone == null)
        {
            NormalUtil.error_hint(getApplicationContext(), "程序错误！请联系管理员！");
            finish();
        }

        password_Validation.addValidation(getpassword2.this, R.id.user_password, "^[A-Za-z0-9_]{5,15}+$", R.string.err_password);
        password2_Validation.addValidation(getpassword2.this, R.id.user_password2,"^[A-Za-z0-9_]{5,15}+$" , R.string.err_password);
        this.password_reveal=(ImageButton) super.findViewById(R.id.reveal_button);
        this.password_show=(ImageButton) super.findViewById(R.id.show_button);
        this.password_reveal2=(ImageButton) super.findViewById(R.id.reveal_button2);
        this.password_show2=(ImageButton) super.findViewById(R.id.show_button2);
        this.text_password=(EditText)super.findViewById(R.id.user_password);
        this.text_password2=(EditText)super.findViewById(R.id.user_password2);
        ImageButton getback = (ImageButton) super.findViewById(R.id.getback);
        //返回上一界面
        assert getback != null;
        getback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        finishEdit = (Button)findViewById(R.id.finish);
        finishEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NormalUtil.isempty(text_password)) {
                    NormalUtil.error_hint(getApplicationContext(), "请输入新的密码");
                } else if (password_Validation.validate()) {
                    if (NormalUtil.isempty(text_password2)) {
                        NormalUtil.error_hint(getApplicationContext(), "请再次输入密码");
                    } else if (password2_Validation.validate()) {
                        if (text_password.getText().toString().trim().equals(text_password2.getText().toString().trim())) {
                            String nPassword = text_password2.getText().toString().trim();
                            setNewPassword(nPassword);
                        } else {
                            NormalUtil.error_hint(getApplicationContext(), "两次输入的密码不一致！");
                        }
                    }
                }
            }
        });
        editTextIsNull();
    }

    //点击眼睛1按钮，设置密码显示或者隐藏
    public void showClick(View v) {
        password_reveal.setVisibility(View.VISIBLE);
        password_show.setVisibility(View.GONE);
        getpassword2.this.text_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
    public void revealClick(View v) {
        password_reveal.setVisibility(View.GONE);
        password_show.setVisibility(View.VISIBLE);
        getpassword2.this.text_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
    }
    //点击眼睛2按钮，设置密码显示或者隐藏
    public void showClick2(View v) {
        password_reveal2.setVisibility(View.VISIBLE);
        password_show2.setVisibility(View.GONE);
        getpassword2.this.text_password2.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
    public void revealClick2(View v) {
        password_reveal2.setVisibility(View.GONE);
        password_show2.setVisibility(View.VISIBLE);
        getpassword2.this.text_password2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
    }

    //设置新的密码到数据库
    public void setNewPassword(final String password) {

        // Tag used to cancel the request
        String tag_string_req = "req_reset_password";

        pDialog.setMessage("正在重置密码 ...");
        showDialog();

        if (!NetUtil.checkNet(getpassword2.this)) {
            NormalUtil.error_hint(getApplicationContext(), "网络连接错误");
            hideDialog();
        } else {
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_GETPASSWORD22, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Register Response: " + response);
                    hideDialog();

                    try {
                        JSONObject jObj = new JSONObject(response);
                        int status = jObj.getInt("status");
                        if (status == 0) {
                            session.setLogin(false, false, "");
                            db.editUser(0, "", telephone, "", "");
                            NormalUtil.error_hint(getApplicationContext(), "重置密码成功，请重新登录。");

                            // 重新登录
                            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        } else if(status == 17){
                            // Error occurred in registration. Get the error message
                            NormalUtil.error_hint(getApplicationContext(), "请输入手机号");
                        }else if(status == 18){
                            NormalUtil.error_hint(getApplicationContext(), "请输入新密码");
                        }else {
                            NormalUtil.error_hint(getApplicationContext(), "服务器遇到未知错误。");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Registration Error: " + error.getMessage());
                    NormalUtil.error_hint(getpassword2.this, error.getMessage());
                    hideDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {

                    Map<String, String> params = new HashMap<>();
                    params.put("phone", telephone);
                    params.put("password", password);
                    return params;
                }

            };

            // Adding request to request queue
            strReq.setRetryPolicy(new DefaultRetryPolicy(2000, 1, 1.0f)); //请求超时时间2S，重复1次
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //输入是否为空，判断是否禁用按钮
    private void editTextIsNull(){

        text_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(text_password2.getText())) {
                    finishEdit.setClickable(true);
                    finishEdit.setEnabled(true);
                } else {
                    finishEdit.setEnabled(false);
                    finishEdit.setClickable(false);
                }
            }
        });

        text_password2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(text_password.getText())) {
                    finishEdit.setClickable(true);
                    finishEdit.setEnabled(true);
                } else {
                    finishEdit.setEnabled(false);
                    finishEdit.setClickable(false);
                }
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        View view = findViewById(R.id.top_layout);
        assert view != null;
        view.setBackgroundResource(0); //释放背景图片
    }

    //不跟随系统变化字体大小
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics() );
        return res;
    }
}
