package com.example.vinguyen.assignmentprototype.Model;

public class Question {
    private String option1, option2, option3, answer, question;

    public Question(String option1, String option2, String option3, String answer, String question) {
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.answer = answer;
        this.question = question;
    }

    public Question() {
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
