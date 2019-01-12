package simsage

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.PropertySource


@SpringBootApplication
@PropertySource("file:./simsage.properties")
@ComponentScan("simsage")
open class Main

fun main(args: Array<String>) {
    SpringApplication.run(Main::class.java, *args);
}

