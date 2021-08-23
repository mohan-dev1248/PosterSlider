package com.asura.library.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EmptyViewFragment extends Fragment {

    private int layout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout = getArguments().getInt("layout");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(layout, container, false);
    }

    public static EmptyViewFragment newInstance(@LayoutRes int layout) {
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        EmptyViewFragment fragment = new EmptyViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
