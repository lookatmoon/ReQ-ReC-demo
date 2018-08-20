# compiling C++ code
%.o: %.cpp
	$(CXX) $(CXXFLAGS) -o $@ -c $<
%: %.o
	$(CXX) $(CXXFLAGS) -o $@ $< $(CPPLDFLAGS)

arma_prefix = /storage2/foreseer/users/raywang/mlpack/armadillo-3.800.2
INC = -I$(arma_prefix)/include -I$(lbfgs_prefix)/include -I./
#antlr is already set up properly for include path

LIBPATH = -L$(arma_prefix) -L$(lbfgs_prefix)/lib
LIB = -larmadillo -lm -lpthread
CXX = g++
CFLAGS = 
CXXFLAGS  = -Wall -O3 -g $(INC)

CPPLDFLAGS  =  $(LIBPATH) $(LIB) 
## specify your object files here
OBJS = gen_graph.o
## specify your program here
PROG = gen_graph

all: $(PROG)

$(PROG): $(OBJS)
	$(CXX) $(CXXFLAGS) -o $@ $^ $(CPPLDFLAGS)

clean:
	rm $(PROG) $(OBJS)
