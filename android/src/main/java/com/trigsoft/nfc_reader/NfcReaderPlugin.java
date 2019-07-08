package com.trigsoft.nfc_reader;

import android.app.Activity;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.nio.charset.StandardCharsets;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * NfcReaderPlugin
 */

public class NfcReaderPlugin implements MethodCallHandler, StreamHandler, NfcAdapter.ReaderCallback {

    private NfcAdapter nfcAdapter;
    private EventChannel.EventSink eventSink;

    private String oid = "nfcId";
    private String content = "nfcContent";
    private String error = "nfcError";
    private String status = "nfcStatus";

    private Integer PERMISSION_NFC = 1007;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel methodChannel = new MethodChannel(registrar.messenger(), "nfc_reader");
        final EventChannel eventChannel = new EventChannel(registrar.messenger(), "com.trigsoft.nfc_read_write.nfc_reader");
        NfcReaderPlugin instance = new NfcReaderPlugin(registrar.activity());
        methodChannel.setMethodCallHandler(instance);
        eventChannel.setStreamHandler(instance);
    }

    private final Activity activity;

    NfcReaderPlugin(Activity activity) {
        this.activity = activity;
    }


    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "readNFC":
                startReading();
                result.success(null);
                break;
            case "stopNFC":
                stopReading();
                Map<String, String> stopMap = new HashMap<>();
                stopMap.put(error, "");
                stopMap.put(status, "stopped");
            default:
                result.notImplemented();
        }
    }

    @Override
    public void onListen(Object arguments, EventSink events) {
        this.eventSink = events;
    }

    @Override
    public void onCancel(Object arguments) {
        eventSink = null;
        stopReading();
    }

    private void startReading() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter == null) {
            return;
        }
        Bundle bundle = new Bundle();
        nfcAdapter.enableReaderMode(activity, this, NfcAdapter.FLAG_READER_NFC_A, bundle);
    }

    private void stopReading() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            nfcAdapter.disableReaderMode(activity);
        }
        eventSink = null;
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        Ndef ndef = Ndef.get(tag);

        if (ndef == null) {
            return;
        }

        try {

            ndef.connect();
            NdefMessage message = ndef.getNdefMessage();

            if (message == null) {
                return;
            }

            Map<String, String> recordMap = new HashMap<>();
            recordMap.put(content, new String(message.getRecords()[0].getPayload(), StandardCharsets.UTF_8));
            recordMap.put(oid, new String(message.getRecords()[0].getId(), StandardCharsets.UTF_8));
            recordMap.put(error, "");
            recordMap.put(status, "read");

            eventSuccess(recordMap);

            try {
                ndef.close();
            } catch (IOException e) {
                Map<String, Object> details = new HashMap<>();
                details.put("fatal", true);
                eventError("IOError", e.getMessage(), details);
            }
        } catch (IOException e) {
            Map<String, Object> details = new HashMap<>();
            details.put("fatal", true);
            eventError("IOError", e.getMessage(), details);
        } catch (FormatException e) {
            eventError("NDEFBadFormatError", e.getMessage(), null);
        }


    }

    private void eventSuccess(final Object result) {
        Handler mainThread = new Handler(activity.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (eventSink != null) {
                    eventSink.success(result);
                }
            }
        };
        mainThread.post(runnable);
    }

    private void eventError(final String code, final String message, final Object details) {
        Handler mainThread = new Handler(activity.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (eventSink != null) {
                    eventSink.error(code, message, details);
                }
            }
        };
        mainThread.post(runnable);
    }

}
