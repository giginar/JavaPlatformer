# Third-party software notice inventory

This inventory covers runtime dependencies declared by the Maven reactor and is
derived from the resolved dependency tree and license fields in locally resolved
Maven metadata. It is an engineering inventory, not a legal-sufficiency opinion.

| Runtime component(s) | Version(s) | Metadata license | Notice action / status |
| --- | --- | --- | --- |
| libGDX core, Android/LWJGL3 backends, FreeType bindings and platform natives | 1.14.2 | Apache License 2.0 | Preserve copyright, NOTICE if supplied, and Apache-2.0 license text in distributions |
| libGDX jnigen loader | 2.5.2 | Apache License 2.0 | Same Apache-2.0 packaging requirement |
| gdx-controllers core, desktop and Android | 2.2.4 | Apache License 2.0 | Same Apache-2.0 packaging requirement |
| LWJGL core, GLFW, OpenAL, OpenGL, stb, jemalloc and natives | 3.3.3 | BSD-3-Clause | Verified upstream text retained at `licenses/LWJGL-BSD-3-Clause.txt`; reproduce it in binary distributions |
| Jamepad | 2.26.5.0 | Apache License 2.0 | Same Apache-2.0 packaging requirement |
| JLayer libGDX fork | 1.0.1-gdx | LGPL-2.1 in resolved POM | Preserve LGPL notices/license and verify binary distribution obligations; MANUAL VERIFICATION REQUIRED |
| JOrbis | 0.0.17 | GNU Lesser General Public License (version unspecified in resolved POM) | Exact applicable text and binary distribution obligations are MANUAL VERIFICATION REQUIRED |
| AndroidX core, annotation, collection, concurrent, interpolator, lifecycle, profileinstaller, startup, tracing and versionedparcelable modules | Versions declared in `android/pom.xml` | Apache License 2.0 in resolved POM metadata | Preserve Apache-2.0 license and supplied notices |
| Kotlin standard library | 2.0.21 | Apache License 2.0 | Preserve Apache-2.0 license and supplied notices |
| kotlinx-coroutines Android/core | 1.8.1 | Apache License 2.0 | Preserve Apache-2.0 license and supplied notices |
| JetBrains annotations | 23.0.0 | Apache License 2.0 | Preserve Apache-2.0 license and supplied notices |
| JSpecify | 1.0.0 | Apache License 2.0 | Preserve Apache-2.0 license and supplied notices |
| Guava `listenablefuture` compatibility artifact | 1.0 | No license field in the resolved artifact POM | License determination is MANUAL VERIFICATION REQUIRED |

Before production distribution, inspect the final shaded desktop JAR/installer and
Android bundle to confirm that required license and NOTICE files survive packaging.
The in-game About screen supplies a discoverable summary; it does not replace the
full texts required by dependency licenses.
