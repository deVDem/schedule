package ru.devdem.reminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class NetworkController {
    private static RequestQueue queue;

    private static String URL_LOGIN = "https://api.devdem.ru/apps/schedule/accounts/login.php";
    private static String URL_REGISTER = "https://api.devdem.ru/apps/schedule/accounts/register.php";
    private static String URL_GET_GROUPS = "https://api.devdem.ru/apps/schedule/groups/get.php";
    private static String URL_NOTIFICATIONS = "https://api.devdem.ru/apps/schedule/notifications.php";
    private static String URL_LESSONS = "https://api.devdem.ru/apps/schedule/lessons.php";
    private static String URL_GET_VER_INT = "https://api.devdem.ru/apps/schedule/getver.php";
    private static String URL_UPDATE_PROFILE = "https://api.devdem.ru/apps/schedule/accounts/update.php";
    private static String URL_ADD_NOTIFICATION = "https://api.devdem.ru/apps/schedule/notifications/add.php";

    private static final String TAG = "NetworkController";

    static Response.ErrorListener getErrorListener(Context context) {
        return error -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(R.string.errorNetwork)
                    .setMessage(context.getString(R.string.detail) + " " + error.toString())
                    .setCancelable(false)
                    .setPositiveButton(R.string.retry, (dialog1, which) -> {
                        Activity activity = (Activity) context;
                        activity.recreate();
                        dialog1.cancel();
                    })
                    .create();
            dialog.show();
        };
    }

    private static void goSend(Context context, Response.Listener<String> listener, Response.ErrorListener errorListener, String URL, Map<String, String> map) {
        SendRequest sendRequest = new SendRequest(listener, errorListener, URL, map);
        if (queue == null) queue = Volley.newRequestQueue(context);
        queue.add(sendRequest);
    }

    static void addNotification(Context context, Response.Listener<String> listener, Response.ErrorListener errorListener, String token, String group, String title, String message, String urlImage) {
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("group", group);
        map.put("title", title);
        map.put("message", message);
        map.put("urlImage", urlImage);
        goSend(context, listener, errorListener, URL_ADD_NOTIFICATION, map);

    }

    static void editProfile(Context context, Response.Listener<String> listener, Response.ErrorListener errorListener, String name, String email, String login, String token) {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("login", login);
        map.put("token", token);
        goSend(context, listener, errorListener, URL_UPDATE_PROFILE, map);
    }

    static void Login(Context context, String login, String password, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("login", login);
        map.put("password", password);
        goSend(context, listener, errorListener, URL_LOGIN, map);
    }

    static void Register(Context context, String login, String name, String email, String password, String group, String spam, Response.Listener<String> listener) {
        Map<String, String> map = new HashMap<>();
        map.put("login", login);
        map.put("name", name);
        map.put("email", email);
        map.put("password", password);
        map.put("group", group);
        map.put("spam", spam);
        goSend(context, listener, getErrorListener(context), URL_REGISTER, map);
    }

    static void getLessons(Context context, Response.Listener<String> listener, Response.ErrorListener errorListener, String group, String token) {
        Map<String, String> map = new HashMap<>();
        map.put("group", group);
        map.put("token", token);
        goSend(context, listener, errorListener, URL_LESSONS, map);
    }

    static void getGroups(Context context, String group) {
        Response.Listener<String> listener = response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                for (int i = 1; i < 255; i++) {
                    try {
                        if (i == Integer.parseInt(group)) {
                            context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
                                    .putString("group_name", jsonResponse.getJSONObject(String.valueOf(i)).getString("name"))
                                    .apply();
                            break;
                        }
                    } catch (Exception e) {
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };
        goSend(context, listener, null, URL_GET_GROUPS, new HashMap<>());
    }

    static void GetGroupsToSpinner(Context context, Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        adapter.add(context.getString(R.string.choose));
        Response.Listener<String> listener = response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                for (int i = 1; i <= 100; i++) {
                    try {
                        adapter.add(jsonResponse.getJSONObject(String.valueOf(i)).getString("name"));
                    } catch (Exception e) {
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            spinner.setAdapter(adapter);
        };
        goSend(context, listener, getErrorListener(context), URL_GET_GROUPS, new HashMap<>());
    }

    static void getNotifications(Context context, String group, String token, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("group", group);
        map.put("token", token);
        goSend(context, listener, errorListener, URL_NOTIFICATIONS, map);
    }

    static void getLastVerInt(Context context, Response.Listener<String> listener) {
        goSend(context, listener, null, URL_GET_VER_INT, new HashMap<>());
    }

    private static class SendRequest extends StringRequest {
        private Map<String, String> mParams;

        SendRequest(Response.Listener<String> listener, Response.ErrorListener errorListener, String url, Map<String, String> params) {
            super(Method.POST, url, listener, errorListener);
            mParams = params;
        }

        @Override
        public Map<String, String> getParams() {
            return mParams;
        }
    }
}

