package com.fivecentscdnanalytics.stateMachines;


import com.fivecentscdnanalytics.data.ErrorCode;

public interface StateMachineListener {

    void onStartup(long videoStartupTime, long playerStartupTime);

    void onPauseExit(long duration);

    void onPlayExit(long duration);

    void onHeartbeat(long duration);

    void onRebuffering(long duration);

    void onError(ErrorCode errorCode);

    void onSeekComplete(long duration);

    void onAd();

    void onMute();

    void onUnmute();

    void onUpdateSample();

    void onQualityChange();

    void onVideoChange();

    void onSubtitleChange();

    void onAudioTrackChange();

    void onVideoStartFailed();
}
