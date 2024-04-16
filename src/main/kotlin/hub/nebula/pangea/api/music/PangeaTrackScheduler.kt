package hub.nebula.pangea.api.music

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.audio.player.Equalizer
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.lavakord.audio.player.rotation
import dev.schlaubi.lavakord.audio.player.timescale
import hub.nebula.pangea.PangeaInstance
import mu.KotlinLogging
import java.util.LinkedList
import kotlin.reflect.jvm.jvmName

class PangeaTrackScheduler(private val link: Link) {
    val queue = LinkedList<Track>()
    val logger = KotlinLogging.logger(this::class.jvmName)

    suspend fun queue(track: Track) {
        if (link.player.playingTrack != null) {
            logger.info { "The bot is still playing songs, queueing..." }
            queue.offer(track)
        } else {
            logger.info { "No songs, yay, playing ${track.info.title} (${track.info.sourceName}) immediately!" }
            link.player.playTrack(track)
        }
    }

    suspend fun togglePause() = link.player.pause(!link.player.paused)

    suspend fun toggleBassBoost(state: Boolean) {
        link.player.applyFilters {
            if (state) {
                equalizers.add(
                    Equalizer(
                        1,
                        0.5f
                    )
                )
            } else {
                equalizers.removeIf { it.band == 1 }
            }
        }
    }

    suspend fun toggle8D(state: Boolean) {
        link.player.applyFilters {
            if (state) {
                rotation {
                    rotationHz = 0.2
                }
            } else {
                unsetRotation()
            }
        }
    }

    suspend fun toggleNightcore(state: Boolean) {
        link.player.applyFilters {
            if (state) {
                timescale {
                    speed = 1.2
                    pitch = 1.2
                    rate = 1.0
                }
            } else {
                unsetTimescale()
            }
        }
    }

    suspend fun toggleVaporwave(state: Boolean) {
        link.player.applyFilters {
            if (state) {
                timescale {
                    speed = 0.8
                    pitch = 0.8
                    rate = 1.0
                }
            } else {
                unsetTimescale()
            }
        }
    }

    suspend fun nextTrack() = link.player.playTrack(queue.poll())

    suspend fun terminate() {
        link.player.stopTrack()
        queue.clear()
        PangeaInstance.pangeaPlayers.remove(link.guildId.toLong())
        link.disconnectAudio()
        link.destroy()
    }

    init {
        link.player.on<TrackEvent> {
            when (this) {
                is TrackEndEvent -> {
                    if (queue.isNotEmpty()) {
                        nextTrack()
                    }
                }

                is TrackExceptionEvent -> {
                    logger.info { "An error occurred when playing the track: ${this.exception}" }
                }
            }
        }
    }
}