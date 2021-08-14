package com.Norvan.LockPick.TutorialMode;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.Norvan.LockPick.AnnouncementHandler;
import com.Norvan.LockPick.Helpers.UserType;
import com.Norvan.LockPick.Helpers.VolumeToggleHelper;
import com.Norvan.LockPick.R;
import com.Norvan.LockPick.SharedPreferencesHandler;
import com.Norvan.LockPick.VibrationHandler;


public class TutorialActivity extends Activity {
    private TutorialHandler tutorialHandler;
    private Context context;
    private VibrationHandler vibrationHandler;
    private AnnouncementHandler announcementHandler;
    private int userType;
    private Button butStartExit;
    private TextView textStepInstructions;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        userType = SharedPreferencesHandler.getUserType(context);
        if (userType == UserType.USER_NORMAL) {
            setUpNormalUI();
        } else {
            setUpAccessibleUI();
        }
        vibrationHandler = new VibrationHandler(context);
        vibrationHandler.setVibrationCompletedInterface(vibrationCompletedInterface);
        announcementHandler = new AnnouncementHandler(context, vibrationHandler);
        tutorialHandler = new TutorialHandler(context, tutorialStatusInterface, vibrationHandler);
        if (userType != UserType.USER_DEAFBLIND) {
            announcementHandler.tutorialLaunch();
        } else {
            startTutorial();
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

    }

    private void setUpNormalUI() {
        setContentView(R.layout.tutoriallayout);
        textStepInstructions = (TextView) findViewById(R.id.textTutorialInstructions);
        textStepInstructions.setKeepScreenOn(true);
        butStartExit = (Button) findViewById(R.id.butGameButton);
        butStartExit.setOnClickListener(onClickNormal);
        ImageButton imgbutToggleVolume = (ImageButton) findViewById(R.id.imgbutGameVolume);
        VolumeToggleHelper volumeToggleHelper = new VolumeToggleHelper(this, imgbutToggleVolume);


    }

    private void setUpAccessibleUI() {
        setContentView(R.layout.accessibletutuoriallayout);
        textStepInstructions = (TextView) findViewById(R.id.textTutorialInstructions);
        textStepInstructions.setKeepScreenOn(true);
        textStepInstructions.setOnClickListener(onClickAccessible);
        textStepInstructions.setOnLongClickListener(onLongClickAccessible);
        if (userType == UserType.USER_BLIND) {
            textStepInstructions.setText("Press either volume button to begin");
            textStepInstructions.setContentDescription("");
        }
    }

    private void startTutorial() {
        announcementHandler.tutorialTurnPhoneOnSide();
        goToStep(TutorialHandler.STEP_preTURNPHONEONSIDE);

    }

