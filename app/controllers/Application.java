package controllers;

import java.util.ArrayList;
import java.util.List;

import models.RunStatistic;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.gson.Gson;

public class Application extends Controller {

	public static Result index() {

		String[] listOfRates = {
				"{\"version\":1,\"timestamp\":\"2012-03-17 09:31:37 +0100\", \"heart_rate\":121, \"signal_strength\":100, \"duration\":0, \"distance\":0}",
				"{\"version\":1,\"timestamp\":\"2012-03-17 09:31:41 +0100\", \"heart_rate\":122, \"signal_strength\":100, \"duration\":4002, \"distance\":0}",
				"{\"version\":1,\"timestamp\":\"2012-03-17 09:31:45 +0100\", \"heart_rate\":126, \"signal_strength\":100, \"duration\":8001, \"distance\":0}",
				"{\"version\":1,\"timestamp\":\"2012-03-17 09:31:49 +0100\", \"heart_rate\":130, \"signal_strength\":100, \"duration\":12001, \"distance\":0}",
				"{\"version\":1,\"timestamp\":\"2012-03-17 09:31:53 +0100\", \"heart_rate\":136, \"signal_strength\":100, \"duration\":16001, \"distance\":0}",
				"{\"version\":1,\"timestamp\":\"2012-03-17 09:31:57 +0100\", \"heart_rate\":136, \"signal_strength\":100, \"duration\":20001, \"distance\":0}",
				"{\"version\":1,\"timestamp\":\"2012-03-17 09:32:05 +0100\", \"heart_rate\":133, \"signal_strength\":97, \"duration\":28001, \"distance\":10}",
				"{\"version\":1,\"timestamp\":\"2012-03-17 09:32:11 +0100\", \"heart_rate\":132, \"signal_strength\":100, \"duration\":34000, \"distance\":33}",
				"{\"version\":1,\"timestamp\":\"2012-03-17 09:32:17 +0100\", \"heart_rate\":155, \"signal_strength\":100, \"duration\":40001, \"distance\":55}" };

		List<RunStatistic> listOfRateObj = new ArrayList<RunStatistic>();

		for (int i = 0; i < listOfRates.length; i++) {
			listOfRateObj.add(new Gson().fromJson(listOfRates[i],
					RunStatistic.class));
		}
		// Collections.sort(listOfRateObj);
		process(listOfRateObj);

		return ok("parsed JSOn");
	}

	protected static void process(List<RunStatistic> list) {
		int currentHeartRate, currentDuration;
		float easyDuration = 0, fatBurningDuration = 0, aerobicDuration = 0, anaerobicDuration = 0, redlineDuration = 0;

		if (list.size() > 1) {
			// store the 0th element
			RunStatistic firstElem = list.remove(0);
			currentDuration = firstElem.getDuration();
			currentHeartRate = firstElem.getHeartRate();

			float finalDuration = list.get(list.size() - 1).getDuration();

			for (RunStatistic runStatistic : list) {
				currentDuration = runStatistic.getDuration() - currentDuration;
				currentHeartRate = runStatistic.getHeartRate();

				if (currentHeartRate > 90 && currentHeartRate < 120) {
					easyDuration += currentDuration;
				} else if (currentHeartRate > 119 && currentHeartRate < 140) {
					fatBurningDuration += currentDuration;
				} else if (currentHeartRate > 139 && currentHeartRate < 160) {
					aerobicDuration += currentDuration;
				} else if (currentHeartRate > 159 && currentHeartRate < 172) {
					anaerobicDuration += currentDuration;
				} else if (currentHeartRate > 171 && currentHeartRate < 180) {
					redlineDuration += currentDuration;
				}
				currentDuration = runStatistic.getDuration();
			}
			// percentage
			Logger.info("easy duratiion percentage: "
					+ (easyDuration / finalDuration) * 100);
			Logger.info("fat duratiion percentage: "
					+ (fatBurningDuration / finalDuration) * 100);
			Logger.info("aerobic duratiion percentage: "
					+ (aerobicDuration / finalDuration) * 100);
			Logger.info("anaerobic duratiion percentage: "
					+ (anaerobicDuration / finalDuration) * 100);
			Logger.info("redLine duratiion )percentage: "
					+ (redlineDuration / finalDuration) * 100);
		}

	}

}