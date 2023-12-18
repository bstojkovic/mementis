package com.mementis.mementis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

class QA {
    private String question;
    private String answer;

    public QA(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    // Getters
    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}

public class HelloWorldSwing {

    private static List<QA> qaList = new ArrayList<>();
    private static int currentIndex = 0;
    private static Random random = new Random();

    public static void main(String[] args) {
        loadQAFromFile("QnA.txt");

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mementis");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // Creating a panel to hold components
            JPanel panel = new JPanel(new BorderLayout());

            // Creating the question area
            JTextArea questionArea = new JTextArea("Enter your question here");
            questionArea.setLineWrap(true);
            questionArea.setWrapStyleWord(true);
            questionArea.setEditable(false);
            JScrollPane questionScrollPane = new JScrollPane(questionArea);
            questionScrollPane.setPreferredSize(new Dimension(800, 50));
            panel.add(questionScrollPane, BorderLayout.NORTH);

            // Creating the answer area
            JTextArea answerArea = new JTextArea("Your answer will appear here");
            answerArea.setLineWrap(true);
            answerArea.setWrapStyleWord(true);
            answerArea.setEditable(false);
            JScrollPane answerScrollPane = new JScrollPane(answerArea);
            answerScrollPane.setPreferredSize(new Dimension(800, 200));
            panel.add(answerScrollPane, BorderLayout.CENTER);

            // Creating buttons for navigation
            JButton prevButton = new JButton("Previous");
            JButton nextButton = new JButton("Next");
            JButton randomButton = new JButton("Random");

            // Adding action listeners
            prevButton.addActionListener(e -> navigateQuestions(questionArea, answerArea, -1));
            nextButton.addActionListener(e -> navigateQuestions(questionArea, answerArea, 1));
            randomButton.addActionListener(e -> selectRandomQuestion(questionArea, answerArea));

            // Creating a panel for buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(prevButton);
            buttonPanel.add(nextButton);
            buttonPanel.add(randomButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            // Initialize with the first question, if available
            if (!qaList.isEmpty()) {
                questionArea.setText(qaList.get(0).getQuestion());
                answerArea.setText(qaList.get(0).getAnswer());
            }

            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null); // center the window
            frame.setVisible(true);
        });
    }

    private static void loadQAFromFile(String filePath) {
        try (InputStream inputStream = HelloWorldSwing.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            String line;
            String question = "";
            String answer = "";
            boolean isQuestion = true;
    
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    qaList.add(new QA(question.trim(), answer.trim()));
                    question = "";
                    answer = "";
                    isQuestion = true;
                } else {
                    if (isQuestion) {
                        question += line + "\n";
                    } else {
                        answer += line + "\n";
                    }
                    isQuestion = false;
                }
            }
            // Add the last QA pair if the file does not end with a blank line
            if (!question.isEmpty() || !answer.isEmpty()) {
                qaList.add(new QA(question.trim(), answer.trim()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void navigateQuestions(JTextArea questionArea, JTextArea answerArea, int direction) {
        // Update the currentIndex based on the direction
        currentIndex += direction;

        // Check for bounds
        if (currentIndex < 0) {
            currentIndex = qaList.size() - 1;
        } else if (currentIndex >= qaList.size()) {
            currentIndex = 0;
        }

        // Set the text area with the new question
        questionArea.setText(qaList.get(currentIndex).getQuestion());
        answerArea.setText(qaList.get(currentIndex).getAnswer());
    }

    private static void selectRandomQuestion(JTextArea questionArea, JTextArea answerArea) {
        if (qaList.isEmpty()) return;

        currentIndex = random.nextInt(qaList.size());
        questionArea.setText(qaList.get(currentIndex).getQuestion());
        answerArea.setText(qaList.get(currentIndex).getAnswer());
    }
}
