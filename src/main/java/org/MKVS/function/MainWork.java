package org.MKVS.function;

// Standard Java I/O
import java.io.IOException;

import javax.sound.sampled.*;
//import org.MKVS.function.SettingParameters;

// MaryTTS classes for speech synthesis
import marytts.*;
//import marytts.exceptions.*;

// Utility for playing audio
//import marytts.util.data.audio.AudioPlayer;


import org.json.JSONObject;
import org.vosk.*;

//import java.time.*;
//import java.time.format.*;

public class MainWork{
    public static void main(String[] st){

        //Set audio sample rate to improve audio quality
        System.setProperty("mary.audio.sampleRate","16000");

        //Set audio bit depth
        System.setProperty("mary.audio.bits","16");

        // Disable default audio mixers to avoid audio issues
        System.setProperty("java.sound.useDefaultMixers","false");

        //Configuration config = SpeechConfig.getConfiguration();

        try{
            // Create MaryTTS interface for speech synthesis
            MaryInterface marytts= new LocalMaryInterface();

            // Set TTS voice to female  american english
            marytts.setVoice("cmu-slt-hsmm");

            // Apply audio effect (reduce volume to avoid crackling)
           marytts.setAudioEffects("Volume(amount:0.6)");

            // Speak a message using MaryTTS
            Speaker.speak(marytts,"Welcome to MKVS, mini prototype Artificial Intelligence model.");
            Speaker.speak(marytts,"MKVS, stands for teams first initials.");
            Speaker.speak(marytts,"M for Mayank, K for Kushal, V for Vidhu, and S for Sunny.");
            Speaker.speak(marytts,"model that is decide for me is Mach for MK. five for V, and Special for S.  ");
            Speaker.speak(marytts,"tech specs used are, first is VOSK for speech recognition, second is Mary TTS, for voice output.");
            Speaker.speak(marytts,"voice is the female United states adult that is CMU-SLT-HSMM.");
            Speaker.speak(marytts,"thats! all about me, thank you.");
            CommandProcessor.startListening(marytts);
        }catch (Exception e){
            System.err.println("Startup error : "+e.getMessage());
            e.printStackTrace();
        }
    }
}


class Speaker {
    // Static method to synthesize speech from text using MaryTTS
   public static void speak(MaryInterface marytts, String text){
       try{
           // Generate audio from input text using MaryTTS
           AudioInputStream audio = marytts.generateAudio(text);

           // Get the format of the generated audio
           AudioFormat originalFormat = audio.getFormat();
    // Convert to PCM signed format (required for most audio playback systems)
           AudioFormat pcmFormat= new AudioFormat(
                   AudioFormat.Encoding.PCM_SIGNED,         // Encoding type
                   originalFormat.getSampleRate(),16,       // Sample rate (Hz) and Sample size in bits 16
                   originalFormat.getChannels(),            // Number of channels
                   originalFormat.getChannels()*2,            // Frame size (bytes)
                   originalFormat.getSampleRate(),            // Frame rate (same as sample rate)
                   false
           );
            //Convert to PCM format if needed
           AudioInputStream pcmAudio=AudioSystem.getAudioInputStream(pcmFormat, audio);

           DataLine.Info info = new DataLine.Info(SourceDataLine.class,pcmFormat);
           SourceDataLine line =(SourceDataLine) AudioSystem.getLine(info);
           line.open(pcmFormat);
           line.start();

           // Stream audio data to
           byte[] buffer = new byte[4096];
           int bytesRead;
           while((bytesRead=pcmAudio.read(buffer))!=-1){
               line.write(buffer,0,bytesRead);
           }

           // Finish and release resources
           line.drain();
           line.stop();
           line.close();
           pcmAudio.close();
       }catch(Exception e){
           System.err.println("Text to speech error " + e.getMessage());
           e.printStackTrace();
       }
   }
}


class CommandProcessor{


