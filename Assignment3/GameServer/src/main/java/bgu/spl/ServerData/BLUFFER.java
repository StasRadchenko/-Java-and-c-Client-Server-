package main.java.bgu.spl.ServerData;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import main.java.bgu.spl.ServerData.jsonInput.Questions;
import main.java.bgu.spl.protocol.ProtocolCallback;
import main.java.bgu.spl.tokenizer.StringMessage;

/**
 * Object that holds all the logic of the game,it holds the round
 * iteration(iter),number of players according to number of clients in the
 * room,current question out of json questions,count is the number of players
 * that sent the answer to the current question,boolean flag that tells us if in
 * the current moment we are waiting for text respond,to exclude the option for
 * players to send other commands(excluding the MSG).In addition we hold string
 * of the name of the game-"BLUFFER".Room that for which we created the
 * game.Arraylist of answers-all the false answers that the players gave+the
 * real one.ArrayList of game questions-the 3 question that we recived from the
 * json after starting the game.Map of players and for each player array of
 * players score per round.Map of all players and the false answer they gave.
 * 
 * @author BabaGanush
 *
 */
public class BLUFFER implements TBGP {
	private int iter;
	private int count;
	private int numOfPlayers;
	private int current;
	private boolean waitingForChoices;
	private final String gameName;
	private Room room;
	private ArrayList<String> answers;
	private ArrayList<Questions> _gameQuestions;
	private ConcurrentHashMap<Player, int[]> scores;
	private ConcurrentHashMap<Player, String> falseAnswers;
	private ConcurrentHashMap<ProtocolCallback<StringMessage>, Player> _players;

	public BLUFFER() {
		gameName = "BLUFFER";
		falseAnswers = new ConcurrentHashMap<Player, String>();
		scores = new ConcurrentHashMap<Player, int[]>();
		answers = null;
		current = 0;
		count = 0;
		waitingForChoices = false;
	}
	/**
	 * called from the room to initialize the game before playing 
	 */
	public void initialize(Room r) {
		iter = 0;
		this.numOfPlayers = r.numOfPlayers();
		this._players = r.getPlayers();
		this.room = r;
		load();
		_players.forEach((v, k) -> {
			scores.put(k, new int[3]);
		});
	}

	/**
	 * @return - this game name
	 */
	public String getName() {
		return gameName;
	}

	/**
	 * loads the questions from the json file
	 */
	public void load() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("Bluffer.json"));
		} catch (FileNotFoundException e) {
			System.out.println("There is no file in the given location");
			e.printStackTrace();
		}
		Gson gson = new Gson();
		jsonInput readJson = gson.fromJson(br, jsonInput.class);

		// initializing Questions data-base
		//Questions[] gameQuestions = new Questions[readJson.questions.length];
	//	for (int i = 0; i < gameQuestions.length; i++) {
	//		gameQuestions[i] = readJson.questions[i];
		//}
		_gameQuestions = new ArrayList<Questions>(Arrays.asList(readJson.questions));
	}

	/**
	 * sends a question to the players
	 */
	public void ASKTXT() {
		current = (int) (Math.random() * (_gameQuestions.size() - 1));
		String ask = _gameQuestions.get(current).questionText;
		_players.forEach((k, v) -> {
			gameData.getInstance().ASKTXT(ask, k);
		});
	}

	/**
	 * sends all the possible answers to the players
	 */
	public void ASKCHOICES() {
		waitingForChoices = true;
		String ret = "";
		answers = new ArrayList<String>();
		falseAnswers.forEach((v, k) -> {
			answers.add(k);
		});
		answers.add(_gameQuestions.get(current).realAnswer);
		long seed = System.nanoTime();
		Collections.shuffle(answers, new Random(seed));
		for (int i = 0; i < answers.size(); i++)
			ret += i + "." + answers.get(i) + " ";
		for (Entry<ProtocolCallback<StringMessage>, Player> entry : _players.entrySet()) {
			ProtocolCallback<StringMessage> key = entry.getKey();
			gameData.getInstance().ASKCHOICES(ret, key);
		}
	}
	
	/**
	 * gets the SELECTRESP from each player
	 */
	public void SELECTRESP(int msg, Player player) {
		if (answers != null && msg < answers.size()) {
			gameData.getInstance().SYSMSG("SELECTRESP ACCEPTED", player.getCallback());
			String realAnswer = _gameQuestions.get(current).realAnswer;
			count++;
			if (answers.get(msg) == realAnswer) {
				scores.get(player)[iter - 1] = 10;
				player.setRoundWinner(true);
			} else {
				scores.get(player)[iter - 1] += 0;
				player.setRoundWinner(false);
				String ans = answers.get(msg);
				falseAnswers.forEach((k, v) -> {
					if (v.equals(ans)) {
						scores.get(k)[iter - 1] += 5;
					}
				});
			}
			if (count == numOfPlayers && iter <= 3) {
				answers = null;
				falseAnswers.clear();
				waitingForChoices = false;
				_gameQuestions.remove(current);

				for (Entry<ProtocolCallback<StringMessage>, Player> entry : _players.entrySet()) {
					ProtocolCallback<StringMessage> key = entry.getKey();
					Player value = entry.getValue();
					gameData.getInstance().GAMEMSG("The correct answer is: " + realAnswer, key);
					if (value.roundWinner())
						gameData.getInstance().GAMEMSG("correct! +" + scores.get(value)[iter - 1], key);
					else
						gameData.getInstance().GAMEMSG("wrong! +" + scores.get(value)[iter - 1], key);
				}
				if (iter == 3) {
					String ret = "Summary: ";
					for (Entry<Player, int[]> entry : scores.entrySet()) {
						int sum = 0;
						Player key = entry.getKey();
						int[] value = entry.getValue();

						for (int i = 0; i < value.length; i++)
							sum += value[i];

						ret += key.getName() + ": " + sum + "pts ";
					}
					for (Entry<ProtocolCallback<StringMessage>, Player> entry : _players.entrySet()) {
						ProtocolCallback<StringMessage> key = entry.getKey();
						gameData.getInstance().GAMEMSG(ret, key);

					}
					room.done();
				} else
					play();
			}
		} else
			gameData.getInstance().SYSMSG("SELECTRESP REJECTED", player.getCallback());
	}
	
	/**
	 * gets the false answers from the players
	 */
	public void TXTRESP(String msg, Player player) {
		if (!falseAnswers.containsKey(player) && !waitingForChoices) {
			gameData.getInstance().SYSMSG("TXTRESP ACCEPTED", player.getCallback());
			falseAnswers.put(player, msg);
			if (numOfPlayers == falseAnswers.size())
				ASKCHOICES();
		} else
			gameData.getInstance().SYSMSG("TXTRESP REJECTED", player.getCallback());
	}

	/**
	 * Plays one round
	 */
	public void play() {
		count = 0;
		iter++;
		ASKTXT();
	}

}
