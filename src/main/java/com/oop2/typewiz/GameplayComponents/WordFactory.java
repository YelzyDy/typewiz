package com.oop2.typewiz.GameplayComponents;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A factory class for generating words of varying difficulty based on game progression.
 * Implements the Factory Method design pattern.
 */
public class WordFactory {
    // Singleton instance
    private static WordFactory instance;

    // Random number generator
    private final Random random = new Random();

    // Word categories
    private final List<String> easyWords = Arrays.asList(
            // Original programming words
            "code", "java", "type", "game", "block",
            "keyboard", "screen", "input", "output", "variable",
            "function", "class", "method", "array", "string",

            // Original magical words
            "winter", "wizard", "magic", "spell", "potion",
            "frost", "snow", "ice", "cold",

            // New magical words
            "wand", "book", "rune", "mana", "fire",
            "wind", "earth", "water", "light", "dark",
            "staff", "orb", "ring", "cape", "brew",
            "charm", "scroll", "tome", "sage", "mage",
            "witch", "power", "aura", "glow", "beam",
            "spark", "flame", "storm", "cloud", "star",

            // New programming words
            "loop", "data", "byte", "list", "void",
            "main", "test", "debug", "run", "code"
    );

    private final List<String> mediumWords = Arrays.asList(
            // Original programming words
            "variable", "function", "method", "algorithm", "interface",
            "inheritance", "polymorphism", "abstraction", "encapsulation", "iteration",
            "recursion", "exception", "debugging", "framework", "compiler",
            "library", "component", "parameter", "structure", "observer",

            // New magical words
            "enchantment", "conjuration", "divination", "illusion", "necromancy",
            "alchemy", "familiar", "grimoire", "talisman", "arcane-art",
            "elemental", "sorcery", "mystical", "ethereal", "celestial",
            "prophecy", "ritual", "crystal", "ancient", "mystic-rune",
            "dragon-scale", "fairy-dust", "mana-pool", "soul-gem", "void-stone",

            // New programming words
            "boolean", "integer", "floating", "string-builder", "array-list",
            "hash-map", "linked-list", "stack-frame", "heap-memory", "binary-tree",
            "data-type", "code-block", "namespace", "package", "module"
    );

    private final List<String> hardWords = Arrays.asList(
            // Original programming words
            "synchronization", "multithreading", "serialization", "optimization", "implementation",
            "initialization", "authentication", "configuration", "virtualization", "documentation",
            "architecture", "dependency", "infrastructure", "persistence", "transaction",
            "asynchronous", "development", "integration", "management", "deployment",

            // New magical words
            "transmutation", "teleportation", "levitation-mastery", "mind-control",
            "dragon-summoning", "phoenix-rebirth", "time-manipulation", "reality-bending",
            "elemental-mastery", "arcane-projection", "astral-projection", "soul-binding",
            "ancient-incantation", "forbidden-magic", "dimensional-rift", "ethereal-plane",
            "mystical-convergence", "archmage-ritual", "celestial-alignment", "void-walking",

            // New programming words
            "object-oriented", "polymorphic-type", "abstract-factory", "design-pattern",
            "dependency-injection", "aspect-oriented", "memory-management", "garbage-collection",
            "concurrency-control", "transaction-isolation", "distributed-system", "microservice",
            "reactive-programming", "functional-paradigm", "event-sourcing", "message-queue",
            "circuit-breaker", "service-discovery", "load-balancing", "fault-tolerance"
    );

    /**
     * Private constructor for singleton pattern
     */
    private WordFactory() {
        // Private constructor to prevent direct instantiation
    }

    /**
     * Gets the singleton instance of WordFactory
     * @return The WordFactory instance
     */
    public static WordFactory getInstance() {
        if (instance == null) {
            instance = new WordFactory();
        }
        return instance;
    }

    /**
     * Gets a random word appropriate for the current wave
     * @param currentWave The current wave number
     * @return A randomly selected word with appropriate difficulty
     */
    public String getWordForWave(int currentWave) {
        // Calculate appropriate difficulty distribution based on wave number
        double rand = random.nextDouble();

        if (currentWave <= 3) {
            // Early waves - mostly easy words
            if (rand < 0.7) return getRandomEasyWord();
            else return getRandomMediumWord();
        }
        else if (currentWave <= 6) {
            // Middle waves - mix of easy and medium words
            if (rand < 0.3) return getRandomEasyWord();
            else if (rand < 0.8) return getRandomMediumWord();
            else return getRandomHardWord();
        }
        else {
            // Late waves - mostly medium and hard words
            if (rand < 0.1) return getRandomEasyWord();
            else if (rand < 0.5) return getRandomMediumWord();
            else return getRandomHardWord();
        }
    }

    /**
     * Gets a random easy word
     * @return A random easy word
     */
    public String getRandomEasyWord() {
        return getRandomWord(easyWords);
    }

    /**
     * Gets a random medium word
     * @return A random medium word
     */
    public String getRandomMediumWord() {
        return getRandomWord(mediumWords);
    }

    /**
     * Gets a random hard word
     * @return A random hard word
     */
    public String getRandomHardWord() {
        return getRandomWord(hardWords);
    }

    /**
     * Gets a random word from a specific word list
     * @param wordList The list to select from
     * @return A randomly selected word
     */
    private String getRandomWord(List<String> wordList) {
        int index = random.nextInt(wordList.size());
        return wordList.get(index);
    }

    /**
     * Gets all available easy words
     * @return List of easy words
     */
    public List<String> getEasyWords() {
        return easyWords;
    }

    /**
     * Gets all available medium words
     * @return List of medium words
     */
    public List<String> getMediumWords() {
        return mediumWords;
    }

    /**
     * Gets all available hard words
     * @return List of hard words
     */
    public List<String> getHardWords() {
        return hardWords;
    }
} 