    private View.OnClickListener onClickAccessible = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (tutorialHandler.getCurrentStep() == TutorialHandler.STEP_START) {
                announcementHandler.tutorialHoldToBegin();

            } else if (userType != UserType.USER_DEAFBLIND) {

                switch (tutorialHandler.getCurrentStep()) {

                    case TutorialHandler.STEP_preTURNPHONEONSIDE:
                        announcementHandler.tutorialTurnPhoneOnSide();
                        break;

                    case TutorialHandler.STEP_preFINDSWEETSPOT:
                        announcementHandler.tutorialFindSweetSpot(true);
                        break;

                    case TutorialHandler.STEP_PREFORMUNLOCK:
                        announcementHandler.tutorialPreformUnlock(true);
                        break;

                    case TutorialHandler.STEP_FINISHED:
                        announcementHandler.tutorialWin();
                        break;
                }
            }
        }
    };
    private View.OnLongClickListener onLongClickAccessible = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (textStepInstructions.equals(view)) {
                if (tutorialHandler.getCurrentStep() == TutorialHandler.STEP_START) {
                    startTutorial();
                    return true;
                } else if (tutorialHandler.getCurrentStep() == TutorialHandler.STEP_FINISHED) {
                    finish();
                    return true;
                }
            }
            return false;
        }
    };
    private View.OnClickListener onClickNormal = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (tutorialHandler.getCurrentStep() == TutorialHandler.STEP_START) {
                startTutorial();
                v.setVisibility(View.GONE);
            } else {
                finish();
            }

        }
    };


    private void setTextStepInstruction(String text) {
        textStepInstructions.setText(text);
    }

    private void goToStep(int step) {
        switch (step) {
            case TutorialHandler.STEP_START:

                break;
            case TutorialHandler.STEP_preTURNPHONEONSIDE:
                setTextStepInstruction(getResources().getString(R.string.tutorialHoldPhoneOnSide));
                break;
            case TutorialHandler.STEP_postTURNPHONEONSIDE:
                setTextStepInstruction(getResources().getString(R.string.tutorialFindSweetSpot));
                break;
            case TutorialHandler.STEP_preFINDSWEETSPOT:
                break;
            case TutorialHandler.STEP_postFINDSWEETSPOT:
                setTextStepInstruction(getResources().getString(R.string.tutorialBeginUnlock));
                break;
            case TutorialHandler.STEP_PREFORMUNLOCK:
                break;
            case TutorialHandler.STEP_postPREFORMUNLOCKwin:
                SharedPreferencesHandler.didTutorial(this);
                if (butStartExit != null) {
                    butStartExit.setText("EXIT");
                    butStartExit.setVisibility(View.VISIBLE);
                }
                break;
            case TutorialHandler.STEP_postPREFORMUNLOCKlose:
                setTextStepInstruction(context.getResources().getString(R.string.tutorialUnlockFailed));
                break;
            case TutorialHandler.STEP_FINISHED:
                setTextStepInstruction(context.getResources().getString(R.string.tutorialUnlockSuccess));
                break;
        }
        tutorialHandler.goToStep(step);

    }


    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        if (tutorialHandler.isPolling()) {
            tutorialHandler.setSensorPollingState(false);
        }
        vibrationHandler.stopVibrate();

    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        if (tutorialHandler.getCurrentStep() > TutorialHandler.STEP_START) {
            tutorialHandler.setSensorPollingState(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        if (tutorialHandler.isPolling()) {
            tutorialHandler.setSensorPollingState(false);
        }
        vibrationHandler.stopVibrate();
        tutorialHandler = null;
        vibrationHandler = null;
        announcementHandler = null;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            if (tutorialHandler.getCurrentStep() == TutorialHandler.STEP_START) {
                if (userType == UserType.USER_BLIND) {
                    if (!event.isLongPress() && event.getRepeatCount() == 0) {
                        startTutorial();
                    }
                    return true;
                }
            }else if (tutorialHandler.getCurrentStep() == TutorialHandler.STEP_FINISHED) {
                if (userType == UserType.USER_BLIND) {
                    if (!event.isLongPress() && event.getRepeatCount() == 0) {
                        finish();
                    }

                } return true;
            } else {
                if (!event.isLongPress() && event.getRepeatCount() == 0) {
                    tutorialHandler.gotKeyDown();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            if (tutorialHandler.getCurrentStep() != TutorialHandler.STEP_START && tutorialHandler.getCurrentStep() != TutorialHandler.STEP_FINISHED) {
                tutorialHandler.gotKeyUp();
            } 
            return true;
        }
        return super.onKeyUp(keyCode, event);    //To change body of overridden methods use File | Settings | File Templates.
    }

    private TutorialHandler.TutorialStatusInterface tutorialStatusInterface = new TutorialHandler.TutorialStatusInterface() {
        @Override
        public void isOnSide() {
            goToStep(TutorialHandler.STEP_postTURNPHONEONSIDE);
            announcementHandler.tutorialFindSweetSpot(false);
            if (userType != UserType.USER_DEAFBLIND) {
                goToStep(TutorialHandler.STEP_preFINDSWEETSPOT);
            }
        }

        @Override
        public void sweetSpotFound() {
            goToStep(TutorialHandler.STEP_postFINDSWEETSPOT);
            announcementHandler.tutorialPreformUnlock(false);
            if (userType != UserType.USER_DEAFBLIND) {
                goToStep(TutorialHandler.STEP_PREFORMUNLOCK);
            }
        }

        @Override
        public void preformedUnlock(boolean success) {
            if (success) {
                goToStep(TutorialHandler.STEP_postPREFORMUNLOCKwin);
                vibrationHandler.playHappyNotified();
                if (userType != UserType.USER_DEAFBLIND) {
                    goToStep(TutorialHandler.STEP_FINISHED);
                    announcementHandler.tutorialWin();
                }
            } else {
                goToStep(TutorialHandler.STEP_postPREFORMUNLOCKlose);

                vibrationHandler.playSadNotified();
                if (userType != UserType.USER_DEAFBLIND) {
                    announcementHandler.tutorialLose();
                }
            }
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };
    private VibrationHandler.VibrationCompletedInterface vibrationCompletedInterface = new VibrationHandler.VibrationCompletedInterface() {
        @Override
        public void vibrationCompleted() {
            switch (tutorialHandler.getCurrentStep()) {
                case TutorialHandler.STEP_START:
                    if (userType == UserType.USER_DEAFBLIND) {
                        goToStep(TutorialHandler.STEP_preTURNPHONEONSIDE);
                    }
                    break;
                case TutorialHandler.STEP_preTURNPHONEONSIDE:
                    break;
                case TutorialHandler.STEP_postTURNPHONEONSIDE:
                    if (userType == UserType.USER_DEAFBLIND) {
                        goToStep(TutorialHandler.STEP_preFINDSWEETSPOT);
                    }
                    break;
                case TutorialHandler.STEP_preFINDSWEETSPOT:
                    break;
                case TutorialHandler.STEP_postFINDSWEETSPOT:
                    if (userType == UserType.USER_DEAFBLIND) {
                        goToStep(TutorialHandler.STEP_PREFORMUNLOCK);
                    }
                    break;
                case TutorialHandler.STEP_PREFORMUNLOCK:
                    break;
                case TutorialHandler.STEP_FINISHED:
                    break;
                case TutorialHandler.STEP_postPREFORMUNLOCKwin:
                    if (userType == UserType.USER_DEAFBLIND) {
                        goToStep(TutorialHandler.STEP_FINISHED);
                        announcementHandler.tutorialWin();
                    }
                    break;
                case TutorialHandler.STEP_postPREFORMUNLOCKlose:
                    if (userType == UserType.USER_DEAFBLIND) {
                        announcementHandler.tutorialLose();
                        goToStep(TutorialHandler.STEP_PREFORMUNLOCK);
                    } else {
                        goToStep(TutorialHandler.STEP_PREFORMUNLOCK);
                    }
                    break;
            }
        }
    };
}