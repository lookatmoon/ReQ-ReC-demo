#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <cstring>
#include <float.h>
#include <iostream>
#include <armadillo>

using namespace arma;
using namespace std;

static const int MAX_BUF = 65536;
static const int NUM_ITER = 1;
static int NUM_CAT;
static double ALPHA;

static void load_labels(mat& A, char* filename);

int main(int argc, char** argv)
{
    if (argc != 6) {
        cout << "LGC. propagate labels with normalized Laplacian. Usage:" << endl;
        cout << "\tlaplacian num_cat alpha_LG_tradeoff labels output_prediction" << endl;
        cout << "\toutput format (each line): predicted_cat <confidence_per_cat>+" << endl;
        exit(1);
    }

    int i, j;
    uword guessed_class;
    rowvec r;
    mat sim, label, guess;

    sim.load(argv[1], arma_ascii);
    NUM_CAT = atoi(argv[2]);
    sscanf(argv[3], "%lf", &ALPHA);

    label = zeros(sim.n_rows, NUM_CAT);
    load_labels(label, argv[4]);
    guess = label; // 'guess' is an independent copy of 'label'

    //propagation
    for (i = 0; i < NUM_ITER; i++) {
        guess = ALPHA * sim * guess + (1 - ALPHA) * label;
    }

    //output
    FILE* outfile;
    outfile = fopen(argv[5], "w");
    for (i = 0; i < sim.n_rows; i++) {
        r = guess.row(i);
        r.max(guessed_class);
        fprintf(outfile, "%u\t", guessed_class + 1);
        for (j = 0; j < NUM_CAT; j++) {
            fprintf(outfile, "%lf\t", guess(i, j));
        }
        fprintf(outfile, "\n");
    }
    fclose(outfile);
}

// label file format:
// <line> := <line number>\t<class label>
// <line number> starts from 1, <class label> starts from 1
static void load_labels(mat& A, char* filename)
{
    FILE *infile;
    int line_num, label;
    char *buf, *res;

    buf = new char[MAX_BUF];
    infile = fopen(filename, "r");
    fprintf(stderr, "Start loading labels...\n");
    res = fgets(buf, MAX_BUF, infile);
    while (!feof(infile)) {    
            sscanf(res, "%d\t%d\n", &line_num, &label);
            A(line_num - 1, label - 1) = 1;
            res = fgets(buf, MAX_BUF, infile);
    }
    fclose(infile);
    delete [] buf;
    fprintf(stderr, "Finish loading labels!\n");
}