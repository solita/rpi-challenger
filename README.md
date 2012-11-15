# Raspberry Pi Challenger Appears!

A programming game.


## Getting Started

First install [Leiningen](https://github.com/technomancy/leiningen). Then start up the server with

    lein run

or

    lein uberjar
    java -jar target/rpi-challenger-*-standalone.jar

or

    lein repl
    (-main)

Then open <http://localhost/> in a web browser.


## Creating Challenges

Have a look at the sample in `src/rpi_challenger/challenges.clj` and `test/rpi_challenger/challenges_test.clj`. Challenges will be generated using functions such as the `hello-world` in there.

The details of deployment are TBD, but you can start creating challenges now. Just store them in a different Git repository than this one - you wouldn't want to make them public too early.


## License

Copyright © 2012 Solita Oy

Distributed under the MIT License.
