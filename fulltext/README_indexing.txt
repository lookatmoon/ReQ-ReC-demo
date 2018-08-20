The JAR balls work only in Linux/Unix environment.

Format of files to be indexed:
docID <tab> text

Example files can be found in data/



How to index:
cd fulltext/
java -jar ../resources/indexing/index.jar <data_dir> <index_dir>

e.g.
java -jar ../resources/indexing/index.jar data/ index/

After indexing, the Lucene index files are stored in the directory <index_dir>. <index_dir> will be automatically created if does not exist; however it is recommended that it is an empty directory.