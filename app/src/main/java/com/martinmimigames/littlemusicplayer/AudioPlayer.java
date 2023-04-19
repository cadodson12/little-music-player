package com.martinmimigames.littlemusicplayer;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;

public class AudioPlayer extends Thread
  implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

  private final Service service;
  private final MediaPlayer mediaPlayer;

  public AudioPlayer(Service service, Uri audioLocation) {
    this.service = service;
    /* initiate new audio player */
    mediaPlayer = new MediaPlayer();

    /* setup player variables */
    try {
      mediaPlayer.setDataSource(service, audioLocation);
    } catch (IllegalArgumentException e) {
      Exceptions.throwError(service, Exceptions.IllegalArgument);
      service.stopSelf();
      return;
    } catch (SecurityException e) {
      Exceptions.throwError(service, Exceptions.Security);
      service.stopSelf();
      return;
    } catch (IllegalStateException e) {
      Exceptions.throwError(service, Exceptions.IllegalState);
      service.stopSelf();
      return;
    } catch (IOException e) {
      Exceptions.throwError(service, Exceptions.IO);
      service.stopSelf();
      return;
    }

    if (Build.VERSION.SDK_INT < 21) {
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    } else {
      mediaPlayer.setAudioAttributes(
        new AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .build()
      );
      System.out.println("Happy Listening!");
    }

    mediaPlayer.setLooping(false);

    /* setup listeners for further logics */
    mediaPlayer.setOnPreparedListener(this);
    mediaPlayer.setOnCompletionListener(this);
    mediaPlayer.setOnErrorListener(this);
  }

  @Override
  public void run() {
    try {
      /* get ready for playback */
      mediaPlayer.prepareAsync();
    } catch (IllegalStateException e) {
      Exceptions.throwError(service, Exceptions.IllegalState);
      service.stopSelf();
    }
  }

  /**
   * check if audio is playing
   */
  public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  /**
   * Switch to play state
   */
  public void play() {
    mediaPlayer.start();
  }

  /**
   * Switch to pause state
   */
  public void pause() {
    mediaPlayer.pause();
  }

  /**
   * playback when ready
   */
  @Override
  public void onPrepared(MediaPlayer mp) {
    service.play();
  }

  /**
   * release resource when playback finished
   */
  @Override
  public void onCompletion(MediaPlayer mp) {
    service.stopSelf();
  }

  /**
   * release and kill service
   */
  @Override
  public void interrupt() {
    mediaPlayer.release();
    super.interrupt();
  }

  @Override
  public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
    // -2147483648 = System error
    if (i == MediaPlayer.MEDIA_ERROR_UNKNOWN && i1 == -2147483648) {
      Exceptions.throwError(service, Exceptions.FormatNotSupported);
    }
    return false;
  }
}
