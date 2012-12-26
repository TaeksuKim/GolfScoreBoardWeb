package org.dolicoli.golfz;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;

public class UpdateGameServlet extends HttpServlet {
	private static final long serialVersionUID = -8353171528427765952L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		doPost(req, resp);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		req.setCharacterEncoding("UTF-8");

		resp.setContentType("text/xml");
		PrintWriter writer = resp.getWriter();

		String gameId = req.getParameter("gameId");
		if (gameId == null || gameId.equals("")) {
			writer.println("ERROR");
			return;
		}

		Game game = new Game();
		game.setGameId(gameId);

		game.setDate(getLongParameter(req, "gameDate"));
		game.setHoleCount(getIntParameter(req, "holeCount"));
		game.setPlayerCount(getIntParameter(req, "playerCount"));

		game.setHoleFee(getIntParameter(req, "holeFee"));
		game.setExtraFee(getIntParameter(req, "extraFee"));
		game.setRankingFee(getIntParameter(req, "rankingFee"));
		String fieldName = req.getParameter("fieldName");
		if (fieldName == null) {
			fieldName = "";
		}
		game.setFieldName(fieldName);
		game.setFairwayDifficulty(getIntParameter(req, "fairwayDifficulty"));
		game.setGreenDifficulty(getIntParameter(req, "greenDifficulty"));

		for (int i = 0; i < 6; i++) {
			game.setHoleFeePerRanking(i + 1,
					getIntParameter(req, "holeFeePerRanking" + (i + 1)));
			game.setRankingFeePerRanking(i + 1,
					getIntParameter(req, "rankingFeePerRanking" + (i + 1)));

			String playerName = new String(req.getParameter("playerName" + i)
					.getBytes("utf-8"), "utf-8");
			game.setPlayerName(i, playerName);
			game.setHandicap(i, getIntParameter(req, "handicap" + i));
			game.setExtraScore(i, getIntParameter(req, "extraScore" + i));
		}

		updateGame(game);

		int resultCount = getIntParameter(req, "resultCount");
		ArrayList<Result> results = new ArrayList<Result>();
		for (int i = 0; i < resultCount; i++) {
			Result result = new Result();
			result.setGameId(gameId);

			result.setHoleNumber(getIntParameter(req, "holeNumber_" + i));
			result.setParCount(getIntParameter(req, "parCount_" + i));

			for (int j = 0; j < 6; j++) {
				result.setScore(j,
						getIntParameter(req, "result_score_" + i + "_" + j));
				result.setUsedHandicap(
						j,
						getIntParameter(req, "result_usedHandicap_" + i + "_"
								+ j));
			}

			results.add(result);
		}

		updateResults(gameId, results);
		writer.println("OK");
	}

	private void updateGame(Game game) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		Entity gameEntity = new Entity("Game", game.getGameId());

		gameEntity.setProperty("gameId", game.getGameId());

		Date gameDate = new Date();
		gameDate.setTime(game.getDate());
		gameEntity.setProperty("gameDate", gameDate);

		gameEntity.setProperty("holeCount", game.getHoleCount());
		gameEntity.setProperty("playerCount", game.getPlayerCount());

		gameEntity.setProperty("holeFee", game.getHoleFee());
		gameEntity.setProperty("extraFee", game.getExtraFee());
		gameEntity.setProperty("rankingFee", game.getRankingFee());

		gameEntity.setProperty("fieldName", game.getFieldName());
		gameEntity
				.setProperty("fairwayDifficulty", game.getFairwayDifficulty());
		gameEntity.setProperty("greenDifficulty", game.getGreenDifficulty());

		for (int i = 0; i < 6; i++) {
			gameEntity.setProperty("holeFeePerRanking" + (i + 1),
					game.getHoleFeePerRanking(i + 1));

			gameEntity.setProperty("rankingFeePerRanking" + (i + 1),
					game.getRankingFeePerRanking(i + 1));

			gameEntity.setProperty("playerName" + i, game.getPlayerName(i));
			gameEntity.setProperty("handicap" + i, game.getHandicap(i));
			gameEntity.setProperty("extraScore" + i, game.getExtraScore(i));
		}

		datastore.put(gameEntity);
	}

	private void updateResults(String gameId, ArrayList<Result> results) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Transaction txn = datastore.beginTransaction();

		try {
			{
				ArrayList<Key> deleteKeys = new ArrayList<Key>();
				for (int hole = 1; hole <= 18; hole++) {
					Key key = new KeyFactory.Builder("Result", gameId)
							.addChild("Result", hole).getKey();
					deleteKeys.add(key);
				}
				datastore.delete(deleteKeys);
			}
			for (Result result : results) {
				Key key = new KeyFactory.Builder("Result", result.getGameId())
						.addChild("Result", result.getHoleNumber()).getKey();

				Entity resultEntity = new Entity(key);

				resultEntity.setProperty("gameId", result.getGameId());
				resultEntity.setProperty("holeNumber", result.getHoleNumber());
				resultEntity.setProperty("parCount", result.getParCount());

				for (int i = 0; i < 6; i++) {
					resultEntity.setProperty("score" + i, result.getScore(i));
					resultEntity.setProperty("usedHandicap" + i,
							result.getUsedHandicap(i));
				}
				datastore.put(resultEntity);
			}
			txn.commit();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	private static int getIntParameter(HttpServletRequest req, String key) {
		String text = req.getParameter(key);
		try {
			return Integer.parseInt(text);
		} catch (Throwable t) {
			return 0;
		}
	}

	private static long getLongParameter(HttpServletRequest req, String key) {
		String text = req.getParameter(key);
		try {
			return Long.parseLong(text);
		} catch (Throwable t) {
			return 0L;
		}
	}
}
