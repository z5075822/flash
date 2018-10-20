package com.example.vinguyen.assignmentprototype;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.Context.BIND_AUTO_CREATE;

public class MainActivity extends Fragment implements SpeechDialogFragment.SpeechDialogListener {
    private static final String TAG = "MainActivity";
    private GoogleTranslate translator;
    private TextView translatabletext, contentTitle;
    private CharSequence selectedText = "";
    private String value = "", mContent = "", mContentTitle = "";
    private ArrayList<String> contentArray = new ArrayList<>();

    private Button btnClear;
    private FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        value = getArguments().getString("Key");
        Log.d(TAG, "onCreateView: " + value);
        return inflater.inflate(R.layout.activity_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        View view = getView();
        contentTitle = view.findViewById(R.id.contentTitle);
        initContent(value);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSpeechDialog();
            }
        });

        translatabletext = view.findViewById(R.id.translatabletext);
        translatabletext.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Called when action mode is first created. The menu supplied
                // will be used to generate action buttons for the action mode

                // Here is an example MenuItem
                menu.add(0, 1, 0, "Translate");
                menu.add(0, 2, 0, "Add To Notes");

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                menu.removeItem(android.R.id.shareText);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case 1:
                        int min = 0;
                        int max = translatabletext.getText().length();
                        if (translatabletext.isFocused()) {
                            final int selStart = translatabletext.getSelectionStart();
                            final int selEnd = translatabletext.getSelectionEnd();

                            min = Math.max(0, Math.min(selStart, selEnd));
                            max = Math.max(0, Math.max(selStart, selEnd));
                        }
                        // Perform your definition lookup with the selected text
                        selectedText = translatabletext.getText().subSequence(min, max);
                        Log.d(TAG, "onActionItemClicked: " + selectedText);
                        new EnglishToChinese().execute();
                        // Finish and close the ActionMode
                        mode.finish();
                        return true;
                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        btnClear = view.findViewById(R.id.btnClear);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spannable WordtoSpan = new SpannableString(translatabletext.getText());
                BackgroundColorSpan[] spans = WordtoSpan.getSpans(0, WordtoSpan.length(), BackgroundColorSpan.class);
                for (BackgroundColorSpan span : spans) {
                    WordtoSpan.removeSpan(span);
                }
                translatabletext.setText(WordtoSpan, TextView.BufferType.SPANNABLE);
            }
        });

    }

    private void showSpeechDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SpeechDialogFragment speechDialogFragment = SpeechDialogFragment.newInstance("Listening...");
        speechDialogFragment.setTargetFragment(MainActivity.this, 300);
        speechDialogFragment.show(fm, "speech_dialog_fragment");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        if (mAdapter != null) {
//            outState.putStringArrayList(STATE_RESULTS, mAdapter.getResults());
//        }
    }

    @Override
    public void onFinishSpeechDialog(String searchPhrase) {
        String textViewString = translatabletext.getText().toString().toLowerCase();
        int ofe = textViewString.indexOf(searchPhrase, 0);
        Spannable WordtoSpan = new SpannableString(translatabletext.getText());
        BackgroundColorSpan[] spans = WordtoSpan.getSpans(0, WordtoSpan.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : spans) {
            WordtoSpan.removeSpan(span);
        }

        for (int ofs = 0; ofs < textViewString.length() && ofe != -1; ofs = ofe + 1) {
            ofe = textViewString.indexOf(searchPhrase, ofs);
            if (ofe == -1)
                break;
            else {
                WordtoSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), ofe, ofe + searchPhrase.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                translatabletext.setText(WordtoSpan, TextView.BufferType.SPANNABLE);
            }
        }
    }


    private class EnglishToChinese extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        protected void onError(Exception ex) {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                translator = new GoogleTranslate("AIzaSyBDSlMKtpmSXvu8KN6-7XuFj46DR1eGpwU");
                Thread.sleep(1000);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            //start the progress dialog
            progress = ProgressDialog.show(getActivity(), null, "Translating...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.dismiss();
            super.onPostExecute(result);
            translated();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    public void translated() {
        String text = translator.translate(selectedText.toString(), "en", "zh-CN");
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    public void initContent(String topic) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference topicsRef = rootRef.child("INFS3604").child("Topics").child(topic).child("Content");
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mContent = ds.getValue().toString();
                    String mContentBreak = mContent.replace("_b", "<br/>");
//                    String mContentPoint = mContentBreak.replace("_p", "\u2022");
//                    Log.d(TAG, "onDataChange: " + mContentFinal);
                    mContentTitle = ds.getKey();
                    String stringTitle = mContentTitle.substring(0,1) + ". " + mContentTitle.substring(2,mContentTitle.length());
                    contentArray.add(stringTitle);
                    contentArray.add(mContentBreak);
                    //translatabletext.setText(mContentFinal);
                    //contentTitle.setText(mContentTitle);
                }
                String contentString = "";
                for (int i=0; i < contentArray.size(); i++) {
                    if ((i%2 ==0) || (i==0)){
                        contentString += "<b>" + contentArray.get(i) + "</b>";
                    }
                    else {
                        contentString += "<p>" + contentArray.get(i) + "</p>";
                    }
//                    contentString += contentArray.get(i) + "\n\n";
                }
                translatabletext.setText(Html.fromHtml(contentString));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        topicsRef.addValueEventListener(eventListener);
    }
}
