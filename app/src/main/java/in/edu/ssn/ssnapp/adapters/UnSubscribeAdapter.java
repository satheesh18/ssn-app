package in.edu.ssn.ssnapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import in.edu.ssn.ssnapp.ClubPageActivity;
import in.edu.ssn.ssnapp.R;
import in.edu.ssn.ssnapp.models.Club;
import in.edu.ssn.ssnapp.utils.Constants;
import in.edu.ssn.ssnapp.utils.FCMHelper;
import in.edu.ssn.ssnapp.utils.SharedPref;
import spencerstudios.com.bungeelib.Bungee;

//this Adapter is Used in ClubFragment to Show the Clubs that are Not-Subscribed by the user.
public class UnSubscribeAdapter extends RecyclerView.Adapter<UnSubscribeAdapter.FeedViewHolder> {

    private List<Club> clubs;
    private Context context;
    private boolean darkMode;

    public UnSubscribeAdapter(Context context, List<Club> clubs) {
        this.context = context;
        this.clubs = clubs;
        //get darkmode preference
        darkMode = SharedPref.getBoolean(context, "dark_mode");
    }

    @NonNull
    @Override
    public UnSubscribeAdapter.FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        //check if the darkmode enabled and open respective Item layout.
        if (darkMode)
            view = LayoutInflater.from(context).inflate(R.layout.club_item_dark, parent, false);
        else
            view = LayoutInflater.from(context).inflate(R.layout.club_item, parent, false);

        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UnSubscribeAdapter.FeedViewHolder holder, final int position) {

        final Club model = clubs.get(position);
        holder.nameTV.setText(model.getName());
        holder.descriptionTV.setText(model.getDescription());

        //Subscribe to the club's notifications in Firebase cloud messaging.
        FCMHelper.UnSubscribeToTopic(context, "club_" + model.getId());

        //Set club DP.
        try {
            Glide.with(context).load(model.getDp_url()).placeholder(R.color.shimmering_back).into(holder.dpIV);
        } catch (Exception e) {
            holder.dpIV.setImageResource(R.color.shimmering_back);
        }

        //Subscribing to CLub By clicking on Bell icon.
        holder.lottie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore.getInstance().collection(Constants.collection_club).document(model.getId()).update("followers", FieldValue.arrayUnion(SharedPref.getString(context, "email")));
                clubs.remove(position);
                notifyDataSetChanged();
            }
        });

        //clicking on the entire Card proceeds to club activity
        holder.club_RL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ClubPageActivity.class);
                intent.putExtra("data", model);
                context.startActivity(intent);
                Bungee.slideLeft(context);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clubs.size();
    }

    public class FeedViewHolder extends RecyclerView.ViewHolder {
        LinearLayout club_RL;
        TextView nameTV, descriptionTV;
        ImageView dpIV;
        LottieAnimationView lottie;

        public FeedViewHolder(View convertView) {
            super(convertView);

            nameTV = convertView.findViewById(R.id.nameTV);
            descriptionTV = convertView.findViewById(R.id.descriptionTV);
            dpIV = convertView.findViewById(R.id.dpIV);
            lottie = convertView.findViewById(R.id.lottie);
            club_RL = convertView.findViewById(R.id.club_RL);

            lottie.setProgress(0.0f);
        }
    }
}
