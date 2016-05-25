package com.droletours.com.standdetector;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by Rune on 14-05-2016.
 */
public class UsernameDialogFragment extends DialogFragment implements TextView.OnEditorActionListener, View.OnClickListener {
    private EditText mUserNameEditText;
    private EditText mGroupNameEditText;
    private RadioGroup mRadioGroup;
    private Button mButton;
    private View parent;

    public UsernameDialogFragment() {
    }

    public interface UserGroupNameListener{
        void onFinishDialog(String user, String group);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_username, container);
        parent = view;
        mUserNameEditText = (EditText) view.findViewById(R.id.user_name_edit_text);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.radio_group_collective_individual);
        mButton = (Button) view.findViewById(R.id.button_update_username_groupname);
        mButton.setOnClickListener(this);

        mUserNameEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().setTitle("Please enter user and group name");

        return view;
    }

    @Override
    public void onClick(View v){
        UserGroupNameListener activity = (UserGroupNameListener) getActivity();
        String groupname = ((RadioButton)parent.findViewById( mRadioGroup.getCheckedRadioButtonId())).getText().toString().toLowerCase();
        activity.onFinishDialog(mUserNameEditText.getText().toString(), groupname);
        this.dismiss();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        UserGroupNameListener activity = (UserGroupNameListener) getActivity();
        activity.onFinishDialog(mUserNameEditText.getText().toString(), mGroupNameEditText.getText().toString());
        this.dismiss();
        return true;
    }
}
