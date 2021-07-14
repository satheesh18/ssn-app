package in.edu.ssn.ssnapp;

import android.animation.LayoutTransition;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import in.edu.ssn.ssnapp.adapters.BusRouteAdapter;
import in.edu.ssn.ssnapp.models.BusRoute;
import in.edu.ssn.ssnapp.utils.CommonUtils;
import spencerstudios.com.bungeelib.Bungee;

// Extends Base activity for darkmode variable and status bar.
public class BusRoutesActivity extends BaseActivity implements TextWatcher {
    ImageView backIV;
    RecyclerView busRoutesRV;
    EditText et_num;
    ImageView clearIV;
    AppBarLayout searchRL;
    LinearLayout searchRL1;
    RelativeLayout layout_empty;
    ChipGroup chipCloud;
    List<String> suggestions;

    BusRouteAdapter adapter;
    ArrayList<BusRoute> busRoutesList, recyclerList;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check if darkmode is enabled and open the appropriate layout.
        if (darkModeEnabled) {
            setContentView(R.layout.activity_bus_route_dark);
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkColor1));
        } else {
            setContentView(R.layout.activity_bus_route);
        }
        recyclerList = new ArrayList<>();
        initUI();
        new getBusRoute().execute();

        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        clearIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_num.setText("");
            }
        });
    }

    /***********************************************************/
    // Initiate variables and UI elements.
    void initUI() {
        backIV = findViewById(R.id.backIV);
        et_num = findViewById(R.id.et_num);
        et_num.addTextChangedListener(this);

        chipCloud = findViewById(R.id.chipCloud);
        clearIV = findViewById(R.id.clearIV);
        layout_empty = findViewById(R.id.layout_empty);

        searchRL = findViewById(R.id.searchRL);
        searchRL1 = findViewById(R.id.searchRL1);
        searchRL.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        searchRL1.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        suggestions = new ArrayList<>();
        busRoutesList = new ArrayList<>();

        busRoutesRV = findViewById(R.id.busRoutesRV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        busRoutesRV.setLayoutManager(layoutManager);
        busRoutesRV.setHasFixedSize(true);
    }
    /***********************************************************/

    /***********************************************************/
    //Suggestion chips seen while searching for a Bus stop.
    private Chip getChip(final ChipGroup entryChipGroup, String text) {
        final Chip chip = new Chip(this);
        if (darkModeEnabled) {
            chip.setChipDrawable(ChipDrawable.createFromResource(this, R.xml.suggestion_item_dark));
        } else {
            chip.setChipDrawable(ChipDrawable.createFromResource(this, R.xml.suggestion_item));
        }
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics()
        );
        chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        chip.setText(text);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entryChipGroup.removeView(chip);
            }
        });
        return chip;
    }
    /********************************************************/

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    /***********************************************************/
    // Searching for a stop
    @Override
    public void afterTextChanged(Editable s) {
        busRoutesRV.setVisibility(View.VISIBLE);
        layout_empty.setVisibility(View.GONE);

        String val = s.toString().trim().toLowerCase();
        //if the search bar has no texts or cleared.
        if (val.equals("")) {
            clearIV.setVisibility(View.GONE);
            //No Suggestion chips.
            chipCloud.removeAllViews();
            try {
                recyclerList.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Show all the bus routes.
            for (BusRoute b : busRoutesList) {
                Collections.addAll(recyclerList, b);
            }

            if(adapter!=null) {
                adapter.notifyDataSetChanged();
            }
        }
        //if the search key is a number then show route with respective number(i.e., for search key '6' shows 'route 6')
        else if (Character.isDigit(val.charAt(0))) {
            clearIV.setVisibility(View.VISIBLE);

            //No suggestion chips.
            chipCloud.removeAllViews();

            //Add routes with number that matches the search key to the list.
            recyclerList.clear();
            for (BusRoute b : busRoutesList) {
                if (b.getName().equalsIgnoreCase(val))
                    Collections.addAll(recyclerList, b);
            }

            //check whether the routes list is empty or has some routes.(i.e., for search key '6' adds 'route 6' But for search key '88' there is nothing to add to the list)
            if (recyclerList.size() == 0) {
                //Show sorry msg.
                busRoutesRV.setVisibility(View.GONE);
                layout_empty.setVisibility(View.VISIBLE);
            } else {
                //Show the bus route.
                busRoutesRV.setVisibility(View.VISIBLE);
                layout_empty.setVisibility(View.GONE);
                //refresh adapter to show results.
                if(adapter!=null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }
        //if the search key is a String then show bus route with respective Bus Stops that match the string
        else {
            clearIV.setVisibility(View.VISIBLE);

            //Show suggestion chips for bus stops available in our JSON list.(assets/data_bus.json)
            if (val.length() > 2) {
                chipCloud.removeAllViews();
                chipCloud.setVisibility(View.VISIBLE);

                for (int i = 0; i < suggestions.size(); i++) {
                    if (suggestions.get(i).toLowerCase().contains(val.toLowerCase())) {
                        final Chip chip = getChip(chipCloud, suggestions.get(i));
                        chipCloud.addView(chip);
                        final int finalI = i;

                        //when selecting one of the suggested chip, make that string as the search key and display routes with that bus stops.
                        chip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CommonUtils.hideKeyboard(BusRoutesActivity.this);
                                chipCloud.removeAllViews();

                                //Set the string as search key in the Search edittext.
                                et_num.removeTextChangedListener(BusRoutesActivity.this);
                                et_num.setText(suggestions.get(finalI));
                                et_num.addTextChangedListener(BusRoutesActivity.this);

                                //Add routes with Bus Stops that matches the search key to the list.
                                recyclerList.clear();
                                for (BusRoute b : busRoutesList) {
                                    for (String s : b.getStop()) {
                                        if (s.equalsIgnoreCase(suggestions.get(finalI)))
                                            Collections.addAll(recyclerList, b);
                                    }
                                }

                                //refresh the adapter to show the results.
                                if(adapter!=null) {
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }
                if (chipCloud.getChildCount() == 0) {
                    CommonUtils.hideKeyboard(BusRoutesActivity.this);
                    busRoutesRV.setVisibility(View.GONE);
                    layout_empty.setVisibility(View.VISIBLE);
                } else {
                    busRoutesRV.setVisibility(View.VISIBLE);
                    layout_empty.setVisibility(View.GONE);
                }
            } else {
                chipCloud.removeAllViews();
            }
        }
    }
    /********************************************************/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Bungee.slideRight(BusRoutesActivity.this);
    }

    /***********************************************************/
    // Getting the bus route details from the JSON file.(assets/data_bus.json)
    public class getBusRoute extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("data_bus.json")));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                final Set<String> linkedHashSet = new LinkedHashSet<>();
                JSONArray arr = new JSONArray(sb.toString());
                busRoutesList.clear();
                suggestions.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = (JSONObject) arr.get(i);
                    BusRoute bus = new Gson().fromJson(obj.toString(), BusRoute.class);
                    busRoutesList.add(bus);
                    ArrayList<String> stop = (ArrayList<String>) bus.getStop();
                    for (String s : stop) {
                        linkedHashSet.add(s);
                    }
                }

                for (String s : linkedHashSet) {
                    suggestions.add(s);
                }
                Collections.sort(busRoutesList);
                recyclerList = new ArrayList<BusRoute>(busRoutesList);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new BusRouteAdapter(getApplicationContext(), recyclerList);
                        busRoutesRV.setAdapter(adapter);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
    /***********************************************************/

}
