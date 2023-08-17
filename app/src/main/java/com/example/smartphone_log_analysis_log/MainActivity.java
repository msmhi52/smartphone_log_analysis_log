package com.example.smartphone_log_analysis_log;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private AudioService audioService;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean isServiceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AudioService.AudioServiceBinder binder = (AudioService.AudioServiceBinder) iBinder;
            audioService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.smartphone_log_analysis_log.START_CHIRP_RECORDING".equals(intent.getAction())) {
                // need to send and record chirp
                Intent serviceIntent = new Intent(MainActivity.this, AudioService.class);
                serviceIntent.setAction(intent.getAction());
                startService(serviceIntent);
                Log.d("Broadcast", "Broadcast received");  // For debugging
            }
            else if ("com.example.smartphone_log_analysis_log.START_MODIFIED_CHIRP".equals(intent.getAction())) {
                // need to send and record modified chirp
                Intent serviceIntent = new Intent(MainActivity.this, AudioService.class);
                serviceIntent.setAction(intent.getAction());
                startService(serviceIntent);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ask audio recording permission when started
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // if the permission is not granted, ask permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        IntentFilter intentFilter1 = new IntentFilter("com.example.smartphone_log_analysis_log.START_CHIRP_RECORDING");
        IntentFilter intentFilter2 = new IntentFilter("com.example.smartphone_log_analysis_log.START_MODIFIED_CHIRP");
        registerReceiver(intentReceiver, intentFilter1);
        registerReceiver(intentReceiver, intentFilter2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        unregisterReceiver(intentReceiver);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
            }
        }
    }
}

