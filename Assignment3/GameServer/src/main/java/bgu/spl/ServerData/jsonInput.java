package main.java.bgu.spl.ServerData;

import com.google.gson.annotations.SerializedName;

/**
 * @author EvilBamba
 * a static class used to read from json file in the given format
 */
public class jsonInput {
	
	@SerializedName("questions")
		public Questions [] questions;
	
	public class Questions{
		
		@SerializedName("questionText")
		public String questionText;
		
		@SerializedName("realAnswer")
		public String realAnswer;
	}
}
