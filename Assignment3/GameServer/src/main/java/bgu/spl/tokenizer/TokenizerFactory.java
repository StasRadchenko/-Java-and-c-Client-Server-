package main.java.bgu.spl.tokenizer;

public interface TokenizerFactory<T> {
   MessageTokenizer<T> create();
}
