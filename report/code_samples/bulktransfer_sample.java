UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
UsbDeviceConnection connection = manager.openDevice(device)
/* lot of code to find endpoints */
connection.controlTransfer(/* lot of hard to deduct parameters */);
// write
connection.bulkTransfer(usbEndpointOut, writeBuffer, writeBuffer.length, TIMEOUT);
// read
connection.bulkTransfer(usbEndpointIn, readBuffer, readBuffer.length, TIMEOUT);