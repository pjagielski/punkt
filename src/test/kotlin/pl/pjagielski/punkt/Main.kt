package pl.pjagielski.punkt

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import pl.pjagielski.punkt.config.Configuration

fun main() = application(
    config = Config { addSpec(Configuration) }.from.yaml.resource("config.yaml")
)
