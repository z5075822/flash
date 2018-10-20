package com.example.vinguyen.assignmentprototype.Model;

public class Topic {
    private String topicID;
    private String title;
    private String content;

    public Topic() {
    }

    public Topic(String topicID, String title, String content) {
        this.topicID = topicID;
        this.title = title;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
