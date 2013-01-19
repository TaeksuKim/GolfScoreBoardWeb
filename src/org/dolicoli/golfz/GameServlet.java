package org.dolicoli.golfz;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class GameServlet extends HttpServlet {
	private static final long serialVersionUID = -8353171528427765952L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/xml; charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				resp.getOutputStream(), "UTF-8"), true);
		writer.println("<?xml version='1.0' encoding='utf-8'?>");

		long tick = getTick();

		String gameId = req.getParameter("gameId");
		if (gameId == null || gameId.equals("")) {
			writer.println("<response result='ERROR' message='Game id is null.' tick='"
					+ tick + "'/>");
			return;
		}

		Game game = getGame(gameId);
		if (game == null) {
			writer.println("<response result='ERROR' message='Cannot find game with id: "
					+ gameId + "'  tick='" + tick + "'/>");
			return;
		}

		writer.print("<response result='OK' type='game' ");
		writer.print("tick='" + tick + "' ");
		writer.print("gameId='" + game.getGameId() + "' ");
		writer.print("gameDate='" + game.getDate() + "' ");
		writer.print("holeCount='" + game.getHoleCount() + "' ");
		int playerCount = game.getPlayerCount();
		writer.print("playerCount='" + playerCount + "' ");
		writer.print("fieldName='" + game.getFieldName() + "' ");
		writer.print("fairwayDifficulty='" + game.getFairwayDifficulty() + "' ");
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

			writer.println(" />");
		}

		writer.println("<results>");
		ArrayList<Result> results = getResults(gameId);
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

	private Game getGame(String gameId) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		Filter filter = new FilterPredicate("gameId", FilterOperator.EQUAL,
				gameId);
		Query q = new Query("Game").setFilter(filter);

		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);

		int count = 0;
		Game gameSetting = new Game();
		for (Entity gameEntity : pq.asIterable()) {
			if (count++ > 0)
				break;

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

			for (int i = 0; i < Common.MAX_PLAYER_COUNT; i++) {
				String name = (String) gameEntity.getProperty("playerName" + i);
				gameSetting.setPlayerName(i, name);

				int handicap = Common
						.getIntProperty(gameEntity, "handicap" + i);
				gameSetting.setHandicap(i, handicap);

				int extraScore = Common.getIntProperty(gameEntity, "extraScore"
						+ i);
				gameSetting.setExtraScore(i, extraScore);
			}
		}
		if (count < 1)
			return null;

		return gameSetting;
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
