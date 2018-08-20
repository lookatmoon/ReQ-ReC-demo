README.txt

Toy dataset:
	The deliverable is a web application, so one can follow INSTALL.txt and set up the web service. A tiny dataset (20 tweets) is already shipped as part of the deliverable, located under <base_dir>/fulltext/data/; one can try the system with it without preparing one's own data. After setting up the system, one can simply type in "obama", and there should be a few tweets shown up in the first round.

Directory structure:
	src/					
		conf/				
		java/				- the web application is a Java servlet
			feature/		- feature extraction for text classification
			fileFetch/		- file manipulation utilities
			global/			- global variables, such as file paths
			process/		- control logic of double-loop
			svm/			- wrappers of SVM classifier package (LibLinear)
	resources/				
		clustering/			- document clustering after each new retrieval
		indexing/			- Lucene full-text indexing utilities
		lexicon/			- sentiment analysis lexicon (*not used in this project)
		liblinear/			- LibLinear binary classification package
		lucene/				- Lucene jar libraries 
		search/				- index search module, return ranked documents given a query
	web/
		index.jsp			- main page of the web application
		allTweetsInfo.jsp	- client/browswer-end parsing functions for displaying information
		files/				
			data/			- files holding documents received from index search
			results/		- files holding intermediate results of active learning
	fulltext/
		data/				- folder for raw documents; examples provided
		index/				- folder for Lucene index files; examples provided
		README_indexing.txt	- brief instructions for formatting documents and building index
	dist/					- after NetBeans compilation, DoubleLoop.war can be found here
	build/ build.xml		- NetBeans IDE build files
	nbproject/				- NetBeans IDE project files
	README.txt				- this file
	INSTALL.txt				- step-by-step instructions to install the Double-Loop system.
