#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <cstring>
#include <float.h>
#include <iostream>
#include <armadillo>

using namespace arma;
using namespace std;

static const int MAX_BUF = 1048576;
static int N_DATA, M_VOCA;
static double SIGMA;

static void load_sparse_mat(mat& A, char* filename);

int main(int argc, char** argv)
{
    if (argc != 6) {
        cout << "Compute normalized Laplacian matrix (cosine distance; Gaussian kernel). Usage:" << endl;
        cout << "\tsparse_mat N_data M_feat sigma_width output_graph" << endl;
        cout << "\toutput format: {N_data x N_data} normalized Laplacian matrix" << endl;
        exit(1);
    }

    N_DATA = atoi(argv[2]);
    M_VOCA = atoi(argv[3]);
    sscanf(argv[4], "%lf", &SIGMA);

    mat data;

    data = zeros(N_DATA, M_VOCA);
    load_sparse_mat(data, argv[1]);

    // construct the similarity matrix

    // 1) pairwise cosine similarity
    mat norm_data, sim;
    double tmp_val;

    norm_data = sqrt(sum(data % data, 1));
    sim = data * data.st();
    sim.each_col() /= norm_data;
    sim.each_row() /= norm_data.st();

    // 2) Gaussian kernel
    tmp_val = 2*SIGMA*SIGMA; // the width
    sim = 1 - sim;
    sim = sim % sim;
    sim /= tmp_val;
    sim = exp( -sim );

    // 3) normalized Laplacian
    norm_data = sqrt(sum(sim, 1));
    sim.each_col() /= norm_data;
    sim.each_row() /= norm_data.st();
    
    // save the matrix
    sim.save(argv[5], arma_ascii);

    return 0;
}

// load sparse matrix into A, assuming that A is already initialized
// as a zero matrix with correct dimensionality (rows, columns)
static void load_sparse_mat(mat& A, char* filename)
{
    FILE *infile;
    int i, j, feat_id;
    double feat_val;
    char *buf, *pch, *res;

    buf = new char[MAX_BUF];
    infile = fopen(filename, "r");
    fprintf(stderr, "Start loading data...\n");
    for (i = 0; i < N_DATA; i++) {
            fprintf(stderr, "%d\r", i);
            res = fgets(buf, MAX_BUF, infile);
            //res = strchr(buf, '\t');
            //res[0] = 0;
            //targets[i] = string(buf);
            //res += 1;
            pch = strtok(res, " \n");
            for (j = 0; j < M_VOCA && pch != NULL; j++) {
                sscanf(pch, "%d:%lf", &feat_id, &feat_val);
                //A(i, feat_id - 1) = feat_val;
                //cout << "i = " << i << " feat_id = " << feat_id << " feat_val = " << feat_val << endl;
                A(i, feat_id - 1) = feat_val;
                //A(i, feat_id - 1) = 1;
                //A(i, feat_id) = feat_val;
                pch = strtok(NULL, " \n");
            }
    }
    fclose(infile);
    delete [] buf;
    fprintf(stderr, "Finish loading data!\n");
}
