# Expert Pancake

An open-source Java arcade prototype inspired by first-person glass-smashing games. The project starts with a simple pseudo-3D renderer and is intended to be customizable, with a possible Java ME (J2ME) version later.

This is an original implementation: it does not include proprietary Smash Hit source code or assets.

## Current prototype

- Pseudo-3D corridor drawn with Java2D
- Glass targets that approach the player
- Click targets to throw a ball and earn points
- Limited balls, score, and restart
- No external libraries

## Requirements

- JDK 8 or newer
- Windows, Linux, or macOS with a desktop environment

## Run it

From the repository root:

```sh
javac -d out src/Main.java src/Game.java
java -cp out Main
```

On Windows, these commands also work in Command Prompt or PowerShell.

## Controls

- **Left-click:** throw a ball at a glass target
- **R:** restart the run
- **Esc:** exit

## Roadmap

- [x] First playable Java desktop prototype
- [ ] Separate game settings and customizable parameters
- [ ] Add original sound effects and simple level data
- [ ] Improve collision and perspective
- [ ] Explore a Java ME / J2ME-compatible renderer and controls

## License

MIT. See [LICENSE](LICENSE).
