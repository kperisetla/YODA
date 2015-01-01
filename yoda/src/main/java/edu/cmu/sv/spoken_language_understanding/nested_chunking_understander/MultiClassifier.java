package edu.cmu.sv.spoken_language_understanding.nested_chunking_understander;

import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by David Cohen on 12/30/14.
 */
public class MultiClassifier {
    public static final String classifierModelFile = "src/resources/models_and_serialized_objects/classifier.model";
    public static final String classifierTrainingFile = "src/resources/corpora/classifier_training_file.txt";
    public static final String serializedClassifierPreferencesFile = "src/resources/models_and_serialized_objects/serialized_classifier_preferences.srl";

    public static final String NOT_CLASSIFIED = "~~<<< VARIABLE HAS NO VALUE IN CONTEXT >>>~~";

    static HashMap<String, Integer> featurePositionMap = new HashMap<>();
    static HashMap<String, LinkedList<Serializable>> outputInterpretation;

    Process theanoSubProcess;
    InputStreamReader stdoutInputStreamReader;
    BufferedReader stdoutBufferedReader;


    public MultiClassifier(){
        ProcessBuilder processBuilder =
                new ProcessBuilder("../slu_tools/run_classifier.py", "-m", classifierModelFile);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        try {
            System.out.println("launching theano...");
            theanoSubProcess = processBuilder.start();
            stdoutInputStreamReader = new InputStreamReader(theanoSubProcess.getInputStream());
            stdoutBufferedReader = new BufferedReader(stdoutInputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(stdoutBufferedReader.readLine());
            System.out.println("theano message:" + stringBuilder.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void loadPreferences(){
        try {
            FileInputStream fileInputStream = new FileInputStream(serializedClassifierPreferencesFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            List<Object> preferences = (List<Object>) objectInputStream.readObject();
            featurePositionMap = (HashMap<String, Integer>) preferences.get(0);
            outputInterpretation = (HashMap<String, LinkedList<Serializable>>) preferences.get(1);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void trainTheanoModel(){
        ProcessBuilder processBuilder =
                new ProcessBuilder("../slu_tools/train_classifier.py", "-t", classifierTrainingFile, "-m", classifierModelFile);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            Process p = processBuilder.start();
            System.out.println("exit status:" + p.waitFor());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void classify(NodeMultiClassificationProblem classificationProblem) {
        String theanoString = packTestSample(classificationProblem)+"\n";
        try {
            theanoSubProcess.getOutputStream().write(theanoString.getBytes());
            theanoSubProcess.getOutputStream().flush();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(stdoutBufferedReader.readLine());
            System.out.println("string returned from subprocess:" + stringBuilder.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        //todo: parse generated result

    }

    public static String packTestSample(NodeMultiClassificationProblem classificationProblem){
        List<Double> features = featureVector(extractFeatures(classificationProblem));
        return "[" + String.join(", ", features.stream().map(Object::toString).collect(Collectors.toList())) + "]";
    }

    public static String packTrainingSample(NodeMultiClassificationProblem classificationProblem){
        String ans = packTestSample(classificationProblem);
        List<Integer> output = new LinkedList<>();
        for (String variable : outputInterpretation.keySet().stream().sorted().collect(Collectors.toList())){
            Object valueForVariable = classificationProblem.outputRolesAndFillers.get("ground_truth").get(variable);
            if (valueForVariable==null)
                valueForVariable = NOT_CLASSIFIED;
            output.add(outputInterpretation.get(variable).indexOf(valueForVariable));
        }
        ans += " -> ";
        ans += "[" + String.join(", ", output.stream().map(Object::toString).collect(Collectors.toList())) + "]";
        return ans;
    }

    public static Set<String> extractFeatures(NodeMultiClassificationProblem classificationProblem) {
        Set<String> featuresPresent = new HashSet<>();

        //// collect features present
        String[] tmp = classificationProblem.stringForAnalysis.split(" ");
        List<String> tokens = new LinkedList<>(Arrays.asList(tmp));

        // collect unigram features
        for (int i = 0; i < tokens.size(); i++) {
            featuresPresent.add("Unigram: " + tokens.get(i));
        }

        tokens.add(0, "<S>");
        tokens.add("</S>");

        // collect bigram features
        for (int i = 0; i < tokens.size() - 1; i++) {
            featuresPresent.add("Bigram: " + tokens.get(i) + ", " + tokens.get(i + 1));
        }

        // collect context features
        String[] fillerPath = classificationProblem.contextPathInStructure.split("\\.");
        for (int i = 0; i < fillerPath.length; i++) {
            featuresPresent.add("NodeContext: " + (fillerPath.length - i) + ", " + fillerPath[i]);
        }
        return featuresPresent;
    }

    public static List<Double> featureVector(Set<String> featuresPresent) {
            //// assemble feature vector
        List<Double> features = new LinkedList<>();
        Set<Integer> featuresOn = new HashSet<>();
        for (String presentFeature : featuresPresent) {
            if (featurePositionMap.containsKey(presentFeature))
                featuresOn.add(featurePositionMap.get(presentFeature));
            else {
//                System.out.println("WARNING: present feature not in model:" + presentFeature);
            }
        }

        for (int i = 0; i < featurePositionMap.size(); i++) {
            if (featuresOn.contains(i))
                features.add(1.0);
            else
                features.add(0.0);
        }
        return features;
    }



}
