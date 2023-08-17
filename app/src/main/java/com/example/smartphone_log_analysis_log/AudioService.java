package com.example.smartphone_log_analysis_log;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import static java.lang.Math.round;
//
//import java.util.Arrays;

//import org.apache.commons.math3.complex.Complex;
//import org.jtransforms.fft.DoubleFFT_1D;

public class AudioService extends Service {
    private int SAMPLE_RATE;
    private AudioRecord audioRecord;
    private final IBinder binder = new AudioServiceBinder();
    private ChirpGenerator chirpGenerator;
    private volatile boolean isRecording = false;
    private PowerManager.WakeLock wakeLock;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "audio_channel";

    public class AudioServiceBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }

    }

    @Override
    public void onCreate() {
        Log.d("AudioService", "Service Created");  // For debugging

        // Explicitly assign the volume of the output chirp
        AudioManager audioManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        SAMPLE_RATE = Integer.parseInt(audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
        int outputVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, outputVolume, 0);

        chirpGenerator = new ChirpGenerator(SAMPLE_RATE, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);
        Log.d("AudioService", "Service Started");  // For debugging

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("com.example.smartphone_log_analysis_log.START_CHIRP_RECORDING")) {
                    monitorTemperature(this);
                    double minFrequency = 10000;
                    double maxFrequency = 23000;
                    double chirpDurationMs = 3000;
                    // Code to generate sound here
                    Log.d("simpleChirpIntentReceiver", "Broadcast received");  // For debugging
                    if (!isRecording) {
                        startRecording("chirp.wav");
//                            startRecording();
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        chirpGenerator.startChirpSound(minFrequency, maxFrequency, chirpDurationMs);
                        isRecording = true;
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stopRecording();
                        isRecording = false;
                    } else {
                        isRecording = false;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stopRecording();
                    }
                } else if (action.equals("com.example.smartphone_log_analysis_log.START_MODIFIED_CHIRP")) {
                    // Code to generate sound here
                    Log.d("ModifiedChirpIntentReceiver", "Broadcast received");  // For debugging
                    for (int i = 0; i < 10; i++){
                        monitorTemperature(this);
                        if (!isRecording) {
                            startRecording();
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            chirpGenerator.startRecordedAudio("modified_chirp.wav");
                            isRecording = true;
                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            stopRecording();
                            isRecording = false;
                        } else {
                            isRecording = false;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            stopRecording();
                        }
                    }
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Main code
//    private Complex[] getInverseChannelResponse(){
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
//            return null;
//        }
//
//        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT); // designate buffer size for audio recording
//        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
//            bufferSize = SAMPLE_RATE * 2;   // resize when it is too small
//        }
//        Log.d("chirp", "bufferSize : " + bufferSize);
//
//        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
//                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
//
//        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
//            Log.e("AudioRecord", "AudioRecord initialization failed");
//            return null;
//        }
//
//        audioRecord.startRecording();
//        isRecording = true;
//        // Start a new thread to read the audio data from AudioRecord and save it to a file
//        // The thread runs recordAudioRunnable to log IMU data
//        getAudioDataRunnable getaudiodatarunnable = new getAudioDataRunnable(bufferSize);
//        Thread getAudioDataThread = new Thread(getaudiodatarunnable);
//        getAudioDataThread.start();
//
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        chirpGenerator.startChirpSound();
//        try {
//            Thread.sleep(round(chirpDurationMs)+10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        stopRecording();
//
//        List<Double> receivedChirp = getaudiodatarunnable.getRecordedChirp();
//        double[] kernel = bpf.bandPassKernel(1000, minFrequency / (SAMPLE_RATE/2), maxFrequency / (SAMPLE_RATE/2));
//        double[] bpfChirp = bpf.filter(receivedChirp, kernel);
//
//        Log.d("chirp", "Length of bpfChirp: " + bpfChirp.length);
//
//        int signalLength = (int)(chirpDurationMs * SAMPLE_RATE / 1000);
//        double[] outputSignal = extract(bpfChirp, signalLength);
//        double[] inputSignal = chirpGenerator.getChirpSignal(minFrequency, maxFrequency, chirpDurationMs);
//
//        Complex[] outputComplexSignal = new Complex[signalLength];
//        Complex[] inputComplexSignal = new Complex[signalLength];
//        for (int i = 0; i < signalLength; i++){
//            outputComplexSignal[i] = new Complex(outputSignal[i], 0.0);
//            inputComplexSignal[i] = new Complex(inputSignal[i], 0.0);
//        }
//
//
//        Complex[] fftOutput = new Complex[signalLength];
//        Complex[] fftInput = new Complex[signalLength];
//        FFTUtils.computeFFT(outputComplexSignal, fftOutput);
//        FFTUtils.computeFFT(inputComplexSignal, fftInput);
//
//        Complex[] inverseChannelFrequencyResponse = new Complex[signalLength];
//        for (int i = 0; i < signalLength; i++){
//            if (fftOutput[i].abs() > 0) {
//                inverseChannelFrequencyResponse[i] = fftInput[i].divide(fftOutput[i]);
//            } else {
//                inverseChannelFrequencyResponse[i] = Complex.ZERO; // Avoid division by zero
//            }
//        }
//
//        return inverseChannelFrequencyResponse;
//    }
//
//    private double[] extract(double[] signal, int signalLength) {
//        double[] window = new double[signalLength];
//        Arrays.fill(window, 1.0); // Initialize the window with ones
//
//        // Calculate cross-correlation using FFT-based convolution
//        double[] crossCorr = calculateFFTConvolution(signal, window);
//
//        int startingPoint = findMaxIndex(crossCorr);
//        double[] extractedSignal = new double[signalLength];
//        System.arraycopy(signal, startingPoint, extractedSignal, 0, signalLength);
//        return extractedSignal;
//    }
//
//    private double[] calculateFFTConvolution(double[] signal, double[] window) {
//        int length = Math.max(signal.length, window.length);
//        double[] padSignal = new double[length];
//        double[] padWindow = new double[length];
//
//        System.arraycopy(signal, 0, padSignal, 0, signal.length);
//        System.arraycopy(window, 0, padWindow, 0, window.length);
//
//        DoubleFFT_1D fft = new DoubleFFT_1D(length);
//        fft.realForward(padSignal);
//        fft.realForward(padWindow);
//
//        double[] crossCorrelation = new double[length * 2];
//        for (int i = 0; i < length * 2; i += 2) {
//            double real1 = paddedSignal1[i / 2];
//            double imag1 = paddedSignal1[i / 2 + 1];
//            double real2 = paddedSignal2[i / 2];
//            double imag2 = paddedSignal2[i / 2 + 1];
//
//            crossCorrelation[i] = real1 * real2 - imag1 * imag2;
//            crossCorrelation[i + 1] = real1 * imag2 + imag1 * real2;
//        }
//
//        // Perform inverse FFT on the cross-correlation
//        fft.realInverseFull(crossCorrelation, true);
//
//        return crossCorrelation;
//    }
//
//    private int findMaxIndex(double[] array) {
//        int maxIndex = 0;
//        double maxValue = array[0];
//        for (int i = 1; i < array.length; i++) {
//            if (array[i] > maxValue) {
//                maxValue = array[i];
//                maxIndex = i;
//            }
//        }
//        return maxIndex;
//    }

    // find out whether chirp is still generating
    public boolean getIsRecording() {
        return isRecording;
    }

    // Records audio signal
    @SuppressLint("MissingPermission")
    private void startRecording() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AudioRecordService:WakeLock");
        wakeLock.acquire();

        // For a given path, check for available file number.
        // This prevents the overlapping of files recorded from multiple trials.
        // After that, designate the file where the recorded audio is saved
        File path = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_AUDIOBOOKS);
        }
        int trialNumber = 1;
        String audioFilename;

        while (true) {
            @SuppressLint("DefaultLocale") String s = String.format("%03d", trialNumber);
            audioFilename = "recorded_audio" + s + ".wav";
            File file = new File(path, audioFilename);
            if (!file.exists()) {
                break;
            }
            trialNumber++;
        }

        String audioFilepath = new File(path, audioFilename).getAbsolutePath();

        // designate buffer size for audio recording
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioRecord", "AudioRecord initialization failed");
            return;
        }

        audioRecord.startRecording();
        isRecording = true;

        // Start a new thread to read the audio data from AudioRecord and save it to a file
        // The thread runs recordAudioRunnable to log IMU data
        recordAudioRunnable audioRunnable = new recordAudioRunnable(audioFilepath, bufferSize);
        Thread AudioThread = new Thread(audioRunnable);
        AudioThread.start();
    }

    @SuppressLint("MissingPermission")
    private void startRecording(String audioFilename) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AudioRecordService:WakeLock");
        wakeLock.acquire();

        // For a given path, check for available file number.
        // This prevents the overlapping of files recorded from multiple trials.
        // After that, designate the file where the recorded audio is saved
        File path = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_AUDIOBOOKS);
        }


        String audioFilepath = new File(path, audioFilename).getAbsolutePath();

        // designate buffer size for audio recording
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioRecord", "AudioRecord initialization failed");
            return;
        }

        audioRecord.startRecording();
        isRecording = true;

        // Start a new thread to read the audio data from AudioRecord and save it to a file
        // The thread runs recordAudioRunnable to log IMU data
        recordAudioRunnable audioRunnable = new recordAudioRunnable(audioFilepath, bufferSize);
        Thread AudioThread = new Thread(audioRunnable);
        AudioThread.start();
    }

    private class recordAudioRunnable implements Runnable {
        private final String audioFilepath;
        private final int bufferSize;

        public recordAudioRunnable(String audioFilepath, int bufferSize){
            this.audioFilepath = audioFilepath;
            this.bufferSize = bufferSize;
        }

        @Override
        public void run() {
            File outputFile = new File(audioFilepath);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[bufferSize];
                while (isRecording) {
                    int readBytes = audioRecord.read(buffer, 0, bufferSize);
                    if (readBytes > 0) {
                        fos.write(buffer, 0, readBytes);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class getAudioDataRunnable implements Runnable {
        private final int bufferSize;
        List<Double> doubleList = new ArrayList<>();

        public getAudioDataRunnable(int bufferSize){
            this.bufferSize = bufferSize;
        }

        public List<Double> getRecordedChirp() {
            return this.doubleList;
        }

        @Override
        public void run() {
            byte[] audioData = new byte[bufferSize];
            while (isRecording) {
                int bytesRead = audioRecord.read(audioData, 0, bufferSize);

                for (int i = 0; i < bytesRead; i++) {
                    doubleList.add((double) audioData[i] / 32768.0); // Convert to double [-1.0, 1.0]
                }
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        createNotificationChannel();

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audio Recording")
                .setContentText("Recording audio in the background")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void monitorTemperature(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        float temp = ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0)) / 10;

        Log.d("Temperature", "Device temperature: " + String.valueOf(temp) + "*C");
    }

    //////////////////////////////////////////////////
//////////////////////////////////////////////////
    // After you click Stop Recording button
    private void stopRecording() {
        isRecording = false;

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }
}
