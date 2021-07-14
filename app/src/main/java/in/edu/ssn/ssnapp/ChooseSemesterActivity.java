package in.edu.ssn.ssnapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import spencerstudios.com.bungeelib.Bungee;

// Extends Base activity for darkmode variable and status bar.
public class ChooseSemesterActivity extends BaseActivity {

    CardView sem1, sem2, sem3, sem4, sem5, sem6, sem7, sem8;
    ImageView back;
    View.OnClickListener onClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check if darkmode is enabled and open the appropriate layout.
        if (darkModeEnabled) {
            setContentView(R.layout.activity_choose_semester_dark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(getResources().getColor(R.color.darkColorLight));
        } else
            setContentView(R.layout.activity_choose_semester);

        /************************************************************************/
        //When clicking any buttons on the page this function is called.
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GPACalculatorActivity.class);
                // On click function is differentiated for different buttons using the buttons ID.
                switch (view.getId()) {
                    case R.id.sem1:
                        //passing the semester number an intent extra
                        intent.putExtra("sem", 0);
                        break;
                    case R.id.sem2:
                        intent.putExtra("sem", 1);
                        break;
                    case R.id.sem3:
                        intent.putExtra("sem", 2);
                        break;
                    case R.id.sem4:
                        intent.putExtra("sem", 3);
                        break;
                    case R.id.sem5:
                        intent.putExtra("sem", 4);
                        break;
                    case R.id.sem6:
                        intent.putExtra("sem", 5);
                        break;
                    case R.id.sem7:
                        intent.putExtra("sem", 6);
                        break;
                    case R.id.sem8:
                        intent.putExtra("sem", 7);
                        break;
                }
                startActivity(intent);
                Bungee.slideLeft(ChooseSemesterActivity.this);
                finish();
            }
        };
        /************************************************************************/

        initUI();
        setListeners();
    }

    /**********************************************************************/
    // Initiate variables and UI elements.
    private void initUI() {
        sem1 = findViewById(R.id.sem1);
        sem2 = findViewById(R.id.sem2);
        sem3 = findViewById(R.id.sem3);
        sem4 = findViewById(R.id.sem4);
        sem5 = findViewById(R.id.sem5);
        sem6 = findViewById(R.id.sem6);
        sem7 = findViewById(R.id.sem7);
        sem8 = findViewById(R.id.sem8);
        back = findViewById(R.id.backIV);

        /************************************************************************/
        //Animation
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sem1.setVisibility(View.VISIBLE);
                sem7.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.ZoomIn).duration(500).playOn(sem1);
                YoYo.with(Techniques.ZoomIn).duration(500).playOn(sem7);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sem2.setVisibility(View.VISIBLE);
                        sem5.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.ZoomIn).duration(500).playOn(sem2);
                        YoYo.with(Techniques.ZoomIn).duration(500).playOn(sem5);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sem4.setVisibility(View.VISIBLE);
                                sem6.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.ZoomIn).duration(500).playOn(sem4);
                                YoYo.with(Techniques.ZoomIn).duration(500).playOn(sem6);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        sem8.setVisibility(View.VISIBLE);
                                        sem3.setVisibility(View.VISIBLE);
                                        YoYo.with(Techniques.ZoomIn).duration(500).playOn(sem8);
                                        YoYo.with(Techniques.ZoomIn).duration(500).playOn(sem3);
                                    }
                                }, 300);
                            }
                        }, 300);
                    }
                }, 300);
            }
        }, 300);
        /************************************************************************/

    }
    /**********************************************************************/

    /**********************************************************************/
    //Setting the listener function to all the buttons.
    private void setListeners() {
        sem1.setOnClickListener(onClickListener);
        sem2.setOnClickListener(onClickListener);
        sem3.setOnClickListener(onClickListener);
        sem4.setOnClickListener(onClickListener);
        sem5.setOnClickListener(onClickListener);
        sem6.setOnClickListener(onClickListener);
        sem7.setOnClickListener(onClickListener);
        sem8.setOnClickListener(onClickListener);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    /**********************************************************************/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Bungee.slideRight(ChooseSemesterActivity.this);
    }
}