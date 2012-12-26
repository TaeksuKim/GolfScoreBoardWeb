package org.dolicoli.golfz;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

public class GameListServlet extends HttpServlet {
	private static final long serialVersionUID = 8837998838859386723L;

	private static final SimpleDateFormat format = new SimpleDateFormat(
			"yyyyMMddHH");

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/xml; charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				resp.getOutputStream(), "UTF-8"), true);
		writer.println("<?xml version='1.0' encoding='utf-8'?>");

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

		writer.println("<response result='OK' type='games'>");

		ArrayList<Game> games = getGames(from, to);
		for (Game game : games) {
			writer.print("<game ");
			writer.print("gameId='" + game.getGameId() + "' ");
			writer.print("gameDate='" + game.getDate() + "' ");
			int playerCount = game.getPlayerCount();
			writer.print("playerCount='" + playerCount + "' ");
			writer.println(">");

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

				writer.println("/>");
			}

			writer.println("</game>");
		}

		writer.println("</response>");
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
			int playerCount = Common.getIntProperty(gameEntity, "playerCount");
			gameSetting.setPlayerCount(playerCount);

			for (int i = 0; i < Common.MAX_PLAYER_COUNT; i++) {
				String name = (String) gameEntity.getProperty("playerName" + i);
				gameSetting.setPlayerName(i, name);
			}

			list.add(gameSetting);
		}

		return list;
	}
}
