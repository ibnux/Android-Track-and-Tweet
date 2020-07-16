package com.ibnux.trackandtweet.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ibnux.trackandtweet.R;

public class WarningFragment extends BottomSheetDialogFragment {

    public static WarningFragment newInstance() {
        return new WarningFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_warning_list_dialog, container, false);
    }


}