package ru.devxem.reminder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import ru.devxem.reminder.api.Error;
import ru.devxem.reminder.api.Groups;
import ru.devxem.reminder.api.SendInfo;

public class FirstActivity extends AppCompatActivity {

    TextView hello1;
    TextView hello2;
    RelativeLayout hello;
    Spinner list;
    Button acceptbt;
    EditText eTname;
    EditText eTemail;
    Switch swspam;
    Switch swprivacy;
    int object;
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_first);
            activity = this;
            eTname = findViewById(R.id.editText2);
            eTemail = findViewById(R.id.editText);
            swspam = findViewById(R.id.switch1);
            swprivacy = findViewById(R.id.switch2);
            hello = findViewById(R.id.hello_r);
            hello1 = findViewById(R.id.hello_t);
            hello2 = findViewById(R.id.hello2_t);
            list = findViewById(R.id.spinner);
            acceptbt = findViewById(R.id.button);
            ArrayAdapter<String> adp2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Groups.getGroups(this));
            adp2.add(this.getString(R.string.choose));
            list.setAdapter(adp2);
            object = R.id.hello_t;
            acceptbt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (eTname.getText().toString().length() < 4) {
                        Toast.makeText(FirstActivity.this, R.string.namec, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (eTemail.getText().toString().length() < 10) {
                        Toast.makeText(FirstActivity.this, R.string.emailc, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (list.getSelectedItemPosition() == 0) {
                        Toast.makeText(FirstActivity.this, R.string.groupc, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!swprivacy.isChecked()) {
                        Toast.makeText(FirstActivity.this, R.string.spamc, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final AlertDialog.Builder builder = new AlertDialog.Builder(FirstActivity.this);
                    builder.setMessage("Пожалуйста, подождите.")
                            .setTitle("Отправка данных..")
                            .setCancelable(false);
                    final Dialog dialog = builder.create();
                    dialog.show();
                    Response.Listener<String> listener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                int id = jsonResponse.getInt("id");
                                if (jsonResponse.getBoolean("success")) {
                                    SharedPreferences settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putBoolean("first", false);
                                    editor.putInt("id", id);
                                    editor.putString("email", eTemail.getText().toString());
                                    editor.putString("group", list.getSelectedItem().toString());
                                    editor.apply();
                                    Toast.makeText(FirstActivity.this, R.string.welcometoast, Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(FirstActivity.this, MainActivity.class));
                                    overridePendingTransition(R.anim.transition_out, R.anim.transition_in);
                                    finish();
                                }
                            } catch (JSONException e) {
                                Error.setError(FirstActivity.this, null);
                                e.printStackTrace();
                            }
                            dialog.cancel();
                        }
                    };
                    Response.ErrorListener errorListener = new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.cancel();
                            Toast.makeText(FirstActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                            Error.setError(FirstActivity.this, null);
                        }
                    };
                    SendInfo groupss = new SendInfo(listener, errorListener, eTname.getText().toString(), eTemail.getText().toString(), list.getSelectedItemPosition(), swspam.isChecked());
                    RequestQueue queue = Volley.newRequestQueue(FirstActivity.this);
                    queue.add(groupss);
                }
            });
            mainProcessing();
        } catch (Exception e) {
            Error.setErr(this, e.toString(), getSharedPreferences("settings", Context.MODE_PRIVATE).getString("email", null));
        }
    }

    void ok() {
        switch (object) {
            case R.id.hello_t:
                object = R.id.hello2_t;
                mainProcessing();
                break;
            case R.id.hello2_t:
                object = R.id.hello_r;
                mainProcessing();
                break;
        }
    }

    private void mainProcessing() {
        Thread thread = new Thread(null, doBackgroundThreadProcessing,
                "Background");
        thread.start();
    }

    private Runnable doBackgroundThreadProcessing = new Runnable() {
        public void run() {
            backgroundThreadProcessing();
        }
    };

    private void backgroundThreadProcessing() {
        float b;
        float a;
        float c;
        float i;
        if (object != R.id.hello_r) {
            for (i = 0f; i <= 1000f; i++) {
                a = i / 1000f;
                final float finalA = a;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(object).setAlpha(finalA);
                    }
                });
                if(i%2==0) {
                    try {
                        Thread.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (c = 1000f; c > 0f; c--) {
            b = c / 1000f;
            findViewById(object).setAlpha(b);
            if (c % 2 == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ok();
    }
}
