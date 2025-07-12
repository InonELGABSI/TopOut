import com.topout.kmp.data.track_points.TrackPointsRepository

class StopSession(
    private val trackPointsRepo: TrackPointsRepository
) {
    suspend operator fun invoke() {
        trackPointsRepo.endCurrentSession()
    }
}
