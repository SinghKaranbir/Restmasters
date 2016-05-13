#!/bin/bash
while [ 5 -lt 10 ]
do
	sdptool add sp
	sudo rfcomm listen hci0
done
