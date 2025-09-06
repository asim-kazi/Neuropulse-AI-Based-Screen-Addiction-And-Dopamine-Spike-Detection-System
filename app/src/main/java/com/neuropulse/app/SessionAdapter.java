package com.neuropulse.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private List<SessionEntity> sessions;

    public SessionAdapter(List<SessionEntity> sessions) {
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        SessionEntity session = sessions.get(position);

        // Show app name (converted from package name)
        String appName = Utils.pkgToAppName(holder.itemView.getContext(), session.appPackage);
        holder.appName.setText(appName);

        // Convert timestamp to readable date
        String formattedTime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                .format(new Date(session.timestamp));
        holder.timestamp.setText(formattedTime);

        // Show duration in minutes and seconds
        long totalSeconds = session.duration / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        String durationText;
        if (minutes > 0) {
            durationText = minutes + "m " + seconds + "s";
        } else {
            durationText = seconds + "s";
        }
        holder.duration.setText("Duration: " + durationText);

        // Show additional info: category, risk, and flags
        StringBuilder extraInfo = new StringBuilder();
        extraInfo.append("Category: ").append(session.appCategory);
        extraInfo.append(" • Risk: ").append(session.addictionRisk);

        if (session.bingeFlag == 1) {
            extraInfo.append(" • BINGE");
        }
        if (session.nightFlag == 1) {
            extraInfo.append(" • NIGHT");
        }
        if (session.dopamineSpikeLabel == 1) {
            extraInfo.append(" • SPIKE");
        }

        holder.extraInfo.setText(extraInfo.toString());

        // Set text color based on addiction risk
        switch (session.addictionRisk) {
            case "High":
                holder.appName.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                break;
            case "Medium":
                holder.appName.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "Low":
            default:
                holder.appName.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return sessions != null ? sessions.size() : 0;
    }

    public void updateSessions(List<SessionEntity> newSessions) {
        this.sessions = newSessions;
        notifyDataSetChanged();
    }

    public static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView appName, timestamp, duration, extraInfo;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.tvAppName);
            timestamp = itemView.findViewById(R.id.tvTimestamp);
            duration = itemView.findViewById(R.id.tvDuration);
            extraInfo = itemView.findViewById(R.id.tvExtraInfo);
        }
    }
}