    public static void startListening(MaryInterface marytts) throws IOException, LineUnavailableException {
        int count = 0;
        //using vosk model
        LibVosk.setLogLevel(LogLevel.WARNINGS);
        Model model=new Model("src/main/resources/vosk-model-small-en-in-0.4");
        Recognizer recognizer = new Recognizer(model,16000.0f);

        //using default microphone
        TargetDataLine microphone;
        AudioFormat  format=new AudioFormat(16000.0f,16,1,true,false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,format);
        microphone =(TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
        System.out.println("listening...");
        Speaker.speak(marytts,"Now we can proceed to voice commands.");

        byte[] buffer = new byte[4096];
        // Flag to control recognition loop
        boolean exit = false;
        String searchQuery;
        String finalSearch;
        boolean waiting=false;
        boolean waitingForParameterInput=false;
        String param;
        String tempParamValue;
        double paramValue;

        while(!exit){
            int bytesRead = microphone.read(buffer,0, buffer.length);
            if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                String resultJson = recognizer.getResult();
//                System.out.println("Raw Vosk result: " + resultJson);

                // Parse JSON properly
                String command = "";
                try {
                    JSONObject obj = new JSONObject(resultJson);
                    command = obj.optString("text", "").trim().toLowerCase();
                } catch (Exception e) {
                    System.err.println("JSON parsing error: " + e.getMessage());
                }

                System.out.println("Recognized command: '" + command + "'");

                if (command.isBlank()) {
                    if(count < 5){
                       marytts.setAudioEffects("Volume(amount:0.6)");

                        Speaker.speak(marytts,"sir are you there?");
                        count++;
                        System.out.println("No command recognized, continuing...");
                    }else{
                        Speaker.speak(marytts,"looks like no one is here.");
                        break;
                    }
                    continue;
                }
                count=0;

                 if(!command.isBlank()){
                     if(command.toLowerCase().contains("browser") || command.toLowerCase().contains("browse")) {
                         Speaker.speak(marytts,"what should i search for? ");
                         waiting=true;
                         continue;
                     }
                     if(waiting){
                         if(command.contains("search for")) {
                             int indexforSearchFor = command.toLowerCase().indexOf("search for");
                             if (indexforSearchFor != -1) {
                                 searchQuery = command.substring(indexforSearchFor + "search for".length()).trim();
                                 if (!searchQuery.isBlank()) {
                                     finalSearch = searchQuery.replace(" ", "+");
                                     Speaker.speak(marytts, "searching for." + searchQuery );
                                     try {
                                         Runtime.getRuntime().exec("cmd.exe /c start https://google.com/search?q=" + finalSearch);
                                     } catch (IOException e) {
                                         Speaker.speak(marytts,"unable to perform actions.");
                                         System.err.println("error "+ e.getMessage());
                                     }
                                 }else{
                                     Speaker.speak(marytts,"you didn't say anything.");
                                 }
                                 waiting=false;
                                 continue;
                             }
                         }else{
                             searchQuery=command.trim();
                             if(!searchQuery.isBlank()){
                                 finalSearch=searchQuery.replace(" ","+");
                                 Speaker.speak(marytts,"searching for."+searchQuery);
                                 try {
                                     Runtime.getRuntime().exec("cmd.exe /c start chrome.exe --guest https://google.com/search?q="+finalSearch);
                                 }catch (IOException e){
                                     Speaker.speak(marytts,"unable to perform actions.");
                                     System.err.println("error "+ e.getMessage());
                                 }
                             }
                         }
                         // Close Google Chrome
                        waiting=false;
                     }
                     if (command.contains("close search")||command.contains("close tab")) {
                         Speaker.speak(marytts, "closing search.");
                         try {
                             Runtime.getRuntime().exec("cmd.exe /c TASKKILL /IM chrome.exe /F");
                         } catch (IOException e) {
                             Speaker.speak(marytts, "unable to perform actions.");
                             System.err.println("error " + e.getMessage());
                         }
                     }
                     if (!command.isBlank()) {
                         if (!waitingForParameterInput && (command.contains("go to settings") || command.contains("settings"))) {
                             Speaker.speak(marytts, "Opening my settings.");
                             Speaker.speak(marytts, "What do you want to change? Say something like 'honesty 75'.");
                             waitingForParameterInput = true;
                             continue;
                         }
                         if (waitingForParameterInput) {
                             String[] parts = command.split("\\s+");
                             if (parts.length >= 1) {
                                 param = parts[0].toLowerCase();
                                  paramValue = 75.0;  // Fixed value

                                 // Allow only specific parameters
                                 if (param.equals("honesty") || param.equals("curiosity") ||
                                         param.equals("patience") || param.equals("humour")) {

                                     SettingParameters.updateParameters(param, paramValue);
                                     Speaker.speak(marytts, "Updated " + param + " to " + paramValue);
                                     System.out.println("current " +param + SettingParameters.showParameters().get(param));

                                 } else {
                                     Speaker.speak(marytts, "I can only update honesty, curiosity, patience or humour.");
                                 }
                             } else {
                                 Speaker.speak(marytts, "Please say the parameter name like 'humour'.");
                             }

                             waitingForParameterInput = false;
                             continue;
                         }

                     }

                     if(command.contains("open whatsapp")) {
                         Speaker.speak(marytts, "opening whatsapp.");
                         Runtime.getRuntime().exec("cmd.exe /c start whatsapp:");
                     }
                     if(command.contains("close whatsapp")) {
                         Speaker.speak(marytts, "closing whatsapp.");
                         Runtime.getRuntime().exec("cmd.exe /c TASKKILL /IM whatsapp.exe /F");
                     }
                     if(command.contains("bye")||command.contains("exit")||command.contains("thank you")) {
                         Speaker.speak(marytts, "thank you very much sir.");
                         break;
                     }

                 }
             }
        }
        recognizer.close();
        microphone.stop();
        microphone.close();
    }
}
