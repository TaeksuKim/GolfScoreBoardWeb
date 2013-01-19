package org.dolicoli.golfz;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class HistoryListServlet extends HttpServlet {
	private static final long serialVersionUID = 8837998838859386723L;

	private static final SimpleDateFormat format = new SimpleDateFormat(
			"yyyyMMddHH");

	private HashMap<String, ArrayList<Result>> resultsMap;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/xml; charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				resp.getOutputStream(), "UTF-8"), true);
		writer.println("<?xml version='1.0' encoding='utf-8'?>");

		resultsMap = new HashMap<String, ArrayList<Result>>();

		String from = req.getParameter("from");
		if (from == null || from.equals("")) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 5);
			calendar.add(Calendar.MONTH, -2);

			from = format.format(calendar.getTime());
		}

		String to = req.getParameter("to");
		if (to == null || to.equals("")) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			to = format.format(calendar.getTime());
		}

		long tick = getTick();

		writer.println("<response result='OK' type='historyList' tick='" + tick
				+ "'>");

		ArrayList<Game> games = getGames(from, to);
		for (Game game : games) {
			writer.print("<game ");
			writer.print("gameId='" + game.getGameId() + "' ");
			writer.print("gameDate='" + game.getDate() + "' ");
			writer.print("holeCount='" + game.getHoleCount() + "' ");
			int playerCount = game.getPlayerCount();
			writer.print("playerCount='" + playerCount + "' ");
			writer.print("fieldName='" + game.getFieldName() + "' ");
			writer.print("fairwayDifficulty='" + game.getFairwayDifficulty()
					+ "' ");
			writer.print("greenDifficulty='" + game.getGreenDifficulty() + "' ");
			writer.println(">");

			writer.print("<fee ");
			writer.print("holeFee='" + game.getHoleFee() + "' ");
			writer.print("extraFee='" + game.getExtraFee() + "' ");
			writer.print("rankingFee='" + game.getRankingFee() + "' ");
			writer.println(" /> ");

			for (int i = 1; i <= Common.MAX_PLAYER_COUNT; i++) {
				writer.print("<holeFee ");
				writer.print("ranking='" + i + "' ");
				writer.print("fee='" + game.getHoleFeePerRanking(i) + "' ");
				writer.println(" /> ");
			}

			for (int i = 1; i <= Common.MAX_PLAYER_COUNT; i++) {
				writer.print("<rankingFee ");
				writer.print("ranking='" + i + "' ");
				writer.print("fee='" + game.getRankingFeePerRanking(i) + "' ");
				writer.println(" /> ");
			}

			for (int i = 0; i < Common.MAX_PLAYER_COUNT; i++) {
				writer.print("<player ");

				writer.print("id='");
				writer.print(i);
				writer.print("' ");

				writer.print("name='");
				if (i < playerCount)
					writer.print(game.getPlayerName(i));
				else
					writer.print("Player " + i);
				writer.print("' ");

				writer.print("handicap='");
				if (i < playerCount)
					writer.print(game.getHandicap(i));
				else
					writer.print("0");
				writer.print("' ");

				writer.print("extraScore='");
				if (i < playerCount)
					writer.print(game.getExtraScore(i));
				else
					writer.print("0");
				writer.print("' ");

				writer.print("score='");
				if (i < playerCount)
					writer.print(game.getPlayerScore(i));
				else
					writer.print("0");
				writer.print("' ");

				writer.print("usedHandicap='");
				if (i < playerCount)
					writer.print(game.getUsedHandicap(i));
				else
					writer.print("0");
				writer.print("' ");

				writer.print("fee='");
				if (i < playerCount)
					writer.print(game.getPlayerFee(i));
				else
					writer.print("0");
				writer.print("' ");

				writer.println(" />");
			}

			writer.println("<results>");
			ArrayList<Result> results = resultsMap.get(game.getGameId());
			for (Result result : results) {
				writer.print("<result ");
				writer.print("gameId='" + result.getGameId() + "' ");
				writer.print("holeNumber='" + result.getHoleNumber() + "' ");
				writer.print("parCount='" + result.getParCount() + "' ");
				writer.println("> ");
				for (int i = 0; i < Common.MAX_PLAYER_COUNT; i++) {
					writer.print("<score ");
					writer.print("playerId='" + i + "' ");
					writer.print("score='" + result.getScore(i) + "' ");
					writer.print("usedHandicap='" + result.getUsedHandicap(i)
							+ "' ");
					writer.println("/>");
				}
				writer.println("</result>");
			}
			writer.println("</results>");

			writer.println("</game>");
		}

		writer.println("</response>");
	}

	private long getTick() {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		Filter keyFilter = new FilterPredicate("key", FilterOperator.EQUAL, 1);
		Query q = new Query("Tick").setFilter(keyFilter);

		long tick = 0L;
		PreparedQuery pq = datastore.prepare(q);
		for (Entity gameEntity : pq.asIterable()) {
			tick = Common.getLongProperty(gameEntity, "tick", 0L);
			break;
		}

		return tick;
	}

	private ArrayList<Game> getGames(String from, String to) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		Filter fromFilter = new FilterPredicate("gameId",
				FilterOperator.GREATER_THAN_OR_EQUAL, from);
		Filter toFilter = new FilterPredicate("gameId",
				FilterOperator.LESS_THAN_OR_EQUAL, to);

		Filter filter = CompositeFilterOperator.and(fromFilter, toFilter);
		Query q = new Query("Game").setFilter(filter);

		PreparedQuery pq = datastore.prepare(q);

		ArrayList<Game> list = new ArrayList<Game>();
		for (Entity gameEntity : pq.asIterable()) {
			Game gameSetting = new Game();

			String gameId = (String) gameEntity.getProperty("gameId");
			gameSetting.setGameId(gameId);
			Date gameDate = (Date) gameEntity.getProperty("gameDate");
			gameSetting.setDate(gameDate.getTime());
			int holeCount = Common.getIntProperty(gameEntity, "holeCount");
			gameSetting.setHoleCount(holeCount);
			int playerCount = Common.getIntProperty(gameEntity, "playerCount");
			gameSetting.setPlayerCount(playerCount);

			if (gameEntity.hasProperty("fieldName")) {
				gameSetting.setFieldName((String) gameEntity
						.getProperty("fieldName"));
			} else {
				gameSetting.setFieldName("");
			}
			gameSetting.setFairwayDifficulty(Common.getIntProperty(gameEntity,
					"fairwayDifficulty"));
			gameSetting.setGreenDifficulty(Common.getIntProperty(gameEntity,
					"greenDifficulty"));

			int holeFee = Common.getIntProperty(gameEntity, "holeFee");
			gameSetting.setHoleFee(holeFee);
			int extraFee = Common.getIntProperty(gameEntity, "extraFee");
			gameSetting.setExtraFee(extraFee);
			int rankingFee = Common.getIntProperty(gameEntity, "rankingFee");
			gameSetting.setRankingFee(rankingFee);

			for (int i = 1; i <= Common.MAX_PLAYER_COUNT; i++) {
				int fee = Common.getIntProperty(gameEntity, "holeFeePerRanking"
						+ i);
				gameSetting.setHoleFeePerRanking(i, fee);

				fee = Common.getIntProperty(gameEntity, "rankingFeePerRanking"
						+ i);
				gameSetting.setRankingFeePerRanking(i, fee);
			}

			int[] playerScores = new int[Common.MAX_PLAYER_COUNT];
			int[] usedHandicaps = new int[Common.MAX_PLAYER_COUNT];

			for (int i = 0; i < Common.MAX_PLAYER_COUNT; i++) {
				playerScores[i] = 0;
				usedHandicaps[i] = 0;

				String name = (String) gameEntity.getProperty("playerName" + i);
				gameSetting.setPlayerName(i, name);

				int handicap = Common
						.getIntProperty(gameEntity, "handicap" + i);
				gameSetting.setHandicap(i, handicap);

				int extraScore = Common.getIntProperty(gameEntity, "extraScore"
						+ i);
				gameSetting.setExtraScore(i, extraScore);
			}

			ArrayList<Result> results = getResults(gameId);
			resultsMap.put(gameId, results);
			for (Result result : results) {
				for (int playerId = 0; playerId < Common.MAX_PLAYER_COUNT; playerId++) {
					playerScores[playerId] += result.getScore(playerId);
					usedHandicaps[playerId] += result.getUsedHandicap(playerId);
				}
			}

			for (int i = 0; i < Common.MAX_PLAYER_COUNT; i++) {
				gameSetting.setPlayerScore(i, playerScores[i]);
				gameSetting.setUsedHandicap(i, usedHandicaps[i]);
			}

			list.add(gameSetting);
		}

		return list;
	}

	private ArrayList<Result> getResults(String gameId) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		Filter filter = new FilterPredicate("gameId", FilterOperator.EQUAL,
				gameId);

		Query q = new Query("Result").setFilter(filter);

		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);

		ArrayList<Result> results = new ArrayList<Result>();
		int score = 0;
		int usedHandicap = 0;
		for (Entity resultEntity : pq.asIterable()) {
			Result result = new Result();
			result.setGameId(gameId);

			int holeNumber = Common.getIntProperty(resultEntity, "holeNumber");
			result.setHoleNumber(holeNumber);
			int parCount = Common.getIntProperty(resultEntity, "parCount");
			result.setParCount(parCount);

			for (int i = 0; i < Common.MAX_PLAYER_COUNT; i++) {
				score = Common.getIntProperty(resultEntity, "score" + i);
				result.setScore(i, score);
				usedHandicap = Common.getIntProperty(resultEntity,
						"usedHandicap" + i);
				result.setUsedHandicap(i, usedHandicap);
			}
			results.add(result);
		}

		return results;
	}
}
