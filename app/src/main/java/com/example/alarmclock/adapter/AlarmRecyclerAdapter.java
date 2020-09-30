package com.example.alarmclock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.alarmclock.MainActivity;
import com.example.alarmclock.R;
import com.example.alarmclock.database.AlarmDbHelper;
import com.example.alarmclock.model.AlarmModel;

import java.util.List;


public class AlarmRecyclerAdapter
        extends RecyclerView.Adapter<AlarmRecyclerAdapter.ViewHolder> {

    private List<AlarmModel> mDataSet;

    // variable to hold (Fragment fragment when using fragment)
    private Context context;


    private OnItemLongClickListener mOnItemLongClickListener;


    public class ViewHolder extends RecyclerView.ViewHolder
        implements View.OnLongClickListener {



        public View view;
        public TextView mTitle, mTime;
        public Switch aSwitch;
        public Button delBtn;

        public ViewHolder(View v) {
            super(v);
            view = v;
            mTitle = v.findViewById(R.id.alarm_title);
            mTime = v.findViewById(R.id.alarm_time);
            aSwitch = v.findViewById(R.id.alarm_switch);
            delBtn = v.findViewById(R.id.alarm_deleteBtn);

            v.setOnLongClickListener(this);
        }


        @Override
        public boolean onLongClick(View view) {
            if (mOnItemLongClickListener != null) mOnItemLongClickListener.onItemSelected(view, getAdapterPosition());
            return false;
        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public AlarmRecyclerAdapter(Context context, List<AlarmModel> data) {
        this.context = context;
        this.mDataSet = data;

        this.notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AlarmRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.alarm_row_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element


        // Set alarm title
        holder.mTitle.setText(String.format("Alarm Clock %d", mDataSet.get(position).getItemID()));


        // Get Hour and Minute
        int hourOfDay = mDataSet.get(position).getHour();
        final int hour = hourOfDay % 12;
        final int min = mDataSet.get(position).getMinute();

        holder.mTime.setText(
                String.format("%02d:%02d %s", hour == 0 ? 12 : hour, min, hourOfDay < 12 ? "A.M" : "P.M")); // change format 12hour


        // Change status for Switch
        if (mDataSet.get(position).isEnable() == 1) {
            holder.aSwitch.setChecked(true);
        } else {
            holder.aSwitch.setChecked(false);
        }

        // Control Switch
        holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                AlarmDbHelper sqLiteDb = new AlarmDbHelper(holder.aSwitch.getContext());
                sqLiteDb.close();

                AlarmModel alarmModel = mDataSet.get(holder.getAdapterPosition());

                if (compoundButton.isChecked()) {
                    // Change true(1) status
                    mDataSet.get(holder.getAdapterPosition()).setEnable(1);

                    // Update database
                    sqLiteDb.updateAlarm(alarmModel.getItemID(), alarmModel.getHour(), alarmModel.getMinute(),1);

                    // Set alarm again
                    MainActivity main = new MainActivity();
                    main.setAlarm(context, alarmModel.getItemID(), alarmModel.getHour(), alarmModel.getMinute());

                } else {
                    // Change false(0) status
                    mDataSet.get(holder.getAdapterPosition()).setEnable(0);

                    // Cancel alarm
                    mDataSet.get(holder.getAdapterPosition()).cancelAlarm(holder.aSwitch.getContext());

                    // Update database
                    sqLiteDb.updateAlarm(alarmModel.getItemID(), alarmModel.getHour(), alarmModel.getMinute(), 0);

                }


            }
        });

        // Delete the alarm
        holder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Using holder.getAdapterPosition() to get exactly where is position after it was removed

                AlarmDbHelper sqLiteDb = new AlarmDbHelper(view.getContext());
                sqLiteDb.deleteAlarm(mDataSet.get(holder.getAdapterPosition()).getItemID());
                sqLiteDb.close();


                // Cancel Intent
                mDataSet.get(holder.getAdapterPosition()).cancelAlarm(view.getContext());

                // Remove from RecyclerView
                mDataSet.remove(holder.getAdapterPosition());

                notifyItemRemoved(position);

            }
        });


    }

    public List<AlarmModel> getData() {
        return mDataSet;
    }


    public void setData(List<AlarmModel> data) {
        this.mDataSet = data;
        notifyDataSetChanged();
    }


    public AlarmModel getItem(int position) {
        return mDataSet.get(position);
    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


    // Allows clicks events to be caught
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }


    // Parent activity will implement this method to respond to click events
    public interface OnItemLongClickListener {
        void onItemSelected(View view, int position);
    }

}