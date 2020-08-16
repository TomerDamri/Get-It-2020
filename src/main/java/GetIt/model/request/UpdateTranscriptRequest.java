package GetIt.model.request;

public class UpdateTranscriptRequest {
    private String youtubeUrl;
    private Integer timeSlots;
    private String oldSentence;
    private String fixedSentence;

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public void setTimeSlots(Integer timeSlots) {
        this.timeSlots = timeSlots;
    }

    public void setOldSentence(String oldSentence) {
        this.oldSentence = oldSentence;
    }

    public void setFixedSentence(String fixedSentence) {
        this.fixedSentence = fixedSentence;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public Integer getTimeSlots() {
        return timeSlots;
    }

    public String getOldSentence() {
        return oldSentence;
    }

    public String getFixedSentence() {
        return fixedSentence;
    }

}
