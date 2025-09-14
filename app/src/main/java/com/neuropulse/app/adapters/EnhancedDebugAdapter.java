package com.neuropulse.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.neuropulse.app.R;
import com.neuropulse.app.models.EnhancedDebugInfo;

public class EnhancedDebugAdapter extends RecyclerView.Adapter<EnhancedDebugAdapter.EnhancedViewHolder> {
    private EnhancedDebugInfo debugInfo;

    public void updateEnhancedInfo(EnhancedDebugInfo info) {
        this.debugInfo = info;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EnhancedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_enhanced_debug, parent, false);
        return new EnhancedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnhancedViewHolder holder, int position) {
        if (debugInfo == null || debugInfo.featureLabels == null ||
                debugInfo.featureValues == null || position >= debugInfo.featureLabels.length) {
            return;
        }

        String label = debugInfo.featureLabels[position];
        String value = debugInfo.featureValues[position];

        if (label == null || value == null) {
            return;
        }

        holder.bind(label, value);

        // Color coding for important metrics with null safety
        int colorResId = android.R.color.black; // default

        if (label.contains("Dopamine Risk") && debugInfo.prediction != null) {
            String riskLevel = debugInfo.prediction.getRiskLevel();
            if ("HIGH".equals(riskLevel)) {
                colorResId = android.R.color.holo_red_dark;
            } else if ("MEDIUM".equals(riskLevel)) {
                colorResId = android.R.color.holo_orange_dark;
            } else {
                colorResId = android.R.color.holo_green_dark;
            }
        } else if (label.contains("Addiction Level") && debugInfo.prediction != null) {
            switch (debugInfo.prediction.addictionLevel) {
                case 0: colorResId = android.R.color.holo_green_dark; break;
                case 1: colorResId = android.R.color.holo_orange_dark; break;
                case 2: colorResId = android.R.color.holo_red_dark; break;
            }
        } else if (label.contains("Binge Flag") && "YES".equals(value)) {
            colorResId = android.R.color.holo_red_dark;
        } else {
            colorResId = android.R.color.holo_blue_dark;
        }

        holder.valueText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), colorResId));

        // Add content description for accessibility
        holder.itemView.setContentDescription(label + ": " + value);
    }

    @Override
    public int getItemCount() {
        return debugInfo != null && debugInfo.featureLabels != null ? debugInfo.featureLabels.length : 0;
    }

    static class EnhancedViewHolder extends RecyclerView.ViewHolder {
        TextView labelText, valueText;

        public EnhancedViewHolder(@NonNull View itemView) {
            super(itemView);
            labelText = itemView.findViewById(R.id.textEnhancedLabel);
            valueText = itemView.findViewById(R.id.textEnhancedValue);
        }

        public void bind(String label, String value) {
            if (labelText != null) labelText.setText(label);
            if (valueText != null) valueText.setText(value);
        }
    }
}