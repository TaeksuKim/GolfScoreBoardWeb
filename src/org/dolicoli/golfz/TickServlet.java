package org.dolicoli.golfz;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

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

public class TickServlet extends HttpServlet {

	private static final long serialVersionUID = -5661900843863895173L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/xml; charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				resp.getOutputStream(), "UTF-8"), true);
		writer.println("<?xml version='1.0' encoding='utf-8'?>");

		long tick = getTick();
		writer.println("<response result='OK' type='tick' tick='" + tick + "'>");
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
}
