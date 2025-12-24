## Baguette - an example Canvas fork

This repo showcases an example fork of Canvas using [Weaver](https://github.com/CraftCanvasMC/Weaver).
This repo is always up to date with the latest Canvas version available

#### Useful Links

- [Discord](https://canvasmc.io/discord)

---

Here we assume you're already familiar with how to fork with Paperweight, but if you're not, you can get an idea [here](https://github.com/PurpurMC/Purpur/blob/HEAD/CONTRIBUTING.md) and [here](https://github.com/PaperMC/Paper/blob/HEAD/CONTRIBUTING.md).

A beginners guide can also be found here -> [link](https://github.com/Toffikk/paperweight-examples/tree/v2-fork-of-fork)

_note: those guides don't contain info on Weaver specific behaviours and thus should be only be treated as a starting point_

---

For info on how to use the new system and how it differs from Paperweight, refer to the [example base patch](https://github.com/CraftCanvasMC/Baguette/blob/HEAD/baguette-server/minecraft-patches/base/0001-Appreciate-base-patches-more.patch) and the `build.gradle.kts` file.

*Notably Weaver adds an additional patch set `base` which is applied before your fork's source and feature patches. It also exposes some additional configuration and tasks for which you can get detailed info in the `build.gradle.kts` file.*
