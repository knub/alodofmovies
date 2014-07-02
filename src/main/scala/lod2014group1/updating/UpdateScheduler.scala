package lod2014group1.updating

import java.util.{Date, TimerTask, Timer}

import org.joda.time.DateTime


object UpdateScheduler {
	val ONCE_PER_DAY = 1000 * 60 * 60 * 24
	val ONCE_PER_WEEK =  1000 * 60 * 60 * 24 * 7
	val ONCE_PER_MONTH =  1000 * 60 * 60 * 24 * 28
	val ONCE_PER_YEAR =  1000 * 60 * 60 * 24 * 365
	val MIDNIGHT = 	getDate
	val NOW = 0

	private def getDate: Date = {
		val tomorrow = new DateTime().plusDays(1)
		tomorrow.withTimeAtStartOfDay()
		tomorrow.toDate
	}
}

class UpdateScheduler {

	def update() {
		val imdbUpcomingUpdater = new ImdbComingSoonMovieUpdater
		val imdbExistingUpdater = new ImdbExistingMovieUpdater

		val timer = new Timer()

		// Daily Tasks
		timer.schedule(new TimerTask() {
			@Override
			def run() {
				imdbUpcomingUpdater.updateComingSoonMovies()
			}
		}, UpdateScheduler.NOW, UpdateScheduler.ONCE_PER_DAY)

//		// Weekly Tasks
//		timer.schedule(new TimerTask() {
//			@Override
//			def run() {
//				imdbExistingUpdater.updateOneYearOldMovies()
//			}
//		}, UpdateScheduler.NOW, UpdateScheduler.ONCE_PER_WEEK)
//
//		// Monthly Tasks
//		timer.schedule(new TimerTask() {
//			@Override
//			def run() {
//				imdbExistingUpdater.updateFiveYearOldMovies()
//			}
//		}, UpdateScheduler.MIDNIGHT, UpdateScheduler.ONCE_PER_MONTH)
//
//		// Yearly Tasks
//		timer.schedule(new TimerTask() {
//			@Override
//			def run() {
//				imdbExistingUpdater.updateFiveToTwentyFiveYearOldMovies()
//			}
//		}, UpdateScheduler.MIDNIGHT, UpdateScheduler.ONCE_PER_YEAR)
	}

}

