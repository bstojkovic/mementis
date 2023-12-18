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
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

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
    private static boolean wordsRevealed = false;
    private static Random random = new Random();

    private static JTextArea questionArea;
    private static JTextPane answerArea;

    public static void main(String[] args) {
        loadQAFromFile("QnA.txt");

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mementis");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // Creating a panel to hold components
            JPanel panel = new JPanel(new BorderLayout());

            // Creating the question area
            questionArea = new JTextArea("Enter your question here");
            questionArea.setLineWrap(true);
            questionArea.setWrapStyleWord(true);
            questionArea.setEditable(false);
            JScrollPane questionScrollPane = new JScrollPane(questionArea);
            questionScrollPane.setPreferredSize(new Dimension(800, 50));
            panel.add(questionScrollPane, BorderLayout.NORTH);

            // Creating the answer area
            answerArea = new JTextPane();
            answerArea.setEditable(false);
            JScrollPane answerScrollPane = new JScrollPane(answerArea);
            answerScrollPane.setPreferredSize(new Dimension(800, 200));
            panel.add(answerScrollPane, BorderLayout.CENTER);

            // Creating buttons for navigation
            JButton prevButton = new JButton("Previous");
            JButton nextButton = new JButton("Next");
            JButton randomButton = new JButton("Random");

            // Adding action listeners
            prevButton.addActionListener(e -> navigateQuestion(-1));
            nextButton.addActionListener(e -> navigateQuestion(1));
            randomButton.addActionListener(e -> selectRandomQuestion());

            // Creating a panel for buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(prevButton);
            buttonPanel.add(nextButton);
            buttonPanel.add(randomButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            // Initialize with the first question, if available
            if (!qaList.isEmpty()) {
                viewCurrentQuestion();
            }

            Action spaceBarAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!wordsRevealed) {
                        blankOutWordsInPane(answerArea, qaList.get(currentIndex).getAnswer(), true, null);
                        wordsRevealed = true;
                    } else {
                        blankOutWordsInPane(answerArea, qaList.get(currentIndex).getAnswer(), true, Color.RED);
                    }
                }
            };

            // Action for Enter Key - Mark Words Green
            Action markGreenAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (wordsRevealed) {
                        blankOutWordsInPane(answerArea, qaList.get(currentIndex).getAnswer(), true, Color.GREEN);
                    }
                }
            };

            // Set up key bindings for the JTextPane
            KeyStroke spaceKey = KeyStroke.getKeyStroke("SPACE");
            KeyStroke enterKey = KeyStroke.getKeyStroke("ENTER");

            InputMap inputMap = answerArea.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = answerArea.getActionMap();

            inputMap.put(spaceKey, "spaceKey");
            inputMap.put(enterKey, "markGreen");

            actionMap.put("spaceKey", spaceBarAction);
            actionMap.put("markGreen", markGreenAction);

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

    private static void viewQuestionWithIndex(int index) {
        questionArea.setText(qaList.get(index).getQuestion());
        blankOutWordsInPane(answerArea, qaList.get(currentIndex).getAnswer(), false, null);
    }

    private static void viewCurrentQuestion() {
        viewQuestionWithIndex(currentIndex);
    }

    private static void navigateQuestion(int direction) {
        // Update the currentIndex based on the direction
        currentIndex += direction;

        // Check for bounds
        if (currentIndex < 0) {
            currentIndex = qaList.size() - 1;
        } else if (currentIndex >= qaList.size()) {
            currentIndex = 0;
        }

        // Set the text area with the new question
        viewCurrentQuestion();
    }

    private static void selectRandomQuestion() {
        if (qaList.isEmpty()) return;

        currentIndex = random.nextInt(qaList.size());

        viewCurrentQuestion();
    }

    private static void blankOutWordsInPane(JTextPane textPane, String answer, boolean revealFirstThree, Color firstThreeWordsColor) {
        String[] words = answer.split("\\s+");

        try {
            StyledDocument doc = textPane.getStyledDocument();

            Color darkBlue = new Color(0, 0, 139);

            Style regularStyle = textPane.addStyle("RegularStyle", null);
            StyleConstants.setForeground(regularStyle, Color.BLUE);

            Style highlightedStyle = textPane.addStyle("HighlightedStyle", null);
            StyleConstants.setBackground(highlightedStyle, darkBlue);

            Style selectedStyle = textPane.addStyle("SelectedStyle", null);
            StyleConstants.setForeground(selectedStyle, Color.WHITE);
            StyleConstants.setBackground(selectedStyle, darkBlue);

            if (firstThreeWordsColor == null) {
                firstThreeWordsColor = Color.BLACK;
            }
            Style firstThreeStyle = textPane.addStyle("FirstThreeStyle", null);
            StyleConstants.setForeground(firstThreeStyle, firstThreeWordsColor); // First three words style

            doc.remove(0, doc.getLength());

            for (int i = 0; i < words.length; i++) {
                String word = words[i];

                Style style;
                String text;

                if (i < 3) {
                    if (revealFirstThree) {
                        if (firstThreeWordsColor == Color.BLACK) {
                            style = selectedStyle;
                            text = " " + word + " ";
                        } else {
                            style = firstThreeStyle;
                            text = word;
                        }
                    } else {
                        style = highlightedStyle;
                        text = "_".repeat(word.length());
                    }
                } else {
                    style = regularStyle;
                    text = "_".repeat(word.length());
                }

                doc.insertString(doc.getLength(), text, style);
                doc.insertString(doc.getLength(), " ", regularStyle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
