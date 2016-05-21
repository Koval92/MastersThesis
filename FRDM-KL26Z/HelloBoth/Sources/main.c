/* ###################################################################
**     Filename    : main.c
**     Project     : HelloCDC
**     Processor   : MKL26Z128VMC4
**     Version     : Driver 01.01
**     Compiler    : GNU C Compiler
**     Date/Time   : 2016-05-10, 16:46, # CodeGen: 0
**     Abstract    :
**         Main module.
**         This module contains user's application code.
**     Settings    :
**     Contents    :
**         No public methods
**
** ###################################################################*/
/*!
** @file main.c
** @version 01.01
** @brief
**         Main module.
**         This module contains user's application code.
*/         
/*!
**  @addtogroup main_module main module documentation
**  @{
*/         
/* MODULE main */


/* Including needed modules to compile this module/procedure */
#include "Cpu.h"
#include "Events.h"
#include "WAIT1.h"
#include "KSDK1.h"
#include "USB1.h"
#include "USB0.h"
#include "CDC1.h"
#include "Tx1.h"
#include "Rx1.h"
#include "CS1.h"
#include "TMOUT1.h"
#include "LEDR.h"
#include "LEDpin1.h"
#include "BitIoLdd1.h"
#include "LEDG.h"
#include "LEDpin2.h"
#include "BitIoLdd2.h"
#include "LEDB.h"
#include "LEDpin3.h"
#include "BitIoLdd3.h"
#include "Button.h"
#include "AS1.h"
#include "RxBuf.h"
/* Including shared modules, which are used for whole project */
#include "PE_Types.h"
#include "PE_Error.h"
#include "PE_Const.h"
#include "IO_Map.h"

/* User includes (#include below this line is not maintained by Processor Expert) */
#include "main.h"

static uint8_t cdc_buffer[USB1_DATA_BUFF_SIZE];
static uint8_t in_buffer[USB1_DATA_BUFF_SIZE];
static UART_Desc deviceData;

bool cdc_connected = FALSE;
bool pressed = FALSE;

static void LED_Neg(void) {
	LEDR_Neg();
	LEDG_Neg();
	LEDB_Neg();
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

void reactToButton(void) { // TODO signal cdc not connected
	if (!Button_GetVal(Button_DeviceData ) && !pressed) {
		pressed = TRUE;
		LED_Neg();
		SendString((unsigned char*) "press\r\n", &deviceData);
		if(cdc_connected) {
			CDC1_SendString((unsigned char*) "press\r\n");
		}
	} else if (Button_GetVal(Button_DeviceData ) && pressed) {
		pressed = FALSE;
		SendString((unsigned char*) "release\r\n", &deviceData);
		if(cdc_connected) {
			CDC1_SendString((unsigned char*) "release\r\n");
		}
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

static void reactToUART(void) {
	if (RxBuf_NofElements() != 0) {
		SendString((unsigned char*) "uart: ", &deviceData);
		while (RxBuf_NofElements() != 0) {
			unsigned char ch;
			(void) RxBuf_Get(&ch);
			SendChar(ch, &deviceData);
			switchLEDs(ch);
		}
		SendString((unsigned char*) "\r\n", &deviceData);
	}
}

static void CDC_Run(void) {
  int i;
  Init();
  SendString((unsigned char*) "uart: Hello World\r\n", &deviceData);
  for(;;) {
	reactToButton();  
	reactToUART();
    cdc_connected = !(CDC1_App_Task(cdc_buffer, sizeof(cdc_buffer))==ERR_BUSOFF);
    if (CDC1_GetCharsInRxBuf()!=0) {
      i = 0;
      while(i<sizeof(in_buffer)-1 && CDC1_GetChar(&in_buffer[i])==ERR_OK) {
    	switchLEDs(in_buffer[i]);  
        i++;
      }
      in_buffer[i] = '\0';
      (void)CDC1_SendString((unsigned char*)"cdc: ");
      (void)CDC1_SendString(in_buffer);
      (void)CDC1_SendString((unsigned char*)"\r\n");
    } else {
      WAIT1_Waitms(10);
    }
  }
}

/*lint -save  -e970 Disable MISRA rule (6.3) checking. */
int main(void)
/*lint -restore Enable MISRA rule (6.3) checking. */
	{
  /* Write your local variable definition here */

  /*** Processor Expert internal initialization. DON'T REMOVE THIS CODE!!! ***/
  PE_low_level_init();
  /*** End of Processor Expert internal initialization.                    ***/

  /* Write your code here */
  /* For example: for(;;) { } */
  CDC_Run();
  /*** Don't write any code pass this line, or it will be deleted during code generation. ***/
  /*** RTOS startup code. Macro PEX_RTOS_START is defined by the RTOS component. DON'T MODIFY THIS CODE!!! ***/
  #ifdef PEX_RTOS_START
    PEX_RTOS_START();                  /* Startup of the selected RTOS. Macro is defined by the RTOS component. */
  #endif
  /*** End of RTOS startup code.  ***/
  /*** Processor Expert end of main routine. DON'T MODIFY THIS CODE!!! ***/
  for(;;){}
  /*** Processor Expert end of main routine. DON'T WRITE CODE BELOW!!! ***/
} /*** End of main routine. DO NOT MODIFY THIS TEXT!!! ***/

/* END main */
/*!
** @}
*/
/*
** ###################################################################
**
**     This file was created by Processor Expert 10.3 [05.09]
**     for the Freescale Kinetis series of microcontrollers.
**
** ###################################################################
*/
