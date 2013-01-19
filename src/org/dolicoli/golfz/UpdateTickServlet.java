package org.dolicoli.golfz;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Transaction;

public class UpdateTickServlet extends HttpServlet {
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

		long time = new Date().getTime();
		updateTick(time);

		writer.println("OK");

	}

	private void updateTick(long time) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Transaction txn = datastore.beginTransaction();

		try {
			Entity tickEntity = new Entity("Tick", 1);
			tickEntity.setProperty("key", 1);
			tickEntity.setProperty("tick", time);
			datastore.put(tickEntity);
			txn.commit();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
