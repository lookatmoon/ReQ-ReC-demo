// Includes all relevant components of mlpack.
#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <cstring>
#include <float.h>
#include <ctime>
#include <iostream>
#include <armadillo>

using namespace arma;
using namespace std;

static const int MAX_BUF = 65535;
static const double TOLERANCE_MOU = 1.0;
static const double TOLERANCE_KMN = 1.0;
static const double MINIMUM_VAL = DBL_MIN * 1024;
static const double BACKGROUND_LAMBDA = 0.995;
static int N_DATA, M_VOCA, K_COMP;
static char *FN_MODEL, *FN_RESULT;

static void load_sparse_mat(mat& A, char* filename);
static void get_max(mat& A, mat& val_vec, umat& pos_vec, int dim);
static void get_min(mat& A, mat& val_vec, umat& pos_vec, int dim);

static void mix_multinomial(mat& data);
static void k_means(mat& data);
static void k_medoids(mat& data);
static void preprocess(mat& data);

int main(int argc, char** argv)
{
    if (argc < 2) {
        cout << "mx_uni sparse_mat N_data M_voca K_cluster model result, OR" << endl;
        cout << "k_mean sparse_mat N_data M_voca K_cluster model result, OR" << endl;
        cout << "k_medo sparse_mat N_data M_voca K_cluster model result" << endl;
        cout << "model format: cluster_id centroid centroid_conf cluster_size cluster_model" << endl;
        exit(1);
    } else if (strcmp(argv[1], "mx_uni") == 0) {
        if (argc != 8) {
            cout << "mx_uni sparse_mat N_data M_voca K_cluster model result" << endl;
            exit(1);
        }
    } else if (strcmp(argv[1], "k_mean") == 0) {
        if (argc != 8) {
            cout << "k_mean sparse_mat N_data M_voca K_cluster model result" << endl;
            exit(1);
        }
    } else if (strcmp(argv[1], "k_medo") == 0) {
        if (argc != 8) {
            cout << "k_medo sparse_mat N_data M_voca K_cluster model result" << endl;
            exit(1);
        }
    } else {
        cout << "mx_uni sparse_mat N_data M_voca K_cluster model result, OR" << endl;
        cout << "k_mean sparse_mat N_data M_voca K_cluster model result, OR" << endl;
        cout << "k_medo sparse_mat N_data M_voca K_cluster model result" << endl;
        exit(1);
    }
    N_DATA = atoi(argv[3]);
    M_VOCA = atoi(argv[4]);
    K_COMP = atoi(argv[5]);
    FN_MODEL  = argv[6];
    FN_RESULT = argv[7];

    // N_DATA = 10000;
    // M_VOCA = 94247;
    // K_COMP = 5;
    
    // N_DATA = 4;
    // M_VOCA = 3;
    // K_COMP = 2;
    
    // N_DATA = 935;
    // M_VOCA = 4100;
    // K_COMP = 2;

    mat data;

    srand (time(NULL));

    data = zeros(N_DATA, M_VOCA);
    load_sparse_mat(data, argv[2]);
    // load_sparse_mat(data, "vi ");
    // load_sparse_mat(data, "/storage2/foreseer/users/raywang/si650/political/anno_data/norm.vec");
    // load_sparse_mat(data, "data.svm");
    // cout << "sparse mat loaded: " << endl << data << endl;

    // there should be some feature selection and data cleaning
    // preprocess(data);

    if (strcmp(argv[1], "mx_uni") == 0) {
        mix_multinomial(data);
    } else if (strcmp(argv[1], "k_mean") == 0) {
        k_means(data);
    } else if (strcmp(argv[1], "k_medo") == 0) {
    	k_medoids(data);
    }
    
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

static void get_max(mat& A, mat& val_vec, umat& pos_vec, int dim)
{   
    int i, j;
    if (dim == 0) {
        val_vec = zeros(1, A.n_cols);
        pos_vec = zeros<umat>(1, A.n_cols);
        for (j = 0; j < A.n_cols; j++) {
            for (i = 0; i < A.n_rows; i++) {
                if (A(i, j) > val_vec(0, j)) {
                    val_vec(0, j) = A(i, j);
                    pos_vec(0, j) = i;
                }
            }
        }
    } else if (dim == 1) {
        val_vec = zeros(A.n_rows, 1);
        pos_vec = zeros<umat>(A.n_rows, 1);
        for (i = 0; i < A.n_rows; i++) {
            for (j = 0; j < A.n_cols; j++) {
                if (A(i, j) > val_vec(i, 0)) {
                    val_vec(i, 0) = A(i, j);
                    pos_vec(i, 0) = j;
                }
            }
        }
    } else {
        cout << "ERROR in get_max(): invalid \"dim\". " << endl;
    }
}

static void get_min(mat& A, mat& val_vec, umat& pos_vec, int dim)
{   
    int i, j;
    if (dim == 0) {
        val_vec = ones(1, A.n_cols);
        pos_vec = zeros<umat>(1, A.n_cols);
        for (j = 0; j < A.n_cols; j++) {
            for (i = 0; i < A.n_rows; i++) {
                if (A(i, j) < val_vec(0, j)) {
                    val_vec(0, j) = A(i, j);
                    pos_vec(0, j) = i;
                }
            }
        }
    } else if (dim == 1) {
        val_vec = ones(A.n_rows, 1);
        pos_vec = zeros<umat>(A.n_rows, 1);
        for (i = 0; i < A.n_rows; i++) {
            for (j = 0; j < A.n_cols; j++) {
                if (A(i, j) < val_vec(i, 0)) {
                    val_vec(i, 0) = A(i, j);
                    pos_vec(i, 0) = j;
                }
            }
        }
    } else {
        cout << "ERROR in get_min(): invalid \"dim\". " << endl;
    }
}

static void mix_multinomial(mat& data)
{
    int i, j, k;
    double loglike, loglike_old, tmp;
    rowvec z;
    vec norm, norm2, resp_b, background_lm;

    // normalize the data by idf
    // uvec nz;
    // z = zeros(1, M_VOCA);
    // for (i = 0; i < M_VOCA; i++) {
    //     nz = find(data.col(i));
    //     z(0, i) = nz.n_rows;
    // }
    // data.each_row() /= z;

    // z = max(data);
    // z = sum(data);
    // data.each_row() /= z;
    // cout << "sparse mat loaded: " << endl << data << endl;    

    // 1. initialize internal matrices
    mat mixture, multinomial, resp;

    // mixture component, sum up to one
    mixture = randu(1, K_COMP);
    mixture = mixture / accu(mixture);

    // multinomial distributions, columns sum up to one
    multinomial = randu(M_VOCA, K_COMP) + 0.01;

    // multinomial = ones(M_VOCA, K_COMP) / ((double)M_VOCA);
    //multinomial += randu(M_VOCA, K_COMP) * 0.1 / ((double)M_VOCA);

    // multinomial = zeros(M_VOCA, K_COMP);
    // for (i = 0; i < N_DATA; i++) {
    //     k = i % K_COMP;
    //     multinomial.col(k) += data.row(i).st();
    // }
    // multinomial += 1;
    z = sum(multinomial);
    multinomial.each_row() /= z;

    // there is a background LM
    background_lm = (sum(data)).st() + 1;
    background_lm = background_lm / accu(background_lm);


    //cout << "k-mixture: " << endl << mixture << endl;
    //cout << "k-multinomial: " << endl << multinomial << endl;
    
    // responsibility matrix
    //resp = zeros(N_DATA, K_COMP);

    // e-step
    // resp = data * log(multinomial);
    // resp = resp - (resp.min() + resp.max()) / 2;
    // resp = exp(resp);
    // for (i = 0; i < N_DATA; i++) {
    //     resp.row(i) = resp.row(i) % mixture;
    //     resp.row(i) /= sum(resp.row(i));
    // }
    // cout << "n-responsibility: " << endl << resp << endl;

    //cout << "sum of resp: " << sum(multinomial, 0) << endl;

    // 2. start EM iterations
    j = 0;
    loglike = 0.0;
    loglike_old = 0.0;
    while (true) {
        // e-step
        resp = data * log(multinomial);
        resp_b = data * log(background_lm);
        // cout << "data.row(0)" << data.row(0) << endl;
        // cout << "multinomial.col(0)" << multinomial.col(0) << endl;
        // cout << "sum of log(multinomial): " << accu(log(multinomial)) << endl;
        // cout << "responsibility: " << endl << resp << endl;
        //resp = resp - (resp.min() + resp.max()) / 2; //avoid underflow
        //resp = resp - resp.max(); //avoid underflow
        resp.each_row() += log(mixture);
        resp = exp(resp);
        resp += MINIMUM_VAL;
        norm = sum(resp, 1);
        resp.each_col() /= norm;
        // cout << "sum of resp: " << accu(resp) << endl;

        // responsibility of the background
        resp_b = exp(resp_b);
        resp_b += MINIMUM_VAL;
        resp_b *= BACKGROUND_LAMBDA;
        norm2 = (1 - BACKGROUND_LAMBDA)*norm + resp_b;
        resp_b /= norm2;
        // cout << "sum of resp_b: " << accu(resp_b) << endl;

        //cout << "norm2: " << endl << norm2 << endl;
        loglike = accu(log(norm2));

        // for (i = 0; i < N_DATA; i++) {
        //     //resp.row(i) = resp.row(i) % mixture;
        //     // cout << "Row" << i << ": " << resp.row(i);
        //     tmp = accu(resp.row(i));
        //     cout << i << " row sum: " << tmp << endl;
        //     resp.row(i) /= tmp;
        //     cout << i << " row sum after norm: " << accu(resp.row(i)) << endl;
        //     loglike += log(tmp);
        // }
        // cout << "rows of resp: " << sum(resp, 1) << endl;
        // cout << "sum of rows of resp: " << sum(sum(resp, 1)) << endl;
        // cout << "sum of resp: " << accu(resp) << endl;


        //m-step
        mixture = sum(resp);
        // mixture += 1e-3; // smoothing the mixture
        
        mixture = mixture / accu(mixture);

        // cout << "k-mixture: " << endl << mixture << endl;

        // cout << "sum of mixture: " << accu(mixture) << endl;

        multinomial = data.st() * resp;
        // cout << "count multinomial:" << endl << multinomial << endl;
        multinomial += 1; // smoothing
        z = sum(multinomial);
        multinomial.each_row() /= z;

        // cout << "sum of multinomial: " << accu(multinomial) << endl;

        // cout << "Iteration " << j << endl;
        // cout << "n-responsibility: " << endl << resp << endl;
        // cout << "k-mixture: " << endl << mixture << endl;
        // cout << "k-multinomial: " << endl << multinomial << endl;
        cout << j << " log-likelihood: " << loglike << endl;

        // check if converges, then break
        if (abs(loglike_old - loglike) < TOLERANCE_MOU && j > 0) {
            mat val_vec;
            umat pos_vec;

            // output model
            get_max(resp, val_vec, pos_vec, 0);
            FILE *out_f;
            out_f = fopen(FN_MODEL, "w");
            for (i = 0; i < K_COMP; i++) {
                fprintf(out_f, "%d\t%d\t%lf\t", i+1, pos_vec(0, i)+1, val_vec(0, i));
                fprintf(out_f, "%lf\t", mixture(0, i));
                for (k = 0; k < M_VOCA; k++) {
                    fprintf(out_f, "%d:%lf ", k+1, multinomial(k, i));
                }
                fprintf(out_f, "\n");
            }
            fclose(out_f);

            // output each file
            get_max(resp, val_vec, pos_vec, 1);
            out_f = fopen(FN_RESULT, "w");
            for (i = 0; i < N_DATA; i++) {
                fprintf(out_f, "%d\t%lf\n", pos_vec(i, 0)+1, val_vec(i, 0));
            }
            fclose(out_f);

            break;
        } else {
            loglike_old = loglike;
            loglike = 0;
        }
        j++;
    }
}

static void k_means(mat& data)
{
    int i, j, k, ind, cluster_ind;
    double cost, cost_old;
    mat center, sim, norm_data, norm_center, resp;
    umat resp_cluster;
    uvec cnt_vec;

    cout << "initializing memory" << endl;    
    center = zeros(K_COMP, M_VOCA);
    sim = zeros(N_DATA, K_COMP);
    norm_data = zeros(N_DATA, 1);
    norm_center = zeros(K_COMP, 1);

    norm_data = sqrt(sum(data % data, 1));
    data.each_col() /= norm_data;

    // cout << "norm_data: " << endl << norm_data << endl;

    // initialize center
    // cout << "initializing cluster centers" << endl;
    umat shuf = zeros<umat>(N_DATA, 1);
    for (i = 0; i < N_DATA; i++) {
        shuf(i, 0) = i;
    }
    shuf = shuffle(shuf, 0);
    for (i = 0; i < K_COMP; i++) {
        center.row(i) = data.row(shuf(i, 0));
    }
    center += 0.1*randu(K_COMP, M_VOCA);
    norm_center = sqrt(sum(center % center, 1));
    center.each_col() /= norm_center;

    // cout << "centers: " << endl << center << endl;
    j = 0;
    cost = 0.0;
    cost_old = 0.0;
    while (true) {

        // calculate similarity
        // m-step
        // cout << (j+1) << " m-step" << endl;
        // cout << (j+1) << " dot-product" << endl;
        sim = data * center.st();
        // cout << "dot prod sim: " << endl << sim << endl;
        // cout << (j+1) << " normalization" << endl;
        // norm_center = sqrt(sum(center % center, 1));
        // cout << "norm_center: " << endl << norm_center << endl;
        // sim.each_col() /= norm_data;
        // sim.each_row() /= norm_center.st();
        // cout << "cosine sim: " << endl << sim << endl;

        // e-step
        // cout << (j+1) << " e-step" << endl;
        
        // cout << (j+1) << " group into clusters" << endl;
        get_max(sim, resp, resp_cluster, 1);
        // cout << (j+1) << " averaging cluster centers" << endl;
        
        // check if there are empty clusters
        bool has_empty = false;
        for (i = 0; i < K_COMP; i++) {
            cnt_vec = find(resp_cluster == i);
            if (cnt_vec.n_rows == 0) {
                has_empty = true;
                k = (int)(((double)rand() / (double)RAND_MAX) * N_DATA);
                center.row(i) = 0.1*randu(1, M_VOCA) + data.row(k);
                center.row(i) /= sqrt(accu(center.row(i) % center.row(i)));
            }
        }
        if (has_empty) {
            cout << "emtpy cluster. restart." << endl;
            continue;
        }

        center.zeros();
        for (i = 0; i < K_COMP; i++) {
            printf("cluster %d\r", i);
            center.row(i) = mean(data.rows(find(resp_cluster == i)));
            center.row(i) /= sqrt(accu(center.row(i) % center.row(i)));
            cost += accu(resp.rows(find(resp_cluster == i)));
        }
        /*
        for (i = 0; i < N_DATA; i++) {
            printf("data %d\r", i);
            cluster_ind = resp_cluster(i, 0);
            center.row(cluster_ind) += data.row(i);
            cluster_size(cluster_ind, 0) += 1;
            cost += resp(i, 0);
        }
        center.each_col() /= cluster_size;
        */
        // cout << "centers: " << endl << center << endl;
        cerr << "similarity: " << cost << endl;

        if (abs(cost_old - cost) < TOLERANCE_KMN && j > 0) {
            mat val_vec;
            umat pos_vec;

            // output model
            get_max(sim, val_vec, pos_vec, 0);
            get_max(sim, resp, resp_cluster, 1);

            FILE *out_f;
            out_f = fopen(FN_MODEL, "w");
            for (i = 0; i < K_COMP; i++) {
                fprintf(out_f, "%d\t%d\t%lf\t", i+1, pos_vec(0, i)+1, val_vec(0, i));
                cnt_vec = find(resp_cluster == i);
                fprintf(out_f, "%u\t", cnt_vec.n_rows);
                for (k = 0; k < M_VOCA; k++) {
                    fprintf(out_f, "%d:%lf ", k+1, center(i, k));
                }
                fprintf(out_f, "\n");
            }
            fclose(out_f);

            // output each file
            out_f = fopen(FN_RESULT, "w");
            for (i = 0; i < N_DATA; i++) {
                fprintf(out_f, "%d\t%lf\n", resp_cluster(i, 0)+1, resp(i, 0));
            }
            fclose(out_f);

            break;
        } else {
            cost_old = cost;
            cost = 0;
        }
        j++;
    }
}

static void preprocess(mat& data)
{
    int i;
    
    rowvec tf, df, v;
    uvec nz;
    // we don't want low-TF: add the terms
    tf = sum(data);

    // we don't want low-IDF: add the docs
    df = zeros(1, M_VOCA);
    for (i = 0; i < M_VOCA; i++) {
        nz = find(data.col(i));
        df(0, i) = nz.n_rows;
    }
    v = log(tf + 1) / log(N_DATA / (df + 1));
    cout << "tf:" << endl << tf << endl;
    cout << "df:" << endl << df << endl;
    cout << "tfidf:" << endl << v.st() << endl;

}

static void k_medoids(mat& data)
{
	int i, k;
	mat resp, kern, k_dist_cols;
    umat resp_cluster;

	// precompute the pairwise distances
	vec norm = sqrt(sum(data % data, 1));
	data.each_col() /= norm;
	fprintf(stderr, "Computing cosine...\n");
	kern = data * data.st(); //cosine similarity
		
	// initialize centers: random selection
	uvec center_ind = zeros<uvec>(K_COMP);
	vec cost_per_cluster = zeros(K_COMP);
	uvec center_old;
	uvec one_clus_ind;
	uvec shuf = zeros<uvec>(N_DATA);
	mat one_clus_sim_mat;
	vec one_clus_sim_vec;
	uword one_clus_medoid;

    for (i = 0; i < N_DATA; i++) {
        shuf(i) = i;
    }
    shuf = shuffle(shuf, 0);
    for (i = 0; i < K_COMP; i++) {
        center_ind(i) = shuf(i);
    }
    //cout << "center_ind" << center_ind << endl;
    // compute the cluster assignment
    k_dist_cols = kern.cols(center_ind);
    get_max(k_dist_cols, resp, resp_cluster, 1);
    
    // compute the similarity in each cluster
    for (i = 0; i < K_COMP; i++) {
		fprintf(stderr, "cluster %d\r", i);
        cost_per_cluster(i) = accu(resp.rows(find(resp_cluster == i)));
    }
    cerr << "similarity: " << accu(cost_per_cluster) << endl;
    center_old = center_ind;
    
    while (true) {
		//search for the medoids in the entire dataset
		for (i = 0; i < K_COMP; i++) {
			fprintf(stderr, "cluster %d\r", i);
			one_clus_ind = find(resp_cluster == i);
			one_clus_sim_mat = kern.cols(one_clus_ind); //each row of the entire data is a center candidate
			one_clus_sim_vec = sum(one_clus_sim_mat, 1);
			cost_per_cluster(i) = one_clus_sim_vec.max(one_clus_medoid); // searching for the maximum similarity in class
			//center_ind(i) = one_clus_ind(one_clus_medoid);
			center_ind(i) = one_clus_medoid;
		}
		//cerr << "new medoids similarity: " << accu(cost_per_cluster) << endl;
		if ( accu(center_ind - center_old) == 0 ) { // medoids does not change anymore
			mat val_vec;
            umat pos_vec;
            uvec cnt_vec;
			// output model
            get_max(k_dist_cols, val_vec, pos_vec, 0);
            //get_max(k_dist_cols, resp, resp_cluster, 1);

            FILE *out_f;
            out_f = fopen(FN_MODEL, "w");
            for (i = 0; i < K_COMP; i++) {
                fprintf(out_f, "%d\t%d\t%lf\t", i+1, pos_vec(0, i)+1, val_vec(0, i));
                cnt_vec = find(resp_cluster == i);
                fprintf(out_f, "%u\t", cnt_vec.n_rows);
                for (k = 0; k < M_VOCA; k++) {
                    fprintf(out_f, "%d:%lf ", k+1, data(center_ind(i), k));
                }
                fprintf(out_f, "\n");
            }
            fclose(out_f);

            // output each file
            out_f = fopen(FN_RESULT, "w");
            for (i = 0; i < N_DATA; i++) {
                fprintf(out_f, "%d\t%lf\n", resp_cluster(i, 0)+1, resp(i, 0));
            }
            fclose(out_f);

            break;
		}
		center_old = center_ind;
		// compute the cluster assignment, again
		k_dist_cols = kern.cols(center_ind);
		get_max(k_dist_cols, resp, resp_cluster, 1);
		
		//cout << "resp " << resp << endl;
    	//cout << "resp_cluster " << resp_cluster << endl;
    
		for (i = 0; i < K_COMP; i++) {
			fprintf(stderr, "cluster %d\r", i);
        	cost_per_cluster(i) = accu(resp.rows(find(resp_cluster == i)));
    	}
    	cerr << "similarity: " << accu(cost_per_cluster) << endl;
	}
}