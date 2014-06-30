package lod2014group1.updating

import lod2014group1.Config
import lod2014group1.database.Queries
import org.joda.time.DateTime


class ImdbExistingMovieUpdater extends ImdbUpdater {

	// update weekly
	def updateOneYearOldMovies() {
		val currentDate = new DateTime()
		val dateOneYearAgo = new DateTime().plusYears(-1)

		val movieIds = Queries.getMovieIdsInTimeRange(dateOneYearAgo, currentDate)

		createCrawlifyTasks(movieIds, Config.DELETE_FIRST_FLAG)
	}

	// update monthly
	def updateFiveYearOldMovies() {
		val currentDate = new DateTime()
		val dateOneYearAgo = new DateTime().plusYears(-5)

		val movieIds = Queries.getMovieIdsInTimeRange(dateOneYearAgo, currentDate)

		createCrawlifyTasks(movieIds, Config.DELETE_FIRST_FLAG)
	}

	// update yearly
	def updateFiveToTwentyFiveYearOldMovies() {
		val currentDate = new DateTime().plusYears(-5)
		val dateOneYearAgo = new DateTime().plusYears(-25)

		val movieIds = Queries.getMovieIdsInTimeRange(dateOneYearAgo, currentDate)

		createCrawlifyTasks(movieIds, Config.DELETE_FIRST_FLAG)
	}






}
