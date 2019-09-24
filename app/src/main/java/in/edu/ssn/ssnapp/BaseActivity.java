package in.edu.ssn.ssnapp;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import in.edu.ssn.ssnapp.utils.FontChanger;
import in.edu.ssn.ssnapp.utils.SharedPref;

public class BaseActivity extends AppCompatActivity {

    //Fonts
    public Typeface regular, bold, semi_bold;
    public boolean darkModeEnabled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        darkModeEnabled = SharedPref.getBoolean(getApplicationContext(),"darkMode");

        initFonts();

    }

    private void initFonts(){
        regular = ResourcesCompat.getFont(this, R.font.open_sans);
        bold = ResourcesCompat.getFont(this, R.font.open_sans_bold);
        semi_bold = ResourcesCompat.getFont(this, R.font.open_sans_semi_bold);
    }

    //This changes font for all the text views in a view group
    //fontChanger.replaceFonts((ViewGroup)this.findViewById(android.R.id.content));
    public void changeFont(Typeface typeface, ViewGroup viewGroup){
        FontChanger fontChanger = new FontChanger(typeface);
        fontChanger.replaceFonts(viewGroup);
    }
}
