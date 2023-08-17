package com.example.smartphone_log_analysis_log;

import static java.lang.Math.round;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.math3.complex.Complex;

import java.io.File;
import java.nio.file.NoSuchFileException;
import android.media.SoundPool;


public class ChirpGenerator extends AppCompatActivity {
    private AudioTrack audioTrack;
    private SoundPool soundPool;

    private int SAMPLE_RATE;
    private AudioService audioService;
    private int soundId;
    private volatile boolean isDividedChirpBeingGenerated = false;

    /** Class Constructor **/
    public ChirpGenerator(int SAMPLE_RATE, AudioService audioService) {
        this.SAMPLE_RATE = SAMPLE_RATE;
        this.audioService = audioService;
    }

    /** Interfaces **/
    public void startChirpSound(double minFrequency, double maxFrequency, double chirpDurationMs) {
        chirpRunnable cRunnable = new chirpRunnable(minFrequency, maxFrequency, chirpDurationMs);
        Thread ChirpThread = new Thread(cRunnable);
        ChirpThread.start();
    }

//    public void startDividedChirpSound() {
//        dividedChirpRunnable cRunnable = new dividedChirpRunnable(minFrequency, maxFrequency);
//        Thread ChirpThread = new Thread(cRunnable);
//        ChirpThread.start();
//        isDividedChirpBeingGenerated = true;
//    }

    public void startRecordedAudio(String file_name) {
        readAndRunAudio cRunnable = new readAndRunAudio(file_name);
        Thread ChirpThread = new Thread(cRunnable);
        ChirpThread.start();
    }

//    public void startModifiedDividedChirpSound(Complex[] inverseChannelFrequencyResponse) {
//        modifiedDividedChirpRunnable cRunnable = new modifiedDividedChirpRunnable(minFrequency, maxFrequency, inverseChannelFrequencyResponse);
//        Thread ChirpThread = new Thread(cRunnable);
//        ChirpThread.start();
//    }

    /** getter **/
    public boolean getIsDividedChirpBeingGenerated(){
        return isDividedChirpBeingGenerated;
    }

    /** Runnables **/
    private class chirpRunnable implements Runnable {
        private double minFrequency;
        private double maxFrequency;
        private double chirpDurationMs;
        private int chirpPeriodMs;

        public chirpRunnable(double minFrequency, double maxFrequency, double chirpDurationMs){
            this.minFrequency = minFrequency;
            this.maxFrequency = maxFrequency;
            this.chirpDurationMs = chirpDurationMs;
        }

