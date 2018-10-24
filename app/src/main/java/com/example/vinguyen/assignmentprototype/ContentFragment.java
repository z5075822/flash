package com.example.vinguyen.assignmentprototype;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.vinguyen.assignmentprototype.Model.Topic;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ContentFragment extends Fragment implements SpeechDialogFragment.SpeechDialogListener {
    private static final String TAG = "ContentFragment";
    private GoogleTranslate translator;
    private TextView textViewContent, textViewContentTitle;
    private CharSequence selectedText = "";
    private String mTopicID = "", mTopicTitle ="", mContent = "", mContentTitle = "";
    private ArrayList<String> contentArray = new ArrayList<>();
    private Button btnClear, btnTest, btnFlashcards;
    private FloatingActionButton fab;
    private ImageView imageViewTopic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mTopicID = getArguments().getString("topicID");
        mTopicTitle = getArguments().getString("title");
        return inflater.inflate(R.layout.fragment_content, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        View view = getView();
        textViewContentTitle = view.findViewById(R.id.textViewContentTitle);
        initContent(mTopicID);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSpeechDialog();
            }
        });

        textViewContent = view.findViewById(R.id.textViewContent);
        textViewContent.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
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
                int min = 0;
                int max = textViewContent.getText().length();
                if (textViewContent.isFocused()) {
                    final int selStart = textViewContent.getSelectionStart();
                    final int selEnd = textViewContent.getSelectionEnd();

                    min = Math.max(0, Math.min(selStart, selEnd));
                    max = Math.max(0, Math.max(selStart, selEnd));
                }
                selectedText = textViewContent.getText().subSequence(min, max);
                switch (item.getItemId()) {
                    case 1:
                        new EnglishToChinese().execute();
                        mode.finish();
                        return true;
                    case 2:
                        addToNotes(selectedText.toString());
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
                Spannable WordtoSpan = new SpannableString(textViewContent.getText());
                BackgroundColorSpan[] spans = WordtoSpan.getSpans(0, WordtoSpan.length(), BackgroundColorSpan.class);
                for (BackgroundColorSpan span : spans) {
                    WordtoSpan.removeSpan(span);
                }
                textViewContent.setText(WordtoSpan, TextView.BufferType.SPANNABLE);
            }
        });

        btnFlashcards = view.findViewById(R.id.btnFlashCards);

        btnFlashcards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlashcardsFragment fragment = new FlashcardsFragment();
                Bundle args = new Bundle();
                args.putString("topicID", mTopicID);
                args.putString("title", mTopicTitle);
                FragmentManager manager = getFragmentManager();
                fragment.setArguments(args);
                manager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("content").commit();
            }
        });

        btnTest = view.findViewById(R.id.btnTest);

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuestionsFragment fragment = new QuestionsFragment();
                Bundle args = new Bundle();
                args.putString("Key", mTopicID);
                FragmentManager manager = getFragmentManager();
                fragment.setArguments(args);
                manager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("content").commit();
            }
        });

        imageViewTopic = view.findViewById(R.id.imageViewTopic);

        initImage(mTopicID);
    }

    private void showSpeechDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SpeechDialogFragment speechDialogFragment = SpeechDialogFragment.newInstance("Listening...");
        speechDialogFragment.setTargetFragment(ContentFragment.this, 300);
        speechDialogFragment.show(fm, "speech_dialog_fragment");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onFinishSpeechDialog(String searchPhrase) {
        String textViewString = textViewContent.getText().toString().toLowerCase();
        int ofe = textViewString.indexOf(searchPhrase, 0);
        Spannable WordtoSpan = new SpannableString(textViewContent.getText());
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
                textViewContent.setText(WordtoSpan, TextView.BufferType.SPANNABLE);
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
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        mContent = ds.getValue().toString();
                        String mContentBreak = mContent.replace("_b", "<br/>");
                        mContentTitle = ds.getKey();
                        String stringTitle = mContentTitle.substring(0, 1) + ". " + mContentTitle.substring(2, mContentTitle.length());
                        contentArray.add(stringTitle);
                        contentArray.add(mContentBreak);
                    }
                    String contentString = "";
                    for (int i = 0; i < contentArray.size(); i++) {
                        if ((i % 2 == 0) || (i == 0)) {
                            contentString += "<b>" + contentArray.get(i) + "</b>";
                        } else {
                            contentString += "<p>" + contentArray.get(i) + "</p>";
                        }
                    }
                    textViewContent.setText(Html.fromHtml(contentString));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        topicsRef.addValueEventListener(eventListener);

    }

    public void addToNotes(final String selectedText) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference notesRef = rootRef.child("Users").child(user.getUid()).child("Notes");
        Query lastQuery = notesRef.orderByKey().limitToLast(1);
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.exists()) {
                            Integer key = Integer.parseInt(ds.getKey().toString());
                            key += 10;
                            notesRef.child(Integer.toString(key)).setValue(selectedText);
                        }
                    }
                } else {
                    notesRef.child("0").setValue(selectedText);
                }
                Toast.makeText(getActivity(), "Added to Notes!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void initImage(final String topicID) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imageRef = rootRef.child("INFS3604").child("Topics").child(topicID).child("Image");
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.getValue().toString());
                    if (dataSnapshot.getValue().toString().equals("Exists")) {
                        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(topicID + ".png");
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (getActivity() != null) {
                                    Glide.with(getActivity()).using(new FirebaseImageLoader())
                                            .load(storageReference)
                                            .into(imageViewTopic);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: ", e);
                            }
                        });
                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        imageRef.addListenerForSingleValueEvent(eventListener);
    }
}
