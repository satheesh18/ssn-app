package in.edu.ssn.ssnapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sahurjt.objectcsv.CsvDelimiter;
import com.sahurjt.objectcsv.CsvHolder;
import com.sahurjt.objectcsv.ObjectCsv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.edu.ssn.ssnapp.database.DataBaseHelper;
import in.edu.ssn.ssnapp.models.Club;
import in.edu.ssn.ssnapp.models.Faculty;
import in.edu.ssn.ssnapp.models.Post;
import in.edu.ssn.ssnapp.onboarding.OnboardingActivity;
import in.edu.ssn.ssnapp.utils.CommonUtils;
import in.edu.ssn.ssnapp.utils.Constants;
import in.edu.ssn.ssnapp.utils.SharedPref;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import spencerstudios.com.bungeelib.Bungee;

public class SplashActivity extends AppCompatActivity {

    private final static String TAG = "test_set";
    //    In worstcase scenarios
    private static Boolean worst_case = true;
    //    To check the version number available in playstore against the apps version.
    private static Boolean isUpdateAvailable = false;

    Intent intent, notif_intent;
    private GifDrawable gifFromResource;
    //    For the apps api version
    private int currentApiVersion;
    private Map<String, Object> nodes = new HashMap<>();

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //  To Check whether the app is in debug mode.( Used while testing )
        CommonUtils.isDebug();

        initUI();

        //  To Check for BLOCK variables in firebase.
        checkIsBlocked();

        //  To implement Force update ( if a newer version is available.)
        new checkForceUpdate().execute();

        //  To create crash reports in firebase.
        setUpCrashReport();