        @Override
        public void run() {
            audioTrack = generateChirp(minFrequency, maxFrequency, chirpDurationMs);
//            audioTrack = generateTripleTone(17000, 20000, 23000, chirpDurationMs);

            if (audioTrack != null) {
//                int loopCount = 0; // Set loop count to infinite, change this value if you want a finite number of loops
//                int loopStartInFrames = 0; // Start of the loop in frames
//                int loopEndInFrames = audioTrack.getBufferSizeInFrames(); // End of the loop in frames
//
//                audioTrack.setLoopPoints(loopStartInFrames, loopEndInFrames, loopCount); // Set loop points
                audioTrack.play();

                while (audioService.getIsRecording()) {
                    try {
                        Thread.sleep(round(chirpDurationMs));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(audioTrack != null) {
                    audioTrack.stop();
                    audioTrack.release();
                }
            }
        }
    };

//    private class dividedChirpRunnable implements Runnable {
//        private double minFrequency;
//        private double maxFrequency;
//
//        public dividedChirpRunnable(double minFrequency, double maxFrequency){
//            this.minFrequency = minFrequency;
//            this.maxFrequency = maxFrequency;
//        }
//
//        @Override
//        public void run() {
//            audioTrack = generateDividedChirp(minFrequency, maxFrequency);
////            audioTrack = generateChirp(minFrequency, maxFrequency,  0.1, 1);
//
//            if (audioTrack != null) {
//                int loopCount = 7; // replay ten times
//                int loopStartInFrames = 0; // Start of the loop in frames
//                int loopEndInFrames = audioTrack.getBufferSizeInFrames(); // End of the loop in frames
//
//                audioTrack.setLoopPoints(loopStartInFrames, loopEndInFrames, loopCount); // Set loop points
//
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                audioTrack.play();
//
//                // Wait for the audio track to finish playing
//                while (audioTrack.getPlaybackHeadPosition() < loopEndInFrames*(loopCount+1)) {
//                    try {
//                        // Sleep for a short duration to avoid excessive CPU usage
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                isDividedChirpBeingGenerated = false;
//
//                if(audioTrack != null) {
//                    audioTrack.stop();
//                    audioTrack.release();
//                    audioTrack = null;
//                }
//            }
//        }
//    };

//    private class modifiedDividedChirpRunnable implements Runnable {
//        private double minFrequency;
//        private double maxFrequency;
//        private Complex[] inverseChannelFrequencyResponse;
//
//        public modifiedDividedChirpRunnable(double minFrequency, double maxFrequency, Complex[] channelImpulseResponse){
//            this.minFrequency = minFrequency;
//            this.maxFrequency = maxFrequency;
//            this.inverseChannelFrequencyResponse = channelImpulseResponse;
//        }
//
//        @Override
//        public void run() {
//            audioTrack = generateModifiedDividedChirp(minFrequency, maxFrequency, inverseChannelFrequencyResponse);
//
//            if (audioTrack != null) {
//                int loopCount = 7; // replay eight times
//                int loopStartInFrames = 0; // Start of the loop in frames
//                int loopEndInFrames = audioTrack.getBufferSizeInFrames(); // End of the loop in frames
//
//                audioTrack.setLoopPoints(loopStartInFrames, loopEndInFrames, loopCount); // Set loop points
//
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                audioTrack.play();
//
//                // Wait for the audio track to finish playing
//                while (audioTrack.getPlaybackHeadPosition() < loopEndInFrames*(loopCount+1)) {
//                    try {
//                        // Sleep for a short duration to avoid excessive CPU usage
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                isDividedChirpBeingGenerated = false;
//
//                if(audioTrack != null) {
//                    audioTrack.stop();
//                    audioTrack.release();
//                    audioTrack = null;
//                }
//            }
//        }
//    };

    private class readAndRunAudio implements Runnable {
        String file_name;
        public readAndRunAudio(String file_name){
            this.file_name = file_name;
        }

        @Override
        public void run() {
            File wavFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), file_name);
            final boolean[] isSoundLoaded = {false};

            if(wavFile.exists()) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                soundPool = new SoundPool.Builder()
                        .setMaxStreams(1)
                        .setAudioAttributes(audioAttributes)
                        .build();
                soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int soundId, int status) {
                        if (status == 0) {
                            // Sound loaded successfully
                            isSoundLoaded[0] = true;
                        } else {
                            // Error occurred while loading the sound
                            Log.e("SoundPool", "Error loading sound");
                        }
                    }
                });

                // Load the sound file
                soundId = soundPool.load(wavFile.getPath(), 1);
                if (isSoundLoaded[0]) {
                    // Sound is loaded, you can play it
                    soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
                } else {
                    // Sound is not loaded yet, wait or handle accordingly
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int sound_valid = soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
                    if(sound_valid == 0) {
                        throw new RuntimeException("Sound play not successful!");
                    }
                }
            }
            else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        throw new NoSuchFileException("Sound file does not exist!");
                    } catch (NoSuchFileException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            while (audioService.getIsRecording()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(soundPool != null){
                soundPool.stop(soundId);
                soundPool.release();
                soundPool = null;
            }
        }
    };

    /** Signal Generator **/
    private AudioTrack generateChirp(double minFrequency, double maxFrequency, double chirpDurationMs) {
        double duration = chirpDurationMs / 1000.0; // Duration of the chirp in seconds
//        double delay = chirpPeriodMs / 1000.0;
        int numSamples = (int) (duration * SAMPLE_RATE);
//        int numDelay = (int) (delay * SAMPLE_RATE);
        double[] sample = new double[numSamples];
        byte[] generatedSnd = new byte[2 * numSamples];

        double[] window = new double[numSamples];
        for (int i = 0; i < numSamples; i++){
            window[i] = Math.pow(Math.sin(Math.PI * i / numSamples), 2) ;
        }

        for (int i = 0; i < numSamples; i++) {
            double instantFrequency = minFrequency + ((maxFrequency - minFrequency) / 2 * i / (numSamples - 1));
            sample[i] = Math.cos(2 * Math.PI * instantFrequency * i / SAMPLE_RATE);
        }

        int idx = 0;
        for (final double dVal : sample) {
            final short val = (short) ((dVal * 32767));
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >> 8);
        }

        if (audioTrack != null) {
            audioTrack.release();
        }

        audioTrack = new AudioTrack.Builder()
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(generatedSnd.length)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();

        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        return audioTrack;
    }

    public double[] getChirpSignal(double minFrequency, double maxFrequency, double chirpDurationMs){
        double duration = chirpDurationMs / 1000.0; // Duration of the chirp in seconds
        int numSamples = (int) (duration * SAMPLE_RATE);
        double[] sample = new double[numSamples];

        double[] window = new double[numSamples];
        for (int i = 0; i < numSamples; i++){
            window[i] = Math.pow(Math.sin(Math.PI * i / numSamples), 2) ;
        }

        for (int i = 0; i < numSamples; i++) {
            double instantFrequency = minFrequency + ((maxFrequency - minFrequency) / 2 * i / (numSamples - 1));
            sample[i] = Math.cos(2 * Math.PI * instantFrequency * i / SAMPLE_RATE) * window[i];
        }

        return sample;
    }

    // please revise the code as it can match for all bandwidths
