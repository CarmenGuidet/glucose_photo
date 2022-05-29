package com.carmenguidetgomez.glucemyphoto;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carmenguidetgomez.glucemyphoto.Glucose;
import com.carmenguidetgomez.glucemyphoto.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;



public class GlucoseAdapter extends FirestoreRecyclerAdapter<Glucose, GlucoseAdapter.GlucoseHolder> {

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     */
    public GlucoseAdapter(@NonNull FirestoreRecyclerOptions<Glucose> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull GlucoseHolder holder, int position, @NonNull Glucose model) {

        holder.textViewGlucose.setText(String.valueOf(model.getGlucose()));

        holder.textViewDate.setText(String.valueOf(model.getDate()));

    }

    @NonNull
    @Override
    public GlucoseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.glucose_item, parent, false);

        return new GlucoseHolder(v);
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    static class GlucoseHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewGlucose;
        TextView textViewMedia;
        TextView textViewDate;


        public GlucoseHolder(View itemView) {
            super(itemView);
            //textViewName = itemView.findViewById(R.id.text_view_name);
            textViewGlucose = itemView.findViewById(R.id.text_view_glucose);
            //textViewMedia = itemView.findViewById(R.id.text_view_media);
            textViewDate = itemView.findViewById(R.id.text_view_date);

        }
    }
}

