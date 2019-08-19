package in.edu.ssn.ssnapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;

import in.edu.ssn.ssnapp.adapters.NotifyAdapter;
import in.edu.ssn.ssnapp.adapters.SavedPostAdapter;
import in.edu.ssn.ssnapp.database.DataBaseHelper;
import in.edu.ssn.ssnapp.models.Post;

public class SavedPostActivity extends AppCompatActivity {

    ListView lv_savedPost;
    SavedPostAdapter savedPostAdapter;
    DataBaseHelper dbHelper;
    ImageView iv_back;
    RelativeLayout layout_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_post);

        lv_savedPost = findViewById(R.id.lv_items);
        layout_progress = findViewById(R.id.layout_progress);
        iv_back = findViewById(R.id.iv_back);
        savedPostAdapter = new SavedPostAdapter(this, new ArrayList<Post>());
        dbHelper=DataBaseHelper.getInstance(this);
        lv_savedPost.setAdapter(savedPostAdapter);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        savedPostAdapter.clear();
        ArrayList<Post> savedPostList=dbHelper.getSavedPostList();
        Collections.reverse(savedPostList);
        savedPostAdapter.addAll(savedPostList);
        savedPostAdapter.notifyDataSetChanged();

        if(savedPostList.size() > 0){
            layout_progress.setVisibility(View.GONE);
            lv_savedPost.setVisibility(View.VISIBLE);
        }
        else{
            layout_progress.setVisibility(View.VISIBLE);
            lv_savedPost.setVisibility(View.GONE);
        }
    }
}
