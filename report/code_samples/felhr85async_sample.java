usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
	device = entry.getValue();
}
UsbDeviceConnection connection = usbManager.openDevice(device);
UsbSerialDevice serial = UsbSerialDevice.createUsbSerialDevice(device, connection);
serial.open();
serial.setBaudRate(115200);
serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
serial.setParity(UsbSerialInterface.PARITY_ODD);
serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF); 
serial.read(new UsbSerialInterface.UsbReadCallback() {
            @Override
            public void onReceivedData(final byte[] bytes) {
				// to domething with bytes, like: 
				String str = new String(bytes, "UTF-8");
			}
        });
serial.write("TEXT".getBytes());
serial.close();