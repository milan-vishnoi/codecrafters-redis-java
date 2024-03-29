[![progress-banner](https://backend.codecrafters.io/progress/redis/fe327532-1430-4737-997f-701cc745bd25)](https://app.codecrafters.io/users/codecrafters-bot?r=2qF)

This is a starting point for Java solutions to the
["Build Your Own Redis" Challenge](https://codecrafters.io/challenges/redis).

In this challenge, I have built a toy Redis clone that's capable of handling
basic commands like `PING`, `SET` and `GET`. I have also implemented the Expiry of the set value by using `PX` command.


# Entry point

The entry point for my Redis implementation is in `src/main/java/Main.java`.


# How to run?

1. Ensure you have `java (1.8)` installed locally
2. Run `./spawn_redis_server.sh` to run your Redis server, which is implemented
   in `src/main/java/Main.java`.