        try {
            gifFromResource = new GifDrawable(getResources(), R.drawable.splash_screen);
            GifImageView gifImageView = findViewById(R.id.splashIV);
            gifImageView.setImageDrawable(gifFromResource);

            gifFromResource.addAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationCompleted(int loopNumber) {
                    // Once the animation is completed we call "passIntent" if there is no update available.
                    gifFromResource.stop();
                    if (!isUpdateAvailable) {
                        passIntent();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get the current login time.
        Calendar calendar = Calendar.getInstance();
        Long current = calendar.getTimeInMillis();

        //Get the last login time.
        Long prev = SharedPref.getLong(getApplicationContext(), "dont_delete", "db_update");

        //If the difference is greater than 7 days( 7 days = 604800000 milliseconds) update the faculty Json file.
        if (current - prev > 604800000) {
            new updateFaculty().execute();
            //Update the last login time to latest entry.
            SharedPref.putLong(getApplicationContext(), "dont_delete", "db_update", current);
        }

        if (!isUpdateAvailable)
            handleIntent();
    }


    /**********************************************************************/
    // Initiate variables and UI elements.
    void initUI() {
        currentApiVersion = Build.VERSION.SDK_INT;

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // To make the Splash activity go full screen for the animations.
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
        }

        //Create Firebase reference for the BLOCK variables.
        mDatabase = FirebaseDatabase.getInstance().getReference("block_screen");
        intent = getIntent();
        notif_intent = null;
    }
    /**********************************************************************/


    /**********************************************************************/
    // check current version and force update
    public void showForceUpdateDialog(String latestVersion) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.custom_alert_dialog2, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        TextView okTV = dialogView.findViewById(R.id.okTV);
        TextView titleTV = dialogView.findViewById(R.id.titleTV);
        TextView messageTV = dialogView.findViewById(R.id.messageTV);

        titleTV.setText("New Update available!");
        messageTV.setText("Please update your app to the latest version: " + latestVersion);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.show();

        okTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //redirects to Google Play Store.
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                alertDialog.dismiss();
                finish();
            }
        });
    }
    /**********************************************************************/

    /**********************************************************************/
    //Check for BLOCK variables and block respective screens.
    public void checkIsBlocked() {
        //Use the Firebase reference created for the BLOCK variables.
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            //Is called whenever you change any variables in the cloud storage in realtime database.
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nodes = (Map<String, Object>) dataSnapshot.getValue();
                try {
                    // overall - to block entire app.
                    CommonUtils.setIs_blocked((Boolean) nodes.get("overall"));
                    // to block global chat.
                    CommonUtils.setGlobal_chat_is_blocked((Boolean) nodes.get("global_chat"));
                    // to block ppl with non-ssn mail ids.
                    CommonUtils.setNon_ssn_email_is_blocked((Boolean) nodes.get("non_ssn_email"));

                    // when overall is blocked redirect to blockscreen.
                    if (CommonUtils.getIs_blocked()) {
                        startActivity(new Intent(getApplicationContext(), BlockScreenActivity.class));
                        Bungee.fade(SplashActivity.this);
                    }

                    try {
                        //  If a fresher has been logged in already clear all the data and signout.
                        if (SharedPref.getInt(getApplicationContext(), "dont_delete", "is_logged_in") == 2 && Constants.fresher_email.contains(SharedPref.getString(getApplicationContext(), "email")) && CommonUtils.getNon_ssn_email_is_blocked()) {
                            FirebaseAuth.getInstance().signOut();
                            CommonUtils.UnSubscribeToAlerts(getApplicationContext());
                            DataBaseHelper dbHelper = DataBaseHelper.getInstance(SplashActivity.this);
                            dbHelper.dropAllTables();
                            SharedPref.removeAll(getApplicationContext());

                            //update logged_in status to signed out and redirect to login screen.
                            SharedPref.putInt(getApplicationContext(), "dont_delete", "is_logged_in", 1);
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            Bungee.fade(SplashActivity.this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    /**********************************************************************/

    /**********************************************************************/
    //Setup firebase crashlytics to track Bugs.
    void setUpCrashReport() {
        //only enable bug tracking in release version
        if (!BuildConfig.DEBUG) {
            //https://stackoverflow.com/a/49836972/10664312
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        }
    }
    /**********************************************************************/

    /**********************************************************************/
    //Get a post requested if the app is opened with a shared link via whatsapp or other social media.
    public void FetchPostById(final String postId, final String collectionName, final String post_id, final int type) {
        String collection = collectionName;
        if (collectionName.equals(Constants.collection_post_club))
            collection = Constants.collection_club;

        if (!CommonUtils.alerter(getApplicationContext())) {
            FirebaseFirestore.getInstance().collection(collection).document(postId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    if (collectionName.equals(Constants.collection_club) || collectionName.equals(Constants.collection_post_club)) {
                        Club club = CommonUtils.getClubFromSnapshot(getApplicationContext(), snapshot);

                        if (collectionName.equals(Constants.collection_post_club)) {
                            try {
                                notif_intent = new Intent(SplashActivity.this, ClubPostDetailsActivity.class);
                                notif_intent.putExtra("data", post_id);
                                notif_intent.putExtra("club", club);
                                worst_case = false;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            notif_intent = new Intent(SplashActivity.this, ClubPageActivity.class);
                            notif_intent.putExtra("data", club);
                            worst_case = false;
                        }
                    } else {
                        Post post = CommonUtils.getPostFromSnapshot(getApplicationContext(), snapshot);
                        post.setId(postId);
                        if (type == Constants.placement) {
                            post.setAuthor_image_url("placement@ssn.edu.in");
                            post.setAuthor("SSN Career Development Centre");
                            post.setPosition("Placement Coordinator");
                        } else if (type == Constants.exam_cell) {
                            post.setAuthor_image_url("examcell@ssn.edu.in");
                            post.setAuthor("SSNCE COE");
                            post.setPosition("Exam cell Coordinator");
                        } else if (type == Constants.event) {
                            post.setAuthor_image_url("eventmanagement@ssn.edu.in");
                            post.setAuthor("SSN Event Management");
                            post.setPosition("Event Coordinator");
                        }

                        notif_intent = new Intent(getApplicationContext(), PostDetailsActivity.class);
                        notif_intent.putExtra("post", post);
                        notif_intent.putExtra("type", type);
                        worst_case = false;
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "failed to fetch the post");
                }
            });
        } else {
            Intent intent = new Intent(getApplicationContext(), NoNetworkActivity.class);
            intent.putExtra("key", "splash");
            startActivity(intent);
            finish();
            Bungee.fade(SplashActivity.this);
        }
    }
    /**********************************************************************/

    /**********************************************************************/
    //To Handle a Share link. When the app opens from a link shared via whatsapp or other social media for a particular post.
    // Each collection has a particular type id and each post has a post id.
    void handleIntent() {
        int type = 1;
        String pdfUrl = "";
        String vca = "", vac = "";

        String collectionName = "";

        Bundle bundle = intent.getExtras();
        Uri uri = null;

        if ((Intent.ACTION_VIEW.equalsIgnoreCase(intent.getAction()) || bundle != null) && SharedPref.getInt(this, "dont_delete", "is_logged_in") == 2) {
            if (Intent.ACTION_VIEW.equalsIgnoreCase(intent.getAction())) {
                uri = intent.getData();
                try {
                    type = Integer.parseInt(uri.getQueryParameter("type"));
                    collectionName = CommonUtils.getCollectionName(type);
                } catch (Exception e) {
                    e.printStackTrace();
                    type = 1;
                    collectionName = Constants.collection_post;
                }

                try {
                    vca = uri.getQueryParameter("vca");
                    vac = uri.getQueryParameter("vac");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (bundle != null) {
                try {
                    if (bundle.containsKey("PostType")) {
                        type = Integer.parseInt(intent.getStringExtra("PostType"));
                        collectionName = CommonUtils.getCollectionName(type);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    type = 1;
                    collectionName = Constants.collection_post;
                }

                if (bundle.containsKey("vca"))
                    vca = intent.getStringExtra("vca");
                if (bundle.containsKey("vac"))
                    vac = intent.getStringExtra("vac");
                if (bundle.containsKey("PostUrl"))
                    pdfUrl = intent.getStringExtra("PostUrl");
            } else
                return;

            if (pdfUrl != null && !pdfUrl.equals("")) {
                if (!CommonUtils.alerter(getApplicationContext())) {
                    notif_intent = new Intent(getApplicationContext(), PdfViewerActivity.class);
                    notif_intent.putExtra(Constants.PDF_URL, pdfUrl);
                    worst_case = false;
                } else {
                    Intent intent = new Intent(getApplicationContext(), NoNetworkActivity.class);
                    intent.putExtra("key", "splash");
                    startActivity(intent);
                    finish();
                    Bungee.fade(SplashActivity.this);
                }
            } else if (collectionName.equals(Constants.collection_post_club)) {
                // http://ssnportal.netlify.app/share.html?vca =     K1gFiFwA3A2Y2O30PJUA & type=4  & acv=5d &  vac=43
                // http://ssnportal.netlify.app/share.html?club_id = K1gFiFwA3A2Y2O30PJUA & type=4  & time=5d & post_id=43

                //vca ==> id [club_id]
                //vac ==> id [post_id]
                FetchPostById(vca, collectionName, vac, type);
            } else if (!collectionName.equals("")) {
                Log.d("test_set", vca);
                FetchPostById(vca, collectionName, vac, type);
            }
        }
    }
    /**********************************************************************/

    /**********************************************************************/
    // Check the different status of the application and proceed to next screen accordingly
    public void passIntent() {
        //check whether the app is blocked.
        if (!CommonUtils.getIs_blocked()) {
            worst_case = false;
            if (notif_intent != null) {
                startActivity(notif_intent);
                finish();
                Bungee.slideLeft(SplashActivity.this);
            }
            // If we have a active network connection.
            else if (!CommonUtils.alerter(getApplicationContext())) {
                // if already loggedin
                if (SharedPref.getInt(getApplicationContext(), "dont_delete", "is_logged_in") == 2) {
                    //if its a professor
                    if (SharedPref.getInt(getApplicationContext(), "clearance") == 3) {
                        startActivity(new Intent(getApplicationContext(), FacultyHomeActivity.class));
                        finish();
                        Bungee.fade(this);
                    }
                    //it its a student ( UG, PG, Alumni)
                    else {
                        startActivity(new Intent(getApplicationContext(), StudentHomeActivity.class));
                        finish();
                        Bungee.fade(this);
                    }
                }
                // if Already Signed Out
                else if (SharedPref.getInt(getApplicationContext(), "dont_delete", "is_logged_in") == 1) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.putExtra("is_log_in", true);
                    startActivity(intent);
                    finish();
                    Bungee.slideLeft(this);
                }
                // If completely new (If they dont have any shared preferences stored)
                else {
                    startActivity(new Intent(getApplicationContext(), OnboardingActivity.class));
                    finish();
                    Bungee.slideLeft(this);
                }
            }
            //If we don't have a active network connection
            else {
                Intent intent = new Intent(getApplicationContext(), NoNetworkActivity.class);
                intent.putExtra("key", "splash");
                startActivity(intent);
                finish();
                Bungee.fade(SplashActivity.this);
            }
        }
    }

    /**********************************************************************/

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Worst-case scenario (it will intent to any other activity) after 5s
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (worst_case & !isUpdateAvailable)
                    passIntent();
            }
        }, 5000);
    }

    /**********************************************************************/
    // Update faculty details fetched from a json file present in portal
    public class updateFaculty extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (!CommonUtils.alerter(getApplicationContext())) {
                Glide.with(SplashActivity.this).asFile().load("https://ssnportal.netlify.app/scripts/data_faculty.csv").into(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "SSNCE");
                        if (!dir.exists())
                            dir.mkdir();

                        File file = new File(dir, "data_faculty.csv");
                        try {
                            FileInputStream inStream = new FileInputStream(resource);
                            FileOutputStream outStream = new FileOutputStream(file);
                            FileChannel inChannel = inStream.getChannel();
                            FileChannel outChannel = outStream.getChannel();
                            inChannel.transferTo(0, inChannel.size(), outChannel);
                            inStream.close();
                            outStream.close();

                            if (file.exists()) {
                                try {
                                    CsvHolder<Faculty> holder = ObjectCsv.getInstance().from(file.getPath()).with(CsvDelimiter.COMMA).getCsvHolderforClass(Faculty.class);
                                    List<Faculty> models = holder.getCsvRecords();

                                    for (Faculty m : models) {
                                        String email = m.getEmail();
                                        SharedPref.putString(getApplicationContext(), "faculty_access", email, m.getAccess());
                                        SharedPref.putString(getApplicationContext(), "faculty_dept", email, m.getDept());
                                        SharedPref.putString(getApplicationContext(), "faculty_name", email, m.getName());
                                        SharedPref.putString(getApplicationContext(), "faculty_position", email, m.getPosition());
                                    }

                                    file.delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!isUpdateAvailable)
                            handleIntent();
                    }
                });
            } else {
                Intent intent = new Intent(getApplicationContext(), NoNetworkActivity.class);
                intent.putExtra("key", "splash");
                startActivity(intent);
                finish();
                Bungee.fade(SplashActivity.this);
            }
            return null;
        }
    }

    public class checkForceUpdate extends AsyncTask<Void, Void, Void> {
        String latestVersion = null;

        @Override
        protected Void doInBackground(Void... voids) {
            latestVersion = CommonUtils.getLatestVersionName(getApplicationContext());
            // if the version in PlayStore not matches our app version we make suggest update.
            if (latestVersion != null && !BuildConfig.VERSION_NAME.equals(latestVersion)) {
                isUpdateAvailable = true;
            } else {
                isUpdateAvailable = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isUpdateAvailable) {
                showForceUpdateDialog(latestVersion);
            }

        }
    }
}