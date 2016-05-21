package com.example.bulktransfer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends Activity {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private TextView logTextView;
    private TextView infoButton;
    private TextView redButton;
    private TextView greenButton;
    private TextView blueButton;
    private TextView negButton;
    private UsbDevice device = null;
    private UsbInterface usbInterface = null;
    private UsbEndpoint usbEndpointOut = null;
    private UsbEndpoint usbEndpointIn = null;
    private PendingIntent mPermissionIntent;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice newDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (newDevice != null) {
                            device = newDevice;
                            readDescriptors();
                        }
                    } else {
                        addText("-----Error: permission!");
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logTextView = (TextView) findViewById(R.id.logTexrView);
        infoButton = (Button) findViewById(R.id.connectButton);
        redButton = (Button) findViewById(R.id.redButton);
        greenButton = (Button) findViewById(R.id.greenButton);
        blueButton = (Button) findViewById(R.id.blueButton);
        negButton = (Button) findViewById(R.id.negButton);

        addText("Start!");

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    checkDeviceInfo();
                    addText("\n\n");
                    readDescriptors();
                } catch (Exception e) {
                    addText("\n---Error 0!\n");
                    addText(e.getMessage() + "\n");
                    addText(e.getLocalizedMessage() + "\n");
                    addText(e.toString() + "\n");
                }
            }
        });

        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addText("RED clicked");
                byte values[] = "r".getBytes();
                sendBytes(values);
            }
        });

        greenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addText("GREEN clicked");
                byte values[] = "g".getBytes();
                sendBytes(values);
            }
        });

        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addText("BLUE clicked");
                byte values[] = "b".getBytes();
                sendBytes(values);
            }
        });

        negButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addText("NEG clicked");
                byte values[] = "d".getBytes();
                sendBytes(values);
            }
        });


        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
    }

    private boolean sendBytes(byte values[]) {
        try {
            addText("Sending: " + values.toString() + " " + new String(values, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (device == null) {
            addText("-----Error: no device!");
            return false;
        }

        final int RQSID_SET_LINE_CODING = 0x20;
        final int RQSID_SET_CONTROL_LINE_STATE = 0x22;

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Boolean permitToRead = manager.hasPermission(device);

        if (permitToRead) {
            UsbDeviceConnection connection = manager.openDevice(device);
            if (connection != null) {
                connection.claimInterface(usbInterface, true);

                int usbResult;
                usbResult = connection.controlTransfer(
                        0x21,                            // requestType
                        RQSID_SET_CONTROL_LINE_STATE,    // SET_CONTROL_LINE_STATE
                        0,                                // value
                        0,                                // index
                        null,                            // buffer
                        0,                                // length
                        0                                // timeout
                );

                // baud rate = 9600
                // 8 data bit
                // 1 stop bit

                byte[] encodingSetting = new byte[]{(byte) 0x80, 0x25, 0x00, 0x00, 0x00, 0x00, 0x08};
                usbResult = connection.controlTransfer(
                        0x21,                        //requestType
                        RQSID_SET_LINE_CODING,        //SET_LINE_CODING
                        0,                            //value
                        0,                            //index
                        encodingSetting,            //buffer
                        7,                            //length
                        0                            //timeout
                );

                usbResult = connection.bulkTransfer(
                        usbEndpointOut,
                        values,
                        values.length,
                        0
                );

                byte readBuffer[] = new byte[64];
                connection.bulkTransfer(usbEndpointIn, readBuffer, readBuffer.length, 0);
                try {
                    addText("Read: " + new String(readBuffer, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return (usbResult > 0);
            } else {
                addText("-----Error: can't open device!");
            }
        } else {
            manager.requestPermission(device, mPermissionIntent);
            addText("-----Error: permission!");
        }

        return false;
    }

    private void readDescriptors() {
        if (device == null) {
            addText("-----Error: no device!");
            return;
        }

        final int STD_USB_REQUEST_GET_DESCRIPTOR = 0x06;
        final int LIBUSB_DT_STRING = 0x03;

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Boolean permitToRead = manager.hasPermission(device);

        if (permitToRead) {
            boolean forceClaim = true;
            byte[] buffer = new byte[255];
            int indexManufacturer = 14;
            int indexProduct = 15;
            String stringManufacturer = "";
            String stringProduct = "";

            UsbDeviceConnection connection = manager.openDevice(device);

            if (connection != null) {
                UsbInterface intf = device.getInterface(0);
                connection.claimInterface(intf, forceClaim);

                byte[] rawDescriptors = connection.getRawDescriptors();

                int lengthManufacturer = connection.controlTransfer(
                        UsbConstants.USB_DIR_IN | UsbConstants.USB_TYPE_STANDARD,
                        STD_USB_REQUEST_GET_DESCRIPTOR,
                        (LIBUSB_DT_STRING << 8) | rawDescriptors[indexManufacturer],
                        0,
                        buffer,
                        0xFF,
                        0
                );

                try {
                    stringManufacturer = new String(buffer, 2, lengthManufacturer - 2, "UTF-16LE");
                } catch (UnsupportedEncodingException e) {
                    addText("-----Error 1: " + e.getMessage());
                }

                int lengthProduct = connection.controlTransfer(
                        UsbConstants.USB_DIR_IN | UsbConstants.USB_TYPE_STANDARD,
                        STD_USB_REQUEST_GET_DESCRIPTOR,
                        (LIBUSB_DT_STRING << 8) | rawDescriptors[indexProduct],
                        0,
                        buffer,
                        0xFF,
                        0
                );
                try {
                    stringProduct = new String(buffer, 2, lengthProduct - 2, "UTF-16LE");
                } catch (UnsupportedEncodingException e) {
                    addText("-----Error 2: " + e.getMessage());
                }

                addText("Manufacturer: " + stringManufacturer);
                addText("Product: " + stringProduct);

                connection.releaseInterface(intf);
                connection.close();
            } else {
                addText("-----Error: can't open device");
            }
        } else {
            manager.requestPermission(device, mPermissionIntent);
            addText("-----Error: permission!");
        }
    }

    private void checkDeviceInfo() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        UsbDevice device = null;
        UsbInterface USBinterface = null;
        UsbEndpoint tIn = null;
        UsbEndpoint tOut = null;


        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();

            addText("\n\n---New device:");
            addText("-----DeviceID:" + device.getDeviceId());
            addText("-----DeviceName:" + device.getDeviceName());
            addText("-----DeviceClass:" + device.getDeviceClass());
            addText("-----DeviceSubClass:" + device.getDeviceSubclass());
            addText("-----VendorID:" + device.getVendorId());
            addText("-----ProductID:" + device.getProductId());
            addText("-----InterfaceCount:" + device.getInterfaceCount());

            for (int i = 0; i < device.getInterfaceCount(); i++) {
                UsbInterface usbif = device.getInterface(i);

                addText("-----New Interface:");
                addText("-------Id:" + usbif.getId());
                addText("-------InterfaceClass:" + usbif.getInterfaceClass());
                addText("-------InterfaceProtocol:" + usbif.getInterfaceProtocol());
                addText("-------InterfaceSubclass:" + usbif.getInterfaceSubclass());
                addText("-------EndpointCount:" + usbif.getEndpointCount());

                for (int j = 0; j < usbif.getEndpointCount(); j++) {
                    UsbEndpoint usbEndpoint = usbif.getEndpoint(j);
                    addText("-------New Endpoint:");
                    addText("---------Endpoint: " + usbEndpoint.toString());

                    if (usbif.getEndpointCount() >= 2) {
                        if (usbEndpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                            if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                                USBinterface = usbif;
                                tOut = usbEndpoint;
                            }

                            if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                                USBinterface = usbif;
                                tIn = usbEndpoint;
                            }
                        }
                    }
                }
            }
        }

        if (device == null) {
            addText("-----Error: no device!");
            return;
        }

        this.device = device;
        this.usbInterface = USBinterface;
        this.usbEndpointOut = tOut;
        this.usbEndpointIn = tIn;

        if (this.usbInterface == null || this.usbEndpointOut == null || this.usbEndpointIn == null) {
            return;
        }

        addText("\n");
        addText("---device: " + this.device.toString());
        addText("---usbInterface: " + this.usbInterface.toString());
        addText("---usbEndpointOut: " + this.usbEndpointOut.toString());
        addText("---usbEndpointIn: " + this.usbEndpointIn.toString());
    }

    private void addText(String text) {
        logTextView.append("\n" + text);
    }
}
