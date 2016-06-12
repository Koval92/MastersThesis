UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
ProbeTable customTable = new ProbeTable();
customTable.addProduct(VENDOR_ID1, PRODUCT_ID1, CdcAcmSerialDriver.class);
customTable.addProduct(VENDOR_ID2, PRODUCT_ID2, CdcAcmSerialDriver.class);
UsbSerialProber prober = new UsbSerialProber(customTable);
List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);
UsbSerialDriver driver = availableDrivers.get(0);
UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
if (connection == null) {
  // probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
  return;
}
UsbSerialPort port = driver.getPorts.get(0);
port.open(connection);
try {
  port.setParameters(/*int baudRate, int dataBits, int stopBits, int parity*/);
  byte writeBuffer[] = "text".getBytes();
  byte readBuffer[] = new byte[16];
  int numBytesWritten = port.write(writeBuffer, TIMEOUT)
  int numBytesRead = port.read(readBuffer, TIMEOUT);
} catch (IOException e) {
  // Deal with error.
} finally {
  port.close();
}