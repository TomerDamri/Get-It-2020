package GetIt.model.request;

public class UpdateTranscriptRequest extends BaseRequest {
    private Integer timeSlot;
    private String fixedExpression;

    public void setTimeSlot(Integer timeSlot) {
        this.timeSlot = timeSlot;
    }

    public void setFixedExpression(String fixedExpression) {
        this.fixedExpression = fixedExpression;
    }

    public Integer getTimeSlot() {
        return timeSlot;
    }

    public String getFixedExpression() {
        return fixedExpression;
    }

}
