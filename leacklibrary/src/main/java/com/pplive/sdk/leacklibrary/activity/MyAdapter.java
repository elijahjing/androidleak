package com.pplive.sdk.leacklibrary.activity;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.pplive.sdk.leacklibrary.R;
import com.pplive.sdk.leacklibrary.analysis.Leak;
import com.pplive.sdk.leacklibrary.heap.AnalysisResult;

import java.util.List;

import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.Formatter.formatShortFileSize;

public class MyAdapter extends BaseAdapter {
    private List<Leak> leaks;
    private LayoutInflater inflater;
    private Context context;
    private OnClick onClick;

    public MyAdapter(List<Leak> leaks, Context context) {
        this.context = context;
        this.leaks = leaks;
        this.inflater = LayoutInflater.from(context);

    }

    public void setData(List<Leak> leaks) {
        this.leaks = leaks;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return leaks.size();
    }

    @Override
    public Object getItem(int position) {
        return leaks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.layout_title_iteam, null);
            viewHolder.name = view.findViewById(R.id.name);
            viewHolder.time = view.findViewById(R.id.time);
            viewHolder.size = view.findViewById(R.id.size);
            viewHolder.delete = view.findViewById(R.id.delete);
            viewHolder.detail = view.findViewById(R.id.detail);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        Leak leak = (Leak) getItem(position);
        viewHolder.name.setText(leak.result.className);
        viewHolder.time.setText(getTime(leak.resultFileLastModified));
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClick != null) {
                    onClick.delete(position);
                }
            }
        });
        viewHolder.detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClick != null) {
                    onClick.onClick(position);
                }
            }
        });
        if(position==0){
            viewHolder.detail.requestFocus();
        }
        if (leak.result.retainedHeapSize != AnalysisResult.RETAINED_HEAP_SKIPPED) {
            String size = formatShortFileSize(context, leak.result.retainedHeapSize);
            viewHolder.size.setText(size);
        }
        return view;
    }

    public void setOnDelete(OnClick onClick) {
        this.onClick = onClick;
    }

    public interface OnClick {
        void delete(int pos);
        void onClick(int pos);

    }

    public class ViewHolder {
        TextView name;
        TextView time;
        TextView size;
        Button delete,detail;
    }

    public String getTime(long time) {
        return DateUtils.formatDateTime(context, time,
                FORMAT_SHOW_TIME);
    }
}
