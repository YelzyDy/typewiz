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
        "code", "java", "type", "game", "block",
        "winter", "wizard", "magic", "spell", "potion",
        "frost", "snow", "ice", "cold", "programming",
        "keyboard", "screen", "input", "output", "variable",
        "function", "class", "method", "array", "string","jay","vince","cool","normal"
    );
    
    private final List<String> mediumWords = Arrays.asList(
        "variable", "function", "method", "algorithm", "interface",
        "inheritance", "polymorphism", "abstraction", "encapsulation", "iteration",
        "recursion", "exception", "debugging", "framework", "compiler",
        "library", "component", "parameter", "structure", "observer"
    );
    
    private final List<String> hardWords = Arrays.asList(
        "synchronization", "multithreading", "serialization", "optimization", "implementation",
        "initialization", "authentication", "configuration", "virtualization", "documentation",
        "architecture", "dependency", "infrastructure", "persistence", "transaction",
        "asynchronous", "development", "integration", "management", "deployment","cool and normal", "DAA"
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