
# Distributed Database

## Database Structure
The database consists of nodes that together form an undirected graph. In this type of structure, cycles can form, which require special attention. Some database operations resemble the DFS (Depth First Search) algorithm, which in its pure form on a cyclic graph can execute recursively infinitely, effectively preventing the database from functioning.

## Protocol
The protocol used in the database is a stateless protocol.

### Message Structure
```
[task ID] <operation> [args]
```
- *operation* - an operation performed on the database requested by the client
- *args* - optionally, additional parameters for the operation
- *task ID* - UUID of the operation, a number uniquely representing the operation

### Task ID
In client-node communication, the task ID is not required, which means the client can omit it without any implications. The node that receives the message checks if it has an identifier, and if it is absent, generates one. Nodes, before propagating the message to other nodes, record its identifier to determine if the same message has reached them again, creating a cycle. If a cycle is detected, the node closes the connection and does not process the operation further. Nodes remember identifiers for 60 seconds.

### Possible Operations
- set-value\
  Sets a new value for a key.\
  *Syntax*: ```set-value <key>:<value>```\
  *Response*: ```OK``` if the value is set, ```ERROR``` if there is no node in the database holding the specified key

- get-value\
  Searches for a record with the specified key.\
  *Syntax*: ```get-value <key>```\
  *Response*: ```<key>:<value>``` if the key is found, ```ERROR``` if there is no node in the database holding the specified key

- find-key\
  Searches for the node that holds the specified key.\
  *Syntax*: ```find-key <key>```\
  *Response*: ```<IP address>:<port>``` if the node holding the record with the specified key exists, ```ERROR``` if there is no node in the database holding the specified key

- get-max\
  Searches for the record with the highest value.\
  *Syntax*: ```get-max```\
  *Response*: ```<key>:<value>```

- get-min\
  Searches for the record with the lowest value.\
  *Syntax*: ```get-min```\
  *Response*: ```<key>:<value>```

- new-record\
  Sets a new record held by the node performing this operation.\
  *Syntax*: ```new-record <key>:<value>```\
  *Response*: ```OK```

- terminate\
  Shuts down the node performing this operation, disconnecting it from the network beforehand.\
  *Syntax*: ```terminate```\
  *Response*: ```OK```

#### Special Operations
Special operations are used by nodes to organize the database structure. **They should not be used by the client.**
- handshake\
  Informs a node that another node wants to connect to it.\
  *Syntax*: ```handshake <sender IP address>:<sender port>```\
  *Response*: **None**

- bye\
  Informs a node that another node wants to disconnect from it.\
  *Syntax*: ```bye <sender IP address>:<sender port>```\
  *Response*: **None**

## Multithreading
Node processes have a dynamic thread pool that adjusts to the load on the given node, allowing a single node to process many operations simultaneously.

## Compilation and Execution
### Compilation
- Manual
```cmd
:: In the main project folder
javac -d out src/*.java
```
- Using the compilation script
```cmd
:: In the main project folder
.\compile.bat
```
### Running a database node
> To run a node, you must be in the folder with the compiled `.class` files or set the appropriate `CLASSPATH`.
```
java DatabaseNode -tcpport <port> -record <key>:<value> [<neighbour ip>:<neighbour port> ...]
```
*\<port\>* - the port on which the running node will listen\
*\<key\>:\<value\>* - key and the value stored under it\
*[\<neighbour ip\>:\<neighbour port\> ...]* - pairs of IP addresses and ports of neighboring nodes

### Running the client
> To run the client application, you must be in the folder with the compiled `.class` files or set the appropriate `CLASSPATH`.
```
java Client -gateway <ip>:<port> -operation <operation> [<args>]
```
*ip:port* - the IP address and port of the node to which the client will delegate the specified operation\
*operation* - operation to be performed on the database\
*args* - optional operation arguments

### Running example tests
The tests directory contains three scripts with example tests.
```cmd
:: In the tests folder
.\<test name>.bat
```

***Notes:***
- Test scripts should be run after compiling the project
- Test scripts use the included database client (```Client.java```)

## Implemented Features
- Ability to organize a network forming the database (incremental node connection and termination)
- All the operations listed above
- Cycle detection
- Multithreading (ability to process multiple operations simultaneously, allowing multiple clients to use the database concurrently)
  So, presumably, everything mentioned in the specification.