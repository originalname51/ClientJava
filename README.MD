This is a FTP Client written in Java. This works by following the protocols listed in server.c. I have included the protocols below.

This will allow download of ANY filetype (including binary files).

1) To access the server the following input is expected.

For a LIST command:

serverName serverPortNumber -l ftpPortNumber

For a GET command:

serverName serverPortNumber -g fileName ftpPortNumber


The following are protocols used in order to communicate with the server. Software must be writtent o work with the protocols the server has.

2) The server communicates with the client by parsing the first message by reading 4 characters as integers. 
In order to send over the command first send the amount of characters in the message + 100(CHAR_OFFSET) to the server. 
After this send the message.

3)  For List command : 
	
a) Server will send over amount of files it has in a string of 17 characters. 
Please note: the string sent over will have the integer value in characters and then it will be buffered with '\0'.

b) Server will send over 256 characters each containing a file name. Extra characters will be buffered by a '\0'.

4) For Get command : 

The server will sent how large the file requested was, or -1 for error file not found/availible. 
This is sent over in a string of 17 characters with non integer characters set to '\0'
The server will then send the file.

The client will read all data into a dynamically allocated byte buffer. It will then write byte buffer to a file. 
This will work up to files up to 2^31-1 bytes large.

**How this works: This works because the C server send data in char size. The char size is the size of a byte. 
This allows for easy transfering of files from C server to java byte array. This uses a InputStream instead of InputStreamReader.





  


