package org.dolicoli.golfz;

public class Result {
	private String gameId;
	private int holeNumber;
	private int parCount;

	private int[] scores;
	private int[] usedHandicaps;

	public Result() {
		scores = new int[6];
		usedHandicaps = new int[6];
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public int getHoleNumber() {
		return holeNumber;
	}

	public void setHoleNumber(int holeNumber) {
		this.holeNumber = holeNumber;
	}

	public int getParCount() {
		return parCount;
	}

	public void setParCount(int parCount) {
		this.parCount = parCount;
	}

	public int getScore(int playerId) {
		if (playerId < 0 || playerId > 6 - 1)
			return 0;

		return scores[playerId];
	}

	public void setScore(int playerId, int score) {
		if (playerId < 0 || playerId > 6 - 1)
			return;

		this.scores[playerId] = score;
	}

	public int getUsedHandicap(int playerId) {
		if (playerId < 0 || playerId > 6 - 1)
			return 0;

		return usedHandicaps[playerId];
	}

	public void setUsedHandicap(int playerId, int usedHandicap) {
		if (playerId < 0 || playerId > 6 - 1)
			return;

		this.usedHandicaps[playerId] = usedHandicap;
	}
}
