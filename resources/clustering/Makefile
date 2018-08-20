# compiling C++ code
%.o: %.cpp
	$(CXX) $(CXXFLAGS) -o $@ -c $<
%: %.o
	$(CXX) $(CXXFLAGS) -o $@ $< $(CPPLDFLAGS)

arma_prefix = arma
INC = -I$(arma_prefix)/include -I$(lbfgs_prefix)/include -I./
#antlr is already set up properly for include path

LIBPATH = -L$(arma_prefix) 
LIB = -larmadillo -lm -lpthread
CXX = g++
CFLAGS = 
CXXFLAGS  = -Wall -O3 -g $(INC)

CPPLDFLAGS  =  $(LIBPATH) $(LIB) 
## specify your object files here
OBJS = clustering.o
## specify your program here
PROG = clustering

all: $(PROG)

$(PROG): $(OBJS)
	$(CXX) $(CXXFLAGS) -o $@ $^ $(CPPLDFLAGS)

clean:
	rm $(PROG) $(OBJS)
