package in.edu.ssn.ssnapp;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.suke.widget.SwitchButton;


import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import in.edu.ssn.ssnapp.adapters.DrawerAdapter;
import in.edu.ssn.ssnapp.adapters.ViewPagerAdapter;
import in.edu.ssn.ssnapp.database.DataBaseHelper;
import in.edu.ssn.ssnapp.fragments.BusAlertsFragment;
import in.edu.ssn.ssnapp.models.Drawer;
import in.edu.ssn.ssnapp.models.Faculty;
import in.edu.ssn.ssnapp.utils.CommonUtils;
import in.edu.ssn.ssnapp.utils.Constants;
import in.edu.ssn.ssnapp.utils.FCMHelper;
import in.edu.ssn.ssnapp.utils.SharedPref;
import spencerstudios.com.bungeelib.Bungee;

public class FacultyHomeActivity extends BaseActivity {

    ImageView settingsIV;
    CircleImageView userImageIV;
    ViewPager viewPager;
    private static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(darkModeEnabled){
            setContentView(R.layout.activity_faculty_home_dark);
            clearLightStatusBar(this);
        }
        else
            setContentView(R.layout.activity_faculty_home);

        initUI();

        /******************************************************************/
        //What's new

        if(BuildConfig.VERSION_CODE > SharedPref.getInt(getApplicationContext(),"dont_delete","current_version_code")){
            SharedPref.putInt(getApplicationContext(),"dont_delete","current_version_code", BuildConfig.VERSION_CODE);
            CommonUtils.showWhatsNewDialog(this,darkModeEnabled);
        }
    }

    /*********************************************************/

    void initUI() {
        settingsIV = findViewById(R.id.settingsIV);
        userImageIV = findViewById(R.id.userImageIV);
        viewPager = findViewById(R.id.viewPager);

        try{
            Glide.with(this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString()).placeholder(R.drawable.ic_user_white).into(userImageIV);
        }
        catch (Exception e){
            e.printStackTrace();
            Glide.with(this).load(SharedPref.getString(getApplicationContext(),"dp_url")).placeholder(R.drawable.ic_user_white).into(userImageIV);
        }

        setupViewPager();

        settingsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                Bungee.slideLeft(FacultyHomeActivity.this);
            }
        });
    }

    void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new BusAlertsFragment(),"Bus alert");
        viewPager.setAdapter(adapter);
    }

    /*********************************************************/

    @Override
    protected void onResume() {
        super.onResume();

        if(CommonUtils.alerter(getApplicationContext())){
            Intent intent = new Intent(getApplicationContext(), NoNetworkActivity.class);
            intent.putExtra("key","home");
            startActivity(intent);
            Bungee.fade(FacultyHomeActivity.this);
        }
    }

    /*********************************************************/

    @Override
    public void onBackPressed() {
        if (count > 0) {
            count = 0;
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(startMain);
            finish();
        } else {
            count++;
            Toast toast = Toast.makeText(getApplicationContext(), "Press back once again to exit!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
    }
}
