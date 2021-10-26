package org.intelehealth.msfarogyabharat.activities.missedCallResponseActivity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.models.IVR_Call_Models.Call_Details_Response;


/**
 * Created By: Ritika Rathore on 21-10-21
 */

public class MissCallResponse_Adapter extends RecyclerView.Adapter<MissCallResponse_Adapter.MyViewHolder> {
    private Context mcontext;
    private Call_Details_Response m_call_details_response;

    public MissCallResponse_Adapter(Context context, Call_Details_Response call_details_response) {
        this.mcontext = context;
        this.m_call_details_response = call_details_response;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_card_ivr_response, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mobile_no.setText(m_call_details_response.getData().get(position).getCallto());
        holder.status_response.setText(m_call_details_response.getData().get(position).getStatus());

        if (holder.status_response.getText().toString().equalsIgnoreCase("ANSWER")) {
            holder.status_response.setTextColor(Color.parseColor("#00A300"));
        } else {
            holder.status_response.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return m_call_details_response.getData().size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView mobile_no, status_response;

        public MyViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardview_response);
            mobile_no = itemView.findViewById(R.id.mobile_no_textview);
            status_response = itemView.findViewById(R.id.status_response_textview);
        }
    }
}
