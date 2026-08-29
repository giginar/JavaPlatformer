package com.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.game.DeepDiveDrift;
import com.game.GameConfig;
import com.game.diver.Background;
import com.game.diver.Diver;
import com.game.diver.Harpoon;
import com.game.diver.OxygenTank;
import com.game.diver.PowerUpPickup;
import com.game.effects.ParticleSystem;
import com.game.enemies.AbyssalOctopus;
import com.game.enemies.EnemyFish;
import com.game.enemies.EnemyProjectile;
import com.game.enemies.ElectricEel;
import com.game.enemies.FastFish;
import com.game.enemies.PiranhaSwarm;
import com.game.enemies.Shark;
import com.game.enemies.SmallFish;
import com.game.hazards.CrushingGate;
import com.game.hazards.EnvironmentalHazard;
import com.game.hazards.FallingDebris;
import com.game.hazards.ReefBarrier;
import com.game.hazards.SeaMine;
import com.game.hazards.ThermalVent;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;
import com.game.model.GameBalance;
import com.game.model.GameSession;
import com.game.model.DiverSuit;
import com.game.model.PowerUpType;
import com.game.model.ProgressionStore;
import com.game.model.RunDirector;
import com.game.model.RunUpgradeSchedule;
import com.game.model.UpgradeType;
import com.game.settings.DisplaySettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GameScreen extends BaseScreen {
    private static final String[] PAUSE_OPTIONS = {"CONTINUE", "OPTIONS", "MAIN MENU"};
    private static final boolean DEBUG_MODE = GameConfig.TEST_SHORTCUTS_ENABLED
        || Boolean.getBoolean("deepdive.debug");
    private static final boolean CAPTURE_AUTOPLAY = Boolean.getBoolean("deepdive.capture.autoplay");
    private static final Color BOSS_TEXT_COLOR = new Color(0.9f, 0.55f, 1f, 1f);
    private static final Color UPGRADE_TEXT_COLOR = new Color(0.45f, 0.92f, 1f, 1f);

    private static final float MAX_FRAME_DELTA = 1f / 15f;
    private static final float OXYGEN_TANK_SPAWN_INTERVAL = 8f;
    private static final float BASE_OXYGEN_PICKUP = 30f;
    private static final float INVULNERABILITY_DURATION = 1.1f;
    private static final float SEABED_CONTACT_DAMAGE = 18f;
    private static final float BREATH_INTERVAL = 10f;
    private static final float TUTORIAL_DURATION = 7f;
    private static final float BANNER_DURATION = 1.8f;
    private static final float POWER_UP_BANNER_DURATION = 2.8f;
    private static final float CELEBRATION_DURATION = 2.7f;
    private static final float BOSS_WARNING_DURATION = 2.7f;
    private static final float POWER_UP_SPAWN_INTERVAL = 18f;
    private static final float BASE_MAGNET_RANGE = 78f;
    private static final int MAX_DASH_CHARGES = 2;
    private static final Rectangle TOUCH_SWIM_BUTTON = new Rectangle(32f, 28f, 230f, 120f);
    private static final Rectangle TOUCH_FIRE_BUTTON = new Rectangle(1018f, 28f, 230f, 120f);
    private static final Rectangle TOUCH_DASH_BUTTON = new Rectangle(530f, 28f, 220f, 76f);
    private static final Rectangle TOUCH_PAUSE_BUTTON = new Rectangle(1182f, 548f, 66f, 66f);
    private static final Rectangle GAME_OVER_RETRY_BUTTON = new Rectangle(405f, 236f, 470f, 48f);
    private static final Rectangle GAME_OVER_MENU_BUTTON = new Rectangle(405f, 194f, 470f, 38f);

    private final BitmapFont smallFont;
    private final BitmapFont mediumFont;
    private final BitmapFont largeFont;
    private final GlyphLayout layout;
    private final Background background;
    private final List<EnemyFish> enemies;
    private final List<Harpoon> harpoons;
    private final List<OxygenTank> oxygenTanks;
    private final List<EnvironmentalHazard> hazards;
    private final List<EnemyProjectile> enemyProjectiles;
    private final List<EnemyFish> enemySpawnBuffer;
    private final List<PowerUpPickup> powerUpPickups;
    private final EnumMap<PowerUpType, Float> activePowerUps;
    private final ParticleSystem particles;
    private final GameSession session;
    private final Random random;
    private final Preferences preferences;
    private final ProgressionStore progression;
    private final DiverSuit equippedSuit;
    private final Rectangle interactiveRow = new Rectangle();

    private Diver diver;
    private RunDirector runDirector;
    private GameBalance.Difficulty difficulty;
    private List<UpgradeType> upgradeChoices = Collections.emptyList();
    private String bannerText = "";
    private String bannerSubtitle = "";
    private float enemySpawnTimer;
    private float oxygenTankSpawnTimer;
    private float hazardSpawnTimer;
    private float powerUpSpawnTimer;
    private float safetyTimer;
    private float shootCooldownTimer;
    private float invulnerabilityTimer;
    private float breathTimer;
    private float tutorialTimer;
    private float bannerTimer;
    private float bossWarningTimer;
    private float hitStopTimer;
    private float shakeTimer;
    private float shakeMagnitude;
    private float bubbleTimer;
    private float damageFlashTimer;
    private float inkVeilTimer;
    private float descentMotionTimer;
    private float ambientTime;
    private int selectedPauseIndex;
    private int selectedUpgradeIndex;
    private int highScore;
    private int nextRunUpgradeIndex;
    private int earnedPearls;
    private int dashCharges;
    private boolean highScoreDirty;
    private boolean paused;
    private boolean gameOver;
    private boolean showTutorial;
    private boolean choosingUpgrade;
    private boolean bossSpawned;
    private boolean victory;
    private boolean finalePending;
    private boolean progressionRewardGranted;
    private boolean captureSwimmingUp;

    public GameScreen(DeepDiveDrift game) {
        super(game);
        smallFont = FontManager.getSmallFont();
        mediumFont = FontManager.getMediumFont();
        largeFont = FontManager.getLargeFont();
        layout = new GlyphLayout();
        background = new Background(RunDirector.BASE_SCROLL_SPEED_WORLD_UNITS);
        enemies = new ArrayList<>();
        harpoons = new ArrayList<>();
        oxygenTanks = new ArrayList<>();
        hazards = new ArrayList<>();
        enemyProjectiles = new ArrayList<>();
        enemySpawnBuffer = new ArrayList<>();
        powerUpPickups = new ArrayList<>();
        activePowerUps = new EnumMap<>(PowerUpType.class);
        particles = new ParticleSystem();
        random = new Random();
        preferences = Gdx.app.getPreferences(GameConfig.PREFERENCES_NAME);
        progression = new ProgressionStore(preferences);
        equippedSuit = progression.selectedSuit();
        session = new GameSession(
            progression.startingMaxOxygen() + equippedSuit.oxygenBonus());
        highScore = preferences.getInteger("highScore", 0);
        resetGame();
        if (Boolean.getBoolean("deepdive.capture.boss")) {
            jumpToBossForDebug();
        }
    }

    @Override
    public void show() {
        AudioManager.playBackgroundMusic();
    }

    @Override
    public void render(float delta) {
        float frameDelta = Math.min(delta, MAX_FRAME_DELTA);
        updateWorldCamera(frameDelta);
        prepareFrame(0f, 0.04f, 0.12f);
        layoutTouchControls();
        updateInput();

        if (handleInput()) {
            return;
        }

        boolean simulationRunning = !paused && !gameOver && !choosingUpgrade;
        if (simulationRunning) {
            if (hitStopTimer > 0f) {
                hitStopTimer = Math.max(0f, hitStopTimer - frameDelta);
            } else {
                updateGame(frameDelta);
            }
            particles.update(frameDelta);
        } else if (gameOver) {
            particles.update(frameDelta);
        }

        drawWorld();
        drawWorldEffects();

        centerCamera();
        drawUiShapes();
        drawUiText();
    }

    private boolean handleInput() {
        if (choosingUpgrade) {
            handleUpgradeInput();
            return false;
        }

        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)
                || game.input().confirmJustPressed()
                || game.input().pointerJustPressed(GAME_OVER_RETRY_BUTTON)) {
                AudioManager.playConfirm();
                resetGame();
            } else if (game.input().backJustPressed()
                || game.input().pointerJustPressed(GAME_OVER_MENU_BUTTON)) {
                AudioManager.playSelect();
                game.showMainMenu();
                return true;
            }
            return false;
        }

        if (paused) {
            for (int i = 0; i < PAUSE_OPTIONS.length; i++) {
                pauseRowBounds(i);
                if (game.input().pointerOver(interactiveRow) && selectedPauseIndex != i) {
                    selectedPauseIndex = i;
                    AudioManager.playSelect();
                }
                if (game.input().pointerJustPressed(interactiveRow)) {
                    selectedPauseIndex = i;
                    return handlePauseSelection();
                }
            }

            if (game.input().pauseJustPressed() || game.input().backJustPressed()) {
                paused = false;
                AudioManager.playSelect();
            } else if (game.input().menuUpJustPressed()) {
                selectedPauseIndex = Math.floorMod(selectedPauseIndex - 1, PAUSE_OPTIONS.length);
                AudioManager.playSelect();
            } else if (game.input().menuDownJustPressed()) {
                selectedPauseIndex = (selectedPauseIndex + 1) % PAUSE_OPTIONS.length;
                AudioManager.playSelect();
            } else if (game.input().confirmJustPressed()) {
                return handlePauseSelection();
            }
            return false;
        }

        if (game.input().pauseJustPressed()
            || game.input().pointerJustPressed(TOUCH_PAUSE_BUTTON)) {
            paused = true;
            selectedPauseIndex = 0;
            AudioManager.playSelect();
        } else if (dashCharges > 0 && (game.input().dashJustPressed()
            || game.input().isMobile()
            && game.input().pointerJustPressed(TOUCH_DASH_BUTTON))) {
            useDashCharge();
        } else if (game.input().helpJustPressed()) {
            showTutorial = true;
            tutorialTimer = 0f;
            AudioManager.playSelect();
        }

        if (DEBUG_MODE) {
            handleDebugInput();
        }
        return false;
    }

    private void handleUpgradeInput() {
        for (int i = 0; i < upgradeChoices.size(); i++) {
            upgradeCardBounds(i);
            if (game.input().pointerOver(interactiveRow) && selectedUpgradeIndex != i) {
                selectedUpgradeIndex = i;
                AudioManager.playSelect();
            }
            if (game.input().pointerJustPressed(interactiveRow)) {
                selectedUpgradeIndex = i;
                applySelectedUpgrade();
                return;
            }
        }

        if (game.input().menuLeftJustPressed() || game.input().menuUpJustPressed()) {
            selectedUpgradeIndex = Math.floorMod(selectedUpgradeIndex - 1, upgradeChoices.size());
            AudioManager.playSelect();
        } else if (game.input().menuRightJustPressed() || game.input().menuDownJustPressed()) {
            selectedUpgradeIndex = (selectedUpgradeIndex + 1) % upgradeChoices.size();
            AudioManager.playSelect();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            selectedUpgradeIndex = 0;
            applySelectedUpgrade();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && upgradeChoices.size() > 1) {
            selectedUpgradeIndex = 1;
            applySelectedUpgrade();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && upgradeChoices.size() > 2) {
            selectedUpgradeIndex = 2;
            applySelectedUpgrade();
        } else if (game.input().confirmJustPressed()) {
            applySelectedUpgrade();
        }
    }

    private void applySelectedUpgrade() {
        UpgradeType selected = upgradeChoices.get(selectedUpgradeIndex);
        if (session.applyUpgrade(selected)) {
            choosingUpgrade = false;
            showBanner(selected.title() + " INSTALLED");
            particles.spawnOxygenBurst(diver.getX() + Diver.WIDTH / 2f,
                diver.getY() + Diver.HEIGHT / 2f);
            AudioManager.playConfirm();
        }
    }

    private boolean handlePauseSelection() {
        AudioManager.playConfirm();
        switch (selectedPauseIndex) {
            case 0 -> paused = false;
            case 1 -> {
                game.openOptions();
                return true;
            }
            case 2 -> {
                game.showMainMenu();
                return true;
            }
            default -> throw new IllegalStateException("Unknown pause option: " + selectedPauseIndex);
        }
        return false;
    }

    private void handleDebugInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            runDirector.advanceStageForDebug();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3) && !bossSpawned) {
            jumpToBossForDebug();
        }
    }

    private void jumpToBossForDebug() {
        runDirector.jumpToFinaleForDebug();
        difficulty = runDirector.difficulty();
        background.setDepthStage(difficulty.level(), difficulty.scrollSpeedMultiplier());
        safetyTimer = 0f;
        finalePending = true;
        spawnBoss();
    }

    private void updateGame(float delta) {
        session.update(delta);
        refreshHighScore();
        if (session.isOutOfOxygen()) {
            endGame(false);
            return;
        }

        updateTimers(delta);
        RunDirector.UpdateResult runUpdate = runDirector.update(delta);
        updateRunProgression(runUpdate);
        if (updateRunUpgradeProgression()) {
            return;
        }

        if (finalePending && safetyTimer <= 0f && !bossSpawned) {
            spawnBoss();
        }

        float threatDelta = isPowerActive(PowerUpType.TIME_BUBBLE) ? delta * 0.45f : delta;
        spawnEntities(threatDelta);
        shootIfRequested();

        if (CAPTURE_AUTOPLAY) {
            if (diver.getY() < 250f) {
                captureSwimmingUp = true;
            } else if (diver.getY() > 410f) {
                captureSwimmingUp = false;
            }
        }
        boolean swimmingUp = game.input().swimPressed()
            || game.input().pointerPressed(TOUCH_SWIM_BUTTON)
            || CAPTURE_AUTOPLAY && captureSwimmingUp;
        diver.update(delta, swimmingUp,
            session.getAgilityMultiplier() * equippedSuit.agilityMultiplier());
        updateDiverBubbles(delta, swimmingUp);
        background.update(delta);

        for (Harpoon harpoon : harpoons) {
            harpoon.update(delta);
            particles.spawnHarpoonTrail(harpoon.getX(), harpoon.getY() + 5f);
        }

        float targetX = diver.getX() + Diver.WIDTH / 2f;
        float targetY = diver.getY() + Diver.HEIGHT / 2f;
        enemySpawnBuffer.clear();
        for (EnemyFish enemy : enemies) {
            enemy.update(threatDelta, targetX, targetY);
            EnemyProjectile projectile = enemy.pollProjectile(targetX, targetY);
            if (projectile != null) {
                enemyProjectiles.add(projectile);
            }
            EnemyFish spawnedEnemy = enemy.pollSpawnedEnemy();
            if (spawnedEnemy != null) {
                enemySpawnBuffer.add(spawnedEnemy);
            }
            EnvironmentalHazard spawnedHazard = enemy.pollHazard();
            if (spawnedHazard != null) {
                hazards.add(spawnedHazard);
            }
        }
        enemies.addAll(enemySpawnBuffer);
        hazards.forEach(hazard -> hazard.update(threatDelta));
        for (Iterator<EnemyProjectile> iterator = enemyProjectiles.iterator(); iterator.hasNext(); ) {
            EnemyProjectile projectile = iterator.next();
            projectile.update(threatDelta);
            if (projectile.shouldBurstIntoInk()) {
                inkVeilTimer = Math.max(inkVeilTimer, 4.5f);
                iterator.remove();
                triggerShake(3f, 0.25f);
            }
        }
        float magnetRange = activeMagnetRange();
        oxygenTanks.forEach(tank -> tank.update(delta, targetX, targetY, magnetRange));
        powerUpPickups.forEach(pickup -> pickup.update(delta, targetX, targetY, magnetRange));

        harpoons.removeIf(Harpoon::isOutOfScreen);
        enemies.removeIf(EnemyFish::isOutOfScreen);
        oxygenTanks.removeIf(OxygenTank::isOutOfScreen);
        hazards.removeIf(EnvironmentalHazard::isOutOfScreen);
        enemyProjectiles.removeIf(EnemyProjectile::isOutOfScreen);
        powerUpPickups.removeIf(PowerUpPickup::isOutOfScreen);

        checkCollisions();
        refreshHighScore();
        if (session.isOutOfOxygen()) {
            endGame(false);
        }
    }

    private void updateDiverBubbles(float delta, boolean swimmingUp) {
        bubbleTimer -= delta;
        if (bubbleTimer > 0f) {
            return;
        }
        particles.spawnSwimBubbles(diver.getX() + 8f, diver.getY() + 32f, swimmingUp ? 2 : 1);
        bubbleTimer = swimmingUp ? 0.09f : 0.42f;
    }

    private void updateTimers(float delta) {
        shootCooldownTimer = Math.max(0f, shootCooldownTimer - delta);
        invulnerabilityTimer = Math.max(0f, invulnerabilityTimer - delta);
        bannerTimer = Math.max(0f, bannerTimer - delta);
        bossWarningTimer = Math.max(0f, bossWarningTimer - delta);
        damageFlashTimer = Math.max(0f, damageFlashTimer - delta);
        inkVeilTimer = Math.max(0f, inkVeilTimer - delta);
        descentMotionTimer = Math.max(0f, descentMotionTimer - delta);
        ambientTime += delta;
        safetyTimer = Math.max(0f, safetyTimer - delta);
        for (PowerUpType type : PowerUpType.values()) {
            float remaining = activePowerUps.getOrDefault(type, 0f);
            if (remaining > 0f) {
                activePowerUps.put(type, Math.max(0f, remaining - delta));
            }
        }

        breathTimer += delta;
        if (breathTimer >= BREATH_INTERVAL) {
            breathTimer -= BREATH_INTERVAL;
            AudioManager.playBreath();
        }

        if (showTutorial) {
            tutorialTimer += delta;
            if (tutorialTimer >= TUTORIAL_DURATION) {
                showTutorial = false;
            }
        }
    }

    private void updateRunProgression(RunDirector.UpdateResult update) {
        GameBalance.Difficulty nextDifficulty = runDirector.difficulty();
        if (update.stageChanged()) {
            difficulty = nextDifficulty;
            enemySpawnTimer = Math.min(enemySpawnTimer, difficulty.spawnInterval());
            background.setDepthStage(difficulty.level(), difficulty.scrollSpeedMultiplier());
            descentMotionTimer = CELEBRATION_DURATION + 1.2f;
            String stageSubtitle = difficulty.level() == GameBalance.stageCount()
                ? "ANY HIT IS FATAL"
                : "DESCENDING TO " + runDirector.displayDepthMeters() + " M";
            beginCelebration(difficulty.name(), stageSubtitle);
        } else {
            difficulty = nextDifficulty;
        }
        if (update.milestoneMeters() > 0) {
            String distance = formatDistance(update.milestoneMeters());
            beginCelebration(distance + " REACHED", "NEW DIVE MILESTONE");
        }
        if (update.finaleReady() && !finalePending) {
            finalePending = true;
            beginCelebration("THE ABYSS OPENS", "COLOSSAL OCTOPUS SIGNAL DETECTED");
        }
    }

    private boolean updateRunUpgradeProgression() {
        if (safetyTimer > 0f
            || session.getScore() < RunUpgradeSchedule.scoreForSelection(nextRunUpgradeIndex)) {
            return false;
        }
        nextRunUpgradeIndex++;
        beginUpgradeSelection();
        return choosingUpgrade;
    }

    private void beginUpgradeSelection() {
        List<UpgradeType> candidates = new ArrayList<>(session.getAvailableUpgrades());
        Collections.shuffle(candidates, random);
        int choiceCount = Math.min(3, candidates.size());
        upgradeChoices = Collections.unmodifiableList(
            new ArrayList<>(candidates.subList(0, choiceCount)));
        selectedUpgradeIndex = 0;
        choosingUpgrade = !upgradeChoices.isEmpty();
        if (!choosingUpgrade) {
            showBanner(difficulty.name());
        }
    }

    private void spawnEntities(float delta) {
        if (!hasActiveBoss() && safetyTimer <= 0f && !finalePending) {
            enemySpawnTimer += delta;
            if (enemySpawnTimer >= difficulty.spawnInterval()) {
                enemySpawnTimer -= difficulty.spawnInterval();
                spawnEncounter();
            }

            hazardSpawnTimer += delta;
            if (hazardSpawnTimer >= difficulty.hazardInterval()) {
                hazardSpawnTimer -= difficulty.hazardInterval();
                spawnHazard();
            }
        }

        oxygenTankSpawnTimer += delta;
        if (oxygenTankSpawnTimer >= OXYGEN_TANK_SPAWN_INTERVAL) {
            oxygenTankSpawnTimer -= OXYGEN_TANK_SPAWN_INTERVAL;
            oxygenTanks.add(new OxygenTank(GameConfig.WORLD_WIDTH, randomY()));
        }

        powerUpSpawnTimer += delta;
        if (!hasActiveBoss() && powerUpSpawnTimer >= POWER_UP_SPAWN_INTERVAL) {
            powerUpSpawnTimer -= POWER_UP_SPAWN_INTERVAL;
            spawnPowerUp();
        }
    }

    private void shootIfRequested() {
        boolean shootPressed = game.input().shootJustPressed()
            || game.input().pointerJustPressed(TOUCH_FIRE_BUTTON)
            || CAPTURE_AUTOPLAY;
        boolean overdrive = isPowerActive(PowerUpType.HARPOON_OVERDRIVE);
        if (!shootPressed || shootCooldownTimer > 0f
            || harpoons.size() >= GameBalance.maxActiveHarpoons(overdrive)) {
            return;
        }

        harpoons.add(new Harpoon(diver.getX() + 50f, diver.getY() + 26f,
            session.getHarpoonHitCount() + progression.startingHarpoonBonus()
                + (overdrive ? 2 : 0)));
        shootCooldownTimer = GameBalance.harpoonCooldown(
            session.getShootCooldownMultiplier(), overdrive) * equippedSuit.reloadMultiplier();
        particles.spawnImpact(diver.getX() + 56f, diver.getY() + 30f, false);
        AudioManager.playShoot();
    }

    private void spawnEncounter() {
        float y = randomY();
        int roll = random.nextInt(100);
        switch (difficulty.level()) {
            case 1 -> addEnemy(new SmallFish(y));
            case 2 -> {
                if (roll < 30) {
                    addEnemy(new ElectricEel(y));
                } else {
                    addEnemy(roll < 68 ? new SmallFish(y) : new FastFish(y));
                }
                if (roll < 22) {
                    hazards.add(new FallingDebris(GameConfig.WORLD_WIDTH - 90f,
                        RunDirector.BASE_SCROLL_SPEED_WORLD_UNITS));
                }
            }
            case 3 -> {
                if (roll < 25) {
                    addEnemy(new ElectricEel(y));
                    addEnemy(new SmallFish(oppositeLane(y)));
                } else if (roll < 55) {
                    addEnemy(new PiranhaSwarm(y));
                } else {
                    addEnemy(new FastFish(y));
                    addEnemy(new SmallFish(oppositeLane(y)));
                }
            }
            case 4 -> {
                if (roll < 32) {
                    addEnemy(new Shark(y));
                    addEnemy(new SmallFish(oppositeLane(y)));
                } else if (roll < 63) {
                    addEnemy(new ElectricEel(y));
                    addEnemy(new FastFish(oppositeLane(y)));
                } else {
                    addEnemy(new PiranhaSwarm(y));
                }
            }
            default -> {
                if (roll < 35) {
                    addEnemy(new Shark(y));
                    addEnemy(new ElectricEel(oppositeLane(y)));
                } else if (roll < 68) {
                    addEnemy(new PiranhaSwarm(y));
                    addEnemy(new FastFish(oppositeLane(y)));
                } else {
                    addEnemy(new ElectricEel(y));
                    addEnemy(new FastFish(MathUtils.clamp(y + 145f, 90f, 610f)));
                    addEnemy(new SmallFish(MathUtils.clamp(y - 145f, 90f, 610f)));
                }
            }
        }
    }

    private void addEnemy(EnemyFish enemy) {
        enemy.setSpeedMultiplier(difficulty.enemySpeedMultiplier());
        enemies.add(enemy);
    }

    private void spawnHazard() {
        float speed = RunDirector.BASE_SCROLL_SPEED_WORLD_UNITS
            * difficulty.scrollSpeedMultiplier();
        int roll = random.nextInt(100);
        EnvironmentalHazard hazard = switch (difficulty.level()) {
            case 1 -> new ReefBarrier(random.nextBoolean(), MathUtils.random(115f, 195f), speed);
            case 2 -> roll < 55
                ? new ReefBarrier(random.nextBoolean(), MathUtils.random(130f, 220f), speed)
                : new FallingDebris(MathUtils.random(900f, 1200f), speed);
            case 3 -> roll < 35 ? new SeaMine(randomY(), speed)
                : roll < 70 ? new FallingDebris(MathUtils.random(820f, 1180f), speed)
                : new ReefBarrier(random.nextBoolean(), MathUtils.random(150f, 230f), speed);
            case 4 -> roll < 35 ? new CrushingGate(randomY(), speed)
                : roll < 65 ? new ThermalVent(speed)
                : new SeaMine(randomY(), speed);
            default -> roll < 30 ? new CrushingGate(randomY(), speed)
                : roll < 55 ? new ThermalVent(speed)
                : roll < 78 ? new FallingDebris(MathUtils.random(760f, 1150f), speed)
                : new SeaMine(randomY(), speed);
        };
        hazards.add(hazard);
    }

    private void spawnPowerUp() {
        PowerUpType type = switch (difficulty.level()) {
            case 1 -> PowerUpType.TORPEDO_DASH;
            case 2 -> random.nextBoolean() ? PowerUpType.PRESSURE_SHIELD : PowerUpType.TORPEDO_DASH;
            case 3 -> switch (random.nextInt(3)) {
                case 0 -> PowerUpType.PRESSURE_SHIELD;
                case 1 -> PowerUpType.TIME_BUBBLE;
                default -> PowerUpType.MAGNETIC_CURRENT;
            };
            default -> PowerUpType.values()[random.nextInt(PowerUpType.values().length)];
        };
        powerUpPickups.add(new PowerUpPickup(type, GameConfig.WORLD_WIDTH, randomY()));
    }

    private float oppositeLane(float y) {
        return y < GameConfig.WORLD_HEIGHT / 2f
            ? MathUtils.clamp(y + 245f, 90f, 610f)
            : MathUtils.clamp(y - 245f, 90f, 610f);
    }

    private void spawnBoss() {
        bossSpawned = true;
        bossWarningTimer = BOSS_WARNING_DURATION;
        for (EnemyFish enemy : enemies) {
            Rectangle bounds = enemy.getBounds();
            particles.spawnExplosion(bounds.x + bounds.width / 2f,
                bounds.y + bounds.height / 2f, 0.55f);
        }
        enemies.clear();
        harpoons.clear();
        hazards.clear();
        enemyProjectiles.clear();
        enemies.add(new AbyssalOctopus());
        triggerShake(13f, 0.65f);
        AudioManager.playBreath();
    }

    private void checkCollisions() {
        checkHarpoonCollisions();
        checkOxygenCollisions();
        checkPowerUpCollisions();
        checkDiverCollisions();
    }

    private void checkHarpoonCollisions() {
        for (Iterator<Harpoon> harpoonIterator = harpoons.iterator(); harpoonIterator.hasNext(); ) {
            Harpoon harpoon = harpoonIterator.next();
            for (Iterator<EnemyFish> enemyIterator = enemies.iterator(); enemyIterator.hasNext(); ) {
                EnemyFish enemy = enemyIterator.next();
                if (!harpoon.canHit(enemy) || !harpoon.getBounds().overlaps(enemy.getBounds())) {
                    continue;
                }

                if (harpoon.registerHit(enemy)) {
                    harpoonIterator.remove();
                }
                enemy.hit();
                Rectangle bounds = enemy.getBounds();
                float impactX = Math.max(bounds.x, harpoon.getX());
                float impactY = harpoon.getY() + 5f;
                particles.spawnImpact(impactX, impactY, enemy.isBoss());
                triggerHitFeedback(enemy.isBoss() ? 7f : 3.5f, enemy.isBoss() ? 0.055f : 0.028f);
                AudioManager.playHit();

                if (enemy.isDead()) {
                    enemyIterator.remove();
                    session.awardKill(enemy.getScoreValue());
                    particles.spawnExplosion(bounds.x + bounds.width / 2f,
                        bounds.y + bounds.height / 2f, enemy.isBoss() ? 1.8f : 0.75f);
                    triggerHitFeedback(enemy.isBoss() ? 18f : 7f, enemy.isBoss() ? 0.18f : 0.065f);
                    if (enemy.isBoss()) {
                        endGame(true);
                    }
                }
                break;
            }
        }
    }

    private void checkOxygenCollisions() {
        for (Iterator<OxygenTank> iterator = oxygenTanks.iterator(); iterator.hasNext(); ) {
            OxygenTank tank = iterator.next();
            if (tank.getBounds().overlaps(diver.getBounds())) {
                session.collectOxygen(BASE_OXYGEN_PICKUP + session.getOxygenPickupBonus());
                Rectangle bounds = tank.getBounds();
                particles.spawnOxygenBurst(bounds.x + bounds.width / 2f,
                    bounds.y + bounds.height / 2f);
                iterator.remove();
                AudioManager.playOxygen();
            }
        }
    }

    private void checkDiverCollisions() {
        if (invulnerabilityTimer > 0f) {
            enemyProjectiles.removeIf(projectile -> projectile.bounds().overlaps(diver.getBounds()));
            return;
        }

        if (diver.isTouchingBottom()) {
            damageDiver(SEABED_CONTACT_DAMAGE, false);
            return;
        }

        for (Iterator<EnemyProjectile> iterator = enemyProjectiles.iterator(); iterator.hasNext(); ) {
            EnemyProjectile projectile = iterator.next();
            if (projectile.bounds().overlaps(diver.getBounds())) {
                iterator.remove();
                damageDiver(projectile.damage(), false);
                return;
            }
        }

        for (Iterator<EnvironmentalHazard> iterator = hazards.iterator(); iterator.hasNext(); ) {
            EnvironmentalHazard hazard = iterator.next();
            if (hazard.collides(diver.getBounds())) {
                if (hazard.removeOnCollision()) {
                    iterator.remove();
                }
                damageDiver(hazard.collisionDamage(), false);
                return;
            }
        }

        for (Iterator<EnemyFish> iterator = enemies.iterator(); iterator.hasNext(); ) {
            EnemyFish enemy = iterator.next();
            if (!enemy.getBounds().overlaps(diver.getBounds())) {
                continue;
            }

            if (enemy.removeOnPlayerCollision()) {
                iterator.remove();
            }
            damageDiver(enemy.getCollisionDamage(), enemy.isBoss());
            break;
        }
    }

    private void checkPowerUpCollisions() {
        for (Iterator<PowerUpPickup> iterator = powerUpPickups.iterator(); iterator.hasNext(); ) {
            PowerUpPickup pickup = iterator.next();
            if (!pickup.bounds().overlaps(diver.getBounds())) {
                continue;
            }
            iterator.remove();
            activatePowerUp(pickup.type());
        }
    }

    private void activatePowerUp(PowerUpType type) {
        if (!type.activatesOnPickup()) {
            dashCharges = Math.min(MAX_DASH_CHARGES, dashCharges + 1);
        } else {
            activePowerUps.put(type, type.duration());
        }
        if (type == PowerUpType.PRESSURE_SHIELD) {
            invulnerabilityTimer = Math.max(invulnerabilityTimer, type.duration());
        }
        Rectangle bounds = diver.getBounds();
        particles.spawnOxygenBurst(bounds.x + bounds.width / 2f, bounds.y + bounds.height / 2f);
        if (safetyTimer <= 0f) {
            showPowerUpBanner(type);
        }
        AudioManager.playConfirm();
    }

    private void useDashCharge() {
        dashCharges--;
        PowerUpType type = PowerUpType.TORPEDO_DASH;
        activePowerUps.put(type, type.duration());
        invulnerabilityTimer = Math.max(invulnerabilityTimer, type.duration());
        enemies.removeIf(enemy -> !enemy.isBoss());
        hazards.clear();
        enemyProjectiles.clear();
        Rectangle bounds = diver.getBounds();
        particles.spawnOxygenBurst(bounds.x + bounds.width / 2f,
            bounds.y + bounds.height / 2f);
        triggerShake(11f, 0.4f);
        showBanner("TORPEDO DASH!");
        game.input().vibrateController(120, 0.7f);
        AudioManager.playConfirm();
    }

    private void damageDiver(float damage, boolean bossHit) {
        if (damage <= 0f) {
            return;
        }
        session.takeDamage(GameBalance.playerDamageForStage(
            difficulty.level(), damage, session.getOxygen()));
        invulnerabilityTimer = INVULNERABILITY_DURATION;
        damageFlashTimer = 0.25f;
        Rectangle diverBounds = diver.getBounds();
        particles.spawnImpact(diverBounds.x + diverBounds.width / 2f,
            diverBounds.y + diverBounds.height / 2f, bossHit);
        triggerHitFeedback(bossHit ? 14f : 9f, 0.075f);
        AudioManager.playHit();
    }

    private void triggerHitFeedback(float shake, float hitStop) {
        triggerShake(shake, Math.min(0.5f, 0.12f + shake * 0.018f));
        hitStopTimer = Math.max(hitStopTimer, hitStop);
        game.input().vibrateController(Math.round(28f + shake * 4f),
            MathUtils.clamp(0.18f + shake * 0.035f, 0f, 1f));
    }

    private void triggerShake(float magnitude, float duration) {
        if (!DisplaySettings.screenShakeEnabled()) {
            return;
        }
        shakeMagnitude = Math.max(shakeMagnitude, magnitude);
        shakeTimer = Math.max(shakeTimer, duration);
    }

    private void updateWorldCamera(float delta) {
        float centerX = GameConfig.WORLD_WIDTH / 2f;
        float centerY = GameConfig.WORLD_HEIGHT / 2f;
        if (shakeTimer > 0f && DisplaySettings.screenShakeEnabled()) {
            shakeTimer = Math.max(0f, shakeTimer - delta);
            float falloff = Math.min(1f, shakeTimer * 5f);
            camera.position.set(centerX + MathUtils.random(-shakeMagnitude, shakeMagnitude) * falloff,
                centerY + MathUtils.random(-shakeMagnitude, shakeMagnitude) * falloff, 0f);
            if (shakeTimer == 0f) {
                shakeMagnitude = 0f;
            }
        } else {
            shakeTimer = 0f;
            shakeMagnitude = 0f;
            camera.position.set(centerX, centerY, 0f);
        }
    }

    private void endGame(boolean won) {
        if (gameOver) {
            return;
        }
        victory = won;
        gameOver = true;
        paused = false;
        choosingUpgrade = false;
        if (!progressionRewardGranted) {
            earnedPearls = progression.awardDistance(runDirector.distanceMeters());
            progressionRewardGranted = true;
        }
        refreshHighScore();
        saveHighScore();
        if (won) {
            AudioManager.playConfirm();
        } else {
            AudioManager.playGameOver();
        }
    }

    private void refreshHighScore() {
        int currentScore = session.getDisplayScore();
        if (currentScore > highScore) {
            highScore = currentScore;
            highScoreDirty = true;
        }
    }

    private void saveHighScore() {
        if (!highScoreDirty) {
            return;
        }
        preferences.putInteger("highScore", highScore);
        preferences.flush();
        highScoreDirty = false;
    }

    private void showBanner(String message) {
        bannerText = message;
        bannerSubtitle = "";
        bannerTimer = BANNER_DURATION;
    }

    private void showPowerUpBanner(PowerUpType type) {
        bannerText = type.title() + (type.activatesOnPickup() ? " ACTIVE" : " READY");
        bannerSubtitle = type == PowerUpType.TORPEDO_DASH
            ? type.usageHint() + "  |  " + dashInstruction()
            : type.usageHint() + " FOR " + Math.round(type.duration()) + "s";
        bannerTimer = POWER_UP_BANNER_DURATION;
    }

    private void beginCelebration(String title, String subtitle) {
        bannerText = title;
        bannerSubtitle = subtitle;
        bannerTimer = CELEBRATION_DURATION;
        safetyTimer = CELEBRATION_DURATION;
        enemies.removeIf(enemy -> !enemy.isBoss());
        hazards.clear();
        enemyProjectiles.clear();
        triggerShake(4f, 0.3f);
        AudioManager.playConfirm();
    }

    private boolean isPowerActive(PowerUpType type) {
        return activePowerUps.getOrDefault(type, 0f) > 0f;
    }

    private float activeMagnetRange() {
        float range = BASE_MAGNET_RANGE + progression.magnetBonusRange()
            + equippedSuit.magnetBonusRange();
        if (isPowerActive(PowerUpType.MAGNETIC_CURRENT)) {
            range += 260f;
        }
        return range;
    }

    private String activePowerText() {
        for (PowerUpType type : PowerUpType.values()) {
            float remaining = activePowerUps.getOrDefault(type, 0f);
            if (remaining > 0f) {
                return type.title() + "  " + String.format(Locale.ROOT, "%.1fs", remaining);
            }
        }
        return "";
    }

    private String dashInstruction() {
        if (game.input().usingController()) {
            return "PRESS [LB] TO DASH";
        }
        if (game.input().usingTouch()) {
            return "TAP THE DASH BUTTON";
        }
        return "PRESS [C] TO DASH";
    }

    private String dashStatusText() {
        return "DASH x" + dashCharges + " READY  |  " + dashInstruction();
    }

    private static String formatDistance(int meters) {
        if (meters >= 1000) {
            return meters % 1000 == 0
                ? (meters / 1000) + " KM"
                : String.format(Locale.ROOT, "%.1f KM", meters / 1000f);
        }
        return meters + " M";
    }

    private float randomY() {
        float margin = 85f;
        return margin + random.nextFloat() * (GameConfig.WORLD_HEIGHT - margin * 2f);
    }

    private boolean hasActiveBoss() {
        return activeBoss() != null;
    }

    private EnemyFish activeBoss() {
        for (EnemyFish enemy : enemies) {
            if (enemy.isBoss()) {
                return enemy;
            }
        }
        return null;
    }

    private void drawWorld() {
        batch.begin();
        background.render(batch);
        oxygenTanks.forEach(tank -> tank.render(batch));
        enemies.forEach(enemy -> enemy.render(batch));
        harpoons.forEach(harpoon -> harpoon.render(batch));
        diver.render(batch, invulnerabilityTimer);
        batch.end();
    }

    private void drawWorldEffects() {
        beginFilledShapes();
        drawDepthAtmosphere();
        hazards.forEach(hazard -> hazard.render(shapeRenderer));
        enemyProjectiles.forEach(projectile -> projectile.render(shapeRenderer));
        powerUpPickups.forEach(pickup -> pickup.render(shapeRenderer));
        for (EnemyFish enemy : enemies) {
            if (enemy.isTelegraphing()) {
                Rectangle bounds = enemy.getBounds();
                float alpha = DisplaySettings.flashEffectsEnabled()
                    ? 0.3f + MathUtils.sin(Gdx.graphics.getFrameId() * 0.35f) * 0.18f
                    : 0.22f;
                shapeRenderer.setColor(1f, 0.12f, 0.08f, alpha);
                shapeRenderer.rect(0f, bounds.y + bounds.height / 2f - 2f,
                    Math.max(0f, bounds.x), 4f);
            }
        }
        if (isPowerActive(PowerUpType.PRESSURE_SHIELD)
            || isPowerActive(PowerUpType.TORPEDO_DASH)) {
            Rectangle bounds = diver.getBounds();
            float pulse = 39f + MathUtils.sin(Gdx.graphics.getFrameId() * 0.14f) * 4f;
            shapeRenderer.setColor(0.25f, 0.9f, 1f, 0.28f);
            shapeRenderer.circle(bounds.x + bounds.width / 2f,
                bounds.y + bounds.height / 2f, pulse, 24);
        }
        particles.render(shapeRenderer);
        drawInkVeil();
        endShapes();
    }

    private void drawDepthAtmosphere() {
        int stage = difficulty.level();
        float darkness = (stage - 1) * 0.055f;
        if (darkness > 0f) {
            shapeRenderer.setColor(0.005f, 0.015f, 0.075f, darkness);
            shapeRenderer.rect(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        }

        int moteCount = 7 + stage * 4;
        float riseSpeed = 28f + stage * 13f;
        for (int i = 0; i < moteCount; i++) {
            float x = (i * 193f + 47f) % GameConfig.WORLD_WIDTH;
            float y = (i * 109f + ambientTime * riseSpeed)
                % (GameConfig.WORLD_HEIGHT + 40f) - 20f;
            float radius = 1.5f + (i % 3);
            shapeRenderer.setColor(0.25f, 0.72f, 0.82f, 0.1f + stage * 0.025f);
            shapeRenderer.circle(x, y, radius, 8);
        }

        if (descentMotionTimer > 0f) {
            float alpha = Math.min(0.46f, descentMotionTimer * 0.16f);
            for (int i = 0; i < 14; i++) {
                float x = (i * 101f + 35f) % GameConfig.WORLD_WIDTH;
                float y = (i * 71f + ambientTime * 260f)
                    % (GameConfig.WORLD_HEIGHT + 120f) - 60f;
                shapeRenderer.setColor(0.38f, 0.82f, 0.94f, alpha);
                shapeRenderer.rect(x, y, 2.5f, 58f + (i % 4) * 17f);
            }
        }
    }

    private void drawInkVeil() {
        if (inkVeilTimer <= 0f) {
            return;
        }
        float alpha = Math.min(0.78f, 0.34f + inkVeilTimer * 0.11f);
        shapeRenderer.setColor(0.012f, 0.002f, 0.025f, alpha);
        shapeRenderer.rect(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        for (int i = 0; i < 11; i++) {
            float x = (i * 157f + 83f) % GameConfig.WORLD_WIDTH;
            float y = (i * 211f + 61f) % GameConfig.WORLD_HEIGHT;
            float radius = 95f + (i % 4) * 34f
                + MathUtils.sin(ambientTime * 1.8f + i) * 16f;
            shapeRenderer.setColor(0.055f, 0.005f, 0.09f, alpha * 0.72f);
            shapeRenderer.circle(x, y, radius, 24);
        }
    }

    private void drawUiShapes() {
        float oxygenRatio = session.getOxygenRatio();
        EnemyFish boss = activeBoss();

        beginFilledShapes();
        shapeRenderer.setColor(0.01f, 0.04f, 0.09f, 0.78f);
        shapeRenderer.rect(16f, 616f, 300f, 88f);
        shapeRenderer.rect(958f, 548f, 306f, 156f);

        shapeRenderer.setColor(0.12f, 0.15f, 0.18f, 0.95f);
        shapeRenderer.rect(24f, 626f, 276f, 22f);
        float red = Math.min(1f, (1f - oxygenRatio) * 2f);
        float green = Math.min(1f, oxygenRatio * 2f);
        shapeRenderer.setColor(red, green, 0.08f, 1f);
        shapeRenderer.rect(24f, 626f, 276f * oxygenRatio, 22f);

        shapeRenderer.setColor(0.08f, 0.13f, 0.2f, 0.95f);
        shapeRenderer.rect(974f, 558f, 274f, 7f);
        shapeRenderer.setColor(0.16f, 0.78f, 0.92f, 1f);
        shapeRenderer.rect(974f, 558f, 274f * runDirector.stageProgress(), 7f);

        if (boss != null) {
            shapeRenderer.setColor(0.01f, 0.02f, 0.08f, 0.9f);
            shapeRenderer.rect(385f, 650f, 510f, 48f);
            shapeRenderer.setColor(0.2f, 0.05f, 0.25f, 1f);
            shapeRenderer.rect(405f, 660f, 470f, 16f);
            shapeRenderer.setColor(0.75f, 0.16f, 0.95f, 1f);
            shapeRenderer.rect(405f, 660f, 470f * boss.getHealthRatio(), 16f);
        }

        if (oxygenRatio < 0.25f && !gameOver && DisplaySettings.flashEffectsEnabled()) {
            float pulse = 0.04f + (0.25f - oxygenRatio) * 0.35f;
            shapeRenderer.setColor(0.85f, 0.03f, 0.02f, pulse);
            shapeRenderer.rect(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        }
        if (damageFlashTimer > 0f && DisplaySettings.flashEffectsEnabled()) {
            shapeRenderer.setColor(0.95f, 0.05f, 0.03f, damageFlashTimer * 0.8f);
            shapeRenderer.rect(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        }

        boolean touchControlsVisible = game.input().isMobile()
            && !paused && !gameOver && !choosingUpgrade;
        if (touchControlsVisible) {
            shapeRenderer.setColor(0.02f, 0.12f, 0.2f,
                game.input().pointerPressed(TOUCH_SWIM_BUTTON) ? 0.92f : 0.72f);
            shapeRenderer.rect(TOUCH_SWIM_BUTTON.x, TOUCH_SWIM_BUTTON.y,
                TOUCH_SWIM_BUTTON.width, TOUCH_SWIM_BUTTON.height);
            shapeRenderer.setColor(0.16f, 0.24f, 0.3f,
                game.input().pointerPressed(TOUCH_FIRE_BUTTON) ? 0.96f : 0.76f);
            shapeRenderer.rect(TOUCH_FIRE_BUTTON.x, TOUCH_FIRE_BUTTON.y,
                TOUCH_FIRE_BUTTON.width, TOUCH_FIRE_BUTTON.height);
            if (dashCharges > 0) {
                shapeRenderer.setColor(0.08f, 0.38f, 0.28f,
                    game.input().pointerPressed(TOUCH_DASH_BUTTON) ? 0.98f : 0.82f);
                shapeRenderer.rect(TOUCH_DASH_BUTTON.x, TOUCH_DASH_BUTTON.y,
                    TOUCH_DASH_BUTTON.width, TOUCH_DASH_BUTTON.height);
            }
            shapeRenderer.setColor(0.01f, 0.08f, 0.15f, 0.8f);
            shapeRenderer.rect(TOUCH_PAUSE_BUTTON.x, TOUCH_PAUSE_BUTTON.y,
                TOUCH_PAUSE_BUTTON.width, TOUCH_PAUSE_BUTTON.height);
        }

        if (showTutorial && !paused && !gameOver && !choosingUpgrade) {
            shapeRenderer.setColor(0.01f, 0.04f, 0.09f, 0.84f);
            float tutorialY = game.input().isMobile() ? tutorialPanelY() : 18f;
            shapeRenderer.rect(220f, tutorialY, 840f, 78f);
        }

        if (bannerTimer > 0f && !bannerSubtitle.isEmpty()
            && !paused && !gameOver && !choosingUpgrade) {
            float alpha = Math.min(0.94f, bannerTimer * 1.4f);
            shapeRenderer.setColor(0.005f, 0.035f, 0.09f, alpha);
            shapeRenderer.rect(310f, 432f, 660f, 128f);
            shapeRenderer.setColor(0.12f, 0.8f, 0.95f, alpha);
            shapeRenderer.rect(310f, 552f, 660f, 8f);
            shapeRenderer.rect(310f, 432f, 660f, 4f);
        }

        if (choosingUpgrade) {
            drawUpgradeShapes();
        } else if (paused || gameOver) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.62f);
            shapeRenderer.rect(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
            shapeRenderer.setColor(0.01f, 0.04f, 0.09f, 0.96f);
            shapeRenderer.rect(365f, 175f, 550f, 370f);
            shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 0.9f);
            shapeRenderer.rect(365f, 540f, 550f, 5f);
            if (paused) {
                pauseRowBounds(selectedPauseIndex);
                shapeRenderer.setColor(0.06f, 0.22f, 0.29f, 0.85f);
                shapeRenderer.rect(interactiveRow.x, interactiveRow.y,
                    interactiveRow.width, interactiveRow.height);
            } else {
                shapeRenderer.setColor(0.04f, 0.21f, 0.28f,
                    game.input().pointerOver(GAME_OVER_RETRY_BUTTON) ? 0.95f : 0.75f);
                shapeRenderer.rect(GAME_OVER_RETRY_BUTTON.x, GAME_OVER_RETRY_BUTTON.y,
                    GAME_OVER_RETRY_BUTTON.width, GAME_OVER_RETRY_BUTTON.height);
                shapeRenderer.setColor(0.02f, 0.1f, 0.17f,
                    game.input().pointerOver(GAME_OVER_MENU_BUTTON) ? 0.95f : 0.72f);
                shapeRenderer.rect(GAME_OVER_MENU_BUTTON.x, GAME_OVER_MENU_BUTTON.y,
                    GAME_OVER_MENU_BUTTON.width, GAME_OVER_MENU_BUTTON.height);
            }
        }
        endShapes();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(24f, 626f, 276f, 22f);
        if (boss != null) {
            shapeRenderer.rect(405f, 660f, 470f, 16f);
        }
        if (choosingUpgrade) {
            for (int i = 0; i < upgradeChoices.size(); i++) {
                float x = upgradeCardX(i);
                shapeRenderer.setColor(i == selectedUpgradeIndex ? Color.YELLOW : Color.CYAN);
                shapeRenderer.rect(x, 170f, 350f, 360f);
            }
        }
        if (touchControlsVisible) {
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(TOUCH_SWIM_BUTTON.x, TOUCH_SWIM_BUTTON.y,
                TOUCH_SWIM_BUTTON.width, TOUCH_SWIM_BUTTON.height);
            shapeRenderer.setColor(Color.LIGHT_GRAY);
            shapeRenderer.rect(TOUCH_FIRE_BUTTON.x, TOUCH_FIRE_BUTTON.y,
                TOUCH_FIRE_BUTTON.width, TOUCH_FIRE_BUTTON.height);
            if (dashCharges > 0) {
                shapeRenderer.setColor(Color.LIME);
                shapeRenderer.rect(TOUCH_DASH_BUTTON.x, TOUCH_DASH_BUTTON.y,
                    TOUCH_DASH_BUTTON.width, TOUCH_DASH_BUTTON.height);
            }
            shapeRenderer.setColor(Color.LIGHT_GRAY);
            shapeRenderer.rect(TOUCH_PAUSE_BUTTON.x, TOUCH_PAUSE_BUTTON.y,
                TOUCH_PAUSE_BUTTON.width, TOUCH_PAUSE_BUTTON.height);
        }
        shapeRenderer.end();
    }

    private void drawUpgradeShapes() {
        shapeRenderer.setColor(0f, 0f, 0f, 0.78f);
        shapeRenderer.rect(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        for (int i = 0; i < upgradeChoices.size(); i++) {
            float x = upgradeCardX(i);
            if (i == selectedUpgradeIndex) {
                shapeRenderer.setColor(0.04f, 0.23f, 0.31f, 0.98f);
            } else {
                shapeRenderer.setColor(0.01f, 0.06f, 0.12f, 0.95f);
            }
            shapeRenderer.rect(x, 170f, 350f, 360f);
            shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 0.9f);
            shapeRenderer.rect(x, 525f, 350f, 5f);
        }
    }

    private void drawUiText() {
        batch.begin();

        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, "SCORE  " + session.getDisplayScore(), 26f, 694f);
        smallFont.draw(batch, "BEST   " + highScore, 26f, 668f);
        smallFont.draw(batch, "OXYGEN " + Math.round(session.getOxygen()) + " / "
            + Math.round(session.getMaxOxygen()), 54f, 645f);
        smallFont.draw(batch, difficulty.name(), 976f, 692f);
        smallFont.setColor(suitUiColor());
        smallFont.draw(batch, equippedSuit.title(), 976f, 666f);
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, "BELOW  " + runDirector.displayDepthMeters() + " M", 976f, 640f);
        smallFont.draw(batch, "DIST  " + formatDistance(runDirector.displayMeters()), 976f, 615f);
        smallFont.draw(batch, "NEXT  " + formatDistance(runDirector.nextMilestoneMeters()), 976f, 590f);

        float powerStatusY = 600f;
        if (dashCharges > 0) {
            smallFont.setColor(Color.LIME);
            smallFont.draw(batch, dashStatusText(), 24f, powerStatusY);
            powerStatusY -= 26f;
        }
        String activePower = activePowerText();
        if (!activePower.isEmpty()) {
            smallFont.setColor(Color.GOLD);
            smallFont.draw(batch, activePower, 24f, powerStatusY);
            powerStatusY -= 26f;
        }
        if (difficulty.level() == GameBalance.stageCount() && !gameOver) {
            smallFont.setColor(Color.SCARLET);
            smallFont.draw(batch, "ANY HIT IS FATAL", 24f, powerStatusY);
        }

        EnemyFish boss = activeBoss();
        if (boss != null) {
            drawCentered(smallFont, "ABYSSAL OCTOPUS", 695f, BOSS_TEXT_COLOR);
        }

        if (session.getCombo() > 1) {
            mediumFont.setColor(Color.YELLOW);
            mediumFont.draw(batch, String.format(Locale.ROOT, "COMBO x%.2f",
                session.getComboMultiplier()), 1000f, 542f);
        }

        if (showTutorial && !paused && !gameOver && !choosingUpgrade) {
            if (game.input().usingController()) {
                drawCentered(smallFont, "[A] / STICK UP SWIM  |  [X / B / RB] FIRE", 76f, Color.WHITE);
                drawCentered(smallFont, "AVOID THE SEABED  |  [LB] DASH  |  [START] PAUSE  |  [Y] HELP",
                    47f, Color.LIGHT_GRAY);
            } else if (game.input().usingTouch()) {
                float tutorialY = tutorialPanelY();
                drawCentered(smallFont, "HOLD SWIM TO RISE  |  TAP FIRE TO SHOOT",
                    tutorialY + 58f, Color.WHITE);
                drawCentered(smallFont, "AVOID THE SEABED  |  TAP DASH  |  PAUSE IS AT THE TOP RIGHT",
                    tutorialY + 29f, Color.LIGHT_GRAY);
            } else {
                drawCentered(smallFont, "SPACE / W / UP OR LEFT MOUSE TO SWIM  |  Z / X OR RIGHT MOUSE FIRE",
                    76f, Color.WHITE);
                drawCentered(smallFont, "AVOID THE SEABED  |  [C] DASH  |  [P / ESC] PAUSE  |  [T] HELP",
                    47f, Color.LIGHT_GRAY);
            }
        }

        if (game.input().isMobile() && !paused && !gameOver && !choosingUpgrade) {
            drawCenteredAt(mediumFont, "SWIM", TOUCH_SWIM_BUTTON.x + TOUCH_SWIM_BUTTON.width / 2f,
                TOUCH_SWIM_BUTTON.y + 74f, Color.WHITE);
            drawCenteredAt(mediumFont, "FIRE", TOUCH_FIRE_BUTTON.x + TOUCH_FIRE_BUTTON.width / 2f,
                TOUCH_FIRE_BUTTON.y + 74f, Color.WHITE);
            if (dashCharges > 0) {
                drawCenteredAt(mediumFont, "DASH x" + dashCharges,
                    TOUCH_DASH_BUTTON.x + TOUCH_DASH_BUTTON.width / 2f,
                    TOUCH_DASH_BUTTON.y + 52f, Color.WHITE);
            }
            drawCenteredAt(smallFont, "II", TOUCH_PAUSE_BUTTON.x + TOUCH_PAUSE_BUTTON.width / 2f,
                TOUCH_PAUSE_BUTTON.y + 44f, Color.WHITE);
        }

        if (bannerTimer > 0f && !paused && !gameOver && !choosingUpgrade) {
            float alpha = Math.min(1f, bannerTimer * 1.5f);
            if (bannerSubtitle.isEmpty()) {
                mediumFont.setColor(0.45f, 0.92f, 1f, alpha);
                drawCenteredWithCurrentColor(mediumFont, bannerText, 535f);
            } else {
                BitmapFont bannerTitleFont = bannerText.length() > 20 ? mediumFont : largeFont;
                bannerTitleFont.setColor(0.45f, 0.92f, 1f, alpha);
                drawCenteredWithCurrentColor(bannerTitleFont, bannerText, 520f);
                smallFont.setColor(1f, 1f, 1f, alpha);
                drawCenteredWithCurrentColor(smallFont, bannerSubtitle, 470f);
            }
        }
        if (bossWarningTimer > 0f && !gameOver) {
            float alpha = DisplaySettings.flashEffectsEnabled()
                ? 0.65f + MathUtils.sin(bossWarningTimer * 18f) * 0.35f
                : 1f;
            largeFont.setColor(1f, 0.2f, 0.18f, alpha);
            drawCenteredWithCurrentColor(largeFont, "ABYSSAL OCTOPUS AWAKENS", 560f);
        }

        if (choosingUpgrade) {
            drawUpgradeSelection();
        } else if (paused) {
            drawPauseMenu();
        } else if (gameOver) {
            drawGameOverScreen();
        }

        batch.end();
    }

    private void drawUpgradeSelection() {
        drawCentered(largeFont, "CHOOSE AN UPGRADE", 625f, Color.WHITE);
        String hint = game.input().usingController()
            ? "GAMEPAD  D-PAD CHOOSE  |  [A] INSTALL"
            : game.input().usingTouch()
            ? "TAP A CARD TO INSTALL"
            : "KEYBOARD  ARROWS / WASD OR 1-3 CHOOSE  |  [ENTER / SPACE] INSTALL";
        drawCentered(smallFont, hint, 580f, Color.LIGHT_GRAY);

        for (int i = 0; i < upgradeChoices.size(); i++) {
            UpgradeType type = upgradeChoices.get(i);
            float centerX = 260f + i * 380f;
            Color color = i == selectedUpgradeIndex ? Color.YELLOW : Color.WHITE;
            drawCenteredAt(mediumFont, type.title(), centerX, 465f, color);
            drawCenteredAt(smallFont, type.description(), centerX, 382f, Color.LIGHT_GRAY);
            int currentLevel = session.getUpgradeLevel(type);
            drawCenteredAt(smallFont, "LEVEL " + currentLevel + "  >  " + (currentLevel + 1),
                centerX, 300f, UPGRADE_TEXT_COLOR);
            drawCenteredAt(largeFont, Integer.toString(i + 1), centerX, 225f,
                i == selectedUpgradeIndex ? Color.YELLOW : Color.DARK_GRAY);
        }
    }

    private void drawPauseMenu() {
        drawCentered(largeFont, "PAUSED", 485f, Color.WHITE);
        float startY = 385f;
        for (int i = 0; i < PAUSE_OPTIONS.length; i++) {
            Color color = i == selectedPauseIndex ? Color.YELLOW : Color.LIGHT_GRAY;
            String prefix = i == selectedPauseIndex ? ">  " : "   ";
            drawCentered(mediumFont, prefix + PAUSE_OPTIONS[i], startY - i * 62f, color);
        }
        String hint = game.input().usingController()
            ? "GAMEPAD  D-PAD CHOOSE  |  [A] SELECT  |  [B / START] RESUME"
            : game.input().usingTouch() ? "TAP AN OPTION"
            : "KEYBOARD  ARROWS / W-S CHOOSE  |  [ENTER / SPACE] SELECT  |  [ESC] RESUME";
        drawCentered(smallFont, hint, 195f, Color.LIGHT_GRAY);
    }

    private void drawGameOverScreen() {
        drawCentered(largeFont, victory ? "ABYSS CONQUERED" : "DIVE OVER", 485f,
            victory ? Color.CYAN : Color.WHITE);
        drawCentered(mediumFont, "SCORE  " + session.getDisplayScore(), 385f, Color.YELLOW);
        drawCentered(smallFont, "DISTANCE  " + formatDistance(runDirector.displayMeters()),
            348f, Color.CYAN);
        drawCentered(smallFont, "+" + earnedPearls + " PEARLS  |  WALLET "
            + progression.pearls(), 316f, Color.GOLD);
        drawCentered(smallFont, "BEST SCORE  " + highScore, 286f, Color.LIGHT_GRAY);
        String retry = game.input().usingController() ? "[A] DIVE AGAIN"
            : game.input().usingTouch() ? "TAP TO DIVE AGAIN"
            : "[ENTER / SPACE / R] OR CLICK TO DIVE AGAIN";
        String menu = game.input().usingController() ? "[B] RETURN TO MENU"
            : game.input().usingTouch() ? "TAP TO RETURN TO MENU"
            : "[ESC] OR CLICK TO RETURN TO MENU";
        drawCentered(smallFont, retry, 267f, Color.WHITE);
        drawCentered(smallFont, menu, 220f, Color.LIGHT_GRAY);
    }

    private Rectangle pauseRowBounds(int index) {
        float baseline = 385f - index * 62f;
        return interactiveRow.set(365f, baseline - 40f, 550f, 54f);
    }

    private Rectangle upgradeCardBounds(int index) {
        return interactiveRow.set(upgradeCardX(index), 170f, 350f, 360f);
    }

    private float upgradeCardX(int index) {
        return 85f + index * 380f;
    }

    private void layoutTouchControls() {
        if (!game.input().isMobile()) {
            return;
        }
        float horizontalScale = viewport.getScreenWidth() == 0 ? 1f
            : GameConfig.WORLD_WIDTH / viewport.getScreenWidth();
        float verticalScale = viewport.getScreenHeight() == 0 ? 1f
            : GameConfig.WORLD_HEIGHT / viewport.getScreenHeight();
        float safeLeft = Gdx.graphics.getSafeInsetLeft() * horizontalScale;
        float safeRight = Gdx.graphics.getSafeInsetRight() * horizontalScale;
        float safeTop = Gdx.graphics.getSafeInsetTop() * verticalScale;
        float safeBottom = Gdx.graphics.getSafeInsetBottom() * verticalScale;

        TOUCH_SWIM_BUTTON.set(32f + safeLeft, 28f + safeBottom, 230f, 120f);
        TOUCH_FIRE_BUTTON.set(GameConfig.WORLD_WIDTH - safeRight - 262f,
            28f + safeBottom, 230f, 120f);
        TOUCH_DASH_BUTTON.set((GameConfig.WORLD_WIDTH - 220f) / 2f,
            28f + safeBottom, 220f, 76f);
        TOUCH_PAUSE_BUTTON.set(GameConfig.WORLD_WIDTH - safeRight - 98f,
            GameConfig.WORLD_HEIGHT - safeTop - 172f, 66f, 66f);
    }

    private float tutorialPanelY() {
        return TOUCH_SWIM_BUTTON.y + TOUCH_SWIM_BUTTON.height + 16f;
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        drawCenteredAt(font, text, GameConfig.WORLD_WIDTH / 2f, y, color);
    }

    private void drawCenteredAt(BitmapFont font, String text, float centerX, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, centerX - layout.width / 2f, y);
    }

    private void drawCenteredWithCurrentColor(BitmapFont font, String text, float y) {
        layout.setText(font, text);
        font.draw(batch, layout, (GameConfig.WORLD_WIDTH - layout.width) / 2f, y);
    }

    public void resetGame() {
        saveHighScore();
        session.reset();
        runDirector = new RunDirector();
        enemies.clear();
        harpoons.clear();
        oxygenTanks.clear();
        hazards.clear();
        enemyProjectiles.clear();
        powerUpPickups.clear();
        activePowerUps.clear();
        particles.clear();
        diver = new Diver(equippedSuit);
        difficulty = runDirector.difficulty();
        background.setDepthStage(difficulty.level(), difficulty.scrollSpeedMultiplier());
        enemySpawnTimer = 0.65f;
        oxygenTankSpawnTimer = 0f;
        hazardSpawnTimer = 0f;
        powerUpSpawnTimer = 0f;
        shootCooldownTimer = 0f;
        invulnerabilityTimer = progression.startingShieldSeconds();
        if (invulnerabilityTimer > 0f) {
            activePowerUps.put(PowerUpType.PRESSURE_SHIELD, invulnerabilityTimer);
        }
        safetyTimer = CELEBRATION_DURATION;
        breathTimer = 0f;
        tutorialTimer = 0f;
        bannerTimer = CELEBRATION_DURATION;
        bannerText = difficulty.name();
        bannerSubtitle = "DESCENDING TO " + runDirector.displayDepthMeters() + " M";
        bossWarningTimer = 0f;
        hitStopTimer = 0f;
        shakeTimer = 0f;
        shakeMagnitude = 0f;
        bubbleTimer = 0f;
        damageFlashTimer = 0f;
        inkVeilTimer = 0f;
        descentMotionTimer = CELEBRATION_DURATION + 1.2f;
        ambientTime = 0f;
        selectedPauseIndex = 0;
        selectedUpgradeIndex = 0;
        nextRunUpgradeIndex = 0;
        earnedPearls = 0;
        dashCharges = 0;
        upgradeChoices = Collections.emptyList();
        paused = false;
        gameOver = false;
        showTutorial = !Boolean.getBoolean("deepdive.hideTutorial");
        choosingUpgrade = false;
        bossSpawned = false;
        victory = false;
        finalePending = false;
        progressionRewardGranted = false;
        captureSwimmingUp = false;
        centerCamera();
        AudioManager.playBackgroundMusic();
    }

    @Override
    public void pause() {
        if (!gameOver && !choosingUpgrade) {
            paused = true;
        }
    }

    @Override
    public void dispose() {
        saveHighScore();
        super.dispose();
    }

    private Color suitUiColor() {
        return switch (equippedSuit) {
            case TIDELINE_BLUE -> Color.CYAN;
            case SALVAGE_GREEN -> Color.LIME;
            case RESCUE_RED -> Color.SCARLET;
            case ABYSS_BLACK -> Color.LIGHT_GRAY;
        };
    }
}
