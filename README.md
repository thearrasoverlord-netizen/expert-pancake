# Expert Pancake

An open-source Java arcade prototype inspired by first-person glass-smashing games. The project starts with a dependency-free 3D-world prototype and is intended to become customizable, with a possible Java ME (J2ME) version later.

This is an original implementation; it does not include proprietary Smash Hit source code or assets.

## Current prototype

- 3D world coordinates with perspective projection
- First-person corridor drawn from 3D lines and faces
- Glass targets with volume and perspective
- Balls with 3D position, velocity, and gravity
- Collision detection between projectiles and glass targets
- Glass-break particles with individual motion
- Score, limited balls, and restart
- No external libraries

> The current renderer is a lightweight software renderer using Java2D and perspective projection, not a hardware-accelerated 3D engine yet. This keeps the prototype easy to compile and provides a base for future renderer improvements.

## Requirements

- JDK 8 or newer
- Windows, Linux, or macOS with a desktop environment

## Run it

From the repository root:

```sh
mkdir out
javac -d out src/Main.java src/Game.java
java -cp out Main
```

If the `out` directory already exists, you can skip `mkdir out`.

## Controls

- **Left-click:** throw a ball toward the cursor
- **R:** restart
- **Esc:** exit

## Roadmap

- [x] 3D coordinates and perspective projection
- [x] 3D projectile motion and gravity
- [x] Glass collision and break particles
- [ ] Improved fragment geometry and collision response
- [ ] Camera movement and level configuration
- [ ] Explore a Java ME / J2ME-compatible renderer and controls

## License

MIT. See [LICENSE](LICENSE).
