package com.example;

import android.content.Context;
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

    private final String TAG = getString(R.string.app_name);
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
    private TextView consoleTextView;

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
        consoleTextView = (TextView) findViewById(R.id.consoleTextView);

        enableLEDs(false);
        setOnClickListeners();

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();

        for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
            device = entry.getValue();
        }

        if (!usbManager.hasPermission(device)) {
            usbManager.requestPermission(device, null);
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
                consoleTextView.setText("");
            }
        });

        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serial.write("r".getBytes());
            }
        });

        greenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serial.write("g".getBytes());
            }
        });

        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serial.write("b".getBytes());
            }
        });

        negButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serial.write("rgb".getBytes());
            }
        });
    }

    private void enableLEDs(boolean enable) {
        redButton.setEnabled(enable);
        greenButton.setEnabled(enable);
        blueButton.setEnabled(enable);
        negButton.setEnabled(enable);
    }

    private void connect() {
        connection = usbManager.openDevice(device);
        serial = UsbSerialDevice.createUsbSerialDevice(device, connection);

        serial.open(); // TODO move to end?
        serial.setBaudRate(9600);
        serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
        serial.setParity(UsbSerialInterface.PARITY_ODD);
        serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

        serial.read(new UsbSerialInterface.UsbReadCallback() {
            @Override
            public void onReceivedData(final byte[] bytes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String str = null;
                        try {
                            str = new String(bytes, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        consoleTextView.append(str);
                    }
                });
            }
        });

        enableLEDs(true);

        connectButton.setText("disconnect");
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serial.close();
                enableLEDs(false);
                connectButton.setText("connect");
                connectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connect();
                    }
                });
            }
        });
    }
}
