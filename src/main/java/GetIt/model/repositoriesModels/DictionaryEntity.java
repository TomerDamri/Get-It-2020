package GetIt.model.repositoriesModels;

import javax.persistence.*;
import java.util.Set;

@Entity
public class DictionaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ElementCollection
    private Set<String> dictionary;

    public DictionaryEntity() {
    }

    public DictionaryEntity(Set<String> dictionary) {
        this.dictionary = dictionary;
    }

    public Long getId() {
        return id;
    }

    public Set<String> getDictionary() {
        return dictionary;
    }

    public void setDictionary(Set<String> dictionary) {
        this.dictionary = dictionary;
    }
}
