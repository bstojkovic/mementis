package com.mementis.mementis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.LinkedList;
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

class QAList {
    private static String filePath = "QnA.txt";
    private static Random random = new Random();

    private List<QA> list = new ArrayList<>();

    public void load() {
        try (InputStream inputStream = HelloWorldSwing.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            String question = "";
            String answer = "";
            boolean isQuestion = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    question = question.trim();
                    answer = answer.trim().replace("\t", "    ");

                    list.add(new QA(question, answer));

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
                list.add(new QA(question.trim(), answer.trim()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public QA getQA(int index) {
        return list.get(index);
    }

    public int getRandomQAIndex() {
        return random.nextInt(list.size());
    }

    public QA getRandomQA() {
        return getQA(getRandomQAIndex());
    }

    public int getIncrementedQAIndex(int current, int increment) {
        current += increment;

        if (current < 0) {
            // Roll over to end of list
            current = list.size() - 1;
        } else if (current >= list.size()) {
            current = 0;
        }

        return current;
    }
}

class Token {
    public String text;

    public Token(String tokenString) {
        text = tokenString;
    }
}

class AnswerTokenizer {
    public List<Token> list;

    public AnswerTokenizer(String answerString) {
        list = new ArrayList<Token>();

        String[] words = answerString.split("[ ]+");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            Token token = new Token(word);
            list.add(token);
        }
    }
}

public class HelloWorldSwing {
    private static QAList qaList;
    private static JTextArea questionArea;
    private static JTextPane answerArea;

    private static int currentQAIndex;

    private static String currentQuestion;
    private static AnswerTokenizer currentAnswerTokens;

    public static void main(String[] args) {
        qaList = new QAList();
        qaList.load();

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
            prevButton.addActionListener(e -> handlePrevButton());
            nextButton.addActionListener(e -> handleNextButton());
            randomButton.addActionListener(e -> handleRandomButton());

            // Creating a panel for buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(prevButton);
            buttonPanel.add(nextButton);
            buttonPanel.add(randomButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null); // center the window
            frame.setVisible(true);

            setRandomQA();
        });
    }

    private static void viewCurrentQA() {
        questionArea.setText(String.valueOf(currentQAIndex + 1) + ". " + currentQuestion);

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < currentAnswerTokens.list.size(); i++) {
            Token token = currentAnswerTokens.list.get(i);
            // TODO: Do something with token's properties...
            buffer.append(token.text);
            buffer.append(" ");
        }
        answerArea.setText(buffer.toString());
    }

    private static void setQAWithIndex(int index) {
        currentQAIndex = index;

        QA qa = qaList.getQA(currentQAIndex);
        currentQuestion = qa.getQuestion();
        currentAnswerTokens = new AnswerTokenizer(qa.getAnswer());

        viewCurrentQA();
    }

    private static void setRandomQA() {
        int randomQAIndex = qaList.getRandomQAIndex();
        setQAWithIndex(randomQAIndex);
    }

    private static void incrementQAIndex(int inc) {
        int newIndex = qaList.getIncrementedQAIndex(currentQAIndex, inc);
        setQAWithIndex(newIndex);
    }

    private static void handlePrevButton() {
        incrementQAIndex(-1);
    }

    private static void handleNextButton() {
        incrementQAIndex(1);
    }

    private static void handleRandomButton() {
        setRandomQA();
    }
}
