package ru.devdem.reminder.ui.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.Response;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.devdem.reminder.R;
import ru.devdem.reminder.controllers.NetworkController;
import ru.devdem.reminder.controllers.ObjectsController;
import ru.devdem.reminder.object.User;
import ru.devdem.reminder.ui.group.GroupInfoActivity;
import ru.devdem.reminder.ui.view.HoldButton;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class ProfileFragment extends Fragment {
    private Context mContext;
    private SharedPreferences mSettings;
    private String[] permissions;
    private MainActivity mMainActivity;
    private NetworkController networkController;
    private FrameLayout mProfileFrame;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.fragment_profile, null);
        mContext = requireContext();
        mMainActivity = (MainActivity) requireActivity();
        networkController = NetworkController.get();
        permissions = getResources().getStringArray(R.array.permissions);
        mSettings = mContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
        mProfileFrame = v.findViewById(R.id.profileFrame);
        setHasOptionsMenu(true);
        updateUI();
        Button mDetailButton = v.findViewById(R.id.buttonDetailGroup);
        mDetailButton.setOnClickListener(view -> {
            mMainActivity.startActivity(GroupInfoActivity.getAIntent(mContext, Integer.parseInt(Objects.requireNonNull(mSettings.getString("group", "0"))), false));
            mMainActivity.overridePendingTransition(R.anim.transition_out, R.anim.transition_in);
        });
        HoldButton mLeaveButton = v.findViewById(R.id.buttonLeaveGroup);
        mLeaveButton.setHoldDownListener(v1 -> exitGroup());
        return v;
    }

    private void exitGroup() {
        AlertDialog dialog = new AlertDialog.Builder(mContext).setTitle(R.string.out_of_the_group).setMessage(R.string.wait).setCancelable(false).create();
        dialog.show();
        Response.Listener<String> listener = response -> {
            Log.d(TAG, "exitGroup: "+response);
            try {
                mMainActivity.checkAccount();
                mContext.getSharedPreferences("jsondata", Context.MODE_PRIVATE).edit().clear().apply();
                dialog.cancel();
            } catch (Exception e) {
                e.printStackTrace();
                dialog.cancel();
            }
        };
        Response.ErrorListener errorListener = error -> {
            Toast.makeText(mContext, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            dialog.cancel();
        };
        networkController.joinToGroup(mContext, listener, errorListener, "-1", mSettings.getString("token", "null"));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reload:
                mMainActivity.checkAccount();
                updateUI();
                return true;
            case R.id.menu_settings:
                // TODO: сделать редактор профиля
                //startActivityForResult(new Intent(mContext, EditProfileActivity.class), 228);
                //requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                Toast.makeText(mContext, "Редактор профилей ещё не готов", Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    private void updateUI() {
        mProfileFrame.removeAllViews();
        User user = ObjectsController.getLocalUserInfo(mSettings);
        String name = user.getNames();
        String login = "@" + user.getLogin();
        String email = user.getEmail();
        int urlImage = user.getImageId();
        View profileCard = View.inflate(mContext, R.layout.group_info_user_view_full, null);
        TextView textName = profileCard.findViewById(R.id.profileName);
        TextView textLogin = profileCard.findViewById(R.id.profileLogin);
        TextView textEmail = profileCard.findViewById(R.id.profileEmail);
        TextView textPermission = profileCard.findViewById(R.id.profilePermission);
        ImageView imagePro = profileCard.findViewById(R.id.proImage);
        CircleImageView imageProfile = profileCard.findViewById(R.id.profileImage);
        Button goProBtn = profileCard.findViewById(R.id.goProBtn);
        goProBtn.setOnClickListener(v -> {
            /*startActivity(new Intent(mMainActivity, PurchaseActivity.class));
            mMainActivity.overridePendingTransition(R.anim.transition_out, R.anim.transition_in);*/
            Toast.makeText(mContext, "Временно не работает", Toast.LENGTH_LONG).show();
        });
        CardView cardView = profileCard.findViewById(R.id.card_view);
        textName.setText(name);
        textEmail.setText(email);
        textLogin.setText(login);
        imagePro.setVisibility(user.isPro() ? View.VISIBLE : View.GONE);
        goProBtn.setVisibility(user.isPro() ? View.GONE : View.VISIBLE);
        if (urlImage != 0 )
            Picasso.get().load(urlImage).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    new Thread(null, () -> {
                        int width = 250;
                        int height = Math.round((float) width / bitmap.getWidth() * bitmap.getHeight());
                        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
                        Bitmap preparePixel = Bitmap.createScaledBitmap(scaled, 1, 1, true);
                        int color = preparePixel.getPixel(0, 0);
                        int rez = 0xFFF - color + 0xFF000000;
                        float[] hsv = new float[3];
                        Color.colorToHSV(rez, hsv);
                        hsv[0] = hsv[0] + 180;
                        int cardColor = Color.HSVToColor(hsv);
                        int textColor = -1 * cardColor + 0xFF000000;
                        mMainActivity.runOnUiThread(() -> {
                            imageProfile.setImageBitmap(scaled);
                            imageProfile.setBorderColor(textColor);
                            if (user.isPro()) {
                                int[][] states = new int[][]{
                                        new int[]{android.R.attr.state_enabled}
                                };
                                int[] colors = new int[]{
                                        cardColor
                                };
                                textName.setTextColor(textColor);
                                textLogin.setTextColor(textColor);
                                textEmail.setTextColor(textColor);
                                textPermission.setTextColor(textColor);
                                cardView.setBackgroundTintList(new ColorStateList(states, colors));
                                imagePro.setColorFilter(textColor);
                            }
                            new CountDownTimer(2000, 16) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    float alpha = (10000f - millisUntilFinished) / 10000f;
                                    imageProfile.setAlpha(alpha);
                                }

                                @Override
                                public void onFinish() {
                                    imageProfile.setAlpha(1.0f);
                                }
                            }.start();
                        });
                    }, "Card background").start();
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        mProfileFrame.addView(profileCard);
    }
}
