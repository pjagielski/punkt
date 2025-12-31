package pl.pjagielski.punkt.osc

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import org.junit.jupiter.api.Test

class OscMetaTest {

    private val oscMeta = OscMeta()

    @Test
    fun shouldReturnCorrectTrackBusId() {
        val buses = (1..10).map { oscMeta.nextTrackBusId() }
        assertThat(buses).containsExactly(20, 22, 24, 26, 28, 30, 32, 34, 36, 38)

        assertThat(runCatching { oscMeta.nextTrackBusId() })
            .isFailure().hasMessage("Too many tracks")
    }

    @Test
    fun shouldReturnCorrectBusId() {
        val buses = (1..100).map { oscMeta.nextBusId() }
        assertThat(buses[0]).isEqualTo(40)
        assertThat(buses[10]).isEqualTo(50)
        assertThat(buses[40]).isEqualTo(80)
        assertThat(buses[60]).isEqualTo(40)
        assertThat(buses[80]).isEqualTo(60)
    }
}
