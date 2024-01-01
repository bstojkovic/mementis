package com.mementis.mementis;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;

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

    public int count() {
        return list.size();
    }
}

class QAWrapper {
    public int qaIndex;

    QAWrapper(int index) {
        qaIndex = index;
    }
}

class QABoxes {
    private static Random random = new Random();

    private static List<QAWrapper> currentBox = new ArrayList<>();
    private static List<QAWrapper> reviseBox = new ArrayList<>();
    private static List<QAWrapper> finalBox = new ArrayList<>();

    public static boolean addQAIndex(int index) {
        if (getWrapperFromIndex(index) != null) {
            return false;
        }

        QAWrapper wrapper = new QAWrapper(index);
        currentBox.add(wrapper);

        return true;
    }

    public static int getRandomQAIndex() {
        int randomIndex = random.nextInt(currentBox.size());
        QAWrapper wrapper = currentBox.get(randomIndex);
        return wrapper.qaIndex;
    }

    public static boolean containsQAIndex(int index) {
        for (QAWrapper wrapper : currentBox) {
            if (wrapper.qaIndex == index) {
                return true;
            }
        }

        for (QAWrapper wrapper : reviseBox) {
            if (wrapper.qaIndex == index) {
                return true;
            }
        }

        for (QAWrapper wrapper : finalBox) {
            if (wrapper.qaIndex == index) {
                return true;
            }
        }

        return false;
    }

    public static int countQAIndexes() {
        return currentBox.size() + reviseBox.size() + finalBox.size();
    }

    public static int currentBoxCount() {
        return currentBox.size();
    }

    public static int reviseBoxCount() {
        return reviseBox.size();
    }

    public static int finalBoxCount() {
        return finalBox.size();
    }

    public static boolean firstBoxIsEmpty() {
        return currentBox.isEmpty();
    }

    private static QAWrapper getWrapperFromIndex(int index) {
        for (QAWrapper wrapper : currentBox) {
            if (wrapper.qaIndex == index) {
                return wrapper;
            }
        }
        return null;
    }

    public static void advanceQAIndex(int index) {
        QAWrapper wrapper = getWrapperFromIndex(index);
        if (wrapper == null) {
            System.out.println("Tried to advance null for index '" + String.valueOf(index) + "'");
            return;
        }
        currentBox.remove(wrapper);
        finalBox.add(wrapper);
    }

    public static void reviseQAIndex(int index) {
        QAWrapper wrapper = getWrapperFromIndex(index);
        if (wrapper == null) {
            System.out.println("Tried to revise null for index '" + String.valueOf(index) + "'");
            return;
        }
        currentBox.remove(wrapper);
        reviseBox.add(wrapper);
    }

    public static void advanceBoxes() {
        Iterator<QAWrapper> it;

        if (reviseBox.isEmpty()) {
            it = finalBox.iterator();
            while (it.hasNext()) {
                QAWrapper wrapper = it.next();
                currentBox.add(wrapper);
                it.remove();
            }
        } else {
            it = reviseBox.iterator();
            while (it.hasNext()) {
                QAWrapper wrapper = it.next();
                currentBox.add(wrapper);
                it.remove();
            }

            it = finalBox.iterator();
            while (it.hasNext()) {
                QAWrapper wrapper = it.next();
                reviseBox.add(wrapper);
                it.remove();
            }
        }
    }

    public static void print() {
        System.out.print("Current (" + currentBox.size() + "): ");
        for (QAWrapper wrapper : currentBox) {
            System.out.print(wrapper.qaIndex + ", ");
        }
        System.out.print("\n");

        System.out.print("Revise (" + reviseBox.size() + "): ");
        for (QAWrapper wrapper : reviseBox) {
            System.out.print(wrapper.qaIndex + ", ");
        }
        System.out.print("\n");

        System.out.print("Final (" + finalBox.size() + "): ");
        for (QAWrapper wrapper : finalBox) {
            System.out.print(wrapper.qaIndex + ", ");
        }
        System.out.print("\n");
    }
}

class TokenState {
    public static String HIDDEN = "HIDDEN";
    public static String HIGHLIGHTED = "HIGHLIGHTED";
    public static String SELECTED = "SELECTED";
    public static String CORRECT = "CORRECT";
    public static String INCORRECT = "INCORRECT";
}

class TokenType {
    public static String WORD = "WORD";
    public static String WHITESPACE = "WHITESPACE";
}

class Token {
    public String text;
    public String state;
    public String type;

    public Token(String tokenString) {
        text = tokenString;
        state = TokenState.HIDDEN;
    }
}

class AnswerTokenizer {
    public List<Token> list;

