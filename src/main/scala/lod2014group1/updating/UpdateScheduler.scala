package lod2014group1.updating

import java.util.{Date, TimerTask, Timer}

import org.joda.time.DateTime


object UpdateScheduler {
	val ONCE_PER_DAY = 1000 * 60 // * 60 * 24
	val MIDNIGHT = 	getDate

	private def getDate: Date = {
		val tomorrow = new DateTime().plusDays(1)
		tomorrow.withTimeAtStartOfDay()
		tomorrow.toDate
	}
}

class UpdateScheduler {

	def update() {
		val imdbUpdater = new ImdbComingSoonMovieUpdater

		val timer = new Timer()
		timer.schedule(new TimerTask() {
			@Override
			def run() {
				imdbUpdater.updateComingSoonMovies()
			}
		}, UpdateScheduler.MIDNIGHT, UpdateScheduler.ONCE_PER_DAY)
	}

}

