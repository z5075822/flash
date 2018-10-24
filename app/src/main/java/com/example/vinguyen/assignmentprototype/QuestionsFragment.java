package com.example.vinguyen.assignmentprototype;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.vinguyen.assignmentprototype.Model.Question;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;

public class QuestionsFragment extends Fragment {
    private static final String TAG = "QuestionsFragment";

    private OnFragmentInteractionListener mListener;
    private Button btnOption1, btnOption2, btnOption3, btnOption4;
    private TextView textViewQuestion;
    private ImageView imageViewQuestion;
    private int total = 1, correct = 0, incorrect = 0, totalQuestions = 4, delay = 1500;
    private String answer = "";
    private ArrayList<String> options;
    private Question question;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private String value = "";
    private Activity activity;


    public QuestionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        value = getArguments().getString("Key");
        activity = getActivity();
        return inflater.inflate(R.layout.fragment_questions, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        btnOption1 = view.findViewById(R.id.btnOption1);
        btnOption2 = view.findViewById(R.id.btnOption2);
        btnOption3 = view.findViewById(R.id.btnOption3);
        btnOption4 = view.findViewById(R.id.btnOption4);
        textViewQuestion = view.findViewById(R.id.textViewQuestion);
        imageViewQuestion = view.findViewById(R.id.imageViewQuestion);
        progressBar = view.findViewById(R.id.progressBar);
        btnOption1.setOnClickListener(onOptionClickListener);
        btnOption2.setOnClickListener(onOptionClickListener);
        btnOption3.setOnClickListener(onOptionClickListener);
        btnOption4.setOnClickListener(onOptionClickListener);
        btnOption1.setEnabled(false);
        btnOption2.setEnabled(false);
        btnOption3.setEnabled(false);
        btnOption4.setEnabled(false);

        updateQuestion();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof QuestionsFragment.OnFragmentInteractionListener) {
            mListener = (QuestionsFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void updateQuestion() {
        if (total < totalQuestions + 1) {
            progressBar.setVisibility(View.VISIBLE);
            databaseReference = FirebaseDatabase.getInstance().getReference().child("INFS3604").child("Topics").child(value).child("Questions").child(String.valueOf(total));
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    question = dataSnapshot.getValue(Question.class);
                    final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(question.getQuestion() + ".png");
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            textViewQuestion.setVisibility(View.INVISIBLE);
                            imageViewQuestion.setVisibility(View.VISIBLE);
                            if (activity != null) {
                                Glide.with(activity).using(new FirebaseImageLoader())
                                        .load(storageReference)
                                        .into(imageViewQuestion);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            textViewQuestion.setVisibility(View.VISIBLE);
                            imageViewQuestion.setVisibility(View.INVISIBLE);
                        }
                    });
                    answer = question.getAnswer();
                    options = new ArrayList<>();
                    options.clear();
                    options.add(question.getOption1());
                    options.add(question.getOption2());
                    options.add(question.getOption3());
                    options.add(question.getAnswer());
                    Collections.shuffle(options);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            textViewQuestion.setText(question.getQuestion());
                            btnOption1.setText(options.get(0));
                            btnOption2.setText(options.get(1));
                            btnOption3.setText(options.get(2));
                            btnOption4.setText(options.get(3));
                            btnOption1.setEnabled(true);
                            btnOption2.setEnabled(true);
                            btnOption3.setEnabled(true);
                            btnOption4.setEnabled(true);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }, delay);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("INFS3604").child("Highscore").child(user.getUid()).child(value);
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                String highScore = dataSnapshot.getValue().toString();
                                //Log.d(TAG, "highScore: " + highScore + " correct: " + correct);
                                if (Integer.parseInt(highScore) < correct) {
                                    databaseReference.setValue(correct);
                                }
                            } else {
                                databaseReference.setValue(correct);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    //Log.d(TAG, "updateQuestion: correct: " + Integer.toString(correct) + " incorrect: " + Integer.toString(incorrect) + " total: " + Integer.toString(total - 1));
                    Snackbar snackbar = Snackbar
                            .make(getView(), "You answered " + Integer.toString(correct) + " out of " + Integer.toString(total - 1) + " correctly!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    TestTopicsFragment fragment = new TestTopicsFragment();
                    FragmentManager manager = getFragmentManager();
                    manager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("questions").commit();
                }
            }, delay + 500);
        }
    }

    private View.OnClickListener onOptionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            total++;
            switch (v.getId()) {
                case R.id.btnOption1:
                    if (btnOption1.getText().equals(answer)) {
                        btnOption1.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                correct++;
                            }
                        }, delay);
                    } else {
                        btnOption1.setBackground(getResources().getDrawable(R.drawable.button_incorrect, null));
                        if (btnOption2.getText().equals(answer)) {
                            btnOption2.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        } else if (btnOption3.getText().equals(answer)) {
                            btnOption3.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        } else {
                            btnOption4.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                incorrect++;
                            }
                        }, delay);
                    }
                    resetButtonColours();
                    break;
                case R.id.btnOption2:
                    if (btnOption2.getText().equals(answer)) {
                        btnOption2.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                correct++;
                            }
                        }, delay);
                    } else {
                        btnOption2.setBackground(getResources().getDrawable(R.drawable.button_incorrect, null));
                        if (btnOption1.getText().equals(answer)) {
                            btnOption1.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        } else if (btnOption3.getText().equals(answer)) {
                            btnOption3.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        } else {
                            btnOption4.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                incorrect++;
                            }
                        }, delay);
                    }
                    resetButtonColours();
                    break;
                case R.id.btnOption3:
                    if (btnOption3.getText().equals(answer)) {
                        btnOption3.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                correct++;
                            }
                        }, delay);
                    } else {
                        btnOption3.setBackground(getResources().getDrawable(R.drawable.button_incorrect, null));
                        if (btnOption1.getText().equals(answer)) {
                            btnOption1.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        } else if (btnOption2.getText().equals(answer)) {
                            btnOption2.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        } else {
                            btnOption4.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                incorrect++;
                            }
                        }, delay);
                    }
                    resetButtonColours();
                    break;
                case R.id.btnOption4:
                    if (btnOption4.getText().equals(answer)) {
                        btnOption4.setBackground(getResources().getDrawable(R.drawable.button_correct, null));
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                correct++;
                            }
                        }, delay);
                    } else {
                        btnOption4.setBackground(getResources().getDrawable(R.drawable.button_incorrect, null));
                        if (btnOption2.getText().equals(answer)) {
                            btnOption2.setBackgroundColor(Color.GREEN);
                        } else if (btnOption3.getText().equals(answer)) {
                            btnOption3.setBackgroundColor(Color.GREEN);
                        } else {
                            btnOption1.setBackgroundColor(Color.GREEN);
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                incorrect++;
                            }
                        }, delay);
                    }
                    resetButtonColours();
                    break;
            }
        }
    };

    public void resetButtonColours() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btnOption1.setBackground(getResources().getDrawable(R.drawable.button_login, null));
                btnOption2.setBackground(getResources().getDrawable(R.drawable.button_login, null));
                btnOption3.setBackground(getResources().getDrawable(R.drawable.button_login, null));
                btnOption4.setBackground(getResources().getDrawable(R.drawable.button_login, null));
            }
        }, delay);
        updateQuestion();
    }
}
