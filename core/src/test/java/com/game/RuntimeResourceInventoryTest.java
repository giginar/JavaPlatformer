package com.game;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeResourceInventoryTest {
    private static final List<String> RUNTIME_ASSETS = List.of(
        "background.png",
        "diver.png",
        "diver_green.png",
        "diver_red.png",
        "harpoon.png",
        "oxygen_tank.png",
        "enemy_small.png",
        "enemy_fast.png",
        "enemy_piranha.png",
        "enemy_shark.png",
        "enemy_octopus_boss.png",
        "fonts/Orbitron-Regular.ttf",
        "fonts/Orbitron-Bold.ttf",
        "i18n/messages.properties",
        "i18n/messages_tr.properties",
        "shaders/diver-outline.vert",
        "shaders/diver-outline.frag",
        "underwater.mp3",
        "breath.mp3",
        "shoot.wav",
        "hit.wav",
        "oxygen.wav",
        "gameover.wav",
        "select.wav",
        "confirm.wav"
    );

    private static final List<String> REQUIRED_NOTICES = List.of(
        "assets/fonts/OFL.txt",
        "assets/licenses/LWJGL-BSD-3-Clause.txt",
        "assets/THIRD_PARTY_NOTICES.md",
        "assets/THIRD_PARTY_SOFTWARE_NOTICES.md",
        "assets/ASSET_PROVENANCE_AUDIT.md",
        "assets/AUDIO_RIGHTS_AUDIT.md"
    );

    @Test
    void everyRuntimeAssetIsPresentAndNonEmpty() {
        Path assetRoot = projectRoot().resolve("assets");
        assertAll(RUNTIME_ASSETS.stream().map(relative -> () -> {
            Path asset = assetRoot.resolve(relative);
            assertTrue(Files.isRegularFile(asset), "Missing runtime asset: " + relative);
            assertTrue(Files.size(asset) > 0L, "Empty runtime asset: " + relative);
        }));
    }

    @Test
    void requiredNoticesAndRightsAuditsArePresentAndNonEmpty() {
        Path root = projectRoot();
        assertAll(REQUIRED_NOTICES.stream().map(relative -> () -> {
            Path notice = root.resolve(relative);
            assertTrue(Files.isRegularFile(notice), "Missing notice or audit: " + relative);
            assertTrue(Files.size(notice) > 0L, "Empty notice or audit: " + relative);
        }));
    }

    private static Path projectRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve("assets"))
                && Files.isRegularFile(current.resolve("pom.xml"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to locate the project root from "
            + Path.of("").toAbsolutePath());
    }
}
