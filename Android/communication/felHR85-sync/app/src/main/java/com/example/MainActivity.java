package com.example;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int WRITE_TIMEOUT = 100;
    private static final int READ_TIMEOUT = 100;
    public static final int REPEATS = 10;
    private final String TAG = "felHR85-own";
    private final String TEXT_SHORT = "String with 32 chars..987654321!";
    private final String TEXT_MEDIUM = "String with 50 chars....................987654321!";
    private final String TEXT_LONG = "String with 64 chars..................................987654321!";
    UsbSerialDevice serial;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private Button connectButton;
    private Button clearButton;
    private Button redButton;
    private Button greenButton;
    private Button blueButton;
    private Button negButton;
    private Button sendShortButton;
    private Button sendMediumButton;
    private Button sendLongButton;
    private TextView logTextView;

    private long writeTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = (Button) findViewById(R.id.connectButton);
        clearButton = (Button) findViewById(R.id.clearButton);
        redButton = (Button) findViewById(R.id.redButton);
        greenButton = (Button) findViewById(R.id.greenButton);
        blueButton = (Button) findViewById(R.id.blueButton);
        negButton = (Button) findViewById(R.id.negButton);
        logTextView = (TextView) findViewById(R.id.logTextView);
        sendShortButton = (Button) findViewById(R.id.sendShortButton);
        sendMediumButton = (Button) findViewById(R.id.sendMediumButton);
        sendLongButton = (Button) findViewById(R.id.sendLongButton);

        setOnClickListeners();

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();

        for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
            device = entry.getValue();
        }

        if (!usbManager.hasPermission(device)) {
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent("perm_intent"), 0);
            usbManager.requestPermission(device, pi);
        }
    }

    private void setOnClickListeners() {
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logTextView.setText("");
            }
        });

        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("r");
            }
        });

        greenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("g");
            }
        });

        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("b");
            }
        });

        negButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("d");
            }
        });

        sendShortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(TEXT_SHORT);
            }
        });

        sendMediumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(TEXT_MEDIUM);
            }
        });

        sendLongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(TEXT_LONG);
            }
        });
    }

    private void write(final String text) {
        logTextView.append("Sending " + REPEATS + " times: " + text + "\n");
        byte buffer[] = text.getBytes();
        writeTime = System.nanoTime();
        for(int i = 0; i< REPEATS; i++) {
            serial.syncWrite(buffer, WRITE_TIMEOUT);
            logTextView.append(read());
        }
        long endTime = System.nanoTime();
        long durationInMillis = (endTime-writeTime)/1000000;

        logTextView.append("\nExecuted in " + durationInMillis + " ms\n");
    }

    private String read() {
        byte buffer[] = new byte[128];

        int readBytes;
        do {
            readBytes = serial.syncRead(buffer, READ_TIMEOUT);
        } while (readBytes < 1);

        String str = null;
        try {
            str = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return str;
    }

    private void connect() {
        connection = usbManager.openDevice(device);
        serial = UsbSerialDevice.createUsbSerialDevice(device, connection);

        serial.syncOpen(); // TODO move to end?
        serial.setBaudRate(9600);
        serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
        serial.setParity(UsbSerialInterface.PARITY_NONE);
        serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
    }
}
