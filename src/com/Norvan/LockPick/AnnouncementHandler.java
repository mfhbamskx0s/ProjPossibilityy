package com.Norvan.LockPick;

import android.content.Context;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/5/12
 * Time: 2:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnnouncementHandler {
    int userType;
    VibrationHandler vibrationHandler;
    TTSHandler tts;
    Context context;

    public AnnouncementHandler(Context context, VibrationHandler vibrationHandler) {
        userType = SharedPreferencesHandler.getUserType(context);
        this.vibrationHandler = vibrationHandler;
        this.context = context;
        tts = new TTSHandler(context);
    }

    public void shutDown() {
        tts.shutDownTTS();
    }

    public void cancelAnnouncement() {
        tts.shutUp();
        vibrationHandler.stopVibrate();

    }

    public void mainActivityLaunch() {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase(context.getResources().getString(R.string.mainactivityBlind));
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.stopVibrate();
            vibrationHandler.playString(context.getResources().getString(R.string.mainactivityDeafBlind));
        }
    }

    public void playDeafBlindInstructions() {
        vibrationHandler.playString(context.getResources().getString(R.string.instructionsDeafBlind));
    }

    public void newLaunch() {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase("Turn the phone on its side with the screen facing left. Press the volume button to begin.");
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.stopVibrate();
            vibrationHandler.playString("Turn phone on side screen facing left. Press volume to begin.");
        }

    }

    public void levelStart(int level, int picksLeft) {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase("Level " + String.valueOf(level + 1) + ", " + String.valueOf(picksLeft) + " picks left.");
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
        }
    }

    public void levelWon(float time, int wonLevel) {
        vibrationHandler.playHappy();
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase("You beat level " + String.valueOf(wonLevel + 1) + " in " + getTimeString(time) + ". Press volume to continue.");
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.playString("You won level " + String.valueOf(wonLevel) + " in " + getTimeString(time));
        } else {
            tts.speakPhrase("Good Job!");
        }
    }

    public void levelLost(int level, int picksLeft) {
        vibrationHandler.playSad();

        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase("You lost. Press volume to try again");
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.playString("You lost " + String.valueOf(picksLeft) + " picks left");
        } else {
            tts.speakPhrase("You failed");
        }
    }

    public void gameOver(int maxLevel) {
        vibrationHandler.playSad();

        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            if (new SharedPreferencesHandler(context).getHighScore() < maxLevel) {
                tts.speakPhrase("Game Over. Your reached level " + String.valueOf((maxLevel + 1)) + ". A new high score! Press volume for a new game.");
            } else {
                tts.speakPhrase("Game Over. Your reached level " + String.valueOf((maxLevel + 1)) + ". Press volume for a new game.");
            }

        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.playString("Game over. Reached level " + String.valueOf(maxLevel + 1));
        } else {
            tts.speakPhrase("Game Over");
        }

    }

    private String getTimeString(float time) {
        int seconds = (int) time / 1000;
        return String.valueOf(seconds) + " seconds";

    }
}
