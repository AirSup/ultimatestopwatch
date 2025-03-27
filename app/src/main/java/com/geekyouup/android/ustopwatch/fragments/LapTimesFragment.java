package com.geekyouup.android.ustopwatch.fragments;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import android.view.Menu;
import android.view.MenuItem;

import androidx.fragment.app.ListFragment;

import com.geekyouup.android.ustopwatch.R;
import com.geekyouup.android.ustopwatch.UltimateStopwatchActivity;

public class LapTimesFragment extends ListFragment implements LapTimeListener {

    private LapTimesBaseAdapter mAdapter;
    private ArrayList<LapTimeBlock> mLapTimes = new ArrayList<>();
    private LapTimeRecorder mLapTimeRecorder;
    private ArrayList<Integer> mCheckedItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLapTimeRecorder = LapTimeRecorder.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.laptimes_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        ListView listView = getListView();
        listView.setCacheColorHint(Color.WHITE);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        setupMultiChoiceSelect(listView);

        mAdapter = new LapTimesBaseAdapter(getActivity(), mLapTimes);
        super.setListAdapter(mAdapter);

        //noinspection DataFlowIssue
        ((UltimateStopwatchActivity) getActivity()).registerLapTimeFragment(this);

    }

    private void setupMultiChoiceSelect(ListView listView) {
        final LapTimesFragment ltf = this;
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
                if (mCheckedItems == null) mCheckedItems = new ArrayList<>();
                if (checked) {
                    mCheckedItems.add(position);
                } else {
                    mCheckedItems.remove(Integer.valueOf(position));
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.menu_laptimes_contextual, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                // Respond to clicks on the actions in the CAB
                if (menuItem.getItemId() == R.id.menu_context_delete) {
                    mLapTimeRecorder.deleteLapTimes(mCheckedItems, ltf);
                    actionMode.finish(); // Action picked, so close the CAB
                    mCheckedItems.clear();
                    mCheckedItems = null;
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
            }
        });

        //on long touch start the contextual actionbar
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //noinspection DataFlowIssue
                getActivity().startActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return false;
                    }
                });
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // if vars stored then use them
        mLapTimes.clear();
        mLapTimes.addAll(mLapTimeRecorder.getTimes());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * @noinspection unused
     */
    public void reset() {
        mLapTimes.clear();
        mAdapter.notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void lapTimesUpdated() {
        if (mLapTimeRecorder == null) mLapTimeRecorder = LapTimeRecorder.getInstance();
        if (mLapTimes == null) mLapTimes = new ArrayList<>();

        mLapTimes.clear();
        mLapTimes.addAll(mLapTimeRecorder.getTimes());
        notifyDataSetChanged();
    }
}
