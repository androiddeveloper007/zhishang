package com.smartclothing.module_wefit.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.smartclothing.module_wefit.R;
import com.smartclothing.module_wefit.bean.Device;
import com.smartclothing.module_wefit.bean.Message;
import com.vondear.rxtools.dateUtils.RxTimeUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by ZZP on 2018/5/22.
 */

public class DeviceRvAdapter extends BaseMultiItemQuickAdapter<Device, BaseViewHolder> {

    private deleteClickListener deleteClickListener;
    private addDeviceClickListener addDeviceClickListener;

    public DeviceRvAdapter() {
        super(null);
        addItemType(0, R.layout.viewstub_device_scale_nodata);
        addItemType(1, R.layout.viewstub_device_scale_hasdata);
    }

    @Override
    protected void convert(final BaseViewHolder helper, Device item) {
        switch(helper.getItemViewType()){
            case 0:
                LinearLayout device_no_device_add = helper.convertView.findViewById(R.id.device_no_device_add);
                device_no_device_add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(addDeviceClickListener!=null) {
                            addDeviceClickListener.onAddDeviceClick();
                        }
                    }
                });
                break;
            case 1:
                TextView tv_device_id_scale = helper.convertView.findViewById(R.id.tv_device_id_scale);
                TextView tv_residual_electricity_scale = helper.convertView.findViewById(R.id.tv_residual_electricity_scale);
                TextView tv_standby_time_scale = helper.convertView.findViewById(R.id.tv_standby_time_scale);
                LinearLayout icon_device_delete = helper.convertView.findViewById(R.id.icon_device_delete);
                View line = helper.convertView.findViewById(R.id.line);
                icon_device_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(deleteClickListener!=null) {
                            deleteClickListener.onUpdateClick(helper.getLayoutPosition());
                        }
                    }
                });
                if(helper.getLayoutPosition() == getData().size()-1) {
                    line.setVisibility(View.GONE);
                } else {
                    line.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    public interface deleteClickListener {
        void onUpdateClick(int position);
    }

    public interface addDeviceClickListener {
        void onAddDeviceClick();
    }

    public void setUpdateClickListener(deleteClickListener listener) {
        deleteClickListener = listener;
    }
    public void setAddDeviceClickListener(addDeviceClickListener listener) {
        addDeviceClickListener = listener;
    }

}
