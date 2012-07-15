package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import models.RunStatistic;

import org.codehaus.jackson.JsonNode;

import play.Logger;
import play.libs.F.Function;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

	// this contains the list of Json nodes received from Runtastic WS
	private static List<RunStatistic> dataFromWS = new ArrayList<RunStatistic>();
	private static final String wsUrl = "http://codingcontest.runtastic.com/api/user/2/sport_sessions/10052308/heart_rate_trace";

	public static Result getJson() {

		// call Runtastic Webservice through the Async Module
		// It calls the Url through the Async Akka Webservice thread
		// It'll be much readable if Java had lambdas :-)
		return async(play.libs.WS
				.url(wsUrl)
				.get()
				.map(new Function<play.libs.WS.Response, Result>() {
					public Result apply(play.libs.WS.Response response)
							throws Throwable {
						JsonNode jsonNodes = response.asJson();
						Logger.info("Got: " + jsonNodes.size()
								+ " jsonNodes from WS");
						for (Iterator<JsonNode> iterator = jsonNodes.iterator(); iterator
								.hasNext();) {
							JsonNode node = iterator.next();
							RunStatistic nodeRepresentation = new RunStatistic();
							nodeRepresentation.setDuration(node.findPath(
									"duration").getIntValue());
							nodeRepresentation.setHeartRate(node.findPath(
									"heart_rate").getIntValue());
							nodeRepresentation.setTimestamp(node.findPath(
									"timestamp").getTextValue());
							nodeRepresentation.setSignalStrength(node.findPath(
									"signal_strength").getIntValue());
							nodeRepresentation.setDistance(node.findPath(
									"distance").getLongValue());
							nodeRepresentation.setVersion(node.findPath(
									"version").getLongValue());
							dataFromWS.add(nodeRepresentation);
						}
						return ok("");
					}
				}));
	}

	public static Result index() {
		// get data from Runtastic WS
		getJson();
		// sort because the data can be unsorted
		Collections.sort(dataFromWS);
		// process and o/p results to Logger
		process(dataFromWS);

		return ok("Request Complete");
	}

	protected static void process(List<RunStatistic> list) {
		int currentHeartRate, currentDuration;
		float easyDuration = 0, fatBurningDuration = 0, aerobicDuration = 0, anaerobicDuration = 0, redlineDuration = 0, nonOfThese = 0;

		if (list.size() > 1) {
			// store the 0th element
			RunStatistic firstElem = list.remove(0);
			currentDuration = firstElem.getDuration();
			currentHeartRate = firstElem.getHeartRate();

			float finalDuration = list.get(list.size() - 1).getDuration();

			for (RunStatistic runStatistic : list) {
				currentDuration = runStatistic.getDuration() - currentDuration;
				currentHeartRate = runStatistic.getHeartRate();

				if (currentHeartRate > 89 && currentHeartRate < 120) {
					easyDuration += currentDuration;
				} else if (currentHeartRate > 119 && currentHeartRate < 140) {
					fatBurningDuration += currentDuration;
				} else if (currentHeartRate > 139 && currentHeartRate < 160) {
					aerobicDuration += currentDuration;
				} else if (currentHeartRate > 159 && currentHeartRate < 172) {
					anaerobicDuration += currentDuration;
				} else if (currentHeartRate > 171 && currentHeartRate < 180) {
					redlineDuration += currentDuration;
				} else {
					nonOfThese += currentDuration;
				}
				currentDuration = runStatistic.getDuration();
			}
			// percentage
			Logger.info("easy duration percentage: "
					+ (easyDuration / finalDuration) * 100);
			Logger.info("fat duration percentage: "
					+ (fatBurningDuration / finalDuration) * 100);
			Logger.info("aerobic duration percentage: "
					+ (aerobicDuration / finalDuration) * 100);
			Logger.info("anaerobic duration percentage: "
					+ (anaerobicDuration / finalDuration) * 100);
			Logger.info("redLine duration percentage: "
					+ (redlineDuration / finalDuration) * 100);
			Logger.info("noneOfThese: " + (nonOfThese / finalDuration) * 100);
		}

	}

}