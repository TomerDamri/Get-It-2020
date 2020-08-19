package GetIt.model;

import javax.persistence.*;
import java.util.Map;

@Entity
public class TranscriptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ElementCollection
    private Map<Integer, String> transcript;

    public TranscriptEntity() {
    }

    public TranscriptEntity(Map<Integer, String> transcript) {
        this.transcript = transcript;
    }

    public Long getId() {
        return id;
    }

    public Map<Integer, String> getTranscript() {
        return transcript;
    }

    public void setTranscript(Map<Integer, String> transcript) {
        this.transcript = transcript;
    }
}
