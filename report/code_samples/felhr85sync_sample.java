UsbSerialDevice serial = /* same as in async */

serial.syncOpen();
serial.syncOpen();
serial.syncWrite(/*byte[] writeBuffer, int timeout*/)
serial.syncRead(/*byte[] readBuffer, int timeout*/)
serial.syncClose();