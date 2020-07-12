package GetIt.model.response;

import java.util.List;

public class GetOccurrencesResponse {
    private List<Integer> occurrences;

    public GetOccurrencesResponse(List<Integer> occurrences) {
        this.occurrences = occurrences;
    }

    public List<Integer> getOccurrences() {
        return occurrences;
    }


}