//    private AudioTrack generateDividedChirp(double minFrequency, double maxFrequency) {
//        int numSamples = 20;
//        int numDelay = 70;
//        int subCarrierBW = 50;
//        int numChirp = (int)(maxFrequency - minFrequency) / subCarrierBW;
//        int totalDuration = (numSamples+numDelay)*numChirp;
//        double[] sample = new double[totalDuration];
//        byte[] generatedSnd = new byte[totalDuration*2];
////        int currentFrequency = minFrequency;
//
//        double[] window = new double[numSamples];
//        for (int i = 0; i < numSamples; i++){
//            window[i] = Math.pow(Math.sin(Math.PI * i / numSamples), 2) ;
//        }
//
//        for (int i = 0; i < numChirp; i++){
//            for (int j = 0; j < (numSamples + numDelay); j++) {
//                double cutoffFrequency1 = minFrequency + subCarrierBW * i;
//                double cutoffFrequency2 = minFrequency + subCarrierBW * (i + 1);
//                double baseFrequency = (cutoffFrequency1 + cutoffFrequency2 - numSamples) / 2;
//                if (j < numSamples) {
//                    sample[j + (numSamples + numDelay) * i] = Math.cos(2 * Math.PI * (baseFrequency + j) * j / SAMPLE_RATE) * window[j];
//                }
//                else sample[j] = 0;
//            }
//        }
//
//
//        int idx = 0;
//        for (final double dVal : sample) {
//            final short val = (short) ((dVal * 32767));
//            generatedSnd[idx++] = (byte) (val & 0x00ff);
//            generatedSnd[idx++] = (byte) ((val & 0xff00) >> 8);
//        }
//
//        if (audioTrack != null) {
//            audioTrack.release();
//        }
//
//        audioTrack = new AudioTrack.Builder()
//                .setAudioFormat(new AudioFormat.Builder()
//                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//                        .setSampleRate(SAMPLE_RATE)
//                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
//                .setBufferSizeInBytes(generatedSnd.length)
//                .setTransferMode(AudioTrack.MODE_STATIC)
//                .build();
//
//        audioTrack.write(generatedSnd, 0, generatedSnd.length);
//        return audioTrack;
//    }



//    @Nullable
//    private AudioTrack generateModifiedDividedChirp(double minFrequency, double maxFrequency, @NonNull Complex[] inverseChannelFrequencyResponse) {
//        int numSamples = 20;
//        int numDelay = 70;
//        int subCarrierBW = 50;
//        int numChirp = (int)((maxFrequency - minFrequency) / subCarrierBW);
//        int totalDuration = (numSamples+numDelay)*numChirp;
//        double[] sample = new double[totalDuration];
//        byte[] generatedSnd = new byte[totalDuration*2];
////        int currentFrequency = minFrequency;
//
//        if (totalDuration != inverseChannelFrequencyResponse.length){
//            Log.e("error", "length between generated chirp and frequency response is different!");
//            return null;
//        }
//
//        double[] window = new double[numSamples];
//        for (int i = 0; i < numSamples; i++){
//            window[i] = Math.pow(Math.sin(Math.PI * i / numSamples), 2) ;
//        }
//
//        for (int i = 0; i < numChirp; i++){
//            for (int j = 0; j < (numSamples + numDelay); j++) {
//                double cutoffFrequency1 = minFrequency + subCarrierBW * i;
//                double cutoffFrequency2 = minFrequency + subCarrierBW * (i + 1);
//                double baseFrequency = (cutoffFrequency1 + cutoffFrequency2 - numSamples) / 2;
//                if (j < numSamples) {
//                    sample[j + (numSamples + numDelay) * i] = Math.cos(2 * Math.PI * (baseFrequency + j) * j / SAMPLE_RATE) * window[j];
//                }
//                else sample[j] = 0;
//            }
//        }
//
//        Complex[] fftDividedChirp = dft.computeDFT(sample);
//
//        Complex[] fftModifiedDividedChirp = new Complex[totalDuration];
//        for (int i = 0; i < totalDuration; i++){
//            fftModifiedDividedChirp[i] = fftDividedChirp[i].multiply(inverseChannelFrequencyResponse[i]);
//        }
//
//        Complex[] modifiedDividedChirp = dft.computeIDFT(fftModifiedDividedChirp);
//        double[] realModifiedDividedChirp = new double[totalDuration];
//        for (int i = 0; i < totalDuration; i++) {
//            realModifiedDividedChirp[i] = modifiedDividedChirp[i].getReal();
//        }
//
//        int idx = 0;
//        for (final double dVal : realModifiedDividedChirp) {
//            final short val = (short) ((dVal * 32767));
//            generatedSnd[idx++] = (byte) (val & 0x00ff);
//            generatedSnd[idx++] = (byte) ((val & 0xff00) >> 8);
//        }
//
//        if (audioTrack != null) {
//            audioTrack.release();
//        }
//
//        audioTrack = new AudioTrack.Builder()
//                .setAudioFormat(new AudioFormat.Builder()
//                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//                        .setSampleRate(SAMPLE_RATE)
//                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
//                .setBufferSizeInBytes(generatedSnd.length)
//                .setTransferMode(AudioTrack.MODE_STATIC)
//                .build();
//
//        audioTrack.write(generatedSnd, 0, generatedSnd.length);
//        return audioTrack;
//    }
}
