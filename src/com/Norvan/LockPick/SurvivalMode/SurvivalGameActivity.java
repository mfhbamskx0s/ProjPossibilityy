package com.Norvan.LockPick.SurvivalMode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.Norvan.LockPick.*;
import com.Norvan.LockPick.Helpers.AnalyticsHelper;
import com.Norvan.LockPick.Helpers.ResponseHelper;
import com.Norvan.LockPick.Helpers.VolumeToggleHelper;
import com.Norvan.LockPick.TimeTrialMode.TimeTrialGameHandler;

public class SurvivalGameActivity extends Activity

{
    LinearLayout linearChrono;
    VolumeToggleHelper volumeToggleHelper;
    VibrationHandler vibrationHandler;
    SurvivalGameHandler gameHandler;
    TextView textPicksLeft, textLevelLabel, textGameOver, textHighScore;
    ImageButton imgbutToggleVolume, imgbutTogglePause;
    Button butGameButton;
    Chronometer chronoTimer;
    AnnouncementHandler announcementHandler;
    SharedPreferencesHandler prefs;
    Context context;
    ResponseHelper responseHelper;
    long gamePausedChronoProgress;
    GraphView graphView;
    AnalyticsHelper analyticsHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survivalgamelayout);
        context = this;
        vibrationHandler = new VibrationHandler(this);
        gameHandler = new SurvivalGameHandler(this, gameStatusInterface, vibrationHandler);
        textPicksLeft = (TextView) findViewById(R.id.textPicksLeft);
        textLevelLabel = (TextView) findViewById(R.id.textLevelLabel);
        textGameOver = (TextView) findViewById(R.id.textGameOverLabel);
        textHighScore = (TextView) findViewById(R.id.textHighScore);
        linearChrono = (LinearLayout) findViewById(R.id.linearChrono);
        imgbutToggleVolume = (ImageButton) findViewById(R.id.imgbutGameVolume);
        imgbutToggleVolume.setOnClickListener(onClick);
        imgbutTogglePause = (ImageButton) findViewById(R.id.imgbutTogglePause);
        imgbutTogglePause.setOnClickListener(onClick);
        butGameButton = (Button) findViewById(R.id.butGameButton);
        butGameButton.setOnClickListener(onClick);
        chronoTimer = (Chronometer) findViewById(R.id.chronoTimer);
        volumeToggleHelper = new VolumeToggleHelper(this, imgbutToggleVolume);

        chronoTimer.setKeepScreenOn(true);
        prefs = new SharedPreferencesHandler(this);
        setHighScore(prefs.getSurvivalHighScore() + 1);
        setUiGameState(SurvivalGameHandler.STATE_FRESHLOAD);
        announcementHandler = new AnnouncementHandler(context, vibrationHandler);

        chronoTimer.setOnChronometerTickListener(onTick);
        responseHelper = new ResponseHelper(context);

        analyticsHelper = new AnalyticsHelper(this);
        analyticsHelper.startSurvivalActivity();

    }

    Chronometer.OnChronometerTickListener onTick = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            long timeElapsed = getCurrentTime() - chronometer.getBase();
            if (timeElapsed > 10000) {
                if (timeElapsed % 10000 < 1000 && gameHandler.getCurrentLevel() > 3) {
                    announcementHandler.userTakingTooLong();
                }
            }
        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!event.isLongPress() && event.getRepeatCount() == 0) {
                if (gameHandler.getGameState() == SurvivalGameHandler.STATE_INGAME) {
                    gameHandler.gotKeyDown();
                } else if (gameHandler.getGameState() != SurvivalGameHandler.STATE_PAUSED) {
                    gameHandler.playCurrentLevel();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            gameHandler.gotKeyUp();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (butGameButton.equals(view)) {

                gameHandler.playCurrentLevel();
            } else if (imgbutToggleVolume.equals(view)) {
                volumeToggleHelper.toggleMute();
            } else if (imgbutTogglePause.equals(view)) {
                boolean isPaused = gameHandler.togglePause();
                if (isPaused) {
                    pauseGame();
                } else {
                    resumeGame();
                }
                setTogglePauseImage(isPaused);
            }
        }
    };


    public SurvivalGameHandler.GameStatusInterface gameStatusInterface = new SurvivalGameHandler.GameStatusInterface() {
        @Override
        public void newGameStart() {
            setUiGameState(SurvivalGameHandler.STATE_FRESHLOAD);
            setHighScore(prefs.getSurvivalHighScore() + 1);
            analyticsHelper.newSurvivalGame();
        }

        @Override
        public void levelStart(int level, int picksLeft) {
            setUiGameState(SurvivalGameHandler.STATE_INGAME);
            chronoTimer.setBase(SystemClock.elapsedRealtime());
            chronoTimer.start();
            setPicksLeft(picksLeft);
            setLevelLabel(level);
            announcementHandler.levelStart(level, picksLeft);
//            boolean needsToAdd = false;
//            if (graphView == null) {
//                needsToAdd = true;
//            }
//            graphView = new GraphView(context, gameHandler.getLevelData(), GraphView.LINE);
//            if (needsToAdd) {
//                ((LinearLayout) findViewById(R.id.linearMain)).addView(graphView);
//            }
//            graphView.setVisibility(View.VISIBLE);

        }

        @Override
        public void levelWon(int levelWon, int picksLeft) {
            setUiGameState(SurvivalGameHandler.STATE_BETWEENLEVELS);
            butGameButton.setText("Next Level");
            chronoTimer.stop();
            float levelTime = SystemClock.elapsedRealtime() - chronoTimer.getBase();
            analyticsHelper.winSurvivalLevel(levelWon, (int) levelTime, picksLeft);
            setPicksLeft(picksLeft);
            announcementHandler.levelWon(levelTime, levelWon);

        }

        @Override
        public void levelLost(int level, int picksLeft) {
            setUiGameState(SurvivalGameHandler.STATE_BETWEENLEVELS);

            butGameButton.setText("Try Again");
            chronoTimer.stop();
            analyticsHelper.loseSurvivalLevel(level, (int) (SystemClock.elapsedRealtime() - chronoTimer.getBase()), picksLeft);
            setPicksLeft(picksLeft);
            announcementHandler.levelLost(level, picksLeft);
        }

        @Override
        public void gameOver(int maxLevel) {
            setUiGameState(SurvivalGameHandler.STATE_GAMEOVER);
            Log.i("AMP", "gameOver");
            butGameButton.setText("New Game");
            analyticsHelper.gameOverSurvival(maxLevel);
            if (prefs.getSurvivalHighScore() < maxLevel) {
                textGameOver.setText("GAME OVER\nScore: " + String.valueOf(maxLevel) + "\nNEW RECORD!");
                prefs.setSurvivalHighScore(maxLevel);
                setHighScore(maxLevel + 1);
            } else {
                textGameOver.setText("GAME OVER\nScore: " + String.valueOf(maxLevel + 1) + "\nRecord: " + String.valueOf(prefs.getSurvivalHighScore()));
            }


            chronoTimer.stop();


            announcementHandler.gameOver(maxLevel);

        }
    };


    void setPicksLeft(int picks) {
        textPicksLeft.setText("Picks Left: " + String.valueOf(picks));
    }

    void setLevelLabel(int level) {
        textLevelLabel.setText("Level " + String.valueOf(level + 1));
    }

    void setHighScore(int highScore) {
        textHighScore.setText("High Score: " + String.valueOf(highScore));
    }

    private void pauseGame() {
        gameHandler.pauseGame();
        gamePausedChronoProgress = getCurrentTime() - chronoTimer.getBase();
        chronoTimer.stop();
    }

    private void resumeGame() {
        chronoTimer.setBase(getCurrentTime() - gamePausedChronoProgress);
        chronoTimer.start();
    }

    @Override
    protected void onResume() {
        gameHandler.setSensorPollingState(true);
        if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_PAUSED) {
            setTogglePauseImage(true);
        }


        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onPause() {
        super.onPause();

        gameHandler.setSensorPollingState(false);
        if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_INGAME) {
            pauseGame();
        }
        vibrationHandler.stopVibrate();

    }

    @Override
    protected void onDestroy() {
        announcementHandler.shutDown();
        vibrationHandler.stopVibrate();
        announcementHandler = null;
        vibrationHandler = null;
        gameHandler = null;
        prefs = null;
        analyticsHelper.exitSurvival();
        analyticsHelper = null;
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
    }

    private void setUiGameState(int gameState) {
        switch (gameState) {
            case SurvivalGameHandler.STATE_FRESHLOAD: {
                butGameButton.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.GONE);
                linearChrono.setVisibility(View.GONE);
                textPicksLeft.setVisibility(View.GONE);
                textHighScore.setVisibility(View.GONE);
                textLevelLabel.setVisibility(View.VISIBLE);
                imgbutTogglePause.setVisibility(View.GONE);
                setLevelLabel(0);
            }
            break;
            case SurvivalGameHandler.STATE_INGAME: {
                textLevelLabel.setVisibility(View.VISIBLE);
                butGameButton.setVisibility(View.GONE);
                textGameOver.setVisibility(View.GONE);
                linearChrono.setVisibility(View.VISIBLE);
                textPicksLeft.setVisibility(View.VISIBLE);
                imgbutTogglePause.setVisibility(View.VISIBLE);
                textHighScore.setVisibility(View.VISIBLE);

            }
            break;
            case SurvivalGameHandler.STATE_BETWEENLEVELS: {
                textLevelLabel.setVisibility(View.VISIBLE);
                butGameButton.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.GONE);
                linearChrono.setVisibility(View.GONE);
                textPicksLeft.setVisibility(View.GONE);
                imgbutTogglePause.setVisibility(View.GONE);
                textHighScore.setVisibility(View.GONE);
            }
            break;
            case SurvivalGameHandler.STATE_GAMEOVER: {
                textLevelLabel.setVisibility(View.GONE);
                butGameButton.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.VISIBLE);
                linearChrono.setVisibility(View.GONE);
                textPicksLeft.setVisibility(View.GONE);
                imgbutTogglePause.setVisibility(View.GONE);
                textHighScore.setVisibility(View.GONE);
            }
            break;
        }
    }

    private long getCurrentTime() {
        return SystemClock.elapsedRealtime();
    }

    private void setTogglePauseImage(boolean isPaused) {
        imgbutTogglePause.setImageResource(isPaused ? R.drawable.ic_media_play : R.drawable.ic_media_pause);


    }
}
