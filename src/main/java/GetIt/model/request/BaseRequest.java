package GetIt.model.request;

public abstract class BaseRequest {
    private String expression;
    private String youtubeUrl;

    public String getExpression() {
        return expression;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }
}
