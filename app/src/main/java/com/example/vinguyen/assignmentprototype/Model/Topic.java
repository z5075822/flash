package com.example.vinguyen.assignmentprototype.Model;

public class Topic {
    private String topicID;
    private String title;
    private String QuestionDoc;
    private String questionImage;

    public Topic() {
    }

    public Topic(String topicID, String title, String QuestionDoc, String questionImage) {
        this.topicID = topicID;
        this.title = title;
        this.QuestionDoc = QuestionDoc;
        this.questionImage = questionImage;
    }

    public String getTopicID() {
        return topicID;
    }

    public void setTopicID(String topicID) {
        this.topicID = topicID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestionDoc() {
        return QuestionDoc;
    }

    public void setQuestionDoc(String questionDoc) {
        QuestionDoc = questionDoc;
    }

    public String getQuestionImage() {
        return questionImage;
    }

    public void setQuestionImage(String questionImage) {
        this.questionImage = questionImage;
    }
}
