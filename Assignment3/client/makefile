CFLAGS:=-c -Wall -Weffc++ -g
LDFLAGS:=-lboost_system -lboost_locale -lboost_thread

all: bin/encoder.o gameClient
	g++ -o bin/echoExample bin/encoder.o bin/connectionHandler.o bin/gameClient.o $(LDFLAGS) 

gameClient: bin/connectionHandler.o bin/gameClient.o
	
bin/connectionHandler.o: src/gameClient/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/gameClient/connectionHandler.cpp

bin/gameClient.o: src/gameClient/gameClient.cpp
	g++ $(CFLAGS) -o bin/gameClient.o src/gameClient/gameClient.cpp
	
bin/encoder.o: src/encoder/encoder.cpp
	g++ $(CFLAGS) -o bin/encoder.o src/encoder/encoder.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
