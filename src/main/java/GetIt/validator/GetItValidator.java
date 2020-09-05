package GetIt.validator;

import GetIt.exceptions.EmptyExpressionException;
import GetIt.exceptions.EmptyYoutubeUrlException;
import GetIt.exceptions.InvalidTimeSlotException;
import GetIt.model.request.*;
import org.springframework.stereotype.Component;

@Component
public class GetItValidator {

    public void validateGetTyposRequest(GetTyposRequest request) {
        validateBaseRequest(request);
    }

    public void validateUpdateTranscriptRequest(UpdateTranscriptRequest request) {
        validateBaseRequest(request);
        if (request.getTimeSlot() == null || request.getTimeSlot() < 0) {
            throw new InvalidTimeSlotException("You have to request a positive time slot");
        }
        validateExpression(request.getExpression());
    }

    public void validateGetOccurrencesRequest(GetOccurrencesRequest request) {
        validateBaseRequest(request);
    }

    public void validateGetTranscriptRequest(GetTranscriptRequest request) {
        validateYoutubeUrl(request.getYoutubeUrl());
    }

    public void validateBaseRequest(BaseRequest request) {
        validateYoutubeUrl(request.getYoutubeUrl());
        validateExpression(request.getExpression());
    }

    private void validateYoutubeUrl(String youtubeUrl) {
        if (isEmpty(youtubeUrl)) {
            throw new EmptyYoutubeUrlException("You have to request a non-empty youtube url");
        }
    }

    private void validateExpression(String expression) {
        if (isEmpty(expression)) {
            throw new EmptyExpressionException("You have to request a non-empty expression");
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
