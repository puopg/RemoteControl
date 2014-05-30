RemoteControl
=============

Proof of concept to get 2 android devices to communicate over WiFi using UDP

This implements a Server and Client using UDP. The main thread can be updated due to the use of AsyncTask. Basically
just a simple example of one way of controlling a device from another. 

README for RemoteControl.apk

Communication using UDP. For use on the same WiFi network. 

***************************
* Server ******************
*        ******************
***************************
Server begins in a stopped state. To start the server, press the start button. At this point, it will
be listening for packets on port 4444 (arbitrarily chosen). If no packet is received within 10 seconds
the server will timeout and need to be restarted. You can stop the server by pressing the stop button.


***************************
* Client ******************
*        ******************
***************************
To switch to Client mode, press menu and select the client. Configure the client by providing the IP
that packets should be sent to. The server will display its current IP on the WiFi network for your convenience.
When the server is started, buttons can be pressed and should toggle the buttons on the server. Move arrows will move
the Move Me! box. If a location other than a button is pressed, a red dot will appear on the client and a server
dot will appear at a relative screen location. If you hold the screen location down and drag, the application will
get very laggy since so many packets are being processed. I don't reccomend doing that. 
