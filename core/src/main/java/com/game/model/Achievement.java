package com.game.model;

public enum Achievement {
    FIRST_BLOOD("FIRST BLOOD", "Defeat your first enemy"),
    HUNTER_10("REEF HUNTER", "Defeat 10 enemies across all dives"),
    HUNTER_50("DEEP HUNTER", "Defeat 50 enemies across all dives"),
    HUNTER_250("APEX PREDATOR", "Defeat 250 enemies across all dives"),
    COMBO_3("TRIPLE STRIKE", "Reach a 3-kill combo"),
    COMBO_5("FEEDING FRENZY", "Reach a 5-kill combo"),
    COMBO_10("UNTOUCHABLE HUNTER", "Reach a 10-kill combo"),
    SCORE_10K("FIVE FIGURES", "Score 10,000 points in one dive"),
    SCORE_50K("DEEP VALUE", "Score 50,000 points in one dive"),
    SCORE_100K("LEGEND OF THE ABYSS", "Score 100,000 points in one dive"),
    DISTANCE_100("GETTING YOUR FINS WET", "Travel 100 m in one dive"),
    DISTANCE_500("OPEN WATER", "Travel 500 m in one dive"),
    DISTANCE_1000("LONG HAUL", "Travel 1,000 m in one dive"),
    FIRST_OXYGEN("FRESH AIR", "Collect your first oxygen tank"),
    OXYGEN_25("AIR SUPPLY", "Collect 25 oxygen tanks across all dives"),
    FIRST_POWER_UP("POWER SURGE", "Collect your first special power"),
    POWER_UPS_25("FULLY CHARGED", "Collect 25 special powers across all dives"),
    REEF_CLEARED("REEF DIVER", "Complete Sunlit Reef"),
    RUINS_CLEARED("RUIN RUNNER", "Complete Sinking Ruins"),
    MAZE_CLEARED("CURRENT BREAKER", "Complete Current Maze"),
    TRENCH_CLEARED("BLACKWATER VETERAN", "Complete Blackwater Trench"),
    RIFT_CLEARED("RIFT WALKER", "Complete Abyssal Rift"),
    ABYSS_CONQUERED("ABYSS CONQUERED", "Complete the game"),
    BOSS_SLAYER("COLOSSUS FALLS", "Defeat the Abyssal Octopus"),
    FLAWLESS_STAGE("DRY SUIT", "Complete a stage without taking damage"),
    FLAWLESS_RUN("PERFECT DIVE", "Complete the game without taking damage"),
    CEASEFIRE_STAGE("HOLD YOUR FIRE", "Complete a stage without firing"),
    EMPTY_HANDED_STAGE("LEAVE NO TRACE", "Complete a stage without collecting anything"),
    PURE_SKILL_STAGE("NO SHORTCUTS", "Complete a stage without using a special power"),
    SILENT_100("SILENT RUNNING", "Travel 100 m without firing"),
    MINIMALIST_100("MINIMALIST", "Travel 100 m without collecting anything"),
    CEASEFIRE_RUN("PACIFIST DESCENT", "Complete the game without firing"),
    EMPTY_HANDED_RUN("EMPTY-HANDED HERO", "Complete the game without collecting anything"),
    PURE_SKILL_RUN("PURE SKILL", "Complete the game without using a special power"),
    EASY_COMPLETE("SAFE DESCENT", "Complete the game on Easy"),
    NORMAL_COMPLETE("TRUE DIVER", "Complete the game on Normal"),
    HARD_COMPLETE("PRESSURE PROOF", "Complete the game on Hard"),
    CHALLENGER("CHALLENGER", "Complete a stage with any challenge modifier"),
    LOCKDOWN_STAGE("LOCKDOWN DIVER", "Complete a stage with every challenge modifier"),
    TOTAL_LOCKDOWN("TOTAL LOCKDOWN", "Complete the game with every challenge modifier");

    private final String title;
    private final String description;

    Achievement(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }
}
