<div align="center">
  
  ![header](https://github.com/user-attachments/assets/b8695295-c3da-4a86-9a07-5cce7052490d)
  
</div>

<div align="center">
  
# OOP2 - Capstone Project

</div>

<div align="center">
  
<h3>
Team Members:
</h3>

<p>
Chestine May Mari C. Cabiso |
Axille Dayonot |
Ruhmer Jairus R. Espina |
Lovely Shane P. Ong
</p>

</div>

---

## About the Project 💜✨

**TypeWiz** is an action-packed typing game where you play as a wizard defending your tower from waves of flying mobs. Each enemy is tied to a word—type it correctly to cast spells and destroy them before they reach your tower. Use the following controls to enhance your experience:
- **Spacebar**: Attack monsters.
- **Shift**: Switch targets.
- **Enter**: Restart the game after losing.

Stay alert—if monsters breach your defenses, your health will drop. Lose all your health, and it’s game over. Press Enter to try again and sharpen your typing skills.

### Difficulty Levels:
- 💫 **APPRENTICE**: Fewer monsters, simpler words.
- 🧙‍♂️ **WIZARD**: Balanced difficulty for seasoned typists.
- 🔮 **ARCHMAGE**: Fast waves and tricky words to truly test your reflexes.

The **OOP2 Capstone Project**, **TypeWiz**, showcases object-oriented programming principles applied to game development. Built with FXGL, a Java game development library, it leverages modern tools to deliver an engaging and interactive experience beyond the traditional FXML framework.

Can you survive the onslaught and become the ultimate typing wizard?

---

## OOP Principles 🤓✨

Our project highlights several key OOP principles:

- **Encapsulation:** The game logic, player data, and UI components are modularized into distinct classes, ensuring data hiding and reducing interdependencies.
- **Inheritance:** We utilized inheritance to create reusable and extendable classes, such as shared behaviors for different game entities (e.g., enemies, power-ups, and the main player).
- **Polymorphism:** Polymorphism is evident in how our game entities handle different actions and behaviors dynamically, such as varied responses to collisions or player interactions.
- **Abstraction:** Core functionalities are abstracted into base classes, allowing us to manage the complexity of the codebase effectively.

---

## Design Patterns Used 🎆✨

We incorporated several design patterns to ensure maintainable and efficient code:

- **Singleton Pattern:** Used for managing game states and shared resources like the game timer and score tracker.
- **Observer Pattern:** Implemented for event-driven communication between game components, such as notifying observers when the player achieves a milestone.
- **Factory Pattern:** Utilized for creating game entities dynamically without exposing the instantiation logic.

---

## Advanced Concepts Incorporated 💻✨

### Threads and Multithreading
The game leverages threads and multithreading to enhance performance and responsiveness:
- **Game Loop:** A separate thread manages the game loop, ensuring smooth gameplay and timely updates for animations and collision detection.
- **Background Tasks:** Long-running operations, such as database interactions or loading resources, are executed on background threads to prevent blocking the main UI thread.
- **Multithreading for Concurrency:** By using multiple threads, we ensured responsiveness even under heavy computational loads.

### JDBC (Java Database Connectivity)
To enhance the game's functionality, we integrated JDBC:
- **Database Management:** Player data, including scores and progression, is stored in a relational database.
- **SQL Integration:** We established secure and efficient connections to the database for CRUD (Create, Read, Update, Delete) operations.
- **Leaderboards:** A dynamic leaderboard fetches top player scores in real-time using JDBC to keep players engaged and motivated.

---

## Features
- **Real-time Typing-based Combat:** Type words to cast spells and defeat enemies.
- **Dynamic Difficulty Levels:** Choose from Apprentice, Wizard, or Archmage modes to match your skill level.
- **Leaderboard Integration:** Track high scores and compete with others.
- **Multithreaded Game Loop:** For smooth animations and responsive gameplay.
- **Modular Design:** Built with clean, maintainable code using OOP principles and design patterns.

---

## Getting Started 🚀

To get started with TypeWiz, follow these steps:

### Prerequisites
- Java Development Kit (JDK 8 or higher)
- A relational database (e.g., MySQL)
- An IDE or text editor (e.g., IntelliJ, Eclipse)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/coding-chez/OOP2-Capstone.git
2. Configure the database:
- Create a database and import the provided schema.
- Update the db.properties file with your database credentials.
3. Build and run the project:

       ./gradlew run

---

### From Brainstorming to Open Source 🔮✨

Our journey began with a brainstorming session to decide on the concept of our capstone project. We aimed to create a fun and educational typing game that enhances players' typing speed and accuracy.

Each team member contributed to the ideation, design, and development process:

- **Conceptualization:** We collaborated to identify the core mechanics and objectives of TypeWiz, ensuring they aligned with our goals as a team.
- **Development:** Responsibilities were divided based on individual strengths—team members worked on coding, designing, and integrating features.
- **Testing and Refinement:** We rigorously tested the game for bugs and usability, incorporating feedback to polish the final product.
- **Open Source Contribution:** To give back to the community, we made the project open source, allowing others to learn from and extend our work.

### Contributing

We welcome contributions from the community! Please follow these steps to contribute:

1. Fork the repository.
2. Create a feature branch:

        git checkout -b feature-name

3. Commit your changes:

       git commit -m "Add feature-name"

4. Push to your branch:

        git push origin feature-name

5. Open a pull request.


---
