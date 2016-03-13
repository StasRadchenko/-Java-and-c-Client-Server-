package main.java.bgu.spl.tokenizer;

import java.nio.charset.Charset;

/**our tokenizer factory creates new FixedSeparatorMessageTokenizer while the separator is "/n"
 * and the format is UTF-8
 * 
 * 
 * @author radchens
 *
 */
public class gameTokenizerFactory implements TokenizerFactory<StringMessage> {

	@Override
	public FixedSeparatorMessageTokenizer create() {
		return new FixedSeparatorMessageTokenizer("/n",Charset.forName("UTF-8"));
	}
}