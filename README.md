INSTRUCTIONS FOR THE PROJECT:

The program starts running with initial sound<br>
**" Hello. I am Mary and I am the voice of this project "** <br>
after that it **"starts listening"** to user's voice using **"Sphinx CMU"**.<br>
Using **MaryTTS** we made this program to speak 
_____________________________________________________________________________________________________________________________________________
# How It Works (Class Breakdown)

# 1. `MainWork` (Main Entry Point)

This is the starting point of the program.

# What it does:

* Configures the audio settings (sample rate, bit depth, disables default mixers).
* Initializes the **MaryTTS voice engine**.
* Sets the voice to a female American English voice (`cmu-slt-hsmm`).
* Applies a volume effect to reduce distortion.
* Speaks two introductory lines using the `Speaker.speak()` method.
* Starts listening for voice commands using the `CommandProcessor.startListening()` method.

---

# 2. `Speaker` (Text-to-Speech)

Handles speaking functionality using **MaryTTS**.

# How it works:

* Takes in text and uses MaryTTS to synthesize speech.
* Converts the generated audio to a playable format (PCM signed 16-bit).
* Streams the audio to the system’s speakers in real time.
* Closes and cleans up resources after playback.

---

# 3. `CommandProcessor` (Voice Recognition & Command Execution)

Responsible for **listening to the microphone**, recognizing speech using **Vosk**, and executing system commands.

# What it does:

* Loads the Vosk model from: `src/main/resources/vosk-model-en-in-0.5`.
* Captures live audio input using Java’s `TargetDataLine`.
* Continuously listens and checks if any recognizable speech is received.
* If no input is detected 5 times in a row, MaryTTS prompts: “Sir, are you there?”, then “Looks like no one is here” and exits.
* If valid input is recognized:

  * Parses the Vosk output (JSON) and extracts the command.
  * Matches it against a list of known commands.
  * Speaks a response using MaryTTS.
  * Executes the corresponding Windows command using `Runtime.getRuntime().exec(...)`.

---

 Voice Command Workflow

 Example:

1. User says: “open chrome”
2. [Vosk model] captures and recognizes the voice
3. Recognized text: `"open chrome"`
4. Matched in a switch-case block
5. MaryTTS responds: “Opening Chrome”
6. System executes: `start chrome.exe`

---

 Supported Voice Commands:

| Command          | Action                       |
| ---------------- | ---------------------------- |
| "open chrome"    | Opens Google Chrome          |
| "close chrome"   | Closes Google Chrome         |
| "open settings"  | Opens Windows Settings       |
| "close settings" | Closes Windows Settings      |
| "open whatsapp"  | Opens WhatsApp (UWP)         |
| "close whatsapp" | Closes WhatsApp              |
| "bye"            | Speaks "Thank you" and exits |

---

 Requirements:

* Java 8 or later
* MaryTTS library
* Vosk API for Java
* Vosk English model (e.g., `vosk-model-en-in-0.5`)
* Microphone access
* Windows OS (for the system command integration)

---




-------------------------------------------------------------------------------------------------------------------------------------------
COLLABRATORS NAME:              GIT-HUB ID'S:
1. Vidhu Kaushik                https://github.com/Vi-ic1101/
2. Kushal Trivedi               https://github.com/kushal-trivedi18
3. Mayank Kaul                  https://github.com/Mayankkau1
4. Sunny Gupta                  https://github.com/Sunny841428

