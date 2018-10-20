package com.example.vinguyen.assignmentprototype;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vinguyen.assignmentprototype.Speech.MessageDialogFragment;
import com.example.vinguyen.assignmentprototype.Speech.SpeechService;
import com.example.vinguyen.assignmentprototype.Speech.VoiceRecorder;

import static android.content.Context.BIND_AUTO_CREATE;

public class SpeechDialogFragment extends DialogFragment implements MessageDialogFragment.Listener {
    // Defines the listener interface
    private static final String TAG = "SpeechDialogFragment";
    private TextView mTextView;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";
    private SpeechService mSpeechService;
    private VoiceRecorder mVoiceRecorder;
    private String searchPhrase = "";

    public SpeechDialogFragment() {
    }


    public static SpeechDialogFragment newInstance(String title) {
        SpeechDialogFragment frag = new SpeechDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.speech_dialog_fragment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        mTextView = view.findViewById(R.id.textViewSpeech);
        // Fetch arguments from bundle and set title
        getDialog().setTitle("Listening...");
        // Show soft keyboard automatically and request focus to field
//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecorder();
            Log.d(TAG, "onViewCreated: voice recorder started");
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }


    public interface SpeechDialogListener {
        void onFinishSpeechDialog(String inputText);
    }

    // Call this method to send the data back to the parent fragment
    public void sendBackResult() {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        SpeechDialogListener listener = (SpeechDialogListener) getTargetFragment();
        Log.d(TAG, "sendBackResult: " + searchPhrase);
        listener.onFinishSpeechDialog(searchPhrase);
        dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance("Need permissions")
                .show(getActivity().getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }
                    if (mTextView != null && !TextUtils.isEmpty(text)) {
                        Log.d(TAG, "onSpeechRecognized: " + text);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    mTextView.setText(text.toLowerCase());
                                    if (text.toLowerCase().startsWith("find")) {
                                        searchPhrase = text.toLowerCase().substring(text.indexOf(' ') + 1);
                                        sendBackResult();
                                    }
                                } else {
                                    mTextView.setText(text);
                                }
                            }
                        });
                    }
                }
            };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };

    @Override
    public void onStart() {
        super.onStart();
        // Prepare Cloud Speech API
        getActivity().bindService(new Intent(getActivity(), SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    public void onStop() {
        if (mSpeechService != null) {
            mSpeechService.removeListener(mSpeechServiceListener);
        }
        getActivity().unbindService(mServiceConnection);
        mSpeechService = null;
        stopVoiceRecorder();
        super.onStop();
    }

}