    public AnswerTokenizer(String answerString) {
        list = new ArrayList<>();

        boolean tokenTypeChanged = false;
        String prevTokenType = null;
        String curTokenType = null;

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < answerString.length(); i++) {
            char c = answerString.charAt(i);

            if (Character.isWhitespace(c)) {
                if (curTokenType != TokenType.WHITESPACE) {
                    prevTokenType = curTokenType;
                    curTokenType = TokenType.WHITESPACE;
                    tokenTypeChanged = true;
                }
            } else {
                if (curTokenType != TokenType.WORD) {
                    prevTokenType = curTokenType;
                    curTokenType = TokenType.WORD;
                    tokenTypeChanged = true;
                }
            }

            if (tokenTypeChanged) {
                if (buffer.length() != 0) {
                    Token token = new Token(buffer.toString());
                    if (prevTokenType == TokenType.WHITESPACE) {
                        token.type = TokenType.WHITESPACE;
                    } else if (prevTokenType == TokenType.WORD) {
                        token.type = TokenType.WORD;
                    }
                    list.add(token);

                    buffer.delete(0, buffer.length());
                }

                prevTokenType = null;
                tokenTypeChanged = false;
            }

            buffer.append(c);
        }

        if (buffer.length() != 0) {
            Token token = new Token(buffer.toString());
            if (curTokenType == TokenType.WHITESPACE) {
                token.type = TokenType.WHITESPACE;
            } else if (curTokenType == TokenType.WORD) {
                token.type = TokenType.WORD;
            }
            list.add(token);

            buffer.delete(0, buffer.length());
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

    private static boolean quickReview = true;
    private static boolean correctOnFirstTry = true;

    public static void main(String[] args) {
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

            Action spaceKeyAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleSpaceKey();
                }
            };

