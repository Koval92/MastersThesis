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
    private final String TAG = "felHR85-own";
    private final String TEXT32 = "String with 32 chars..987654321!";
    private final String TEXT50 = "String with 50 chars....................987654321!";
    private final String TEXT64 = "String with 64 chars..................................987654321!";
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
    private Button send32Button;
    private Button send50Button;
    private Button send64Button;
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
        send32Button = (Button) findViewById(R.id.send32Button);
        send50Button = (Button) findViewById(R.id.send50Button);
        send64Button = (Button) findViewById(R.id.send64Button);

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
                write("rgb");
            }
        });

        send32Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(TEXT32);
            }
        });

        send50Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(TEXT50);
            }
        });

        send64Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(TEXT64);
            }
        });
    }

    private void write(final String text) {
        byte buffer[] = text.getBytes();
        writeTime = System.nanoTime();
        serial.syncWrite(buffer, WRITE_TIMEOUT);
    }

    private String read() { // TODO invoke method and measure time
        byte buffer[] = new byte[64];
        serial.syncRead(buffer, READ_TIMEOUT);

        String str = null;
        try {
            str = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final String msg = "Read:>>" + str + "<< \n";

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logTextView.append(msg);
            }
        });

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