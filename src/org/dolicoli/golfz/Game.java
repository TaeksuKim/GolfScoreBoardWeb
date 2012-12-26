package org.dolicoli.golfz;

public class Game {
	private String gameId;
	private long date;

	private int holeCount;
	private int playerCount;

	private int holeFee;
	private int extraFee;
	private int rankingFee;
	private int fairwayDifficulty;
	private int greenDifficulty;
	private String fieldName;

	private int[] holeFeesPerRanking;
	private int[] rankingFeesPerRanking;

	private String[] playerNames;
	private int[] handicaps;
	private int[] extraScores;

	private int[] usedHandicaps, playerScores, playerFees;

	public Game() {
		holeFeesPerRanking = new int[6];
		rankingFeesPerRanking = new int[6];
		playerNames = new String[6];
		handicaps = new int[6];
		extraScores = new int[6];
		usedHandicaps = new int[6];
		playerScores = new int[6];
		playerFees = new int[6];
		fairwayDifficulty = 0;
		greenDifficulty = 0;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public int getHoleCount() {
		return holeCount;
	}

	public void setHoleCount(int holeCount) {
		this.holeCount = holeCount;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}

	public int getHoleFee() {
		return holeFee;
	}

	public void setHoleFee(int holeFee) {
		this.holeFee = holeFee;
	}

	public int getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(int extraFee) {
		this.extraFee = extraFee;
	}

	public int getRankingFee() {
		return rankingFee;
	}

	public void setRankingFee(int rankingFee) {
		this.rankingFee = rankingFee;
	}

	public int getFairwayDifficulty() {
		return fairwayDifficulty;
	}

	public void setFairwayDifficulty(int fairwayDifficulty) {
		this.fairwayDifficulty = fairwayDifficulty;
	}

	public int getGreenDifficulty() {
		return greenDifficulty;
	}

	public void setGreenDifficulty(int greenDifficulty) {
		this.greenDifficulty = greenDifficulty;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public int getHoleFeePerRanking(int ranking) {
		return holeFeesPerRanking[ranking - 1];
	}

	public void setHoleFeePerRanking(int ranking, int fee) {
		this.holeFeesPerRanking[ranking - 1] = fee;
	}

	public int getRankingFeePerRanking(int ranking) {
		return rankingFeesPerRanking[ranking - 1];
	}

	public void setRankingFeePerRanking(int ranking, int fee) {
		this.rankingFeesPerRanking[ranking - 1] = fee;
	}

	public String getPlayerName(int playerId) {
		return playerNames[playerId];
	}

	public void setPlayerName(int playerId, String name) {
		this.playerNames[playerId] = name;
	}

	public int getHandicap(int playerId) {
		return handicaps[playerId];
	}

	public void setHandicap(int playerId, int handicap) {
		this.handicaps[playerId] = handicap;
	}

	public int getExtraScore(int playerId) {
		return extraScores[playerId];
	}

	public void setExtraScore(int playerId, int score) {
		this.extraScores[playerId] = score;
	}

	public int getUsedHandicap(int playerId) {
		return usedHandicaps[playerId];
	}

	public void setUsedHandicap(int playerId, int handicap) {
		this.usedHandicaps[playerId] = handicap;
	}

	public int getPlayerScore(int playerId) {
		return playerScores[playerId];
	}

	public void setPlayerScore(int playerId, int score) {
		this.playerScores[playerId] = score;
	}

	public int getPlayerFee(int playerId) {
		return playerFees[playerId];
	}

	public void setPlayerFee(int playerId, int fee) {
		this.playerFees[playerId] = fee;
	}
}
