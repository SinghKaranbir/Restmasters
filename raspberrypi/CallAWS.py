#! /usr/bin/python

import sched, time
import requests
import json
import sys
import serial
from time import sleep
s = sched.scheduler(time.time, time.sleep)
def do_something(sc): 
    print "Getting data..."
    try:
	bluetoothSerial = serial.Serial( "/dev/rfcomm0", baudrate=9600 )
	line =""
	flag = 0
	email =""
	id =""
	while True:
            for c in bluetoothSerial.read():
        	print c
        	if c == '~':
        	    flag = flag + 1
        	    print("Line: " + line)
        	    if flag == 1:
        	        email = line
        	        line = ""
        	    if flag == 2:
        	        id = line
        	else:
        	    line+=c
            if flag == 2:
        	break
	print email
	print id
	r=requests.post("http://ec2-52-35-75-223.us-west-2.compute.amazonaws.com:3000/attendance/mark",data={'email':email ,'course_id':id})
	print r
	resp = json.loads(r.text)
	print resp
    except:
	e = sys.exc_info()[0]
    sc.enter(2, 1, do_something, (sc,))

s.enter(1, 1, do_something, (s,))
s.run()




