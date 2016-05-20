/*
 * main.h
 *
 *  Created on: May 20, 2016
 *      Author: micha
 */

#ifndef MAIN_H_
#define MAIN_H_

typedef struct {
  LDD_TDeviceData *handle; /* LDD device handle */
  volatile uint8_t isSent; /* this will be set to 1 once the block has been sent */
  uint8_t rxChar; /* single character buffer for receiving chars */
  uint8_t (*rxPutFct)(uint8_t); /* callback to put received character into buffer */
} UART_Desc;

#endif /* MAIN_H_ */
