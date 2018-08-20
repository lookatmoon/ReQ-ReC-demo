package index;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

public class VecTextField extends Field {

	/* Indexed, tokenized, not stored. */
	public static final FieldType TYPE_NOT_TOKENIZED = new FieldType();

	/* Indexed, tokenized, stored. */
	//difference between stored and indexed:
	//stored: when you retrieved one doc, when one field is stored, you can use
	//doc.get("field_name") to get the value of the field of that doc
	//indexed: can use search of that field to retrieve one doc
	public static final FieldType TYPE_STORED = new FieldType();

	static {
		TYPE_NOT_TOKENIZED.setIndexed(true);
		TYPE_NOT_TOKENIZED.setTokenized(false);
		TYPE_NOT_TOKENIZED.setStoreTermVectors(false);
		TYPE_NOT_TOKENIZED.setStoreTermVectorPositions(false);
		TYPE_NOT_TOKENIZED.freeze();

		TYPE_STORED.setIndexed(true);
		TYPE_STORED.setTokenized(true);
		TYPE_STORED.setStored(true);
		TYPE_STORED.setStoreTermVectors(true);
		TYPE_STORED.setStoreTermVectorPositions(true);
		TYPE_STORED.freeze();
	}

	/** Creates a new TextField with Reader value. */
	public VecTextField(String name, Reader reader, Store store) {
		super(name, reader, store == Store.YES ? TYPE_STORED : TYPE_NOT_TOKENIZED);
	}

	/** Creates a new TextField with String value. */
	public VecTextField(String name, String value, Store store) {
		super(name, value, store == Store.YES ? TYPE_STORED : TYPE_NOT_TOKENIZED);
	}
	
	
	/** Creates a new un-stored TextField with TokenStream value. */
	public VecTextField(String name, TokenStream stream) {
		super(name, stream, TYPE_NOT_TOKENIZED);
	}
}