            Action returnKeyAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleReturnKey();
                }
            };

            KeyStroke spaceKey = KeyStroke.getKeyStroke("SPACE");
            KeyStroke returnKey = KeyStroke.getKeyStroke("ENTER");

            InputMap inputMap = answerArea.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap actionMap = answerArea.getActionMap();

            inputMap.put(spaceKey, "spaceKey");
            inputMap.put(returnKey, "returnKey");

            actionMap.put("spaceKey", spaceKeyAction);
            actionMap.put("returnKey", returnKeyAction);

            // Initialize QA's

            qaList = new QAList();
            qaList.load();

            for (int i = 0; i < 5; i++) {
                addRandomQAToFirstBox();
            }

            setQAWithIndex(QABoxes.getRandomQAIndex());
            viewCurrentQA();
        });
    }

    private static void viewCurrentQA() {
        questionArea.setText(String.valueOf(currentQAIndex + 1) + ". " + currentQuestion);

        try {
            StyledDocument doc = answerArea.getStyledDocument();
            Color darkBlue = new Color(0, 0, 139);

            Style regularStyle = answerArea.addStyle("RegularStyle", null);
            StyleConstants.setForeground(regularStyle, Color.BLUE);

            Style highlightedStyle = answerArea.addStyle("HighlightedStyle", null);
            StyleConstants.setBackground(highlightedStyle, darkBlue);

            Style selectedStyle = answerArea.addStyle("SelectedStyle", null);
            StyleConstants.setForeground(selectedStyle, Color.WHITE);
            StyleConstants.setBackground(selectedStyle, darkBlue);

            Style correctStyle = answerArea.addStyle("CorrectStyle", null);
            StyleConstants.setForeground(correctStyle, Color.GREEN);

            Style incorrectStyle = answerArea.addStyle("IncorrectStyle", null);
            StyleConstants.setForeground(incorrectStyle, Color.RED);

            doc.remove(0, doc.getLength());

            for (int i = 0; i < currentAnswerTokens.list.size(); i++) {
                Token token = currentAnswerTokens.list.get(i);

                if (token.type == TokenType.WHITESPACE) {
                    doc.insertString(doc.getLength(), token.text, regularStyle);
                } else {
                    String textToInsert = token.text;
                    Style style = regularStyle;

                    if (token.state == TokenState.HIDDEN) {
                        textToInsert = "_".repeat(token.text.length());
                    } else if (token.state == TokenState.HIGHLIGHTED) {
                        textToInsert = "_".repeat(token.text.length());
                        style = highlightedStyle;
                    } else if (token.state == TokenState.SELECTED) {
                        style = selectedStyle;
                    } else if (token.state == TokenState.CORRECT) {
                        style = correctStyle;
                    } else if (token.state == TokenState.INCORRECT) {
                        style = incorrectStyle;
                    }

                    doc.insertString(doc.getLength(), textToInsert, style);
                }
            }

            answerArea.grabFocus();
        } catch (BadLocationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setQAWithIndex(int index) {
        currentQAIndex = index;

        QA qa = qaList.getQA(currentQAIndex);
        currentQuestion = qa.getQuestion();
        currentAnswerTokens = new AnswerTokenizer(qa.getAnswer());

        highlightNextWordTokens(3);
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
        viewCurrentQA();
    }

    private static void handleNextButton() {
        incrementQAIndex(1);
        viewCurrentQA();
    }

    private static void handleRandomButton() {
        setRandomQA();
        viewCurrentQA();
    }

    private static void handleSpaceKey() {
        boolean selectNextTokens = false;

        for (int i = 0; i < currentAnswerTokens.list.size(); i++) {
            Token token = currentAnswerTokens.list.get(i);

            if (token.state == TokenState.HIGHLIGHTED) {
                token.state = TokenState.SELECTED;
            } else if (token.state == TokenState.SELECTED) {
                token.state = TokenState.INCORRECT;
                selectNextTokens = true;
            }

            /* DEBUG
            selectNextTokens = true;
            token.state = TokenState.CORRECT;
            correctOnFirstTry = false;
            */
        }

        if (selectNextTokens) {
            if (quickReview) {
                highlightNextWordTokens(3);
            } else {
                highlightNextWordTokens(1);
            }
        }

        viewCurrentQA();
    }

    private static void handleReturnKey() {
        boolean selectNextTokens = false;

        for (int i = 0; i < currentAnswerTokens.list.size(); i++) {
            Token token = currentAnswerTokens.list.get(i);

            if (token.state == TokenState.SELECTED) {
                token.state = TokenState.CORRECT;
                selectNextTokens = true;
            }

            /* DEBUG
            selectNextTokens = true;
            token.state = TokenState.CORRECT;
            */
        }

        if (selectNextTokens) {
            if (quickReview) {
                highlightNextWordTokens(3);
            } else {
                highlightNextWordTokens(1);
            }
        }

        viewCurrentQA();
    }

    private static void highlightNextWordTokens(int count) {
        int numWordTokens = 0;
        boolean encounteredWordToken = false;

        Iterator<Token> it = currentAnswerTokens.list.iterator();
        while (it.hasNext() && numWordTokens < count) {
            Token t = it.next();

            if (encounteredWordToken && t.text.equals("\n")) {
                break;
            }

            if (t.type == TokenType.WORD && t.state == TokenState.HIDDEN) {
                encounteredWordToken = true;
                t.state = TokenState.HIGHLIGHTED;
                numWordTokens++;
            }
        }

        if (numWordTokens == 0) {
            boolean anyIncorrectTokens = false;
            for (int i = 0; i < currentAnswerTokens.list.size(); i++) {
                Token token = currentAnswerTokens.list.get(i);

                if (token.state == TokenState.INCORRECT) {
                    anyIncorrectTokens = true;
                    token.state = TokenState.HIDDEN;
                }
            }

            if (anyIncorrectTokens) {
                quickReview = false;

                highlightNextWordTokens(1);
            } else {
                for (int i = 0; i < currentAnswerTokens.list.size(); i++) {
                    Token token = currentAnswerTokens.list.get(i);

                    if (token.state == TokenState.CORRECT) {
                        token.state = TokenState.HIDDEN;
                    }
                }

                if (quickReview) {
                    if (correctOnFirstTry) {
                        QABoxes.advanceQAIndex(currentQAIndex);
                    } else {
                        QABoxes.reviseQAIndex(currentQAIndex);
                    }
                    correctOnFirstTry = true;

                    boolean alreadyAdvancedBoxes = false;
                    if (QABoxes.currentBoxCount() < 5) {
                        boolean addSuccessful = addRandomQAToFirstBox();
                        if (!addSuccessful) {
                            if (QABoxes.firstBoxIsEmpty()) {
                                QABoxes.advanceBoxes();
                                alreadyAdvancedBoxes = true;
                            }
                        }
                    }

                    if (!alreadyAdvancedBoxes && QABoxes.reviseBoxCount() >= 10) {
                        QABoxes.advanceBoxes();
                    }

                    setQAWithIndex(QABoxes.getRandomQAIndex());
                } else {
                    // Reset the current question
                    setQAWithIndex(currentQAIndex);

                    correctOnFirstTry = false;
                    quickReview = true;
                }

                viewCurrentQA();

                System.out.println();
                System.out.println("===================================");
                System.out.println();
                QABoxes.print();
            }
        }
    }

    public static boolean addRandomQAToFirstBox() {
        if (QABoxes.countQAIndexes() == qaList.count()) {
            return false;
        }

        while (true) {
            int randomQAIndex = qaList.getRandomQAIndex();

            if (QABoxes.containsQAIndex(randomQAIndex)) {
                continue;
            }

            QABoxes.addQAIndex(randomQAIndex);

            return true;
        }
    }
}
