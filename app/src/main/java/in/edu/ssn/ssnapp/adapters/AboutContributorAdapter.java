package in.edu.ssn.ssnapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import in.edu.ssn.ssnapp.ContributorProfileActivity;
import in.edu.ssn.ssnapp.R;
import in.edu.ssn.ssnapp.models.TeamDetails;
import in.edu.ssn.ssnapp.utils.SharedPref;

//this Adapter is Used in AppInfoActivity to Show the info about Final and Pre-final year Student Contributors.
public class AboutContributorAdapter extends RecyclerView.Adapter<AboutContributorAdapter.ContributionViewHolder> {

    boolean darkMode = false;
    private ArrayList<TeamDetails> teamDetails;
    private Context context;

    public AboutContributorAdapter(Context context, ArrayList<TeamDetails> teamDetails) {
        this.context = context;
        this.teamDetails = teamDetails;
        //get darkmode preference
        darkMode = SharedPref.getBoolean(context, "dark_mode");
    }

    @NonNull
    @Override
    public AboutContributorAdapter.ContributionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem;

        //check if the darkmode enabled and open respective Item layout.
        if (darkMode) {
            listItem = layoutInflater.inflate(R.layout.contributor_item_dark, parent, false);
        } else {
            listItem = layoutInflater.inflate(R.layout.contributor_item, parent, false);
        }
        return new AboutContributorAdapter.ContributionViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull final AboutContributorAdapter.ContributionViewHolder holder, int position) {
        final TeamDetails drawer = teamDetails.get(position);

        holder.nameTV.setText(drawer.getName());
        holder.positionTV.setText(drawer.getPosition());
        holder.dpIV.setImageResource(drawer.getDp());

        //clicking the item proceeds to Contributor Profile Screen.
        holder.containerRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ContributorProfileActivity.class);
                intent.putExtra("Contributor", drawer);
                ActivityOptionsCompat options = null;

                //Dp itransition animation
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            (Activity) context,
                            holder.dpIV,
                            holder.dpIV.getTransitionName()
                    );
                    ActivityCompat.startActivity(context, intent, options.toBundle());
                } else
                    context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return teamDetails.size();
    }

    public class ContributionViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTV, positionTV;
        public ImageView dpIV, img1IV, img2IV, img3IV;
        public RelativeLayout containerRL;

        public ContributionViewHolder(View convertView) {
            super(convertView);

            nameTV = convertView.findViewById(R.id.nameTV);
            positionTV = convertView.findViewById(R.id.positionTV);
            dpIV = convertView.findViewById(R.id.dpIV);

            containerRL = convertView.findViewById(R.id.containerRL);
        }
    }
}
