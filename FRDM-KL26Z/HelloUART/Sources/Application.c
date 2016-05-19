#include "Application.h"
#include "RxBuf.h"
#include "AS1.h"

static UART_Desc deviceData;
static bool pressed;

static void SendChar(unsigned char ch, UART_Desc *desc) {
	desc->isSent = FALSE; /* this will be set to 1 once the block has been sent */
	while (AS1_SendBlock(desc->handle, (LDD_TData*) &ch, 1) != ERR_OK) {
	} /* Send char */
	while (!desc->isSent) {
	} /* wait until we get the green flag from the TX interrupt */
}

static void SendString(const unsigned char *str, UART_Desc *desc) {
	while (*str != '\0') {
		SendChar(*str++, desc);
	}
}

static void Init(void) {
	pressed = FALSE;
	/* initialize struct fields */
	deviceData.handle = AS1_Init(&deviceData);
	deviceData.isSent = FALSE;
	deviceData.rxChar = '\0';
	deviceData.rxPutFct = RxBuf_Put;
	/* set up to receive RX into input buffer */
	RxBuf_Init(); /* initialize RX buffer */
	/* Set up ReceiveBlock() with a single byte buffer. We will be called in OnBlockReceived() event. */
	while (AS1_ReceiveBlock(deviceData.handle, (LDD_TData *) &deviceData.rxChar,
			sizeof(deviceData.rxChar)) != ERR_OK) {
	} /* initial kick off for receiving data */
}

static void LED_Neg(void) {
	LEDR_Neg();
	LEDG_Neg();
	LEDB_Neg();
}

void reactToButton(void) {
	if (!Button_GetVal(Button_DeviceData ) && !pressed) {
		pressed = TRUE;
		LED_Neg();
		SendString((unsigned char*) "press\r\n", &deviceData);
	} else if (Button_GetVal(Button_DeviceData ) && pressed) {
		pressed = FALSE;
		SendString((unsigned char*) "release\r\n", &deviceData);
	}
}

void switchLEDs(unsigned char ch) {
	switch (ch) {
	case 'r':
		LEDR_Neg();
		break;
	case 'g':
		LEDG_Neg();
		break;
	case 'b':
		LEDB_Neg();
		break;
	default:
		LED_Neg();
		break;
	}
}

void APP_Run(void) {
	Init();
	SendString((unsigned char*) "Hello World\r\n", &deviceData);
	for (;;) {
		if (RxBuf_NofElements() != 0) {
			SendString((unsigned char*) "echo: ", &deviceData);
			while (RxBuf_NofElements() != 0) {
				unsigned char ch;

				(void) RxBuf_Get(&ch);
				SendChar(ch, &deviceData);

				switchLEDs(ch);
			}
			SendString((unsigned char*) "\r\n", &deviceData);
		}

		reactToButton();
	}
}
