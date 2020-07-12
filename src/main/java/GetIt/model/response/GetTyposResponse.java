package GetIt.model.response;

import java.util.List;

public class GetTyposResponse {
    private List<String> typos;

    public GetTyposResponse(List<String> typos) {
        this.typos = typos;
    }

    public List<String> getTypos() {
        return typos;
    }
}
