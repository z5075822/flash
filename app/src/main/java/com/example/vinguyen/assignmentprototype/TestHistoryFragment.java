package com.example.vinguyen.assignmentprototype;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.vinguyen.assignmentprototype.Model.Topic;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class TestHistoryFragment extends Fragment {
    private static final String TAG = "TestHistoryFragment";
    private OnFragmentInteractionListener mListener;
    private ArrayList<Topic> mTopic = new ArrayList<>();
    private ArrayList<String> mScore = new ArrayList<>();
    private boolean topicFinished, scoreFinished;
    private Button btnSend;

    public TestHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test_history, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View view = getView();
        initTopicArrayList();
        btnSend = view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scoreFinished && topicFinished) {
                    //Starts Async task
                    new SendEmail().execute();
                    Toast.makeText(getActivity(), "Email has been sent", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Please wait 10 seconds while system initiates", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TestHistoryFragment.OnFragmentInteractionListener) {
            mListener = (TestHistoryFragment.OnFragmentInteractionListener) context;
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

    //Sends email containing scores and explanations of perfect quizzes
    class SendEmail extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            //Sets from email
            final String username = "infs3634flashproject@gmail.com";
            final String password = "HighDistinction100!";
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final String email = user.getEmail();

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
            try {
                //Sets message of email
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("from-email@gmail.com"));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(email));
                message.setSubject("Flash: INFS3604 Test History");
                StringBuilder messageBody = new StringBuilder(1000);

                //Goes through all topics and adds link to message if perfect score
                for (int i = 0; i < mTopic.size(); i++) {
                    messageBody.append("You got " + mScore.get(i) + " for " + mTopic.get(i).getTitle() + "\n");
                    if (Integer.parseInt(mScore.get(i)) == 4) {
                        messageBody.append("\tReview the questions by going to this link: " + mTopic.get(i).getQuestionDoc() + "\n");
                    }
                }
                message.setText(messageBody.toString());
                Transport.send(message);
                Log.d(TAG, "doInBackground: messsage sent to " + email + " with content: " + message);
            } catch (MessagingException e) {
                Log.e(TAG, "doInBackground: ", e);
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    //Initialises both arraylists
    public void initTopicArrayList() {
        //Creates a reference to database
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference topicsRef = rootRef.child("INFS3604").child("Topics");
        ValueEventListener topicsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //Adds topic from Firebase node to arraylist
                    Topic topic = ds.getValue(Topic.class);
                    mTopic.add(topic);
                    Log.d(TAG, "onDataChange: topic added");
                }
                topicFinished = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        //Creates a different reference to database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference scoreRef = rootRef.child("INFS3604").child("Highscore").child(user.getUid());
        ValueEventListener scoreEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //Adds score to arraylist
                    String topic = ds.getKey();
                    mScore.add(dataSnapshot.child(topic).getValue().toString());
                }
                scoreFinished = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        topicsRef.addListenerForSingleValueEvent(topicsEventListener);
        scoreRef.addListenerForSingleValueEvent(scoreEventListener);
    }
}
