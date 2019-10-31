package ru.devxem.reminder.ui.dashboard;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import ru.devxem.reminder.MainActivity;
import ru.devxem.reminder.R;
import ru.devxem.reminder.api.Error;
import ru.devxem.reminder.api.GetNear;

public class DashboardFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    private static ListView listView;
    private static Dialog dialog;
    static CountDownTimer timer;
    private static boolean loaded;

    public static void reloadLess(ArrayAdapter<String> adp2) {
        dialog.cancel();
        loaded = true;
        timer.cancel();
        listView.setAdapter(adp2);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final String id = MainActivity.getSss().get(0);
        String group = MainActivity.getSss().get(1);
        Objects.requireNonNull(getActivity()).setTitle("хуй");
        // findViewById() делать через root!
        listView = root.findViewById(R.id.listofitems);
        final Context context = Objects.requireNonNull(getContext());
        GetNear.parseLessons(group, id, context);
        loaded = false;
        timer = new CountDownTimer(15000, 250) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (loaded) {
                    this.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (!loaded) {
                    dialog.cancel();
                    Error.setError(context, id);
                }
            }
        }.start();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Пожалуйста, подождите")
                .setTitle("Загрузка..")
                .setCancelable(false);
        dialog = builder.create();
        dialog.show();
        return root;
    }
}